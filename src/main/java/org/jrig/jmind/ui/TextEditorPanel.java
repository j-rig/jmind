/**
 * jmind
 * 
 * @author j-rig
 * mostly hand crafted code 2026
 */

package org.jrig.jmind.ui;

import javax.swing.*;
import java.awt.*;

public class TextEditorPanel extends JPanel {
    private JTextArea textArea;
    private JLabel statusLabel;

    public TextEditorPanel() {
        setLayout(new BorderLayout());

        add(createToolbar(), BorderLayout.NORTH);
        add(createEditorPanel(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        // Font size
        JLabel fontLabel = new JLabel(" Font Size: ");
        JComboBox<Integer> fontSizeCombo = new JComboBox<>(new Integer[] { 10, 12, 14, 16, 18, 20, 24, 28, 32 });
        fontSizeCombo.setSelectedItem(14);
        fontSizeCombo.addActionListener(e -> {
            Integer size = (Integer) fontSizeCombo.getSelectedItem();
            setFontSize(size);
        });
        toolbar.add(fontLabel);
        toolbar.add(fontSizeCombo);

        toolbar.addSeparator();

        // Font family
        JLabel familyLabel = new JLabel(" Font: ");
        String[] fonts = { "Monospaced", "Arial", "Times New Roman", "Courier New", "Verdana" };
        JComboBox<String> fontFamilyCombo = new JComboBox<>(fonts);
        fontFamilyCombo.setSelectedItem("Monospaced");
        fontFamilyCombo.addActionListener(e -> {
            String family = (String) fontFamilyCombo.getSelectedItem();
            setFontFamily(family);
        });
        toolbar.add(familyLabel);
        toolbar.add(fontFamilyCombo);

        toolbar.addSeparator();

        // Word wrap toggle
        JCheckBox wordWrapCheck = new JCheckBox("Word Wrap", true);
        wordWrapCheck.addActionListener(e -> {
            textArea.setLineWrap(wordWrapCheck.isSelected());
            textArea.setWrapStyleWord(wordWrapCheck.isSelected());
        });
        toolbar.add(wordWrapCheck);

        toolbar.addSeparator();

        // Character count
        JLabel charCountLabel = new JLabel(" Characters: 0 ");
        Timer timer = new Timer(500, e -> {
            int count = textArea.getText().length();
            int lines = textArea.getLineCount();
            charCountLabel.setText(String.format(" Characters: %d | Lines: %d ", count, lines));
        });
        timer.start();
        toolbar.add(charCountLabel);

        return toolbar;
    }

    private JScrollPane createEditorPanel() {
        textArea = new JTextArea();
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setTabSize(4);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        return scrollPane;
    }

    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Ready");
        panel.add(statusLabel);
        return panel;
    }

    // Font manipulation methods
    private void setFontSize(int size) {
        Font currentFont = textArea.getFont();
        textArea.setFont(new Font(currentFont.getFamily(), currentFont.getStyle(), size));
    }

    private void setFontFamily(String family) {
        Font currentFont = textArea.getFont();
        textArea.setFont(new Font(family, currentFont.getStyle(), currentFont.getSize()));
    }

    // Public methods for content management
    public void setText(String text) {
        textArea.setText(text);
        textArea.setCaretPosition(0);
    }

    public String getText() {
        return textArea.getText();
    }

    public void clear() {
        textArea.setText("");
    }

    public void setStatusText(String text) {
        statusLabel.setText(text);
    }

    public void requestEditorFocus() {
        textArea.requestFocusInWindow();
    }

    public void appendText(String text) {
        textArea.append(text);
    }

    public void insertText(String text) {
        int pos = textArea.getCaretPosition();
        try {
            textArea.insert(text, pos);
        } catch (Exception e) {
            textArea.append(text);
        }
    }

    public String getSelectedText() {
        return textArea.getSelectedText();
    }

    public void replaceSelection(String text) {
        textArea.replaceSelection(text);
    }
}