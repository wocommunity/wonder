package ns.foundation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class NSForwardException extends RuntimeException
{
  public static RuntimeException _runtimeExceptionForThrowable(Throwable throwable)
  {
    if (throwable == null)
      return null;
    if (throwable instanceof RuntimeException)
      return ((RuntimeException)throwable);
    return new NSForwardException(throwable);
  }

  public static Throwable _originalThrowable(Throwable throwable) {
    if (throwable == null)
      return null;
    if (throwable instanceof NSForwardException)
      return ((NSForwardException)throwable).originalException();
    return throwable;
  }

  public NSForwardException(Throwable wrapped, String extraMessage)
  {
    super(extraMessage, _originalThrowable(wrapped));
  }

  public NSForwardException(String message, Throwable cause)
  {
    super(message, _originalThrowable(cause));
  }

  public NSForwardException(Throwable wrapped)
  {
    super(wrapped);
  }

  public Throwable originalException()
  {
    return getCause();
  }

  public String stackTrace()
  {
    final StringBuffer sb = new StringBuffer();
    OutputStream out = new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        sb.append((byte)b & 0xff);
      }
    };
    getCause().printStackTrace(new PrintStream(out));
    return sb.toString();
  }

  @Override
  public String toString()
  {
    return getClass().getName() + " [" + getCause().getClass().getName() + "] " + getCause().getMessage() + ":" + getMessage();
  }
}