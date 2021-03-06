package com.foobnix.android.utils;

import java.lang.Character.UnicodeBlock;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.foobnix.dao2.FileMeta;
import com.foobnix.pdf.info.ExtUtils;
import com.foobnix.pdf.info.wrapper.AppState;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.util.Pair;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

public class TxtUtils {

    public static String replaceLast(String input, String from, String to) {
        return input.replaceAll(from + "$", to);
    }

    public static String getFileMetaBookName(FileMeta fileMeta) {
        if (!ExtUtils.isTextFomat(fileMeta.getPath())) {
            return ExtUtils.getFileName(fileMeta.getPath());
        }

        if (TxtUtils.isNotEmpty(fileMeta.getAuthor())) {
            return fileMeta.getAuthor() + " - " + fileMeta.getTitle();
        } else {
            return fileMeta.getTitle();
        }

    }

    public static String replaceLastFirstName(String name) {
        if (TxtUtils.isEmpty(name)) {
            return "";
        }
        name = name.trim();

        if (!name.contains(" ") || name.endsWith(".")) {
            return name;
        }
        String[] split = name.split(" ");
        StringBuilder res = new StringBuilder();
        res.append(split[split.length - 1]);
        for (int i = 0; i <= split.length - 2; i++) {
            res.append(" ");
            res.append(split[i]);
        }
        return res.toString();

    }

    public static String space() {
        return AppState.get().selectingByLetters ? "" : " ";
    }

    public static Spanned underline(String text) {
        return Html.fromHtml("<u>" + text + "</u>");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isCJK2(int ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block) || //
                Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block) || //
                Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block) //
        ) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= 19) {
            return Character.isIdeographic(ch);
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isCJK(int ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        List<Character.UnicodeBlock> blocks = Arrays.asList(//
                //
                UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS, //
                UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS, //
                UnicodeBlock.CJK_COMPATIBILITY_FORMS, //
                UnicodeBlock.CJK_RADICALS_SUPPLEMENT, //
                UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION, //
                UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A, //
                UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B, //
                UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS, //
                UnicodeBlock.HIRAGANA, //
                UnicodeBlock.KATAKANA//
        );
        if (blocks.contains(block)) {
            return true;
        }

        if (Build.VERSION.SDK_INT >= 19) {
            return Character.isIdeographic(ch);
        }

        return false;
    }

    public static String firstUppercase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String filterDoubleSpaces(String str) {
        if (str == null) {
            return str;
        }
        return str.replace("   ", " ").replace("  ", " ");
    }

    public static String substring(String str, int len) {
        if (str.length() > len) {
            return str.substring(0, len);
        }
        return str;

    }

    static List<String> dividers = Arrays.asList(" - ", " _ ", "_-_");

    public static Pair<String, String> getTitleAuthorByPath(String name) {
        LOG.d("getTitleAuthorByPath", name);
        String author = "";
        String title = "";
        try {

            if (name.contains(".")) {// remove ext
                name = name.substring(0, name.lastIndexOf("."));
            }

            int indexOf = -1;

            for (String it : dividers) {
                if (name.contains(it)) {
                    indexOf = name.indexOf(it);
                    break;
                }
            }
            if (indexOf > 0) {
                author = name.substring(0, indexOf);
                title = name.substring(indexOf + 3);
            }

            if (TxtUtils.isEmpty(author)) {
                int i = name.lastIndexOf("(");
                int j = name.lastIndexOf(")");
                if (j > 0 && i > 0 && j - i > 10) {
                    author = name.substring(i + 1, j);

                    if (TxtUtils.isEmpty(title)) {
                        title = name.substring(0, i);
                    }
                }
            }

            author = firstUppercase(author.trim());

            if (TxtUtils.isEmpty(title)) {
                title = filterTitle(firstUppercase(name.trim()));
            } else {
                title = filterTitle(firstUppercase(title.trim()));
            }

        } catch (Exception e) {
            LOG.e(e);
        }
        return new Pair<String, String>(title, author);
    }

    static List<String> trash = Arrays.asList("-", "—", "_", "  ");

    public static String filterTitle(String title) {
        if (title == null) {
            return "";
        }

        for (String it : trash) {
            title = title.replace(it, " ");
        }
        return title.trim();

    }

    public static TextView underlineTextView(TextView textView) {
        String text = textView.getText().toString();
        textView.setText(underline(text));
        return textView;
    }

    public static Spanned bold(String text) {
        return Html.fromHtml("<u>" + text + "</u>");
    }

    public static char getLastChar(String line) {
        if (line == null || line.isEmpty()) {
            return 0;
        }
        return line.charAt(line.length() - 1);
    }

    public static boolean isLastCharEq(String line, char[] chars) {
        if (line == null || line.isEmpty()) {
            return false;
        }
        char last = line.charAt(line.length() - 1);
        for (char ch : chars) {
            if (ch == last) {
                return true;
            }
        }
        return false;
    }

    public static String getFirstLetter(String name) {
        if (TxtUtils.isNotEmpty(name)) {
            return String.valueOf(name.charAt(0));
        } else {
            return "";
        }
    }

    public static boolean isFooterNote(String text) {
        Pattern p = Pattern.compile("[\\[{][0-9]+[}\\]]");
        return text != null && text.length() < 30 && (p.matcher(text).find());
    }

    public static boolean isNumber(String text) {
        return text != null && text.matches("\\d+");
    }

    public static boolean isLineStartEndUpperCase(String line) {
        try {
            if (line == null || line.length() <= 4) {
                return false;
            }
            line = line.trim();
            if (line.length() <= 4) {
                return false;
            }
            boolean a1 = Character.isUpperCase(line.charAt(0));
            boolean a2 = Character.isUpperCase(line.charAt(1));
            boolean n1 = Character.isUpperCase(line.charAt(line.length() - 2));
            boolean n2 = Character.isUpperCase(line.charAt(line.length() - 3));
            return a1 && a2 && n1 && n2;
        } catch (Exception e) {
            LOG.e(e);
        }
        return false;
    }

    public static String getFooterNote(String input, Map<String, String> footNotes) {
        if (input == null) {
            return "";
        }
        if (footNotes == null) {
            return "";
        }

        try {
            String id = getFooterNoteNumber(input);

            if (TxtUtils.isNotEmpty(id)) {
                String string = footNotes.get(id);
                LOG.d("Find note for id", string);
                string = string.trim().replaceAll("^[0-9]+ ", "");
                return string;
            }

        } catch (Exception e) {
            LOG.e(e);
            return "";
        }

        return "";

    }

    public static String getFooterNoteNumber(String input) {
        if (input == null) {
            return "";
        }
        String patternString = "[\\[{]([0-9]+)[\\]}]";

        Matcher m = Pattern.compile(patternString).matcher(input);

        if (m.find()) {
            return m.group(0);
        }
        return "";

    }

    public static String filterString(String txt) {
        if (TxtUtils.isEmpty(txt)) {
            return txt;
        }

        String regexp = "[^\\w\\[\\]\\{\\}’']+";
        String replaceAll = txt.trim().replace("   ", " ").replace("  ", " ").replaceAll("\\s", " ").trim().replaceAll(regexp + "$", "").replaceAll("^" + regexp, "");
        replaceAll = replaceAll.replaceAll("(?u)(\\w+)(-\\s)", "$1");// remove
                                                                     // hyphen
        return replaceAll.trim();
    }

    public static String nullToEmpty(Object txt) {
        if (txt == null) {
            return "";
        }
        return txt instanceof String ? ((String) txt).trim() : txt.toString();
    }

    public static String nullNullToEmpty(String txt) {
        if (txt == null || txt.trim().equals("null")) {
            return "";
        }
        return txt.trim();
    }

    public static boolean isEmpty(String txt) {
        return txt == null || txt.trim().length() == 0;
    }

    public static boolean isNotEmpty(String txt) {
        return !isEmpty(txt);
    }

    public static String joinList(String delim, List<?> items) {
        if (items == null || items.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object it : items) {
            sb.append(it);
            sb.append(delim);
        }
        String string = sb.toString();
        if (string.length() > 1) {
            return string.substring(0, string.length() - delim.length());
        } else {
            return string;
        }
    }

    public static String join(String delim, Object... items) {
        if (items == null || items.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object it : items) {
            sb.append(it);
            sb.append(delim);
        }
        String string = sb.toString();
        if (string.length() > 1) {
            return string.substring(0, string.length() - delim.length());
        } else {
            return string;
        }
    }

    /**
     * Replace string "My name is @firstName @lastName"
     * 
     * @param str
     * @param keys
     * @return
     */
    public static String format$(String str, Object... keys) {
        if (str == null) {
            return null;
        }
        for (Object key : keys) {
            int start = str.indexOf("$");
            int end = str.indexOf(" ", start);

            if (end == -1) {
                end = str.length();
            }

            String tag = str.substring(start, end);
            str = str.replace(tag, key.toString());
        }
        return str;
    }

    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)");

    public static final Pattern SIMPLE_EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9_\\+-]+(\\.[A-Za-z0-9_\\+-]+)*@[a-z0-9]+(\\.[a-z0-9]+)*\\.([a-z]{2,4})$");

    public static boolean isEmailValidRFC(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isEmailValid(String email) {
        return SIMPLE_EMAIL_PATTERN.matcher(email).matches();
    }

}
