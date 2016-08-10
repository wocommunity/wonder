package er.persistentsessionstorage.migrations;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;

public class ERPersistentSessionStorage0 extends ERXMigrationDatabase.Migration {
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
		ERXMigrationTable erSessionInfoTable = database.newTableNamed("ERSessionInfo");
		erSessionInfoTable.newTimestampColumn("expirationDate", false);
		erSessionInfoTable.newIntegerColumn("intLock", false);
		erSessionInfoTable.newBlobColumn("sessionData", false);
		erSessionInfoTable.newStringColumn("sessionID", 50, false);


		erSessionInfoTable.create();
	 	erSessionInfoTable.setPrimaryKey("sessionID");

	}
}