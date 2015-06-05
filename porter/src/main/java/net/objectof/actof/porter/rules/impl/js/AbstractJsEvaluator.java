package net.objectof.actof.porter.rules.impl.js;


import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;


public class AbstractJsEvaluator {

    protected String js;
    protected String input;

    public AbstractJsEvaluator(String js) {
        this.js = js;
        this.input = null;
    }

    public AbstractJsEvaluator(String js, String input) {
        this.js = js;
        this.input = input;
    }

    @SuppressWarnings("restriction")
    protected ScriptObjectMirror getFunction() throws ScriptException {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("nashorn");
        engine.getBindings(ScriptContext.ENGINE_SCOPE).put("input", input);
        ScriptObjectMirror fn = (ScriptObjectMirror) engine.eval(js);
        return fn;
    }

}
