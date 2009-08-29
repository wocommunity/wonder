package er.uber.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.eof.ERXEC;
import er.uber.model.Company;

public class Attachment extends UberComponent {
	private EOEditingContext _editingContext;
	public WODisplayGroup _companies;
	public Company _company;

	public Attachment(WOContext context) {
		super(context);
	}

	public Company selectedCompany() {
		return (Company) _companies.selectedObject();
	}

	public WOActionResults newCompany() {
		_editingContext = ERXEC.newEditingContext();
		_companies.setSelectedObject(Company.createCompany(_editingContext, "New Company"));
		return null;
	}

	public WOActionResults selectCompany() {
		_editingContext = ERXEC.newEditingContext();
		_companies.setSelectedObject(_company.localInstanceIn(_editingContext));
		return null;
	}

	public WOActionResults clearAttachment() {
		Company selectedCompany = selectedCompany();
		selectedCompany.setLogo(null);
		return null;
	}

	public WOActionResults save() {
		_editingContext.saveChanges();
		_companies.setSelectedObject(null);
		_companies.fetch();
		return null;
	}
}