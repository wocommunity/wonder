// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to ServerPost.java instead.
package er.restexample.server;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _ServerPost extends  ERXGenericRecord {
	public static final String ENTITY_NAME = "ServerPost";

	// Attributes
	public static final String TITLE_KEY = "title";
	public static final ERXKey<String> TITLE = new ERXKey<String>(TITLE_KEY);

	// Relationships
	public static final String FORUM_KEY = "forum";
	public static final ERXKey<er.restexample.server.ServerForum> FORUM = new ERXKey<er.restexample.server.ServerForum>(FORUM_KEY);
	public static final String TOPIC_KEY = "topic";
	public static final ERXKey<er.restexample.server.ServerTopic> TOPIC = new ERXKey<er.restexample.server.ServerTopic>(TOPIC_KEY);
	public static final String USER_KEY = "user";
	public static final ERXKey<er.restexample.server.ServerUser> USER = new ERXKey<er.restexample.server.ServerUser>(USER_KEY);

  private static Logger LOG = Logger.getLogger(_ServerPost.class);

  public ServerPost localInstanceIn(EOEditingContext editingContext) {
    ServerPost localInstance = (ServerPost)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String title() {
    return (String) storedValueForKey("title");
  }

  public void setTitle(String value) {
    if (_ServerPost.LOG.isDebugEnabled()) {
    	_ServerPost.LOG.debug( "updating title from " + title() + " to " + value);
    }
    takeStoredValueForKey(value, "title");
  }

  public er.restexample.server.ServerForum forum() {
    return (er.restexample.server.ServerForum)storedValueForKey("forum");
  }
  
  public void setForum(er.restexample.server.ServerForum value) {
    takeStoredValueForKey(value, "forum");
  }

  public void setForumRelationship(er.restexample.server.ServerForum value) {
    if (_ServerPost.LOG.isDebugEnabled()) {
      _ServerPost.LOG.debug("updating forum from " + forum() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setForum(value);
    }
    else if (value == null) {
    	er.restexample.server.ServerForum oldValue = forum();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "forum");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "forum");
    }
  }
  
  public er.restexample.server.ServerTopic topic() {
    return (er.restexample.server.ServerTopic)storedValueForKey("topic");
  }
  
  public void setTopic(er.restexample.server.ServerTopic value) {
    takeStoredValueForKey(value, "topic");
  }

  public void setTopicRelationship(er.restexample.server.ServerTopic value) {
    if (_ServerPost.LOG.isDebugEnabled()) {
      _ServerPost.LOG.debug("updating topic from " + topic() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setTopic(value);
    }
    else if (value == null) {
    	er.restexample.server.ServerTopic oldValue = topic();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "topic");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "topic");
    }
  }
  
  public er.restexample.server.ServerUser user() {
    return (er.restexample.server.ServerUser)storedValueForKey("user");
  }
  
  public void setUser(er.restexample.server.ServerUser value) {
    takeStoredValueForKey(value, "user");
  }

  public void setUserRelationship(er.restexample.server.ServerUser value) {
    if (_ServerPost.LOG.isDebugEnabled()) {
      _ServerPost.LOG.debug("updating user from " + user() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setUser(value);
    }
    else if (value == null) {
    	er.restexample.server.ServerUser oldValue = user();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "user");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "user");
    }
  }
  

  public static ServerPost createServerPost(EOEditingContext editingContext, er.restexample.server.ServerForum forum, er.restexample.server.ServerTopic topic, er.restexample.server.ServerUser user) {
    ServerPost eo = (ServerPost) EOUtilities.createAndInsertInstance(editingContext, _ServerPost.ENTITY_NAME);    
    eo.setForumRelationship(forum);
    eo.setTopicRelationship(topic);
    eo.setUserRelationship(user);
    return eo;
  }

  public static NSArray<ServerPost> fetchAllServerPosts(EOEditingContext editingContext) {
    return _ServerPost.fetchAllServerPosts(editingContext, null);
  }

  public static NSArray<ServerPost> fetchAllServerPosts(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ServerPost.fetchServerPosts(editingContext, null, sortOrderings);
  }

  public static NSArray<ServerPost> fetchServerPosts(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_ServerPost.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ServerPost> eoObjects = (NSArray<ServerPost>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static ServerPost fetchServerPost(EOEditingContext editingContext, String keyName, Object value) {
    return _ServerPost.fetchServerPost(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ServerPost fetchServerPost(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<ServerPost> eoObjects = _ServerPost.fetchServerPosts(editingContext, qualifier, null);
    ServerPost eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (ServerPost)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one ServerPost that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ServerPost fetchRequiredServerPost(EOEditingContext editingContext, String keyName, Object value) {
    return _ServerPost.fetchRequiredServerPost(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ServerPost fetchRequiredServerPost(EOEditingContext editingContext, EOQualifier qualifier) {
    ServerPost eoObject = _ServerPost.fetchServerPost(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no ServerPost that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ServerPost localInstanceIn(EOEditingContext editingContext, ServerPost eo) {
    ServerPost localInstance = (eo == null) ? null : (ServerPost)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
