package er.uber.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.components.ERXComponent;
import er.extensions.eof.ERXEC;

public class UberComponent extends ERXComponent {
  private EOEditingContext _editingContext;

  public UberComponent(WOContext context) {
    super(context);
  }

  public EOEditingContext editingContext() {
    EOEditingContext editingContext;
    WOComponent parent = parent();
    if (parent instanceof UberComponent) {
      editingContext = ((UberComponent) parent).editingContext();
    }
    else {
      if (_editingContext == null) {
        _editingContext = ERXEC.newEditingContext();
      }
      editingContext = _editingContext;
    }
    return editingContext;
  }
}
