package com.rookie.stattool.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rookie.stattool.db.DbQueries;
import java.text.ParseException;

public class JsonQueries {
    private final DbQueries dbQueries;

    public JsonQueries(DbQueries dbQueries) {
        this.dbQueries = dbQueries;
    }

    public JsonObject makeQueries(JsonObject jsonQueries) {
        JsonObject outputJson = new JsonObject();
        JsonArray jsonResults = new JsonArray();
        if (jsonQueries.has("criterias")) {
            outputJson.addProperty("type", "search");

            JsonArray jsonCriterias = jsonQueries.get("criterias").getAsJsonArray();
            for (JsonElement jsonElement : jsonCriterias) {
                JsonObject jsonParams = jsonElement.getAsJsonObject();
                if (jsonParams.has("lastName")) {
                    jsonResults.add(dbQueries.searchByLastName(jsonParams.get("lastName").getAsString()));
                } else if (jsonParams.has("productName") && jsonParams.has("minTimes")) {
                    jsonResults.add(dbQueries.searchByProductName(jsonParams.get("productName").getAsString(),
                            jsonParams.get("minTimes").getAsInt()));
                } else if (jsonParams.has("minExpenses") && jsonParams.has("maxExpenses")) {
                    jsonResults.add(dbQueries.searchByExpenses(jsonParams.get("minExpenses").getAsLong(),
                            jsonParams.get("maxExpenses").getAsLong()));
                } else if (jsonParams.has("badCustomers")) {
                    jsonResults.add(dbQueries.searchBadCustomers(jsonParams.get("badCustomers").getAsInt()));
                } else {
                    jsonResults.add(errorJson("Unknown criteria"));
                }

            }
            outputJson.add("results", jsonResults);
        } else if (jsonQueries.has("startDate") && jsonQueries.has("endDate")) {
            try {
                outputJson = dbQueries.statsPeriod(jsonQueries.get("startDate").getAsString(),
                        jsonQueries.get("endDate").getAsString());
            } catch (ParseException e) {
                outputJson = errorJson("Date parsing error. Date should be in yyyy-mm-dd format");
            }

        } else {
            outputJson = errorJson("criterias or startDate and endEnd not found");
        }

        return outputJson;
    }

    public JsonObject errorJson(String errorMessage) {
        JsonObject outputJson = new JsonObject();
        outputJson.addProperty("type", "error");
        outputJson.addProperty("message", errorMessage);
        return outputJson;
    }
}
