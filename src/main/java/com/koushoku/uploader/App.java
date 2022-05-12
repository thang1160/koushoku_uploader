package com.koushoku.uploader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jcraft.jsch.JSchException;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());
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
