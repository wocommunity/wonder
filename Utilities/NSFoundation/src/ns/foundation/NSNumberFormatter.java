package ns.foundation;

import java.text.DecimalFormat;

public class NSNumberFormatter extends DecimalFormat {
  public NSNumberFormatter() {
    super();
  }

  public NSNumberFormatter(String aPattern) {
    super(aPattern);
  }
}
