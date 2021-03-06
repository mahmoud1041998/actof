package net.objectof.actof.connectorui;


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.objectof.actof.common.controller.config.ActofEnv;
import net.objectof.actof.connectorui.beans.Connection;
import net.objectof.actof.connectorui.beans.Last;
import net.objectof.actof.connectorui.beans.Saved;
import net.objectof.connector.Connector;
import net.objectof.connector.Connector.Initialize;
import net.objectof.connector.Connectors;
import net.objectof.connector.Parameter;
import net.objectof.connector.sql.IH2Connector;
import net.objectof.model.Package;
import net.objectof.model.Transaction;


public class SavedConnections {

    private static final Package SETTINGS = getSettingsPackage();

    private static Package getSettingsPackage() {
        File appdir = ActofEnv.appDataDirectory("actof");
        appdir.mkdirs();

        Connector settings = new IH2Connector();
        settings.getParameter(IH2Connector.KEY_DIRECTORY).setValue(appdir.getAbsolutePath() + "/Connections/");
        settings.getParameter(IH2Connector.KEY_REPOSITORY).setValue("objectof.net:1517/Connectors");

        try {
            return settings.getPackage();
        }
        catch (Exception e) {

            try {
                InputStream input = SavedConnections.class.getResourceAsStream("/packages/connections.xml");
                Package repo = settings.createPackage(input, Initialize.WHEN_EMPTY);
                populate(repo);
                return repo;
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }

        }

        return null;
    }

    private static void populate(Package repo) {
        Transaction tx = repo.connect(SavedConnections.class.getName());
        Saved saved = tx.create("Saved");
        saved.setConnections(tx.create("Saved.connections"));
        Last last = tx.create("Last");
        Connection connection = tx.create("Connection");
        last.setConnection(connection);
        tx.post();
    }

    private static Transaction getTx() {
        return SETTINGS.connect(SavedConnections.class.getName());
    }

    private static Saved saved(Transaction tx) {
        Saved saved = tx.retrieve("Saved", "1");
        return saved;
    }

    public static List<ConnectorUI> getSavedConnectors() {
        List<ConnectorUI> connectors = new ArrayList<>();
        Saved saved = saved(getTx());
        for (Connection connection : saved.getConnections()) {
            connectors.add(SavedConnections.asConnectorUI(connection));
        }
        connectors.sort((a, b) -> a.getName().compareTo(b.getName()));
        return connectors;
    }

    public static void addSavedConnector(Connector connector) {

        Transaction tx = getTx();

        Connection connection = tx.create("Connection");
        connectorToConnection(tx, connector, connection);

        saved(tx).getConnections().add(connection);

        tx.post();

    }

    public static void removeSavedConnector(Connector connector) {
        Transaction tx = getTx();

        for (Connection match : new ArrayList<>(saved(tx).getConnections())) {
            if (connector.equals(asConnectorUI(match))) {
                saved(tx).getConnections().remove(match);
                match.setName(null);
                match.setType(null);
                match.setParameters(null);
            }
        }
        tx.post();

    }

    public static ConnectorUI getSavedByName(String name) {
        for (ConnectorUI conn : getSavedConnectors()) {
            if (conn.getName().equals(name)) { return conn; }
        }
        return null;
    }

    public static ConnectorUI getLastConnector() {

        Transaction tx = getTx();
        Last last = tx.retrieve("Last", "1");

        ConnectorUI connector = asConnectorUI(last.getConnection());
        if (connector == null) { return null; }

        for (ConnectorUI saved : getSavedConnectors()) {
            if (saved.equals(connector)) {
                connector = saved;
                break;
            }
        }
        return connector;
    }

    public static void setLastConnector(Connector connector) {
        Transaction tx = getTx();
        Last last = tx.retrieve("Last", "1");
        Connection connection = last.getConnection();
        connectorToConnection(tx, connector, connection);
        connection.setName("\u25C2 Last Connection \u25B8");
        tx.post();
    }

    private static void connectorToConnection(Transaction tx, Connector connector, Connection connection) {

        connection.setName(connector.getName());
        connection.setType(connector.getType());
        connection.setParameters(tx.create("Connection.parameters"));

        for (Parameter p : connector.getParameters()) {
            connection.getParameters().put(p.getTitle(), p.getValue());
        }
    }

    private static ConnectorUI asConnectorUI(Connection connection) {

        if (connection.getType() == null) { return null; }
        Connector connector = Connectors.getConnectorByType(connection.getType());
        if (connector == null) { return null; }

        connector.setName(connection.getName());

        for (Parameter p : connector.getParameters()) {
            p.setValue(connection.getParameters().get(p.getTitle()));
        }

        return new ConnectorUI(connector);

    }
}
