package org.ganymede.migrations;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;

public class WOTested0 extends ERXMigrationDatabase.Migration {
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
    ERXMigrationTable envJoinTable = database.newTableNamed("env_joins");
    envJoinTable.newIntegerColumn("env_pk", false);
    envJoinTable.newIntegerColumn("result_pk", false);
    envJoinTable.create();
    envJoinTable.setPrimaryKey("env_pk", "result_pk");

    ERXMigrationTable environmentTable = database.newTableNamed("environments");
    environmentTable.newStringColumn("info", 64, false);
    environmentTable.newIntegerColumn("pk", false);
    environmentTable.create();
    environmentTable.setPrimaryKey("pk");

    ERXMigrationTable failureTable = database.newTableNamed("failures");
    failureTable.newStringColumn("message", 511, false);
    failureTable.newIntegerColumn("pk", false);
    failureTable.newIntegerColumn("result_pk", false);
    failureTable.create();
    failureTable.setPrimaryKey("pk");

    ERXMigrationTable resultTable = database.newTableNamed("results");
    resultTable.newBigIntegerColumn("duration", false);
    resultTable.newStringColumn("email", 127, false);
    resultTable.newIntegerColumn("pk", false);
    resultTable.newStringColumn("time_zone", 32, false);
    resultTable.newStringColumn("whence", 19, false);
    resultTable.create();
    resultTable.setPrimaryKey("pk");

    ERXMigrationTable versionDigestTable = database.newTableNamed("version_digests");
    versionDigestTable.newStringColumn("digest", 40, false);
    versionDigestTable.newIntegerColumn("pk", false);
    versionDigestTable.newIntegerColumn("result_pk", false);
    versionDigestTable.newStringColumn("rname", 255, false);
    versionDigestTable.create();
    versionDigestTable.setPrimaryKey("pk");

    envJoinTable.addForeignKey("env_pk", "environments", "pk");
    envJoinTable.addForeignKey("result_pk", "results", "pk");
    failureTable.addForeignKey("result_pk", "results", "pk");
    versionDigestTable.addForeignKey("result_pk", "results", "pk");
  }
}