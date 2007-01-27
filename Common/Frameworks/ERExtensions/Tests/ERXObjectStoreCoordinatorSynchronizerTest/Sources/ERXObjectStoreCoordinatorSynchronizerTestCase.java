import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXEC;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXObjectStoreCoordinatorPool;
import er.extensions.ERXRandomGUID;

/**
 * Run this with ERXMainRunner.
 * 
 * @author mschrag
 */
public class ERXObjectStoreCoordinatorSynchronizerTestCase extends TestCase {
  public void testInsertEO() {
    // Create Company in OSC1
    String companyName = "Company" + ERXRandomGUID.newGid();
    EOEditingContext editingContext_osc1 = ERXEC.newEditingContext(ERXObjectStoreCoordinatorPool._pool().nextObjectStore());
    Company company_osc1 = Company.createCompany(editingContext_osc1, companyName);
    editingContext_osc1.saveChanges();
    sleep();

    // Fetch Company in OSC2
    EOEditingContext editingContext_osc2 = ERXEC.newEditingContext(ERXObjectStoreCoordinatorPool._pool().nextObjectStore());
    Company company_osc2 = Company.fetchCompany(editingContext_osc2, Company.NAME_KEY, companyName);
    assertNotNull(company_osc2);
    assertEquals(companyName, company_osc1.name());
    assertEquals(companyName, company_osc2.name());
  }

  public void testUpdateEO() {
    // Create Company1 in OSC1
    String companyName = "Company" + ERXRandomGUID.newGid();
    EOEditingContext editingContext_osc1 = ERXEC.newEditingContext(ERXObjectStoreCoordinatorPool._pool().nextObjectStore());
    Company company_osc1 = Company.createCompany(editingContext_osc1, companyName);
    editingContext_osc1.saveChanges();
    sleep();

    // Fetch Company1 in OSC2
    EOEditingContext editingContext_osc2 = ERXEC.newEditingContext(ERXObjectStoreCoordinatorPool._pool().nextObjectStore());
    Company company_osc2 = Company.fetchCompany(editingContext_osc2, Company.NAME_KEY, companyName);

    for (int i = 0; i < 10; i ++) {
      // Change Company1's name in OSC1
      String companyName_1 = "Company" + ERXRandomGUID.newGid() + "_osc1";
      company_osc1.setName(companyName_1);
      editingContext_osc1.saveChanges();
      sleep();
      assertEquals(companyName_1, company_osc2.name());
      assertEquals(companyName_1, company_osc1.name());
  
      // Fetch Company1 by name in OSC1
      Company company_osc1_with_companyName1 = Company.fetchCompany(editingContext_osc1, Company.NAME_KEY, companyName_1);
      assertEquals(company_osc1, company_osc1_with_companyName1);
  
      // Fetch Company1 by name in OSC2
      Company company_osc2_with_companyName1 = Company.fetchCompany(editingContext_osc2, Company.NAME_KEY, companyName_1);
      assertEquals(company_osc2, company_osc2_with_companyName1);
  
      // Change Company1's name in OSC2
      String companyName_2 = "Company" + ERXRandomGUID.newGid() + "_osc2";
      company_osc2.setName(companyName_2);
      editingContext_osc2.saveChanges();
      sleep();
      assertEquals(companyName_2, company_osc1.name());
      assertEquals(companyName_2, company_osc2.name());
      
      // Fetch Company1 by name in OSC1
      Company company_osc1_with_companyName2 = Company.fetchCompany(editingContext_osc1, Company.NAME_KEY, companyName_2);
      assertEquals(company_osc1, company_osc1_with_companyName2);
  
      // Fetch Company1 by name in OSC2
      Company company_osc2_with_companyName2 = Company.fetchCompany(editingContext_osc2, Company.NAME_KEY, companyName_2);
      assertEquals(company_osc2, company_osc2_with_companyName2);
    }
  }

  public void testMergeEOChanges() {
    // Create Company1 in OSC1
    String companyName = "Company" + ERXRandomGUID.newGid();
    EOEditingContext editingContext_osc1 = ERXEC.newEditingContext(ERXObjectStoreCoordinatorPool._pool().nextObjectStore());
    Company company_osc1 = Company.createCompany(editingContext_osc1, companyName);
    editingContext_osc1.saveChanges();
    sleep();

    // Fetch Company1 in OSC2
    EOEditingContext editingContext_osc2 = ERXEC.newEditingContext(ERXObjectStoreCoordinatorPool._pool().nextObjectStore());
    Company company_osc2 = Company.fetchCompany(editingContext_osc2, Company.NAME_KEY, companyName);

    for (int i = 0; i < 10; i ++) {
      // Change Company1's name in OSC1
      String companyName_1 = "Company" + ERXRandomGUID.newGid() + "_osc1";
      String companyAddress1_1 = company_osc1.address1();
      company_osc1.setName(companyName_1);
      
      // Change Company1's address in OSC2
      String companyAddress1_2 = "Address" + ERXRandomGUID.newGid() + "_osc2";
      company_osc2.setAddress1(companyAddress1_2);

      // Save Company1's new name in OSC1, address in OSC2 should be untouched but merged with new name
      editingContext_osc1.saveChanges();
      sleep();
      assertEquals(companyName_1, company_osc2.name());
      assertEquals(companyName_1, company_osc1.name());
      assertEquals(companyAddress1_2, company_osc2.address1());
      assertEquals(companyAddress1_1, company_osc1.address1());
  
      // Fetch Company1 by name in OSC1
      Company company_osc1_with_companyName1 = Company.fetchCompany(editingContext_osc1, Company.NAME_KEY, companyName_1);
      assertEquals(company_osc1, company_osc1_with_companyName1);
      assertEquals(companyAddress1_1, company_osc1_with_companyName1.address1());
  
      // Fetch Company1 by name in OSC2
      Company company_osc2_with_companyName1 = Company.fetchCompany(editingContext_osc2, Company.NAME_KEY, companyName_1);
      assertEquals(company_osc2, company_osc2_with_companyName1);
      assertEquals(companyAddress1_2, company_osc2_with_companyName1.address1());
  
      // Save Company1's new address in OSC2
      editingContext_osc2.saveChanges();
      sleep();
      assertEquals(companyName_1, company_osc1.name());
      assertEquals(companyName_1, company_osc2.name());
      assertEquals(companyAddress1_2, company_osc2.address1());
      assertEquals(companyAddress1_2, company_osc1.address1());
    }
  }

  public void testAddToUnfaultedToMany() {
    // Create Company1 in OSC1
    String companyName = "Company" + ERXRandomGUID.newGid();
    EOEditingContext editingContext_osc1 = ERXEC.newEditingContext(ERXObjectStoreCoordinatorPool._pool().nextObjectStore());
    Company company_osc1 = Company.createCompany(editingContext_osc1, companyName);
    editingContext_osc1.saveChanges();

    // Fetch Company1 in OSC2
    EOEditingContext editingContext_osc2 = ERXEC.newEditingContext(ERXObjectStoreCoordinatorPool._pool().nextObjectStore());
    Company company_osc2 = Company.fetchCompany(editingContext_osc2, Company.NAME_KEY, companyName);

    // Create and Save Employee1 in Company1 in OSC1
    String employeeName = "Employee" + ERXRandomGUID.newGid();
    Employee employee_osc1 = Employee.createEmployee(editingContext_osc1, Boolean.FALSE, employeeName, company_osc1);
    editingContext_osc1.saveChanges();

    // Fetch employees relationship of Company1 in OSC1
    NSArray employees_osc1 = company_osc1.employees();
    assertEquals(1, employees_osc1.count());
    assertEquals(employee_osc1, employees_osc1.objectAtIndex(0));

    // Fetch employees relationship of Company1 in OSC2
    NSArray employees_osc2 = company_osc2.employees();
    assertEquals(1, employees_osc2.count());
    assertEOEquals(employee_osc1, employees_osc1.objectAtIndex(0));
  }

  public void testAddToFaultedToMany() {
    // Create Company1 in OSC1
    String companyName = "Company" + ERXRandomGUID.newGid();
    EOEditingContext editingContext_osc1 = ERXEC.newEditingContext(ERXObjectStoreCoordinatorPool._pool().nextObjectStore());
    Company company_osc1 = Company.createCompany(editingContext_osc1, companyName);
    editingContext_osc1.saveChanges();
    sleep();

    // Fetch employees for Company1 in OSC1
    NSArray employees_osc1 = company_osc1.employees();

    // Fetch Company1 in OSC2
    EOEditingContext editingContext_osc2 = ERXEC.newEditingContext(ERXObjectStoreCoordinatorPool._pool().nextObjectStore());
    Company company_osc2 = Company.fetchCompany(editingContext_osc2, Company.NAME_KEY, companyName);
    // Fetch employees for Company1 in OSC2
    NSArray employees_osc2 = company_osc2.employees();

    // Create and Save Employee1 for Company1 in OSC1
    String employeeName = "Employee" + ERXRandomGUID.newGid();
    Employee employee_osc1 = Employee.createEmployee(editingContext_osc1, Boolean.FALSE, employeeName, company_osc1);
    editingContext_osc1.saveChanges();
    sleep();

    // Check employees for Company1 in OSC1
    NSArray employees_osc1_AfterInsert = company_osc1.employees();
    assertEquals(1, employees_osc1_AfterInsert.count());
    assertEquals(employee_osc1, employees_osc1_AfterInsert.objectAtIndex(0));

    // Check employees for Company1 in OSC2
    NSArray employees_osc2_AfterInsert = company_osc2.employees();
    assertEquals(1, employees_osc2_AfterInsert.count());
    assertEOEquals(employee_osc1, employees_osc2_AfterInsert.objectAtIndex(0));
    
    // Create employee for Company1 in OSC2 and Save
    String employeeName2 = "Employee" + ERXRandomGUID.newGid();
    Employee employee_osc2 = Employee.createEmployee(editingContext_osc2, Boolean.FALSE, employeeName2, company_osc2);
    editingContext_osc2.saveChanges();
    sleep();

    // Check employees for Company1 in OSC1
    NSArray employees_osc1_AfterInsert2 = company_osc1.employees();
    assertEquals(2, employees_osc1_AfterInsert2.count());
    assertEOEquals(employee_osc1, employees_osc1_AfterInsert2.objectAtIndex(0));
    assertEOEquals(employee_osc2, employees_osc1_AfterInsert2.objectAtIndex(1));
    assertEquals(employeeName2, ((Employee) employees_osc1_AfterInsert2.objectAtIndex(1)).name());

    // Check employees for Company1 in OSC2
    NSArray employees_osc2_AfterInsert2 = company_osc2.employees();
    assertEquals(2, employees_osc2_AfterInsert2.count());
    assertEOEquals(employee_osc1, employees_osc2_AfterInsert2.objectAtIndex(0));
    assertEOEquals(employee_osc2, employees_osc2_AfterInsert2.objectAtIndex(1));
    assertEquals(employeeName2, ((Employee) employees_osc2_AfterInsert2.objectAtIndex(1)).name());
  }

  public void testAddToFaultedToManyWithUncommittedToManyEntries() {
    // Create Company1 in OSC1
    String companyName = "Company" + ERXRandomGUID.newGid();
    EOEditingContext editingContext_osc1 = ERXEC.newEditingContext(ERXObjectStoreCoordinatorPool._pool().nextObjectStore());
    Company company_osc1 = Company.createCompany(editingContext_osc1, companyName);
    editingContext_osc1.saveChanges();
    sleep();

    // Fetch Company1 in OSC2
    EOEditingContext editingContext_osc2 = ERXEC.newEditingContext(ERXObjectStoreCoordinatorPool._pool().nextObjectStore());
    Company company_osc2 = Company.fetchCompany(editingContext_osc2, Company.NAME_KEY, companyName);

    // Create (but do not save) Employee1 for Company1 in OSC2
    String employeeName1 = "Employee" + ERXRandomGUID.newGid();
    Employee employee1_osc2 = Employee.createEmployee(editingContext_osc2, Boolean.FALSE, employeeName1, company_osc2);

    // Check employees for Company1 in OSC1 (should contain uncommitted Employee)
    NSArray employees_osc2_BeforeInsert = company_osc2.employees();
    assertEquals(1, employees_osc2_BeforeInsert.count());
    assertEOEquals(employee1_osc2, employees_osc2_BeforeInsert.objectAtIndex(0));
    
    // Create and save Employee2 for Company1 in OSC1
    String employeeName2 = "Employee" + ERXRandomGUID.newGid();
    Employee employee2_osc1 = Employee.createEmployee(editingContext_osc1, Boolean.FALSE, employeeName2, company_osc1);
    editingContext_osc1.saveChanges();
    sleep();

    // Check employees for Company1 in OSC2 (should contain both Employees)
    NSArray employees_osc2_AfterInsert = company_osc2.employees();
    assertEquals(2, employees_osc2_AfterInsert.count());
    assertEOEquals(employee2_osc1, employees_osc2_AfterInsert.objectAtIndex(0));
    assertEOEquals(employee1_osc2, employees_osc2_AfterInsert.objectAtIndex(1));
    
    // Check employees for Company1 in OSC1 (should contain only Employee2)
    NSArray employees_osc1_AfterInsert = company_osc1.employees();
    assertEquals(1, employees_osc1_AfterInsert.count());
    assertEquals(employee2_osc1, employees_osc1_AfterInsert.objectAtIndex(0));

    // Save Employee1 in OSC2
    editingContext_osc2.saveChanges();
    sleep();

    // Check employees for Company1 in OSC2 (should contain both Employees)
    NSArray employees_osc2_AfterInsert2 = company_osc2.employees();
    assertEquals(2, employees_osc2_AfterInsert2.count());
    assertEOEquals(employee2_osc1, employees_osc2_AfterInsert2.objectAtIndex(0));
    assertEOEquals(employee1_osc2, employees_osc2_AfterInsert2.objectAtIndex(1));
    
    // Check employees for Company1 in OSC1 (should contain only Employee2)
    NSArray employees_osc1_AfterInsert2 = company_osc1.employees();
    assertEquals(2, employees_osc1_AfterInsert2.count());
    assertEOEquals(employee2_osc1, employees_osc1_AfterInsert2.objectAtIndex(0));
    assertEOEquals(employee1_osc2, employees_osc1_AfterInsert2.objectAtIndex(1));
  }
  
  public static void assertEOEquals(Object obj1, Object obj2) {
    if (obj1 != null && obj2 != null && obj1 instanceof EOEnterpriseObject && obj2 instanceof EOEnterpriseObject) {
      EOEnterpriseObject eo1 = (EOEnterpriseObject)obj1;
      EOEnterpriseObject eo2 = (EOEnterpriseObject)obj2;
      if (!ERXEOControlUtilities.eoEquals(eo1, eo2)) {
        throw new AssertionFailedError("Expected " + eo1.editingContext().globalIDForObject(eo1) + ", but got " + eo2.editingContext().globalIDForObject(eo2));
      }
    }
    else {
      assertEquals(obj1, obj2);
    }
  }
  
  protected void sleep() {
    try { Thread.sleep(50); } catch (Throwable t) { }
  }

  public static void main(String[] args) {
    TestRunner.run(ERXObjectStoreCoordinatorSynchronizerTestCase.class);
  }
}
