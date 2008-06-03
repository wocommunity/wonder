package er.chronic.utils;

public class StringUtils {
  public static Integer integerValue(String str) {
    if (str != null) {
      if ("one".equalsIgnoreCase(str)) {
        return Integer.valueOf(1);
      }
      else if ("two".equalsIgnoreCase(str)) {
        return Integer.valueOf(2);
      }
      else if ("three".equalsIgnoreCase(str)) {
        return Integer.valueOf(3);
      }
      else if ("four".equalsIgnoreCase(str)) {
        return Integer.valueOf(4);
      }
      else if ("five".equalsIgnoreCase(str)) {
        return Integer.valueOf(5);
      }
      else if ("six".equalsIgnoreCase(str)) {
        return Integer.valueOf(6);
      }
      else if ("seven".equalsIgnoreCase(str)) {
        return Integer.valueOf(7);
      }
      else if ("eight".equalsIgnoreCase(str)) {
        return Integer.valueOf(8);
      }
      else if ("nine".equalsIgnoreCase(str)) {
        return Integer.valueOf(9);
      }
      else if ("ten".equalsIgnoreCase(str)) {
        return Integer.valueOf(10);
      }
      else if ("eleven".equalsIgnoreCase(str)) {
        return Integer.valueOf(11);
      }
      else if ("twelve".equalsIgnoreCase(str)) {
        return Integer.valueOf(12);
      }
    }
    return null;
  }
}
