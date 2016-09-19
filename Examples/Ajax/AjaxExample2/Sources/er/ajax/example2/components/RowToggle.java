package er.ajax.example2.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;

import er.ajax.example2.model.ExampleDataFactory;
import er.ajax.example2.model.Product;

public class RowToggle extends AjaxWOWODCPage {
  private NSArray<Product> _products;
  private NSMutableSet<Product> _selectedProducts;
  public Product _repetitionProduct;

  public RowToggle(WOContext context) {
    super(context);
    _selectedProducts = new NSMutableSet<>();
  }
  
  @Override
  protected boolean useDefaultComponentCSS() {
    return true;
  }
  
  public NSMutableSet<Product> selectedProducts() {
    return _selectedProducts;
  }

  public void setProductSelected(boolean selected) {
    if (selected) {
      _selectedProducts.addObject(_repetitionProduct);
    }
    else {
      _selectedProducts.removeObject(_repetitionProduct);
    }
  }

  public boolean isProductSelected() {
    return _selectedProducts.containsObject(_repetitionProduct);
  }

  public NSArray<Product> products() {
    if (_products == null) {
      _products = ExampleDataFactory.products(10);
    }
    return _products;
  }
  
  public WOActionResults productSelected() {
    // You would do something here
    return null;
  }
}