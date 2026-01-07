package org.br.prompterjava.teleprompterjava.util;

import org.br.prompterjava.teleprompterjava.config.DatabaseConfig;
import org.br.prompterjava.teleprompterjava.model.Roteiro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DbUtils {
  public static List<Roteiro> listarTodos() throws Exception {
    List<Roteiro> lista = new ArrayList<>();
    String sql = "SELECT id, titulo, conteudo FROM textos ORDER BY id DESC";
    try (Connection c = DatabaseConfig.getConnection();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
      while (rs.next()) {
        lista.add(new Roteiro(rs.getInt("id"), rs.getString("titulo"), rs.getString("conteudo")));
      }
    }
    return lista;
  }

  public static Integer salvar(String titulo, String conteudo) throws SQLException {
    String sql = "INSERT INTO textos (titulo, conteudo) VALUES (?, ?)";
    try (Connection c = DatabaseConfig.getConnection();
         PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, titulo);
      ps.setString(2, conteudo);
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        return rs.next() ? rs.getInt(1) : null;
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void atualizar(int id, String titulo, String conteudo) throws Exception {
    String sql = "UPDATE textos SET titulo=?, conteudo=? WHERE id=?";
    try (Connection c = DatabaseConfig.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, titulo);
      ps.setString(2, conteudo);
      ps.setInt(3, id);
      ps.executeUpdate();
    }
  }

  public static void excluir(int id) throws SQLException {
    try (Connection c = DatabaseConfig.getConnection();
         PreparedStatement ps = c.prepareStatement("DELETE FROM textos WHERE id=?")) {
      ps.setInt(1, id);
      ps.executeUpdate();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
