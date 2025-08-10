package json.formatter.app.gui;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

public class MainWindowFrame {
    private JFrame mainFrame;
    private JsonSyntaxEditorPanel editorPanel;
    private String defaultTitle = "JSON Formatter";
    private String changedTitle = null;

    public void createAndShowGUI() {
        mainFrame = new JFrame();
        mainFrame.setName("MainFrame");
        mainFrame.setTitle(defaultTitle);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        editorPanel = new JsonSyntaxEditorPanel();
        editorPanel.addPropertyChangeListener("fullFilePath", new FileNameChangeListener());
        mainFrame.getContentPane().add(BorderLayout.CENTER, editorPanel);
        
        mainFrame.setMinimumSize(new Dimension(1120, 720));
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    class FileNameChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();

            String newTitle = null;
            if (property.equals("fullFilePath")) {
                newTitle = editorPanel.getFullFilePath();
                changedTitle = String.format("%1$s (%2$s)", defaultTitle, newTitle);
                mainFrame.setTitle(changedTitle);
            }
        }
    }
}
