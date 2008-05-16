package er.ajax.example;

public class Item {
  private String _id;
  private String _name;

  public Item(String id, String name) {
    _id = id;
    _name = name;
  }

  public String name() {
    return _name;
  }

  public String id() {
    return _id;
  }

  public String toString() {
    return "[Item: id = " + _id + "]";
  }
}
