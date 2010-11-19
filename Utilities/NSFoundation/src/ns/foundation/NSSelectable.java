package ns.foundation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface NSSelectable {

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public @interface Selectable {
    
  }
}
