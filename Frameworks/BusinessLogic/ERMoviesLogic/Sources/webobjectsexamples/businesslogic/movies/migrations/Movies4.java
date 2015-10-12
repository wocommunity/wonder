package webobjectsexamples.businesslogic.movies.migrations;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;

public class Movies4 extends ERXMigrationDatabase.Migration {
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
        ERXMigrationTable appUserTable = database.newTableNamed("app_user");
        appUserTable.newIntegerColumn("APP_USER_ID", 9, NOT_NULL);
        appUserTable.newStringColumn("USER_NAME", 50, NOT_NULL);
        appUserTable.create();
        appUserTable.setPrimaryKey("APP_USER_ID");
    }
}