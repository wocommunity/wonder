package er.uber.migrations;

import java.math.BigDecimal;
import java.util.TimeZone;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;
import er.extensions.migration.IERXPostMigration;
import er.uber.model.Company;
import er.uber.model.Employee;
import er.uber.model.EmployeeStatus;

public class Uber0 extends ERXMigrationDatabase.Migration implements IERXPostMigration {
  @Override
  public NSArray<ERXModelVersion> modelDependencies() {
    return new NSArray<ERXModelVersion>(new ERXModelVersion("ERAttachment", 1));
  }

  @Override
  public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    // DO NOTHING
  }

  @Override
  public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    ERXMigrationTable companyTable = database.newTableNamed("Company");
    companyTable.newIntegerColumn("id", false);
    companyTable.newStringColumn("name", 255, false);
    companyTable.newStringColumn("location", 255, true);
    companyTable.create();
    companyTable.setPrimaryKey("id");

    ERXMigrationTable employeeTable = database.newTableNamed("Employee");
    employeeTable.newIntBooleanColumn("admin", false);
    employeeTable.newIntegerColumn("companyID", false);
    employeeTable.newIntegerColumn("exemptions", true);
    employeeTable.newStringColumn("firstName", 255, false);
    employeeTable.newTimestampColumn("hireDate", false);
    employeeTable.newIntegerColumn("id", false);
    employeeTable.newBooleanColumn("insured", false);
    employeeTable.newStringColumn("lastName", 255, false);
    employeeTable.newIntegerColumn("photoID", true);
    employeeTable.newBigDecimalColumn("salary", 38, 4, true);
    employeeTable.newStringColumn("status", 50, false);
    employeeTable.create();
    employeeTable.setPrimaryKey("id");

    employeeTable.addForeignKey("companyID", "Company", "id");
    employeeTable.addForeignKey("photoID", "ERAttachment", "id");
  }

  public void postUpgrade(EOEditingContext editingContext, EOModel model) throws Throwable {
    Company c1 = Company.createCompany(editingContext, "ABC Corp");
    c1.setLocation("Richmond, VA");
    Employee c1e1 = Employee.createEmployee(editingContext, Boolean.TRUE, "Johnny", new NSTimestamp(2008, 1, 15, 6, 30, 0, TimeZone.getDefault()), Boolean.TRUE, "Boss", EmployeeStatus.Available, c1);
    c1e1.setSalary(new BigDecimal("1000000.00"));
    c1e1.setExemptions(Integer.valueOf(2));
    c1e1.taggable().addTagNamed("employee");
    c1e1.taggable().addTagNamed("boss");
    Employee c1e2 = Employee.createEmployee(editingContext, Boolean.FALSE, "Bill", new NSTimestamp(2004, 11, 15, 6, 30, 0, TimeZone.getDefault()), Boolean.FALSE, "Employee", EmployeeStatus.Available, c1);
    c1e2.setSalary(new BigDecimal("50.00"));
    c1e2.taggable().addTagNamed("employee");

    Company c2 = Company.createCompany(editingContext, "XYZ Corp");
    c2.setLocation("Cupertino, CA");
    Employee c2e1 = Employee.createEmployee(editingContext, Boolean.TRUE, "Jane", new NSTimestamp(2008, 1, 15, 6, 30, 0, TimeZone.getDefault()), Boolean.TRUE, "Boss", EmployeeStatus.Available, c2);
    c2e1.taggable().addTagNamed("employee");
    c2e1.taggable().addTagNamed("boss");
    Employee c2e2 = Employee.createEmployee(editingContext, Boolean.FALSE, "Action", new NSTimestamp(2004, 11, 15, 6, 30, 0, TimeZone.getDefault()), Boolean.FALSE, "Jackson", EmployeeStatus.Available, c2);
    c2e2.taggable().addTagNamed("employee");
  }
}