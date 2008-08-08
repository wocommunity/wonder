package er.ajax.example2.model;

import er.extensions.eof.ERXKey;

public class Product {
  public static final ERXKey<String> TITLE = new ERXKey<String>("title");
  public static final ERXKey<String> SUMMARY = new ERXKey<String>("title");

  private String _title;
  private String _summary;

  public Product(String title, String summary) {
    _title = title;
    _summary = summary;
  }

  public String title() {
    return _title;
  }

  public String summary() {
    return _summary;
  }

  public String partialSummary() {
    int length = Math.min(30, _summary.length());
    return _summary.substring(0, 30) + " ...";
  }

  @Override
  public String toString() {
    return "[Product: " + _title + "]";
  }
}
