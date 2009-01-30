package er.uber.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSMutableArray;

public class ComponentTools extends UberComponent {
  public NSMutableArray<String> _values;
  public String _value;
  public String _selectedValue;
  public boolean _brokenOnPurpose;

  public ComponentTools(WOContext context) {
    super(context);
    _values = new NSMutableArray<String>();
    resetValues();
  }

  protected void resetValues() {
    _values.removeAllObjects();
    for (int i = 0; i < 5; i++) {
      _values.addObject("Item #" + String.valueOf(i));
    }
    _brokenOnPurpose = false;
  }

  protected void breakValues() {
    _values.removeAllObjects();
    for (int i = 0; i < 5; i++) {
      _values.addObject("Broken on Purpose #" + System.currentTimeMillis());
    }
    _brokenOnPurpose = true;
  }

  @Override
  public WOActionResults invokeAction(WORequest request, WOContext context) {
    if (!_brokenOnPurpose) {
      breakValues();
    }
    return super.invokeAction(request, context);
  }

  public WOActionResults selectValue() {
    _selectedValue = _value;
    return null;
  }

  @Override
  public void sleep() {
    super.sleep();
    resetValues();
  }
}