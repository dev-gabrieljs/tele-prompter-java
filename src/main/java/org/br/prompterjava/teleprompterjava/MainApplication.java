package org.br.prompterjava.teleprompterjava;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.br.prompterjava.teleprompterjava.util.WindowUtils;

import java.io.IOException;

public class MainApplication extends Application {

  private static final String TITULO_APP = "Teleprompter Interativo";

  @Override
  public void start(Stage stage) {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main.fxml"));
      Scene scene = new Scene(fxmlLoader.load(), 900, 600);

      stage.initStyle(StageStyle.UNDECORATED);
      stage.setTitle(TITULO_APP);
      stage.setScene(scene);
      stage.show();

      Platform.runLater(
          () -> {
            WindowUtils.aplicarProtecao(TITULO_APP, false);
          });

    } catch (IOException e) {
      System.err.println("Erro ao carregar a interface: " + e.getMessage());
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
