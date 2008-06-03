package er.chronic.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import er.chronic.tags.Tag;

public class Token {
  private String _word;
  private List<Tag<?>> _tags;

  public Token(String word) {
    _word = word;
    _tags = new LinkedList<Tag<?>>();
  }

  public String getWord() {
    return _word;
  }

  /**
   * Tag this token with the specified tag
   */
  public void tag(Tag<?> newTag) {
    _tags.add(newTag);
  }

  /**
   * Remove all tags of the given class
   */
  public void untag(Class<?> tagClass) {
    Iterator<Tag<?>> tagIter = _tags.iterator();
    while (tagIter.hasNext()) {
      Tag<?> tag = tagIter.next();
      if (tagClass.isInstance(tag)) {
        tagIter.remove();
      }
    }
  }

  /**
   * Return true if this token has any tags
   */
  public boolean isTagged() {
    return !_tags.isEmpty();
  }

  /**
   * Return the Tag that matches the given class
   */
  @SuppressWarnings("unchecked")
  public <T extends Tag> T getTag(Class<T> tagClass) {
    List<T> matches = getTags(tagClass);
    T matchingTag = null;
    if (matches.size() > 0) {
      matchingTag = matches.get(0);
    }
//    if (matches.size() >= 2) {
//      throw new IllegalStateException("Multiple identical tags found (" + matches + ")");
//    }
//    else if (matches.size() == 1) {
//      matchingTag = matches.get(0);
//    }
    return matchingTag;
  }

  public List<Tag<?>> getTags() {
    return _tags;
  }
  
  /**
   * Return the Tag that matches the given class
   */
  @SuppressWarnings("unchecked")
  public <T extends Tag<?>> List<T> getTags(Class<T> tagClass) {
    List<T> matches = new LinkedList<T>();
    Iterator<Tag<?>> tagIter = _tags.iterator();
    while (tagIter.hasNext()) {
      Tag<?> tag = tagIter.next();
      if (tagClass.isInstance(tag)) {
        matches.add((T)tag);
      }
    }
    return matches;
  }

  @Override
  public String toString() {
    return _word + " " + _tags;
  }
}
