package dev;

import java.util.Stack;

/**
 * A state machine used to validate JSON formats.
 */
public class JsonValidator {
    public static class JsonValidationException extends RuntimeException {
        public JsonValidationException(String message) {
            super(message);
        }
    }

    private enum State {
        BEFORE_KEY,
        IN_KEY,
        BEFORE_VALUE,
        IN_VALUE,
        AFTER_VALUE,
//        ERROR,
    }

    private Stack<Boolean> structureTypes = new Stack<>();
    private boolean emptyStructure = false;
    private boolean escaped = false;
    private char quotes = '\u0000';
    private State state = State.BEFORE_VALUE;

    /**
     * Evaluates the next char in the JSON
     * @return true IF JSON valid, else false
     *
     * ( BEFORE_VALUE, "{") -> BEFORE_KEY
     * (!BEFORE_VALUE, "{") -> ERROR
     * ( BEFORE_KEY, data ) -> IN_KEY
     * ( IN_KEY, data ) -> IN_KEY
     * ( BEFORE_VALUE, data ) -> IN_VALUE
     * ( IN_VALUE, data ) -> IN_VALUE
     * ( BEFORE_VALUE, "[") -> BEFORE_VALUE, !BINDED
     * (!BEFORE_VALUE, "[") -> ERROR
     * ( IN_KEY, ":") -> BEFORE_VALUE, BINDED
     * (!IN_KEY, ":") -> ERROR
     * ( IN_VALUE, ",") -> if (BINDED) then BEFORE_KEY, ELSE BEFORE_VALUE
     * (!IN_VALUE, ",") -> ERROR
     * ( IN_VALUE, "}") -> IN_VALUE
     * (!IN_VALUE, "}") -> if (emptyStructure) then IN_VALUE else ERROR
     * ( IN_VALUE, "]") -> IN_VALUE
     * (!IN_VALUE, "]") -> if (emptyStructure) then IN_VALUE else ERROR
     */
    public State next(char val) {
        if (Character.isWhitespace(val)) return state;

        if (!escaped) {
            if (quotes == '\u0000') {
                if (val == '{' || val == '[') {
                    if (state == State.BEFORE_VALUE) {
                        emptyStructure = true;
                        boolean object = val == '{';
                        structureTypes.push(object);
                        state = object ? State.BEFORE_KEY : State.BEFORE_VALUE;
                    } else {
                        throw new JsonValidationException("Cannot declare array/object outside of value");
                    }
                } else if (val == ':') {
                    if (state == State.IN_KEY) {
                        state = State.BEFORE_VALUE;
                    } else throw new JsonValidationException("No key, can't bind value");
                } else if (val == ',') {
                    if (state == State.IN_VALUE || state == State.AFTER_VALUE) {
                        if (structureTypes.empty()) {
                            throw new JsonValidationException("Outside structure, can't append value");
                        } else state = structureTypes.peek() ? State.BEFORE_KEY : State.BEFORE_VALUE;
                    } else throw new JsonValidationException("No value, can't start next!");
                } else if (val == '}' || val == ']') {
                    if (state != State.IN_VALUE && state != State.AFTER_VALUE && !emptyStructure) {
                        throw new JsonValidationException("No value, can't close non-empty structure");
                    } else if (structureTypes.pop()) {
                        if (val == ']') {
                            throw new JsonValidationException("Object closure expected, got array closure");
                        } else state = State.AFTER_VALUE;
                    } else if (val == '}') {
                        throw new JsonValidationException("Array closure expected, got object closure");
                    } else state = State.AFTER_VALUE;
                } else {
                    if (state == State.BEFORE_KEY || state == State.IN_KEY) {
                        state = State.IN_KEY;
                    } else if (state == State.BEFORE_VALUE || state == State.IN_VALUE) {
                        state = State.IN_VALUE;
                    } else if (state == State.AFTER_VALUE) {
                        throw new JsonValidationException("Outside value, can't accept data!");
                    }
                    emptyStructure = false;
                    if (val == '"' || val == '\'') quotes = val;
                }
            } else if (val == quotes) quotes = '\u0000';
        }

        escaped = (val == '\\') && !escaped;

        return state;
//        return state != State.ERROR;
    }

    public boolean validate(String json) {
        json.chars().forEach(c -> next((char) c));
        return true;
    }

    public static void main(String[] args) throws Throwable {
//        String test = "{\"a\":2}asdfasd";
//        String test = "{ \"key\": \"<div class=\"coolCSS\">some text</div>\" }";
        String test = "[\n" +
            "    {\n" +
            "        \"description\": \"additionalItems as schema\",\n" +
            "        \"schema\": {\n" +
            "            \"items\": [{}],\n" +
            "            \"additionalItems\": {\"type\": \"integer\"}\n" +
            "        },\n" +
            "        \"tests\": [\n" +
            "            {\n" +
            "                \"description\": \"additional items match schema\",\n" +
            "                \"data\": [ null, 2, 3, 4 ],\n" +
            "                \"valid\": true\n" +
            "            },\n" +
            "            {\n" +
            "                \"description\": \"additional items do not match schema\",\n" +
            "                \"data\": [ null, 2, 3, \"foo\" ],\n" +
            "                \"valid\": false\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    {\n" +
            "        \"description\": \"items is schema, no additionalItems\",\n" +
            "        \"schema\": {\n" +
            "            \"items\": {},\n" +
            "            \"additionalItems\": false\n" +
            "        },\n" +
            "        \"tests\": [\n" +
            "            {\n" +
            "                \"description\": \"all items match schema\",\n" +
            "                \"data\": [ 1, 2, 3, 4, 5 ],\n" +
            "                \"valid\": true\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    {\n" +
            "        \"description\": \"array of items with no additionalItems\",\n" +
            "        \"schema\": {\n" +
            "            \"items\": [{}, {}, {}],\n" +
            "            \"additionalItems\": false\n" +
            "        },\n" +
            "        \"tests\": [\n" +
            "            {\n" +
            "                \"description\": \"fewer number of items present\",\n" +
            "                \"data\": [ 1, 2 ],\n" +
            "                \"valid\": true\n" +
            "            },\n" +
            "            {\n" +
            "                \"description\": \"equal number of items present\",\n" +
            "                \"data\": [ 1, 2, 3 ],\n" +
            "                \"valid\": true\n" +
            "            },\n" +
            "            {\n" +
            "                \"description\": \"additional items are not permitted\",\n" +
            "                \"data\": [ 1, 2, 3, 4 ],\n" +
            "                \"valid\": false\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    {\n" +
            "        \"description\": \"additionalItems as false without items\",\n" +
            "        \"schema\": {\"additionalItems\": false},\n" +
            "        \"tests\": [\n" +
            "            {\n" +
            "                \"description\":\n" +
            "                    \"items defaults to empty schema so everything is valid\",\n" +
            "                \"data\": [ 1, 2, 3, 4, 5 ],\n" +
            "                \"valid\": true\n" +
            "            },\n" +
            "            {\n" +
            "                \"description\": \"ignores non-arrays\",\n" +
            "                \"data\": {\"foo\" : \"bar\"},\n" +
            "                \"valid\": true\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    {\n" +
            "        \"description\": \"additionalItems are allowed by default\",\n" +
            "        \"schema\": {\"items\": [{\"type\": \"integer\"}]},\n" +
            "        \"tests\": [\n" +
            "            {\n" +
            "                \"description\": \"only the first item is validated\",\n" +
            "                \"data\": [1, \"foo\", false],\n" +
            "                \"valid\": true\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "]";
        JsonValidator validator = new JsonValidator();
        validator.validate(test);
//        int index = 0;
//        try {
//            for (char c : test.toCharArray()) {
////                validator.next(c);
//                if (Character.isWhitespace(c)) continue;
//                System.out.print(c +" -> ");
//                System.out.println(validator.next(c));
//                index++;
////            System.out.println("char "+ c +" -> "+ validator.next(c));
//            }
////            System.out.println();
//        } catch (JsonValidationException e) {
////            System.out.println();
//            System.out.println("Failed at "+ test.substring(Math.max(0, index - 30), Math.min(test.length(), index + 30)));
//            e.printStackTrace();
//
//        }
    }
}
