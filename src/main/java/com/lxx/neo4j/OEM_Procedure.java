package com.lxx.neo4j;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.util.*;

// Procedures can take arguments, perform operations on the database, and return results.
public class OEM_Procedure {
    @Context public GraphDatabaseService graphDb;

    @Procedure(mode = Mode.WRITE)
    @Description("com.lxx.neo4j.calDegree(Path)")
    public void calDegree(@Name("nodesList") List<Node> nodes, @Name("vertexId") String id){

        int degree = 0;
        Long lastTimestamp = Long.MIN_VALUE;
        Long vertexToTime = Long.MAX_VALUE;
        //String id = p.startNode().getProperty("vid").toString();

        for (Node node : nodes) {
            Long timestamp = Long.valueOf(node.getProperty("timestamp").toString());
            int degree_of_timestamp = Integer.parseInt(node.getProperty("Degree").toString());

            // check integrity
            if (lastTimestamp > timestamp) {
                // This should not happen, seems that a temporal constraint is violated
                throw new IllegalArgumentException("\n!!ERROR!!\nlastTimestamp > entry.getKey()\n");
            }
            if (lastTimestamp.equals(timestamp)) {
                // First timestamp in tree is equal to the lower interval bound of the vertex
                degree += degree_of_timestamp;
                continue;
            }
            // The payload is 0, means the degree does not change and the intervals can be merged
            if (degree_of_timestamp != 0) {
                try(Transaction tx = graphDb.beginTx()) {
                    //collector.collect(new Tuple4<>(vertexId, lastTimestamp, entry.getKey(), degree));
                    Node node_temp = tx.createNode(Label.label("TVD"));
                    node_temp.setProperty("vid", id);
                    node_temp.setProperty("from", lastTimestamp);
                    node_temp.setProperty("to", timestamp);
                    node_temp.setProperty("degree", degree);
                    tx.commit();
                }
                degree += degree_of_timestamp;
                // remember the last timestamp since it is the first one of the next interval
                lastTimestamp = timestamp;
            }
        }

        // last degree is 0 from last occurence of timestamp to t_to(v)
        if (lastTimestamp < vertexToTime) {
            try(Transaction tx = graphDb.beginTx()) {
                //collector.collect(new Tuple4<>(vertexId, lastTimestamp, vertexToTime, degree));
                Node node_temp = tx.createNode(Label.label("TVD"));
                node_temp.setProperty("vid", id);
                node_temp.setProperty("from", lastTimestamp);
                node_temp.setProperty("to", vertexToTime);
                node_temp.setProperty("degree", degree);
                tx.commit();
            }
        } else if (lastTimestamp > vertexToTime) {
            // This should not happen, seems that a temporal constraint is violated
            throw new IllegalArgumentException("\n!!ERROR!! \nlastTimestamp > vertexToTime\n");
        } // else, the ending bound of the vertex interval equals the last timestamp of the edges
    }
}
