package er.taggable;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXGenericRecord;
import er.extensions.eof.ERXQ;
import er.taggable.model.ERTag;

/**
 * ERTaggable provides a wrapper around a taggable EO, extending it with
 * tagging-related methods.
 * 
 * Typically you would provide a cover method from your EO to an 
 * instance of an ERTaggable:
 * 
 * <pre><code>
 * public class Person extends _Person {
 *   ...
 *   public ERTaggable&lt;Person&gt; taggable() {
 *     return ERTaggable.taggable(this);
 *   }
 * }
 * </code></pre>
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
  protected ERTaggable(ERTaggableEntity<T> entity, T item) {
    _entity = entity;
    _item = item;
  }
  
  @Override
  public int hashCode() {
    return _item.hashCode(); 
  }
  
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof ERTaggable && ((ERTaggable<?>)obj)._item.equals(_item)); 
  }

  /**
   * A factory method for generating a taggable from an EO.
   * 
   * @param <T> the type of the EO being wrapped
   * @param eo the EO being wrapped
   * @return a taggable wrapper around the EO
   */
  public static <T extends ERXGenericRecord> ERTaggable<T> taggable(T eo) {
    return ERTaggableEntity.taggableEntity(eo).taggable(eo);
  }

  /**
   * Returns the tagged item that this is taggable is wrapping.
   *  
   * @return the tagged item
   */
  public T item() {
    return _item;
  }

  /**
   * Returns the taggable entity for this taggable.
   * 
   * @return the taggable entity for this taggable
   */
  public ERTaggableEntity<T> taggableEntity() {
    return _entity;
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
  public void clearTags() {
    for (ERTag tag : tags().immutableClone()) {
      removeTag(tag);
    }
  }

  /**
   * This method removes tags from the target object, by parsing the tags parameter
   * into Tag object instances and removing them from the tag collection of the object if they exist.
   *
   * @param tags the tags to remove (String to tokenize, NSArray&lt;String&gt;, etc)
   */
  public void removeTags(Object tags) {
    NSArray<ERTag> erTags = tags();
    NSArray<ERTag> matchingTags = ERXQ.filtered(erTags, ERTag.NAME.in(_entity.splitTagNames(tags)));
    for (ERTag tag : matchingTags) {
      removeTag(tag);
    }
  }

  /**
   * This method removes tags from the target object, by looking up the corresponding 
   * Tag object instances and removing them from the tag collection of the object if they exist.
   *
   * @param tagName the tag to remove (String to tokenize, NSArray&lt;String&gt;, etc)
   */
  public void removeTagNamed(String tagName) {
    NSArray<ERTag> erTags = tags();
    NSArray<ERTag> matchingTags = ERXQ.filtered(erTags, ERTag.NAME.is(tagName));
    for (ERTag tag : matchingTags) {
      removeTag(tag);
    }
  }
  
  /**
   * This method applies tags to the target object, by looking up the corresponding
   * Tag object instances and adding it to the tag collection of the object.
   * If the tag name already exists in the tags table, it just adds a relationship
   * to the existing tag record. If it doesn't exist, it then creates a new
   * Tag record for it.
   * 
   * This is equivalent to addTags(false, tags). 
   *
   * @param tagName the tag name to add
   */
  public void addTagNamed(String tagName) {
    addTags(false, ERTag.escapeTagNamed(tagName));
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
   * @param tags the tags to add (String to tokenize, NSArray&lt;String&gt;, etc)
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
   * @param tags the tags to add (String to tokenize, NSArray&lt;String&gt;, etc)
   * @param clear if true, existing tags will be removed first
   */
  public void addTags(boolean clear, Object tags) {
    // clear the collection if appropriate
    if (clear) {
      clearTags();
    }

    NSArray<ERTag> erTags = tags();
    EOEditingContext editingContext = _item.editingContext();
    // append the tag names to the collection
    for (String tagName : _entity.splitTagNames(tags)) {
      // ensure that tag names don't get duplicated
      ERTag tag = _entity.fetchTagNamed(editingContext, tagName, true);
      if (!erTags.containsObject(tag)) {
        addTag(tag);
      }
    }
  }

  /**
   * Adds the tag to this item.  This is the single method that to override
   * if you need to perform some additional operations.
   * 
   * @param tag the tag to add
   */
  protected void addTag(ERTag tag) {
    _item.addObjectToBothSidesOfRelationshipWithKey(tag, _entity.tagsRelationshipName());
  }

  /**
   * Removes the tag from this item.  This is the single method that to override
   * if you need to perform some additional operations.
   * 
   * @param tag the tag to remove
   */
  protected void removeTag(ERTag tag) {
    _item.removeObjectFromBothSidesOfRelationshipWithKey(tag, _entity.tagsRelationshipName());
  }

  /**
   * Clears the current tags collection and sets the tag names for this object.
   * Equivalent of calling addTags(tags, true).
   *
   * @param tags the tags to add (String to tokenize, NSArray&lt;String&gt;, etc)
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
   * @param tags the tags to add (String to tokenize, NSArray&lt;String&gt;, etc)
   * @return true if this eo is tagged with all of the given tag names, false otherwise
   */
  public boolean isTaggedWithAll(Object tags) {
    NSArray<String> tagNames = _entity.splitTagNames(tags);
    return ERXQ.filtered(tags(), ERTag.NAME.in(tagNames)).count() == tagNames.count();

  }

  /**
   * Checks to see if this object has been tagged with any of the given tags.
   * 
   * @param tags the tags to add (String to tokenize, NSArray&lt;String&gt;, etc)
   * @return true if this eo is tagged with any of the given tag names, false otherwise
   */
  public boolean isTaggedWithAny(Object tags) {
    NSArray<String> tagNames = _entity.splitTagNames(tags);
    return ERXQ.filtered(tags(), ERTag.NAME.in(tagNames)).count() > 0;
  }

//  public NSArray<String> taggedRelated(ERTagOptions options) {
//    return _entity.findRelatedTagged(this, options);
//  }
}
