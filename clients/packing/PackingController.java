package clients.packing;

import clients.packing.PackingModel;
import clients.packing.PackingView;
import debug.DEBUG;
import middle.MiddleFactory;
import middle.OrderProcessing;

/**
 * The Packing Controller
 */

public class PackingController
{
  private PackingModel model = null;
  private PackingView  view  = null;
  /**
   * Constructor
   * @param model The model
   * @param view  The view from which the interaction came
   */
  public PackingController( PackingModel model, PackingView view)
  {
    this.view  = view;
    this.model = model;
  }

  /**
   * Picked interaction from view
   */
  public void doPacked()
  {
    if (!model.isShowingReport()) {  // Only process packing if not showing report
      model.doPacked();
    }
  }

  public void doReport() {model.doReport(); }

  public void doClear() { model.clearReport(); }
}