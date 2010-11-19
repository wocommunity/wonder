package ns.foundation.protectedaccess;

public class RestrictedClass {
  protected Integer knownField = 42;
  Integer knownField2 = 42;
  protected Integer knownMethod() { return 42; }
  Integer knownMethod2() { return 42; }
}
