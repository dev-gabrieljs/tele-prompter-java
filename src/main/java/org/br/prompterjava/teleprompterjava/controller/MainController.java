package org.br.prompterjava.teleprompterjava.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;

import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.br.prompterjava.teleprompterjava.util.WindowUtils;

import java.util.Objects;

public class MainController {
  @FXML
  public Pane resizeHandle;
  @FXML
  public ImageView imgIcone;

  @FXML
  private ToggleButton btnInvisivel;
  @FXML
  private AnchorPane rootPane;

  private double xOffset = 0;
  private double yOffset = 0;
  private Image iconVisivel;
  private Image iconInvisivel;

  @FXML
  private BotoesController botoesController;

  @FXML
  private TextoPrompterController textoPrompterController;

  @FXML
  public void initialize() {
    Platform.runLater(() -> {
      if (botoesController != null && textoPrompterController != null) {
        botoesController.setHtmlEditor(textoPrompterController.getHtmlEditor());
      }
    });
    iconVisivel =
        new Image(
            Objects.requireNonNull(
                getClass()
                    .getResourceAsStream("/org/br/prompterjava/teleprompterjava/images/eye.png")));
    iconInvisivel =
        new Image(
            Objects.requireNonNull(
                getClass()
                    .getResourceAsStream(
                        "/org/br/prompterjava/teleprompterjava/images/hidden.png")));

    resizeHandle.setCursor(Cursor.SE_RESIZE);

    resizeHandle.setOnMousePressed(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
            event.consume();
          }
        });

    resizeHandle.setOnMouseDragged(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            double newWidth = stage.getWidth() + (event.getSceneX() - xOffset);
            double newHeight = stage.getHeight() + (event.getSceneY() - yOffset);
            if (newWidth > 400) {
              stage.setWidth(newWidth);
            }
            if (newHeight > 300) {
              stage.setHeight(newHeight);
            }
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
            event.consume();
          }
        });

    rootPane.setOnMousePressed(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
          }
        });

    rootPane.setOnMouseDragged(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
          }
        });
  }

  @FXML
  public void handleInvisibilidade(ActionEvent actionEvent) {
    String WINDOW_TITLE = "Teleprompter Interativo";
    if (btnInvisivel.isSelected()) {
      WindowUtils.aplicarProtecao(WINDOW_TITLE, true);
      imgIcone.setImage(iconInvisivel);
    } else {
      WindowUtils.aplicarProtecao(WINDOW_TITLE, false);
      imgIcone.setImage(iconVisivel);
    }
  }

  @FXML
  private void handleFechar(ActionEvent event) {
    Platform.exit();
    System.exit(0);
  }

  @FXML
  private void handleMinimizar(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    stage.setIconified(true);
  }
}
