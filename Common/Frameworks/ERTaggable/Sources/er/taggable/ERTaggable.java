package er.taggable;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXGenericRecord;
import er.extensions.ERXQ;
import er.taggable.model.ERTag;

/**
 * ERTaggable provides a wrapper around a taggable EO, extending it with
 * tagging-related methods.
 * 
 * Typically you would provide a cover method from your EO to an 
 * instance of an ERTaggable:
 * 
 * <code>
 * public class Person extends _Person {
 *   ...
 *   public ERTaggable<Person> taggable() {
 *     return ERTaggable.taggable(this);
 *   }
 * }
 * </code>
 * 
 * @author mschrag
 *
 * @param <T> the type of EO that is being wrapped
 */
public class ERTaggable<T extends ERXGenericRecord> {
  private T _item;
  private ERTaggableEntity<T> _entity;

  /**
   * Constructs an ERTaggable wrapper.
   * 
   * @param entity the ERTaggableEntity that corresponds to this item's entity  
   * @param item the item to wrap
   */
  public ERTaggable(ERTaggableEntity<T> entity, T item) {
    _entity = entity;
    _item = item;
  }

  /**
   * A factory method for generating a taggable from an EO.
   * 
   * @param <T> the type of the EO being wrapped
   * @param eo the EO being wrapped
   * @return a taggable wrapper around the EO
   */
  public static <T extends ERXGenericRecord> ERTaggable<T> taggable(T eo) {
    return new ERTaggableEntity<T>(eo.entity()).taggable(eo);
  }

  /**
   * Returns an array of ERTags associated with this item.
   * 
   * @return an array of ERTags associated with this item
   */
  @SuppressWarnings("unchecked")
  public NSArray<ERTag> tags() {
    String tagsRelationshipName = _entity.tagsRelationshipName();
    return (NSArray<ERTag>) _item.valueForKeyPath(tagsRelationshipName);
  }

  /**
   * Removes all of the tags associated with this item.
   */
  @SuppressWarnings("unchecked")
  public void clearTags() {
    String tagsRelationshipName = _entity.tagsRelationshipName();
    for (ERTag tag : tags().immutableClone()) {
      _item.removeObjectFromBothSidesOfRelationshipWithKey(tag, tagsRelationshipName);
    }
  }

  /**
   * This method removes tags from the target object, by parsing the tags parameter
   * into Tag object instances and removing them from the tag collection of the object if they exist.
   *
   * @param tags the tags to remove (String to tokenize, NSArray<String>, etc)
   */
  @SuppressWarnings("unchecked")
  public void removeTags(Object tags) {
    NSArray<ERTag> erTags = tags();
    String tagsRelationshipName = _entity.tagsRelationshipName();
    NSArray<ERTag> matchingTags = ERXQ.filtered(erTags, ERTag.NAME.in(_entity.splitTagNames(tags)));
    for (ERTag tag : matchingTags) {
      _item.removeObjectFromBothSidesOfRelationshipWithKey(tag, tagsRelationshipName);
    }
  }

  /**
   * This method applies tags to the target object, by parsing the tags parameter
   * into Tag object instances and adding them to the tag collection of the object.
   * If the tag name already exists in the tags table, it just adds a relationship
   * to the existing tag record. If it doesn't exist, it then creates a new
   * Tag record for it.
   * 
   * This is equivalent to addTags(false, tags). 
   *
   * @param tags the tags to add (String to tokenize, NSArray<String>, etc)
   */
  public void addTags(Object tags) {
    addTags(false, tags);
  }

  /**
   * This method applies tags to the target object, by parsing the tags parameter
   * into Tag object instances and adding them to the tag collection of the object.
   * If the tag name already exists in the tags table, it just adds a relationship
   * to the existing tag record. If it doesn't exist, it then creates a new
   * Tag record for it. 
   *
   * @param tags the tags to add (String to tokenize, NSArray<String>, etc)
   * @param clear if true, existing tags will be removed first
   */
  public void addTags(boolean clear, Object tags) {
    // clear the collection if appropriate
    if (clear) {
      clearTags();
    }

    String tagsRelationshipName = _entity.tagsRelationshipName();
    NSArray<ERTag> erTags = tags();
    EOEditingContext editingContext = _item.editingContext();
    // append the tag names to the collection
    for (String tagName : _entity.splitTagNames(tags)) {
      // ensure that tag names don't get duplicated
      ERTag tag = _entity.fetchTagNamed(editingContext, tagName, true);
      if (!erTags.containsObject(tag)) {
        _item.addObjectToBothSidesOfRelationshipWithKey(tag, tagsRelationshipName);
      }
    }
  }

  /**
   * Clears the current tags collection and sets the tag names for this object.
   * Equivalent of calling addTags(tags, true).
   *
   * @param tags the tags to add (String to tokenize, NSArray<String>, etc)
   */
  public void setTags(Object tags) {
    addTags(true, tags);
  }

  /**
   * Returns an array of strings containing the tag names applied to this object.
   * 
   * @return an array of strings containing the tag names applied to this object
   */
  @SuppressWarnings("unchecked")
  public NSArray<String> tagNames() {
    return (NSArray<String>) tags().valueForKey("name");
  }

  /**
   * Checks to see if this object has been tagged with the given tag name.
   * 
   * @param tagName the tag name to check
   * @return true if this eo is tagged with the given tag name, false otherwise
   */
  public boolean isTaggedWith(String tagName) {
    return ERXQ.filtered(tags(), ERTag.NAME.is(tagName)).count() > 0;
  }

  /**
   * Checks to see if this object has been tagged with all the given tags.
   * 
   * @param tags the tags to add (String to tokenize, NSArray<String>, etc)
   * @return true if this eo is tagged with all of the given tag names, false otherwise
   */
  @SuppressWarnings("unchecked")
  public boolean isTaggedWithAll(Object tags) {
    NSArray<String> tagNames = _entity.splitTagNames(tags);
    return ERXQ.filtered(tags(), ERTag.NAME.in(tagNames)).count() == tagNames.count();

  }

  /**
   * Checks to see if this object has been tagged with any of the given tags.
   * 
   * @param tags the tags to add (String to tokenize, NSArray<String>, etc)
   * @return true if this eo is tagged with any of the given tag names, false otherwise
   */
  @SuppressWarnings("unchecked")
  public boolean isTaggedWithAny(Object tags) {
    NSArray<String> tagNames = _entity.splitTagNames(tags);
    return ERXQ.filtered(tags(), ERTag.NAME.in(tagNames)).count() > 0;
  }

//  public NSArray<String> taggedRelated(ERTagOptions options) {
//    return _entity.findRelatedTagged(this, options);
//  }
}
