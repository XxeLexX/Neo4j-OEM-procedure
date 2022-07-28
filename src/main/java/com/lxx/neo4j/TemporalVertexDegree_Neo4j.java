package com.lxx.neo4j;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

public class TemporalVertexDegree_Neo4j {
    @Context
    public GraphDatabaseService graphDb;

    @Procedure(mode = Mode.WRITE)
    @Description("com.lxx.neo4j.toTemporalVertexDegree(Path, degreeType, dimensionType)")
    public void toTemporalVertexDegree(@Name("inputPath") String inputPath,
                                       @Name(value = "degreeType", defaultValue = "BOTH") String degreeType,
                                       @Name(value = "dimensionType", defaultValue = "VALID_TIME") String dimensionType)
    {
        // read csv to graph that only contains IDs and timeinterval
        csvToNodes(inputPath);
        createTemporalNodes(degreeType, dimensionType);
        toVertexDegree(degreeType);
    }

    private void csvToNodes(String inputPath){
        try(Transaction tx = graphDb.beginTx()){
            String read = "LOAD CSV FROM " + inputPath + " AS line\n" + "FIELDTERMINATOR ';'\n" +
                          "WITH line, split(line[6], \"),(\") AS time_all\n" +
                          "WITH line, split(time_all[1], ',') AS valid, split(time_all[0], ',') AS tx\n" +
                          "WITH line, valid[0] AS v_from, replace(valid[1], ')', '') AS v_to, replace(tx[0], '(', '') AS t_from, tx[1] AS t_to\n" +
                          "CREATE (:temporalEdges {sid:line[2], tid: line[3], valid_from: v_from, valid_to: v_to, tx_from: t_from, tx_to: t_to})";
            tx.execute(read);
            tx.commit();
        }
    }

    private void createTemporalNodes(String degreeType, String dimensionType){
        try(Transaction tx = graphDb.beginTx()){
            String match = "MATCH (n:temporalEdges)\n";
            String createB = (dimensionType.equals("VALID_TIME"))? "CREATE (b: time {from: n.valid_from, to: n.valid_to})\n" : "CREATE (b: time {from: n.tx_from, to: n.tx_to})\n";
            String createR = "CREATE (a) <-[:" + degreeType +"] -(b)";

            switch (degreeType) {
                case "IN": {
                    String creatNodes = match + "CREATE (a: vertex {VertexId: n.tid})\n" + createB + createR;
                    tx.execute(creatNodes);
                    break;
                }
                case "OUT": {
                    String creatNodes = match + "CREATE (a: vertex {VertexId: n.sid})\n" + createB + createR;
                    tx.execute(creatNodes);
                    break;
                }
                case "BOTH":
                    String creatNodesIN = match + "CREATE (a: vertex {VertexId: n.tid})\n" + createB + createR;
                    tx.execute(creatNodesIN);
                    String creatNodesOUT = match + "CREATE (a: vertex {VertexId: n.sid})\n" + createB + createR;
                    tx.execute(creatNodesOUT);
                    break;
                default:
                    throw new IllegalArgumentException("Arguments are not valid");
            }
            tx.commit();
        }
    }

    private void toVertexDegree(String degreeType){
        try(Transaction tx = graphDb.beginTx()){
            String getVertexDegree = "MATCH p=(n:vertex)<-[r:" + degreeType + "]-(m:time)\n" +
                                     "WITH n.VertexId AS id, m.from AS FROM, count(m.from) AS FROM_D,m.to AS TO, -count(m.to) AS TO_D\n" +
                                     "CREATE (:vertexDegree {vid: id})-[:DEGREE]->(:timeDegree {timestamp: FROM, Degree:FROM_D})\n" +
                                     "CREATE (:vertexDegree {vid: id})-[:DEGREE]->(:timeDegree {timestamp: TO, Degree:TO_D})\n";
            tx.execute(getVertexDegree);

            String mergeVD = "MATCH (n:vertexDegree)\n" +
                             "WITH n.vid AS id, collect(n) as tvd\n" +
                             "CALL apoc.refactor.mergeNodes(tvd)\n" +
                             "YIELD node\n";
                            //+"RETURN node";

            String toTVD = "MATCH (n:vertexDegree)-[:DEGREE]-(m:timeDegree)\n" +
                           "WITH n,m\n" +
                           "ORDER BY m.timestamp\n" +
                           "WITH collect(m) AS ns, n.vid AS id\n" +
                           "CALL com.lxx.neo4j.calDegree(ns,id)";
            tx.execute( mergeVD + toTVD);
            tx.commit();
        }
    }
}
