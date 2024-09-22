package canaryprism.dbc.swing;

import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;

public class WrappedTextPane extends JTextPane {
    public WrappedTextPane() {
        super();
        this.setEditable(false);
        this.setOpaque(false);
        this.setEditorKit(new CustomHTMLEditorKit());
        this.setContentType("text/html");
    }
    

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getUI().getPreferredSize(this).width <= getParent().getSize().width;
    }
    @Override

    public boolean getScrollableTracksViewportHeight() {
        return true;
    }

    class CustomHTMLEditorKit extends HTMLEditorKit {

        private final ViewFactory viewFactory = new HTMLFactory() {
            @Override
            public View create(Element elem) {
                AttributeSet attrs = elem.getAttributes();
                Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
                Object o = (elementName != null) ? null : attrs.getAttribute(StyleConstants.NameAttribute);
                if (o instanceof Tag) {
                    HTML.Tag kind = (HTML.Tag) o;
                    if (Tag.IMPLIED == kind) return new WrappableParagraphView(elem); // <pre>
                    if (Tag.P == kind) return new WrappableParagraphView(elem); // <p>
                }
                return super.create(elem);
            }
        };

        @Override
        public ViewFactory getViewFactory() {
            return this.viewFactory;
        }
    }

    class WrappableParagraphView extends javax.swing.text.html.ParagraphView {

        public WrappableParagraphView(Element elem) {
            super(elem);
        }

        @Override
        public float getMinimumSpan(int axis) {
            return View.X_AXIS == axis ? 0 : super.getMinimumSpan(axis);
        }
    }
}
