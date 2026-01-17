package org.br.prompterjava.teleprompterjava.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sun.net.httpserver.HttpServer;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Logger;

public class RemotoUtil {
  private static final Logger LOGGER = Logger.getLogger(RemotoUtil.class.getName());
  private String tokenAcesso;
  private HttpServer server;
  public static final int PORTA = 9090;

  public interface RemoteCommandHandler {
    void handle(String comando);
  }

  public String gerarNovoToken() {
    this.tokenAcesso = UUID.randomUUID().toString().substring(0, 8);
    LOGGER.info("Token gerado: " + this.tokenAcesso);
    return this.tokenAcesso;
  }

  public String getUrlControle() throws Exception {
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
      String ipLocal = socket.getLocalAddress().getHostAddress();

      if (ipLocal.equals("127.0.0.1")) {
        throw new Exception("Falha ao obter IP da rede local. Verifique sua conexão Wi-Fi.");
      }

      String url = "http://" + ipLocal + ":" + PORTA + "/gui?token=" + tokenAcesso;
      LOGGER.info("URL de controle gerada: " + url);
      return url;
    }
  }

  public void iniciarServidor(RemoteCommandHandler handler) throws Exception {
    if (server != null) {
      LOGGER.warning("Servidor já está em execução.");
      return;
    }

    FirewallUtil.liberarPorta();
    server = HttpServer.create(new InetSocketAddress(PORTA), 50);


    server.createContext("/gui", exchange -> {
      try {
        String html = carregarPaginaHTML();
        byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("Connection", "close");

        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
          os.write(responseBytes);
          os.flush();
        }

        LOGGER.info("Sucesso: Enviados " + responseBytes.length + " bytes para o celular.");
      } catch (Exception e) {
        LOGGER.severe("Falha no servidor: " + e.getMessage());
        try { exchange.sendResponseHeaders(500, 0); } catch (IOException ignored) {}
      } finally {
        exchange.close();
      }
    });

    server.createContext("/control", exchange -> {
      try {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.contains("token=" + tokenAcesso)) {

          String cmd = "status";
          if (query.contains("cmd=")) {
            cmd = query.split("cmd=")[1].split("&")[0];
            LOGGER.info("Comando recebido: " + cmd);
          }
          handler.handle(cmd);

          byte[] response = "{\"status\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, response.length);
          exchange.getResponseBody().write(response);
        } else {
          LOGGER.warning("Tentativa de acesso não autorizado");
          exchange.sendResponseHeaders(403, 0);
        }
      } catch (Exception e) {
        LOGGER.severe("Erro ao processar comando: " + e.getMessage());
        exchange.sendResponseHeaders(500, 0);
      } finally {
        exchange.getResponseBody().close();
      }
    });

    server.setExecutor(null);
    server.start();
    LOGGER.info("Servidor HTTP iniciado na porta " + PORTA);
  }

  public void pararServidor() {
    if (server != null) {
      server.stop(0);
      server = null;
      LOGGER.info("Servidor HTTP parado");
    }
  }

  private String carregarPaginaHTML() {
    try (InputStream is = getClass().getResourceAsStream("/org/br/prompterjava/teleprompterjava/views/remote/remote.html")) {
      if (is == null) {
        throw new IOException("Arquivo remote.html não encontrado no caminho especificado.");
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      LOGGER.severe("Erro ao carregar HTML: " + e.getMessage());
      return "<html><body><h1>Erro ao carregar interface: " + e.getMessage() + "</h1></body></html>";
    }
  }

  public WritableImage gerarQRCode(String conteudo) throws Exception {
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode(conteudo, BarcodeFormat.QR_CODE, 300, 300);
    WritableImage img = new WritableImage(300, 300);
    PixelWriter pw = img.getPixelWriter();

    for (int y = 0; y < 300; y++) {
      for (int x = 0; x < 300; x++) {
        pw.setColor(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
      }
    }

    LOGGER.info("QR Code gerado com sucesso");
    return img;
  }


}