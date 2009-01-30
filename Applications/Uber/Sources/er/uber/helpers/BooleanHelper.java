package er.uber.helpers;

public class BooleanHelper {
  public String yesNo(boolean value) {
    return value ? "yes" : "no";
  }

  public String yesNo(boolean value, String yesValue, String noValue) {
    return value ? yesValue : noValue;
  }
}
