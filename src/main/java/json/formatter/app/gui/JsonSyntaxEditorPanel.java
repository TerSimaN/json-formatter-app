package json.formatter.app.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Caret;

import com.google.gson.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

import json.formatter.app.constants.ImageIconConstants;

public class JsonSyntaxEditorPanel extends JPanel {
    private Gson serializeNullsGsonBuilder = new GsonBuilder().serializeNulls().create();
    private Gson prettyPrintSerializeNullsGsonBuilder = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    private FileNameExtensionFilter fileFilter;
    private FlowLayout leadingFlowLayout;
    private Dimension iconBtnPreferredSize = new Dimension(30, 30);
    private Frame parentFrame;

    // File controls panel
    private JButton newButton;
    private JButton openButton;
    private JButton saveButton;
    private JButton copyButton;
    private String lastOpenDirectoryPath = null;
    private String lastSaveDirectoryPath = null;
    private String fullFilePath = null;
    private boolean hasFullFilePathChanged = false;

    // Control options panel
    private JButton lineWrapButton;
    
    // Json RSyntax text area
    private TextEditorPane jsonSyntaxTextArea;
    private Caret caret;
    private JLabel caretLabel;
    private boolean hasLineWrap = true;

    // Undo and redo helpers
    protected UndoListener undoListener;
    protected RedoListener redoListener;
    
    JsonSyntaxEditorPanel(Frame parent) {
        this.parentFrame = parent;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setName("JsonEditorPanel");
        this.addComponentListener(new PanelEventListener());

        fileFilter = new FileNameExtensionFilter("JSON files (*.json)", "json");
        leadingFlowLayout = new FlowLayout(FlowLayout.LEADING, 5, 0);

        JPanel fileControlsPanel = createFileControlsPanel();
        JPanel controlOptionsPanel = createControlOptionsPanel();
        JPanel editorTextAreaPanel = createSyntaxTextAreaPanel();
        updateCaretLabel();

        this.add(Box.createVerticalStrut(5));
        this.add(fileControlsPanel);
        this.add(Box.createVerticalStrut(5));
        this.add(controlOptionsPanel);
        this.add(Box.createVerticalStrut(5));
        this.add(editorTextAreaPanel);
    }

    public String getFullFilePath() {
        return fullFilePath;
    }

    public void setFullFilePath(String filePath) {
        if (!filePath.isEmpty()) {
            fullFilePath = filePath;
        }
    }

    /**
     * Creates a file controls JPanel
     * @return the panel
     */
    private JPanel createFileControlsPanel() {
        JPanel panel = new JPanel(leadingFlowLayout);
        
        newButton = new JButton("New", ImageIconConstants.newFileIcon);
        newButton.setToolTipText("New document");
        newButton.addActionListener(e -> createAndShowNewWindowFrame());
        panel.add(newButton);

        openButton = new JButton("Open", ImageIconConstants.openFileIcon);
        openButton.setToolTipText("Open a JSON file");
        openButton.addActionListener(e -> open());
        panel.add(openButton);

        saveButton = new JButton("Save", ImageIconConstants.saveFileIcon);
        saveButton.setToolTipText("Save file");
        saveButton.addActionListener(e -> save());
        panel.add(saveButton);

        copyButton = new JButton("Copy", ImageIconConstants.copyFileIcon);
        copyButton.setToolTipText("Copy");
        copyButton.addActionListener(e -> System.err.println("Unimplemented method 'copy'"));
        copyButton.setEnabled(false);
        panel.add(copyButton);

        return panel;
    }

    /**
     * Creates a control options JPanel
     * @return the panel
     */
    private JPanel createControlOptionsPanel() {
        JPanel panel = new JPanel(leadingFlowLayout);

        JButton formatJsonButton = new JButton(ImageIconConstants.formatJsonIcon);
        formatJsonButton.setPreferredSize(iconBtnPreferredSize);
        formatJsonButton.setToolTipText("Format JSON: add proper identation and new lines");
        formatJsonButton.setActionCommand("prettyJson");
        formatJsonButton.addActionListener(new FormatJsonListener());
        panel.add(formatJsonButton);

        JButton compactJsonButton = new JButton(ImageIconConstants.compactJsonIcon);
        compactJsonButton.setPreferredSize(iconBtnPreferredSize);
        compactJsonButton.setToolTipText("Compact JSON: remove all white spacing and new lines");
        compactJsonButton.setActionCommand("compactJson");
        compactJsonButton.addActionListener(new FormatJsonListener());
        panel.add(compactJsonButton);

        JButton undoButton = new JButton(ImageIconConstants.arrowLeftBoldIcon);
        undoButton.setPreferredSize(iconBtnPreferredSize);
        undoButton.setToolTipText("Undo");
        undoListener = new UndoListener(undoButton);
        undoButton.addActionListener(undoListener);
        panel.add(undoButton);

        JButton redoButton = new JButton(ImageIconConstants.arrowRightBoldIcon);
        redoButton.setPreferredSize(iconBtnPreferredSize);
        redoButton.setToolTipText("Redo");
        redoListener = new RedoListener(redoButton);
        redoButton.addActionListener(redoListener);
        panel.add(redoButton);

        lineWrapButton = new JButton(ImageIconConstants.wrapEnableIcon);
        lineWrapButton.setPreferredSize(iconBtnPreferredSize);
        lineWrapButton.setToolTipText("Line wraping disabled");
        lineWrapButton.addActionListener(e -> updateLineWrapState());
        panel.add(lineWrapButton);

        return panel;
    }

    /**
     * Creates a RSyntax text area editor JPanel
     * @return the panel
     */
    private JPanel createSyntaxTextAreaPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        jsonSyntaxTextArea = new TextEditorPane();
        jsonSyntaxTextArea.setWrapStyleWord(true);
        jsonSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        jsonSyntaxTextArea.setCodeFoldingEnabled(true);
        jsonSyntaxTextArea.getDocument().addUndoableEditListener(new JsonUndoableEditListener());

        caret = jsonSyntaxTextArea.getCaret();
        caret.addChangeListener(e -> updateCaretLabel());

        RTextScrollPane jsonSyntaxScrollPane = new RTextScrollPane(jsonSyntaxTextArea);
        jsonSyntaxScrollPane.setPreferredSize(new Dimension(0, 650));
        panel.add(jsonSyntaxScrollPane, BorderLayout.CENTER);

        JPanel caretPanel = new JPanel(leadingFlowLayout);
        caretPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        caretLabel = new JLabel();
        caretPanel.add(caretLabel);
        panel.add(caretPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void createAndShowNewWindowFrame() {
        NewWindowFrame newWindowFrame = new NewWindowFrame(parentFrame);
        newWindowFrame.pack();
        newWindowFrame.setVisible(true);
    }

    private void createAndShowErrorDialog(Exception exception) {
        String errorMessage;
        int newlineIndex = exception.getMessage().indexOf('\n');
        if (newlineIndex > -1) {
            errorMessage = exception.getMessage().substring(0, newlineIndex);
        } else {
            errorMessage = exception.getMessage();
        }

        if (errorMessage.contains("malformed JSON")) {
            int firstIndex = errorMessage.indexOf("at");
            errorMessage = "Malformed JSON " + errorMessage.substring(firstIndex, newlineIndex);
        } else if (errorMessage.contains("Unterminated string")) {
            int firstIndex = errorMessage.indexOf("Unterminated");
            errorMessage = errorMessage.substring(firstIndex, newlineIndex);
        }

        JOptionPane.showMessageDialog(null, errorMessage,
            "JSON Editor Error", JOptionPane.ERROR_MESSAGE);
    }

    private void updateCaretLabel() {
        int dotPosition = caret.getDot();
        int markPosition = caret.getMark();
        int currentLine = jsonSyntaxTextArea.getCaretLineNumber() + 1;
        int currentColumn = jsonSyntaxTextArea.getCaretOffsetFromLineStart() + 1;

        int selectedChars = 0;
        if (dotPosition > markPosition) {
            selectedChars = dotPosition - markPosition;
        } else if (markPosition > dotPosition) {
            selectedChars = markPosition - dotPosition;
        }
        String selectedCharsString = (selectedChars > 0) ? String.format(" (%1$d selected)", selectedChars) : "";

        String labelText = String.format("Line: %1$d Column: %2$d%3$s", currentLine, currentColumn, selectedCharsString);
        caretLabel.setText(labelText);
    }

    private void updateLineWrapState() {
        jsonSyntaxTextArea.setLineWrap(hasLineWrap);
        hasLineWrap = !hasLineWrap;
        Icon lineWrapIcon = hasLineWrap ? ImageIconConstants.wrapEnableIcon : ImageIconConstants.wrapDisableIcon;
        lineWrapButton.setIcon(lineWrapIcon);
        String toolTipText = hasLineWrap ? "Line wraping disabled" : "Line wraping enabled";
        lineWrapButton.setToolTipText(toolTipText);
    }

    private void open() {
        JFileChooser fileOpen = new JFileChooser(this.lastOpenDirectoryPath);
        fileOpen.setFileFilter(fileFilter);
        int returnValue = fileOpen.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            this.lastOpenDirectoryPath = fileOpen.getCurrentDirectory().getPath();
            loadFile(fileOpen.getSelectedFile());
        } else {
            System.err.println("Open command canceled by user.");
        }
    }

    private void save() {
        JFileChooser fileSave = new JFileChooser(this.lastSaveDirectoryPath);
        fileSave.setFileFilter(fileFilter);

        File saveFile = new File(fullFilePath);
        fileSave.setSelectedFile(saveFile);
        
        int returnValue = fileSave.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            this.lastSaveDirectoryPath = fileSave.getCurrentDirectory().getPath();
            saveFile(fileSave.getSelectedFile());
        } else {
            System.err.println("Save command canceled by user.");
        }
    }

    private void loadFile(File file) {
        try {
            fullFilePath = file.getAbsolutePath();
            updateWindowTitle();

            FileLocation selectedFileLocation = FileLocation.create(file);
            jsonSyntaxTextArea.load(selectedFileLocation);
        } catch (IOException e) {
            System.err.println("Couldn't read file: " + e.getMessage());
        }
    }

    private void saveFile(File file) {
        try {
            updateWindowTitle();
            FileLocation selectedFileLocation = FileLocation.create(file);
            jsonSyntaxTextArea.saveAs(selectedFileLocation);
        } catch (IOException e) {
            System.err.println("Couldn't write to file: " + e.getMessage());
        }
    }

    private void updateWindowTitle() {
        this.firePropertyChange("fullFilePath", hasFullFilePathChanged, !hasFullFilePathChanged);
        hasFullFilePathChanged = !hasFullFilePathChanged;
    }

    // Class listening for edits that can be undone
    protected class JsonUndoableEditListener implements UndoableEditListener {
        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            undoListener.updateUndoState();
            redoListener.updateRedoState();
        }
    }

    // An undo event listener
    class UndoListener implements ActionListener {
        private Component component;
        
        public UndoListener(Component component) {
            this.component = component;
            this.component.setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            jsonSyntaxTextArea.undoLastAction();
            updateUndoState();
            redoListener.updateRedoState();
        }

        protected void updateUndoState() {
            if (jsonSyntaxTextArea.canUndo()) {
                component.setEnabled(true);
            } else {
                component.setEnabled(false);
            }
        }
    }

    // A redo event listener
    class RedoListener implements ActionListener {
        private Component component;

        public RedoListener(Component component) {
            this.component = component;
            this.component.setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            jsonSyntaxTextArea.redoLastAction();
            undoListener.updateUndoState();
            updateRedoState();
        }

        protected void updateRedoState() {
            if (jsonSyntaxTextArea.canRedo()) {
                component.setEnabled(true);
            } else {
                component.setEnabled(false);
            }
        }   
    }
    
    class FormatJsonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            String jsonString = jsonSyntaxTextArea.getText();
            
            if (!jsonString.isEmpty()) {
                try {
                    JsonElement parsedJsonElement = JsonParser.parseString(jsonString);
                    String formattedJsonString = null;

                    if (command.equals("prettyJson")) {
                        formattedJsonString = prettyPrintSerializeNullsGsonBuilder.toJson(parsedJsonElement);
                    }
                    if (command.equals("compactJson")) {
                        formattedJsonString = serializeNullsGsonBuilder.toJson(parsedJsonElement);
                    }

                    jsonSyntaxTextArea.setText(formattedJsonString);
                    jsonSyntaxTextArea.setCaretPosition(0);
                } catch (JsonParseException jsonParseException) {
                    createAndShowErrorDialog(jsonParseException);
                }
            }
        }
    }

    // Custom event listeners and classes
    class PanelEventListener extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            Dimension componentDimension = e.getComponent().getSize();
            int componentWidth = componentDimension.width;
            
            if (componentWidth < 580) {
                copyButton.setText("");
                copyButton.setPreferredSize(iconBtnPreferredSize);
            } else {
                copyButton.setText("Copy");
                copyButton.setPreferredSize(null);
            }

            if (componentWidth < 524) {
                saveButton.setText("");
                saveButton.setPreferredSize(iconBtnPreferredSize);
            } else {
                saveButton.setText("Save");
                saveButton.setPreferredSize(null);
            }

            if (componentWidth < 467) {
                openButton.setText("");
                openButton.setPreferredSize(iconBtnPreferredSize);
            } else {
                openButton.setText("Open");
                openButton.setPreferredSize(null);
            }

            if (componentWidth < 408) {
                newButton.setText("");
                newButton.setPreferredSize(iconBtnPreferredSize);
            } else {
                newButton.setText("New");
                newButton.setPreferredSize(null);
            }
        }
    }
}
