import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXLoremIpsumGenerator;

public class AjaxSelectionListExample extends com.webobjects.appserver.WOComponent {
	private NSMutableArray<Product> _products;
	private Product _itemProduct;
	private Product _selectedProduct;
	public boolean _ajax;

	public AjaxSelectionListExample(WOContext context) {
		super(context);
		_products = new NSMutableArray<Product>();
		for (int i = 0; i < 30; i++) {
			String title = ERXLoremIpsumGenerator.words(1, 5);
			String summary = ERXLoremIpsumGenerator.paragraph();
			Product product = new Product(title, summary);
			_products.addObject(product);
		}
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

	public static final class Product {
		private String _title;
		private String _summary;

		public Product(String title, String summary) {
			_title = title;
			_summary = summary;
		}

		public String title() {
			return _title;
		}

		public String summary() {
			return _summary;
		}

		public String partialSummary() {
			int length = Math.min(30, _summary.length());
			return _summary.substring(0, 30) + " ...";
		}

		@Override
		public String toString() {
			return "[Product: " + _title + "]";
		}
	}
}