package org.br.prompterjava.teleprompterjava.util;

import com.sun.jna.platform.win32.WinDef.HWND;
import org.br.prompterjava.teleprompterjava.interfaces.CustomUser32;

public class WindowUtils {
  public static final int WDA_EXCLUDEFROMCAPTURE = 0x00000011;
  public static final int WDA_NONE = 0x00000000;

  public static void aplicarProtecao(String title, boolean ativar) {
    HWND hwnd = CustomUser32.INSTANCE.FindWindow(null, title);
    if (hwnd != null) {
      int modo = ativar ? WDA_EXCLUDEFROMCAPTURE : WDA_NONE;
      CustomUser32.INSTANCE.SetWindowDisplayAffinity(hwnd, modo);
    }
  }
}
