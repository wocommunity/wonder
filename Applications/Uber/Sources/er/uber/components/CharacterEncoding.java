package er.uber.components;


import org.junit.Assert;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;

import er.uber.model.Company;

public class CharacterEncoding extends UberComponent {
  private String _initialCompanyName = "汉语/漢語";
  private String _initialFormValue = "客家話";
  private Company _company;
  public String _value;

  public CharacterEncoding(WOContext context) {
    super(context);
    _value = _initialFormValue;
  }

  public Company company() {
    if (_company == null) {
      EOEditingContext editingContext = editingContext();
      _company = Company.createCompany(editingContext, _initialCompanyName);
    }
    return _company;
  }

  public WOActionResults testFormSubmit() {
    Assert.assertEquals(_value + " should have been " + _initialFormValue + " after the form submit.", _initialFormValue, _value);
    return null;
  }

  public WOActionResults testCompany() {
    EOEditingContext editingContext = editingContext();
    Company company = company();
    Assert.assertEquals("Company name should have been " + _initialCompanyName + " before we save to the database.", _initialCompanyName , company.name());
    editingContext.saveChanges();

    Company reloadCompany = (Company) company.refetchObjectFromDBinEditingContext(editingContext());
    Assert.assertEquals("Company name should have been " + _initialCompanyName  + " after saving and refetching from the database.", _initialCompanyName, reloadCompany.name());

    String newName = "北方話 " + System.currentTimeMillis();
    reloadCompany.setName(newName);
    editingContext().saveChanges();

    Company updateCompany = (Company) _company.refetchObjectFromDBinEditingContext(editingContext());
    Assert.assertEquals("Company name should have been " + newName + " after updating and refetching from the database.", newName, updateCompany.name());

    Company fetchCompany = Company.fetchCompany(editingContext, Company.NAME_KEY, newName);
    Assert.assertNotNull("There should have been a Company named " + newName + " after updating and refetching from the database.", fetchCompany);
    Assert.assertEquals("There should have been a Company named " + newName + " after updating and refetching from the database.", newName, fetchCompany.name());
    
    updateCompany.delete();
    editingContext.saveChanges();

    _company = null;

    return null;
  }
}