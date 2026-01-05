package org.br.prompterjava.teleprompterjava.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import org.br.prompterjava.teleprompterjava.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TextoPrompterController {
  private static final Logger LOGGER = Logger.getLogger(TextoPrompterController.class.getName());

  @FXML
  private TextArea textArea;

  public TextArea getTextArea() {
    return textArea;
  }
  private Integer idTextoAtual = null;
  private String tituloAtual = "";

  @FXML
  public void initialize() {
    LOGGER.info("Inicializando TextoPrompterController e Banco de Dados H2...");
    DatabaseConfig.initDatabase();
    configurarMenuContexto();
  }

  private void configurarMenuContexto() {
    ContextMenu menu = new ContextMenu();

    MenuItem selecinarTudo = new MenuItem("Selecionar Tudo");
    selecinarTudo.setOnAction(e -> textArea.selectAll());

    MenuItem copiar = new MenuItem("Copiar");
    copiar.setOnAction(e -> textArea.copy());

    MenuItem colar = new MenuItem("Colar");
    colar.setOnAction(e -> textArea.paste());

    MenuItem recortar = new MenuItem("Recortar");
    recortar.setOnAction(e -> textArea.cut());

    MenuItem itemSalvar = new MenuItem("Salvar");
    itemSalvar.setOnAction(e -> abrirDialogoSalvar());

    MenuItem itemLimpar = new MenuItem("Limpar Tela");
    itemLimpar.setOnAction(e -> textArea.clear());

    Menu menuMudarTexto = new Menu("Mudar Texto");
    menuMudarTexto.getItems().add(new MenuItem("Carregando..."));
    menuMudarTexto.setOnShowing(e -> {
      LOGGER.info("Menu 'Mudar Texto' clicado. Buscando dados no H2...");
      carregarListaDoBanco(menuMudarTexto);
    });

    MenuItem editar = new MenuItem("Salvar Edição");
    editar.setOnAction(e -> abrirDialogoSalvarEdicao());

    MenuItem exluir = new MenuItem("Exluir");
    exluir.setOnAction(e -> abrirDialogoExcluir());


    menu.getItems().addAll(selecinarTudo,
        copiar, colar, recortar,editar,exluir,
        new SeparatorMenuItem(),
        itemSalvar, itemLimpar,
        new SeparatorMenuItem(),
        menuMudarTexto
    );

    menu.setOnShowing(e -> {
      copiar.setDisable(textArea.getSelectedText().isEmpty());
      recortar.setDisable(textArea.getSelectedText().isEmpty());
    });

    textArea.setContextMenu(menu);
  }

  private void abrirDialogoSalvar() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Salvar Roteiro");
    dialog.setHeaderText("Salvar no Banco de Dados H2");
    dialog.setContentText("Nome do Roteiro:");

    dialog.showAndWait().ifPresent(this::salvarNoBanco);
  }

  private void abrirDialogoSalvarEdicao() {
    if (idTextoAtual == null) {
      LOGGER.warning("Tentativa de editar sem um texto carregado.");
      return;
    }
    TextInputDialog dialog = new TextInputDialog(tituloAtual);
    dialog.setTitle("Editar Roteiro");
    dialog.setHeaderText("Atualizando Roteiro: " + tituloAtual);
    dialog.setContentText("Novo nome (ou mantenha o mesmo):");

    dialog.showAndWait().ifPresent(novoTitulo -> {
      String sql = "UPDATE textos SET titulo = ?, conteudo = ? WHERE id = ?";
      try (Connection conn = DatabaseConfig.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, novoTitulo);
        pstmt.setString(2, textArea.getText());
        pstmt.setInt(3, idTextoAtual);
        pstmt.executeUpdate();

        tituloAtual = novoTitulo;
        LOGGER.info("Log: Roteiro ID " + idTextoAtual + " EDITADO para '" + novoTitulo + "'");

      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Erro ao editar no H2", e);
      }
    });
  }

  private void abrirDialogoExcluir() {
    if (idTextoAtual == null) return;

    javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
    alerta.setTitle("Excluir Roteiro");
    alerta.setHeaderText("Deseja realmente excluir: " + tituloAtual + "?");
    alerta.setContentText("Essa operação não pode ser desfeita.");

    alerta.showAndWait().ifPresent(botao -> {
      if (botao == javafx.scene.control.ButtonType.OK) {
        String sql = "DELETE FROM textos WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

          pstmt.setInt(1, idTextoAtual);
          pstmt.executeUpdate();

          LOGGER.info("Log: Roteiro '" + tituloAtual + "' EXCLUÍDO do banco.");
          textArea.clear();
          idTextoAtual = null;
          tituloAtual = "";

        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Erro ao excluir do H2", e);
        }
      }
    });
  }

  private void salvarNoBanco(String titulo) {
    String sql = "INSERT INTO textos (titulo, conteudo) VALUES (?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, titulo);
      pstmt.setString(2, textArea.getText());
      pstmt.executeUpdate();

      LOGGER.info("Roteiro '" + titulo + "' salvo com sucesso no banco de dados.");

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Erro ao salvar roteiro: " + titulo, e);
    }
  }

  private void carregarListaDoBanco(Menu menuPai) {
    String sql = "SELECT id, titulo FROM textos ORDER BY id DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      java.util.List<MenuItem> novosItens = new java.util.ArrayList<>();

      while (rs.next()) {
        int id = rs.getInt("id");
        String titulo = rs.getString("titulo");

        MenuItem item = new MenuItem(titulo);
        item.setOnAction(e -> carregarConteudoDoBanco(id));
        novosItens.add(item);
      }

      javafx.application.Platform.runLater(() -> {
        menuPai.getItems().setAll(novosItens);
        if (novosItens.isEmpty()) {
          MenuItem vazio = new MenuItem("Nenhum roteiro encontrado");
          vazio.setDisable(true);
          menuPai.getItems().add(vazio);
        }
        LOGGER.info("Interface atualizada com " + novosItens.size() + " roteiros.");
      });

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Erro ao buscar dados do H2", e);
    }
  }

  private void carregarConteudoDoBanco(int id) {
    String sql = "SELECT id, titulo, conteudo FROM textos WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, id);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        this.idTextoAtual = rs.getInt("id");
        this.tituloAtual = rs.getString("titulo");
        textArea.setText(rs.getString("conteudo"));
        LOGGER.info("Roteiro '" + tituloAtual + "' (ID: " + idTextoAtual + ") carregado para a tela.");
      }

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Erro ao carregar conteúdo do roteiro ID: " + id, e);
    }
  }

  public void setEstiloDinamico(int tamanhoFonte) {
    textArea.setStyle(
        "-fx-font-size: " + tamanhoFonte + "px; " +
            "-fx-text-fill: white; " +
            "-fx-control-inner-background: #1a1a1a; " +
            "-fx-background-color: transparent; " +
            "-fx-padding: 0 15 0 0;"
    );
  }
}