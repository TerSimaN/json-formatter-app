package json.formatter.app.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import json.formatter.app.constants.ImageIconConstants;

public class MainPanel extends JPanel {
    private GridLayout gridLayout;
    private FlowLayout customFlowLayout;
    private JsonSyntaxEditorPanel leftJsonEditorPanel;
    private JsonSyntaxEditorPanel rightJsonEditorPanel;

    private boolean isComponentUsed = false;

    MainPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        gridLayout = new GridLayout(2, 1);
        customFlowLayout = new FlowLayout(FlowLayout.CENTER, 10, 0);
        leftJsonEditorPanel = new JsonSyntaxEditorPanel();
        rightJsonEditorPanel = new JsonSyntaxEditorPanel();

        JPanel jsonCopyPanel = createCopyPanel(gridLayout, customFlowLayout);
        JPanel jsonTransformPanel = createTransformPanel(gridLayout, customFlowLayout);
        JPanel jsonDifferencesPanel = createDifferencesPanel(gridLayout, customFlowLayout);

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
        // centralPanel.setBackground(Color.MAGENTA);

        this.add(leftJsonEditorPanel);
        this.add(centralPanel);
        this.add(rightJsonEditorPanel);
        // this.setBackground(Color.CYAN);
    }

    /**
     * Create a copy controls JPanel with the specified layout managers
     * @param gridLayout the GridLayout to use for the panel
     * @param flowLayout the FlowLayout to use for the button panel
     * @return the panel
     */
    private JPanel createCopyPanel(LayoutManager gridLayout, LayoutManager flowLayout) {
        JPanel copyPanel = new JPanel(gridLayout);
        
        JLabel copyLabel = new JLabel("Copy");
        copyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        copyPanel.add(copyLabel);

        JPanel btnCopyPanel = new JPanel(flowLayout);
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
     * Create a transform controls JPanel with the specified layout managers
     * @param gridLayout the GridLayout to use for the panel
     * @param flowLayout the FlowLayout to use for the button panel
     * @return the panel
     */
    private JPanel createTransformPanel(LayoutManager gridLayout, LayoutManager flowLayout) {
        JPanel transformPanel = new JPanel(gridLayout);
        
        JLabel transformLabel = new JLabel("Transform");
        transformLabel.setHorizontalAlignment(SwingConstants.CENTER);
        transformPanel.add(transformLabel);

        JPanel btnTransformPanel = new JPanel(flowLayout);
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
     * Create a differences controls JPanel with the specified layout managers
     * @param gridLayout the GridLayout to use for the panel
     * @param flowLayout the FlowLayout to use for the button panel
     * @return the panel
     */
    private JPanel createDifferencesPanel(LayoutManager gridLayout, LayoutManager flowLayout) {
        JPanel differencesPanel = new JPanel(gridLayout);
        
        JLabel differencesLabel = new JLabel("Differences");
        differencesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        differencesPanel.add(differencesLabel);

        JPanel btnDifferencesPanel = new JPanel(flowLayout);
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
}
