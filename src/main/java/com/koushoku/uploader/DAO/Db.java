package com.koushoku.uploader.DAO;

import com.google.gson.JsonObject;
import com.koushoku.uploader.App;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Db {
    private static Logger logger = Logger.getLogger(Db.class.getName());
    private static HikariDataSource pool;

    static {
        try {
            JsonObject db = App.getConfig().get("db").getAsJsonObject();
            Properties props = new Properties();
            props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
            props.setProperty("dataSource.user", db.get("username").getAsString());
            props.setProperty("dataSource.password", db.get("password").getAsString());
            props.setProperty("dataSource.databaseName", db.get("database").getAsString());
            props.setProperty("dataSource.portNumber", db.get("port").getAsString());
            props.setProperty("dataSource.serverName", App.getConfig().get("host").getAsString());
            props.put("dataSource.logWriter", new PrintWriter(System.out));
            HikariConfig hikariConfig114 = new HikariConfig(props);
            hikariConfig114.setPoolName("koushoku");
            hikariConfig114.setMinimumIdle(1);
            hikariConfig114.setMaximumPoolSize(3);
            hikariConfig114.setConnectionTestQuery("SELECT now();");
            pool = new HikariDataSource(hikariConfig114);
            logger.info("Pool created");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "", e);
        }
    }

    public static void close(AutoCloseable... objs) {
        for (AutoCloseable obj : objs)
            try {
                if (obj != null)
                    obj.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "", e);
            }
    }

    protected static Connection getConnection() throws SQLException {
        return pool.getConnection();
    }
}
