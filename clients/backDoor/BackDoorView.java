package clients.backDoor;

import clients.TextFieldHint;
import middle.MiddleFactory;
import middle.StockReadWriter;
import sounds.PlaySound;
import theme.ThemeObserver;
import theme.ThemeProvider;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

import static javax.swing.SwingUtilities.getRootPane;

/**
 * Implements the Customer view.
 */

public class BackDoorView implements Observer, ThemeObserver
{
  private static final String RESTOCK  = "Add";
  private static final String CLEAR    = "Clear";
  private static final String QUERY    = "Query";
  private static final String REPORTS  = "Reports";
 
  private static final int H = 300;       // Height of window pixels
  private static final int W = 400;       // Width  of window pixels

  private final JLabel      pageTitle  = new JLabel();
  private final JLabel      theAction  = new JLabel();
  private final JTextField  theInput   = new TextFieldHint("Product number");
  private final JTextField  theInputNo = new TextFieldHint("Quantity");
  private final JTextArea   theOutput  = new JTextArea();
  private final JScrollPane theSP      = new JScrollPane();
  private final JButton     theBtClear = new JButton( CLEAR );
  private final JButton     theBtRStock = new JButton( RESTOCK );
  private final JButton     theBtQuery = new JButton( QUERY );
  private final JButton     theBtReports = new JButton(REPORTS);

  private final ThemeProvider themeProvider = ThemeProvider.getInstance();
  private Container cp;
  private StockReadWriter theStock     = null;
  private BackDoorController cont= null;

  /**
   * Construct the view
   * @param rpc   Window in which to construct
   * @param mf    Factor to deliver order and stock objects
   * @param x     x-cordinate of position of window on screen 
   * @param y     y-cordinate of position of window on screen  
   */
  public BackDoorView(  RootPaneContainer rpc, MiddleFactory mf, int x, int y )
  {
    try                                             // 
    {      
      theStock = mf.makeStockReadWriter();          // Database access
    } catch ( Exception e )
    {
      System.out.println("Exception: " + e.getMessage() );
    }
    cp         = rpc.getContentPane();    // Content Pane
    Container rootWindow = (Container) rpc;         // Root Window
    cp.setLayout(null);                             // No layout manager
    rootWindow.setSize( W, H );                     // Size of Window
    rootWindow.setLocation( x, y );
    
    Font f = new Font("Monospaced",Font.PLAIN,12);  // Font f is

    pageTitle.setBounds( 110, 0 , 270, 20 );       
    pageTitle.setText( "Staff check and manage stock" );                        
    cp.add( pageTitle );
    
    theBtQuery.setBounds( 16, 25+60*0, 80, 40 );    // Buy button 
    theBtQuery.addActionListener(                   // Call back code
      e -> {
        PlaySound.playClickSound();
        cont.doQuery( theInput.getText());
      } );
    cp.add( theBtQuery );                           //  Add to canvas

    theBtRStock.setBounds( 16, 25+60*1, 80, 40 );   // Check Button
    theBtRStock.addActionListener(                  // Call back code
      e -> {
        PlaySound.playClickSound();
        cont.doRStock( theInput.getText(),theInputNo.getText());
      } );
    cp.add( theBtRStock );                          //  Add to canvas

    theBtClear.setBounds( 16, 25+60*2, 80, 40 );    // Buy button 
    theBtClear.addActionListener(                   // Call back code
      e -> {
        PlaySound.playClickSound();
        cont.doClear();
      });
    cp.add( theBtClear );                           //  Add to canvas

    theBtReports.setBounds(16, 25+60*3, 80, 40);
    theBtReports.addActionListener(e -> {
      PlaySound.playClickSound();
      cont.generateReport();
    } );  // Call new controller method
    cp.add(theBtReports);

    theAction.setBounds( 110, 25 , 270, 20 );       // Message area
    theAction.setText( "" );                        // Blank
    cp.add( theAction );                            //  Add to canvas

    theInput.setBounds( 110, 50, 120, 40 );         // Input Area
    cp.add( theInput );                             //  Add to canvas
    
    theInputNo.setBounds( 260, 50, 120, 40 );       // Input Area
    cp.add( theInputNo );                           //  Add to canvas

    theSP.setBounds( 110, 100, 270, 160 );          // Scrolling pane
    theOutput.setText( "" );                        //  Blank
    theOutput.setFont( f );                         //  Uses font  
    cp.add( theSP );                                //  Add to canvas
    theSP.getViewport().add( theOutput );           //  In TextArea

    // Register as theme observer
    themeProvider.addObserver(this);

    // Apply current theme immediately
    themeProvider.applyTheme(cp);

    rootWindow.setVisible( true );                  // Make visible
    theInput.requestFocus();                        // Focus is here
  }

  @Override
  public void onThemeChanged(boolean isDarkMode) {
    // Just apply the theme when it changes
    Container rootContainer = (Container) getRootPane(cp);
    if (rootContainer != null) {
      themeProvider.applyTheme(rootContainer);
    }
  }
  
  public void setController( BackDoorController c )
  {
    cont = c;
  }

  /**
   * Update the view, called by notifyObservers(theAction) in model,
   * @param modelC   The observed model
   * @param arg      Specific args 
   */
  @Override
  public void update( Observable modelC, Object arg )  
  {
    BackDoorModel model  = (BackDoorModel) modelC;
    String        message = (String) arg;
    if(message != null && message.contains("\n")) {
      theAction.setText( "Stock Status Report" );
    } else {
      theAction.setText( message );
    }

    // If the message is a report (contains multiple lines), display it in theOutput
    if (message != null && message.contains("\n")) {
      theOutput.setText(message);
      theOutput.setCaretPosition(0);  // Scroll to top
    } else {
      theOutput.setText(model.getBasket().getDetails());
    }

    // Schedule focus requests for both text fields on the event dispatch thread
    SwingUtilities.invokeLater(() -> {
      theInput.requestFocus();
      theInputNo.requestFocus();
    });

    themeProvider.applyTheme((Container) theInput.getRootPane());
    themeProvider.applyTheme((Container) theInputNo.getRootPane());
  }
}