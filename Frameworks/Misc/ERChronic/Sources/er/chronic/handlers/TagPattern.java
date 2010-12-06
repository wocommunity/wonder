package er.chronic.handlers;

import er.chronic.tags.Tag;

@SuppressWarnings("unchecked")
public class TagPattern extends HandlerPattern {
  private Class<? extends Tag> _tagClass;

  public TagPattern(Class<? extends Tag> tagClass) {
    this(tagClass, false);
  }
  
  public TagPattern(Class<? extends Tag> tagClass, boolean optional) {
    super(optional);
    _tagClass = tagClass;
  }
  
  public Class<? extends Tag> getTagClass() {
    return _tagClass;
  }
  
  @Override
  public String toString() {
    return "[TagPattern: tagClass = " + _tagClass + "]";
  }
}
