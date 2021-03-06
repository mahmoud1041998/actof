package net.objectof.actof.common.component.editor.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import net.objectof.actof.common.component.display.Panel;
import net.objectof.actof.common.component.editor.Editor;
import net.objectof.actof.common.component.feature.FXLoaded;
import net.objectof.actof.common.component.feature.FXRegion;
import net.objectof.actof.common.component.feature.StageProperty;
import net.objectof.actof.common.component.feature.Titled;
import net.objectof.actof.common.component.resource.Action;
import net.objectof.actof.common.component.resource.Resource;
import net.objectof.actof.common.icons.ActofIcons;
import net.objectof.actof.common.util.FXUtil;


public class ResourceView implements Titled, FXRegion, FXLoaded, StageProperty {

    @FXML
    private BorderPane panel, topPane, displayPanel;
    @FXML
    private TabPane panels;
    @FXML
    private HBox toolbar;
    @FXML
    private SplitPane splitPane;
    @FXML
    private MenuButton actionsButton;
    @FXML
    private Separator separator;

    private Region windowNode;
    private ObjectProperty<Stage> stageProperty = new SimpleObjectProperty<>(new Stage());

    private Resource resource;
    private BooleanProperty alwaysShowTabs = new SimpleBooleanProperty(false);

    private InvalidationListener panelsListener = (Observable change) -> layoutPanels();
    private InvalidationListener toolbarsListener = (Observable change) -> layoutToolbars();
    private InvalidationListener actionsListener = (Observable change) -> layoutActions();

    Map<Panel, Tab> panelTabs = new HashMap<>();

    public static ResourceView load() throws IOException {
        return FXUtil.loadFX(ResourceView.class, "ResourceView.fxml");
    }

    @Override
    public ObjectProperty<Stage> stageProperty() {
        return stageProperty;
    }

    @Override
    public StringProperty titleProperty() {
        return resource.titleProperty();
    }

    public void onFXLoad() {
        panels.setStyle("-fx-open-tab-animation: NONE; -fx-close-tab-animation: NONE;");
        SplitPane.setResizableWithParent(panel, false);
        SplitPane.setResizableWithParent(displayPanel, true);
        actionsButton.setGraphic(ActofIcons.getCustomIcon(ResourceView.class, "icons/menu.png"));
        actionsButton.getStyleClass().add("tool-bar-button");
        toolbar.getChildren().clear();

        // topPane.sceneProperty().addListener(event -> fixTabBar());
        alwaysShowTabs.addListener(e -> layoutPanels());

        stageProperty.addListener(event -> {
            if (resource != null && resource.getEditor() != null) {
                resource.getEditor().setStage(getStage());
            }
        });
    };

    public void setTabClosingPolicy(TabClosingPolicy policy) {
        panels.setTabClosingPolicy(policy);
    }

    private void updateDisplay() {

        layoutPanels();
        layoutToolbars();
        layoutActions();
        layoutDisplay();

    }

    private void layoutDisplay() {
        displayPanel.setCenter(resource.getEditor().getDisplay().getFXRegion());
    }

    private void layoutActions() {

        actionsButton.getItems().clear();
        for (Action a : resource.getEditor().getActions()) {
            MenuItem item = new MenuItem(a.getTitle());
            item.disableProperty().bind(a.getEnabledProperty().not());
            item.setOnAction(event -> {
                Optional<Resource> result = a.perform();
                if (!result.isPresent()) { return; }
                resource.getEditor().getResources().add(result.get());
            });
            actionsButton.getItems().add(item);
        }

    }

    // private void fixTabBar() {
    // final StackPane header = (StackPane) panels.lookup(".tab-header-area");
    // if (header != null) {
    // if (panels.getTabs().size() == 1) header.setPrefHeight(0);
    // else header.setPrefHeight(-1);
    // }
    // }

    private void layoutToolbars() {

        toolbar.getChildren().clear();

        if (resource.getEditor() == null) { return; }

        toolbar.getChildren().addAll(resource.getEditor().getToolbars());
        if (resource.getEditor().getActions().size() > 0) {
            toolbar.getChildren().addAll(separator, actionsButton);
        }
    }

    private void layoutPanels() {

        Editor editor = resource.getEditor();

        if (editor == null) {
            panels.getTabs().clear();
            panelTabs.clear();
            return;
        }

        if (editor.getPanels().size() == 0) {
            topPane.setCenter(displayPanel);
            panels.getTabs().clear();
            panelTabs.clear();
        } else if ((editor.getPanels().size() == 1) && (!alwaysShowTabs.get())) {
            topPane.setCenter(splitPane);
            Panel p = editor.getPanels().get(0);
            panel.setCenter(p.getFXRegion());
        } else {
            topPane.setCenter(splitPane);
            panel.setCenter(panels);

            // in panels but not tabs
            List<Panel> toAdd = new ArrayList<>(editor.getPanels());
            toAdd.removeAll(panelTabs.keySet());

            // in tabs but not in panels
            List<Panel> toRemove = new ArrayList<>(panelTabs.keySet());
            toRemove.removeAll(editor.getPanels());

            // go through all panels for the display. If we haven't created a
            // tab for it yet, do so now.
            panels.getTabs().clear();
            for (Panel panel : editor.getPanels()) {
                Tab tab;
                if (panelTabs.containsKey(panel)) {
                    tab = panelTabs.get(panel);
                    tab.setContent(panel.getFXRegion());
                } else {
                    tab = new Tab(panel.getTitle(), panel.getFXRegion());
                    tab.textProperty().bind(panel.titleProperty());
                    tab.closableProperty().bind(panel.dismissibleProperty());
                    tab.setOnClosed(event -> {
                        panel.dismiss();
                    });
                    panelTabs.put(panel, tab);
                }
                panels.getTabs().add(tab);
            }

        }

        // fixTabBar();

    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {

        if (this.resource != null && this.resource.getEditor() != null) {
            this.resource.getEditor().getActions().removeListener(actionsListener);
            this.resource.getEditor().getPanels().removeListener(panelsListener);
            this.resource.getEditor().getToolbars().removeListener(toolbarsListener);
        }

        this.resource = resource;
        resource.getEditor().setStage(getStage());

        if (this.resource.getEditor() == null) { return; }
        this.resource.getEditor().getActions().addListener(actionsListener);
        this.resource.getEditor().getPanels().addListener(panelsListener);
        this.resource.getEditor().getToolbars().addListener(toolbarsListener);

        updateDisplay();

        getStage().sizeToScene();

    }

    @Override
    public void setFXRegion(Region node) {
        this.windowNode = node;
    }

    @Override
    public Region getFXRegion() {
        return windowNode;
    }

    public final BooleanProperty alwaysShowTabsProperty() {
        return this.alwaysShowTabs;
    }

    public final boolean isAlwaysShowTabs() {
        return this.alwaysShowTabsProperty().get();
    }

    public final void setAlwaysShowTabs(final boolean alwaysShowTabs) {
        this.alwaysShowTabsProperty().set(alwaysShowTabs);
    }

}
