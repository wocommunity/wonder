package er.ajax.example2.util;

import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.eof.ERXEOControlUtilities;

public class ComparisonUtils {
  public static boolean notEquals(Object obj1, Object obj2) {
    return !ComparisonUtils.equals(obj1, obj2);
  }
  
  public static boolean equals(Object obj1, Object obj2) {
    boolean equals;
    if (obj1 == obj2) {
      equals = true;
    }
    else if (obj1 != null) {
      if (obj1 instanceof EOEnterpriseObject && obj2 instanceof EOEnterpriseObject) {
        equals = ERXEOControlUtilities.eoEquals((EOEnterpriseObject)obj1, (EOEnterpriseObject)obj2);
      }
      else {
        equals = obj1.equals(obj2);
      }
    }
    else {
      equals = (obj2 == null);
    }
    return equals;
  }

  public static boolean empty(String str) {
    return ComparisonUtils.empty(str, false);
  }

  public static boolean empty(String str, boolean trim) {
    boolean empty = str == null || str.length() == 0 || (trim && str.trim().length() == 0);
    return empty;
  }

  public static boolean notEmpty(String str) {
    return !ComparisonUtils.empty(str);
  }

  public static boolean notEmpty(String str, boolean trim) {
    return !ComparisonUtils.empty(str, trim);
  }
}
