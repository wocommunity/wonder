// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Topic.java instead.
package se.caboo.beast.model;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.ERXGenericRecord;
import er.extensions.ERXKey;

@SuppressWarnings("all")
public abstract class _Topic extends  ERXGenericRecord {
	public static final String ENTITY_NAME = "Topic";

	// Attributes
	public static final String CREATED_AT_KEY = "createdAt";
	public static final ERXKey<NSTimestamp> CREATED_AT = new ERXKey<NSTimestamp>(CREATED_AT_KEY);
	public static final String HITS_KEY = "hits";
	public static final ERXKey<Integer> HITS = new ERXKey<Integer>(HITS_KEY);
	public static final String LOCKED_KEY = "locked";
	public static final ERXKey<java.lang.Boolean> LOCKED = new ERXKey<java.lang.Boolean>(LOCKED_KEY);
	public static final String POSTS_COUNT_KEY = "postsCount";
	public static final ERXKey<Integer> POSTS_COUNT = new ERXKey<Integer>(POSTS_COUNT_KEY);
	public static final String REPLIED_AT_KEY = "repliedAt";
	public static final ERXKey<NSTimestamp> REPLIED_AT = new ERXKey<NSTimestamp>(REPLIED_AT_KEY);
	public static final String STICKY_KEY = "sticky";
	public static final ERXKey<Integer> STICKY = new ERXKey<Integer>(STICKY_KEY);
	public static final String TITLE_KEY = "title";
	public static final ERXKey<String> TITLE = new ERXKey<String>(TITLE_KEY);
	public static final String UPDATED_AT_KEY = "updatedAt";
	public static final ERXKey<NSTimestamp> UPDATED_AT = new ERXKey<NSTimestamp>(UPDATED_AT_KEY);

	// Relationships
	public static final String FORUM_KEY = "forum";
	public static final ERXKey<se.caboo.beast.model.Forum> FORUM = new ERXKey<se.caboo.beast.model.Forum>(FORUM_KEY);
	public static final String POSTS_KEY = "posts";
	public static final ERXKey<se.caboo.beast.model.Post> POSTS = new ERXKey<se.caboo.beast.model.Post>(POSTS_KEY);
	public static final String REPLIED_BY_KEY = "repliedBy";
	public static final ERXKey<se.caboo.beast.model.User> REPLIED_BY = new ERXKey<se.caboo.beast.model.User>(REPLIED_BY_KEY);
	public static final String USER_KEY = "user";
	public static final ERXKey<se.caboo.beast.model.User> USER = new ERXKey<se.caboo.beast.model.User>(USER_KEY);

  private static Logger LOG = Logger.getLogger(_Topic.class);

  public Topic localInstanceIn(EOEditingContext editingContext) {
    Topic localInstance = (Topic)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public NSTimestamp createdAt() {
    return (NSTimestamp) storedValueForKey("createdAt");
  }

  public void setCreatedAt(NSTimestamp value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating createdAt from " + createdAt() + " to " + value);
    }
    takeStoredValueForKey(value, "createdAt");
  }

  public Integer hits() {
    return (Integer) storedValueForKey("hits");
  }

  public void setHits(Integer value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating hits from " + hits() + " to " + value);
    }
    takeStoredValueForKey(value, "hits");
  }

  public java.lang.Boolean locked() {
    return (java.lang.Boolean) storedValueForKey("locked");
  }

  public void setLocked(java.lang.Boolean value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating locked from " + locked() + " to " + value);
    }
    takeStoredValueForKey(value, "locked");
  }

  public Integer postsCount() {
    return (Integer) storedValueForKey("postsCount");
  }

  public void setPostsCount(Integer value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating postsCount from " + postsCount() + " to " + value);
    }
    takeStoredValueForKey(value, "postsCount");
  }

  public NSTimestamp repliedAt() {
    return (NSTimestamp) storedValueForKey("repliedAt");
  }

  public void setRepliedAt(NSTimestamp value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating repliedAt from " + repliedAt() + " to " + value);
    }
    takeStoredValueForKey(value, "repliedAt");
  }

  public Integer sticky() {
    return (Integer) storedValueForKey("sticky");
  }

  public void setSticky(Integer value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating sticky from " + sticky() + " to " + value);
    }
    takeStoredValueForKey(value, "sticky");
  }

  public String title() {
    return (String) storedValueForKey("title");
  }

  public void setTitle(String value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating title from " + title() + " to " + value);
    }
    takeStoredValueForKey(value, "title");
  }

  public NSTimestamp updatedAt() {
    return (NSTimestamp) storedValueForKey("updatedAt");
  }

  public void setUpdatedAt(NSTimestamp value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating updatedAt from " + updatedAt() + " to " + value);
    }
    takeStoredValueForKey(value, "updatedAt");
  }

  public se.caboo.beast.model.Forum forum() {
    return (se.caboo.beast.model.Forum)storedValueForKey("forum");
  }
  
  public void setForum(se.caboo.beast.model.Forum value) {
    takeStoredValueForKey(value, "forum");
  }

  public void setForumRelationship(se.caboo.beast.model.Forum value) {
    if (_Topic.LOG.isDebugEnabled()) {
      _Topic.LOG.debug("updating forum from " + forum() + " to " + value);
    }
    if (er.extensions.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setForum(value);
    }
    else if (value == null) {
    	se.caboo.beast.model.Forum oldValue = forum();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "forum");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "forum");
    }
  }
  
  public se.caboo.beast.model.User repliedBy() {
    return (se.caboo.beast.model.User)storedValueForKey("repliedBy");
  }
  
  public void setRepliedBy(se.caboo.beast.model.User value) {
    takeStoredValueForKey(value, "repliedBy");
  }

  public void setRepliedByRelationship(se.caboo.beast.model.User value) {
    if (_Topic.LOG.isDebugEnabled()) {
      _Topic.LOG.debug("updating repliedBy from " + repliedBy() + " to " + value);
    }
    if (er.extensions.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setRepliedBy(value);
    }
    else if (value == null) {
    	se.caboo.beast.model.User oldValue = repliedBy();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "repliedBy");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "repliedBy");
    }
  }
  
  public se.caboo.beast.model.User user() {
    return (se.caboo.beast.model.User)storedValueForKey("user");
  }
  
  public void setUser(se.caboo.beast.model.User value) {
    takeStoredValueForKey(value, "user");
  }

  public void setUserRelationship(se.caboo.beast.model.User value) {
    if (_Topic.LOG.isDebugEnabled()) {
      _Topic.LOG.debug("updating user from " + user() + " to " + value);
    }
    if (er.extensions.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setUser(value);
    }
    else if (value == null) {
    	se.caboo.beast.model.User oldValue = user();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "user");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "user");
    }
  }
  
  public NSArray<se.caboo.beast.model.Post> posts() {
    return (NSArray<se.caboo.beast.model.Post>)storedValueForKey("posts");
  }

  public NSArray<se.caboo.beast.model.Post> posts(EOQualifier qualifier) {
    return posts(qualifier, null);
  }

  public NSArray<se.caboo.beast.model.Post> posts(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<se.caboo.beast.model.Post> results;
      results = posts();
      if (qualifier != null) {
        results = (NSArray<se.caboo.beast.model.Post>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<se.caboo.beast.model.Post>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
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
    if (_Topic.LOG.isDebugEnabled()) {
      _Topic.LOG.debug("adding " + object + " to posts relationship");
    }
    if (er.extensions.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToPosts(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, "posts");
    }
  }

  public void removeFromPostsRelationship(se.caboo.beast.model.Post object) {
    if (_Topic.LOG.isDebugEnabled()) {
      _Topic.LOG.debug("removing " + object + " from posts relationship");
    }
    if (er.extensions.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
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


  public static Topic createTopic(EOEditingContext editingContext, NSTimestamp createdAt
, Integer hits
, java.lang.Boolean locked
, Integer postsCount
, NSTimestamp repliedAt
, Integer sticky
, String title
, NSTimestamp updatedAt
, se.caboo.beast.model.Forum forum, se.caboo.beast.model.User repliedBy, se.caboo.beast.model.User user) {
    Topic eo = (Topic) EOUtilities.createAndInsertInstance(editingContext, _Topic.ENTITY_NAME);    
		eo.setCreatedAt(createdAt);
		eo.setHits(hits);
		eo.setLocked(locked);
		eo.setPostsCount(postsCount);
		eo.setRepliedAt(repliedAt);
		eo.setSticky(sticky);
		eo.setTitle(title);
		eo.setUpdatedAt(updatedAt);
    eo.setForumRelationship(forum);
    eo.setRepliedByRelationship(repliedBy);
    eo.setUserRelationship(user);
    return eo;
  }

  public static NSArray<Topic> fetchAllTopics(EOEditingContext editingContext) {
    return _Topic.fetchAllTopics(editingContext, null);
  }

  public static NSArray<Topic> fetchAllTopics(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Topic.fetchTopics(editingContext, null, sortOrderings);
  }

  public static NSArray<Topic> fetchTopics(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Topic.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Topic> eoObjects = (NSArray<Topic>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static Topic fetchTopic(EOEditingContext editingContext, String keyName, Object value) {
    return _Topic.fetchTopic(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Topic fetchTopic(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Topic> eoObjects = _Topic.fetchTopics(editingContext, qualifier, null);
    Topic eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (Topic)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Topic that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Topic fetchRequiredTopic(EOEditingContext editingContext, String keyName, Object value) {
    return _Topic.fetchRequiredTopic(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Topic fetchRequiredTopic(EOEditingContext editingContext, EOQualifier qualifier) {
    Topic eoObject = _Topic.fetchTopic(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Topic that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Topic localInstanceIn(EOEditingContext editingContext, Topic eo) {
    Topic localInstance = (eo == null) ? null : (Topic)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
