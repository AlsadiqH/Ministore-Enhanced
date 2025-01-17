package clients.packing;

import catalogue.Basket;
import middle.MiddleFactory;
import middle.OrderProcessing;
import sounds.PlaySound;
import theme.ThemeObserver;
import theme.ThemeProvider;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

import static javax.swing.SwingUtilities.getRootPane;

/**
 * Implements the Packing view.
 */

public class PackingView implements Observer, ThemeObserver {
    private static final String PACKED = "Pack";
    private static final String REPORTS = "Reports";
    private static final String CLEAR_TEXT_AREA = "Clear";

    private static final int H = 300;       // Height of window pixels
    private static final int W = 400;       // Width  of window pixels

    private final JLabel pageTitle = new JLabel();
    private final JLabel theAction = new JLabel();
    private final JTextArea theOutput = new JTextArea();
    private final JScrollPane theSP = new JScrollPane();
    private final JButton theBtPack = new JButton(PACKED);
    private final JButton     theBtReport = new JButton( REPORTS );
    private final JButton     theBtClear = new JButton( CLEAR_TEXT_AREA );

    private OrderProcessing theOrder = null;

    private final ThemeProvider themeProvider = ThemeProvider.getInstance();
    private Container cp;

    private PackingController cont = null;

    /**
     * Construct the view
     *
     * @param rpc Window in which to construct
     * @param mf  Factor to deliver order and stock objects
     * @param x   x-cordinate of position of window on screen
     * @param y   y-cordinate of position of window on screen
     */
    public PackingView(RootPaneContainer rpc, MiddleFactory mf, int x, int y) {
        try                                           //
        {
            theOrder = mf.makeOrderProcessing();        // Process order
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        cp = rpc.getContentPane();    // Content Pane
        Container rootWindow = (Container) rpc;         // Root Window
        cp.setLayout(null);                             // No layout manager
        rootWindow.setSize(W, H);                     // Size of Window
        rootWindow.setLocation(x, y);

        Font f = new Font("Monospaced", Font.PLAIN, 12);  // Font f is

        pageTitle.setBounds(110, 0, 270, 20);
        pageTitle.setText("Packing Bought Order");
        cp.add(pageTitle);

        theBtPack.setBounds(16, 25 + 60 * 0, 80, 40);   // Check Button
        theBtPack.addActionListener(                   // Call back code
                e -> {
                    PlaySound.playClickSound();
                    cont.doPacked();
                }
        );
        cp.add(theBtPack);                          //  Add to canvas

        theBtReport.setBounds( 16, 25+60*1, 80, 40 );   // Position below Pack button
        theBtReport.addActionListener(                   // Call back code
                e -> cont.doReport() );
        cp.add( theBtReport );

        theBtClear.setBounds(16, 25+60*2, 80, 40);
        theBtClear.addActionListener(
                e -> cont.doClear()
        );
        cp.add( theBtClear );

        theAction.setBounds(110, 25, 270, 20);       // Message area
        theAction.setText("");                        // Blank
        cp.add(theAction);                            //  Add to canvas

        theSP.setBounds(110, 55, 270, 205);           // Scrolling pane
        theOutput.setText("");                        //  Blank
        theOutput.setFont(f);                         //  Uses font
        cp.add(theSP);                                //  Add to canvas

        // Register as theme observer
        themeProvider.addObserver(this);

        // Apply current theme immediately
        themeProvider.applyTheme(cp);

        theSP.getViewport().add(theOutput);           //  In TextArea
        rootWindow.setVisible(true);                  // Make visible
    }

    @Override
    public void onThemeChanged(boolean isDarkMode) {
        // Just apply the theme when it changes
        Container rootContainer = (Container) getRootPane(cp);
        if (rootContainer != null) {
            themeProvider.applyTheme(rootContainer);
        }
    }

    public void setController(PackingController c) {
        cont = c;
    }

    /**
     * Update the view
     *
     * @param modelC The observed model
     * @param arg    Specific args
     */
    @Override
    public void update(Observable modelC, Object arg) {
        PackingModel model = (PackingModel) modelC;
        String message = (String) arg;
        theAction.setText(message);

        if(message != null && message.contains("\n")) {
            theAction.setText( "Orders Report" );
        } else {
            theAction.setText( message );
        }

        if (model.isShowingReport()) {
            theOutput.setText(model.getCurrentReport());
        } else {
            Basket basket = model.getBasket();
            if (basket != null) {
                theOutput.setText(basket.getDetails());
            } else {
                theOutput.setText("");
            }
        }
    }

}

