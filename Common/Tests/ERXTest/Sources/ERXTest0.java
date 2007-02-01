import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXJDBCUtilities;
import er.extensions.migration.IERXMigration;

public class ERXTest0 implements IERXMigration {
  public void downgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model) throws Throwable {
  }

  public NSArray modelDependencies() {
    return NSArray.EmptyArray;
  }

  public void upgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model) throws Throwable {
    ERXJDBCUtilities.createTablesForModel(channel, model);
  }
}
