import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class InPlaceEditorExample extends WOComponent {
	private String _multilineValue;
	public String _exampleValue;
	public String _strippedValue;
	public int _numericValue;

	public InPlaceEditorExample(WOContext context) {
		super(context);
		_exampleValue = "Test Value";
		_multilineValue = "Multi\nLine\nValue";
		_strippedValue = "Stripped Value";
		_numericValue = 5;
	}

	public void setMultilineValue(String multilineValue) {
		_multilineValue = multilineValue;
	}

	public String multilineValue() {
		return (_multilineValue == null) ? null : _multilineValue.replaceAll("\n\n", "<p>").replaceAll("\n", "<br>");
	}

	public WOActionResults someAction() {
		System.out.println("InPlaceEditorExample.someAction: Some action was performed ...");
		return null;
	}
	
	
	public void saveOnChange() {
		System.out.println("InPlaceEditorExample.saveOnChange: save on change was performed for value " + _exampleValue);
		// You could call ec.saveChanges() here if you wanted to...
	}
}
