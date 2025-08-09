package json.formatter.app.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

import json.formatter.app.constants.ImageIconConstants;

public class MainWindowFrame {
    private JFrame mainFrame;
    private GridLayout gridLayout;
    private FlowLayout centerFlowLayout;
    private JsonSyntaxEditorPanel leftJsonEditorPanel;
    private JsonSyntaxEditorPanel rightJsonEditorPanel;
    private String defaultTitle = "JSON Formatter";
    private String changedTitle = null;

    private boolean isComponentUsed = false;

    public void createAndShowGUI() {
        mainFrame = new JFrame();
        mainFrame.setName("MainFrame");
        mainFrame.setTitle(defaultTitle);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel mainPanel = createMainPanel();
        mainFrame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        
        mainFrame.setMinimumSize(new Dimension(1120, 720));
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        gridLayout = new GridLayout(2, 1);
        centerFlowLayout = new FlowLayout(FlowLayout.CENTER, 10, 0);

        leftJsonEditorPanel = new JsonSyntaxEditorPanel();
        leftJsonEditorPanel.addPropertyChangeListener("fileNameChange", new FileNameChangeListener());

        rightJsonEditorPanel = new JsonSyntaxEditorPanel();
        rightJsonEditorPanel.addPropertyChangeListener("fileNameChange", new FileNameChangeListener());

        JPanel jsonCopyPanel = createCopyPanel();
        JPanel jsonTransformPanel = createTransformPanel();
        JPanel jsonDifferencesPanel = createDifferencesPanel();

        // JSON controls panel
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.add(jsonCopyPanel);
        controlsPanel.add(jsonTransformPanel);
        controlsPanel.add(jsonDifferencesPanel);

        int controlsPanelPreferredHeight = controlsPanel.getPreferredSize().height + 10;
        int controlsPanelPreferredWidth = controlsPanel.getPreferredSize().width;
        Dimension centralPanelMaximumSize = new Dimension(controlsPanelPreferredWidth, controlsPanelPreferredHeight);

        JPanel centralPanel = new JPanel();
        centralPanel.setMaximumSize(centralPanelMaximumSize);
        centralPanel.add(controlsPanel);

        panel.add(leftJsonEditorPanel);
        panel.add(centralPanel);
        panel.add(rightJsonEditorPanel);

        return panel;
    }

    /**
     * Creates a copy controls JPanel
     * @return the panel
     */
    private JPanel createCopyPanel() {
        JPanel copyPanel = new JPanel(gridLayout);
        
        JLabel copyLabel = new JLabel("Copy");
        copyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        copyPanel.add(copyLabel);

        JPanel btnCopyPanel = new JPanel(centerFlowLayout);
        JButton copyRightToLeftBtn = new JButton(ImageIconConstants.arrowLeftBoldIcon);
        copyRightToLeftBtn.setToolTipText("Copy the contents of the right panel to the left panel");
        copyRightToLeftBtn.setActionCommand("copyRightToLeft");
        copyRightToLeftBtn.addActionListener(new CopyContentListener());
        btnCopyPanel.add(copyRightToLeftBtn);
        
        JButton copyLeftToRightBtn = new JButton(ImageIconConstants.arrowRightBoldIcon);
        copyLeftToRightBtn.setToolTipText("Copy the contents of the left panel to the right panel");
        copyLeftToRightBtn.setActionCommand("copyLeftToRight");
        copyLeftToRightBtn.addActionListener(new CopyContentListener());
        btnCopyPanel.add(copyLeftToRightBtn);
        
        copyPanel.add(btnCopyPanel);

        return copyPanel;
    }

    /**
     * Creates a transform controls JPanel
     * @return the panel
     */
    private JPanel createTransformPanel() {
        JPanel transformPanel = new JPanel(gridLayout);
        
        JLabel transformLabel = new JLabel("Transform");
        transformLabel.setHorizontalAlignment(SwingConstants.CENTER);
        transformPanel.add(transformLabel);

        JPanel btnTransformPanel = new JPanel(centerFlowLayout);
        JButton transformRightToLeftBtn = new JButton(ImageIconConstants.arrowLeftBoldIcon);
        transformRightToLeftBtn.setToolTipText("Transform the contents of the right panel into the left panel");
        transformRightToLeftBtn.setActionCommand("transformRightToLeft");
        transformRightToLeftBtn.addActionListener(new TransformContentListener());
        transformRightToLeftBtn.setEnabled(isComponentUsed);
        btnTransformPanel.add(transformRightToLeftBtn);
        
        JButton transformLeftToRightBtn = new JButton(ImageIconConstants.arrowRightBoldIcon);
        transformLeftToRightBtn.setToolTipText("Transform the contents of the left panel into the right panel");
        transformLeftToRightBtn.setActionCommand("transformLeftToRight");
        transformLeftToRightBtn.addActionListener(new TransformContentListener());
        transformLeftToRightBtn.setEnabled(isComponentUsed);
        btnTransformPanel.add(transformLeftToRightBtn);
        
        transformPanel.add(btnTransformPanel);

        return transformPanel;
    }

    /**
     * Creates a differences controls JPanel
     * @return the panel
     */
    private JPanel createDifferencesPanel() {
        JPanel differencesPanel = new JPanel(gridLayout);
        
        JLabel differencesLabel = new JLabel("Differences");
        differencesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        differencesPanel.add(differencesLabel);

        JPanel btnDifferencesPanel = new JPanel(centerFlowLayout);
        JButton compareDifferencesBtn = new JButton("Compare");
        compareDifferencesBtn.setToolTipText("Highlight the differences between left and right panel contents (currently disabled)");
        compareDifferencesBtn.addActionListener(e -> compareJsonContent());
        compareDifferencesBtn.setEnabled(false);
        btnDifferencesPanel.add(compareDifferencesBtn);
        
        JCheckBox enableCompareBtnCheckBox = new JCheckBox();
        enableCompareBtnCheckBox.addActionListener(e -> {
            boolean isEnabled = compareDifferencesBtn.isEnabled();
            compareDifferencesBtn.setToolTipText("Highlight the differences between left and right panel contents (currently enabled)");
            compareDifferencesBtn.setEnabled(!isEnabled);
        });
        enableCompareBtnCheckBox.setEnabled(isComponentUsed);
        btnDifferencesPanel.add(enableCompareBtnCheckBox, 0);

        differencesPanel.add(btnDifferencesPanel);

        return differencesPanel;
    }

    /**
     * Compares the contents of both editors
     */
    private void compareJsonContent() {
        System.err.println("Unimplemented method 'compareJsonContent'");
    }

    class CopyContentListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            String jsonStringToCopy = null;
            String fileNameString = null;

            if (command.equals("copyLeftToRight")) {
                jsonStringToCopy = leftJsonEditorPanel.getJsonText();
                rightJsonEditorPanel.setJsonText(jsonStringToCopy);
                
                fileNameString = rightJsonEditorPanel.getFileName();
                if ((!jsonStringToCopy.isEmpty() || !jsonStringToCopy.isBlank()) &&
                        (fileNameString.isEmpty() || fileNameString.isBlank())) {
                    rightJsonEditorPanel.setFileName("New document");
                }
            }

            if (command.equals("copyRightToLeft")) {
                jsonStringToCopy = rightJsonEditorPanel.getJsonText();
                leftJsonEditorPanel.setJsonText(jsonStringToCopy);

                fileNameString = leftJsonEditorPanel.getFileName();
                if ((!jsonStringToCopy.isEmpty() || !jsonStringToCopy.isBlank()) &&
                        (fileNameString.isEmpty() || fileNameString.isBlank())) {
                    leftJsonEditorPanel.setFileName("New document");
                }
            }
        }
    }

    class TransformContentListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if (command.equals("transformLeftToRight")) {
                System.err.println("Unimplemented method 'transformLeftToRightJsonContent'");
            }

            if (command.equals("transformRightToLeft")) {
                System.err.println("Unimplemented method 'transformRightToLeftJsonContent'");
            }
        }
    }

    class FileNameChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();

            String newTitle = null;
            if (property.equals("fileNameChange")) {
                newTitle = makeNewTitle();
                changedTitle = String.format("%1$s (%2$s)", defaultTitle, newTitle);
                mainFrame.setTitle(changedTitle);
            }
        }

        private String makeNewTitle() {
            String leftTitle = leftJsonEditorPanel.getFileName();
            String rightTitle = rightJsonEditorPanel.getFileName();

            String combinedFileNames = null;
            if (!leftTitle.isEmpty() || !leftTitle.isBlank()) {
                combinedFileNames = leftTitle;
            }

            if ((combinedFileNames != null) && (!rightTitle.isEmpty() || !rightTitle.isBlank())) {
                combinedFileNames += ", " + rightTitle;
            } else if (!rightTitle.isEmpty() || !rightTitle.isBlank()) {
                combinedFileNames = rightTitle;
            }

            return (combinedFileNames == null) ? "Untitled" : combinedFileNames;
        }
    }
}
