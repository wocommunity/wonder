package er.ajax.example2.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.ajax.example2.model.ExampleDataFactory;
import er.ajax.example2.model.Product;
import er.extensions.foundation.ERXStringUtilities;

public class LiveSearch extends AjaxWOWODCPage {
  private String _searchText;

  public Product _repetitionProduct;
  private NSArray<Product> _products;
  private NSArray<Product> _matchingProducts;

  public LiveSearch(WOContext context) {
    super(context);
  }
  
  @Override
  protected boolean useDefaultComponentCSS() {
    return true;
  }

  public void setSearchText(String searchText) {
    if (!ERXStringUtilities.stringEqualsString(_searchText, searchText)) {
      _searchText = searchText;
      _matchingProducts = null;
    }
  }

  public String searchText() {
    return _searchText;
  }

  public NSArray<Product> products() {
    if (_products == null) {
      _products = ExampleDataFactory.products(100);
    }
    return _products;
  }

  public NSArray<Product> matchingProducts() {
    if (_matchingProducts == null) {
      if (_searchText == null) {
        _matchingProducts = null;
      }
      else {
        _matchingProducts = Product.TITLE.containsAll(_searchText).or(Product.SUMMARY.containsAll(_searchText)).filtered(products());
      }
    }
    return _matchingProducts;
  }
}