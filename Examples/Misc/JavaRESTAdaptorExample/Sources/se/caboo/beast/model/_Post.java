// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to Post.java instead.
package se.caboo.beast.model;

import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXGenericRecord;
import er.extensions.eof.ERXKey;

@SuppressWarnings("all")
public abstract class _Post extends  ERXGenericRecord {
	public static final String ENTITY_NAME = "Post";

	// Attributes
	public static final String BODY_KEY = "body";
	public static final ERXKey<String> BODY = new ERXKey<String>(BODY_KEY);
	public static final String BODY_HTML_KEY = "bodyHtml";
	public static final ERXKey<String> BODY_HTML = new ERXKey<String>(BODY_HTML_KEY);
	public static final String CREATED_AT_KEY = "createdAt";
	public static final ERXKey<NSTimestamp> CREATED_AT = new ERXKey<NSTimestamp>(CREATED_AT_KEY);
	public static final String UPDATED_AT_KEY = "updatedAt";
	public static final ERXKey<NSTimestamp> UPDATED_AT = new ERXKey<NSTimestamp>(UPDATED_AT_KEY);

	// Relationships
	public static final String FORUM_KEY = "forum";
	public static final ERXKey<se.caboo.beast.model.Forum> FORUM = new ERXKey<se.caboo.beast.model.Forum>(FORUM_KEY);
	public static final String TOPIC_KEY = "topic";
	public static final ERXKey<se.caboo.beast.model.Topic> TOPIC = new ERXKey<se.caboo.beast.model.Topic>(TOPIC_KEY);
	public static final String USER_KEY = "user";
	public static final ERXKey<se.caboo.beast.model.User> USER = new ERXKey<se.caboo.beast.model.User>(USER_KEY);

  private static Logger LOG = Logger.getLogger(_Post.class);

  public Post localInstanceIn(EOEditingContext editingContext) {
    Post localInstance = (Post)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String body() {
    return (String) storedValueForKey("body");
  }

  public void setBody(String value) {
    if (_Post.LOG.isDebugEnabled()) {
    	_Post.LOG.debug( "updating body from " + body() + " to " + value);
    }
    takeStoredValueForKey(value, "body");
  }

  public String bodyHtml() {
    return (String) storedValueForKey("bodyHtml");
  }

  public void setBodyHtml(String value) {
    if (_Post.LOG.isDebugEnabled()) {
    	_Post.LOG.debug( "updating bodyHtml from " + bodyHtml() + " to " + value);
    }
    takeStoredValueForKey(value, "bodyHtml");
  }

  public NSTimestamp createdAt() {
    return (NSTimestamp) storedValueForKey("createdAt");
  }

  public void setCreatedAt(NSTimestamp value) {
    if (_Post.LOG.isDebugEnabled()) {
    	_Post.LOG.debug( "updating createdAt from " + createdAt() + " to " + value);
    }
    takeStoredValueForKey(value, "createdAt");
  }

  public NSTimestamp updatedAt() {
    return (NSTimestamp) storedValueForKey("updatedAt");
  }

  public void setUpdatedAt(NSTimestamp value) {
    if (_Post.LOG.isDebugEnabled()) {
    	_Post.LOG.debug( "updating updatedAt from " + updatedAt() + " to " + value);
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
    if (_Post.LOG.isDebugEnabled()) {
      _Post.LOG.debug("updating forum from " + forum() + " to " + value);
    }
    if (ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
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
  
  public se.caboo.beast.model.Topic topic() {
    return (se.caboo.beast.model.Topic)storedValueForKey("topic");
  }
  
  public void setTopic(se.caboo.beast.model.Topic value) {
    takeStoredValueForKey(value, "topic");
  }

  public void setTopicRelationship(se.caboo.beast.model.Topic value) {
    if (_Post.LOG.isDebugEnabled()) {
      _Post.LOG.debug("updating topic from " + topic() + " to " + value);
    }
    if (ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setTopic(value);
    }
    else if (value == null) {
    	se.caboo.beast.model.Topic oldValue = topic();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "topic");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "topic");
    }
  }
  
  public se.caboo.beast.model.User user() {
    return (se.caboo.beast.model.User)storedValueForKey("user");
  }
  
  public void setUser(se.caboo.beast.model.User value) {
    takeStoredValueForKey(value, "user");
  }

  public void setUserRelationship(se.caboo.beast.model.User value) {
    if (_Post.LOG.isDebugEnabled()) {
      _Post.LOG.debug("updating user from " + user() + " to " + value);
    }
    if (ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
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
  

  public static Post createPost(EOEditingContext editingContext, String body
, String bodyHtml
, NSTimestamp createdAt
, NSTimestamp updatedAt
, se.caboo.beast.model.Forum forum, se.caboo.beast.model.Topic topic, se.caboo.beast.model.User user) {
    Post eo = (Post) EOUtilities.createAndInsertInstance(editingContext, _Post.ENTITY_NAME);    
		eo.setBody(body);
		eo.setBodyHtml(bodyHtml);
		eo.setCreatedAt(createdAt);
		eo.setUpdatedAt(updatedAt);
    eo.setForumRelationship(forum);
    eo.setTopicRelationship(topic);
    eo.setUserRelationship(user);
    return eo;
  }

  public static NSArray<Post> fetchAllPosts(EOEditingContext editingContext) {
    return _Post.fetchAllPosts(editingContext, null);
  }

  public static NSArray<Post> fetchAllPosts(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Post.fetchPosts(editingContext, null, sortOrderings);
  }

  public static NSArray<Post> fetchPosts(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Post.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Post> eoObjects = (NSArray<Post>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static Post fetchPost(EOEditingContext editingContext, String keyName, Object value) {
    return _Post.fetchPost(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Post fetchPost(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Post> eoObjects = _Post.fetchPosts(editingContext, qualifier, null);
    Post eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (Post)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Post that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Post fetchRequiredPost(EOEditingContext editingContext, String keyName, Object value) {
    return _Post.fetchRequiredPost(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Post fetchRequiredPost(EOEditingContext editingContext, EOQualifier qualifier) {
    Post eoObject = _Post.fetchPost(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Post that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Post localInstanceIn(EOEditingContext editingContext, Post eo) {
    Post localInstance = (eo == null) ? null : (Post)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
