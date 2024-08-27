package pd.guimx.utils;

import java.sql.*;

public class ManageDatabase {
    public Connection conn;

    public ManageDatabase(){
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:players.db");
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void createTable(){
        //return System.getProperty("user.dir");
        try {
            String sql = "CREATE TABLE IF NOT EXISTS players (" +
                    "player TEXT NOT NULL PRIMARY KEY," +
                    "afk REAL," +
                    "isBanned BOOLEAN," +
                    "UNIQUE (player COLLATE NOCASE))";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public boolean userExists(String player){
        try{
            String sql = "SELECT * FROM players WHERE player = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,player);
            ResultSet rs = pstmt.executeQuery();;
            return rs.next();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean userBanned(String player){
        try{
            String sql = "SELECT * FROM players WHERE player = ? AND isBanned = 1";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,player);
            ResultSet rs = pstmt.executeQuery();;
            return rs.next();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUser(String player){
        if (userExists(player)){
            return;
        }
        try{
            String sql = "INSERT INTO players VALUES (?, 0, 0)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,player);
            pstmt.executeUpdate();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean banOrUnbanPlayer(String player, boolean op){
        //Returns true if ran without issue, false if player already banned/unbanned or doesn't exist
        if (!userExists(player) || userBanned(player)==op){
            return false;
        }
        try{
            String sql = "UPDATE players " +
                    "SET isBanned = ? " +
                    "WHERE player = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1,op);
            pstmt.setString(2,player);
            pstmt.executeUpdate();
            return true;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
