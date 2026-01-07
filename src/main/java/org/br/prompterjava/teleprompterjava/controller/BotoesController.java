package org.br.prompterjava.teleprompterjava.controller;

import com.sun.jna.platform.unix.X11.Window;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.br.prompterjava.teleprompterjava.interfaces.CustomUser32;
import org.br.prompterjava.teleprompterjava.util.WindowUtils;

import java.lang.ModuleLayer.Controller;
import java.util.logging.Logger;

public class BotoesController {
  private static final Logger LOGGER = Logger.getLogger(BotoesController.class.getName());

  @FXML public Label lblValorVelocidade;
  @FXML public Circle circuloPulso;
  @FXML public ToggleButton btnPlayPause;
  @FXML public HBox barraControle;
  public Slider sliderTransparencia;

  private HTMLEditor htmlEditor;
  private double velocidadeAtual = 1.0;
  private FadeTransition animacaoPulso;
  private Timeline motorRolagem;
  private boolean modoFantasma = false;

  @FXML
  public void initialize() {
    animacaoPulso = new FadeTransition(Duration.millis(1000), circuloPulso);
    animacaoPulso.setFromValue(1.0);
    animacaoPulso.setToValue(0.2);
    animacaoPulso.setCycleCount(Timeline.INDEFINITE);
    animacaoPulso.setAutoReverse(true);

    lblValorVelocidade.setText("0.0x");

    motorRolagem = new Timeline(new KeyFrame(Duration.millis(20), e -> {
      if (htmlEditor != null && btnPlayPause.isSelected()) {
        WebView webView = (WebView) htmlEditor.lookup("WebView");
        if (webView != null) {
          double deslocamento = velocidadeAtual * 0.8;
          webView.getEngine().executeScript("window.scrollBy(0, " + deslocamento + ");");
        }
      }
    }));
    motorRolagem.setCycleCount(Timeline.INDEFINITE);

    sliderTransparencia.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (barraControle.getScene() != null) {
        Stage stage = (Stage) barraControle.getScene().getWindow();

        double opacidade = newValue.doubleValue() / 100.0;
        stage.setOpacity(opacidade);
      }
    });

    configurarAtalhoScrollLock();
  }

  public void setHtmlEditor(HTMLEditor htmlEditor) {
    this.htmlEditor = htmlEditor;
  }

  @FXML
  public void aoClicarPlayPause(ActionEvent evento) {
    if (btnPlayPause.isSelected()) {
      btnPlayPause.setText("⏸");
      circuloPulso.setVisible(true);
      animacaoPulso.play();
      motorRolagem.play();
    } else {
      interromperMotor();
    }
  }

  @FXML
  public void aoReiniciar(ActionEvent evento) {
    interromperMotor();
    if (htmlEditor != null) {
      WebView webView = (WebView) htmlEditor.lookup("WebView");
      if (webView != null) {
        // window.scrollTo(0, 0) volta para o topo absoluto da página
        webView.getEngine().executeScript("window.scrollTo(0, 0);");
        LOGGER.info("Texto reiniciado para o topo.");
      }
    }
  }

  private void interromperMotor() {
    btnPlayPause.setText("▶");
    btnPlayPause.setSelected(false);
    circuloPulso.setVisible(false);
    animacaoPulso.stop();
    motorRolagem.pause();
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
  public void inserirBlocoCodigo(ActionEvent evento) {
    if (htmlEditor == null) return;
    WebView webView = (WebView) htmlEditor.lookup("WebView");
    if (webView == null) return;

    String script =
        "var sel = window.getSelection();" +
            "if (sel.rangeCount) {" +
            "   var range = sel.getRangeAt(0);" +
            "   range.deleteContents();" +

            "   var table = document.createElement('table');" +
            "   table.style.cssText = 'width:100%; background-color:#1e1e1e; border-left:4px solid #10b981; border-collapse:collapse; margin:15px 0; border-radius:4px;';" +
            "   table.contentEditable = 'false';" +

            "   var row = table.insertRow(0);" +
            "   var cell = row.insertCell(0);" +
            "   cell.style.cssText = 'position:relative; padding:15px;';" +

            "   var btnDelete = document.createElement('div');" +
            "   btnDelete.innerHTML = `<svg width='18' height='18' viewBox='0 0 24 24' fill='none' stroke='#ff4444' stroke-width='2.5'><polyline points='3 6 5 6 21 6'></polyline><path d='M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2'></path></svg>`;" +
            "   btnDelete.style.cssText = 'position:absolute; right:10px; top:10px; cursor:pointer; z-index:999; padding:5px;';" +

            "   btnDelete.onclick = function(e) {" +
            "       e.preventDefault();" +
            "       e.stopPropagation();" +
            "       table.remove();" +
            "   };" +

            "   var area = document.createElement('div');" +
            "   area.contentEditable = 'true';" +
            "   area.style.cssText = 'min-height:40px; color:#e0e0e0; outline:none; font-family:monospace; font-size:16px; white-space:pre-wrap; display:block; width:calc(100% - 25px);';" +

            "   cell.appendChild(btnDelete);" +
            "   cell.appendChild(area);" +

            "   range.insertNode(table);" +

            "   var pApos = document.createElement('p');" +
            "   pApos.innerHTML = '&#8203;';" +
            "   table.parentNode.insertBefore(pApos, table.nextSibling);" +

            "   var dispararFoco = function() {" +
            "       area.click();" + // Simula clique real
            "       area.focus();" +
            "       var r = document.createRange();" +
            "       r.selectNodeContents(area);" +
            "       r.collapse(false);" +
            "       var s = window.getSelection();" +
            "       s.removeAllRanges();" +
            "       s.addRange(r);" +
            "   };" +

            "   setTimeout(dispararFoco, 10);" +
            "   setTimeout(dispararFoco, 100);" +

            "   area.onpaste = function(e) {" +
            "       e.stopPropagation();" +
            "       setTimeout(() => {" +
            "           var children = this.querySelectorAll('*');" +
            "           for(var i=0; i<children.length; i++) {" +
            "               children[i].style.backgroundColor = 'transparent';" +
            "           }" +
            "       }, 100);" +
            "   };" +
            "}";

    webView.getEngine().executeScript(script);
  }
  private void atualizarInterfaceVelocidade() {
    lblValorVelocidade.setText(String.format("%.1fx", velocidadeAtual));
  }

  public void settings(ActionEvent actionEvent) {
  }

  @FXML
  public void alternarClickThrough() {
    Stage stage = (Stage) barraControle.getScene().getWindow();
    modoFantasma = !modoFantasma;
    WindowUtils.configurarModoFantasma(stage.getTitle(), modoFantasma);

    if (modoFantasma) {
      stage.setAlwaysOnTop(true);
      barraControle.setOpacity(0.5);
    } else {
      barraControle.setOpacity(1.0);
      stage.requestFocus();
    }
  }
  private void configurarAtalhoScrollLock() {
    Thread hotkeyThread = new Thread(() -> {
      CustomUser32.INSTANCE.RegisterHotKey(null, 1, 0, 0x91);

      com.sun.jna.platform.win32.WinUser.MSG msg = new com.sun.jna.platform.win32.WinUser.MSG();
      while (CustomUser32.INSTANCE.GetMessage(msg, null, 0, 0) != 0) {
        if (msg.message == CustomUser32.WM_HOTKEY && msg.wParam.intValue() == 1) {
          javafx.application.Platform.runLater(this::alternarClickThrough);
        }
      }
    });
    hotkeyThread.setDaemon(true);
    hotkeyThread.start();
  }
}
