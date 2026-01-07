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
  public void start(Stage primaryStage) {
    try {
      primaryStage.initStyle(StageStyle.UTILITY);
      primaryStage.setOpacity(0);
      primaryStage.setHeight(0);
      primaryStage.setWidth(0);
      primaryStage.show();

      Stage realStage = new Stage();
      realStage.initOwner(primaryStage);
      realStage.initStyle(StageStyle.UNDECORATED);

      FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main.fxml"));
      Scene scene = new Scene(fxmlLoader.load(), 900, 600);

      realStage.setTitle(TITULO_APP);
      realStage.setScene(scene);
      realStage.show();

      Platform.runLater(() -> {
        WindowUtils.aplicarProtecao(TITULO_APP, false);
      });

    } catch (IOException e) {
      System.err.println("Erro ao carregar a interface: " + e.getMessage());
    }
  }
}