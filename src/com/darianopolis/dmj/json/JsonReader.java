package com.darianopolis.dmj.json;

import java.util.Stack;

/**
 * An extremely fast and memory efficient reader of JSON inputs.
 *
 * This JsonReader achieves an unmatched performance level by reading the JSON in a single
 * parse and producing a single integer array that is subsequently used for all further queries.
 *
 * A JSON string is encoded based on the positions of its symbols.
 * For each opening brace, a 'jump' is also stored as a negative value that refers to the symbol
 * index of the matching closing brace. An additional 0 is stored at the end of the array for
 * the purposes of reducing the requirement of a range check on 'next..' operations
 *
 * The following is a graphical representation of the index produced from an example json string.
 *
 *                  11111111112222222222333333333344444444445555555555666666666677
 *        012345678901234567890123456789012345678901234567890123456789012345678901
 * json = { "nums": [1, 2, [3, 4], 5], "int": 1, "decimal": 12.325, "bool": true }
 *
 *           1111111
 * 01234567890123456
 * {:[,,[,],],:,:,:}   // This is the extracted set of features from the above json string.
 *
 *            {       :   [        ,   ,   [        ,   ]   ,   ]   ,   :   ,   :   ,   :   }
 * symbols = [0, -19, 8, 10, -12, 12, 15, 17, -10, 19, 22, 23, 26, 27, 34, 36, 48, 56, 64, 71, 0]
 *                 |           |               |________|       |                           |
 *                 |           |________________________________|                           |
 *                 |________________________________________________________________________|
 *
 * -- Memory Efficiency --
 * The benefit of this indexing technique, is that it ensures that for any arbitrarily complex JSON
 * string, only a single highly efficient primitive type array is created with the minimum number of
 * elements required to quickly and efficiently navigate and represent any element within the data
 * structure.
 *
 * During the process of navigation, additional elements will be created such as lists of element indexes
 * and mappings. These are required for efficient mapping of requested values onto elements within the index.
 * However, since all JsonPointers that reference this index are stored at the same level, this ensures that
 * there are no recursive call structures or unreachable objects. As each JsonPointer only maintains a reference
 * to the base JsonReader class. This means that once a JsonPointer has been de-referenced in your java code,
 *   even if you have stored a JsonPointer as a 'child' of that JsonPointer, it will be garbage collected.
 *
 * -- Time Complexity --
 * The json string is only parsed over in it's entirety once, during the initial findSymbols() call.
 * Afterwards the index can be used to navigate the json in linear time for each element or mapping.
 * For example a subsequent call to findElements() in a child JsonPointer runs in time O(N) where N =
 *   number of element in that layer of the JSON. Note that the size or complexity of each individual
 *   element does not affect the runtime due to the use of symbol skips.
 *
 * -- Parse Safety --
 * The JsonReader performs a full parse check on the json string during the find symbol phase and will through
 * an exception at any invalid json structure that is found.
 * This enables the system to make subsequent assumptions about the expected structure of the symbol index.
 * Further increasing the speed of querying the json.
 */
@SuppressWarnings("Duplicates")
public class JsonReader {
    private static final int INITIAL_SYMBOL_SIZE = 100;

    String source;
    public char[] chars;
    public int[] symbols;

    public JsonReader() {}

    public static JsonPointer read(String source) {
        JsonReader reader = new JsonReader();
        reader.source = source;
        reader.chars = source.toCharArray();
        reader.findSymbols();
//        for (int i = 0; i < reader.symbols.length - 1; i++) {
//            int s = reader.symbols[i];
//            if (s >= 0) System.out.println(i +" -> "+ s +" -> "+ reader.chars[s]);
//            else System.out.println(i +" -> "+ s);
//        }
//        System.out.println(Arrays.toString(reader.symbols));
        return new JsonPointer(reader, -1);
    }

    /**
     * Adds a symbol to the symbols array, ensures there is enough space in the
     *  array for the new symbol AND a jump index if required.
     */
    private void addSymbol(int symbolIndex, int i) {
        if (symbolIndex + 1 >= symbols.length) {
            int[] newArray = new int[(symbols.length + 1) * 2];
            System.arraycopy(symbols, 0, newArray, 0, symbols.length);
            symbols = newArray;
        }
        symbols[symbolIndex] = i;
    }

    /**
     * Extracts the symbolic structure of the source json.
     * Each symbol {}[]:, is represented by an integer referring to its position in the source string.
     * Opening bracket symbols {[ have an additional negative integer referring to the symbol index of
     *   their matching closing bracket ]}
     *   This additional value is used to avoid having to traverse the entirety of a value
     *   when searching for elements or mappings.
     *
     * This is the only time that the library reads through the entirety of the source string.
     * All subsequent queries to the JSON structure are done through the created symbol jump structure
     *   and can be done in linear time.
     * This ensures that irregardless of JSON structure complexity, the time complexity of the parsing
     * process remains consistent and minimal.
     */
    private void findSymbols() {
        char quotes = '\u0000';
        boolean escape = false;

        int symbolIndex = 0;
        Stack<Integer> skips = new Stack<>();
//        Stack<Character> closer = new Stack<>();

        symbols = new int[INITIAL_SYMBOL_SIZE];

        boolean emptyStructure = false;
        for (int i = 0, end = chars.length; i < end; i++) {
            char c = chars[i];
            if (!escape) {
                if (quotes == '\u0000') {
                    if (c == '{' || c == '[') {
                        emptyStructure = true;
                        addSymbol(symbolIndex++, i);
                        skips.push(symbolIndex++);
//                        closer.push((c == '{') ? '}' : ']');
                    } else {
                        if (c == '}' || c == ']') {
//                            if (closer.empty() || closer.pop() != c) throw new IllegalStateException("Mismatched bracers! "+ c);

                            if (emptyStructure) {
                                addSymbol(symbolIndex, -1);         // Add sentinel character to denote empty array
                                symbols[symbolIndex + 1] = i;       // Add array symbol
                                symbolIndex += 2;
                            } else {
                                addSymbol(symbolIndex++, i);
                            }
                            symbols[skips.pop()] = 1 - symbolIndex;
                        } else if (c == ':' || c == ',') {
                            addSymbol(symbolIndex++, i);
                        } else if (c == '"' || c == '\'') {
                            quotes = c;
                        }
                        emptyStructure = false;
                    }
                } else if (c == quotes) quotes = '\u0000';
            }

            escape = (c == '\\') && !escape;
        }

        if (!skips.empty()) throw new IllegalStateException("Expected } or ], found EOF");

//        Cut down size of symbols array Leave zero pad at end for jump checking overflow protection
//        Note that since symbol exploration is always bounded by jumps, padded 0s will never be treated
//          as symbols. An addition, since addSymbol always ensures there is enough space for a jump
//          character, symbol will always end in a valid jump or at least one padded 0
        if (symbols.length > symbolIndex + 1) {
            int[] newSymbols = new int[symbolIndex + 1];
            System.arraycopy(symbols, 0, newSymbols, 0, symbolIndex);
             symbols = newSymbols;
        }
    }

    /*
    *  Index Navigation Functions
    *
    *  The following functions are for navigated and accessing the json structure using the symbol index.
    */

    /**
     * Extracts the substring of the json string bounded by two symbols.
     */
    String getString(int startSymbol, int endSymbol) {
        return source.substring(symbols[startSymbol] + 1, symbols[endSymbol]);
    }

    /**
     * Extracts a quoted string from the json string bounded by two symbols.
     */
    String getQuotedString(int startSymbol, int endSymbol) {
        int firstQuote = source.indexOf('"', symbols[startSymbol] + 1);
        int lastQuote = source.lastIndexOf('"', symbols[endSymbol]);
        return source.substring(firstQuote + 1, lastQuote);
    }

    /**
     * Extracts the char that represents the symbol at a given symbol index.
     * This is only used for identity checks when accessing JsonPointer as an
     *   array or object.
     */
    char getChar(int symbol) {
        return chars[symbols[symbol]];
    }

    /**
     * Return false if symbol points to start of non-empty array or object
     * Else true
     */
    boolean isEmpty(int startSymbol) {
        return symbols[startSymbol + 1] >= 0 || symbols[startSymbol + 2] < 0;
    }

    /**
     * Returns the next symbol index
     */
    int next(int symbol) {
        if (symbols[symbol + 1] < 0) return symbol + 2;
        return symbol + 1;
    }

    /**
     * Returns the previous symbol index
     */
    int prev(int symbol) {
        if (symbols[symbol - 1] < 0) return symbol - 2;
        return symbol - 1;
    }

    /**
     * Returns the matching close brackets to the current symbol
     * Behaviour is undefined is supplied with a symbol that is not an open bracket
     *
     * This is used to skip to the ending of an internal block
     */
    int matchingClose(int symbol) {
        return -symbols[symbol + 1];
    }

    /**
     * If symbol is an open bracket, returns the next symbol after the matching close bracket
     * Otherwise, return the symbol itself.
     *
     * This is used to skip over non-atomic values
     */
    int skipBrackets(int symbol) {
        int j = symbols[symbol + 1];
        if (j < 0) return 1 - j;
        return symbol;
    }
}
