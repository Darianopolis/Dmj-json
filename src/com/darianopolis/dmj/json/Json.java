package com.darianopolis.dmj.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

public interface Json extends Iterator<Json>, Iterable<Json> {
    String asString();
    int asInteger();
    long asLong();
    double asDouble();
    float asFloat();
    BigInteger asBigInteger();
    BigDecimal asBigDecimal();
    String asRaw();
    boolean asBoolean();
    Json get(String key);
    Json get(int index);
    String getKey();
    @Override
    Json iterator();
    default Json next() {
        return this;
    }
//    boolean nextValue();
//    boolean nextObject();
//    Json asObject();
//    Json asArray();

//    Iterable<Map.Entry<String, Json>> getMappings();
//    Iterable<Json> getElements();
}
