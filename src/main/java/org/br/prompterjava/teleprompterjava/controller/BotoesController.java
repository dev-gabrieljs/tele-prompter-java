package org.br.prompterjava.teleprompterjava.controller;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class BotoesController {
  @FXML
  public Label lblValorFonte;
  @FXML
  public Label lblValorVelocidade;
  @FXML
  public Circle circuloPulso;
  @FXML
  public ToggleButton btnPlayPause;
  @FXML
  public HBox barraControle;
  public Button btnFormatar;

  private TextArea areaTexto;
  private double velocidadeAtual = 1.0;
  private int fonteAtual = 14;
  private FadeTransition animacaoPulso;
  private Timeline motorRolagem;

  @FXML
  public void initialize() {
    animacaoPulso = new FadeTransition(Duration.millis(1000), circuloPulso);
    animacaoPulso.setFromValue(1.0);
    animacaoPulso.setToValue(0.2);
    animacaoPulso.setCycleCount(Timeline.INDEFINITE);
    animacaoPulso.setAutoReverse(true);

    lblValorFonte.setText(String.valueOf(fonteAtual));
    lblValorVelocidade.setText("1.0x");

    motorRolagem = new Timeline(new KeyFrame(Duration.millis(20), e -> {
      if (areaTexto != null && btnPlayPause.isSelected()) {
        double deslocamento = velocidadeAtual * 0.4;
        areaTexto.setScrollTop(areaTexto.getScrollTop() + deslocamento);
      }
    }));
    motorRolagem.setCycleCount(Timeline.INDEFINITE);
  }

  public void setAreaTexto(TextArea areaTexto) {
    this.areaTexto = areaTexto;

    this.areaTexto.setWrapText(true);

    atualizarInterfaceFonte();
  }

  @FXML
  public void aoClicarPlayPause(ActionEvent evento) {
    if (btnPlayPause.isSelected()) {
      btnPlayPause.setText("⏸");
      circuloPulso.setVisible(true);
      animacaoPulso.play();
      motorRolagem.play();
    } else {
      btnPlayPause.setText("▶");
      circuloPulso.setVisible(false);
      animacaoPulso.stop();
      motorRolagem.pause();
    }
  }

  @FXML
  public void aoReiniciar(ActionEvent evento) {
    if (areaTexto != null) {
      motorRolagem.pause();
      btnPlayPause.setSelected(false);
      btnPlayPause.setText("▶");
      circuloPulso.setVisible(false);
      animacaoPulso.stop();

      areaTexto.setScrollTop(0);
      areaTexto.selectRange(0, 0);
    }
  }

  @FXML
  public void decrementarVelocidade(ActionEvent evento) {
    if (velocidadeAtual > 0.5) {
      velocidadeAtual -= 0.5;
      atualizarInterfaceVelocidade();
    }
  }

  @FXML
  public void incrementarVelocidade(ActionEvent evento) {
    if (velocidadeAtual < 10.0) {
      velocidadeAtual += 0.5;
      atualizarInterfaceVelocidade();
    }
  }

  @FXML
  public void incrementarFonte(ActionEvent evento) {
    if (fonteAtual < 150) {
      fonteAtual += 4;
      atualizarInterfaceFonte();
    }
  }

  @FXML
  public void decrementarFonte(ActionEvent evento) {
    if (fonteAtual > 9) {
      fonteAtual -= 3;
      atualizarInterfaceFonte();
    }
  }

  private void atualizarInterfaceVelocidade() {
    lblValorVelocidade.setText(String.format("%.1fx", velocidadeAtual));
  }

  private void atualizarInterfaceFonte() {
    lblValorFonte.setText(String.valueOf(fonteAtual));
    if (areaTexto != null) {
      areaTexto.setStyle(
          "-fx-font-size: " + fonteAtual + "px; " +
              "-fx-text-fill: white; " +
              "-fx-control-inner-background: #1a1a1a; " +
              "-fx-background-color: transparent; " +
              "-fx-padding: 0 15 0 0;"
      );
    }
  }


  public void settings(ActionEvent actionEvent) {
  }

  @FXML
  private void abrirFormatacao() {
    ContextMenu menuFormatacao = new ContextMenu();
    menuFormatacao.getStyleClass().add("menu-formatacao");

    MenuItem negrito = new MenuItem("Negrito");
    negrito.setOnAction(e -> aplicarTag("**", "**"));

    MenuItem codigo = new MenuItem("Bloco de Código");
    codigo.setOnAction(e -> aplicarTag("\n```\n", "\n```\n"));

    MenuItem destaque = new MenuItem("Destaque Verde");
    destaque.setOnAction(e -> aplicarTag("<green>", "</green>"));

    MenuItem limpar = new MenuItem("Limpar Formatação");
    limpar.setOnAction(e -> removerTags());

    menuFormatacao.getItems().addAll(negrito, codigo, destaque, new SeparatorMenuItem(), limpar);
    menuFormatacao.show(btnFormatar, javafx.geometry.Side.TOP, 0, -10);
  }

  private void aplicarTag(String inicio, String fim) {
    if (areaTexto != null) {
      String selecionado = areaTexto.getSelectedText();
      if (!selecionado.isEmpty()) {
        areaTexto.replaceSelection(inicio + selecionado + fim);
      } else {
        int posicao = areaTexto.getCaretPosition();
        areaTexto.insertText(posicao, inicio + fim);
        areaTexto.positionCaret(posicao + inicio.length());
      }
    }
  }

  // Método para limpar tags básicas (Markdown simples)
  private void removerTags() {
    if (areaTexto != null) {
      String texto = areaTexto.getSelectedText();
      if (!texto.isEmpty()) {
        // Regex simples para remover **, ``` e as tags de cor que criamos
        String limpo = texto.replace("**", "")
            .replace("```", "")
            .replaceAll("<[^>]*>", "");
        areaTexto.replaceSelection(limpo);
      }
    }
  }
}