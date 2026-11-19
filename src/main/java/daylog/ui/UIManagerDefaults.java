package daylog.ui;

import java.awt.Color;
import javax.swing.UIManager;

/** This isolates reading the look and feel defaults, so the dialog can restore a field's normal color. */
final class UIManagerDefaults {

    private UIManagerDefaults() {
    }

    /** The normal background color of a text field in the current look and feel. */
    static Color textFieldBackground() {
        return UIManager.getColor("TextField.background");
    }
}
