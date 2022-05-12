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
        // JFrame f = new JFrame();// tạo thể hiện của JFrame

        // JButton b = new JButton("click");// tạo thể hiện của JButton
        // b.setBounds(130, 50, 100, 40);// trục x , y , width, height

        // f.setTitle("Hello World!");
        // f.add(b);// thêm button vào JFrame

        // f.setSize(400, 200);// thiết lập kích thước cho của sổ
        // f.setLayout(null);// không sử dụng trình quản lý bố cục
        // f.setVisible(true);// hiển thị cửa sổ
        try {
            Remote r = new Remote();
            r.publish();
            r.disconnect();
        } catch (JSchException | IOException e) {
            logger.log(Level.SEVERE, "", e);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "", e);
            Thread.currentThread().interrupt();
        }
    }

    public static JsonObject getConfig() {
        return config;
    }
}
