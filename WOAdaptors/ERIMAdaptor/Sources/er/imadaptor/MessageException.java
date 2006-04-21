package er.imadaptor;

public class MessageException extends InstantMessengerException {
  public MessageException(String _message) {
    super(_message);
  }

  public MessageException(String _message, Throwable _cause) {
    super(_message, _cause);
  }
}
