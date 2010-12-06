package er.chronic.utils;

public class Tick {
  private int _time;
  private boolean _ambiguous;

  public Tick(int time, boolean ambiguous) {
    _time = time;
    _ambiguous = ambiguous;
  }

  public boolean isAmbiguous() {
    return _ambiguous;
  }

  public void setTime(int time) {
    _time = time;
  }
  
  public Tick times(int other) {
    return new Tick(_time * other, _ambiguous);
  }

  public int intValue() {
    return _time;
  }
  
  public float floatValue() {
    return _time;
  }

  @Override
  public String toString() {
    return _time + (_ambiguous ? "?" : "");
  }
}
