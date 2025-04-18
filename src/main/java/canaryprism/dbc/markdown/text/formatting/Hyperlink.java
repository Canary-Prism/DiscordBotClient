package canaryprism.dbc.markdown.text.formatting;

import canaryprism.dbc.markdown.text.TextComponent;

import java.net.URL;
import java.util.List;

public final class Hyperlink extends FormattingTextComponent {
    
    private final URL url;
    
    private Hyperlink(List<TextComponent> components, URL url) {
        super(components);
        this.url = url;
    }
    
    public URL getUrl() {
        return url;
    }
}
