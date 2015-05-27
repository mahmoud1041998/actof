package net.objectof.actof.porter.visitor;


import net.objectof.actof.porter.Porter;
import net.objectof.actof.porter.PorterContext;
import net.objectof.aggr.Aggregate;
import net.objectof.model.Resource;
import net.objectof.model.Stereotype;


/**
 * Visitor for migrating old repo tree to new repo tree
 * 
 * @author NAS
 *
 */
public class MigrationVisitor implements Visitor {

    public PorterContext visitContainer(Porter porter, PorterContext context,
            Resource<Aggregate<Object, Object>> toParent) {

        PorterContext ported = porter.transform(context);
        if (ported.isDropped()) { return ported; }

        if (ported.getKind().getStereotype() == Stereotype.REF) {
            // there's a chance that the user passed us a reference to something
            // in the old repo.
            porter.runLater(() -> {
                if (toParent == null) { return; }
                toParent.value().set(porter.unqualify(ported.getKey(), toParent), ported.getValue());
            });
        } else {
            if (toParent == null) { return ported; }
            toParent.value().set(porter.unqualify(ported.getKey(), toParent), ported.getValue());
        }

        return ported;

    }

    public PorterContext visitLeaf(Porter porter, PorterContext context, Resource<Aggregate<Object, Object>> toParent) {
        PorterContext ported = porter.transform(context);
        if (ported.isDropped()) { return ported; }
        toParent.value().set(porter.unqualify(ported.getKey(), toParent), ported.getValue());
        return ported;
    }

}
