package json.formatter.app.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.*;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.parser.*;
import org.fife.ui.rtextarea.*;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;

import com.google.gson.*;

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
    private RSyntaxTextArea jsonSyntaxTextArea;
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
    
    public String getJsonText() {
        return jsonSyntaxTextArea.getText();
    }

    public void setJsonText(String jsonText) {
        jsonSyntaxTextArea.setText(jsonText);
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
        openButton.addActionListener(e -> {
            open();
            updateUndoRedoState();
        });
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
        formatJsonButton.addActionListener(e -> prettyPrintJson());
        panel.add(formatJsonButton);

        JButton compactJsonButton = new JButton(ImageIconConstants.compactJsonIcon);
        compactJsonButton.setPreferredSize(iconBtnPreferredSize);
        compactJsonButton.setToolTipText("Compact JSON: remove all white spacing and new lines");
        compactJsonButton.addActionListener(e -> compactPrintJson());
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

        jsonSyntaxTextArea = new RSyntaxTextArea();
        jsonSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        jsonSyntaxTextArea.setCodeFoldingEnabled(true);
        jsonSyntaxTextArea.addParser(new TextAreaParser());

        jsonSyntaxTextArea.getDocument().addUndoableEditListener(new JsonUndoableEditListener());
        jsonSyntaxTextArea.getDocument().addDocumentListener(new TextAreaDocumentListener());

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

    private void prettyPrintJson() {
        String jsonString = jsonSyntaxTextArea.getText();
        if (!jsonString.isEmpty()) {
            try {
                JsonElement parsedJsonElement = jsonStringParser.parseJsonString(jsonString);
                String prettyPrintedJsonString = prettyPrintSerializeNullsGsonBuilder.toJson(parsedJsonElement);
                jsonSyntaxTextArea.setText(prettyPrintedJsonString);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void compactPrintJson() {
        String jsonString = jsonSyntaxTextArea.getText();
        if (!jsonString.isEmpty()) {
            try {
                JsonElement parsedJsonElement = jsonStringParser.parseJsonString(jsonString);
                String compactJsonString = serializeNullsGsonBuilder.toJson(parsedJsonElement);
                jsonSyntaxTextArea.setText(compactJsonString);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
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
        if (!fileName.equals("")) {
            fileName = fileName.concat(".json");
            File saveFile = new File(fileName);
            fileSave.setSelectedFile(saveFile);
        }
        
        int returnValue = fileSave.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            this.lastSaveDirectoryPath = fileSave.getCurrentDirectory().getPath();
            saveFile(fileSave.getSelectedFile());
        } else {
            System.out.println("Save command canceled by user.");
        }
    }

    private void loadFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String fileName = file.getName().replaceFirst(".json", "");
            fileNameField.setText(fileName);

            StringBuilder strBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                strBuilder.append(line + "\n");
            }
            
            jsonSyntaxTextArea.setText(strBuilder.toString());
        } catch (FileNotFoundException fe) {
            System.out.println("File not found: " + fe.getMessage());
        } catch (IOException e) {
            System.out.println("Couldn't read file: " + e.getMessage());
        }
    }

    private void saveFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String jsonString = jsonSyntaxTextArea.getText();
            writer.write(jsonString);
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

    class TextAreaParser extends AbstractParser {
        @Override
        public ParseResult parse(RSyntaxDocument arg0, String arg1) {
            DefaultParseResult result = new DefaultParseResult(this);
            result.addNotice(new DefaultParserNotice(this, "Message", 0));
            return result;
        }
    }

    // Custom event listener classes
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

    class TextAreaDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            displayEventInfo(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            displayEventInfo(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            // displayEventInfo(e);
        }

        private void displayEventInfo(DocumentEvent e) {
            Document document = e.getDocument();
            int documentLength = document.getLength();
            int changeLength = e.getLength();
            String eventType = e.getType().toString();
            System.out.printf("%1$s: %2$d character%3$s Text length: %4$d;\n",
                eventType, changeLength, ((changeLength == 1) ? "," : "s,"), documentLength);
        }
    }
}
