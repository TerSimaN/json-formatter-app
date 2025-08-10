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
            switch (token) {
                case JsonToken.BEGIN_OBJECT:
                    jsonElement = parseReaderToObject(reader);
                    break;
                case JsonToken.BEGIN_ARRAY:
                    jsonElement = parseReaderToArray(reader);
                    break;
                default:
                    break;
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
            switch (token) {
                case JsonToken.NAME:
                    nameKey = reader.nextName();
                    break;
                case JsonToken.STRING:
                    jsonElement = JsonParser.parseReader(reader);
                    jsonObject.add(nameKey, jsonElement);
                    break;
                case JsonToken.NUMBER:
                    jsonElement = JsonParser.parseReader(reader);
                    jsonObject.add(nameKey, jsonElement);
                    break;
                case JsonToken.BEGIN_OBJECT:
                    jsonObject.add(nameKey, parseReaderToObject(reader));
                    break;
                case JsonToken.BEGIN_ARRAY:
                    jsonObject.add(nameKey, parseReaderToArray(reader));
                    break;
                case JsonToken.BOOLEAN:
                    jsonElement = JsonParser.parseReader(reader);
                    jsonObject.add(nameKey, jsonElement);
                    break;
                case JsonToken.NULL:
                    jsonElement = JsonParser.parseReader(reader);
                    jsonObject.add(nameKey, jsonElement);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return jsonObject;
    }

    private JsonArray parseReaderToArray(JsonReader reader) throws IOException {
        JsonArray jsonArray = new JsonArray();
        JsonElement jsonElement = null;

        reader.beginArray();
        while (reader.hasNext()) {
            JsonToken token = reader.peek();
            switch (token) {
                case JsonToken.STRING:
                    jsonElement = JsonParser.parseReader(reader);
                    jsonArray.add(jsonElement);
                    break;
                case JsonToken.NUMBER:
                    jsonElement = JsonParser.parseReader(reader);
                    jsonArray.add(jsonElement);
                    break;
                case JsonToken.BEGIN_OBJECT:
                    jsonArray.add(parseReaderToObject(reader));
                    break;
                case JsonToken.BEGIN_ARRAY:
                    jsonArray.add(parseReaderToArray(reader));
                    break;
                case JsonToken.BOOLEAN:
                    jsonElement = JsonParser.parseReader(reader);
                    jsonArray.add(jsonElement);
                    break;
                case JsonToken.NULL:
                    jsonElement = JsonParser.parseReader(reader);
                    jsonArray.add(jsonElement.getAsJsonNull());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endArray();

        return jsonArray;
    }
}
