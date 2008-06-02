// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Forum.java instead.
package se.caboo.beast.model;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXGenericRecord;
import er.extensions.eof.ERXKey;

@SuppressWarnings("all")
public abstract class _Forum extends  ERXGenericRecord {
	public static final String ENTITY_NAME = "Forum";

	// Attributes
	public static final String DESCRIPTION_KEY = "description";
	public static final ERXKey<String> DESCRIPTION = new ERXKey<String>(DESCRIPTION_KEY);
	public static final String DESCRIPTION_HTML_KEY = "descriptionHtml";
	public static final ERXKey<String> DESCRIPTION_HTML = new ERXKey<String>(DESCRIPTION_HTML_KEY);
	public static final String NAME_KEY = "name";
	public static final ERXKey<String> NAME = new ERXKey<String>(NAME_KEY);
	public static final String POSITION_KEY = "position";
	public static final ERXKey<Integer> POSITION = new ERXKey<Integer>(POSITION_KEY);
	public static final String POSTS_COUNT_KEY = "postsCount";
	public static final ERXKey<Integer> POSTS_COUNT = new ERXKey<Integer>(POSTS_COUNT_KEY);
	public static final String TOPICS_COUNT_KEY = "topicsCount";
	public static final ERXKey<Integer> TOPICS_COUNT = new ERXKey<Integer>(TOPICS_COUNT_KEY);

	// Relationships
	public static final String POSTS_KEY = "posts";
	public static final ERXKey<se.caboo.beast.model.Post> POSTS = new ERXKey<se.caboo.beast.model.Post>(POSTS_KEY);
	public static final String TOPICS_KEY = "topics";
	public static final ERXKey<se.caboo.beast.model.Topic> TOPICS = new ERXKey<se.caboo.beast.model.Topic>(TOPICS_KEY);

  private static Logger LOG = Logger.getLogger(_Forum.class);

  public Forum localInstanceIn(EOEditingContext editingContext) {
    Forum localInstance = (Forum)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String description() {
    return (String) storedValueForKey("description");
  }

  public void setDescription(String value) {
    if (_Forum.LOG.isDebugEnabled()) {
    	_Forum.LOG.debug( "updating description from " + description() + " to " + value);
    }
    takeStoredValueForKey(value, "description");
  }

  public String descriptionHtml() {
    return (String) storedValueForKey("descriptionHtml");
  }

  public void setDescriptionHtml(String value) {
    if (_Forum.LOG.isDebugEnabled()) {
    	_Forum.LOG.debug( "updating descriptionHtml from " + descriptionHtml() + " to " + value);
    }
    takeStoredValueForKey(value, "descriptionHtml");
  }

  public String name() {
    return (String) storedValueForKey("name");
  }

  public void setName(String value) {
    if (_Forum.LOG.isDebugEnabled()) {
    	_Forum.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, "name");
  }

  public Integer position() {
    return (Integer) storedValueForKey("position");
  }

  public void setPosition(Integer value) {
    if (_Forum.LOG.isDebugEnabled()) {
    	_Forum.LOG.debug( "updating position from " + position() + " to " + value);
    }
    takeStoredValueForKey(value, "position");
  }

  public Integer postsCount() {
    return (Integer) storedValueForKey("postsCount");
  }

  public void setPostsCount(Integer value) {
    if (_Forum.LOG.isDebugEnabled()) {
    	_Forum.LOG.debug( "updating postsCount from " + postsCount() + " to " + value);
    }
    takeStoredValueForKey(value, "postsCount");
  }

  public Integer topicsCount() {
    return (Integer) storedValueForKey("topicsCount");
  }

  public void setTopicsCount(Integer value) {
    if (_Forum.LOG.isDebugEnabled()) {
    	_Forum.LOG.debug( "updating topicsCount from " + topicsCount() + " to " + value);
    }
    takeStoredValueForKey(value, "topicsCount");
  }

  public NSArray<se.caboo.beast.model.Post> posts() {
    return (NSArray<se.caboo.beast.model.Post>)storedValueForKey("posts");
  }

  public NSArray<se.caboo.beast.model.Post> posts(EOQualifier qualifier) {
    return posts(qualifier, null, false);
  }

  public NSArray<se.caboo.beast.model.Post> posts(EOQualifier qualifier, boolean fetch) {
    return posts(qualifier, null, fetch);
  }

  public NSArray<se.caboo.beast.model.Post> posts(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<se.caboo.beast.model.Post> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(se.caboo.beast.model.Post.FORUM_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = se.caboo.beast.model.Post.fetchPosts(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = posts();
      if (qualifier != null) {
        results = (NSArray<se.caboo.beast.model.Post>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<se.caboo.beast.model.Post>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToPosts(se.caboo.beast.model.Post object) {
    includeObjectIntoPropertyWithKey(object, "posts");
  }

  public void removeFromPosts(se.caboo.beast.model.Post object) {
    excludeObjectFromPropertyWithKey(object, "posts");
  }

  public void addToPostsRelationship(se.caboo.beast.model.Post object) {
    if (_Forum.LOG.isDebugEnabled()) {
      _Forum.LOG.debug("adding " + object + " to posts relationship");
    }
    if (ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToPosts(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, "posts");
    }
  }

  public void removeFromPostsRelationship(se.caboo.beast.model.Post object) {
    if (_Forum.LOG.isDebugEnabled()) {
      _Forum.LOG.debug("removing " + object + " from posts relationship");
    }
    if (ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromPosts(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, "posts");
    }
  }

  public se.caboo.beast.model.Post createPostsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Post");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "posts");
    return (se.caboo.beast.model.Post) eo;
  }

  public void deletePostsRelationship(se.caboo.beast.model.Post object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "posts");
    editingContext().deleteObject(object);
  }

  public void deleteAllPostsRelationships() {
    Enumeration objects = posts().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deletePostsRelationship((se.caboo.beast.model.Post)objects.nextElement());
    }
  }

  public NSArray<se.caboo.beast.model.Topic> topics() {
    return (NSArray<se.caboo.beast.model.Topic>)storedValueForKey("topics");
  }

  public NSArray<se.caboo.beast.model.Topic> topics(EOQualifier qualifier) {
    return topics(qualifier, null, false);
  }

  public NSArray<se.caboo.beast.model.Topic> topics(EOQualifier qualifier, boolean fetch) {
    return topics(qualifier, null, fetch);
  }

  public NSArray<se.caboo.beast.model.Topic> topics(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<se.caboo.beast.model.Topic> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(se.caboo.beast.model.Topic.FORUM_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = se.caboo.beast.model.Topic.fetchTopics(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = topics();
      if (qualifier != null) {
        results = (NSArray<se.caboo.beast.model.Topic>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<se.caboo.beast.model.Topic>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToTopics(se.caboo.beast.model.Topic object) {
    includeObjectIntoPropertyWithKey(object, "topics");
  }

  public void removeFromTopics(se.caboo.beast.model.Topic object) {
    excludeObjectFromPropertyWithKey(object, "topics");
  }

  public void addToTopicsRelationship(se.caboo.beast.model.Topic object) {
    if (_Forum.LOG.isDebugEnabled()) {
      _Forum.LOG.debug("adding " + object + " to topics relationship");
    }
    if (ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToTopics(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, "topics");
    }
  }

  public void removeFromTopicsRelationship(se.caboo.beast.model.Topic object) {
    if (_Forum.LOG.isDebugEnabled()) {
      _Forum.LOG.debug("removing " + object + " from topics relationship");
    }
    if (ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromTopics(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, "topics");
    }
  }

  public se.caboo.beast.model.Topic createTopicsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Topic");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "topics");
    return (se.caboo.beast.model.Topic) eo;
  }

  public void deleteTopicsRelationship(se.caboo.beast.model.Topic object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "topics");
    editingContext().deleteObject(object);
  }

  public void deleteAllTopicsRelationships() {
    Enumeration objects = topics().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteTopicsRelationship((se.caboo.beast.model.Topic)objects.nextElement());
    }
  }


  public static Forum createForum(EOEditingContext editingContext, String description
, String descriptionHtml
, String name
, Integer position
, Integer postsCount
, Integer topicsCount
) {
    Forum eo = (Forum) EOUtilities.createAndInsertInstance(editingContext, _Forum.ENTITY_NAME);    
		eo.setDescription(description);
		eo.setDescriptionHtml(descriptionHtml);
		eo.setName(name);
		eo.setPosition(position);
		eo.setPostsCount(postsCount);
		eo.setTopicsCount(topicsCount);
    return eo;
  }

  public static NSArray<Forum> fetchAllForums(EOEditingContext editingContext) {
    return _Forum.fetchAllForums(editingContext, null);
  }

  public static NSArray<Forum> fetchAllForums(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Forum.fetchForums(editingContext, null, sortOrderings);
  }

  public static NSArray<Forum> fetchForums(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Forum.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Forum> eoObjects = (NSArray<Forum>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static Forum fetchForum(EOEditingContext editingContext, String keyName, Object value) {
    return _Forum.fetchForum(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Forum fetchForum(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Forum> eoObjects = _Forum.fetchForums(editingContext, qualifier, null);
    Forum eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (Forum)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Forum that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Forum fetchRequiredForum(EOEditingContext editingContext, String keyName, Object value) {
    return _Forum.fetchRequiredForum(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Forum fetchRequiredForum(EOEditingContext editingContext, EOQualifier qualifier) {
    Forum eoObject = _Forum.fetchForum(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Forum that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Forum localInstanceIn(EOEditingContext editingContext, Forum eo) {
    Forum localInstance = (eo == null) ? null : (Forum)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
