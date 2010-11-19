package ns.foundation;

import java.text.SimpleDateFormat;

public class NSTimestampFormatter extends SimpleDateFormat {
  public NSTimestampFormatter() {
    super("z' 'yyyy'-'MM'-'dd' 'HH':'mm':'ss");
  }

  public NSTimestampFormatter(String aPattern) {
    super(aPattern);
  }
}
