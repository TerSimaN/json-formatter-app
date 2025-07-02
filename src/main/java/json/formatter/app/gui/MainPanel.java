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
        centralPanel.setBackground(Color.MAGENTA);

        this.add(leftJsonEditorPanel);
        this.add(centralPanel);
        this.add(rightJsonEditorPanel);
        this.setBackground(Color.CYAN);
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
        copyRightToLeftBtn.addActionListener(e -> copyJsonContent(ContentDirection.LEFT));
        btnCopyPanel.add(copyRightToLeftBtn);
        
        JButton copyLeftToRightBtn = new JButton(ImageIconConstants.arrowRightBoldIcon);
        copyLeftToRightBtn.setToolTipText("Copy the contents of the left panel to the right panel");
        copyLeftToRightBtn.addActionListener(e -> copyJsonContent(ContentDirection.RIGHT));
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
        transformRightToLeftBtn.addActionListener(e -> transformJsonContent(ContentDirection.LEFT));
        btnTransformPanel.add(transformRightToLeftBtn);
        
        JButton transformLeftToRightBtn = new JButton(ImageIconConstants.arrowRightBoldIcon);
        transformLeftToRightBtn.setToolTipText("Transform the contents of the left panel into the right panel");
        transformLeftToRightBtn.addActionListener(e -> transformJsonContent(ContentDirection.RIGHT));
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
        btnDifferencesPanel.add(enableCompareBtnCheckBox, 0);

        differencesPanel.add(btnDifferencesPanel);

        return differencesPanel;
    }

    /**
     * Copies the contents of one editor to another based on the given direction
     * @param direction One of the following directions
     *          defined in <code>ContentDirection</code>:
     *          <code>LEFT</code>, <code>RIGHT</code>
     */
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

    /**
     * Transforms the contents of one editor to another based on the given direction
     * @param direction One of the following directions
     *          defined in <code>ContentDirection</code>:
     *          <code>LEFT</code>, <code>RIGHT</code>
     */
    private void transformJsonContent(ContentDirection direction) {
        System.err.println("Unimplemented method 'transformJsonContent'");
    }

    /**
     * Compares the contents of both editors
     */
    private void compareJsonContent() {
        System.err.println("Unimplemented method 'compareJsonContent'");
    }
}
