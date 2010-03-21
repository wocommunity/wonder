package er.bugtracker.migrations;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.BTBusinessLogic;
import er.bugtracker.BTDataCreator;
import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXModelVersion;
import er.extensions.migration.IERXPostMigration;

public class BugTracker1 extends ERXMigrationDatabase.Migration implements IERXPostMigration {
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
  }

  public void postUpgrade(EOEditingContext editingContext, EOModel model) throws Throwable {
    BTBusinessLogic.initializeSharedData();
    new BTDataCreator(editingContext).createDummyData();
  }
}