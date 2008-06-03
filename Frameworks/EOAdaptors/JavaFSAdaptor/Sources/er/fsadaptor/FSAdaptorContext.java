
/* FSAdaptorContext - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package er.fsadaptor;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;

public final class FSAdaptorContext extends EOAdaptorContext {
    private boolean _hasTransaction = false;

    public FSAdaptorContext(EOAdaptor anAdaptor) {
        super(anAdaptor);
    }

    public void beginTransaction() {
        if (!_hasTransaction) {
            _hasTransaction = true;
            this.transactionDidBegin();
        }
    }

    public void commitTransaction() {
        if (_hasTransaction == true) {
            _hasTransaction = false;
            this.transactionDidCommit();
        }
    }

    public EOAdaptorChannel createAdaptorChannel() {
        return new FSAdaptorChannel(this);
    }

    public void handleDroppedConnection() {
        /* empty */
    }

    public void rollbackTransaction() {
        throw new UnsupportedOperationException("FSAdaptorContext.rollbackTransaction");
    }
}
