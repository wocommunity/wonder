package er.chronic.handlers;

public class HandlerTypePattern extends HandlerPattern {
  private Handler.HandlerType _type;

  public HandlerTypePattern(Handler.HandlerType type) {
    this(type, false);
  }
  
  public HandlerTypePattern(Handler.HandlerType type, boolean optional) {
    super(optional);
    _type = type;
  }

  public Handler.HandlerType getType() {
    return _type;
  }
}
