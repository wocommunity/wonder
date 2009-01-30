package er.uber.migrations;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;

public class Uber1 extends ERXMigrationDatabase.Migration {
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
    ERXMigrationTable companyTable = database.existingTableNamed("Company");
    companyTable.newIntegerColumn("logoID", true);
    companyTable.addIndex("name");

    ERXMigrationTable employeeTable = database.existingTableNamed("Employee");
    employeeTable.addIndex("lastName");

    companyTable.addForeignKey("logoID", "ERAttachment", "id");
  }
}