package clients;

import theme.ThemeProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class TextFieldHint extends JTextField {
    private final String placeholder;
    private boolean showingPlaceholder;

    public TextFieldHint(String placeholder) {
        this.placeholder = placeholder;
        this.showingPlaceholder = true;

        // Initialize the placeholder and apply the theme
        initPlaceholder();
        applyTheme();

        // Add focus listener for placeholder functionality
        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (showingPlaceholder) {
                    setText("");
                    showingPlaceholder = false;
                }
                // Update text color based on theme
                setForeground(getForegroundTextColor());
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    initPlaceholder();
                } else {
                    // Keep the appropriate text color for non-empty field
                    setForeground(getForegroundTextColor());
                }
            }
        });
        // Register as an observer for theme changes
        ThemeProvider.getInstance().addObserver(this::onThemeChanged);
    }

    @Override
    public String getText() {
        return showingPlaceholder ? "" : super.getText();
    }

    /**
     * Initializes the placeholder state.
     */
    private void initPlaceholder() {
        showingPlaceholder = true;
        setForeground(getHintColor());
        setText(placeholder);
    }

    /**
     * Applies the current theme to the text field.
     */
    private void applyTheme() {
        ThemeProvider themeProvider = ThemeProvider.getInstance();
        setBackground(themeProvider.isDarkMode()
                ? ThemeProvider.getDarkBackground()
                : ThemeProvider.getLightBackground());
        if (!showingPlaceholder) {
            setForeground(getForegroundTextColor());
        } else {
            setForeground(getHintColor());
        }
        setCaretColor(getForegroundTextColor());
    }

    /**
     * Handles theme changes by reapplying the theme.
     *
     * @param isDarkMode Indicates whether dark mode is active.
     */
    private void onThemeChanged(boolean isDarkMode) {
        applyTheme();
    }

    /**
     * Gets the appropriate foreground text color based on the theme.
     *
     * @return The text color for the current theme.
     */
    private Color getForegroundTextColor() {
        ThemeProvider themeProvider = ThemeProvider.getInstance();
        return themeProvider.isDarkMode() ? ThemeProvider.getDarkText() : ThemeProvider.getLightText();
    }

    /**
     * Gets the appropriate placeholder color based on the theme.
     *
     * @return The placeholder color for the current theme.
     */
    private Color getHintColor() {
        ThemeProvider themeProvider = ThemeProvider.getInstance();
        return themeProvider.isDarkMode() ? ThemeProvider.getDarkHint() : ThemeProvider.getLightHint();
    }
}
