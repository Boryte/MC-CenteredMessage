package dev.bytecore.trollreborn.utilities.chat;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CenteredMessage {
    public static final int DEFAULT_WIDTH_PX = 154;
    private static final int SPACE_PX = DefaultFontSize.SPACE.getLength() + 1;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .hexColors()
            .extractUrls()
            .build();

    private static final MiniMessage MINI = MiniMessage.builder().build();

    /// aligment modes
    public enum Alignment { LEFT, CENTER, RIGHT }


    public static String align(String text, Alignment align) {
        return align(text, align, DEFAULT_WIDTH_PX);
    }

    public static String align(String text, Alignment align, int widthPx) {
        if (text == null || text.isBlank()) return "";
        // 1) legacy color codes (& → §)
        var legacy = ChatColor.translateAlternateColorCodes('&', text);
        // 2) bukkit hex (&#RRGGBB → §xRRGGBB)
        legacy = HEX_PATTERN.matcher(legacy)
                .replaceAll(mr -> ChatColor.of("#" + mr.group(1)).toString());

        var msgPx = measurePixels(legacy);
        var padPx = switch (align) {
            case LEFT   -> 0;
            case CENTER -> widthPx - msgPx / 2;
            case RIGHT  -> widthPx - msgPx;
        };
        if (padPx <= 0) return legacy;
        var spaces = padPx / SPACE_PX;
        return " ".repeat(spaces) + legacy;
    }

    /// adventure

    public static Component align(Component comp, Alignment align) {
        return align(comp, align, DEFAULT_WIDTH_PX);
    }

    public static Component align(Component comp, Alignment align, int widthPx) {
        if (comp == null) return Component.empty();
        var legacy = LEGACY.serialize(comp);
        if (legacy.isBlank()) return comp;

        var msgPx = measurePixels(legacy);
        var padPx = switch (align) {
            case LEFT   -> 0;
            case CENTER -> widthPx - msgPx / 2;
            case RIGHT  -> widthPx - msgPx;
        };
        if (padPx <= 0) return comp;
        var spaces = padPx / SPACE_PX;

        return Component.text(" ".repeat(spaces))
                .append(comp);
    }

    /// mini message

    public static Component alignMini(String miniMsg, Alignment align) {
        return alignMini(miniMsg, align, DEFAULT_WIDTH_PX);
    }

    public static Component alignMini(String miniMsg, Alignment align, int widthPx) {
        var comp = MINI.deserialize(miniMsg);
        return align(comp, align, widthPx);
    }

    /// pixel measurement

    private static int measurePixels(String legacy) {
        var width = 0;
        var bold  = false;

        for (var i = 0; i < legacy.length(); i++) {
            var c = legacy.charAt(i);
            if (c == '§' && i + 1 < legacy.length()) {
                bold = Character.toLowerCase(legacy.charAt(++i)) == 'l';
                continue;
            }
            var info = DefaultFontSize.getDefaultFontSize(c);
            width += (bold ? info.getBoldLength() : info.getLength()) + 1;
            bold = false;
        }
        return width;
    }

    /// font metadata

    private enum DefaultFontSize {
        A('A',5),  a('a',5),  B('B',5),  b('b',5),
        C('C',5),  c('c',5),  D('D',5),  d('d',5),
        E('E',5),  e('e',5),  F('F',5),  f('f',4),
        G('G',5),  g('g',5),  H('H',5),  h('h',5),
        I('I',3),  i('i',1),  J('J',5),  j('j',5),
        K('K',5),  k('k',4),  L('L',5),  l('l',1),
        M('M',5),  m('m',5),  N('N',5),  n('n',5),
        O('O',5),  o('o',5),  P('P',5),  p('p',5),
        Q('Q',5),  q('q',5),  R('R',5),  r('r',5),
        S('S',5),  s('s',5),  T('T',5),  t('t',4),
        U('U',5),  u('u',5),  V('V',5),  v('v',5),
        W('W',5),  w('w',5),  X('X',5),  x('x',5),
        Y('Y',5),  y('y',5),  Z('Z',5),  z('z',5),
        NUM_1('1',5),NUM_2('2',5),NUM_3('3',5),NUM_4('4',5),
        NUM_5('5',5),NUM_6('6',5),NUM_7('7',5),NUM_8('8',5),
        NUM_9('9',5),NUM_0('0',5),
        EXCLAMATION_POINT('!',1), AT_SYMBOL('@',6), NUM_SIGN('#',5),
        DOLLAR_SIGN('$',5), PERCENT('%',5), UP_ARROW('^',5),
        AMPERSAND('&',5), ASTERISK('*',5),
        LEFT_PARENTHESIS('(',4), RIGHT_PERENTHESIS(')',4),
        MINUS('-',5), UNDERSCORE('_',5), PLUS_SIGN('+',5),
        EQUALS_SIGN('=',5), LEFT_CURL_BRACE('{',4), RIGHT_CURL_BRACE('}',4),
        LEFT_BRACKET('[',3), RIGHT_BRACKET(']',3),
        COLON(':',1), SEMI_COLON(';',1),
        DOUBLE_QUOTE('"',3), SINGLE_QUOTE('\'',1),
        LEFT_ARROW('<',4), RIGHT_ARROW('>',4),
        QUESTION_MARK('?',5), SLASH('/',5), BACK_SLASH('\\',5),
        LINE('|',1), TILDE('~',5), TICK('`',2),
        PERIOD('.',1), COMMA(',',1),
        SPACE(' ',3),
        DEFAULT('a',4);

        private final char character;
        @Getter
        private final int length;

        DefaultFontSize(char character, int length) {
            this.character = character;
            this.length    = length;
        }

        public int getBoldLength()  { return this == SPACE ? length : length + 1; }

        private static final Map<Character, DefaultFontSize> lookup =
                Arrays.stream(values())
                        .collect(Collectors.toUnmodifiableMap(e -> e.character, e -> e));

        public static DefaultFontSize getDefaultFontSize(char c) {
            return lookup.getOrDefault(c, DEFAULT);
        }
    }
}
