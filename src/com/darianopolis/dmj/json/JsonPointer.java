package com.darianopolis.dmj.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * References a specific location within a decoded json object.
 * index refers to previous symbol before the object starts.
 *
 * E.g. for a Pointer to the "bravo" value in this json the
 *   index would point to the preceding : binding marker
 * { "alpha": "bravo" }
 *          ^- Index
 *
 * A pointer to the base object of a Json has an initial index of -1
 * E.g. The initial pointer for the above structure would point to the position before the first {
 *   { "alpha": "bravo", "three", "four": "five" }
 * ^- index
 */
public class JsonPointer implements Json {
    public final JsonReader reader;
    public int index;

    private List<Integer> elements;
    public Map<String, Integer> mappings;

    public JsonPointer(JsonReader reader, int index) {
        this.reader = reader;
        this.index = index;
    }

    /**
     * Read JsonItem as object and get value
     * Note that JsonItem object returned is never stored, and thus subsequent calls will not satisfy identity checks
     * get(someKey) == get(someKey) -> false
     */
    public Json get(String key) {
        if (mappings == null && !findMappings()) return JsonEmpty.EMPTY;
        Integer mapping = mappings.get(key);
        if (mapping == null) return JsonEmpty.EMPTY;
        return new JsonPointer(reader, mapping);
    }

    /**
     * Read JsonItem as array and get element
     * Note that JsonItem object returned is never stored, and thus subsequent calls will not satisfy identity checks
     * get(someKey) == get(someKey) -> false
     */
    public Json get(int index) {
        if (index < 0) return JsonEmpty.EMPTY;
        if (elements == null && !findElements()) return JsonEmpty.EMPTY;
        if (index >= elements.size()) return JsonEmpty.EMPTY;
        return new JsonPointer(reader, elements.get(index));
    }

    @Override
    public String asString() {
        return JsonUtil.decode(reader.getQuotedString(index, reader.next(index)));
    }

    @Override
    public String asRaw() {
        return reader.getString(index, reader.skipBrackets(reader.next(index)));
    }

    @Override
    public boolean asBoolean() {
        return Boolean.parseBoolean(reader.getString(index, reader.next(index)).strip());
    }

    @Override
    public int asInteger() {
        return Integer.parseInt(reader.getString(index, reader.next(index)).strip());
    }

    @Override
    public long asLong() {
        return Long.parseLong(reader.getString(index, reader.next(index)).strip());
    }

    @Override
    public double asDouble() {
        return Double.parseDouble(reader.getString(index, reader.next(index)).strip());
    }

    @Override
    public float asFloat() {
        return Float.parseFloat(reader.getString(index, reader.next(index)).strip());
    }

    @Override
    public BigInteger asBigInteger() {
        return new BigInteger(reader.getString(index, reader.next(index)).strip());
    }

    @Override
    public BigDecimal asBigDecimal() {
        return new BigDecimal(reader.getString(index, reader.next(index)).strip());
    }


    public static void main(String[] args) throws Throwable {
        String jsonTest = "{\"nums\":[1,\"bind\":2,3,4,5],\"obj\":{\"a\":1,\"b\":2,\"c\":3}}";

        Json pointer = JsonReader.read(jsonTest);
        Json nums = pointer.get("nums").iterator();
//        System.out.println("First = "+ nums.asRaw());
        while (nums.hasNext()) {
            nums.next();
            System.out.println("Next = "+ nums.asRaw());
            String key = nums.getKey();
            if (key.length() > 0) System.out.println("  Key = "+ key);
        }

        System.out.println("-------");
        Json nums1 = pointer.get("nums");
        for (int i = 0; i < 5; i++) {
            System.out.println("Next = "+ nums1.get(i).asRaw());
        }
    }

    public String getKey() {
//        if (reader.getChar(index) != ':') return "";
        if (reader.getChar(index) != ':') throw new IllegalStateException("Value not in binding!");
        return reader.getQuotedString(reader.prev(index), index);
    }

//    int nextIndex;
    private boolean first = true;

    /**
     * If first, index -> { or [ or , or :
     * Else,     index -> , or :
     */
    @Override
    public boolean hasNext() {
        if (first) {
            first = false;
            return true;
        }

        int symbol = reader.skipBrackets(reader.next(index));
        char next = reader.getChar(symbol);
        if (next == '}' || next == ']') return false;
        if (next != ',') throw new IllegalStateException("Expected , } or ] but got "+ next);
        if (reader.getChar(symbol + 1) == ':') symbol++;
        index = symbol;
        return true;
    }

    @Override
    public Json iterator() {
        int first = reader.next(index);

//        char firstChar = reader.getChar(first);
//        if (firstChar != '[' && firstChar != '{') return JsonEmpty.EMPTY;
        if (reader.isEmpty(first)) return JsonEmpty.EMPTY;
        if (reader.getChar(reader.next(first)) == ':') first = reader.next(first);
        return new JsonPointer(reader, first);

//        if (reader.getChar(first) == '[') return new JsonPointer(reader, first);
//        if (reader.getChar(first) == '{') return new JsonPointer(reader, reader.next(first));

    }

    private boolean findElements() {
        int i = reader.next(index);
        if (reader.getChar(i) != '[') return false;
        if (reader.isEmpty(i)) return false;

        elements = new ArrayList<>();

        JsonPointer iter = new JsonPointer(reader, i);
        while (iter.hasNext()) elements.add(iter.index);

        return true;
    }

    public boolean findMappings() {
        int i = reader.next(index);
        if (reader.getChar(i) != '{') return false;
        if (reader.isEmpty(i)) return false;
        int j = i + 2;
        if (reader.getChar(j) != ':') return false;

        mappings = new TreeMap<>();

        JsonPointer iter = new JsonPointer(reader, j);
        while (iter.hasNext()) mappings.put(iter.getKey(), iter.index);

        return true;
    }
}
