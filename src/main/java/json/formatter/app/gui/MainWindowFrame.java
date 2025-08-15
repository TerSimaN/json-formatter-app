package json.formatter.app.gui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

public class MainWindowFrame extends JFrame implements PropertyChangeListener {
    private JsonSyntaxEditorPanel editorPanel;
    private String defaultTitle = "JSON Formatter";
    private String changedTitle = null;

    public MainWindowFrame(Frame frame) {
        this.setName("MainFrame");
        this.setTitle(defaultTitle);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                if (isDisplayable()) {
                    setVisible(false);
                    dispose();
                }
            }
        });
        
        editorPanel = new JsonSyntaxEditorPanel(this);
        editorPanel.addPropertyChangeListener("fullFilePath", this);
        editorPanel.addPropertyChangeListener("fileChanged", this);
        this.getContentPane().add(BorderLayout.CENTER, editorPanel);
        
        this.setMinimumSize(new Dimension(820, 720));
        if (frame != null) {
            this.setLocationRelativeTo(frame);
        } else {
            this.setLocationRelativeTo(null);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();

        String nextTitle = null;
        if (property.equals("fullFilePath")) {
            nextTitle = editorPanel.getFullFilePath();
            changedTitle = String.format("%1$s (%2$s)", defaultTitle, nextTitle);
            this.setTitle(changedTitle);
        }

        if (property.equals("fileChanged")) {
            nextTitle = editorPanel.getFullFilePath();
            changedTitle = String.format("%1$s (* %2$s)", defaultTitle, nextTitle);
            this.setTitle(changedTitle);
        }
    }
}
