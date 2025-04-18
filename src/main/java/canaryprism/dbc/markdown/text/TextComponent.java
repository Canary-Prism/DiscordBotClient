package canaryprism.dbc.markdown.text;

import canaryprism.dbc.markdown.text.data.DataTextComponent;
import canaryprism.dbc.markdown.text.formatting.FormattingTextComponent;

public sealed interface TextComponent permits DataTextComponent, FormattingTextComponent {

}
