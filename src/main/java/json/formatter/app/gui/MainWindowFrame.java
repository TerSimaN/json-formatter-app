package json.formatter.app.gui;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

public class MainWindowFrame extends JFrame implements PropertyChangeListener {
    private JsonSyntaxEditorPanel editorPanel;
    private String defaultTitle = "JSON Formatter";
    private String changedTitle = null;

    public MainWindowFrame() {
        this.setName("MainFrame");
        this.setTitle(defaultTitle);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        editorPanel = new JsonSyntaxEditorPanel(this);
        editorPanel.addPropertyChangeListener("fullFilePath", this);
        this.getContentPane().add(BorderLayout.CENTER, editorPanel);
        
        this.setMinimumSize(new Dimension(1120, 720));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();

        String newTitle = null;
        if (property.equals("fullFilePath")) {
            newTitle = editorPanel.getFullFilePath();
            changedTitle = String.format("%1$s (%2$s)", defaultTitle, newTitle);
            this.setTitle(changedTitle);
        }
    }
}
