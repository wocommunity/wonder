package er.iui.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;

import er.extensions.components.ERXComponent;

public class ERIUIDialogButton extends ERXComponent {
  public ERIUIDialogButton(WOContext context) {
    super(context);
  }

  public boolean isDialogVisible() {
    return booleanValueForBinding("dialogVisible");
  }

  @Override
  public void takeValuesFromRequest(WORequest request, WOContext context) {
    super.takeValuesFromRequest(request, context);
  }

  @Override
  public WOActionResults invokeAction(WORequest request, WOContext context) {
    WOActionResults results = super.invokeAction(request, context);
    return results;
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  public WOActionResults toggleDialog() {
    boolean dialogVisible = !isDialogVisible();
    setValueForBinding(Boolean.valueOf(dialogVisible), "dialogVisible");
    return null;
  }
}