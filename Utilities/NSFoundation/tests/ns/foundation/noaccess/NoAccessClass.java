package ns.foundation.noaccess;

public class NoAccessClass {
  protected Integer knownField = 42;
  Integer knownField2 = 42;
  protected Integer knownMethod() { return 42; }
  Integer knownMethod2() { return 42; }
}
