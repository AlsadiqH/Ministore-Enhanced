package orders;

import catalogue.Basket;
import catalogue.Product;
import debug.DEBUG;
import middle.OrderException;
import middle.OrderProcessing;

import java.util.stream.Collectors;

import java.util.*;

/**
 * The order processing system.<BR>
 * Manages the progression of customer orders, 
 *  instances of a Basket as they are progressed through the system.
 * These stages are:
 * <BR><B>Waiting to be processed<BR>
 * Currently being packed<BR>
 * Waiting to be collected<BR></B>
 * @author  Mike Smith University of Brighton
 * @version 3.0
 */

public class Order implements OrderProcessing
{
  private enum State {Waiting, BeingPacked, ToBeCollected };
  /**
   * Wraps a Basket and it state into a folder
   */
  private class Folder
  {
    private State  stateIs;       // Order state
    private Basket basket;        // For this basket

    public Folder( Basket anOrder )
    {
      stateIs = State.Waiting;
      basket  = anOrder;
    }

    public State getState()                { return this.stateIs; }

    public Basket getBasket()              { return this.basket; }

    public void newState( State newState ) { stateIs = newState; }
  }

  // Active orders in the Catshop system
  private final ArrayList<Folder>  folders = new ArrayList<>();
  private static int theNextNumber = 1;          // Start at order 1

  /**
   * Used to generate debug information
   * @param  basket an instance of a basket
   * @return Description of contents
   */
  private String asString( Basket basket )
  {
    StringBuilder sb = new StringBuilder(1024);
    Formatter     fr = new Formatter(sb);
    fr.format( "#%d (", basket.getOrderNum() );
    for ( Product pr: basket )
    {
      fr.format( "%-15.15s: %3d ", pr.getDescription(), pr.getQuantity() );
    }
    fr.format( ")" );
    fr.close();
    return sb.toString();
  }

  /**
   * Generates a unique order number
   *   would be good to recycle numbers after 999
   * @return A unique order number
   */
  public synchronized int uniqueNumber()
          throws OrderException
  {
    return theNextNumber++;
  }

  /**
   * Add a new order to the order processing system
   * @param bought A new order that is to be processed
   */
  public synchronized void newOrder( Basket bought )
          throws OrderException
  {
    DEBUG.trace( "DEBUG: New order" );
    folders.add( new Folder( bought ) );
    for ( Folder bws : folders )
    {
      DEBUG.trace( "Order: " + asString( bws.getBasket() ) );
    }
  }

  /**
   * Returns an order to pack from the warehouse.
   * @return An order to pack or null if no order
   */
  public synchronized Basket getOrderToPack()
          throws OrderException
  {
    DEBUG.trace( "DEBUG: Get order to pack" );
    Basket foundWaiting = null;
    for ( Folder bws : folders )
    {
      if ( bws.getState() == State.Waiting )
      {
        foundWaiting = bws.getBasket();
        bws.newState( State.BeingPacked );
        break;
      }
    }
    return foundWaiting;
  }

  /**
   * Informs the order processing system that the order has been
   * packed and the products are now being delivered to the
   * collection desk
   * @param  orderNum The order that has been packed
   * @return true Order in system, false no such order
   */
  public synchronized boolean informOrderPacked( int orderNum )
          throws OrderException
  {
    DEBUG.trace( "DEBUG: Order packed [%d]", orderNum );
    for ( int i=0; i < folders.size(); i++)
    {
      if ( folders.get(i).getBasket().getOrderNum() == orderNum &&
              folders.get(i).getState()                == State.BeingPacked )
      {
        folders.get(i).newState( State.ToBeCollected );
        return true;
      }
    }
    return false;
  }

  /**
   * Informs the order processing system that the order has been
   * collected by the customer
   * @return true If order is in the system, otherwise false
   */
  public synchronized boolean informOrderCollected( int orderNum )
          throws OrderException
  {
    DEBUG.trace( "DEBUG: Order collected [%d]", orderNum );
    for ( int i=0; i < folders.size(); i++)
    {
      if ( folders.get(i).getBasket().getOrderNum() == orderNum &&
              folders.get(i).getState()                == State.ToBeCollected )
      {
        folders.remove(i);
        return true;
      }
    }
    return false;
  }

  /**
   * Returns information about all the orders (there order number) 
   * in the order processing system
   * This consists of a map with the following keys:
   *<PRE>
   * Key "Waiting"        a list of orders waiting to be processed
   * Key "BeingPacked"    a list of orders that are currently being packed
   * Key "ToBeCollected"  a list of orders that can now be collected
   * Associated with each key is a List&lt;Integer&gt; of order numbers.
   * Note: Each order number will be unique number.
   * </PRE>
   * @return a Map with the keys: "Waiting", "BeingPacked", "ToBeCollected"
   */
  public synchronized Map<String, List<Integer> > getOrderState()
          throws OrderException
  {
    //DEBUG.trace( "DEBUG: get state of order system" );
    Map < String, List<Integer> > res = new HashMap<>();

    res.put( "Waiting",       orderNums(State.Waiting) );
    res.put( "BeingPacked",   orderNums(State.BeingPacked) );
    res.put( "ToBeCollected", orderNums(State.ToBeCollected) );

    return res;
  }

  /**
   * Return the list of order numbers in selected state
   * @param inState The state to find order numbers in
   * @return A list of order numbers
   */
  private List<Integer> orderNumsOldWay( State inState )
  {
    List <Integer> res = new ArrayList<>();
    for ( Folder folder : folders )
    {
      if ( folder.getState() == inState )
      {
        res.add( folder.getBasket().getOrderNum() );
      }
    }
    return res;
  }

  /**
   * Return the list of order numbers in selected state
   * @param inState The state to find order numbers in
   * @return A list of order numbers
   */
  private List<Integer> orderNums( State inState )
  {
    return folders.stream()
            .filter( folder -> folder.getState() == inState )
            .map( folder -> folder.getBasket().getOrderNum() )
            .collect( Collectors.toList() );
  }

  public synchronized String generateOrderReport() throws OrderException
  {
    StringBuilder report = new StringBuilder();
    Formatter formatter = new Formatter(report);

    // Header
    formatter.format("ORDER PROCESSING SYSTEM REPORT%n");
    formatter.format("==============================%n%n");

    // Group orders by state
    Map<State, List<Folder>> ordersByState = folders.stream()
            .collect(Collectors.groupingBy(Folder::getState));

    // Orders waiting to be processed
    formatter.format("ORDERS WAITING TO BE PROCESSED:%n");
    formatter.format("--------------------------------%n");
    List<Folder> waitingOrders = ordersByState.getOrDefault(State.Waiting, new ArrayList<>());
    if (waitingOrders.isEmpty()) {
      formatter.format("No orders waiting%n");
    } else {
      waitingOrders.forEach(folder ->
              formatter.format("%s%n", asString(folder.getBasket())));
    }
    formatter.format("%n");

    // Orders being packed
    formatter.format("ORDERS CURRENTLY BEING PACKED:%n");
    formatter.format("-------------------------------%n");
    List<Folder> packingOrders = ordersByState.getOrDefault(State.BeingPacked, new ArrayList<>());
    if (packingOrders.isEmpty()) {
      formatter.format("No orders being packed%n");
    } else {
      packingOrders.forEach(folder ->
              formatter.format("%s%n", asString(folder.getBasket())));
    }
    formatter.format("%n");

    // Orders to be collected
    formatter.format("ORDERS WAITING TO BE COLLECTED:%n");
    formatter.format("--------------------------------%n");
    List<Folder> collectOrders = ordersByState.getOrDefault(State.ToBeCollected, new ArrayList<>());
    if (collectOrders.isEmpty()) {
      formatter.format("No orders waiting for collection%n");
    } else {
      collectOrders.forEach(folder ->
              formatter.format("%s%n", asString(folder.getBasket())));
    }

    // Summary statistics
    formatter.format("%nSUMMARY:%n");
    formatter.format("---------%n");
    formatter.format("Total orders in system: %d%n", folders.size());
    formatter.format("- Waiting to be processed: %d%n", waitingOrders.size());
    formatter.format("- Currently being packed: %d%n", packingOrders.size());
    formatter.format("- Waiting to be collected: %d%n", collectOrders.size());

    formatter.close();
    return report.toString();
  }
}