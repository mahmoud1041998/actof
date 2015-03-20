package net.objectof.actof.repospy.controllers.review;


import java.net.URL;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import net.objectof.actof.common.controller.change.ChangeController;
import net.objectof.actof.common.icons.ActofIcons;
import net.objectof.actof.common.icons.ActofIcons.Icon;
import net.objectof.actof.common.icons.ActofIcons.Size;
import net.objectof.actof.common.util.RepoUtils;
import net.objectof.actof.repospy.changes.EditingChange;
import net.objectof.actof.repospy.changes.FieldChange;
import net.objectof.actof.repospy.changes.HistoryChange;
import net.objectof.actof.repospy.controllers.history.HistoryController;
import net.objectof.actof.widgets.PropertiesPane;
import net.objectof.actof.widgets.card.Card;
import net.objectof.actof.widgets.card.CardsPane;


public class ReviewController extends CardsPane {

    private HistoryController history;

    public ReviewController(HistoryController history, ChangeController changes) {

        URL css = ReviewController.class.getResource("style.css");
        getStylesheets().add(css.toString());
        this.history = history;

        changes.listen(HistoryChange.class, this::updateUI);
        updateUI();
    }

    private void updateUI() {

        getChildren().clear();
        for (EditingChange change : history.getChanges()) {

            Card card = new Card();
            card.setShadowRadius(5);
            card.setShadowOffsetY(0.7d);
            card.setPadding(new Insets(3));
            card.setTitle(change.getName());
            card.setDescription(RepoUtils.prettyPrint(change.getStereotype()));

            if (change instanceof FieldChange && !RepoUtils.isAggregateStereotype(change.getStereotype())) {

                Node desc = card.getDescription();
                AnchorPane.setTopAnchor(desc, 0d);
                AnchorPane.setBottomAnchor(desc, 0d);
                AnchorPane.setLeftAnchor(desc, 0d);
                AnchorPane.setRightAnchor(desc, 0d);
                desc = new AnchorPane(desc);

                Button revert = new Button("", ActofIcons.getIconView(Icon.UNDO, Size.BUTTON));
                revert.getStyleClass().add("tool-bar-button");
                revert.setOnAction(event -> {
                    if (change instanceof FieldChange) {
                        ((FieldChange) change).undo();;
                    }
                });

                HBox box = new HBox(10, desc, revert);
                card.setDescription(box);
            }

            PropertiesPane fields = new PropertiesPane();
            fields.setPadding(new Insets(0, 0, 0, 20));
            fields.addProperty("Old", RepoUtils.prettyPrint(change.oldValue()));
            fields.addProperty("New", RepoUtils.prettyPrint(change.newValue()));
            card.setContent(fields);

            getChildren().add(card);
        }
    }

}
