package com.darianopolis.dmj.json;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonEmpty implements Json {
    final static JsonEmpty EMPTY = new JsonEmpty();

    private JsonEmpty() {

    }

    @Override
    public String asString() {
        return "";
    }

    @Override
    public int asInteger() {
        return 0;
    }

    @Override
    public long asLong() {
        return 0;
    }

    @Override
    public double asDouble() {
        return 0;
    }

    @Override
    public float asFloat() {
        return 0;
    }

    @Override
    public BigInteger asBigInteger() {
        return null;
    }

    @Override
    public BigDecimal asBigDecimal() {
        return null;
    }

    @Override
    public String asRaw() {
        return "";
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public Json get(int index) {
        return this;
    }

    @Override
    public String getKey() {
        throw new IllegalStateException("Empty!");
    }

    @Override
    public Json get(String key) {
        return this;
    }

    @Override
    public Json iterator() {
        throw new IllegalStateException("Empty!");
    }

    @Override
    public boolean hasNext() {
        return false;
    }

//    @Override
//    public Json next() {
//        throw new IllegalStateException("Empty!");
//    }

//    public boolean nextValue() { return false; }
//    public boolean nextObject() { return  false; }
}
