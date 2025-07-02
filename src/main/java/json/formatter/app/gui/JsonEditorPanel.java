package json.formatter.app.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.*;
import javax.swing.undo.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

import com.google.gson.*;

import json.formatter.app.constants.ImageIconConstants;
import json.formatter.app.json.JsonStringParser;

public class JsonEditorPanel extends JPanel {
    private Gson serializeNullsGsonBuilder = new GsonBuilder().serializeNulls().create();
    private Gson prettyPrintSerializeNullsGsonBuilder = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private JsonStringParser jsonStringParser = new JsonStringParser();

    private JButton newButton;
    private JButton openButton;
    private JButton saveButton;
    private JButton copyButton;
    private JTextField fileNameField;
    private JTextArea jsonTextArea;
    private Dimension iconBtnPreferredSize = new Dimension(30, 30);

    private FileNameExtensionFilter filter;
    private FlowLayout customFlowLayout;
    private String lastOpenDirectoryPath;
    private String lastSaveDirectoryPath;

    // Undo and redo helpers
    protected UndoListener undoListener;
    protected RedoListener redoListener;
    protected UndoableEdit undoableEdit;
    protected UndoManager undoManager = new UndoManager();

    JsonEditorPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(Color.PINK);
        this.setName("JsonEditorPanel");
        this.addComponentListener(new PanelEventListener());

        filter = new FileNameExtensionFilter("JSON files (*.json)", "json");
        customFlowLayout = new FlowLayout(FlowLayout.LEADING, 5, 0);
        lastOpenDirectoryPath = null;
        lastSaveDirectoryPath = null;

        JPanel fileControlsPanel = createFileControlsPanel(customFlowLayout);
        JPanel controlOptionsPanel = createControlOptionsPanel(customFlowLayout);

        // Text area
        jsonTextArea = new JTextArea();
        jsonTextArea.getDocument().addUndoableEditListener(new JsonUndoableEditListener());
        jsonTextArea.getDocument().addDocumentListener(new TextAreaDocumentListener());
        jsonTextArea.setMargin(new Insets(2, 5, 2, 5));
        
        JScrollPane jsonScrollPane = new JScrollPane(jsonTextArea);
        jsonScrollPane.setPreferredSize(new Dimension(0, 650));
        jsonScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jsonScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        this.add(fileControlsPanel);
        this.add(Box.createVerticalStrut(5));
        this.add(controlOptionsPanel);
        this.add(Box.createVerticalStrut(5));
        this.add(jsonScrollPane);
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
        newButton.addActionListener(e -> {
            fileNameField.setText("New document");
            jsonTextArea.setText(null);
            updateUndoRedoManagerState();
        });
        panel.add(newButton);

        openButton = new JButton("Open", ImageIconConstants.openFileIcon);
        openButton.addActionListener(e -> {
            open();
            updateUndoRedoManagerState();
        });
        panel.add(openButton);

        saveButton = new JButton("Save", ImageIconConstants.saveFileIcon);
        saveButton.addActionListener(e -> save());
        panel.add(saveButton);

        copyButton = new JButton("Copy", ImageIconConstants.copyFileIcon);
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

    public String getJsonText() {
        return jsonTextArea.getText();
    }

    public void setJsonText(String jsonText) {
        jsonTextArea.setText(jsonText);
    }

    private void printDebugInfo() {
        System.out.println("UndoManager info: " + undoManager);

        Document doc = jsonTextArea.getDocument();
        if (doc instanceof AbstractDocument) {
            System.out.println("jsonTextArea is an instance of AbstractDocument");
        }

        // System.out.println("JButton MinimumSize: " + copyButton.getMinimumSize());
        // System.out.println("JButton PreferredSize: " + copyButton.getPreferredSize());
        // System.out.println("JButton MaximumSize: " + copyButton.getMaximumSize());
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
            
            jsonTextArea.setText(strBuilder.toString());
        } catch (FileNotFoundException fe) {
            System.out.println("File not found: " + fe.getMessage());
        } catch (IOException e) {
            System.out.println("Couldn't read file: " + e.getMessage());
        }
    }

    private void saveFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            String jsonString = jsonTextArea.getText();
            writer.write(jsonString);
        } catch (IOException e) {
            System.out.println("Couldn't write to file: " + e.getMessage());
        }
    }

    private void prettyPrintJson() {
        String jsonString = jsonTextArea.getText();
        if (!jsonString.isEmpty()) {
            try {
                JsonElement parsedJsonElement = jsonStringParser.parseJsonString(jsonString);
                String prettyPrintedJsonString = prettyPrintSerializeNullsGsonBuilder.toJson(parsedJsonElement);
                jsonTextArea.setText(prettyPrintedJsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void compactPrintJson() {
        String jsonString = jsonTextArea.getText();
        if (!jsonString.isEmpty()) {
            try {
                JsonElement parsedJsonElement = jsonStringParser.parseJsonString(jsonString);
                String compactJsonString = serializeNullsGsonBuilder.toJson(parsedJsonElement);
                jsonTextArea.setText(compactJsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateUndoRedoManagerState() {
        undoManager.discardAllEdits();
        undoListener.updateUndoState();
        redoListener.updateRedoState();
    }

    // Class listening for edits that can be undone
    protected class JsonUndoableEditListener implements UndoableEditListener {
        private int eventNumber = 0;

        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            eventNumber++;
            undoableEdit = e.getEdit();
            // System.out.printf("UndoableEditEvent No. %1$d: PresentationName: %2$s\n",
            //     eventNumber, undoableEdit.getPresentationName());
            
            undoManager.addEdit(undoableEdit);
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
            try {
                undoManager.undo();
            } catch (CannotUndoException ex) {
                System.out.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            updateUndoState();
            redoListener.updateRedoState();
        }

        protected void updateUndoState() {
            if (undoManager.canUndo()) {
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
            try {
                undoManager.redo();
            } catch (CannotRedoException ex) {
                System.out.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            undoListener.updateUndoState();
            updateRedoState();
        }

        protected void updateRedoState() {
            if (undoManager.canRedo()) {
                component.setEnabled(true);
            } else {
                component.setEnabled(false);
            }
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
            displayEventInfo(e);
        }

        private void displayEventInfo(DocumentEvent e) {
            Document document = e.getDocument();
            int documentLength = document.getLength();
            int changeLength = e.getLength();
            String eventType = e.getType().toString();
            System.out.printf("%1$s: %2$d character%3$s Text length = %4$d.\n",
                eventType, changeLength, ((changeLength == 1) ? "," : "s,"), documentLength);
        }
    }
}
