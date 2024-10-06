package canaryprism.dbc.swing.text.attachment;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

/**
 * base class for all attachment views
 */
public abstract class AttachmentView extends JComponent {

    private final JPopupMenu context_menu = new JPopupMenu();

    AttachmentView() {
        this.initContextMenu(this.context_menu);

        this.setComponentPopupMenu(context_menu);
    }

    protected abstract void initContextMenu(JPopupMenu context_menu);
}
