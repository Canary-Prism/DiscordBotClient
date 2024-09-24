package canaryprism.dbc.markdown;

import java.util.regex.Pattern;

public class DiscordMarkdown {
    public static final Pattern bold_italic = Pattern.compile("(?<!\\\\)\\*(?<!\\\\)\\*(?<!\\\\)\\*([^`]+?)(?<!\\\\)\\*(?<!\\\\)\\*(?<!\\\\)\\*");
    public static final Pattern bold = Pattern.compile("(?<!\\\\)\\*(?<!\\\\)\\*([^`]+?)(?<!\\\\)\\*(?<!\\\\)\\*");
    public static final Pattern italic = Pattern.compile("(?<!\\\\)\\*([^`]+?)(?<!\\\\)\\*");
    public static final Pattern code = Pattern.compile("`(.+?)`");
    public static final Pattern strikethrough = Pattern.compile("(?<!\\\\)~(?<!\\\\)~(.+?)(?<!\\\\)~(?<!\\\\)~");
    public static final Pattern underline_em = Pattern.compile("(?<!\\\\)_(?<!\\\\)_(?<!\\\\)(.+?)(?<!\\\\)_(?<!\\\\)_(?<!\\\\)_");
    public static final Pattern underline = Pattern.compile("(?<!\\\\)_(?<!\\\\)_(.+?)(?<!\\\\)_(?<!\\\\)_");
    public static final Pattern emphasis = Pattern.compile("(?<!\\\\)_(.+?)(?<!\\\\)_");

    public static final Pattern escape = Pattern.compile("\\\\([\\\\*])");
    
    public static final Pattern emoji = Pattern.compile("&lt;a?:[^\\s]+:([\\d]+)&gt;");

    public static String toXHTML(String str) {

        // var is_emoji_only = emoji.matcher(str).replaceAll("").isBlank();

        str = bold_italic.matcher(str).replaceAll("<b><i>$1</i></b>");
        str = bold.matcher(str).replaceAll("<b>$1</b>");
        str = italic.matcher(str).replaceAll("<i>$1</i>");
        str = code.matcher(str).replaceAll("<pre>$1</pre>");
        str = strikethrough.matcher(str).replaceAll("<s>$1</s>");
        str = underline_em.matcher(str).replaceAll("<u><i>$1</i></u>");
        str = underline.matcher(str).replaceAll("<u>$1</u>");
        str = emphasis.matcher(str).replaceAll("<i>$1</i>");

        str = escape.matcher(str).replaceAll("$1");

        // str = emoji.matcher(str).replaceAll(String.format("<emoji key=\"$1\" %s/>", is_emoji_only ? "size='40' " : ""));

        // text = text.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        return str;
    }

    public static String parseEmojis(String str) {
        return parseEmojis(str, true);
    }
    public static String parseEmojis(String str, boolean allow_big_emojis) {
        var is_emoji_only = false;
        if (allow_big_emojis) {
            emoji.matcher(str).replaceAll("").isBlank();
        }

        return emoji.matcher(str).replaceAll(String.format("<emoji key=\"$1\" %s/>", is_emoji_only ? "size='40' " : ""));
    }
}
