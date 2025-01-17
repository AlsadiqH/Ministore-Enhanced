package clients.cashier;

import catalogue.Basket;
import clients.TextFieldHint;
import middle.MiddleFactory;
import middle.OrderProcessing;
import middle.StockReadWriter;
import theme.ThemeObserver;
import theme.ThemeProvider;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

import static javax.swing.SwingUtilities.getRootPane;


/**
 * View of the model 
 */
public class CashierView implements Observer, ThemeObserver
{
  private static final int H = 300;       // Height of window pixels
  private static final int W = 400;       // Width  of window pixels
  
  private static final String CHECK  = "Check";
  private static final String BUY    = "Basket";
  private static final String BOUGHT = "Buy";

  private final JLabel      pageTitle  = new JLabel();
  private final JLabel      theAction  = new JLabel();
  private final JTextField theInput = new TextFieldHint("Product number");
  private final JTextField quantityInput = new TextFieldHint("Item Quantity");
  private final JTextArea   theOutput  = new JTextArea();
  private final JScrollPane theSP      = new JScrollPane();
  private final JButton     theBtCheck = new JButton( CHECK );
  private final JButton     theBtBuy   = new JButton( BUY );
  private final JButton     theBtBought= new JButton( BOUGHT );

  private final JButton theBtCancel = new JButton("Cancel");
  private final JButton theBtUndo = new JButton("Undo Last");

  private final ThemeProvider themeProvider = ThemeProvider.getInstance();
  private Container cp;
  private StockReadWriter theStock     = null;
  private OrderProcessing theOrder     = null;
  private CashierController cont       = null;
  
  /**
   * Construct the view
   * @param rpc   Window in which to construct
   * @param mf    Factor to deliver order and stock objects
   * @param x     x-coordinate of position of window on screen 
   * @param y     y-coordinate of position of window on screen  
   */
          
  public CashierView(  RootPaneContainer rpc,  MiddleFactory mf, int x, int y  )
  {
    try                                           // 
    {      
      theStock = mf.makeStockReadWriter();        // Database access
      theOrder = mf.makeOrderProcessing();        // Process order
    } catch ( Exception e )
    {
      System.out.println("Exception: " + e.getMessage() );
    }
    cp = rpc.getContentPane();   // Content Pane
    Container rootWindow = (Container) rpc;         // Root Window
    cp.setLayout(null);                             // No layout manager
    rootWindow.setSize( W, H );                     // Size of Window
    rootWindow.setLocation( x, y );

    Font f = new Font("Monospaced",Font.PLAIN,12);  // Font f is

    pageTitle.setBounds( 110, 0 , 270, 20 );       
    pageTitle.setText( "Thank You for Shopping at MiniStrore" );                        
    cp.add( pageTitle );  
    
    theBtCheck.setBounds( 16, 25+60*0, 80, 40 );    // Check Button
    theBtCheck.addActionListener(
            e -> cont.doCheck(theInput.getText(), quantityInput.getText()));
    cp.add( theBtCheck );                           //  Add to canvas

    theBtBuy.setBounds( 16, 25+60*1, 80, 40 );      // Buy button 
    theBtBuy.addActionListener(                     // Call back code
      e -> cont.doBuy() );
    cp.add( theBtBuy );                             //  Add to canvas

    theBtUndo.setBounds(16, 25+60*2, 80, 40);
    theBtUndo.addActionListener(e -> cont.undoLast());
    cp.add(theBtUndo);

    theBtBought.setBounds( 16, 25+60*3, 80, 40 );   // Bought Button
    theBtBought.addActionListener(                  // Call back code
      e -> cont.doBought() );
    cp.add( theBtBought );                          //  Add to canvas

    theAction.setBounds( 110, 25 , 270, 20 );       // Message area
    theAction.setText( "" );                        // Blank
    cp.add( theAction );                            //  Add to canvas

    theInput.setBounds(110, 50, 120, 40); // Input Area
    quantityInput.setBounds(260, 50, 120, 40);

    cp.add( theInput );
    cp.add( quantityInput );//  Add to canvas

    theSP.setBounds( 110, 100, 270, 160 );          // Scrolling pane
    theOutput.setText( "" );                        //  Blank
    theOutput.setFont( f );                         //  Uses font  
    cp.add( theSP );                                //  Add to canvas
    theSP.getViewport().add( theOutput );           //  In TextArea
    rootWindow.setVisible( true );                  // Make visible
    theInput.requestFocus();                        // Focus is here

    // Register as theme observer
    themeProvider.addObserver(this);

    // Apply current theme immediately
    themeProvider.applyTheme(cp);

    rootWindow.setVisible(true);
    theInput.requestFocus();
  }

  @Override
  public void onThemeChanged(boolean isDarkMode) {
    // Just apply the theme when it changes
    Container rootContainer = (Container) getRootPane(cp);
    if (rootContainer != null) {
      themeProvider.applyTheme(rootContainer);
    }
  }

  /**
   * The controller object, used so that an interaction can be passed to the controller
   * @param c   The controller
   */

  public void setController( CashierController c )
  {
    cont = c;
  }

  /**
   * Update the view
   * @param modelC   The observed model
   * @param arg      Specific args 
   */
  @Override
  public void update( Observable modelC, Object arg )
  {
    CashierModel model  = (CashierModel) modelC;
    String      message = (String) arg;
    theAction.setText( message );
    Basket basket = model.getBasket();
    if ( basket == null )
      theOutput.setText( "Customers order" );
    else
      theOutput.setText( basket.getDetails() );

    // Schedule focus requests for both text fields on the event dispatch thread
    SwingUtilities.invokeLater(() -> {
      theInput.requestFocus();
      quantityInput.requestFocus();
    });

    themeProvider.applyTheme((Container) theInput.getRootPane());
    themeProvider.applyTheme((Container) quantityInput.getRootPane());
  }
}