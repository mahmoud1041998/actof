package net.objectof.actof.porter.rules;


import java.util.function.BiConsumer;

import net.objectof.actof.porter.rules.components.Matcher;
import net.objectof.actof.porter.rules.components.Transformer;
import net.objectof.actof.porter.rules.components.impl.IKeyMatcher;
import net.objectof.actof.porter.rules.components.impl.IKindMatcher;
import net.objectof.actof.porter.rules.components.impl.IKindNameMatcher;
import net.objectof.actof.porter.rules.components.impl.IPrettyPrintMatcher;
import net.objectof.actof.porter.rules.components.impl.IPrettyPrintTransformer;
import net.objectof.actof.porter.rules.components.impl.IStereotypeMatcher;
import net.objectof.actof.porter.visitor.IPorterContext;
import net.objectof.model.Kind;
import net.objectof.model.Stereotype;


public class RuleBuilder {

    IRule rule = new IRule();

    private RuleBuilder() {}

    public static RuleBuilder start() {
        return new RuleBuilder();
    }

    public Rule build() {
        return rule;
    }

    public RuleBuilder forKey(Object... key) {
        rule.getMatchers().add(new IKeyMatcher(key));
        return this;
    }

    public RuleBuilder forKind(String kind) {
        rule.getMatchers().add(new IKindNameMatcher(kind));
        return this;
    }

    public RuleBuilder forKind(Kind<?> kind) {
        rule.getMatchers().add(new IKindMatcher(kind));
        return this;
    }

    public RuleBuilder forStereotype(Stereotype... stereotype) {
        rule.getMatchers().add(new IStereotypeMatcher(stereotype));
        return this;
    }

    public RuleBuilder drop() {
        rule.getKeyTransformers().add(context -> {
            context.setDropped(true);
            return null;
        });
        return this;
    }

    public RuleBuilder setKey(Object newKey) {
        rule.getKeyTransformers().add(context -> newKey);
        return this;
    }

    public RuleBuilder setValue(Object newValue) {
        rule.getValueTransformers().add(context -> newValue);
        return this;
    }

    // /////////////////////////////
    // Generic
    // /////////////////////////////

    public RuleBuilder match(Matcher matcher) {
        rule.getMatchers().add(new IPrettyPrintMatcher(matcher));
        return this;
    }

    public RuleBuilder keyTransform(Transformer robotInDisguise) {
        rule.getKeyTransformers().add(new IPrettyPrintTransformer(robotInDisguise));
        return this;
    }

    public RuleBuilder valueTransform(Transformer robotInDisguise) {
        rule.getValueTransformers().add(new IPrettyPrintTransformer(robotInDisguise));
        return this;
    }

    public RuleBuilder onPort(BiConsumer<IPorterContext, IPorterContext> listener) {
        rule.getOnPortListeners().add(listener);
        return this;
    }

}