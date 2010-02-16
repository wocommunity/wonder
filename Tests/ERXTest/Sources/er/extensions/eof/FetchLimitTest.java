package er.extensions.eof;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;

import er.erxtest.ERXTestCase;
import er.erxtest.model.Company;

public class FetchLimitTest extends ERXTestCase {
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
}
