package er.uber.helpers;

public class StringHelper {
  public String nullTest(String str) {
    return str == null ? "Replaced Null" : str;
  }
  
  public String reverse(String str) {
    StringBuffer reverse = new StringBuffer();
    for (int i = str.length() - 1; i >= 0; i--) {
      reverse.append(str.charAt(i));
    }
    return reverse.toString();
  }
}
