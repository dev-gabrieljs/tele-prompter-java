package org.br.prompterjava.teleprompterjava.controller;

import javafx.fxml.FXML;

public class RemoteController {

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
}