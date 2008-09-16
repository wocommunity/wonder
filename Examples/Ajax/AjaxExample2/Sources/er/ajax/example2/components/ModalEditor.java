package er.ajax.example2.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.ajax.AjaxUtils;
import er.ajax.example2.model.ExampleDataFactory;
import er.ajax.example2.model.Product;

public class ModalEditor extends AjaxWOWODCPage {
  public NSArray<Product> _products;
  public Product _repetitionProduct;

  public ModalEditor(WOContext context) {
    super(context);
    _products = ExampleDataFactory.products(10);
  }

  @Override
  protected boolean useDefaultComponentCSS() {
    return true;
  }

  public WOActionResults save() {
    boolean hasValidationErrors = (_repetitionProduct.title() == null);
    if (!hasValidationErrors) {
      AjaxUtils.appendScript(context(), "AMC.close(); productsUpdate()");
    }
    return null;
  }
}