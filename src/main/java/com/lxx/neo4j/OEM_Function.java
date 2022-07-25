package com.lxx.neo4j;

import java.util.List;

import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

/**
 * 将字符串数组 用 指定的字符串联起来，形成一个字符串
 * 用法1
 * RETURN com.lxx.neo4j.join(['A','quick','brown','fox'],'-') as sentence
 *
 * 用法2
 * match (n:Person)
 *with collect(n.name) as namearry
 *return com.lxx.neo4j.join(namearry,'-') as sentence
 */
public class OEM_Function
{
    @UserFunction
    @Description("com.lxx.neo4j.join(['s1','s2',...], delimiter)")
    public String join(
            @Name("strings") List<String> strings,
            @Name(value = "delimiter", defaultValue = ",") String delimiter) {
        if (strings == null || delimiter == null) {
            return null;
        }
        return String.join(delimiter, strings);
    }

}
