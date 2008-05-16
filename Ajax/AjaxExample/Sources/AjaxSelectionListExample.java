import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.example.ExampleDataFactory;
import er.ajax.example.Product;

public class AjaxSelectionListExample extends com.webobjects.appserver.WOComponent {
	private NSMutableArray<Product> _products;
	private Product _itemProduct;
	private Product _selectedProduct;
	public boolean _ajax;

	public AjaxSelectionListExample(WOContext context) {
		super(context);
		_products = ExampleDataFactory.products(30);
	}

	public NSArray<Product> products() {
		return _products;
	}

	public void setItemProduct(Product itemProduct) {
		_itemProduct = itemProduct;
	}

	public Product itemProduct() {
		return _itemProduct;
	}

	public void setSelectedProduct(Product selectedProduct) {
		_selectedProduct = selectedProduct;
	}

	public Product selectedProduct() {
		return _selectedProduct;
	}

	public WOActionResults selectProduct() {
		return null;
	}

	public WOActionResults deleteProduct() {
		int index = _products.indexOfObject(_selectedProduct);
		_products.removeObjectAtIndex(index);
		if (_products.count() > index) {
			_selectedProduct = _products.objectAtIndex(index);
		}
		else if (index > 0) {
			_selectedProduct = _products.objectAtIndex(index - 1);
		}
		else {
			_selectedProduct = null;
		}
		return null;
	}

	public WOActionResults toggleAjax() {
		_ajax = !_ajax;
		return null;
	}
}