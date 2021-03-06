package net.objectof.actof.porter;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.objectof.actof.porter.rules.Rule;
import net.objectof.actof.porter.visitor.IMigrationVisitor;
import net.objectof.actof.porter.visitor.IResourceUpdateVisitor;
import net.objectof.actof.porter.visitor.IWalkVisitor;
import net.objectof.actof.porter.visitor.Visitor;
import net.objectof.actof.porter.walker.IWalker;
import net.objectof.actof.porter.walker.Walker;
import net.objectof.model.Id;
import net.objectof.model.Kind;
import net.objectof.model.Package;
import net.objectof.model.Resource;
import net.objectof.model.Transaction;


public class Porter {

    private List<Rule> rules = new ArrayList<>();
    private Map<Id<?>, Id<?>> idmap = new HashMap<>();
    private Map<String, List<Resource<?>>> transients = new HashMap<>();

    public Porter() {}

    public Porter(Rule... rule) {
        rules.addAll(Arrays.asList(rule));
    }

    public void walk(Package pkg) {
        idmap.clear();
        Transaction tx = pkg.connect(getClass());

        // only pass needed -- we don't need to update resources to new repo,
        // since this is all done in a single repo
        Walker walker = new IWalker(tx);
        Visitor visitor = new IWalkVisitor(this, tx);
        visitor.setWalker(walker);
        walker.setVisitor(visitor);
        walker.walk();

        tx.post();
    }

    public void port(Package from, Package to) {

        idmap.clear();
        Transaction fromTx = from.connect(getClass());
        Transaction toTx = to.connect(getClass());

        // first pass
        Walker walker = new IWalker(fromTx);
        Visitor visitor = new IMigrationVisitor(this, fromTx, toTx);
        visitor.setWalker(walker);
        walker.setVisitor(visitor);
        walker.walk();

        // second pass
        Visitor updater = new IResourceUpdateVisitor(this, toTx);
        updater.setWalker(walker);
        walker.setVisitor(updater);
        walker.setTx(toTx);
        walker.walk();

        toTx.post();

    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public void setRules(Rule... rules) {
        this.rules.clear();
        addRules(rules);
    }

    public void addRules(Rule... rules) {
        this.rules.addAll(Arrays.asList(rules));
    }

    public Map<Id<?>, Id<?>> getIdmap() {
        return idmap;
    }

    public void setIdmap(Map<Id<?>, Id<?>> idmap) {
        this.idmap = idmap;
    }

    public Map<String, List<Resource<?>>> getTransients() {
        return transients;
    }

    public void setTransients(Map<String, List<Resource<?>>> transients) {
        this.transients = transients;
    }

    public void addTransient(String kind, Resource<?> res) {
        List<Resource<?>> created;
        if (!transients.containsKey(kind)) {
            transients.put(kind, new ArrayList<>());
        }
        created = transients.get(kind);
        created.add(res);
    }

    /**
     * Checks if the given object is a resource from the old repo. If it is, it
     * looks up the corresponding resource in the new repo in the idmap, and
     * returns it. This is useful for when a user-generated rule returns a value
     * from the old repo.
     * 
     * The PorterContext supplied to this method shold have a ported
     * {@link Kind}
     * 
     * @param context
     * @param ref
     * @return
     */
    public Object updateReference(Kind<?> kind, Transaction tx, Object object) {
        if (!IPorterUtil.isResourceStale(tx, object)) { return object; }
        Resource<Object> res = (Resource<Object>) object;
        Id<?> oldId = res.id();
        return fetch(oldId, tx, IPorterUtil.kindName(kind));
    }

    private Resource<Object> fetch(Id<?> fromId, Transaction toTx, String kind) {
        if (!idmap.containsKey(fromId)) {
            create(fromId, toTx, kind);
        }
        Id<?> toId = idmap.get(fromId);
        return toTx.retrieve(toId);
    }

    private Resource<Object> create(Id<?> fromId, Transaction toTx, String kind) {
        Resource<Object> newValue = toTx.create(kind);
        idmap.put(fromId, newValue.id());
        addTransient(kind, newValue);
        return newValue;
    }

}
