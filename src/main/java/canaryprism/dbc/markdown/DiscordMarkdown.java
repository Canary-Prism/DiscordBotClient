package canaryprism.dbc.markdown;

import java.util.regex.Pattern;

public class DiscordMarkdown {
    public static final Pattern bold_italic = Pattern.compile("(?<!\\\\)\\*(?<!\\\\)\\*(?<!\\\\)\\*([^`]+?)(?<!\\\\)\\*(?<!\\\\)\\*(?<!\\\\)\\*");
    public static final Pattern bold = Pattern.compile("(?<!\\\\)\\*(?<!\\\\)\\*([^`]+?)(?<!\\\\)\\*(?<!\\\\)\\*");
    public static final Pattern italic = Pattern.compile("(?<!\\\\)\\*([^`]+?)(?<!\\\\)\\*");
    public static final Pattern code = Pattern.compile("`(.+?)`");
    public static final Pattern escape = Pattern.compile("\\\\([\\\\*])");

    public static final Pattern emoji = Pattern.compile("&lt;a?:[^\\s]+:([\\d]+)&gt;");

    public static String toXHTML(String str) {

        var is_emoji_only = emoji.matcher(str).replaceAll("").isBlank();

        str = bold_italic.matcher(str).replaceAll("<b><i>$1</i></b>");
        str = bold.matcher(str).replaceAll("<b>$1</b>");
        str = italic.matcher(str).replaceAll("<i>$1</i>");
        str = code.matcher(str).replaceAll("<pre>$1</pre>");
        str = escape.matcher(str).replaceAll("$1");

        str = emoji.matcher(str).replaceAll(String.format("<emoji key=\"$1\" %s/>", is_emoji_only ? "size='40' " : ""));

        // text = text.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        return str;
    }
}
