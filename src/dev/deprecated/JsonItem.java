//package com.darianopolis.compactjson;
//
//import java.util.*;
//
///**
// * A high performance json reader
// * All json symbols { } [ ] , : are extracted and stored into symbols
// * Index points to the symbol before the start of the subset represented by this json.
// */
//public class JsonItem implements Json {
//    String source;
//    int[] symbols;
//    int index;
//
//    private static final int INITIAL_SYMBOL_SIZE = 100;
//    private static final int INITIAL_DEPTH = 10;
//
//    /**
//     * Standard constructor for creating a JsonItem from a source string
//     */
//    public JsonItem(String source) {
//        this.source = source;
////        this.chars = source.toCharArray();
//        findSymbols();
//        index = -1;
//    }
//
//    /**
//     * Constructor for creating a json sub-component.
//     */
//    private JsonItem(JsonItem parent, int index) {
//        this.source = parent.source;
////        this.chars = parent.chars;
//        this.symbols = parent.symbols;
//        this.index = index;
//    }
//
//    /**
//     * Read JsonItem as object and get value
//     * Note that JsonItem object returned is never stored, and thus subsequent calls will not satisfy identity checks
//     * get(someKey) == get(someKey) -> false
//     *
//     *
//     */
//    public Json get(String key) {
//        if (mappings == null) findMappings();
//        Integer mapping = mappings.get(key);
//        if (mapping == null) return JsonEmpty.EMPTY;
//        return new JsonItem(this, mapping);
//    }
//
//    /**
//     * Read JsonItem as array and get element
//     * Note that JsonItem object returned is never stored, and thus subsequent calls will not satisfy identity checks
//     * get(someKey) == get(someKey) -> false
//     *
//     */
//    public Json get(int index) {
//        if (index < 0) return JsonEmpty.EMPTY;
//        if (elements == null) findElements();
//        if (index >= elements.size()) return JsonEmpty.EMPTY;
//        return new JsonItem(this, elements.get(index));
//    }
//
//    public String asString() {
//        return decode(extractString(index, next(index)));
//    }
//
//    public String asRaw() {
//        return extractRaw(index, skipBrackets(next(index)));
//    }
//
//    public int asInteger() {
//        return Integer.parseInt(extractRaw(index, next(index)).strip());
//    }
//
//    private String extractRaw(int startSymbol, int endSymbol) {
//        return source.substring(symbols[startSymbol] + 1, symbols[endSymbol]);
//    }
//
//    private String extractString(int startSymbol, int endSymbol) {
//        int firstQuote = source.indexOf('"', symbols[startSymbol] + 1);
//        int lastQuote = source.lastIndexOf('"', symbols[endSymbol]);
//        return source.substring(firstQuote + 1, lastQuote);
//    }
//
//    @SuppressWarnings("Duplicates")
//    private static String decode(String source) {
//
//        StringBuilder output = new StringBuilder();
//        boolean escape = false;
//        char quotes = '\u0000';
//        char[] chars = source.toCharArray();
//        int end = source.length();
//        for (int i = 0; i < end; i++) {
//            char c = chars[i];
//            if (escape) {
//                switch (c) {
//                    case 'b': output.append('\b'); break;
//                    case 'f': output.append('\f'); break;
//                    case 'n': output.append('\n'); break;
//                    case 'r': output.append('\r'); break;
//                    case 't': output.append('\t'); break;
//                    case 'u': {
//                        StringBuilder control = new StringBuilder();
//                        for (int j = i + 1; j < i + 5; j++) {
//                            if (j >= chars.length) throw new RuntimeException("Illegal unicode sequence");
//                            char c2 = Character.toLowerCase(chars[j]);
//                            if (Character.isDigit(c2) || (c2 >= 'a' && c2 <= 'f')) {
//                                control.append(c2);
//                            } else {
//                                throw new RuntimeException("Illegal unicode sequence");
//                            }
//                        }
//                        output.append((char)Integer.parseInt(control.toString(), 16));
//                        i += 4;
//                        break;
//                    }
//                    default: output.append(c);
//                }
//            } else if (c != '\\') {
//                if (quotes == '\u0000') {
//                    if (c == '"' || c == '\'') quotes = c;
//                    else if (!Character.isWhitespace(c)) output.append(c);
//                } else {
//                    if (c == quotes) quotes = '\u0000';
//                    else output.append(c);
//                }
//            }
//
//            escape = (c == '\\') && !escape;
//        }
//        return output.toString();
//    }
//
//    private int next(int i) {
//        if (symbols[i + 1] < 0) return i + 2;
//        return i + 1;
//    }
//
//    private int skipBrackets(int i) {
//        int j = symbols[i + 1];
//        if (j < 0) return 1 - j;
//        return i;
//    }
//
//    private List<Integer> elements;
//    private void findElements() {
//        int i = next(index);
//        if (source.charAt(symbols[i]) != '[') throw new IllegalStateException("Not an array!");
//        elements = new ArrayList<>();
//        int end = -symbols[i + 1];
//        while (i < end) {
//            elements.add(i);
//            i = skipBrackets(next(i));
//        }
//    }
//
//    private Map<String, Integer> mappings;
//    private void findMappings() {
//        int i = next(index);
//        if (source.charAt(symbols[i]) != '{') throw new IllegalStateException("Not an array!");
//
//        mappings = new TreeMap<>();
//        int end = -symbols[i + 1];
//        while (i < end) {
//            int j = next(i);
//            String key = decode(extractString(i, j));
//            mappings.put(key, j);
//            i = skipBrackets(j + 1);
//        }
//    }
//
//    private void addSymbol(int symbolIndex, int i) {
//        if (symbolIndex + 1 >= symbols.length) {
//            int[] newSymbols = new int[symbols.length * 2];
//            System.arraycopy(symbols, 0, newSymbols, 0, symbols.length);
//            symbols = newSymbols;
//        }
//        symbols[symbolIndex] = i;
//    }
//
//    private void findSymbols() {
//        char quotes = '\u0000';
//        boolean escape = false;
//        int depth = 0;
//
//        int symbolIndex = 0;
//        int[] symbolSkips = new int[INITIAL_DEPTH];
//        symbols = new int[INITIAL_SYMBOL_SIZE];
//        char[] chars = source.toCharArray();
//
//        for (int i = 0, end = chars.length; i < end; i++) {
//            char c = chars[i];
//
//            if (!escape) {
//                if (quotes == '\u0000') {
//                    if (c == '{' || c == '[') {
//                        addSymbol(symbolIndex++, i);
//                        if (depth >= symbolSkips.length) {
//                            int[] newSymbolSkips = new int[depth * 2];
//                            System.arraycopy(symbolSkips, 0, newSymbolSkips, 0, symbolSkips.length);
//                            symbolSkips = newSymbolSkips;
//                        }
//                        symbolSkips[depth++] = symbolIndex++;
//                    } else if (c == '}' || c == ']') {
//                        addSymbol(symbolIndex++, i);
//                        symbols[symbolSkips[--depth]] = 1 - symbolIndex;
//                    } else if (c == ':' || c == ',') {
//                        addSymbol(symbolIndex++, i);
//                    } else if (c == '"' || c == '\'') {
//                        quotes = c;
//                    }
//                } else if (c == quotes) quotes = '\u0000';
//            }
//
//            escape = (c == '\\') && !escape;
//        }
//
////        Cut down size of symbols array Leave zero pad at end for jump checking overflow protection
////        Note that since symbol exploration is always bounded by jumps, padded 0s will never be treated
////          as symbols
////        Also note that since addSymbol always ensures there is enough space for a jump character, symbol
////          will always end in a valid jump or at least one padded 0
//        if (symbols.length > symbolIndex + 1) {
//            int[] newSymbols = new int[symbolIndex + 1];
//            System.arraycopy(symbols, 0, newSymbols, 0, symbolIndex);
//            symbols = newSymbols;
//        }
//    }
//}
