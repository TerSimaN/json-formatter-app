package json.formatter.app.gui;

import java.awt.*;
import javax.swing.*;

import json.formatter.app.constants.ContentDirection;
import json.formatter.app.constants.ImageIconConstants;

public class MainPanel extends JPanel {
    private GridLayout gridLayout;
    private FlowLayout customFlowLayout;
    private JsonEditorPanel leftJsonEditorPanel;
    private JsonEditorPanel rightJsonEditorPanel;

    MainPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        gridLayout = new GridLayout(2, 1);
        customFlowLayout = new FlowLayout(FlowLayout.CENTER, 10, 0);
        leftJsonEditorPanel = new JsonEditorPanel();
        rightJsonEditorPanel = new JsonEditorPanel();

        // Copy JSON panel
        JPanel jsonCopyPanel = new JPanel(gridLayout);
        JLabel copyLabel = new JLabel("Copy");
        copyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        jsonCopyPanel.add(copyLabel);

        JPanel btnCopyPanel = new JPanel(customFlowLayout);
        JButton copyRightToLeftBtn = new JButton(ImageIconConstants.arrowLeftBoldIcon);
        copyRightToLeftBtn.setToolTipText("Copy the contents of the right panel to the left panel");
        copyRightToLeftBtn.addActionListener(e -> copyJsonContent(ContentDirection.LEFT));
        btnCopyPanel.add(copyRightToLeftBtn);
        JButton copyLeftToRightBtn = new JButton(ImageIconConstants.arrowRightBoldIcon);
        copyLeftToRightBtn.setToolTipText("Copy the contents of the left panel to the right panel");
        copyLeftToRightBtn.addActionListener(e -> copyJsonContent(ContentDirection.RIGHT));
        btnCopyPanel.add(copyLeftToRightBtn);
        jsonCopyPanel.add(btnCopyPanel);

        // Transform JSON panel
        JPanel jsonTransformPanel = new JPanel(gridLayout);
        JLabel transformLabel = new JLabel("Transform");
        transformLabel.setHorizontalAlignment(SwingConstants.CENTER);
        jsonTransformPanel.add(transformLabel);

        JPanel btnTransformPanel = new JPanel(customFlowLayout);
        JButton transformRightToLeftBtn = new JButton(ImageIconConstants.arrowLeftBoldIcon);
        transformRightToLeftBtn.setToolTipText("Transform the contents of the right panel into the left panel");
        transformRightToLeftBtn.addActionListener(e -> transformJsonContent(ContentDirection.LEFT));
        btnTransformPanel.add(transformRightToLeftBtn);
        JButton transformLeftToRightBtn = new JButton(ImageIconConstants.arrowRightBoldIcon);
        transformLeftToRightBtn.setToolTipText("Transform the contents of the left panel into the right panel");
        transformLeftToRightBtn.addActionListener(e -> transformJsonContent(ContentDirection.RIGHT));
        btnTransformPanel.add(transformLeftToRightBtn);
        jsonTransformPanel.add(btnTransformPanel);

        // Check JSON differences panel
        JPanel jsonDifferencesPanel = new JPanel(gridLayout);
        JLabel differencesLabel = new JLabel("Differences");
        differencesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        jsonDifferencesPanel.add(differencesLabel);

        JPanel btnDifferencesPanel = new JPanel(customFlowLayout);
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
        btnDifferencesPanel.add(enableCompareBtnCheckBox, 0);
        jsonDifferencesPanel.add(btnDifferencesPanel);

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
        centralPanel.setBackground(Color.MAGENTA);

        this.add(leftJsonEditorPanel);
        this.add(centralPanel);
        this.add(rightJsonEditorPanel);
        this.setBackground(Color.CYAN);
    }

    private void copyJsonContent(ContentDirection direction) {
        String jsonStringToCopy = null;
        if (direction.equals(ContentDirection.LEFT)) {
            jsonStringToCopy = rightJsonEditorPanel.getJsonText();
            if (!jsonStringToCopy.isEmpty()) {
                leftJsonEditorPanel.setJsonText(jsonStringToCopy);
            }
        }

        if (direction.equals(ContentDirection.RIGHT)) {
            jsonStringToCopy = leftJsonEditorPanel.getJsonText();
            if (!jsonStringToCopy.isEmpty()) {
                rightJsonEditorPanel.setJsonText(jsonStringToCopy);
            }
        }
    }

    private void transformJsonContent(ContentDirection direction) {
        System.err.println("Unimplemented method 'transformJsonContent'");
    }

    private void compareJsonContent() {
        System.err.println("Unimplemented method 'compareJsonContent'");
    }
}
