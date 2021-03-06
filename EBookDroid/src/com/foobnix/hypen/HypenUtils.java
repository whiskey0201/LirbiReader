package com.foobnix.hypen;

import java.util.List;
import java.util.StringTokenizer;

public class HypenUtils {

    private static final String SHY = "&shy;";
    private static DefaultHyphenator instance = new DefaultHyphenator(HyphenPattern.ru);

    public static void applyLanguage(String lang) {
        HyphenPattern pattern = HyphenPattern.valueOf(lang);

        if (instance.pattern != pattern) {
            instance = new DefaultHyphenator(pattern);
        }
    }

    public static String applyHypnes(String input) {
        if (input == null || input.length() == 0) {
            return "";
        }
        input = input.replace("<", " <").replace(">", "> ").replace("\u00A0", " ");

        StringTokenizer split = new StringTokenizer(input, " ", true);
        StringBuilder res = new StringBuilder();

        while (split.hasMoreTokens()) {
            String w = split.nextToken();

            if (w.equals(" ")) {
                res.append(" ");
                continue;
            }

            if (w.length() <= 3) {
                res.append(w);
                continue;
            }

            if (w.contains("<") || w.contains(">") || w.contains("=") || w.contains("[") || w.contains("{")) {
                res.append(w);
                continue;
            }

            char last = w.charAt(w.length() - 1);

            char first = w.charAt(0);

            boolean startWithOther = false;
            if (!Character.isLetter(first)) {
                startWithOther = true;
                w = w.substring(1, w.length());
            }

            boolean endWithOther = false;
            if (w.length() != 0 && !Character.isLetter(last)) {
                endWithOther = true;
                w = w.substring(0, w.length() - 1);
            }

            boolean endWithOther2 = false;
            char last2 = ' ';
            if (endWithOther) {
                last2 = w.charAt(w.length() - 1);
                if (w.length() != 0 && !Character.isLetter(last2)) {
                    endWithOther2 = true;
                    w = w.substring(0, w.length() - 1);
                }

            }

            String result = null;
            if (w.contains("-")) {
                int find = w.indexOf("-");
                String p1 = w.substring(0, find);
                String p2 = w.substring(find + 1, w.length());
                result = join(instance.hyphenate(p1), SHY) + "-" + join(instance.hyphenate(p2), SHY);
            } else {
                result = join(instance.hyphenate(w), SHY);
            }

            if (startWithOther) {
                result = String.valueOf(first) + result;
            }

            if (endWithOther2) {
                result = result + String.valueOf(last2);
            }

            if (endWithOther) {
                result = result + String.valueOf(last);
            }
            res.append(result);

        }

        String result = res.toString();
        result = result.replace(" <", "<").replace("> ", ">");
        return result;
    }

    public static String join(List<String> list, String delimiter) {
        if (list == null || list.size() == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder(list.get(0));
        for (int i = 1; i < list.size(); i++) {
            result.append(delimiter).append(list.get(i));
        }
        String string = result.toString();
        return string;
        // return string.replace(SHY + SHY, SHY);
    }
}
