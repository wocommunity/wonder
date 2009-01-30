package er.uber.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;

import er.uber.model.Company;

public class Attachment extends UberComponent {
  public WODisplayGroup _companies;
  public Company _company;

  public Attachment(WOContext context) {
    super(context);
  }

  public Company selectedCompany() {
    return (Company) _companies.selectedObject();
  }

  public WOActionResults selectCompany() {
    _companies.setSelectedObject(_company);
    return null;
  }

  public WOActionResults save() {
    editingContext().saveChanges();
    return null;
  }
}