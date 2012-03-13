package er.memoryadaptor;

import java.math.BigDecimal;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

import er.erxtest.ERXTestCase;
import er.erxtest.model.Company;
import er.erxtest.model.Employee;
import er.erxtest.model.Paycheck;
import er.erxtest.model.Role;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXQ;
import er.extensions.foundation.ERXValueUtilities;

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
	
	public void testSnapshot() {
		EOEditingContext ec = ERXEC.newEditingContext();
		Company company = Company.createCompany(ec, "Test Company");
		Employee employee = Employee.createEmployee(ec, "John", "Doe", Boolean.FALSE, company);
		Role role1 = Role.createRole(ec);
		employee.addToRoles(role1);
		
		NSDictionary<String, Object> snapshot = employee.snapshot();
		NSDictionary committedSnapshot = employee.committedSnapshot();
		
		/*
		 * Snapshot should have same values as the eo and committed snapshot should be all
		 * null values as eo has not yet been saved.
		 */
		EOEntity entity = employee.entity();
		for (String key : (NSArray<String>) entity.classPropertyNames()) {
			Object snapshotValue = snapshot.valueForKey(key);
			assertEquals(employee.valueForKey(key), ERXValueUtilities.isNull(snapshotValue) ? null : snapshotValue);
			assertTrue(ERXValueUtilities.isNull(committedSnapshot.valueForKey(key)));
		}
		
		ec.saveChanges();
		
		Role role2 = Role.createRole(ec);
		employee.addToRoles(role2);
		employee.removeFromRoles(role1);
		
		NSDictionary changesFromCommittedSnapshot = employee.changesFromCommittedSnapshot();
		
		/*
		 * We changed only the role relationship so the only recorded change should be
		 * roles with the added and removed object.
		 */
		assertEquals(1, changesFromCommittedSnapshot.count());
		Object rolesObjectChanges = changesFromCommittedSnapshot.valueForKey(Employee.ROLES_KEY);
		assertFalse(ERXValueUtilities.isNull(rolesObjectChanges));
		assertTrue(rolesObjectChanges instanceof NSArray);
		NSArray rolesChanges = (NSArray) rolesObjectChanges;
		assertEquals(2, rolesChanges.count());
		assertTrue(rolesChanges.get(0) instanceof NSArray);
		assertTrue(rolesChanges.get(1) instanceof NSArray);
		
		NSArray addedRoles = (NSArray) rolesChanges.get(0);
		NSArray removedRoles = (NSArray) rolesChanges.get(1);
		assertEquals(1, addedRoles.count());
		assertEquals(1, removedRoles.count());
		assertEquals(role2, addedRoles.get(0));
		assertEquals(role1, removedRoles.get(0));
		
		ec.saveChanges();
	}
	
	public void testFetchWithRelationshipQualifier() {
		EOEditingContext ec = ERXEC.newEditingContext();
		
		Company company = Company.createCompany(ec, "Fetch Test Company");
		Employee employee = Employee.createEmployee(ec, "Fetch", "Test", Boolean.FALSE, company);
		Role role = Role.createRole(ec);
		employee.addToRoles(role);
		Paycheck paycheck = Paycheck.createPaycheck(ec, new BigDecimal(10), Boolean.FALSE, new NSTimestamp(), employee);
		
		ec.saveChanges();
		
		EOQualifier baseQualifier = Employee.FIRST_NAME.is("Fetch");
		EOQualifier companyQualifier = Employee.COMPANY.dot(Company.NAME).is("Fetch Test Company");
		EOQualifier alternateCompanyQualifier = Employee.COMPANY.is(company);
		EOQualifier paycheckQualifier = Employee.PAYCHECKS.dot(Paycheck.CASHED).isFalse();
		EOQualifier alternatePaycheckQualifier = Employee.PAYCHECKS.containsObject(paycheck);
		EOQualifier roleQualifier = Employee.ROLES.containsObject(role);
		
		// simple attribute qualifier
		fetchEmployee(ec, baseQualifier, employee);
		// to-one relationship
		fetchEmployee(ec, ERXQ.and(baseQualifier, companyQualifier), employee);
		fetchEmployee(ec, ERXQ.and(baseQualifier, alternateCompanyQualifier), employee);
		// to-many relationship
		fetchEmployee(ec, ERXQ.and(baseQualifier, paycheckQualifier), employee);
		fetchEmployee(ec, ERXQ.and(baseQualifier, alternatePaycheckQualifier), employee);
		// many-to-many relationship
		fetchEmployee(ec, ERXQ.and(baseQualifier, roleQualifier), employee);
	}
	
	private void fetchEmployee(EOEditingContext ec, EOQualifier qualifier, Employee expected) {
		NSArray<Employee> found = Employee.fetchEmployees(ec, qualifier, null);
		assertEquals(1, found.count());
		assertEquals(expected, found.get(0));
	}
}
