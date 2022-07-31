package com.lxx.neo4j;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

public class ExtractTemporalData {
    @Context
    public GraphDatabaseService graphDb;

    @Procedure(mode = Mode.WRITE)
    @Description("com.lxx.neo4j.toTemporalVertex(relationship, degreeType, dimensionType)")
    public void toTemporalVertex(@Name("relationship") Relationship relationship,
                                 @Name(value = "degreeType", defaultValue = "IN") String degreeType,
                                 @Name(value = "dimensionType", defaultValue = "VALID_TIME") String dimensionType) {

        Long from = (dimensionType.equals("VALID_TIME")) ? Long.valueOf(relationship.getProperty("val_from").toString()) : Long.valueOf(relationship.getProperty("tx_from").toString());
        Long to = (dimensionType.equals("VALID_TIME")) ? Long.valueOf(relationship.getProperty("val_to").toString()) : Long.valueOf(relationship.getProperty("tx_to").toString());

        switch (degreeType) {
            case "IN":
                try (Transaction tx = graphDb.beginTx()) {
                    Node nodeIN = tx.createNode(Label.label("vertex"));
                    nodeIN.setProperty("vid", relationship.getEndNode().getProperty("id"));
                    nodeIN.setProperty("from", from);
                    nodeIN.setProperty("to", to);
                    tx.commit();
                }
                break;

            case "OUT":
                try (Transaction tx = graphDb.beginTx()) {
                    Node nodeOUT = tx.createNode(Label.label("vertex"));
                    nodeOUT.setProperty("vid", relationship.getStartNode().getProperty("id"));
                    nodeOUT.setProperty("from", from);
                    nodeOUT.setProperty("to", to);
                    tx.commit();
                }
                break;

            case "BOTH":
                try (Transaction tx = graphDb.beginTx()) {
                    Node node_1 = tx.createNode(Label.label("vertex"));
                    node_1.setProperty("vid", relationship.getEndNode().getProperty("id"));
                    node_1.setProperty("from", from);
                    node_1.setProperty("to", to);

                    Node node_2 = tx.createNode(Label.label("vertex"));
                    node_2.setProperty("vid", relationship.getStartNode().getProperty("id"));
                    node_2.setProperty("from", from);
                    node_2.setProperty("to", to);
                    tx.commit();
                }
                break;

            default:
                throw new IllegalArgumentException(degreeType + " is not valid");
        }
    }
}