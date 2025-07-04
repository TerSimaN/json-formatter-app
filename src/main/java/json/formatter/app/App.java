package json.formatter.app;

import javax.swing.SwingUtilities;

import json.formatter.app.gui.MainWindowFrame;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            MainWindowFrame mainWindowFrame = new MainWindowFrame();
            
            @Override
            public void run() {
                mainWindowFrame.createAndShowGUI();
            }
        });

        // System.out.println("isEventDispatchThread: " + SwingUtilities.isEventDispatchThread());
    }
}
