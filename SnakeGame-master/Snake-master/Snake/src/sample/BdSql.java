package sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BdSql {
    static String url       = "jdbc:mysql://localhost:3306/snake?useSSL=false";
    static String user      = "admin";
    static String password  = "admin";

    // Fonction pour récupérer le score en Base de données
    public static List<Integer> RecupScore (){
      try{

          // Connection
          Connection conn =DriverManager.getConnection(url, user, password);
          Statement stmt  = conn.createStatement();
          String sql = "SELECT * FROM score ORDER BY classement ASC";

          ResultSet rs    = stmt.executeQuery(sql);
          List<Integer> listScore = new ArrayList<>();
          while (rs.next()) {
                            listScore.add(rs.getInt("valeur"));
          }
          rs.close();
          stmt.close();
          return listScore;
      } catch(SQLException e) {
          System.out.println(e.getMessage());
      }
      return null;
    }
    // Inserer le score en base de données
    public static void InsertScore (List<Integer> listScore){
        try{

            // Connection
            Connection conn =DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            String sql="" ;
            // Parcours de la liste de score et fonction Update
            for(int i=0;i<listScore.size();i++){
                sql = "UPDATE score SET valeur=" + listScore.get(i) +" WHERE classement=" + (i+1) + ";";
                stmt.executeUpdate(sql);
            }

            stmt.executeUpdate(sql);

            return ;
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        return ;
    }
}

