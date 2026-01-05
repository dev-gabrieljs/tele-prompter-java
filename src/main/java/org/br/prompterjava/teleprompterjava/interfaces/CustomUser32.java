package org.br.prompterjava.teleprompterjava.interfaces;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.W32APIOptions;

public interface CustomUser32 extends User32 {
    CustomUser32 INSTANCE = Native.load("user32", CustomUser32.class, W32APIOptions.DEFAULT_OPTIONS);
    boolean SetWindowDisplayAffinity(HWND hWnd, int dwAffinity);
}