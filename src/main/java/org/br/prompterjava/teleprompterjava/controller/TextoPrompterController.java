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
import org.br.prompterjava.teleprompterjava.util.DbUtils;
import org.br.prompterjava.teleprompterjava.model.Roteiro;

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
    DatabaseConfig.initDatabase();

    htmlEditor.setHtmlText("<body style=\"background:#1a1a1a; color:white; font-family:Arial;\"></body>");

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
    limpar.setOnAction(e -> {
      htmlEditor.setHtmlText("<body style='background:#1a1a1a; color:white;'></body>");
      LOGGER.info("Tela limpa pelo usuário.");
    });

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
    try {
      List<Roteiro> roteiros = DbUtils.listarTodos();
      if (roteiros.isEmpty()) {
        mostrarAlerta("Aviso", "Nenhum roteiro encontrado.");
        return;
      }

      ChoiceDialog<Roteiro> dialog = new ChoiceDialog<>(roteiros.get(0), roteiros);
      dialog.setTitle("Mudar Roteiro");
      dialog.setHeaderText("Selecione o roteiro:");
      dialog.setContentText("Opções:");

      Node comboBoxNode = dialog.getDialogPane().lookup(".combo-box");
      if (comboBoxNode instanceof ComboBox) {
        ComboBox<Roteiro> cb = (ComboBox<Roteiro>) comboBoxNode;
        cb.setCellFactory(lv -> new ListCell<>() {
          @Override
          protected void updateItem(Roteiro item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? "" : item.titulo());
          }
        });
        cb.setButtonCell(new ListCell<>() {
          @Override
          protected void updateItem(Roteiro item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? "" : item.titulo());
          }
        });
      }

      dialog.showAndWait().ifPresent(selecionado -> {
        if (selecionado != null) {
          carregarConteudoDoBanco(selecionado.id());
        }
      });
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Erro ao listar roteiros para escolha", e);
    }
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
      LOGGER.info("Botões nativos do HTMLEditor estão escondidos");
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
    try {
      List<Roteiro> roteiros = DbUtils.listarTodos();
      for (Roteiro r : roteiros) {
        MenuItem item = new MenuItem(r.titulo());
        item.setOnAction(event -> carregarConteudoDoBanco(r.id()));
        menuPai.getItems().add(item);
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Erro ao carregar lista de roteiros no menu", e);
    }
  }

  private void carregarConteudoDoBanco(int id) {
    try {
      List<Roteiro> lista = DbUtils.listarTodos();
      for (Roteiro r : lista) {
        if (r.id() == id) {
          this.tituloAtual = r.titulo();
          this.idTextoAtual = r.id();
          Platform.runLater(() -> {
            htmlEditor.setHtmlText(r.conteudo());
            htmlEditor.requestFocus();
          });
          LOGGER.info("Roteiro carregado: " + tituloAtual);
          break;
        }
      }
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Erro ao carregar conteúdo do banco", e);
    }
  }

  private void abrirDialogoSalvar() {
    TextInputDialog dialog = new TextInputDialog(tituloAtual);
    dialog.setTitle("Salvar Roteiro");
    dialog.showAndWait().ifPresent(this::salvarNoBanco);
  }

  private void salvarNoBanco(String titulo) {
    try {
      this.idTextoAtual = DbUtils.salvar(titulo, htmlEditor.getHtmlText());
      this.tituloAtual = titulo;
      LOGGER.info("Novo roteiro salvo: " + titulo);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Erro ao salvar novo roteiro", e);
    }
  }

  private void abrirDialogoSalvarEdicao() {

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Salvar Alterações");
    alert.setHeaderText("Deseja salvar as alterações no roteiro?");
    alert.setContentText("Roteiro atual: " + tituloAtual);

    ButtonType btnSalvar = new ButtonType("Salvar");
    ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
    alert.getButtonTypes().setAll(btnSalvar, btnCancelar);

    alert.showAndWait().ifPresent(tipoBotao -> {
      if (tipoBotao == btnSalvar) {
        try {
          DbUtils.atualizar(idTextoAtual, tituloAtual, htmlEditor.getHtmlText());
          LOGGER.info("Alterações salvas para: " + tituloAtual);
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Erro ao atualizar roteiro existente", e);
          mostrarAlerta("Erro", "Falha ao salvar alterações.");
        }
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
          LOGGER.info("Roteiro excluído: " + tituloAtual);
          Platform.runLater(() -> htmlEditor.setHtmlText("<body style='background:#1a1a1a; color:white;'></body>"));
          idTextoAtual = null;
          tituloAtual = "";
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Erro ao excluir roteiro", e);
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