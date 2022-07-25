package com.lxx.neo4j;

import org.neo4j.graphdb.Path;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

// !! READ ONLY !! Can not edit anything in the current database. Need change to a procedure...
public class DegreeCalculate {
    @UserFunction
    @Description("com.lxx.neo4j.getDegree( Path p=()-[]-() )")
    public Path getDegree(@Name("path") Path p){
        // 1) get Path p = (:VertexDegree)-[:DEGREE]-(:timeDegree)
        // 2) loop(timeDegree)
        // 2.1) initial Degree = 0 & lastTimestamp = LONG_MIN_VALUE & MaxTimestamp = LONG_MAX_VALUE
        // 2.2) get timeDegree.timestamp & Degree
        // 2.3) do the job...
        // ...
        return p;
    }

}
