package json.formatter.app.gui;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

public class NewWindowFrame extends JFrame implements PropertyChangeListener {
    private JsonSyntaxEditorPanel editorPanel;
    private String defaultTitle = "JSON Formatter";
    private String changedTitle = null;

    public NewWindowFrame(Frame frame) {
        this.setTitle(defaultTitle);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        editorPanel = new JsonSyntaxEditorPanel(frame);
        editorPanel.addPropertyChangeListener("fullFilePath", this);
        this.add(editorPanel);

        this.setMinimumSize(new Dimension(820, 720));
        this.setLocationRelativeTo(frame);
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
