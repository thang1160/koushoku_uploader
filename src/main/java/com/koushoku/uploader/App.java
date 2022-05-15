package com.koushoku.uploader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.stream.Collectors;

public class App {
    private static JsonObject config;

    public static void main(String[] args) {
        InputStream inStreamConfig = App.class.getClassLoader().getResourceAsStream("config.json");
        String content = new BufferedReader(new InputStreamReader(inStreamConfig)).lines()
                .collect(Collectors.joining("\n"));
        config = new Gson().fromJson(content, JsonObject.class);
        new Desktop();
    }

    public static JsonObject getConfig() {
        return config;
    }
}
