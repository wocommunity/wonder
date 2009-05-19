package er.uber.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EOEditingContext;

import er.attachment.model.ERAttachment;
import er.extensions.eof.ERXEC;
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

	public WOActionResults clearAttachment() {
		EOEditingContext editingContext = ERXEC.newEditingContext();
		Company localCompany = selectedCompany().localInstanceIn(editingContext);
		ERAttachment attachment = localCompany.logo();
		attachment.delete();
		localCompany.setLogo(null);
		editingContext.saveChanges();
		return null;
	}

	public WOActionResults save() {
		editingContext().saveChanges();
		return null;
	}
}