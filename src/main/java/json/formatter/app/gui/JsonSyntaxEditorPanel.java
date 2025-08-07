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
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;

import com.google.gson.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;

import json.formatter.app.constants.ImageIconConstants;
import json.formatter.app.json.JsonStringParser;

public class JsonSyntaxEditorPanel extends JPanel {
    private Gson serializeNullsGsonBuilder = new GsonBuilder().serializeNulls().create();
    private Gson prettyPrintSerializeNullsGsonBuilder = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private JsonStringParser jsonStringParser = new JsonStringParser();

    private FileNameExtensionFilter filter;
    private FlowLayout leadingFlowLayout;
    private Dimension iconBtnPreferredSize = new Dimension(30, 30);

    // File controls panel
    private JTextField fileNameField;
    private JButton newButton;
    private JButton openButton;
    private JButton saveButton;
    private JButton copyButton;
    private String lastOpenDirectoryPath = null;
    private String lastSaveDirectoryPath = null;
    
    // Json RSyntax text area
    private TextEditorPane jsonSyntaxTextArea;
    private Caret caret;
    private JLabel caretLabel;

    // Undo and redo helpers
    protected UndoListener undoListener;
    protected RedoListener redoListener;
    
    JsonSyntaxEditorPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(Color.PINK);
        this.setName("JsonEditorPanel");
        this.addComponentListener(new PanelEventListener());

        filter = new FileNameExtensionFilter("JSON files (*.json)", "json");
        leadingFlowLayout = new FlowLayout(FlowLayout.LEADING, 5, 0);

        JPanel fileControlsPanel = createFileControlsPanel(leadingFlowLayout);
        JPanel controlOptionsPanel = createControlOptionsPanel(leadingFlowLayout);
        JPanel editorTextAreaPanel = createSyntaxTextAreaPanel();
        updateCaretLabel();

        this.add(fileControlsPanel);
        this.add(Box.createVerticalStrut(5));
        this.add(controlOptionsPanel);
        this.add(Box.createVerticalStrut(5));
        this.add(editorTextAreaPanel);
    }

    public String getFileName() {
        return fileNameField.getText();
    }

    public void setFileName(String fileName) {
        if (!fileName.isEmpty()) {
            fileNameField.setText(fileName);
        }
    }
    
    public String getJsonText() {
        return jsonSyntaxTextArea.getText();
    }

    public void setJsonText(String jsonText) {
        if (!jsonText.isEmpty()) {
            jsonSyntaxTextArea.setText(jsonText);
        }
    }

    /**
     * Create a file controls JPanel with the specified layout manager
     * @param flowLayout the FlowLayout to use
     * @return the panel
     */
    private JPanel createFileControlsPanel(LayoutManager flowLayout) {
        JPanel panel = new JPanel(flowLayout);

        fileNameField = new JTextField(20);
        panel.add(fileNameField);
        
        newButton = new JButton("New", ImageIconConstants.newFileIcon);
        newButton.setToolTipText("New empty document");
        newButton.addActionListener(e -> {
            fileNameField.setText("New document");
            jsonSyntaxTextArea.setText(null);
            updateUndoRedoState();
        });
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
        panel.add(copyButton);

        return panel;
    }

    /**
     * Create a control options JPanel with the specified layout manager
     * @param flowLayout the FlowLayout to use
     * @return the panel
     */
    private JPanel createControlOptionsPanel(LayoutManager flowLayout) {
        JPanel panel = new JPanel(flowLayout);

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

        // Button for showing debug/sysout/syserr info
        JButton debugButton = new JButton();
        debugButton.setPreferredSize(iconBtnPreferredSize);
        debugButton.addActionListener(e -> printDebugInfo());
        panel.add(debugButton);

        return panel;
    }

    /**
     * Create a RSyntax text area editor JPanel
     * @return the panel
     */
    private JPanel createSyntaxTextAreaPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        jsonSyntaxTextArea = new TextEditorPane();
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
        
        int currentLine = 0;
        int currentColumn = 0;
        try {
            currentLine = jsonSyntaxTextArea.getLineOfOffset(dotPosition);
            currentColumn = dotPosition - jsonSyntaxTextArea.getLineStartOffset(currentLine);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        int selectedChars = 0;
        if (dotPosition > markPosition) {
            selectedChars = dotPosition - markPosition;
        } else if (markPosition > dotPosition) {
            selectedChars = markPosition - dotPosition;
        }
        String selectedCharsString = (selectedChars > 0) ? String.format(" (%1$d selected)", selectedChars) : "";

        String labelText = String.format("Line: %1$d Column: %2$d%3$s", currentLine + 1, currentColumn + 1, selectedCharsString);
        caretLabel.setText(labelText);
    }

    private void printDebugInfo() {
        // System.out.printf("JButton Size: [Minimum=%1$d, Preferred=%2$d, Maximum=%3$d]\n",
        //     copyButton.getMinimumSize(), copyButton.getPreferredSize(), copyButton.getMaximumSize());
        
        Graphics graphics = jsonSyntaxTextArea.getGraphics();
        Font font = graphics.getFont();
        FontMetrics fontMetrics = graphics.getFontMetrics(font);
        System.out.printf("Font: [Name=%1$s, FontName=%2$s, Family=%3$s, Style=%4$d, Size=%5$d];\n",
            font.getName(), font.getFontName(), font.getFamily(), font.getStyle(), font.getSize());
        System.out.printf("FontMetrics: [Ascent=%1$d, Descent=%2$d, Leading=%3$d, Height=%4$d];\n",
            fontMetrics.getAscent(), fontMetrics.getDescent(), fontMetrics.getLeading(), fontMetrics.getHeight());
        
        // GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        // System.out.println("AvailableFontFamilyNames:");
        // for (String fontFamilyName : graphicsEnvironment.getAvailableFontFamilyNames()) {
        //     System.out.println(fontFamilyName + ";");
        // }
    }

    private void open() {
        JFileChooser fileOpen = new JFileChooser(this.lastOpenDirectoryPath);
        fileOpen.setFileFilter(filter);
        int returnValue = fileOpen.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            this.lastOpenDirectoryPath = fileOpen.getCurrentDirectory().getPath();
            loadFile(fileOpen.getSelectedFile());
        } else {
            System.out.println("Open command canceled by user.");
        }
    }

    private void save() {
        JFileChooser fileSave = new JFileChooser(this.lastSaveDirectoryPath);
        fileSave.setFileFilter(filter);
        String fileName = fileNameField.getText();
        if (fileName.isEmpty() || fileName.isBlank()) {
            fileName = fileName.concat("Untitled.json");
        } else {
            fileName = fileName.trim().concat(".json");
        }

        File saveFile = new File(fileName);
        fileSave.setSelectedFile(saveFile);
        
        int returnValue = fileSave.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            this.lastSaveDirectoryPath = fileSave.getCurrentDirectory().getPath();
            saveFile(fileSave.getSelectedFile());
        } else {
            System.out.println("Save command canceled by user.");
        }
    }

    private void loadFile(File file) {
        try {
            String fileName = file.getName().replaceFirst(".json", "");
            fileNameField.setText(fileName);

            FileLocation selectedFileLocation = FileLocation.create(file);
            jsonSyntaxTextArea.load(selectedFileLocation);
        } catch (IOException e) {
            System.out.println("Couldn't read file: " + e.getMessage());
        }
    }

    private void saveFile(File file) {
        try {
            FileLocation selectedFileLocation = FileLocation.create(file);
            jsonSyntaxTextArea.saveAs(selectedFileLocation);
        } catch (IOException e) {
            System.out.println("Couldn't write to file: " + e.getMessage());
        }
    }

    private void updateUndoRedoState() {
        jsonSyntaxTextArea.discardAllEdits();
        undoListener.updateUndoState();
        redoListener.updateRedoState();
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
                    JsonElement parsedJsonElement = jsonStringParser.parseJsonString(jsonString);
                    String formattedJsonString = null;

                    if (command.equals("prettyJson")) {
                        formattedJsonString = prettyPrintSerializeNullsGsonBuilder.toJson(parsedJsonElement);
                    }
                    
                    if (command.equals("compactJson")) {
                        formattedJsonString = serializeNullsGsonBuilder.toJson(parsedJsonElement);
                    }

                    jsonSyntaxTextArea.setText(formattedJsonString);
                } catch (IOException ioe) {
                    // System.err.println(ioe.getMessage());
                    createAndShowErrorDialog(ioe);
                } catch (JsonParseException jsonParseException) {
                    // System.err.println(jsonParseException.getMessage());
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
            // String componentName = e.getComponent().getName();
            // System.out.println(componentName +  " size: " + componentDimension);
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
