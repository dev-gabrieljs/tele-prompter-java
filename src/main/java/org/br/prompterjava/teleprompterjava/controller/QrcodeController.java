package org.br.prompterjava.teleprompterjava.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

public class QrcodeController {

  public Button btnFechar;
  @FXML private ImageView imgQrCode;
  @FXML private Label lblToken;
  private Runnable onFecharCallback;
  private String currentUrl;

  public void setDados(WritableImage img, String token, String url,Runnable onFechar) {
    this.imgQrCode.setImage(img);
    this.lblToken.setText("Token: " + token);
    this.currentUrl = url;
    this.onFecharCallback = onFechar;
  }

  @FXML
  private void handleFechar() {
    if (onFecharCallback != null) {
      onFecharCallback.run();
    }
    Stage stage = (Stage) btnFechar.getScene().getWindow();
    stage.close();
  }

  public void fecharJanela() {
    javafx.application.Platform.runLater(() -> {
      if (imgQrCode != null && imgQrCode.getScene() != null) {
        Stage stage = (Stage) imgQrCode.getScene().getWindow();
        if (stage != null) stage.close();
      }
    });
  }
}