import com.webobjects.appserver.WOContext;

import er.ajax.json.JSONComponent;

public class ExampleJSONComponent extends JSONComponent {
	private int _counter;

	public ExampleJSONComponent(WOContext context) {
		super(context);
	}

	public int next() {
		return _counter++;
	}
}
