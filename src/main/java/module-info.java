module org.br.prompterjava.teleprompterjava {
  requires javafx.fxml;
  requires com.sun.jna.platform;
    requires com.sun.jna;
  requires java.sql;
  requires javafx.web;
  requires javafx.controls;
  requires jdk.httpserver;
  requires com.google.zxing.javase;
  requires com.google.zxing;
  requires java.desktop;

  opens org.br.prompterjava.teleprompterjava to javafx.fxml;
    exports org.br.prompterjava.teleprompterjava;
    exports org.br.prompterjava.teleprompterjava.controller;
    opens org.br.prompterjava.teleprompterjava.controller to javafx.fxml;
}