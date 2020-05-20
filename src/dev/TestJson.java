package dev;

import com.darianopolis.dmj.json.Json;
import com.darianopolis.dmj.json.JsonPointer;
import com.darianopolis.dmj.json.JsonReader;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import java.io.IOException;

public class TestJson {
    public static int depth = 1000;
    public static int num = 10000;
    @SuppressWarnings("Duplicates")

    public static String createHorriblyMassiveJsonArray() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < depth; i++) builder.append("[");
        for (int i = 0; i < num; i++) {
            if (i > 0) builder.append(", ");
            builder.append(i);
        }
        for (int i = 0; i < depth; i++) builder.append("]");
        return builder.toString();
    }

    public static String createHorriblyMassiveJsonObject() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < depth - 1; i++) builder.append("[");
        builder.append("{");
        for (int i = 0; i < num; i++) {
            if (i > 0) builder.append(", ");
            builder.append("\"").append(i).append("\":").append(i);
        }
        builder.append("}");
        for (int i = 0; i < depth - 1; i++) builder.append("]");
        return builder.toString();
    }

    public static void main(String[] args) throws Throwable {
//        String horriblyMassiveJson = createHorriblyMassiveJsonObject();
//        System.out.println(horriblyMassiveJson.length());
//        Json reader = JsonReader.read(horriblyMassiveJson);
        tortureTestMyJson();
        tortureTestJsoniter();
    }

    public static void emptyTest() {
        String source = "{\"nums\":[]}";
        JsonPointer json = JsonReader.read(source);
        for (Json v : json.get("nums")) {
            System.out.println("V = "+ v.asInteger());
        }
//        JsonPointer nums = (JsonPointer) json.get("nums");
//        System.out.println("-----------");
//        System.out.println(nums.index);
//        System.out.println(nums.asRaw());
//        System.out.println("| = "+ json.reader.chars[json.reader.symbols[nums.index]]);
//        JsonPointer first = (JsonPointer) nums.get(0);
//        System.out.println("first.index = "+ first.index);
//        System.out.println("first.index.strind = "+ json.reader.symbols[first.index]);
//        System.out.println("first.char = "+ json.reader.chars[json.reader.symbols[nums.index]]);
//        System.out.println("first = "+ first.asRaw());

//        System.out.println("----------------");
//        Any any = JsonIterator.deserialize(source);
//        for (Any a : any.get("nums")) {
//            System.out.println("A = "+ a.toInt());
//        }
    }

    public static void tortureTestMyJson() {
        String horriblyMassiveJson = createHorriblyMassiveJsonObject();
        System.out.println(horriblyMassiveJson.length());

        int total1 = 0;

        Timer t = Timer.start("MyJson");

        Json reader = JsonReader.read(horriblyMassiveJson);
        for (int i = 0; i < depth - 1; i++) reader = reader.get(0);
//        for (Json v : reader) total1 += v.asInteger();
//        Json nums = reader.iterator();
//        do {
//            total1 += nums.asInteger();
//        } while (nums.nextObject());

//        for (int i = 0; i < depth - 1; i++) reader = reader.iterator();
        for (int i = 0; i < num; i++) total1 += reader.get("" + i).asInteger();

        t.stop();
        System.out.println("MyJson total = "+ total1);

    }

    public static void tortureTestJsoniter() {
        String horriblyMassiveJson = createHorriblyMassiveJsonObject();
        System.out.println(horriblyMassiveJson.length());
        Timer t = Timer.start("Jsoniter");

        int total2 = 0;
        Any jsoniter = JsonIterator.deserialize(horriblyMassiveJson);
        for (int i = 0; i < depth - 1; i++) jsoniter = jsoniter.get(0);
        for (int i = 0; i < num; i++) total2 += jsoniter.get("" + i).toInt();

//        try {
//            JsonIterator iter = JsonIterator.parse(horriblyMassiveJson);
//            for (int i = 0; i < depth - 1; i++) iter.readArray();
////            while (iter.readObject() != null) total2 += iter.readInt();
//            while (iter.readArray()) total2 += iter.readInt();
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }

        t.stop();
        System.out.println("Jsoniter total = "+ total2);
    }

    public static String tagCountString = "{\n" +
        "    \"users\": [\n" +
        "        {\n" +
        "            \"_id\": \"58451574858913704731\",\n" +
        "            \"about\": \"a4KzKZRVvqfBLdnpUWaD\",\n" +
        "            \"address\": \"U2YC2AEVn8ab4InRwDmu\",\n" +
        "            \"age\": 27,\n" +
        "            \"balance\": \"I5cZ5vRPmVXW0lhhRzF4\",\n" +
        "            \"company\": \"jwLot8sFN1hMdE4EVW7e\",\n" +
        "            \"email\": \"30KqJ0oeYXLqhKMLDUg6\",\n" +
        "            \"eyeColor\": \"RWXrMsO6xi9cpxPqzJA1\",\n" +
        "            \"favoriteFruit\": \"iyOuAekbybTUeDJqkHNI\",\n" +
        "            \"gender\": \"ytgB3Kzoejv1FGU6biXu\",\n" +
        "            \"greeting\": \"7GXmN2vMLcS2uimxGQgC\",\n" +
        "            \"guid\": \"bIqNIywgrzva4d5LfNlm\",\n" +
        "            \"index\": 169390966,\n" +
        "            \"isActive\": true,\n" +
        "            \"latitude\": 70.7333712683406,\n" +
        "            \"longitude\": 16.25873969455544,\n" +
        "            \"name\": \"bvtukpT6dXtqfbObGyBU\",\n" +
        "            \"phone\": \"UsxtI7sWGIEGvM2N1Mh0\",\n" +
        "            \"picture\": \"8fiyZ2oKapWtH5kXyNDZJjvRS5PGzJGGxDCAk1he1wuhUjxfjtGIh6agQMbjovF10YlqOyzhQPCagBZpW41r6CdrghVfgtpDy7YH\",\n" +
        "            \"registered\": \"gJDieuwVu9H7eYmYnZkz\",\n" +
        "            \"tags\": [\n" +
        "                \"M2b9n0QrqC\",\n" +
        "                \"zl6iJcT68v\",\n" +
        "                \"VRuP4BRWjs\",\n" +
        "                \"ZY9jXIjTMR\"\n" +
        "            ]\n" +
        "        }\n" +
        "    ]\n" +
        "}";

    public static void testTagCountJson() {
        var timer = Timer.start("MyJson");

        int count = 0;
        for (Json u : JsonReader.read(tagCountString).get("users")) {
            for (Json t : u.get("tags")) count++;
        }

        timer.stop();
        System.out.println("MyJson count = "+ count);
    }

    public static void testTagCountJsoniter() throws IOException {
        var timer = Timer.start("Jsoniter");

        JsonIterator iter = JsonIterator.parse(tagCountString);
        int totalTagsCount = 0;
        for (String field = iter.readObject(); field != null; field = iter.readObject()) {
            switch (field) {
                case "users":
                    while (iter.readArray()) {
                        for (String field2 = iter.readObject(); field2 != null; field2 = iter.readObject()) {
                            switch (field2) {
                                case "tags":
                                    while (iter.readArray()) {
                                        iter.skip();
                                        totalTagsCount++;
                                    }
                                    break;
                                default:
                                    iter.skip();
                            }
                        }
                    }
                    break;
                default:
                    iter.skip();
            }
        }

        timer.stop();
        System.out.println("Jsoniter count = "+ totalTagsCount);
    }
}
