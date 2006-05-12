package er.imadaptor.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.extensions.ERXLogger;
import er.extensions.ERXStringUtilities;
import er.imadaptor.InstantMessengerAdaptor;

public class IMPickListAction extends WOComponent {
  private static ERXLogger log = ERXLogger.getERXLogger(IMPickListAction.class);

  private Object myRepetitionItem;
  private int myIndex;

  public IMPickListAction(WOContext _context) {
    super(_context);
  }

  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  public void setRepetitionItem(Object _repetitionItem) {
    myRepetitionItem = _repetitionItem;
  }

  public Object repetitionItem() {
    return myRepetitionItem;
  }

  public void setIndex(int _index) {
    myIndex = _index;
  }

  public int index() {
    return myIndex;
  }

  public int displayIndex() {
    return myIndex + 1;
  }

  public String displayItem() {
    String item;
    String displayStringKeyPath = (String) valueForBinding("displayStringKeyPath");
    if (displayStringKeyPath == null) {
      if (myRepetitionItem == null) {
        item = "";
      }
      else {
        item = myRepetitionItem.toString();
      }
    }
    else {
      Object displayValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(myRepetitionItem, displayStringKeyPath);
      if (displayValue == null) {
        item = "";
      }
      else {
        item = displayValue.toString();
      }
    }
    return item;
  }

  public void appendToResponse(WOResponse _response, WOContext _context) {
    String actionUrl = _context._componentActionURL(false);
    _response.setHeader(actionUrl, InstantMessengerAdaptor.IM_ACTION_URL_KEY);
    super.appendToResponse(_response, _context);
  }

  public WOActionResults invokeAction(WORequest _request, WOContext _context) {
    String message = _context.request().stringFormValueForKey(InstantMessengerAdaptor.MESSAGE_KEY);
    NSArray list = (NSArray) valueForBinding("list");
    NSArray selectedObjects;
    Object selectedObject;
    if (ERXStringUtilities.isDigitsOnly(message)) {
      int selectionIndex = Integer.parseInt(message) - 1;
      if (selectionIndex >= 0 || selectionIndex < list.count()) {
        selectedObject = list.objectAtIndex(selectionIndex);
        selectedObjects = new NSArray(selectedObject);
      }
      else {
        selectedObject = null;
        selectedObjects = NSArray.EmptyArray;
      }
    }
    else {
      String displayStringKeyPath = (String) valueForBinding("displayStringKeyPath");
      Boolean quicksilverBoolean = (Boolean) valueForBinding("quicksilver");
      boolean quicksilver = (quicksilverBoolean != null && quicksilverBoolean.booleanValue());
      selectedObjects = IMSearchOptionsAction.selectedValues(list, displayStringKeyPath, quicksilver, message);
      if (selectedObjects.count() == 1) {
        selectedObject = selectedObjects.objectAtIndex(0);
      }
      else {
        selectedObject = null;
      }
    }
    boolean selectionBound = canSetValueForBinding("selection");
    if (canSetValueForBinding("selections")) {
      setValueForBinding(selectedObjects, "selections");
    }
    if (canSetValueForBinding("selection")) {
      setValueForBinding(selectedObject, "selection");
    }
    return (WOActionResults) valueForBinding("action");
  }
}