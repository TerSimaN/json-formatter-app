package json.formatter.app.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

    private ImageIconConstants iconConstants;
    private FileNameExtensionFilter fileFilter;
    private FlowLayout leadingFlowLayout;
    private Dimension iconBtnPreferredSize = new Dimension(32, 32);
    private Frame parentFrame;

    // File controls panel
    private JButton newButton;
    private JButton openButton;
    private JButton saveButton;
    private JButton copyButton;
    private String lastOpenDirectoryPath = null;
    private String lastSaveDirectoryPath = null;
    private String fullFilePath = "Untitled";
    private boolean hasFullFilePathChanged = false;
    private boolean hasFileChanged = false;

    // Control options panel
    private JButton lineWrapButton;
    private JButton findReplaceButton;
    
    // Search options panel
    private JPanel searchPanel;
    private JTextField searchField;
    private JButton prevButton;
    private JButton nextButton;
    private JCheckBox regexCheckBox;
    private JCheckBox matchCaseCheckBox;
    private JCheckBox wholeWordCheckBox;
    private boolean isSearchPanelShowing = true;

    // Replace options panel
    private JPanel replacePanel;
    private JTextField replaceField;
    private boolean isReplacePanelShowing = true;
    
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

        fileFilter = new FileNameExtensionFilter("JSON files (*.json)", "json");
        leadingFlowLayout = new FlowLayout(FlowLayout.LEADING, 5, 0);
        iconConstants = new ImageIconConstants();
        addKeyBindings();

        JPanel fileControlsPanel = createFileControlsPanel();
        JPanel controlOptionsPanel = createControlOptionsPanel();
        JPanel editorTextAreaPanel = createSyntaxTextAreaPanel();
        searchPanel = createSearchPanel();
        searchPanel.setVisible(false);
        replacePanel = createReplacePanel();
        replacePanel.setVisible(false);

        updateCaretLabel();
        updateEditorTheme();

        this.add(Box.createVerticalStrut(5));
        this.add(fileControlsPanel);
        this.add(Box.createVerticalStrut(5));
        this.add(controlOptionsPanel);
        this.add(Box.createVerticalStrut(5));
        this.add(searchPanel);
        this.add(Box.createVerticalStrut(5));
        this.add(replacePanel);
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
        
        newButton = new JButton("New", iconConstants.newFileIcon);
        newButton.setToolTipText("New document");
        newButton.addActionListener(e -> createAndShowNewWindowFrame());
        panel.add(newButton);

        openButton = new JButton("Open", iconConstants.openFileIcon);
        openButton.setToolTipText("Open a JSON file");
        openButton.addActionListener(e -> open());
        panel.add(openButton);

        saveButton = new JButton("Save", iconConstants.saveFileIcon);
        saveButton.setToolTipText("Save file");
        saveButton.addActionListener(e -> save());
        panel.add(saveButton);

        copyButton = new JButton("Copy", iconConstants.copyFileIcon);
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

        JButton formatJsonButton = new JButton(iconConstants.formatJsonIcon);
        formatJsonButton.setPreferredSize(iconBtnPreferredSize);
        formatJsonButton.setToolTipText("Format JSON: add proper identation and new lines");
        formatJsonButton.setActionCommand("prettyJson");
        formatJsonButton.addActionListener(new FormatJsonListener());
        panel.add(formatJsonButton);

        JButton compactJsonButton = new JButton(iconConstants.compactJsonIcon);
        compactJsonButton.setPreferredSize(iconBtnPreferredSize);
        compactJsonButton.setToolTipText("Compact JSON: remove all white spacing and new lines");
        compactJsonButton.setActionCommand("compactJson");
        compactJsonButton.addActionListener(new FormatJsonListener());
        panel.add(compactJsonButton);

        JButton undoButton = new JButton(iconConstants.arrowLeftBoldIcon);
        undoButton.setPreferredSize(iconBtnPreferredSize);
        undoButton.setToolTipText("Undo");
        undoListener = new UndoListener(undoButton);
        undoButton.addActionListener(undoListener);
        panel.add(undoButton);

        JButton redoButton = new JButton(iconConstants.arrowRightBoldIcon);
        redoButton.setPreferredSize(iconBtnPreferredSize);
        redoButton.setToolTipText("Redo");
        redoListener = new RedoListener(redoButton);
        redoButton.addActionListener(redoListener);
        panel.add(redoButton);

        lineWrapButton = new JButton(iconConstants.wrapEnableIcon);
        lineWrapButton.setPreferredSize(iconBtnPreferredSize);
        lineWrapButton.setToolTipText("Line wrap (currently disabled)");
        lineWrapButton.addActionListener(e -> updateLineWrapState());
        panel.add(lineWrapButton);

        findReplaceButton = new JButton(iconConstants.findReplaceIcon);
        findReplaceButton.setPreferredSize(iconBtnPreferredSize);
        findReplaceButton.setToolTipText("Find/Replace");
        findReplaceButton.addActionListener(e -> showHideReplacePanel());
        panel.add(findReplaceButton);

        return panel;
    }

    /**
     * Creates a search JPanel
     * @return the panel
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(leadingFlowLayout);

        JLabel findLabel = new JLabel("       Find: ");
        panel.add(findLabel);
        searchField = new JTextField(20);
        panel.add(searchField);

        prevButton = new JButton(iconConstants.arrowUpBoldIcon);
        prevButton.setPreferredSize(iconBtnPreferredSize);
        prevButton.setToolTipText("Previous Match");
        prevButton.addActionListener(new FindReplaceListener());
        panel.add(prevButton);

        nextButton = new JButton(iconConstants.arrowDownBoldIcon);
        nextButton.setPreferredSize(iconBtnPreferredSize);
        nextButton.setToolTipText("Next Match");
        nextButton.setActionCommand("findNext");
        nextButton.addActionListener(new FindReplaceListener());
        panel.add(nextButton);

        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextButton.doClick();
            }
        });

        regexCheckBox = new JCheckBox("Regex");
        panel.add(regexCheckBox);
        matchCaseCheckBox = new JCheckBox("Match Case");
        panel.add(matchCaseCheckBox);
        wholeWordCheckBox = new JCheckBox("Whole Word");
        panel.add(wholeWordCheckBox);

        return panel;
    }

    /**
     * Creates a replace JPanel
     * @return the panel
     */
    private JPanel createReplacePanel() {
        JPanel panel = new JPanel(leadingFlowLayout);

        JLabel replaceLabel = new JLabel("Replace: ");
        panel.add(replaceLabel);
        replaceField = new JTextField(20);
        panel.add(replaceField);

        JButton replaceButton = new JButton("Replace");
        replaceButton.setActionCommand("replace");
        replaceButton.addActionListener(new FindReplaceListener());
        panel.add(replaceButton);

        replaceField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                replaceButton.doClick();
            }
        });

        JButton replaceAllButton = new JButton("Replace All");
        replaceAllButton.setActionCommand("replaceAll");
        replaceAllButton.addActionListener(new ReplaceAllListener());
        panel.add(replaceAllButton);

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
        jsonSyntaxTextArea.getDocument().addDocumentListener(new SyntaxTextAreaDocumentListener());

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

    /**
     * Creates a new json editor window
     */
    private void createAndShowNewWindowFrame() {
        MainWindowFrame newWindowFrame = new MainWindowFrame(parentFrame);
        newWindowFrame.pack();
        newWindowFrame.setVisible(true);
    }

    /**
     * Brings up an error dialog that displays a message
     * determined by the <code>exception</code> parameter
     * @param exception determines what message to display
     */
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
        } else if (errorMessage.contains("Malformed")) {
            int firstIndex = errorMessage.indexOf("Malformed");
            errorMessage = errorMessage.substring(firstIndex);
        } else if (errorMessage.contains("Unterminated string")) {
            int firstIndex = errorMessage.indexOf("Unterminated");
            errorMessage = errorMessage.substring(firstIndex, newlineIndex);
        }

        JOptionPane.showMessageDialog(null, errorMessage,
            "JSON Editor Error", JOptionPane.ERROR_MESSAGE);
    }

    private void addKeyBindings() {
        InputMap inputMap = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = this.getActionMap();

        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);
        inputMap.put(key, "showHideFind");
        actionMap.put("showHideFind", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHideSearchPanel();
            }
        });

        key = KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK);
        inputMap.put(key, "showHideFindAndReplace");
        actionMap.put("showHideFindAndReplace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHideReplacePanel();
            }
        });

        key = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        inputMap.put(key, "backwardSearch");
        actionMap.put("backwardSearch", new FindReplaceListener());

        key = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
        inputMap.put(key, "forwardSearch");
        actionMap.put("forwardSearch", new FindReplaceListener(true));

        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK);
        inputMap.put(key, "newFile");
        actionMap.put("newFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAndShowNewWindowFrame();
            }
        });

        key = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
        inputMap.put(key, "openFile");
        actionMap.put("openFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        });

        key = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
        inputMap.put(key, "saveFile");
        actionMap.put("saveFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
    }

    private void showHideSearchPanel() {
        if (!replacePanel.isVisible()) {
            searchPanel.setVisible(isSearchPanelShowing);
            isSearchPanelShowing = !isSearchPanelShowing;
            jsonSyntaxTextArea.clearMarkAllHighlights();
        }
    }

    private void showHideReplacePanel() {
        if (!searchPanel.isVisible()) {
            searchPanel.setVisible(isSearchPanelShowing);
            replacePanel.setVisible(isReplacePanelShowing);
            isSearchPanelShowing = !isSearchPanelShowing;
            isReplacePanelShowing = !isReplacePanelShowing;
        } else if (!replacePanel.isVisible()) {
            replacePanel.setVisible(isReplacePanelShowing);
            isReplacePanelShowing = !isReplacePanelShowing;
        } else {
            searchPanel.setVisible(isSearchPanelShowing);            
            replacePanel.setVisible(isReplacePanelShowing);
            isSearchPanelShowing = !isSearchPanelShowing;
            isReplacePanelShowing = !isReplacePanelShowing;
            jsonSyntaxTextArea.clearMarkAllHighlights();
        }
    }

    private void updateEditorTheme() {
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream("/json/formatter/app/themes/json-formatter.xml"));
            theme.apply(jsonSyntaxTextArea);
        } catch (IOException e) {
            createAndShowErrorDialog(e);
        }
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
        Icon lineWrapIcon = hasLineWrap ? iconConstants.wrapDisableIcon : iconConstants.wrapEnableIcon;
        lineWrapButton.setIcon(lineWrapIcon);
        String toolTipText = hasLineWrap ? "Line wrap (currently enabled)" : "Line wrap (currently disabled)";
        lineWrapButton.setToolTipText(toolTipText);
        hasLineWrap = !hasLineWrap;
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

        File saveFile;
        if (fullFilePath.equals("Untitled")) {
            saveFile = new File("Untitled.json");
            fileSave.setSelectedFile(saveFile);
            
            int returnValue = fileSave.showSaveDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                this.lastSaveDirectoryPath = fileSave.getCurrentDirectory().getPath();
                saveFile(fileSave.getSelectedFile());
            } else {
                System.err.println("Save command canceled by user.");
            }
        } else {
            saveFile = new File(fullFilePath);
            saveFile(saveFile);
        }
    }

    private void loadFile(File file) {
        if (!hasLineWrap) {
            updateLineWrapState();
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder strBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if ((line.length() > 10000) && hasLineWrap) {
                    updateLineWrapState();
                }

                strBuilder.append(line + "\n");
            }
            
            JsonElement parsedJsonElement = JsonParser.parseString(strBuilder.toString());
            String formattedJsonString = prettyPrintSerializeNullsGsonBuilder.toJson(parsedJsonElement);
            
            jsonSyntaxTextArea.setText(formattedJsonString);
            jsonSyntaxTextArea.setCaretPosition(0);
            
            hasFileChanged = false;
            fullFilePath = file.getAbsolutePath();
            updateWindowTitle();
        } catch (FileNotFoundException fe) {
            createAndShowErrorDialog(fe);
        } catch (IOException e) {
            createAndShowErrorDialog(e);
        } catch (JsonSyntaxException jsonSyntaxException) {
            createAndShowErrorDialog(jsonSyntaxException);
        }
    }

    private void saveFile(File file) {
        try {
            FileLocation selectedFileLocation = FileLocation.create(file);
            jsonSyntaxTextArea.saveAs(selectedFileLocation);
            
            hasFileChanged = false;
            fullFilePath = file.getAbsolutePath();
            updateWindowTitle();
        } catch (IOException e) {
            createAndShowErrorDialog(e);
        }
    }

    private void updateWindowTitle() {
        String propertyName = null;
        boolean oldValue = false;
        boolean newValue = false;

        if (hasFileChanged) {
            propertyName = "fileChanged";
            newValue = hasFileChanged;
            oldValue = !newValue;
        } else {
            propertyName = "fullFilePath";
            oldValue = hasFullFilePathChanged;
            newValue = !oldValue;
            hasFullFilePathChanged = !newValue;
        }

        this.firePropertyChange(propertyName, oldValue, newValue);
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
    
    // A json format event listener
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

    // A find/replace event listener
    class FindReplaceListener extends AbstractAction {
        private boolean forwardSearch = false;

        FindReplaceListener() {}

        FindReplaceListener(boolean doForwardSearch) {
            this.forwardSearch = doForwardSearch;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            boolean hasCommand = (command != null) && (command.equals("findNext") || command.equals("replace"));
            boolean forward = forwardSearch || hasCommand;

            SearchContext context = new SearchContext();
            String searchText = searchField.getText();
            String replaceText = replaceField.getText();
            if (searchText.isEmpty()) {
                return;
            }

            context.setSearchFor(searchText);
            context.setMatchCase(matchCaseCheckBox.isSelected());
            context.setRegularExpression(regexCheckBox.isSelected());
            context.setWholeWord(wholeWordCheckBox.isSelected());
            context.setSearchForward(forward);

            boolean found;
            if ((command != null) && command.equals("replace")) {
                if (replaceText.isEmpty()) {
                    return;
                }

                context.setReplaceWith(replaceText);
                found = SearchEngine.replace(jsonSyntaxTextArea, context).wasFound();
            } else {
                found = SearchEngine.find(jsonSyntaxTextArea, context).wasFound();
            }

            if (!found) {
                JOptionPane.showMessageDialog(parentFrame, "Text not found");
            }
        }
    }

    // A replaceAll event listener
    class ReplaceAllListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SearchContext replaceContext = new SearchContext();
            String searchText = searchField.getText();
            String replaceText = replaceField.getText();
            if (searchText.isEmpty() || replaceText.isEmpty()) {
                return;
            }

            replaceContext.setSearchFor(searchText);
            replaceContext.setReplaceWith(replaceText);
            replaceContext.setMatchCase(matchCaseCheckBox.isSelected());
            replaceContext.setRegularExpression(regexCheckBox.isSelected());
            replaceContext.setWholeWord(wholeWordCheckBox.isSelected());

            boolean found = SearchEngine.replaceAll(jsonSyntaxTextArea, replaceContext).wasFound();
            if (!found) {
                JOptionPane.showMessageDialog(parentFrame, "Text not found");
            }
        }
    }

    // A custom RSyntaxTextArea document listener
    class SyntaxTextAreaDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) { }

        @Override
        public void removeUpdate(DocumentEvent e) { }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (!hasFileChanged) {
                hasFileChanged = true;
                updateWindowTitle();
            }
        }
    }
}
