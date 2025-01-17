package clients.cashier;

import catalogue.Basket;
import catalogue.Product;
import debug.DEBUG;
import middle.*;
import sounds.PlaySound;
import variableValidator.VariableValidator;

import javax.swing.*;
import java.util.Observable;

/**
 * Implements the Model of the cashier client
 */
public class CashierModel extends Observable {
    private enum State {process, checked}

    private State theState = State.process;   // Current state
    private Product theProduct = null;            // Current product
    private Basket theBasket = null;            // Bought items

    private String pn = "";                      // Product being processed

    private StockReadWriter theStock = null;
    private OrderProcessing theOrder = null;

    /**
     * Construct the model of the Cashier
     *
     * @param mf The factory to create the connection objects
     */

    public CashierModel(MiddleFactory mf) {
        try                                           //
        {
            theStock = mf.makeStockReadWriter();        // Database access
            theOrder = mf.makeOrderProcessing();        // Process order
        } catch (Exception e) {
            DEBUG.error("CashierModel.constructor\n%s", e.getMessage());
        }
        theState = State.process;                  // Current state
    }

    /**
     * Get the Basket of products
     *
     * @return basket
     */
    public Basket getBasket() {
        return theBasket;
    }

    /**
     * Check if the product is in Stock
     *
     * @param productNum The product number
     */
    public void doCheck(String productNum, String amountChosen)
    {
      PlaySound.playClickSound();
        String theAction = "";
        if (!VariableValidator.validIntForStock(amountChosen)) {
            theAction = "Invalid quantity!";
            PlaySound.playErrorSound();
            setChanged(); notifyObservers(theAction);
            return;
        }
        int amount = Integer.parseInt(amountChosen);
      theState  = State.process;                  // State process
      pn  = productNum.trim();                    // Product no.
      try
      {
        if (theStock.exists(pn))                // Stock Exists?
        {                                       // T
          Product pr = theStock.getDetails(pn);   //  Get details
          if (pr.getQuantity() >= amount)     //  In stock?
          {                                   //  T
            theAction =                     //   Display
                    String.format("%s : %7.2f (%2d) ", //
                            pr.getDescription(),    //    description
                            pr.getPrice(),          //    price
                            pr.getQuantity());      //    quantity
            theProduct = pr;                //   Remember prod.
            theProduct.setQuantity(amount); //    & quantity
            theState = State.checked;       //   OK await BUY
            PlaySound.playConfirmationSound(); // Success sound
          } else {                            //  F
            theAction =                     //   Not in Stock
                    pr.getDescription() + " only " + pr.getQuantity() + " in stock";
            PlaySound.playErrorSound();     // Error - no stock
          }
        } else {                                // F Stock exists
          theAction =                         //  Unknown
                  "Unknown product number " + pn;   //  product no.
          PlaySound.playErrorSound();         // Error - unknown product
        }
      } catch(StockException e)
      {
        DEBUG.error("%s\n%s",
                "CashierModel.doCheck", e.getMessage());
        theAction = e.getMessage();
        PlaySound.playErrorSound();             // Error - exception
      }
      setChanged(); notifyObservers(theAction);
    }

    /**
     * Buy the product
     */
    public void doBuy()
    {
      PlaySound.playClickSound();
      String theAction = "";
      int    amount  = 1;                         //  & quantity
      try
      {
        if (theState != State.checked)          // Not checked
        {
          theAction = "please check its availablity";
          PlaySound.playErrorSound();         // Error - not checked
        } else {
          boolean stockBought =               // Buy
                  theStock.buyStock(              //  however
                          theProduct.getProductNum(), //  may fail
                          theProduct.getQuantity());  //
          if (stockBought)                    // Stock bought
          {                                   // T
            makeBasketIfReq();              //  new Basket ?
            theBasket.add(theProduct);      //  Add to bought
            theAction = "Purchased " +      //    details
                    theProduct.getDescription(); //
            PlaySound.playPurchaseSound();  // Success - purchase sound
          } else {                            // F
            theAction = "!!! Not in stock"; //  Now no stock
            PlaySound.playErrorSound();     // Error - no stock
          }
        }
      } catch(StockException e)
      {
        DEBUG.error("%s\n%s",
                "CashierModel.doBuy", e.getMessage());
        theAction = e.getMessage();
        PlaySound.playErrorSound();             // Error - exception
      }
      theState = State.process;                   // All Done
      setChanged(); notifyObservers(theAction);
    }

    /**
     * Customer pays for the contents of the basket
     */
    public void doBought() {
        PlaySound.playClickSound();
        String theAction = "";
        try {
            if (theBasket != null && theBasket.size() >= 1) { // Items in basket
                theOrder.newOrder(theBasket); // Process the order
                theBasket = null; // Reset basket

                PlaySound.playPurchaseSound();
                // Display a success dialog to the user
                javax.swing.SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null,
                            "Your order has been placed successfully!",
                            "Order Confirmation",
                            JOptionPane.INFORMATION_MESSAGE);
                });

                theAction = "Start New Order"; // Indicate new order
            } else { // No items in basket
                theAction = "No items in the basket to process.";
            }
            theState = State.process; // Reset state to process
        } catch (OrderException e) {
            PlaySound.playErrorSound();
            DEBUG.error("%s\n%s", "CashierModel.doBought", e.getMessage());
            theAction = e.getMessage();
        }

        theBasket = null; // Ensure the basket is cleared
        setChanged();
        notifyObservers(theAction); // Notify observers
    }

    /**
     * ask for update of view callled at start of day
     * or after system reset
     */
    public void askForUpdate() {
        PlaySound.playClickSound();
        setChanged();
        notifyObservers("Welcome");
    }

    /**
     * make a Basket when required
     */
    private void makeBasketIfReq() {
        if (theBasket == null) {
            try {
                int uon = theOrder.uniqueNumber();     // Unique order num.
                theBasket = makeBasket();                //  basket list
                theBasket.setOrderNum(uon);            // Add an order number
            } catch (OrderException e) {
                DEBUG.error("Comms failure\n" +
                        "CashierModel.makeBasket()\n%s", e.getMessage());
            }
        }
    }

    /**
     * return an instance of a new Basket
     *
     * @return an instance of a new Basket
     */
    protected Basket makeBasket() {
        return new Basket();
    }

    public void undoLast() {
        if (theBasket != null && !theBasket.isEmpty()) {
            try{
                Product lastProduct = theBasket.removeLastItem();
                theStock.addStock(lastProduct.getProductNum(), lastProduct.getQuantity());
                setChanged();
                notifyObservers("Last item removed");
            } catch (StockException e) {
                DEBUG.error("%s\n%s",
                        "CashierModel.undoLast", e.getMessage());
                setChanged();
                notifyObservers(e.getMessage());
            }
        }
    }

    public void doCancel() {
        if (theBasket != null) {
            try {
                for (Product p : theBasket.getProducts()) {
                    theStock.addStock(p.getProductNum(), p.getQuantity());
                }
                theBasket = null;
                setChanged();
                notifyObservers("Order cancelled");
            } catch (Exception e) {
                DEBUG.error("%s\n%s",
                        "CashierModel.doCancel", e.getMessage());
                setChanged();
                notifyObservers(e.getMessage());
            }
        }
    }
}