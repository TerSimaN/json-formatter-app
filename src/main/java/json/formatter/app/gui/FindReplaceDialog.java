package json.formatter.app.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.*;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import json.formatter.app.constants.ImageIconConstants;

public class FindReplaceDialog extends JDialog {
    private ImageIconConstants iconConstants;
    private GridLayout gridLayout;
    private FlowLayout leadingFlowLayout;
    private Dimension iconBtnPreferredSize = new Dimension(30, 30);
    private Frame ownerFrame;

    private RSyntaxTextArea syntaxTextArea;
    private JTextField searchField;
    private JTextField replaceField;
    private JCheckBox regexCheckBox;
    private JCheckBox matchCaseCheckBox;
    private JCheckBox wholeWordCheckBox;
    
    FindReplaceDialog(Frame owner, RSyntaxTextArea textArea, ImageIconConstants imageIconConstants) {
        super(owner, false);
        this.ownerFrame = owner;
        this.syntaxTextArea = textArea;
        this.iconConstants = imageIconConstants;

        gridLayout = new GridLayout(2, 1);
        leadingFlowLayout = new FlowLayout(FlowLayout.LEADING, 5, 5);
        
        JPanel findReplacePanel = new JPanel(gridLayout);
        JPanel searchPanel = createSearchPanel();
        findReplacePanel.add(searchPanel);
        JPanel replacePanel = createReplacePanel();
        findReplacePanel.add(replacePanel);
        
        this.getContentPane().add(findReplacePanel);        
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(owner);

        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                searchField.requestFocusInWindow();
            }
        });
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(leadingFlowLayout);

        JLabel findLabel = new JLabel("       Find: ");
        panel.add(findLabel);
        searchField = new JTextField(20);
        panel.add(searchField);

        JButton prevButton = new JButton(iconConstants.arrowUpBoldIcon);
        prevButton.setPreferredSize(iconBtnPreferredSize);
        prevButton.setToolTipText("Previous Match");
        prevButton.setActionCommand("findPrev");
        prevButton.addActionListener(new FindReplaceListener());
        panel.add(prevButton);

        JButton nextButton = new JButton(iconConstants.arrowDownBoldIcon);
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

        JButton replaceAllButton = new JButton("Replace All");
        replaceAllButton.setActionCommand("replaceAll");
        replaceAllButton.addActionListener(new ReplaceAllListener());
        panel.add(replaceAllButton);

        return panel;
    }

    class FindReplaceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            boolean forward = command.equals("findNext") || command.equals("replace");

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
            if (command.equals("replace")) {
                if (replaceText.isEmpty()) {
                    return;
                }

                context.setReplaceWith(replaceText);
                found = SearchEngine.replace(syntaxTextArea, context).wasFound();
            } else {
                found = SearchEngine.find(syntaxTextArea, context).wasFound();
            }

            if (!found) {
                JOptionPane.showMessageDialog(ownerFrame, "Text not found");
            }
        }
    }

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

            boolean found = SearchEngine.replaceAll(syntaxTextArea, replaceContext).wasFound();
            if (!found) {
                JOptionPane.showMessageDialog(ownerFrame, "Text not found");
            }
        }
    }
}
