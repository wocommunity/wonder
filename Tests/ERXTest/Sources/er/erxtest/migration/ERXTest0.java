package er.erxtest.migration;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationDatabase.Migration;
import er.extensions.migration.ERXMigrationTable;

public class ERXTest0 extends Migration {
  @Override
  public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    // TODO Auto-generated method stub

  }

  @Override
  public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
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

    ERXMigrationTable employeeTable = database.newTableNamed("Employee");
    employeeTable.newStringColumn("address1", 255, true);
    employeeTable.newStringColumn("address2", 255, true);
    employeeTable.newStringColumn("city", 255, true);
    employeeTable.newIntegerColumn("companyID", false);
    employeeTable.newIntegerColumn("id", false);
    employeeTable.newBooleanColumn("manager", false);
    employeeTable.newStringColumn("firstName", 255, false);
    employeeTable.newStringColumn("lastName", 255, false);
    employeeTable.newStringColumn("state", 255, true);
    employeeTable.newStringColumn("zipcode", 255, true);
    employeeTable.create();
    employeeTable.setPrimaryKey("id");
    employeeTable.addForeignKey("companyID", "Company", "id");

    ERXMigrationTable roleTable = database.newTableNamed("Role");
    roleTable.newIntegerColumn("id", false);
    roleTable.create();
    roleTable.setPrimaryKey("id");

    ERXMigrationTable employeeRoleTable = database.newTableNamed("EmployeeRole");
    employeeRoleTable.newIntegerColumn("employeeId", false);
    employeeRoleTable.newIntegerColumn("roleId", false);
    employeeRoleTable.create();
    employeeRoleTable.setPrimaryKey("employeeId", "roleId");
    employeeRoleTable.addForeignKey("employeeId", "Employee", "id");
    employeeRoleTable.addForeignKey("roleId", "Role", "id");

    ERXMigrationTable paycheckTable = database.newTableNamed("Paycheck");
    paycheckTable.newBigDecimalColumn("amount", 38, 2, false);
    paycheckTable.newBooleanColumn("cashed", false);
    paycheckTable.newIntegerColumn("employeeID", false);
    paycheckTable.newIntegerColumn("id", false);
    paycheckTable.newTimestampColumn("paymentDate", false);
    paycheckTable.create();
    paycheckTable.setPrimaryKey("id");
    paycheckTable.addForeignKey("employeeID", "Employee", "id");
  }
}
