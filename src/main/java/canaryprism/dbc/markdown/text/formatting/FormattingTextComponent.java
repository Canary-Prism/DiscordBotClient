package canaryprism.dbc.markdown.text.formatting;

import canaryprism.dbc.markdown.text.TextComponent;

import java.util.List;

public sealed abstract class FormattingTextComponent implements TextComponent permits Bold, Header1, Header2, Header3, Hyperlink, Italic, Small, Spoiler, Strikethrough, Underline {
    private final List<TextComponent> components;
    
    public final List<TextComponent> getComponents() {
        return components;
    }
    
    protected FormattingTextComponent(List<TextComponent> components) {
        this.components = List.copyOf(components);
    }
}
