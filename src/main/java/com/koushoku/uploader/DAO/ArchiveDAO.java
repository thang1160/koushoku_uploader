package com.koushoku.uploader.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.koushoku.uploader.classes.Archive;

public class ArchiveDAO {
    private static Logger logger = Logger.getLogger(ArchiveDAO.class.getName());
    private static int cosplayerId = 0;

    static {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = Db.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select id from tag where slug  = 'cosplayer'");
            if (rs.next()) {
                cosplayerId = rs.getInt("id");
            }
        } catch (Exception e) {
            logger.log(Level.INFO, "get cosplayer id error", e);
        } finally {
            Db.close(con, stmt, rs);
        }
    }

    public static List<Archive> getArchives() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Archive> archives = new ArrayList<>();
        try {
            con = Db.getConnection();
            ps = con.prepareStatement(
                    "select a.id, a.title from archive a join archive_tags b on a.id = b.archive_id where b.tag_id = ? order by a.published_at desc");
            ps.setInt(1, cosplayerId);
            rs = ps.executeQuery();
            while (rs.next()) {
                archives.add(new Archive(rs.getInt("id"), rs.getString("title")));
            }
        } catch (Exception e) {
            logger.log(Level.INFO, "get cosplayer id error", e);
        } finally {
            Db.close(con, ps, rs);
        }
        return archives;
    }

    public static void UpdateArchive(Archive archive) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = Db.getConnection();
            ps = con.prepareStatement("update archive set source = ? where id = ?");
            ps.setString(1, archive.getSource());
            ps.setInt(2, archive.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            logger.log(Level.INFO, "update archive error", e);
        } finally {
            Db.close(con, ps);
        }
    }
}
