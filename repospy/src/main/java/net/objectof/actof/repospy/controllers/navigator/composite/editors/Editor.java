package net.objectof.actof.repospy.controllers.navigator.composite.editors;


import javafx.scene.Node;
import net.objectof.actof.repospy.controllers.navigator.composite.editors.primitive.EntryLinkEditor;
import net.objectof.actof.repospy.controllers.navigator.composite.editors.primitive.IntegerEditor;
import net.objectof.actof.repospy.controllers.navigator.composite.editors.primitive.MomentEditor;
import net.objectof.actof.repospy.controllers.navigator.composite.editors.primitive.RealEditor;
import net.objectof.actof.repospy.controllers.navigator.composite.editors.primitive.ReferenceEditor;
import net.objectof.actof.repospy.controllers.navigator.composite.editors.primitive.TextEditor;
import net.objectof.actof.repospy.controllers.navigator.composite.editors.primitive.UnsupportedEditor;
import net.objectof.actof.repospy.controllers.navigator.kind.ILeafNode;


public interface Editor {

    void focus();

    Node getNode();

    default boolean expand() {
        return true;
    }

    default boolean inline() {
        return false;
    }

    static Editor createEditor(ILeafNode entry) {

        switch (entry.getStereotype()) {
            case BOOL:
                return new TextEditor(entry);
            case INT:
                return new IntegerEditor(entry);
            case MOMENT:
                return new MomentEditor(entry);
            case NUM:
                return new RealEditor(entry);
            case REF:
                return new ReferenceEditor(entry);
            case TEXT:
                return new TextEditor(entry);

            case MAPPED:
            case INDEXED:
            case COMPOSED:
            case SET:
                return new EntryLinkEditor(entry);

            case MEDIA:
            case FN:
            default:
                return new UnsupportedEditor(entry);

        }
    }
}
