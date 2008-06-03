package er.erxtest.tests;

import junit.framework.TestCase;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;

import er.erxtest.model.Company;
import er.erxtest.model.Employee;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXGenericRecord;

public class ERXGenericRecordUpdateInverseRelationshipsTest extends TestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ERXGenericRecord.InverseRelationshipUpdater.setUpdateInverseRelationships(true);
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    ERXGenericRecord.InverseRelationshipUpdater.setUpdateInverseRelationships(false);
  }
  
  public void testSetToOneImplicitToMany() {
    ERXGenericRecord.InverseRelationshipUpdater.setUpdateInverseRelationships(false);
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    p1.setCompanyRelationship(company);

    assertEquals(company, p1.company());
    assertEquals(1, company.employees().count());
    assertEquals(p1, company.employees().objectAtIndex(0));
  }

  public void testAddToToManyImplicitToOne() {
    ERXGenericRecord.InverseRelationshipUpdater.setUpdateInverseRelationships(false);
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    company.addToEmployeesRelationship(p1);

    assertEquals(company, p1.company());
    assertEquals(1, company.employees().count());
    assertEquals(p1, company.employees().objectAtIndex(0));
  }

  public void testAutoSetToOneImplicitToMany() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    p1.setCompany(company);

    assertEquals(company, p1.company());
    assertEquals(1, company.employees().count());
    assertEquals(p1, company.employees().objectAtIndex(0));
  }

  public void testAutoAddToToManyImplicitToOne() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    company.addToEmployees(p1);

    assertEquals(company, p1.company());
    assertEquals(1, company.employees().count());
    assertEquals(p1, company.employees().objectAtIndex(0));
  }

  public void testAutoSetThenSetToOneToNullImplicitToMany() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    p1.setCompany(company);
    p1.setCompany(null);
   
    assertEquals(null, p1.company());
    assertEquals(0, company.employees().count());
  }

  public void testAutoSetThenSetToOneToAnotherImplicitToMany() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Company company2 = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    p1.setCompany(company);
    p1.setCompany(company2);

    assertEquals(company2, p1.company());
    assertEquals(0, company.employees().count());
    assertEquals(1, company2.employees().count());
    assertEquals(p1, company2.employees().objectAtIndex(0));
  }

  public void testAutoRemoveFromToManyImplicitToOne() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    company.addToEmployees(p1);
    company.removeFromEmployees(p1);

    assertEquals(null, p1.company());
    assertEquals(0, company.employees().count());
  }

  public void testAutoSetTwoToOnesImplicitToMany() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    Employee p2 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    p1.setCompany(company);
    p2.setCompany(company);

    assertEquals(company, p1.company());
    assertEquals(company, p2.company());
    assertEquals(2, company.employees().count());
    assertTrue(company.employees().containsObject(p1));
    assertTrue(company.employees().containsObject(p2));
  }

  public void testAutoRemoveFromToManyWithManyImplicitToOne() {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    Company company = Company.createCompany(editingContext, "XYZ");
    Employee p1 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    Employee p2 = (Employee) EOUtilities.createAndInsertInstance(editingContext, Employee.ENTITY_NAME);
    company.addToEmployees(p1);
    company.addToEmployees(p2);
    company.removeFromEmployees(p1);

    assertEquals(null, p1.company());
    assertEquals(company, p2.company());
    assertEquals(1, company.employees().count());
    assertEquals(p2, company.employees().objectAtIndex(0));
  }
}
