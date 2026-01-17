package org.br.prompterjava.teleprompterjava.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.br.prompterjava.teleprompterjava.util.RemotoUtil;

public class RemoteController {
  @FXML private ImageView imgQRCode;
  @FXML private Label lblUrl;
  @FXML private Label lblToken;
  @FXML private Button btnCopiar;

  private final RemotoUtil remotoUtil = new RemotoUtil();
  private RemotoUtil.RemoteCommandHandler externalHandler;

  public void init(RemotoUtil.RemoteCommandHandler handler) {
    this.externalHandler = handler;
    try {
      remotoUtil.iniciarServidor(comando -> {
        // Encaminha o comando recebido para o BotoesController via handler
        Platform.runLater(() -> externalHandler.handle(comando));
      });
      atualizarUI();
    } catch (Exception e) {
      lblUrl.setText("Erro ao iniciar servidor");
    }
  }

  @FXML
  private void handleGerarNovoToken() {
    remotoUtil.gerarNovoToken();
    atualizarUI();
  }

  private void atualizarUI() {
    try {
      String url = remotoUtil.getUrlControle();
      lblUrl.setText(url);
      lblToken.setText("Token: " + remotoUtil.gerarNovoToken());
      imgQRCode.setImage(remotoUtil.gerarQRCode(url));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void finalizar() {
    remotoUtil.pararServidor();
  }
}