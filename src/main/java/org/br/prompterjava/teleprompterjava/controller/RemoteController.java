package org.br.prompterjava.teleprompterjava.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.br.prompterjava.teleprompterjava.util.RemotoUtil;

import java.io.IOException;

public class RemoteController {
  private QrcodeController qrCodeController;
  private RemotoUtil remoteUtil = new RemotoUtil();
  @FXML
  private void handlePlayPause() {
    enviarComando("playpause");
  }

  @FXML
  private void handleMais() {
    enviarComando("mais");
  }

  @FXML
  private void handleMenos() {
    enviarComando("menos");
  }

  @FXML
  private void handleReiniciar() {
    enviarComando("reiniciar");
  }

  private void enviarComando(String cmd) {
    System.out.println("Enviando comando: " + cmd);
  }

  public void notificarConexaoCelular() {
    if (qrCodeController != null) {
      qrCodeController.fecharJanela();
      this.qrCodeController = null;
    }
  }

  public void iniciarConexao(Runnable playPause, Runnable reiniciar, Runnable mais, Runnable menos, Runnable onManualClose) {
    try {
      String token = remoteUtil.gerarNovoToken();
      String url = remoteUtil.getUrlControle();
      WritableImage qrCode = remoteUtil.gerarQRCode(url);

      remoteUtil.setOnConnectionHandler(this::notificarConexaoCelular);

      remoteUtil.iniciarServidor(comando -> {
        javafx.application.Platform.runLater(() -> {
          switch (comando) {
            case "playpause" -> playPause.run();
            case "reiniciar" -> reiniciar.run();
            case "mais"      -> mais.run();
            case "menos"     -> menos.run();
          }
        });
      });

      exibirPopupQRCode(qrCode, token, url, onManualClose);

    } catch (Exception e) {
    }
  }
  private void exibirPopupQRCode(WritableImage img, String token, String url, Runnable onManualClose) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/br/prompterjava/teleprompterjava/views/qrcode/qrcode.fxml"));
    Parent root = loader.load();

    this.qrCodeController = loader.getController();
    this.qrCodeController.setDados(img, token, url, onManualClose);

    Stage popup = new Stage();
    popup.setScene(new Scene(root));
    popup.setTitle("Conectar Celular");
    popup.setAlwaysOnTop(true);
    popup.show();
  }


  public void pararServico() {
    remoteUtil.pararServidor();
  }
}