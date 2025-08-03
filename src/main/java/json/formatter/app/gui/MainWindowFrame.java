package json.formatter.app.gui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.*;

public class MainWindowFrame {
    private JFrame mainFrame;

    public void createAndShowGUI() {
        mainFrame = new JFrame();
        mainFrame.setName("MainFrame");
        mainFrame.setTitle("JSON Formatter");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        MainPanel mainPanel = new MainPanel();
        mainFrame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        // mainFrame.addComponentListener(new FrameEventListener());
        
        mainFrame.setMinimumSize(new Dimension(1120, 720));
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    class FrameEventListener extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            Dimension componentDimension = e.getComponent().getSize();
            String componentName = e.getComponent().getName();
            System.out.println(componentName +  " size: " + componentDimension);
        }
    }
}
