package com.rookie.customerQuery.json;

import com.google.gson.*;

import java.io.*;

public class JsonFileUtils {
    public static void writeJson(String filename, JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter(filename)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, fileWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonObject readJson(String filename) {
        try(FileReader fileReader = new FileReader(filename)) {
            JsonElement jsonElement = JsonParser.parseReader(fileReader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
