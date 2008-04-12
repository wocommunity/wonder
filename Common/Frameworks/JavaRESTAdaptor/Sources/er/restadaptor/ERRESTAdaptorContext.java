package er.restadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSDictionary;

public class ERRESTAdaptorContext extends EOAdaptorContext {
  private boolean _hasTransaction;

  public ERRESTAdaptorContext(EOAdaptor adaptor) {
    super(adaptor);
  }

  @Override
  public NSDictionary _newPrimaryKey(EOEnterpriseObject object, EOEntity entity) {
    return null;
  }

  @Override
  public void beginTransaction() {
    if (!_hasTransaction) {
      _hasTransaction = true;
      transactionDidBegin();
    }
  }

  @Override
  public void commitTransaction() {
    if (_hasTransaction) {
      _hasTransaction = false;
      transactionDidCommit();
    }
  }

  @Override
  public EOAdaptorChannel createAdaptorChannel() {
    return new ERRESTAdaptorChannel(this);
  }

  @Override
  public void handleDroppedConnection() {
    /* empty */
  }

  @Override
  public void rollbackTransaction() {
    if (_hasTransaction) {
      _hasTransaction = false;
      transactionDidRollback();
    }
  }
}
