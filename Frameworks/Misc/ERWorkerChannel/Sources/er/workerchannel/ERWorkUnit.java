//
// ERWorkUnit.java
// Project ERWorkerChannel
//
// Created by tatsuya on Mon Jul 29 2002
//
package er.workerchannel;

public abstract class ERWorkUnit {

    private ERFutureResult _futureResult;
    private boolean _wasDispatched = false;
    
    protected ERFutureResult futureResult() {
        return _futureResult;
    }
    
    protected void setFutureResult(ERFutureResult futureResult) {
        _futureResult = futureResult;
    }
    
    public boolean wasDispatched() {
        return _wasDispatched;
    }
    
    protected void setWasDispatched(boolean wasDispatched) {
        _wasDispatched = wasDispatched;
    }
    
    public abstract void execute();

}
