package org.kornicameister.crypto;

import org.kornicameister.crypto.view.SchnorrWindow;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public class SchnorrLauncher {
    public static void main(String[] args) {
        SchnorrWindow mainWindow = new SchnorrWindow();
        mainWindow.setTitle("Schnorr - Kornicameister");
        mainWindow.pack();
        mainWindow.setVisible(true);
        System.exit(0);
    }
}
