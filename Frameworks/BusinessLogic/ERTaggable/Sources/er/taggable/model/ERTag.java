package er.taggable.model;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.eof.ERXEC;
import er.taggable.ERTaggableEntity;

/**
 * ERTag represents a single String shared tag.
 * 
 * @author mschrag
 */
public class ERTag extends _ERTag {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  /**
   * Inclusion provides an enum for ANY or ALL.
   * 
   * @author mschrag
   */
  public static enum Inclusion {
    ANY, ALL
  }

  @Override
  public void willDelete() throws ValidationException {
    untagAllTaggables();
    super.willDelete();
  }

  /**
   * Removes this tag from all taggables.  This is automatically
   * called prior to deleting an ERTag.
   */
  public void untagAllTaggables() {
    EOEditingContext editingContext = editingContext();
    for (EOEntity entity : ERTaggableEntity.taggableEntities()) {
      ERTaggableEntity<?> taggableEntity = ERTaggableEntity.taggableEntity(entity);
      taggableEntity.removeTags(editingContext, this);
    }
  }

  /**
   * Deletes the tag with the given name and removes the tag from all taggables.
   * 
   * @param tagName the tag name to delete
   */
  public static void deleteTagNamed(String tagName) {
    EOEditingContext editingContext = ERXEC.newEditingContext();
    ERTag.deleteTagNamed(editingContext, tagName);
    editingContext.saveChanges();
  }

  /**
   * Deletes the tag with the given name and removes the tag from all taggables.
   * 
   * @param editingContext the editing context to delete in
   * @param tagName the tag name to delete
   */
  public static void deleteTagNamed(EOEditingContext editingContext, String tagName) {
    ERTag tag = ERTag.fetchERTag(editingContext, ERTag.NAME_KEY, tagName);
    if (tag != null) {
      tag.delete();
    }
  }

  /**
   * Escapes the quotes inside the given tag name and defensively surrounds this tag with quotes.
   * 
   * @param tagName the tag name to escape
   * @return an escaped tag name
   */
  public static String escapeTagNamed(String tagName) {
    String escapedTagName = tagName;
    if (escapedTagName != null) {
      escapedTagName = escapedTagName.replaceAll("\"", "\\\\\"");
      if (escapedTagName.indexOf(' ') != -1) {
        escapedTagName = "\"" + escapedTagName + "\"";
      }
    }
    return escapedTagName;
  }
}
