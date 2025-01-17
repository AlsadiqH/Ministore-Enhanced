package clients.customer;

import catalogue.Basket;
import catalogue.Product;
import debug.DEBUG;
import middle.MiddleFactory;
import middle.OrderProcessing;
import middle.StockException;
import middle.StockReader;
import sounds.PlaySound;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Implements the Model of the customer client
 */
public class CustomerModel extends Observable
{
  private Product     theProduct = null;          // Current product
  private Basket      theBasket  = null;          // Bought items

  private String      pn = "";                    // Product being processed

  private StockReader     theStock     = null;
  private OrderProcessing theOrder     = null;
  private ImageIcon       thePic       = null;

  String currentCategory = "All";
  private List<Product> filteredProducts = new ArrayList<>();

  /*
   * Construct the model of the Customer
   * @param mf The factory to create the connection objects
   */
  public CustomerModel(MiddleFactory mf)
  {
    try                                          // 
    {  
      theStock = mf.makeStockReader();           // Database access
    } catch ( Exception e )
    {
      DEBUG.error("CustomerModel.constructor\n" +
                  "Database not created?\n%s\n", e.getMessage() );
    }
    theBasket = makeBasket();                    // Initial Basket
  }
  
  /**
   * return the Basket of products
   * @return the basket of products
   */
  public Basket getBasket()
  {
    return theBasket;
  }

  public void filterByCategory(String category) {
    try {
      currentCategory = category;
      filteredProducts = theStock.getProductsByCategory(category);

      String theAction = "Showing " + category + " products";
      if (filteredProducts.isEmpty()) {
        theAction = "No products found in " + category;
        PlaySound.playErrorSound();
      } else {
        PlaySound.playClickSound();
      }

      setChanged();
      notifyObservers(theAction);
    } catch (StockException e) {
      DEBUG.error("CustomerModel.filterByCategory()\n%s", e.getMessage());
      PlaySound.playErrorSound();
    }
  }

  public String getFormattedProductDisplay() {
    if (filteredProducts == null || filteredProducts.isEmpty()) {
      return getBasket().getDetails(); // Assuming `getBasket` gives an appropriate fallback message
    }

    StringBuilder display = new StringBuilder();
    display.append("Available Products:\n\n");
    for (Product product : filteredProducts) {
      display.append(String.format("%s: %s - £%.2f (%d in stock)\n",
              product.getProductNum(),
              product.getDescription(),
              product.getPrice(),
              product.getQuantity()));
    }
    return display.toString();
  }

  /**
   * Check if the product is in Stock
   * @param productNum The product number
   */
  public void doCheck(String productNum)
  {
    PlaySound.playClickSound();
    theBasket.clear();                          // Clear s. list
    String theAction = "";
    doClear();
    pn  = productNum.trim();                    // Product no.
    int    amount  = 1;                         //  & quantity
    try
    {
      if (theStock.exists(pn))                // Stock Exists?
      {                                       // T
        Product pr = theStock.getDetails(pn); //  Product
        if (pr.getQuantity() >= amount)     //  In stock?
        {
          theAction =                     //   Display
                  String.format("%s : %7.2f (%2d) ", //
                          pr.getDescription(),    //    description
                          pr.getPrice(),          //    price
                          pr.getQuantity());      //    quantity
          pr.setQuantity(amount);         //   Require 1
          theBasket.add(pr);              //   Add to basket
          thePic = theStock.getImage(pn); //    product
          PlaySound.playConfirmationSound(); // Success sound
        } else {                            //  F
          theAction =                     //   Inform
                  pr.getDescription() +       //    product not
                          " not in stock";            //    in stock
          PlaySound.playErrorSound();     // Error sound for no stock
        }
      } else {                               // F
        theAction =                        //  Inform Unknown
                "Unknown product number " + pn; //  product number
        PlaySound.playErrorSound();        // Error sound for unknown product
      }
    } catch(StockException e)
    {
      DEBUG.error("CustomerClient.doCheck()\n%s",
              e.getMessage());
      PlaySound.playErrorSound();            // Error sound for exception
    }
    setChanged(); notifyObservers(theAction);
  }

  /**
   * Clear the products from the basket
   */
  public void doClear()
  {
    String theAction = "";
    theBasket.clear();                        // Clear s. list
    theAction = "Enter Product Number";       // Set display
    thePic = null;                            // No picture
    filteredProducts.clear();
    setChanged(); notifyObservers(theAction);
  }
  
  /**
   * Return a picture of the product
   * @return An instance of an ImageIcon
   */ 
  public ImageIcon getPicture()
  {
    return thePic;
  }
  
  /**
   * ask for update of view callled at start
   */
  private void askForUpdate()
  {
    setChanged(); notifyObservers("START only"); // Notify
  }

  /**
   * Make a new Basket
   * @return an instance of a new Basket
   */
  protected Basket makeBasket()
  {
    return new Basket();
  }
}

