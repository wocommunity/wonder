package er.uber.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;

import er.uber.model.Employee;

public class Taggable extends UberComponent {
  public String _tag;
  public String _tagClass;

  public WODisplayGroup _employees;
  public Employee _employee;

  public Taggable(WOContext context) {
    super(context);
  }

  public Employee selectedEmployee() {
    return (Employee) _employees.selectedObject();
  }

  public WOActionResults selectEmployee() {
    _employees.setSelectedObject(_employee);
    return null;
  }

  public WOActionResults save() {
    editingContext().saveChanges();
    return null;
  }
}