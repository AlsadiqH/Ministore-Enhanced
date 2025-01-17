package clients.backDoor;

import catalogue.Basket;
import catalogue.BetterBasket;
import catalogue.Product;
import debug.DEBUG;
import middle.MiddleFactory;
import middle.StockException;
import middle.StockMonitorService;
import middle.StockReadWriter;

import java.util.Observable;

/**
 * Implements the Model of the back door client
 */
public class BackDoorModel extends Observable
{
    private Basket      theBasket  = null;            // Bought items
    private String      pn = "";                      // Product being processed

    private StockReadWriter theStock     = null;

    private StockMonitorService stockMonitor = null;

    /*
     * Construct the model of the back door client
     * @param mf The factory to create the connection objects
     */

    public BackDoorModel(MiddleFactory mf)
    {
        try
        {
            theStock = mf.makeStockReadWriter();        // Database access
            stockMonitor = new StockMonitorService(theStock);
        } catch ( Exception e )
        {
            DEBUG.error("CustomerModel.constructor\n%s", e.getMessage() );
        }

        theBasket = makeBasket();                     // Initial Basket
    }

    /**
     * Get the Basket of products
     * @return basket
     */
    public Basket getBasket()
    {
        return theBasket;
    }

    /**
     * Check The current stock level
     * @param productNum The product number
     */
    public void doCheck(String productNum )
    {
        pn  = productNum.trim();                    // Product no.
        String status = stockMonitor.checkProduct(pn);
        setChanged();
        notifyObservers(status);
    }

    /**
     * Query
     * @param productNum The product number of the item
     */
    public void doQuery(String productNum )
    {
        String theAction = "";
        pn  = productNum.trim();                    // Product no.
        try
        {                 //  & quantity
            if ( theStock.exists( pn ) )              // Stock Exists?
            {                                         // T
                Product pr = theStock.getDetails( pn ); //  Product
                theAction =                             //   Display
                        String.format( "%s : %7.2f (%2d) ",   //
                                pr.getDescription(),                  //    description
                                pr.getPrice(),                        //    price
                                pr.getQuantity() );                   //    quantity
            } else {                                  //  F
                theAction =                             //   Inform
                        "Unknown product number " + pn;       //  product number
            }
        } catch( StockException e )
        {
            theAction = e.getMessage();
        }
        setChanged(); notifyObservers(theAction);
    }

    /**
     * Re stock
     * @param productNum The product number of the item
     * @param quantity How many to be added
     */
    public void doRStock(String productNum, String quantity) {
        String theAction = "";
        theBasket = makeBasket();
        pn = productNum.trim();

        if (pn.isEmpty()) {
            theAction = "Product number can't be empty.";
            setChanged();
            notifyObservers(theAction);
            return;
        }

        // Validate quantity input
        int amount = 0;
        if (quantity == null || quantity.trim().isEmpty()) {
            theAction = "Quantity is required.";
            setChanged();
            notifyObservers(theAction);
            return;
        }

        try {
            String aQuantity = quantity.trim();
            amount = Integer.parseInt(aQuantity);   // Convert to integer

            if (amount <= 0) {
                theAction = "Quantity should be greater than 0.";
                setChanged();
                notifyObservers(theAction);
                return;
            }

            if (theStock.exists(pn)) {              // Stock Exists?
                theStock.addStock(pn, amount);       // Re-stock the product
                Product pr = theStock.getDetails(pn); // Get product details
                theBasket.add(pr);                   // Add to basket

                // Record the restock only once and only if stockMonitor is initialized
                if (stockMonitor != null) {
                    stockMonitor.recordRestock(pn, amount);
                }

                theAction = String.format("Restocked %d - %s.", amount, pr.getDescription());
            } else {
                theAction = "invalid product number";
            }
        } catch (NumberFormatException e) {
            theAction = "Invalid quantity entered.";
        } catch (StockException e) {
            theAction = e.getMessage();
        }

        setChanged();
        notifyObservers(theAction);
    }

    /**
     * Clear the product()
     */
    public void doClear()
    {
        String theAction = "";
        theBasket.clear();                        // Clear s. list
        theAction = "Enter Product Number";       // Set display
        setChanged(); notifyObservers(theAction);  // inform the observer view that model changed
    }

    public void generateReport() {
        String report = stockMonitor.generateReport();
        setChanged();
        notifyObservers(report);
    }

    /**
     * return an instance of a Basket
     * @return a new instance of a Basket
     */
    protected Basket makeBasket()
    {
        return new Basket();
    }
}