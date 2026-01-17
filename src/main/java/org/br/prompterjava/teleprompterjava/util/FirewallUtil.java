package org.br.prompterjava.teleprompterjava.util;

import java.util.logging.Logger;

public class FirewallUtil {
  private static final Logger LOGGER = Logger.getLogger(FirewallUtil.class.getName());
  private static final String NOME_REGRA = "TeleprompterJava_RemoteControl";
  private static boolean regraJaCriada = false;

  public static void liberarPorta() {
    if (regraJaCriada) {
      LOGGER.info("Regra de firewall já foi criada anteriormente.");
      return;
    }

    int porta = RemotoUtil.PORTA; // Usa a porta consistente

    String comandoNetsh = String.format(
        "netsh advfirewall firewall add rule name=\"%s\" dir=in action=allow protocol=TCP localport=%d profile=any",
        NOME_REGRA, porta
    );

    // Script PowerShell que pede permissão de administrador
    String scriptPowerShell = String.format(
        "Start-Process cmd -ArgumentList '/c %s' -Verb RunAs -WindowStyle Hidden",
        comandoNetsh.replace("\"", "\\\"")
    );

    ProcessBuilder pb = new ProcessBuilder(
        "powershell.exe",
        "-WindowStyle", "Hidden",
        "-Command",
        scriptPowerShell
    );

    try {
      LOGGER.info("Solicitando permissão de administrador para abrir porta " + porta + " no Firewall...");
      Process p = pb.start();

      // Aguarda até 3 segundos pela resposta
      boolean finalizado = p.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);

      if (finalizado) {
        int exitCode = p.exitValue();
        if (exitCode == 0) {
          LOGGER.info("Regra de firewall configurada com sucesso.");
          regraJaCriada = true;
        } else {
          LOGGER.warning("Usuário pode ter negado a permissão. Código: " + exitCode);
        }
      } else {
        LOGGER.info("Aguardando confirmação do usuário...");
        // Não bloqueia, assume que eventualmente será permitido
        regraJaCriada = true;
      }

    } catch (Exception e) {
      LOGGER.severe("Erro ao configurar firewall: " + e.getMessage());
      LOGGER.info("O controle remoto pode não funcionar. Configure manualmente a porta " + porta);
    }
  }

  /**
   * Remove a regra do firewall (útil para limpeza)
   */
  public static void removerRegra() {
    String comandoNetsh = String.format(
        "netsh advfirewall firewall delete rule name=\"%s\"",
        NOME_REGRA
    );

    String scriptPowerShell = String.format(
        "Start-Process cmd -ArgumentList '/c %s' -Verb RunAs -WindowStyle Hidden",
        comandoNetsh.replace("\"", "\\\"")
    );

    ProcessBuilder pb = new ProcessBuilder(
        "powershell.exe",
        "-WindowStyle", "Hidden",
        "-Command",
        scriptPowerShell
    );

    try {
      pb.start();
      LOGGER.info("Solicitação de remoção da regra de firewall enviada.");
      regraJaCriada = false;
    } catch (Exception e) {
      LOGGER.warning("Erro ao remover regra: " + e.getMessage());
    }
  }
}