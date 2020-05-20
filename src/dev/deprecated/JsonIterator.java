//package com.darianopolis.compactjson;
//
//public class JsonIterator {
//    public String source;
//    public char[] chars;
//
//    public int head;
//
//    public JsonIterator(String source) {
//        this.source = source;
//        this.chars = source.toCharArray();
//        this.head = 0;
//    }
//
//    public char nextToken() {
//        char token = chars[head];
//        return token;
//    }
//
//    public boolean advanceHead(int steps) {
//        if (head + steps >= chars.length) return false;
//        head += steps;
//        return false;
//    }
//
//    public boolean advanceToChar(char c) {
//        while (nextToken() != c) {
//            if (!advanceHead(1)) return false;
//        }
//        return true;
//    }
//
//    public char findArray() {
//        advanceToChar('[');
//    }
//}
////