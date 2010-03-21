package er.memoryadaptor;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.erxtest.ERXTestCase;
import er.erxtest.model.Company;
import er.extensions.eof.ERXEC;

public class ERMemoryAdaptorTest extends ERXTestCase {
	public void testFetchLimit() {
		EOEditingContext editingContext = ERXEC.newEditingContext();
		for (int i = 0; i < 10; i++) {
			Company.createCompany(editingContext, "Test Company " + i);
		}
		editingContext.saveChanges();
		EOFetchSpecification fetchSpec = new EOFetchSpecification("Company", null, null);
		fetchSpec.setFetchLimit(1);
		NSArray companies = editingContext.objectsWithFetchSpecification(fetchSpec);
		assertEquals(1, companies.size());
	}

	public void testFetchLimitWithSortOrder() {
		EOEditingContext editingContext = ERXEC.newEditingContext();
		for (int i = 0; i < 10; i++) {
			Company.createCompany(editingContext, "Test Company " + i);
		}
		editingContext.saveChanges();
		EOFetchSpecification fetchSpec = new EOFetchSpecification("Company", null, Company.NAME.descs());
		fetchSpec.setFetchLimit(1);
		NSArray<Company> companies = editingContext.objectsWithFetchSpecification(fetchSpec);
		assertEquals(1, companies.size());
		assertEquals("Test Company 9", companies.objectAtIndex(0).name());
	}

	public void testDelete() {
		EOEditingContext editingContext = ERXEC.newEditingContext();
		NSMutableArray<Company> companies = new NSMutableArray<Company>();
		for (int i = 0; i < 10; i++) {
			Company c = Company.createCompany(editingContext, "Test Company " + i);
			companies.addObject(c);
		}
		editingContext.saveChanges();

		for (Company c : companies) {
			c.delete();
			editingContext.saveChanges();
		}
	}
}
