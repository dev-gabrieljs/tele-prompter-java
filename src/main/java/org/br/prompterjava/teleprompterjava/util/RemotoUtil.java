package org.br.prompterjava.teleprompterjava.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sun.net.httpserver.HttpServer;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

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
        byte[] response = gerarPaginaHTML().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        LOGGER.info("Página de controle acessada");
      } catch (Exception e) {
        LOGGER.severe("Erro ao servir GUI: " + e.getMessage());
      } finally {
        exchange.getResponseBody().close();
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

  private String gerarPaginaHTML() {
    return """
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>Teleprompter - Controle Remoto</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        
        body { 
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
            color: white; 
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            text-align: center; 
            padding: 20px; 
            min-height: 100vh;
            user-select: none;
            -webkit-tap-highlight-color: transparent;
            display: flex;
            flex-direction: column;
            justify-content: center;
        }
        
        .header {
            margin-bottom: 30px;
        }
        
        h3 { 
            font-size: 24px; 
            font-weight: 600;
            margin-bottom: 8px;
            color: #10b981;
        }
        
        .subtitle {
            font-size: 14px;
            color: #9ca3af;
        }
        
        .grid { 
            display: grid; 
            grid-template-columns: 1fr 1fr; 
            gap: 15px; 
            max-width: 500px;
            margin: 0 auto;
        }
        
        button { 
            padding: 28px; 
            font-size: 18px; 
            border: none; 
            border-radius: 16px;
            background: #2a2a3e; 
            color: white; 
            font-weight: 600; 
            cursor: pointer;
            transition: all 0.2s ease;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
            position: relative;
            overflow: hidden;
        }
        
        button:active { 
            transform: scale(0.96);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
        }
        
        button::before {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            width: 0;
            height: 0;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.3);
            transform: translate(-50%, -50%);
            transition: width 0.3s, height 0.3s;
        }
        
        button:active::before {
            width: 300px;
            height: 300px;
        }
        
        .btn-main { 
            grid-column: span 2; 
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            height: 110px; 
            font-size: 26px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        
        .btn-danger { 
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
        }
        
        .btn-speed {
            background: #374151;
        }
        
        .status { 
            font-size: 13px; 
            color: #6b7280; 
            margin-top: 30px;
            padding: 12px;
            background: rgba(255, 255, 255, 0.05);
            border-radius: 8px;
            max-width: 500px;
            margin: 30px auto 0;
        }
        
        .connection-indicator {
            display: inline-block;
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #10b981;
            margin-right: 8px;
            animation: pulse 2s infinite;
        }
        
        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
        }
        
        .feedback {
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            background: #10b981;
            color: white;
            padding: 12px 24px;
            border-radius: 8px;
            opacity: 0;
            transition: opacity 0.3s;
            pointer-events: none;
            z-index: 1000;
        }
        
        .feedback.show {
            opacity: 1;
        }
        
        .feedback.error {
            background: #ef4444;
        }
    </style>
</head>
<body>
    <div class="header">
        <h3>📱 Teleprompter Remoto</h3>
        <div class="subtitle">Controle via Wi-Fi</div>
    </div>
    
    <div class="grid">
        <button class="btn-main" onclick="enviar('playpause', this)">
            ▶️ PLAY / PAUSE
        </button>
        <button class="btn-speed" onclick="enviar('menos', this)">
            ➖ MAIS LENTO
        </button>
        <button class="btn-speed" onclick="enviar('mais', this)">
            ➕ MAIS RÁPIDO
        </button>
        <button class="btn-danger" onclick="enviar('reiniciar', this)">
            🔄 REINICIAR
        </button>
    </div>
    
    <div class="status">
        <span class="connection-indicator"></span>
        Conectado ao Teleprompter
    </div>
    
    <div class="feedback" id="feedback"></div>
    
    <script>
        let ultimoComando = 0;
        const DEBOUNCE_MS = 200; // Evita cliques múltiplos
        
        function mostrarFeedback(mensagem, erro = false) {
            const fb = document.getElementById('feedback');
            fb.textContent = mensagem;
            fb.className = 'feedback show' + (erro ? ' error' : '');
            setTimeout(() => fb.classList.remove('show'), 2000);
        }
        
        function enviar(cmd, btn) {
            // Debounce - evita múltiplos comandos
            const agora = Date.now();
            if (agora - ultimoComando < DEBOUNCE_MS) return;
            ultimoComando = agora;
            
            const params = new URLSearchParams(window.location.search);
            const token = params.get('token');
            
            if (!token) {
                mostrarFeedback('❌ Token não encontrado', true);
                return;
            }
            
            // Feedback visual imediato
            const originalBg = btn.style.background;
            btn.style.background = '#34d399';
            
            fetch('/control?token=' + token + '&cmd=' + cmd)
                .then(response => {
                    if (!response.ok) throw new Error('Erro na resposta');
                    return response.json();
                })
                .then(data => {
                    setTimeout(() => btn.style.background = originalBg, 200);
                    
                    // Feedback por comando
                    const mensagens = {
                        'playpause': '▶️ Play/Pause',
                        'reiniciar': '🔄 Reiniciado',
                        'mais': '⚡ Velocidade +',
                        'menos': '🐢 Velocidade -'
                    };
                    mostrarFeedback(mensagens[cmd] || '✓ Comando enviado');
                })
                .catch(err => {
                    console.error('Erro:', err);
                    btn.style.background = '#ef4444';
                    setTimeout(() => btn.style.background = originalBg, 500);
                    mostrarFeedback('❌ Verifique se PC e celular estão no mesmo Wi-Fi', true);
                });
        }
        
        // Previne zoom ao dar duplo toque
        let lastTouchEnd = 0;
        document.addEventListener('touchend', function(event) {
            const now = Date.now();
            if (now - lastTouchEnd <= 300) {
                event.preventDefault();
            }
            lastTouchEnd = now;
        }, false);
    </script>
</body>
</html>
""";}

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