package com.darianopolis.dmj.json;

import java.util.Iterator;

public class JsonUtil {
    @SuppressWarnings("Duplicates")
    static String decode(String source) {

        StringBuilder output = new StringBuilder();
        boolean escape = false;
        char quotes = '\u0000';
        char[] chars = source.toCharArray();
        int end = source.length();
        for (int i = 0; i < end; i++) {
            char c = chars[i];
            if (escape) {
                switch (c) {
                    case 'b': output.append('\b'); break;
                    case 'f': output.append('\f'); break;
                    case 'n': output.append('\n'); break;
                    case 'r': output.append('\r'); break;
                    case 't': output.append('\t'); break;
                    case 'u': {
                        StringBuilder control = new StringBuilder();
                        for (int j = i + 1; j < i + 5; j++) {
                            if (j >= chars.length) throw new RuntimeException("Illegal unicode sequence");
                            char c2 = Character.toLowerCase(chars[j]);
                            if (Character.isDigit(c2) || (c2 >= 'a' && c2 <= 'f')) {
                                control.append(c2);
                            } else {
                                throw new RuntimeException("Illegal unicode sequence");
                            }
                        }
                        output.append((char)Integer.parseInt(control.toString(), 16));
                        i += 4;
                        break;
                    }
                    default: output.append(c);
                }
            } else if (c != '\\') {
                if (quotes == '\u0000') {
                    if (c == '"' || c == '\'') quotes = c;
                    else if (!Character.isWhitespace(c)) output.append(c);
                } else {
                    if (c == quotes) quotes = '\u0000';
                    else output.append(c);
                }
            }

            escape = (c == '\\') && !escape;
        }
        return output.toString();
    }

//    static Iterator EMPTY_ITERATOR = new Iterator() {
//        @Override
//        public boolean hasNext() {
//            return false;
//        }
//
//        @Override
//        public Object next() {
//            return null;
//        }
//    };
}
