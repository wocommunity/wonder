package er.ajax.mootools.example.components;

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

  @Override
public String toString() {
    return "[Item: id = " + _id + "]";
  }
}
