package com.mengcraft.script.util;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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

    public static Object toScriptArray(Object input) {
        return helper.toScriptArray(input);
    }

}
