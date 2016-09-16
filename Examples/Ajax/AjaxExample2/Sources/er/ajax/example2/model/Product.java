package er.ajax.example2.model;

import java.util.UUID;

import er.extensions.eof.ERXKey;

public class Product {
  public static final ERXKey<String> TITLE = new ERXKey<>("title");
  public static final ERXKey<String> SUMMARY = new ERXKey<>("title");

  private String _id;
  private String _title;
  private String _summary;

  public Product(String title, String summary) {
    _id = "Product" + UUID.randomUUID().toString();
    _title = title;
    _summary = summary;
  }
  
  public String id() {
    return _id;
  }

  public String title() {
    return _title;
  }
  
  public void setTitle(String title) {
    _title = title;
  }

  public String summary() {
    return _summary;
  }
  
  public void setSummary(String summary) {
    _summary = summary;
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
