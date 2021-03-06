package net.objectof.actof.repospy.controllers.navigator.editor.layout;


import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import net.objectof.actof.repospy.RepoSpyController;
import net.objectof.actof.repospy.controllers.navigator.treemodel.RepoSpyTreeItem;
import net.objectof.actof.repospy.controllers.navigator.treemodel.TreeNode;
import net.objectof.actof.widgets.masonry.MasonryPane;
import net.objectof.actof.widgets.masonry.MasonryPane.Layout;


public abstract class AbstractLayout extends BorderPane {

    protected MasonryPane cards;
    protected ScrollPane scroll = new ScrollPane(cards);
    protected RepoSpyController repospy;
    protected RepoSpyTreeItem treeitem;

    public AbstractLayout(RepoSpyTreeItem treeitem, RepoSpyController repospy, double columnWidth) {
        this.cards = new MasonryPane(columnWidth, Layout.SHORTEST_COLUMN);
        cards.setPadding(new Insets(10));
        cards.setSpacing(10);
        this.repospy = repospy;
        this.treeitem = treeitem;

        TreeNode treenode = treeitem.getValue();
        treenode.getLeaves().addListener((Observable obs) -> updateUI());
        treeitem.getChildren().addListener((Observable obs) -> updateUI());

        scroll.setStyle("-fx-background-color: transparent;");
        scroll.setFitToWidth(true);
        scroll.setContent(cards);

        setCenter(scroll);

    }

    protected abstract void updateUI();

}
