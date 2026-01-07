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
import org.br.prompterjava.teleprompterjava.model.Roteiro;
import org.br.prompterjava.teleprompterjava.util.DbUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    // Estilo inicial do editor
    limparTela();

    Platform.runLater(() -> {
      configurarMenuContexto();
      moverBotoesNativos();
    });
  }

  @FXML
  private void toggleBarra() {
    double altura = barraControle.getHeight();
    TranslateTransition ttBarra = new TranslateTransition(Duration.millis(300), containerControles);
    TranslateTransition ttEditor = new TranslateTransition(Duration.millis(300), htmlEditor);

    if (!barraEscondida) {
      ttBarra.setToY(-altura);
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

    MenuItem salvar = new MenuItem("Salvar Novo");
    salvar.setOnAction(e -> abrirDialogoSalvar());

    MenuItem editar = new MenuItem("Salvar Alterações");
    editar.setOnAction(e -> abrirDialogoSalvarEdicao());

    MenuItem excluir = new MenuItem("Excluir Roteiro");
    excluir.setOnAction(e -> abrirDialogoExcluir());

    MenuItem limpar = new MenuItem("Limpar Tela");
    limpar.setOnAction(e -> limparTela());

    Menu menuMudarTexto = new Menu("Mudar Texto rápido");
    menuMudarTexto.setOnShowing(e -> popularMenuRapido(menuMudarTexto));

    MenuItem itemEscolherTexto = new MenuItem("Abrir Lista Completa...");
    itemEscolherTexto.setOnAction(e -> abrirEscolhaDeTexto());

    menuContexto.getItems().addAll(
        new MenuItem("Recortar") {{
          setOnAction(e -> executarComandoJS("cut"));
        }},
        new MenuItem("Copiar") {{
          setOnAction(e -> executarComandoJS("copy"));
        }},
        new MenuItem("Colar") {{
          setOnAction(e -> executarComandoJS("paste"));
        }},
        new SeparatorMenuItem(),
        editar, excluir, new SeparatorMenuItem(),
        salvar, limpar, new SeparatorMenuItem(),
        menuMudarTexto, itemEscolherTexto
    );

    Node webView = htmlEditor.lookup("WebView");
    if (webView instanceof WebView wv) {
      wv.setContextMenuEnabled(false);
      wv.setOnContextMenuRequested(e -> menuContexto.show(wv, e.getScreenX(), e.getScreenY()));
      wv.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
        if (menuContexto.isShowing()) menuContexto.hide();
      });
    }
  }

  private void abrirEscolhaDeTexto() {
    try {
      List<Roteiro> roteiros = DbUtils.listarTodos();
      if (roteiros.isEmpty()) {
        mostrarAlerta("Aviso", "Nenhum roteiro encontrado.");
        return;
      }

      ChoiceDialog<Roteiro> dialog = new ChoiceDialog<>(roteiros.get(0), roteiros);
      dialog.setTitle("Mudar Roteiro");
      dialog.setHeaderText("Selecione o roteiro desejado:");

      dialog.showAndWait().ifPresent(this::aplicarRoteiroNaTela);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Erro ao carregar lista", e);
    }
  }

  private void popularMenuRapido(Menu menuPai) {
    menuPai.getItems().clear();
    try {
      List<Roteiro> roteiros = DbUtils.listarTodos();
      for (Roteiro r : roteiros) {
        MenuItem item = new MenuItem(r.titulo());
        item.setOnAction(e -> aplicarRoteiroNaTela(r));
        menuPai.getItems().add(item);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Erro no menu rápido", e);
    }
  }

  private void aplicarRoteiroNaTela(Roteiro roteiro) {
    this.idTextoAtual = roteiro.id();
    this.tituloAtual = roteiro.titulo();
    Platform.runLater(() -> {
      htmlEditor.setHtmlText(roteiro.conteudo());
      htmlEditor.requestFocus();
    });
  }

  private void abrirDialogoSalvar() {
    TextInputDialog dialog = new TextInputDialog(tituloAtual);
    dialog.setTitle("Salvar Roteiro");
    dialog.setHeaderText("Digite o título do novo roteiro:");
    dialog.showAndWait().ifPresent(titulo -> {
      try {
        this.idTextoAtual = DbUtils.salvar(titulo, htmlEditor.getHtmlText());
        this.tituloAtual = titulo;
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Erro ao salvar", e);
      }
    });
  }

  private void abrirDialogoSalvarEdicao() {
    if (idTextoAtual == null) {
      mostrarAlerta("Aviso", "Este texto ainda não foi salvo no banco.");
      return;
    }
    TextInputDialog dialog = new TextInputDialog(tituloAtual);
    dialog.setTitle("Editar Roteiro");
    dialog.showAndWait().ifPresent(novoTitulo -> {
      try {
        DbUtils.atualizar(idTextoAtual, novoTitulo, htmlEditor.getHtmlText());
        this.tituloAtual = novoTitulo;
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
        try {
          DbUtils.excluir(idTextoAtual);
          limparTela();
          this.idTextoAtual = null;
          this.tituloAtual = "";
        } catch (SQLException e) {
          LOGGER.log(Level.SEVERE, "Erro ao excluir", e);
        }
      }
    });
  }

  private void limparTela() {
    htmlEditor.setHtmlText("<body style='background:#1a1a1a; color:white; font-family:Arial;'></body>");
  }

  private void moverBotoesNativos() {
    Node topToolbar = htmlEditor.lookup(".top-toolbar");
    Node bottomToolbar = htmlEditor.lookup(".bottom-toolbar");

    if (topToolbar instanceof ToolBar tb1) {
      barraControle.getChildren().addAll(new ArrayList<>(tb1.getItems()));
      tb1.setVisible(false);
      tb1.setManaged(false);
    }
    if (bottomToolbar instanceof ToolBar tb2) {
      barraControle.getChildren().addAll(new ArrayList<>(tb2.getItems()));
      tb2.setVisible(false);
      tb2.setManaged(false);
    }
    barraControle.setStyle("-fx-background-color: #262626; -fx-padding: 5; -fx-alignment: CENTER_LEFT;");
  }

  private void executarComandoJS(String comando) {
    if (htmlEditor.lookup("WebView") instanceof WebView wv) {
      wv.getEngine().executeScript("document.execCommand('" + comando + "')");
    }
  }

  private void mostrarAlerta(String titulo, String msg) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(titulo);
    alert.setHeaderText(null);
    alert.setContentText(msg);
    alert.show();
  }

  public HTMLEditor getHtmlEditor() {
    return htmlEditor;
  }
}