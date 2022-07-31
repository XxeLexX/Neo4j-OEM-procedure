package com.lxx.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.*;

public class ExtractFromCSV {
    @Context
    public GraphDatabaseService graphDb;

    @Procedure(mode = Mode.WRITE)
    @Description("com.lxx.neo4j.createGraphFromCSV(inputPath,degreeType,dimensionType)")
    public void createGraphFromCSV(@Name("inputPath") String inputPath,
                                   @Name(value = "degreeType", defaultValue = "IN") String degreeType,
                                   @Name(value = "dimensionType", defaultValue = "VALID_TIME") String dimensionType) {
        // read csv to graph
        String read = "USING PERIODIC COMMIT 1000\n" +
                "LOAD CSV FROM " + inputPath + " AS line\n" + "FIELDTERMINATOR ';'\n" +
                "WITH line LIMIT 10000\n" +
                "WITH line, split(line[6], \"),(\") AS time_all\n" +
                "WITH line, split(time_all[1], ',') AS valid, split(time_all[0], ',') AS tx\n" +
                "WITH line, valid[0] AS v_from, replace(valid[1], ')', '') AS v_to, replace(tx[0], '(', '') AS t_from, tx[1] AS t_to\n" +
                "CREATE (a: Station {id: line[2]})\n" +
                "CREATE (b: Station {id: line[3]})\n" +
                "CREATE (a)-[:Trip {val_from: v_from, val_to: v_to, tx_from: t_from, tx_to: t_to}]->(b)";
        graphDb.executeTransactionally(read);
        mergeNodes();
    }

    private void mergeNodes(){
        try(Transaction tx = graphDb.beginTx()){
            String merge = "MATCH (n:Station)\n" +
                    "WITH n.id AS id, collect(n) AS ns\n" +
                    "CALL apoc.refactor.mergeNodes(ns)\n" +
                    "YIELD node\n" +
                    "RETURN node";
            tx.execute(merge);
            tx.commit();
        }
    }
}
