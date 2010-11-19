package ns.foundation.overriddenaccess;

public class OverriddenAccessClass {
  protected Integer knownField = null;
  Integer knownField2 = null;
  protected Integer knownMethod() { return knownField; }
  Integer knownMethod2() { return knownField2; }
  protected void setKnownMethod(Integer value) { knownField = value; }
  void setKnownMethod2(Integer value) { knownField2 = value; } 
}
