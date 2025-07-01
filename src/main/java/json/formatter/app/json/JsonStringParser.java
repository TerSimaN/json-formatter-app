package json.formatter.app.json;

import java.io.IOException;
import java.io.StringReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class JsonStringParser {
    public JsonElement parseJsonString(String jsonString) throws IOException {
        JsonElement jsonElement = null;

        StringReader stringReader = new StringReader(jsonString);
        JsonReader reader = new JsonReader(stringReader);

        try {
            JsonToken token = reader.peek();
            if (token.equals(JsonToken.BEGIN_OBJECT)) {
                jsonElement = parseReaderToObject(reader);
            } else if (token.equals(JsonToken.BEGIN_ARRAY)) {
                jsonElement = parseReaderToArray(reader);
            }
        } finally {
            reader.close();
        }

        return jsonElement;
    }

    private JsonObject parseReaderToObject(JsonReader reader) throws IOException {
        JsonObject jsonObject = new JsonObject();
        String nameKey = null;
        JsonElement jsonElement = null;

        reader.beginObject();
        while (reader.hasNext()) {
            JsonToken token = reader.peek();
            if (token.equals(JsonToken.NAME)) {
                nameKey = reader.nextName();
            } else if (token.equals(JsonToken.STRING)) {
                jsonElement = JsonParser.parseReader(reader);
                jsonObject.add(nameKey, jsonElement);
            } else if (token.equals(JsonToken.NUMBER)) {
                jsonElement = JsonParser.parseReader(reader);
                jsonObject.add(nameKey, jsonElement);
            } else if (token.equals(JsonToken.BEGIN_OBJECT)) {
                jsonObject.add(nameKey, parseReaderToObject(reader));
            } else if (token.equals(JsonToken.BEGIN_ARRAY)) {
                jsonObject.add(nameKey, parseReaderToArray(reader));
            } else if (token.equals(JsonToken.BOOLEAN)) {
                jsonElement = JsonParser.parseReader(reader);
                jsonObject.add(nameKey, jsonElement);
            } else if (token.equals(JsonToken.NULL)) {
                jsonElement = JsonParser.parseReader(reader);
                jsonObject.add(nameKey, jsonElement);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return jsonObject;
    }

    private JsonArray parseReaderToArray(JsonReader reader) throws IOException {
        JsonArray elementsArray = new JsonArray();
        JsonElement jsonElement = null;

        reader.beginArray();
        while (reader.hasNext()) {
            JsonToken token = reader.peek();
            if (token.equals(JsonToken.STRING)) {
                jsonElement = JsonParser.parseReader(reader);
                elementsArray.add(jsonElement);
            } else if (token.equals(JsonToken.NUMBER)) {
                jsonElement = JsonParser.parseReader(reader);
                elementsArray.add(jsonElement);
            } else if (token.equals(JsonToken.BEGIN_OBJECT)) {
                elementsArray.add(parseReaderToObject(reader));
            } else if (token.equals(JsonToken.BEGIN_ARRAY)) {
                elementsArray.add(parseReaderToArray(reader));
            } else if (token.equals(JsonToken.BOOLEAN)) {
                jsonElement = JsonParser.parseReader(reader);
                elementsArray.add(jsonElement);
            } else if (token.equals(JsonToken.NULL)) {
                jsonElement = JsonParser.parseReader(reader);
                elementsArray.add(jsonElement.getAsJsonNull());
            } else {
                reader.skipValue();
            }
        }
        reader.endArray();

        return elementsArray;
    }
}
