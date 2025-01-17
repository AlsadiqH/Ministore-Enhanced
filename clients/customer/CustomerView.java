package clients.customer;

import clients.TextFieldHint;
import clients.Picture;
import middle.MiddleFactory;
import middle.StockReader;
import theme.ThemeObserver;
import theme.ThemeProvider;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Implements the Customer view.
 */

public class CustomerView implements Observer, ThemeObserver {
    class Name                              // Names of buttons
    {
        public static final String CHECK = "Check";
        public static final String CLEAR = "Clear";
    }

    private static final int H = 300;       // Height of window pixels
    private static final int W = 400;       // Width  of window pixels

    private final JLabel pageTitle = new JLabel();
    private final JLabel theAction = new JLabel();
    private final JTextField theInput = new TextFieldHint("Product number");
    private final JTextArea theOutput = new JTextArea();
    private final JScrollPane theSP = new JScrollPane();
    private final JButton theBtCheck = new JButton(Name.CHECK);
    private final JButton theBtClear = new JButton(Name.CLEAR);
    private final JButton darkModeBtn = new JButton("☀");
    private final ThemeProvider themeProvider = ThemeProvider.getInstance();

    private Picture thePicture = new Picture(80, 80);
    private StockReader theStock = null;
    private CustomerController cont = null;

    // Added new components
    private final JComboBox<String> categoryComboBox;

    /**
     * Construct the view
     *
     * @param rpc Window in which to construct
     * @param mf  Factor to deliver order and stock objects
     * @param x   x-cordinate of position of window on screen
     * @param y   y-cordinate of position of window on screen
     */

    public CustomerView(RootPaneContainer rpc, MiddleFactory mf, int x, int y) {
        try                                             //
        {
            theStock = mf.makeStockReader();             // Database Access
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        Container cp = rpc.getContentPane();    // Content Pane
        Container rootWindow = (Container) rpc;         // Root Window
        cp.setLayout(null);                             // No layout manager
        rootWindow.setSize(W, H);                     // Size of Window
        rootWindow.setLocation(x, y);

        Font f = new Font("Monospaced", Font.PLAIN, 12);  // Font f is

        pageTitle.setBounds(110, 0, 270, 20);
        pageTitle.setText("Search products");
        cp.add(pageTitle);

        theBtCheck.setBounds(16, 25 + 60 * 0, 80, 40);    // Check button
        theBtCheck.addActionListener(                   // Call back code
                e -> cont.doCheck(theInput.getText()));
        cp.add(theBtCheck);                           //  Add to canvas

        theBtClear.setBounds(16, 25 + 60 * 1, 80, 40);    // Clear button
        theBtClear.addActionListener(                   // Call back code
                e -> cont.doClear());
        cp.add(theBtClear);                           //  Add to canvas

        theAction.setBounds(110, 25, 270, 20);       // Message area
        theAction.setText(" ");                       // blank
        cp.add(theAction);                            //  Add to canvas

        theInput.setBounds(110, 50, 170, 40);         // Product no area
        cp.add(theInput);                             //  Add to canvas

        theSP.setBounds(110, 100, 270, 160);          // Scrolling pane
        theOutput.setText("");                        //  Blank
        theOutput.setFont(f);                         //  Uses font
        cp.add(theSP);                                //  Add to canvas
        theSP.getViewport().add(theOutput);           //  In TextArea

        thePicture.setBounds(16, 25 + 60 * 2, 80, 80);   // Picture area
        cp.add(thePicture);                           //  Add to canvas
        thePicture.clear();

        // Update dark mode button
        darkModeBtn.setBounds(W - 40, 5, 20, 20);
        darkModeBtn.setFont(new Font("Dialog", Font.PLAIN, 12));
        darkModeBtn.setPreferredSize(new Dimension(40, 40));
        darkModeBtn.setMargin(new Insets(2, 2, 2, 2));
        darkModeBtn.addActionListener(e -> themeProvider.toggleTheme());
        cp.add(darkModeBtn);

        String[] categories = {"All", "Electronics", "Clothing", "Caps", "Shoes"};
        categoryComboBox = new JComboBox<>(categories);
        categoryComboBox.setBounds(290, 50, 90, 40);

        // Add category selection listener
        categoryComboBox.addActionListener(e -> {
            if (cont != null) {
                String selectedCategory = (String) categoryComboBox.getSelectedItem();
                cont.doFilterByCategory(selectedCategory);
            }
        });
        cp.add(categoryComboBox);

        // Register as theme observer
        themeProvider.addObserver(this);

        // Apply initial theme
        themeProvider.applyTheme(cp);

        rootWindow.setVisible(true);                  // Make visible);
        theInput.requestFocus();                        // Focus is here
    }

    @Override
    public void onThemeChanged(boolean isDarkMode) {
        darkModeBtn.setText(isDarkMode ? "☾" : "☀");
        themeProvider.applyTheme((Container) darkModeBtn.getRootPane());
    }


    /**
     * The controller object, used so that an interaction can be passed to the controller
     *
     * @param c The controller
     */

    public void setController(CustomerController c) {
        cont = c;
    }

    /**
     * Update the view
     *
     * @param modelC The observed model
     * @param arg    Specific args
     */

    public void update(Observable modelC, Object arg) {
        CustomerModel model = (CustomerModel) modelC;
        String message = (String) arg;
        theAction.setText(message);
        ImageIcon image = model.getPicture();  // Image of product
        if (image == null) {
            thePicture.clear();                  // Clear picture
        } else {
            thePicture.set(image);             // Display picture
        }
        // Update the basket details or formatted product information
        theOutput.setText(model.getFormattedProductDisplay());
        theInput.requestFocus();               // Focus is here
    }
}
