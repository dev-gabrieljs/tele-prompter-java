package org.br.prompterjava.teleprompterjava.controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.br.prompterjava.teleprompterjava.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TextoPrompterController {

  private static final Logger LOGGER = Logger.getLogger(TextoPrompterController.class.getName());

  @FXML
  private VBox containerControles;
  @FXML
  private Label lblSeta;
  @FXML
  private HTMLEditor htmlEditor;
  @FXML
  private FlowPane barraControle;
  private boolean barraEscondida = false;
  private Integer idTextoAtual = null;
  private String tituloAtual = "";
  private ContextMenu menuContexto;

  @FXML
  public void initialize() {
    LOGGER.info("Inicializando TextoPrompterController...");
    DatabaseConfig.initDatabase();

    htmlEditor.setHtmlText("<body style=\"background:#1a1a1a; color:white; font-family:Arial;\"></body>");

    Platform.runLater(() -> {
      configurarMenuContexto();
      moverBotoesNativos();
    });
  }

  @FXML
  private void toggleBarra() {
    // Altura real da barra que está sendo movida
    double altura = barraControle.getHeight();

    TranslateTransition ttBarra = new TranslateTransition(Duration.millis(300), containerControles);
    TranslateTransition ttEditor = new TranslateTransition(Duration.millis(300), htmlEditor);

    if (!barraEscondida) {
      // Esconde a barra (sobe)
      ttBarra.setToY(-altura);
      // Volta o editor para o topo (0)
      ttEditor.setToY(0);

      lblSeta.setText("▼");
      barraEscondida = true;
    } else {
      ttBarra.setToY(0);
      ttEditor.setToY(altura);

      lblSeta.setText("▲");
      barraEscondida = false;
    }

    ttBarra.play();
    ttEditor.play();
  }

  private void configurarMenuContexto() {
    menuContexto = new ContextMenu();

    MenuItem recortar = new MenuItem("Recortar");
    recortar.setOnAction(e -> executarComandoJS("cut"));
    MenuItem copiar = new MenuItem("Copiar");
    copiar.setOnAction(e -> executarComandoJS("copy"));
    MenuItem colar = new MenuItem("Colar");
    colar.setOnAction(e -> executarComandoJS("paste"));

    MenuItem salvar = new MenuItem("Salvar Novo");
    salvar.setOnAction(e -> abrirDialogoSalvar());
    MenuItem editar = new MenuItem("Salvar Alterações");
    editar.setOnAction(e -> abrirDialogoSalvarEdicao());
    MenuItem excluir = new MenuItem("Excluir Roteiro");
    excluir.setOnAction(e -> abrirDialogoExcluir());
    MenuItem limpar = new MenuItem("Limpar Tela");
    limpar.setOnAction(e -> htmlEditor.setHtmlText("<body style='background:#1a1a1a; color:white;'></body>"));

    Menu menuMudarTexto = new Menu("Mudar Texto");
    menuMudarTexto.setOnShowing(e -> carregarListaDoBanco(menuMudarTexto));

    MenuItem itemMudarTexto = new MenuItem("Selecionar outro Roteiro...");
    itemMudarTexto.setOnAction(e -> abrirEscolhaDeTexto());

    menuContexto.getItems().addAll(
        recortar, copiar, colar, new SeparatorMenuItem(),
        editar, excluir, new SeparatorMenuItem(),
        salvar, limpar, new SeparatorMenuItem(),
        itemMudarTexto
    );

    Node webView = htmlEditor.lookup("WebView");
    if (webView != null) {
      WebView wv = (WebView) webView;
      wv.setContextMenuEnabled(false);
      wv.setOnContextMenuRequested(e -> menuContexto.show(wv, e.getScreenX(), e.getScreenY()));
      wv.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
        if (menuContexto.isShowing()) menuContexto.hide();
      });
    }
  }

  private void abrirEscolhaDeTexto() {
    List<String> titulos = new ArrayList<>();
    Map<String, Integer> mapaTextos = new HashMap<>();

    try (Connection c = DatabaseConfig.getConnection();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery("SELECT id, titulo FROM textos ORDER BY id DESC")) {
      while (rs.next()) {
        String t = rs.getString("titulo");
        int id = rs.getInt("id");
        titulos.add(t);
        mapaTextos.put(t, id);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Erro ao listar roteiros", e);
    }

    if (titulos.isEmpty()) {
      mostrarAlerta("Aviso", "Nenhum roteiro encontrado.");
      return;
    }

    ChoiceDialog<String> dialog = new ChoiceDialog<>(titulos.get(0), titulos);
    dialog.setTitle("Mudar Roteiro");
    dialog.setHeaderText("Selecione o roteiro:");
    dialog.setContentText("Opções:");

    dialog.showAndWait().ifPresent(selecionado -> {
      Integer id = mapaTextos.get(selecionado);
      carregarConteudoDoBanco(id);
    });
  }

  private void moverBotoesNativos() {
    Platform.runLater(() -> {
      Node topToolbar = htmlEditor.lookup(".top-toolbar");
      Node bottomToolbar = htmlEditor.lookup(".bottom-toolbar");

      if (topToolbar instanceof ToolBar) {
        ToolBar tb1 = (ToolBar) topToolbar;
        barraControle.getChildren().addAll(new ArrayList<>(tb1.getItems()));
        tb1.setVisible(false);
        tb1.setManaged(false);
      }

      if (bottomToolbar instanceof ToolBar) {
        ToolBar tb2 = (ToolBar) bottomToolbar;
        barraControle.getChildren().addAll(new ArrayList<>(tb2.getItems()));
        tb2.setVisible(false);
        tb2.setManaged(false);
      }

      barraControle.setStyle("-fx-background-color: #262626; -fx-padding: 5; -fx-alignment: CENTER_LEFT;");
    });
  }

  private void executarComandoJS(String comando) {
    WebView wv = (WebView) htmlEditor.lookup("WebView");
    if (wv != null) {
      wv.getEngine().executeScript("document.execCommand('" + comando + "')");
    }
  }

  private void carregarListaDoBanco(Menu menuPai) {
    menuPai.getItems().clear();
    try (Connection c = DatabaseConfig.getConnection();
         Statement st = c.createStatement();
         ResultSet rs = st.executeQuery("SELECT id, titulo FROM textos ORDER BY id DESC")) {
      while (rs.next()) {
        final int idParaCarregar = rs.getInt("id");
        final String tituloBotao = rs.getString("titulo");
        MenuItem item = new MenuItem(tituloBotao);
        item.setOnAction(event -> carregarConteudoDoBanco(idParaCarregar));
        menuPai.getItems().add(item);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Erro ao listar", e);
    }
  }

  private void carregarConteudoDoBanco(int id) {
    try (Connection c = DatabaseConfig.getConnection();
         PreparedStatement ps = c.prepareStatement("SELECT titulo, conteudo FROM textos WHERE id=?")) {
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        this.tituloAtual = rs.getString("titulo");
        this.idTextoAtual = id;
        String conteudo = rs.getString("conteudo");
        Platform.runLater(() -> {
          htmlEditor.setHtmlText(conteudo);
          htmlEditor.requestFocus();
        });
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Erro ao carregar", e);
    }
  }

  private void abrirDialogoSalvar() {
    TextInputDialog dialog = new TextInputDialog(tituloAtual);
    dialog.setTitle("Salvar Roteiro");
    dialog.showAndWait().ifPresent(this::salvarNoBanco);
  }

  private void salvarNoBanco(String titulo) {
    String sql = "INSERT INTO textos (titulo, conteudo) VALUES (?, ?)";
    try (Connection c = DatabaseConfig.getConnection();
         PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, titulo);
      ps.setString(2, htmlEditor.getHtmlText());
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) idTextoAtual = rs.getInt(1);
        tituloAtual = titulo;
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Erro ao salvar", e);
    }
  }

  private void abrirDialogoSalvarEdicao() {
    if (idTextoAtual == null) return;
    TextInputDialog dialog = new TextInputDialog(tituloAtual);
    dialog.setTitle("Editar Roteiro");
    dialog.showAndWait().ifPresent(novoTitulo -> {
      String sql = "UPDATE textos SET titulo=?, conteudo=? WHERE id=?";
      try (Connection c = DatabaseConfig.getConnection();
           PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setString(1, novoTitulo);
        ps.setString(2, htmlEditor.getHtmlText());
        ps.setInt(3, idTextoAtual);
        ps.executeUpdate();
        tituloAtual = novoTitulo;
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Erro ao editar", e);
      }
    });
  }

  private void abrirDialogoExcluir() {
    if (idTextoAtual == null) return;
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Excluir: " + tituloAtual + "?", ButtonType.OK, ButtonType.CANCEL);
    alert.showAndWait().ifPresent(btn -> {
      if (btn == ButtonType.OK) {
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM textos WHERE id=?")) {
          ps.setInt(1, idTextoAtual);
          ps.executeUpdate();
          Platform.runLater(() -> htmlEditor.setHtmlText("<body style='background:#1a1a1a; color:white;'></body>"));
          idTextoAtual = null;
          tituloAtual = "";
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Erro ao excluir", e);
        }
      }
    });
  }

  private void mostrarAlerta(String titulo, String msg) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(titulo);
    alert.setContentText(msg);
    alert.show();
  }

  public HTMLEditor getHtmlEditor() {
    return htmlEditor;
  }
}