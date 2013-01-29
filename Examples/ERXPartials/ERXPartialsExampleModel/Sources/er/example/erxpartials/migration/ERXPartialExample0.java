package er.example.erxpartials.migration;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.example.erxpartials.model.Company;
import er.example.erxpartials.model.Department;
import er.example.erxpartials.model.EmployeeType;
import er.example.erxpartials.model.Partial_AuthenticatedPerson;
import er.example.erxpartials.model.Person;
import er.extensions.crypting.ERXCrypto;
import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;
import er.extensions.migration.IERXPostMigration;

public class ERXPartialExample0 extends ERXMigrationDatabase.Migration implements IERXPostMigration {
  @Override
  public NSArray<ERXModelVersion> modelDependencies() {
    return null;
  }
  
  @Override
  public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    // DO NOTHING
  }

  @Override
  public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
		ERXMigrationTable employeeTypeTable = database.newTableNamed("EmployeeType");
		employeeTypeTable.newIntegerColumn("id", false);
		employeeTypeTable.newStringColumn("name", 255, false);
		employeeTypeTable.create();
	 	employeeTypeTable.setPrimaryKey("id");

		ERXMigrationTable departmentTable = database.newTableNamed("Department");
		departmentTable.newIntegerColumn("companyID", false);
		departmentTable.newStringColumn("departmentCode", 255, false);
		departmentTable.newIntegerColumn("id", false);
		departmentTable.newStringColumn("name", 255, false);
		departmentTable.create();
	 	departmentTable.setPrimaryKey("id");

		ERXMigrationTable companyTable = database.newTableNamed("Company");
		companyTable.newStringColumn("address1", 255, true);
		companyTable.newStringColumn("address2", 255, true);
		companyTable.newStringColumn("city", 255, true);
		companyTable.newIntegerColumn("id", false);
		companyTable.newStringColumn("name", 255, false);
		companyTable.newStringColumn("state", 255, true);
		companyTable.newStringColumn("zipcode", 255, true);
		companyTable.create();
	 	companyTable.setPrimaryKey("id");

		ERXMigrationTable partial_AuthenticatedPersonTable = database.existingTableNamed("Person");
		partial_AuthenticatedPersonTable.newTimestampColumn("lastLoginDate", true);
		partial_AuthenticatedPersonTable.newStringColumn("password", 255, true);
		partial_AuthenticatedPersonTable.newStringColumn("username", 255, true);

		ERXMigrationTable partial_EmployeePersonTable = database.existingTableNamed("Person");
		partial_EmployeePersonTable.newIntegerColumn("departmentID", true);
		partial_EmployeePersonTable.newStringColumn("employeeNumber", 255, true);
		partial_EmployeePersonTable.newIntegerColumn("employeeTypeID", true);
		partial_EmployeePersonTable.newBigDecimalColumn("salary", 38, 2, true);

		partial_EmployeePersonTable.addForeignKey("departmentID", "Department", "id");
		partial_EmployeePersonTable.addForeignKey("employeeTypeID", "EmployeeType", "id");
		departmentTable.addForeignKey("companyID", "Company", "id");
  }

  public void postUpgrade(EOEditingContext ec, EOModel model) throws Throwable {
	  EmployeeType employee = EmployeeType.createEmployeeType(ec, "Employee");
	  EmployeeType contract = EmployeeType.createEmployeeType(ec, "Contractor");
	  EmployeeType intern = EmployeeType.createEmployeeType(ec, "Intern");
	  ec.saveChanges();
	  
	  Company gvc = Company.createCompany(ec, "Global Village Consulting");
	  Company apple = Company.createCompany(ec, "Apple");
	  Company logicsquad = Company.createCompany(ec, "Logic Squad");
	  ec.saveChanges();
	  
	  Department gvcDev = Department.createDepartment(ec, "123456", "GVC Development", gvc);
	  Department logicDev = Department.createDepartment(ec, "987654321", "Logic Squad Development", logicsquad);
	  ec.saveChanges();
	  
	  NSArray<Person> allPersons = Person.fetchAllPersons(ec);
	  for ( Person aPerson : allPersons) {
		  Partial_AuthenticatedPerson auth = aPerson.partialForClass(Partial_AuthenticatedPerson.class);
		  auth.setLastLoginDate(new NSTimestamp());
		  auth.setUsername( aPerson.firstName().toLowerCase() + "." + aPerson.lastName().toLowerCase() );
		  auth.setPassword( ERXCrypto.base64HashedString(auth.username()) );
	  }
  }
}
