package pd.guimx.utils;

import java.sql.*;

public class ManageDatabase {
    public Connection conn;

    public ManageDatabase(){
        try {
            this.conn = DriverManager.getConnection("jdbc:sqlite:plugins/Permadeath/players.db");
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void createTable(){
        //return System.getProperty("user.dir");
        try {
            String sql = "CREATE TABLE IF NOT EXISTS players (" +
                    "player TEXT NOT NULL PRIMARY KEY," +
                    "lifes INTEGER NOT NULL," +
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
            String sql = "SELECT * FROM players WHERE player = ? AND lifes <= 0";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,player);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUser(String player, int startingLifes){
        if (userExists(player)){
            return;
        }
        try{
            String sql = "INSERT INTO players VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,player);
            pstmt.setInt(2,startingLifes);
            pstmt.executeUpdate();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean setLifes(String player, int newLifes){
        //Returns true if ran without issue, false if player doesn't exist
        if (!userExists(player)){
            return false;
        }
        try{
            String sql = "UPDATE players " +
                    "SET lifes = ? " +
                    "WHERE player = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1,newLifes);
            pstmt.setString(2,player);
            pstmt.executeUpdate();
            return true;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public int getLifes(String player){
        if (!userExists(player)){
            return -1;
        }
        try{
            String sql = "SELECT lifes FROM players WHERE player = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,player);
            ResultSet rs = pstmt.executeQuery();
            return rs.getInt("lifes");
        }catch (SQLException e){
            e.printStackTrace();
        }
        return -1;
    }
}
