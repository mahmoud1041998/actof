package net.objectof.actof.repospy.controllers.navigator.treemodel;


import java.util.List;

import net.objectof.actof.repospy.RepoSpyController;
import net.objectof.model.Resource;
import net.objectof.model.Stereotype;


public interface TreeNode {

    public boolean hasChildren();

    public List<KindTreeItem> getChildren(RepoSpyController repospy);

    public String getEntityKind();

    public Stereotype getStereotype();

    public Resource<?> getRes();

}