package er.ajax.example2.util;

import java.lang.reflect.InvocationTargetException;

import com.webobjects.foundation.NSForwardException;

public class ExceptionUtils {
  public static Throwable getMeaningfulException(Throwable t) {
    Throwable meaningfulThrowable;
    if (t instanceof NSForwardException) {
      meaningfulThrowable = ((NSForwardException)t).originalException();
    }
    else if (t instanceof InvocationTargetException) {
      meaningfulThrowable = ((InvocationTargetException)t).getCause();
    }
    else {
      meaningfulThrowable = t;
    }
    if (meaningfulThrowable != t) {
      meaningfulThrowable = ExceptionUtils.getMeaningfulException(meaningfulThrowable);
    }
    return meaningfulThrowable;
  }
}
