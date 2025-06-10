package com.example.cabinet.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class GuiUtils {

    // Preferred Look and Feel
    public static final String LOOK_AND_FEEL = "Nimbus";

    // Colors
    public static final Color COLOR_BACKGROUND = new Color(240, 245, 250); // Very light blue/gray
    public static final Color COLOR_PANEL_BACKGROUND = Color.WHITE;
    public static final Color COLOR_ACCENT = new Color(70, 130, 180); // Steel blue
    public static final Color COLOR_TEXT_PRIMARY = new Color(50, 50, 50); // Dark gray
    public static final Color COLOR_TEXT_SECONDARY = new Color(100, 100, 100); // Medium gray
    public static final Color COLOR_SUCCESS = new Color(60, 179, 113); // Medium sea green
    public static final Color COLOR_ERROR = new Color(220, 20, 60); // Crimson
    public static final Color COLOR_BUTTON_BACKGROUND = COLOR_ACCENT;
    public static final Color COLOR_BUTTON_TEXT = Color.WHITE;
    public static final Color COLOR_TABLE_HEADER_BACKGROUND = new Color(220, 225, 230);
    public static final Color COLOR_TABLE_GRID = new Color(200, 200, 200);
    public static final Color COLOR_BACKGROUND_LIGHT = new Color(245, 250, 255); // Even lighter blue/gray
    public static final Color COLOR_TEXT_NORMAL = COLOR_TEXT_PRIMARY; // Alias for primary text color

    // Fonts
    public static final Font FONT_PRIMARY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_TEXT_FIELD = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_NORMAL = FONT_PRIMARY; // Alias for primary font

    // Borders
    public static final Border BORDER_EMPTY_PADDING = BorderFactory.createEmptyBorder(10, 10, 10, 10);
    public static final Border BORDER_PANEL = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 205, 210)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
    );
    public static final Border BORDER_FIELD_FOCUS = BorderFactory.createLineBorder(COLOR_ACCENT, 1);
    public static final Border BORDER_FIELD_NORMAL = BorderFactory.createLineBorder(new Color(200, 200, 200), 1);

    /**
     * Applies the standard look and feel.
     */
    public static void applyNimbusLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (LOOK_AND_FEEL.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to the system L&F
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Styles a JButton with standard styling.
     * @param button The JButton to style.
     * @return The styled JButton.
     */
    public static JButton styleButton(JButton button) {
        button.setFont(FONT_BUTTON);
        button.setBackground(COLOR_BUTTON_BACKGROUND);
        button.setForeground(COLOR_BUTTON_TEXT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT.darker(), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(COLOR_ACCENT.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(COLOR_BUTTON_BACKGROUND);
            }
        });
        return button;
    }

    /**
     * Styles a JLabel for use as a title.
     * @param label The JLabel to style.
     */
    public static void styleTitleLabel(JLabel label) {
        label.setFont(FONT_TITLE);
        label.setForeground(COLOR_ACCENT);
    }

    /**
     * Styles a JLabel for use as a regular label.
     * @param label The JLabel to style.
     */
    public static void styleSubtitleLabel(JLabel label) {
        label.setFont(FONT_SUBTITLE);
        label.setForeground(COLOR_TEXT_PRIMARY);
    }

    /**
     * Styles a JLabel with regular or bold font.
     * @param label The JLabel to style.
     * @param isBold If true, use bold font.
     * @return The styled JLabel
     */
    public static JLabel styleLabel(JLabel label, boolean isBold) {
        label.setFont(isBold ? FONT_BOLD : FONT_LABEL);
        label.setForeground(COLOR_TEXT_PRIMARY);
        return label;
    }

    /**
     * Styles a JComboBox.
     * @param comboBox The JComboBox to style.
     * @return The styled JComboBox.
     */
    public static <E> JComboBox<E> styleComboBox(JComboBox<E> comboBox) {
        comboBox.setFont(FONT_TEXT_FIELD);
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BORDER_FIELD_NORMAL,
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        return comboBox;
    }

    /**
     * Styles a JTextArea.
     * @param textArea The JTextArea to style.
     * @param isEditable Sets if the text area is editable.
     * @return The styled JTextArea.
     */
    public static JTextArea styleTextArea(JTextArea textArea, boolean isEditable) {
        textArea.setFont(FONT_TEXT_FIELD);
        textArea.setEditable(isEditable);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BORDER_FIELD_NORMAL,
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        if (!isEditable) {
            textArea.setBackground(new Color(235, 235, 235)); // Slightly off-white for non-editable
        }
        return textArea;
    }

    /**
     * Styles a JList.
     * @param list The JList to style.
     * @return The styled JList.
     */
    public static <E> JList<E> styleJList(JList<E> list) {
        list.setFont(FONT_TEXT_FIELD);
        list.setSelectionBackground(COLOR_ACCENT);
        list.setSelectionForeground(Color.WHITE);
        list.setBackground(Color.WHITE);
        list.setBorder(BORDER_FIELD_NORMAL);
        return list;
    }

    /**
     * Styles a JSpinner.
     * @param spinner The JSpinner to style.
     */
    public static void styleSpinner(JSpinner spinner) {
        spinner.setFont(FONT_TEXT_FIELD);
        spinner.setBorder(BorderFactory.createCompoundBorder(
                BORDER_FIELD_NORMAL,
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        // Style the editor component of the spinner
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField textFieldEditor = ((JSpinner.DefaultEditor) editor).getTextField();
            textFieldEditor.setFont(FONT_TEXT_FIELD);
            textFieldEditor.setForeground(COLOR_TEXT_PRIMARY);
            textFieldEditor.setBackground(COLOR_PANEL_BACKGROUND);
            textFieldEditor.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }
        spinner.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                spinner.setBorder(BorderFactory.createCompoundBorder(
                        BORDER_FIELD_FOCUS,
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                spinner.setBorder(BorderFactory.createCompoundBorder(
                        BORDER_FIELD_NORMAL,
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        });
    }

    public static void styleRegularLabel(JLabel label) {
        label.setFont(FONT_LABEL);
        label.setForeground(COLOR_TEXT_PRIMARY);
    }

    /**
     * Styles a JTextField.
     * @param textField The JTextField to style.
     */
    public static void styleTextField(JTextField textField) {
        textField.setFont(FONT_TEXT_FIELD);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BORDER_FIELD_NORMAL,
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BORDER_FIELD_FOCUS,
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BORDER_FIELD_NORMAL,
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        });
    }

    /**
     * Styles a JTextArea.
     * @param textArea The JTextArea to style.
     */
    public static void styleTextArea(JTextArea textArea) {
        textArea.setFont(FONT_TEXT_FIELD);
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BORDER_FIELD_NORMAL,
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                textArea.setBorder(BorderFactory.createCompoundBorder(
                        BORDER_FIELD_FOCUS,
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                textArea.setBorder(BorderFactory.createCompoundBorder(
                        BORDER_FIELD_NORMAL,
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        });
    }

    /**
     * Styles a JTextField and sets its column count.
     * @param textField The JTextField to style.
     * @param columns The number of columns to set.
     */
    public static void styleTextField(JTextField textField, int columns) {
        styleTextField(textField); // Apply base styling
        textField.setColumns(columns);
    }

    /**
     * Styles a JTable, including its header and scroll pane.
     * @param table The JTable to style.
     * @param scrollPane The JScrollPane containing the table.
     */
    public static void styleTable(JTable table, JScrollPane scrollPane) {
        table.setFont(FONT_PRIMARY);
        table.setGridColor(COLOR_TABLE_GRID);
        table.setShowGrid(true);
        table.setRowHeight(25);
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(COLOR_TABLE_HEADER_BACKGROUND);
        table.getTableHeader().setForeground(COLOR_TEXT_PRIMARY);
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID));
        table.setFillsViewportHeight(true); // Ensures table background is painted for the entire viewport
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_TABLE_GRID));
        scrollPane.getViewport().setBackground(COLOR_PANEL_BACKGROUND);
    }

    /**
     * Creates a styled JPanel to be used as a main content panel.
     * @return A new JPanel with standard styling.
     */
    public static JPanel createMainPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_BACKGROUND);
        panel.setBorder(BORDER_EMPTY_PADDING);
        return panel;
    }

    /**
     * Creates a styled JPanel to be used as a sub-panel or card.
     * @return A new JPanel with card styling.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_PANEL_BACKGROUND);
        panel.setBorder(BORDER_PANEL);
        return panel;
    }
}
