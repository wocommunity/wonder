package com.secretpal.migrations;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;

public class SecretPal2 extends ERXMigrationDatabase.Migration {
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
    ERXMigrationTable spNoNoPalTable = database.newTableNamed("SPNoNoPal");
    spNoNoPalTable.newIntegerColumn("eventID", false);
    spNoNoPalTable.newIntegerColumn("giverID", false);
    spNoNoPalTable.newIntegerColumn("id", false);
    spNoNoPalTable.newIntegerColumn("receiverID", false);
    spNoNoPalTable.create();
    spNoNoPalTable.setPrimaryKey("id");

    spNoNoPalTable.addForeignKey("eventID", "SPEvent", "id");
    spNoNoPalTable.addForeignKey("giverID", "SPPerson", "id");
    spNoNoPalTable.addForeignKey("receiverID", "SPPerson", "id");

    spNoNoPalTable.addUniqueIndex("uniqueNoNoPal", spNoNoPalTable.existingColumnNamed("eventID"), spNoNoPalTable.existingColumnNamed("giverID"), spNoNoPalTable.existingColumnNamed("receiverID"));
  }
}