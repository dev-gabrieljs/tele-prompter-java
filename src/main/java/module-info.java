module org.br.prompterjava.teleprompterjava {
  requires javafx.fxml;
  requires com.sun.jna.platform;
    requires com.sun.jna;
  requires java.sql;
  requires javafx.web;
  requires javafx.controls;

  opens org.br.prompterjava.teleprompterjava to javafx.fxml;
    exports org.br.prompterjava.teleprompterjava;
    exports org.br.prompterjava.teleprompterjava.controller;
    opens org.br.prompterjava.teleprompterjava.controller to javafx.fxml;
}