package theme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ThemeProvider {
    private static ThemeProvider instance;
    private boolean isDarkMode = false;
    private final List<ThemeObserver> observers = new ArrayList<>();

    // Light theme colors
    private static final Color LIGHT_BACKGROUND = new Color(238, 238, 238);
    private static final Color LIGHT_TEXT = new Color(33, 33, 33);
    private static final Color LIGHT_HINT = Color.GRAY;
    private static final Color LIGHT = Color.WHITE;
    private static final Color LIGHT_BUTTON = new Color(218, 232, 245);

    // Dark theme colors
    private static final Color DARK_BACKGROUND = new Color(55, 55, 55);
    private static final Color DARK_TEXT = new Color(238, 238, 238);
    private static final Color DARK_HINT = Color.LIGHT_GRAY;
    private static final Color DARK_BUTTON = new Color(44, 54, 64);

    private ThemeProvider() {}

    public static ThemeProvider getInstance() {
        if (instance == null) {
            instance = new ThemeProvider();
        }
        return instance;
    }

    public void addObserver(ThemeObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ThemeObserver observer) {
        observers.remove(observer);
    }

    public void toggleTheme() {
        isDarkMode = !isDarkMode;
        notifyObservers();
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    private void notifyObservers() {
        for (ThemeObserver observer : observers) {
            observer.onThemeChanged(isDarkMode);
        }
    }

    public void applyTheme(Container container) {
        Color background = isDarkMode ? DARK_BACKGROUND : LIGHT_BACKGROUND;
        Color text = isDarkMode ? DARK_TEXT : LIGHT_TEXT;
        Color button = isDarkMode? DARK_BUTTON : LIGHT_BUTTON;
        Color textArea = isDarkMode? DARK_BACKGROUND : LIGHT;

        UIManager.put("TextField.background", textArea);
        UIManager.put("TextArea.background", textArea);
        UIManager.put("TextField.foreground", text);
        UIManager.put("TextArea.foreground", text);
        UIManager.put("Button.background", button);
        UIManager.put("Button.foreground", text);

        applyThemeToContainer(container, background, text, button, textArea);
    }

    private void applyThemeToContainer(Container container, Color background, Color text, Color button, Color textArea) {
        container.setBackground(background);

        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField) {
                JTextField field = (JTextField) comp;
                field.setOpaque(true);
                SwingUtilities.invokeLater(() -> {
                    field.setBackground(textArea);
                    field.setForeground(text);
                    field.setCaretColor(text);
                });
            } else if (comp instanceof JTextArea) {
                JTextArea area = (JTextArea) comp;
                area.setOpaque(true);
                SwingUtilities.invokeLater(() -> {
                    area.setBackground(textArea);
                    area.setForeground(text);
                    area.setCaretColor(text);
                });
            } else if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (btn.getText().equals("☀") || btn.getText().equals("☾")) {
                    btn.setContentAreaFilled(false);
                    btn.setBorderPainted(false);
                    SwingUtilities.invokeLater(() -> {
                        btn.setForeground(text);
                    });
                } else {
                    btn.setOpaque(true);
                    btn.setBorderPainted(true);
                    btn.setContentAreaFilled(true);
                    SwingUtilities.invokeLater(() -> {
                        btn.setBackground(button);
                        btn.setForeground(text);
                    });
                }
            } else if (comp instanceof JLabel) {
                comp.setForeground(text);

            } else if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                scrollPane.getViewport().setBackground(textArea);
                Component[] components = scrollPane.getViewport().getComponents();
                for (Component c : components) {
                    c.setBackground(textArea);
                    c.setForeground(text);
                }
            } else if (comp instanceof JComboBox) {
                JComboBox jComboBox = (JComboBox) comp;
                SwingUtilities.invokeLater(() -> {
                    jComboBox.setBackground(textArea);
                    jComboBox.setForeground(text);
                });
            }

            if (comp instanceof Container) {
                applyThemeToContainer((Container) comp, background, text, button, textArea);
            }
        }
    }

    public static Color getDarkText() {
        return DARK_TEXT;
    }

    public static Color getLightText() {
        return LIGHT_TEXT;
    }

    public static Color getDarkHint() {
        return DARK_HINT;
    }

    public static Color getLightHint() {
        return LIGHT_HINT;
    }

    public static Color getDarkBackground() {
        return DARK_BACKGROUND;
    }

    public static Color getLightBackground() {
        return LIGHT_BACKGROUND;
    }
}