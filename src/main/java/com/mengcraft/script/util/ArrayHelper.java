package com.mengcraft.script.util;

import com.google.common.collect.ImmutableList;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created on 16-10-28.
 */
public final class ArrayHelper {

    public interface Helper {
        Object toScriptArray(Object input);
    }

    private static Helper helper;

    static {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        try {
            engine.eval("" +
                    "function toScriptArray(input) {\n" +
                    "    return Array.prototype.slice.call(input);\n" +
                    "}");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        helper = Invocable.class.cast(engine).getInterface(Helper.class);
    }

    private ArrayHelper() {
    }

    public static <T> List<T> link(T... in) {
        List<T> list = new LinkedList<>();
        for (T i : in) {
            list.add(i);
        }
        return list;
    }

    public static Object toScriptArray(Iterator<String> input) {
        ImmutableList.Builder<String> b = ImmutableList.builder();
        input.forEachRemaining(b::add);
        ImmutableList<String> list = b.build();
        int l = list.size();
        String[] out = new String[l];
        return toScriptArray(list.toArray(out));
    }

    public static Object toScriptArray(String[] input) {
        return helper.toScriptArray(input);
    }

}
