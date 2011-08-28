package wowodc.eof.migrations;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;

public class ThreadsDemo0 extends ERXMigrationDatabase.Migration {
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
    ERXMigrationTable resultItemTable = database.newTableNamed("resultitem");
    resultItemTable.newBigIntegerColumn("closestfactorial", true);
    resultItemTable.newIntegerColumn("factornumber", true);
    resultItemTable.newIntegerColumn("id", false);
    resultItemTable.newIntBooleanColumn("isfactorialprime", false);
    resultItemTable.newIntBooleanColumn("isprime", false);
    resultItemTable.newTimestampColumn("modificationtime", false);
    resultItemTable.newBigIntegerColumn("numbertocheck", false);
    resultItemTable.newIntegerColumn("taskinfoid", false);
    resultItemTable.newStringColumn("workflowstate", 255, false);
    resultItemTable.create();
    resultItemTable.setPrimaryKey("id");

    ERXMigrationTable taskInfoTable = database.newTableNamed("taskinfo");
    taskInfoTable.newBigIntegerColumn("duration", false);
    taskInfoTable.newBigIntegerColumn("endnumber", true);
    taskInfoTable.newTimestampColumn("endtime", true);
    taskInfoTable.newIntegerColumn("id", false);
    taskInfoTable.newBigIntegerColumn("startnumber", false);
    taskInfoTable.newTimestampColumn("starttime", true);
    taskInfoTable.newStringColumn("workflowstate", 255, false);
    taskInfoTable.create();
    taskInfoTable.setPrimaryKey("id");

    resultItemTable.addForeignKey("taskinfoid", "taskinfo", "id");
  }
}