package com.lxx.neo4j;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

public class TransforToTVD {
    @Context
    public GraphDatabaseService graphDb;

    @Procedure(mode = Mode.WRITE)
    @Description("com.lxx.neo4j.toTemporalVertexDegree()")
    public void toTemporalVertexDegree() {
        getVertexDegree();
        mergeVD();
        toTVD();
    }

    private void getVertexDegree(){
        try (Transaction tx = graphDb.beginTx()){
            String getVertexDegree = "MATCH (n:vertex)\n" +
                    "WITH n.vid AS id, n.from AS FROM, count(n.from) AS FROM_D, n.to AS TO, -count(n.to) AS TO_D\n" +
                    "CREATE (a :vertexDegree {vid: id})\n" +
                    "CREATE (b: timeDegree {timestamp: FROM, Degree:FROM_D})\n" +
                    "CREATE (a)-[:DEGREE]->(b)\n" +
                    "CREATE (m :vertexDegree {vid: id})\n" +
                    "CREATE (n: timeDegree {timestamp: TO, Degree:TO_D})\n" +
                    "CREATE (m)-[:DEGREE]->(n)";
            tx.execute(getVertexDegree);
            tx.commit();
        }
    }

    private void mergeVD(){
        try (Transaction tx = graphDb.beginTx()){
            String mergeVD = "MATCH (n:vertexDegree)\n" +
                    "WITH n.vid AS id, collect(n) as tvd\n" +
                    "CALL apoc.refactor.mergeNodes(tvd)\n" +
                    "YIELD node\n" +
                    "RETURN node";
            tx.execute(mergeVD);
            tx.commit();
        }
    }

    private void toTVD(){
        try (Transaction tx = graphDb.beginTx()){
            String toTVD = "MATCH (a:vertexDegree)-[:DEGREE]->(b:timeDegree)\n" +
                    "WITH a,b\n" +
                    "ORDER BY b.timestamp\n" +
                    "WITH collect(b) AS ns, a.vid AS id\n" +
                    "CALL com.lxx.neo4j.calDegree(ns,id)";
            tx.execute(toTVD);
            tx.commit();
        }
    }
}
