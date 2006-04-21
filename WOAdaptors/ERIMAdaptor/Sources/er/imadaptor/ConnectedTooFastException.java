package er.imadaptor;

public class ConnectedTooFastException extends IMConnectionException {
  public ConnectedTooFastException(String _message) {
    super(_message);
  }

  public ConnectedTooFastException(String _message, Throwable _cause) {
    super(_message, _cause);
  }
}
