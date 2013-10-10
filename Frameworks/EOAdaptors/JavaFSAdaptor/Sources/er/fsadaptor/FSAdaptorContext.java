package er.fsadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;

public final class FSAdaptorContext extends EOAdaptorContext {
    private boolean _hasTransaction = false;

    public FSAdaptorContext(EOAdaptor anAdaptor) {
        super(anAdaptor);
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
        return new FSAdaptorChannel(this);
    }

    @Override
    public void handleDroppedConnection() {
        /* empty */
    }

    @Override
    public void rollbackTransaction() {
        throw new UnsupportedOperationException("FSAdaptorContext.rollbackTransaction");
    }
}
