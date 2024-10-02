package canaryprism.dbc.markdown;

import java.util.regex.Pattern;

import canaryprism.dbc.Main;

public class DiscordMarkdown {
    public static final Pattern bold_italic = Pattern.compile("(?<!\\\\)\\*(?<!\\\\)\\*(?<!\\\\)\\*([^`]+?)(?<!\\\\)\\*(?<!\\\\)\\*(?<!\\\\)\\*");
    public static final Pattern bold = Pattern.compile("(?<!\\\\)\\*(?<!\\\\)\\*([^`]+?)(?<!\\\\)\\*(?<!\\\\)\\*");
    public static final Pattern italic = Pattern.compile("(?<!\\\\)\\*([^`]+?)(?<!\\\\)\\*");
    public static final Pattern code = Pattern.compile("`(.+?)`");
    public static final Pattern strikethrough = Pattern.compile("(?<!\\\\)~(?<!\\\\)~(.+?)(?<!\\\\)~(?<!\\\\)~");
    public static final Pattern underline_em = Pattern.compile("(?<!\\\\)_(?<!\\\\)_(?<!\\\\)(.+?)(?<!\\\\)_(?<!\\\\)_(?<!\\\\)_");
    public static final Pattern underline = Pattern.compile("(?<!\\\\)_(?<!\\\\)_(.+?)(?<!\\\\)_(?<!\\\\)_");
    public static final Pattern emphasis = Pattern.compile("(?<!\\\\)_(.+?)(?<!\\\\)_");

    public static final Pattern escape = Pattern.compile("\\\\([^\\w\\d\\s])");
    public static final Pattern escapable = Pattern.compile("([^\\w\\d\\s]|_)");
    
    public static final Pattern emoji = Pattern.compile("&lt;a?:[^\\s]+:([\\d]+)&gt;");

    public static final Pattern br = Pattern.compile("<br\\s*/?>");

    enum Formattings {
        bold("**", "b"),
        italic("*", "i"),
        code("`", "pre", true, false),
        strikethrough("~~", "s"),
        underline("__", "u"),
        emphasis("_", "i", false, true),

        header1("# ", "h1", false, false, true, false),
        header2("## ", "h2", false, false, true, false),
        header3("### ", "h3", false, false, true, false),

        small("-# ", "small", false, false, true, false),

        quote("&gt; ", "quote", false, false, true, true),
        triplequote("&gt;&gt;&gt; ", "quote", false, false, true, true),
        ;

        /**
         * The characters that are used to denote the formatting
         */
        public final String chars; // i'm pretty sure all of these are symmetrical

        /**
         * The xml tag that the formatting will be converted to
         */
        public final String tag;

        /**
         * Whether the formatting should disable the parsing of other formattings inside
         * of it
         */
        public final boolean raw;

        /**
         * Whether a space is required after the formatting characters
         */
        public final boolean space_mandatory;

        public final boolean line_dominating;

        public final boolean mimicks_newline;

        public static final Pattern space = Pattern.compile("([^\\w\\d]|\\\\)");

        Formattings(String chars, String tag) {
            this(chars, tag, false, false);
        }

        Formattings(String chars, String tag, boolean raw, boolean space_mandatory) {
            this(chars, tag, raw, space_mandatory, false, false);
        }

        Formattings(String chars, String tag, boolean raw, boolean space_mandatory, boolean line_dominating, boolean mimicks_newline) {
            this.chars = chars;
            this.tag = tag;
            this.raw = raw;
            this.space_mandatory = space_mandatory;

            this.line_dominating = line_dominating;
            this.mimicks_newline = mimicks_newline;
        }
    }

    public static String toXHTML(String str) {

        try {
            var chars = str.toCharArray();

            var result = parseMarkdownToXHTML(chars, 0, null);
            
            if (Main.debug)
                System.out.println("Markdown Parse Result: " + result.stringBuilder.toString());

            return result.stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return str;
        }
    }

    record ParseResult(StringBuilder stringBuilder, int end) {
    }

    private static ParseResult parseMarkdownToXHTML(char[] chars, int start, Formattings current_formatting) {
        // System.out.println("Parsing: " + new String(chars, start, chars.length - start));
        var i = start;
        var sb = new StringBuilder();
        var end = chars.length;

        var is_newline = (
            start == 0 
            || chars[start - 1] == '\n' 
            || (current_formatting != null && current_formatting.mimicks_newline)
        );

        char_loop: 
        while (i < end) {
            if (chars[i] == '\\' && i + 1 < end && escapable.matcher(String.valueOf(chars[i + 1])).matches()) {
                sb.append(chars[i + 1]);
                i += 2;
                continue char_loop;
            }

            if (current_formatting != null && i != start) {
                if (current_formatting.line_dominating && chars[i] == '\n') {
                    // sb.append('\n');
                    return new ParseResult(sb, i);
                } else {
                    test_end: {
                        for (int j = i, k = 0; k < current_formatting.chars.length(); j++, k++) {
                            if (j >= end || chars[j] != current_formatting.chars.charAt(k)) {
                                break test_end;
                            }
                        }
    
                        if (current_formatting.space_mandatory) {
                            var index = i + current_formatting.chars.length();
                            if (index + 1 < end && chars[index] != '\\') {
                                index++;
                            }
                            if (index < end && !Formattings.space.matcher(String.valueOf(chars[index])).matches()) {
                                break test_end;
                            }
                        }
    
                        // System.out.println("End of formatting: " + current_formatting.chars + " at " + i);
                        return new ParseResult(sb, i);
                    }
                }
            }

            format_loop: 
            for (var formatting : Formattings.values()) {
                if (formatting.line_dominating && !is_newline) {
                    continue format_loop;
                }
                int format_start = i;
                for (int j = 0; j < formatting.chars.length() && format_start < end; format_start++, j++) {
                    if (chars[format_start] != formatting.chars.charAt(j)) {
                        continue format_loop;
                    }
                }

                int format_end;
                if (formatting.raw) {

                    // might as well avoid a recursive call if it's this simple
                    format_end = -1;

                    find_end: {
                        int j = format_start + 1; // it does format_start + 1 because a markdown format with 0
                                                  // characters
                                                  // inside is not allowed
                        int end_index = 0;

                        while (true) {
                            if (j >= end) {
                                continue format_loop;
                            }

                            if (chars[j] == formatting.chars.charAt(end_index)) {
                                if (end_index == 0) {
                                    format_end = j;
                                }
                                end_index++;
                            } else if (end_index != 0) {
                                end_index = 0;
                                j--;
                            }
                            if (end_index == formatting.chars.length()) {
                                if (formatting.space_mandatory) {
                                    if (j + 1 < end && !Formattings.space.matcher(String.valueOf(chars[j + 1])).matches()) {
                                        continue format_loop;
                                    }
                                }
                                break find_end;
                            }

                            j++;
                        }
                    }

                    if (format_end == -1) {
                        continue format_loop; // should never happen
                    }
                    
                    sb.append("<").append(formatting.tag).append(">");
                    sb.append(chars, format_start, format_end - format_start);
                } else {
                    var result = parseMarkdownToXHTML(chars, format_start, formatting);
                    if (result.stringBuilder == null) {
                        // System.out.println("Failed to parse: " + new String(chars, format_start, chars.length - format_start));
                        continue format_loop;
                    }
                    sb.append("<").append(formatting.tag).append(">");
                    sb.append(result.stringBuilder);
                    format_end = result.end;
                }
                sb.append("</").append(formatting.tag).append(">");

                i = format_end; 
                if (!formatting.line_dominating) i += formatting.chars.length();

                continue char_loop;
            }

            is_newline = (chars[i] == '\n');
            if (is_newline) {
                sb.append("<br/>");
            } else {
                sb.append(chars[i]);
            }


            i++;
        }

        if (current_formatting != null) {
            if (current_formatting.line_dominating) {
                return new ParseResult(sb, end);
            }
            // System.out.println("Failed to find end of formatting: " + current_formatting.chars);
            return new ParseResult(null, 0);
        }
        // System.out.println("End of parsing: " + new String(chars, start, chars.length - start));
        return new ParseResult(sb, end);
    }

    public static String parseEmojis(String str) {
        return parseEmojis(str, true);
    }
    public static String parseEmojis(String str, boolean allow_big_emojis) {
        var is_emoji_only = false;
        if (allow_big_emojis) {
            var s = emoji.matcher(str).replaceAll("");
            s = br.matcher(s).replaceAll("");
            is_emoji_only = s.isBlank();
        }

        return emoji.matcher(str).replaceAll(String.format("<emoji key=\"$1\" %s/>", is_emoji_only ? "size='40' " : ""));
    }
}
