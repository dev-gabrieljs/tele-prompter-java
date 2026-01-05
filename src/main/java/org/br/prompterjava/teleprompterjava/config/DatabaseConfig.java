package org.br.prompterjava.teleprompterjava.config;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseConfig {
  private static final String URL = "jdbc:h2:./prompter_db;DB_CLOSE_DELAY=-1";
  private static final String USER = "sa";
  private static final String PASS = "";

  public static Connection getConnection() throws Exception {
    return DriverManager.getConnection(URL, USER, PASS);
  }

  public static void initDatabase() {
    try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
      stmt.execute("CREATE TABLE IF NOT EXISTS textos (" +
          "id INT AUTO_INCREMENT PRIMARY KEY, " +
          "titulo VARCHAR(255) NOT NULL, " +
          "conteudo CLOB NOT NULL)");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}