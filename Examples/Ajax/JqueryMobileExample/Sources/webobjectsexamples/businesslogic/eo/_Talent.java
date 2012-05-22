//
// _Talent.java
//
// Created with WOLips : Thanks to Mike Schrag
//
// DO NOT EDIT. 
// Make changes to Talent.java instead.
//
// Template created by ishimoto 2012-02-03
//
package webobjectsexamples.businesslogic.eo;

import webobjectsexamples.businesslogic.eo.Talent;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import java.math.*;
import java.util.*;

import org.apache.log4j.Logger;

import wodka.a10.actor.A10Login;
import wodka.a10.eof.*;
import wodka.a10.validation.A10ValidationException;
import er.extensions.crypting.ERXCrypto;
import er.extensions.eof.*;

@SuppressWarnings("all")
public abstract class _Talent extends  A10GenericRecord {

	/** ログ・サポート */
	private static Logger log = Logger.getLogger(_Talent.class);

	//********************************************************************
	//	コンストラクター
	//********************************************************************

	public _Talent() {
		super();
		
		// フェッチ・スペシフィケーションを追加？
		addFetchSpecificationToEntity();
	}

	//********************************************************************
	//	D2W 用フェッチ・スペシフィケーション
	//********************************************************************

	/** エンティティにフェッチ・スペシフィケーションを追加バインディングします */
	public void addFetchSpecificationToEntity() {
		if(_addFetchSpecificationToEntity == null) {
			if (_Talent.log.isDebugEnabled()) {
				_Talent.log.debug("addFetchSpecificationToEntity");
			}
			addFetchSpecification();
			_addFetchSpecificationToEntity = Boolean.TRUE;
		}
	}
	private static Boolean _addFetchSpecificationToEntity = null;
	
	protected void addFetchSpecification() {}
	
	//********************************************************************
	//	エンティティ
	//********************************************************************
	
	/** Entity Name = Talent */
	public static final String ENTITY_NAME = "Talent";

	//********************************************************************
	//	アクセス・プロパティ
	//********************************************************************
	
	public static String ACCSESS_CREATE = "Talent.create";
	public static String ACCSESS_READ = "Talent.read";
	public static String ACCSESS_UPDATE = "Talent.update";
	public static String ACCSESS_DELETE = "Talent.delete";

	/** ユーザが挿入アクセス権限を持つ場合には true が戻ります。 */
	public boolean isCreateAllowed() {
		return A10Login.loginfo().can(ACCSESS_CREATE);
	}

	/** ユーザが読込アクセス権限を持つ場合には true が戻ります。 */
	public boolean isReadAllowed() {
		return A10Login.loginfo().can(ACCSESS_READ);
	}

	/** ユーザが更新アクセス権限を持つ場合には true が戻ります。 */
	public boolean isUpdateAllowed() {
	  if(A10Login.isEntityGrantForUpdate(ENTITY_NAME)) {
	    return true;
	  }     
    A10Login login = A10Login.loginfo();
    if(login == null) {
      log.warn("No A10Login Object Now!");
      return false;
    }   
    return login.can(ACCSESS_UPDATE);
	}
	
	/** ユーザが削除アクセス権限を持つ場合には true が戻ります。 */
	public boolean isDeleteAllowed() {
	  if(A10Login.isEntityGrantForDelete(ENTITY_NAME)) {
	    return true;
	  }
    A10Login login = A10Login.loginfo();
    if(login == null) {
      log.warn("No A10Login Object Now!");
      return false;
    }
		return login.can(ACCSESS_DELETE);
	}
	
	//********************************************************************
	//	アトリビュート
	//********************************************************************

	// Attribute Keys
	public static final ERXKey<String> FIRST_NAME = new ERXKey<String>("firstName");
	public static final ERXKey<String> LAST_NAME = new ERXKey<String>("lastName");

	// Attributes
	public static final String FIRST_NAME_KEY = FIRST_NAME.key();
	public static final String LAST_NAME_KEY = LAST_NAME.key();

	//********************************************************************
	//	リレーションシップ
	//********************************************************************

	// Relationship Keys
	public static final ERXKey<webobjectsexamples.businesslogic.eo.Movie> MOVIES_DIRECTED = new ERXKey<webobjectsexamples.businesslogic.eo.Movie>("moviesDirected");
	public static final ERXKey<webobjectsexamples.businesslogic.eo.TalentPhoto> PHOTO = new ERXKey<webobjectsexamples.businesslogic.eo.TalentPhoto>("photo");
	public static final ERXKey<webobjectsexamples.businesslogic.eo.MovieRole> ROLES = new ERXKey<webobjectsexamples.businesslogic.eo.MovieRole>("roles");

	// Relationships
	public static final String MOVIES_DIRECTED_KEY = MOVIES_DIRECTED.key();
	public static final String PHOTO_KEY = PHOTO.key();
	public static final String ROLES_KEY = ROLES.key();

	//********************************************************************
	//	フィールド・アクセス
	//********************************************************************

	public String firstName() {
		return (String) storedValueForKey(FIRST_NAME_KEY);
	}

	public void setFirstName(String value) {
		if (_Talent.log.isDebugEnabled()) {
			_Talent.log.debug( "updating firstName from " + firstName() + " to " + value);
		}
		takeStoredValueForKey(value, FIRST_NAME_KEY);
	}

	public void initializeFirstName(String value) {
		if (_Talent.log.isDebugEnabled()) {
			_Talent.log.debug( "initialize firstName to " + value);
		}
		takeStoredValueForKey(value, FIRST_NAME_KEY);
	}

	public Object validateFirstName(Object value) throws NSValidation.ValidationException {
		if (_Talent.log.isDebugEnabled()) {
			_Talent.log.debug("validate firstName");
		}
		return A10ValidationException.validateForUserInfo(this, FIRST_NAME_KEY, value);
	}

	public String lastName() {
		return (String) storedValueForKey(LAST_NAME_KEY);
	}

	public void setLastName(String value) {
		if (_Talent.log.isDebugEnabled()) {
			_Talent.log.debug( "updating lastName from " + lastName() + " to " + value);
		}
		takeStoredValueForKey(value, LAST_NAME_KEY);
	}

	public void initializeLastName(String value) {
		if (_Talent.log.isDebugEnabled()) {
			_Talent.log.debug( "initialize lastName to " + value);
		}
		takeStoredValueForKey(value, LAST_NAME_KEY);
	}

	public Object validateLastName(Object value) throws NSValidation.ValidationException {
		if (_Talent.log.isDebugEnabled()) {
			_Talent.log.debug("validate lastName");
		}
		return A10ValidationException.validateForUserInfo(this, LAST_NAME_KEY, value);
	}

	//====================================================================

	//********************************************************************
	//	ToOne リレーションシップ
	//********************************************************************

	public webobjectsexamples.businesslogic.eo.TalentPhoto photo() {
		return (webobjectsexamples.businesslogic.eo.TalentPhoto)storedValueForKey(PHOTO_KEY);
	}

	private void setPhoto(webobjectsexamples.businesslogic.eo.TalentPhoto value) {
		takeStoredValueForKey(value, PHOTO_KEY);
	}
	
	public void setPhotoRelationship(webobjectsexamples.businesslogic.eo.TalentPhoto value) {
		if (_Talent.log.isDebugEnabled()) {
			_Talent.log.debug("updating photo from " + photo() + " to " + value);
		}
		if (ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			setPhoto(value);
		} else if (value == null) {
			webobjectsexamples.businesslogic.eo.TalentPhoto oldValue = photo();
			if (oldValue != null) {
				removeObjectFromBothSidesOfRelationshipWithKey(oldValue, PHOTO_KEY);
			}
		} else {
			addObjectToBothSidesOfRelationshipWithKey(value, PHOTO_KEY);
		}
	}
  
	//********************************************************************
	//	編集コンテキスト入れ替え
	//********************************************************************

	@Override
	public Talent localInstanceIn(EOEditingContext editingContext) {
		Talent localInstance = (Talent)EOUtilities.localInstanceOfObject(editingContext, this);
	    if (localInstance == null) {
	    	throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
	    }
	    return localInstance;
	}

	//********************************************************************
	//	ToMany リレーションシップ
	//********************************************************************

	public NSArray<webobjectsexamples.businesslogic.eo.Movie> moviesDirected() {
		return (NSArray<webobjectsexamples.businesslogic.eo.Movie>)storedValueForKey("moviesDirected");
	}

	public NSArray<webobjectsexamples.businesslogic.eo.Movie> moviesDirected(EOQualifier qualifier) {
		return moviesDirected(qualifier, null);
	}

	public NSArray<webobjectsexamples.businesslogic.eo.Movie> moviesDirected(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		NSArray<webobjectsexamples.businesslogic.eo.Movie> results;
			results = moviesDirected();
			if (qualifier != null) {
				results = (NSArray<webobjectsexamples.businesslogic.eo.Movie>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
			}
			if (sortOrderings != null) {
				results = (NSArray<webobjectsexamples.businesslogic.eo.Movie>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
			}
    	return results;
	}
  
	public void addToMoviesDirected(webobjectsexamples.businesslogic.eo.Movie object) {
		includeObjectIntoPropertyWithKey(object, "moviesDirected");
	}

	public void removeFromMoviesDirected(webobjectsexamples.businesslogic.eo.Movie object) {
		excludeObjectFromPropertyWithKey(object, "moviesDirected");
	}

	public void addToMoviesDirectedRelationship(webobjectsexamples.businesslogic.eo.Movie object) {
		if (_Talent.log.isDebugEnabled()) {
			_Talent.log.debug("adding " + object + " to moviesDirected relationship");
		}
		if (A10GenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			addToMoviesDirected(object);
		} else {
			addObjectToBothSidesOfRelationshipWithKey(object, "moviesDirected");
		}
	}

	public void removeFromMoviesDirectedRelationship(webobjectsexamples.businesslogic.eo.Movie object) {
		if (_Talent.log.isDebugEnabled()) {
			_Talent.log.debug("removing " + object + " from moviesDirected relationship");
		}
		if (A10GenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			removeFromMoviesDirected(object);
		} else {
			removeObjectFromBothSidesOfRelationshipWithKey(object, "moviesDirected");
		}
	}

	public webobjectsexamples.businesslogic.eo.Movie createMoviesDirectedRelationship() {
		EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Movie");
		EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
		editingContext().insertObject(eo);
		addObjectToBothSidesOfRelationshipWithKey(eo, "moviesDirected");
		return (webobjectsexamples.businesslogic.eo.Movie) eo;
	}

	public void deleteMoviesDirectedRelationship(webobjectsexamples.businesslogic.eo.Movie object) {
		removeObjectFromBothSidesOfRelationshipWithKey(object, "moviesDirected");
    	editingContext().deleteObject(object);
	}

	public void deleteAllMoviesDirectedRelationships() {
		Enumeration objects = moviesDirected().immutableClone().objectEnumerator();
		while (objects.hasMoreElements()) {
			deleteMoviesDirectedRelationship((webobjectsexamples.businesslogic.eo.Movie)objects.nextElement());
		}
	}

	public NSArray<webobjectsexamples.businesslogic.eo.MovieRole> roles() {
		return (NSArray<webobjectsexamples.businesslogic.eo.MovieRole>)storedValueForKey("roles");
	}

	public NSArray<webobjectsexamples.businesslogic.eo.MovieRole> roles(EOQualifier qualifier) {
		return roles(qualifier, null, false);
	}

	public NSArray<webobjectsexamples.businesslogic.eo.MovieRole> roles(EOQualifier qualifier, boolean fetch) {
		return roles(qualifier, null, fetch);
	}

	public NSArray<webobjectsexamples.businesslogic.eo.MovieRole> roles(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
		NSArray<webobjectsexamples.businesslogic.eo.MovieRole> results;
		if (fetch) {
			EOQualifier fullQualifier;
      		EOQualifier inverseQualifier = new EOKeyValueQualifier(webobjectsexamples.businesslogic.eo.MovieRole.TALENT_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      		if (qualifier == null) {
      			fullQualifier = inverseQualifier;
      		} else {
      			NSMutableArray qualifiers = new NSMutableArray();
      			qualifiers.addObject(qualifier);
      			qualifiers.addObject(inverseQualifier);
      			fullQualifier = new EOAndQualifier(qualifiers);
      		}

      		results = webobjectsexamples.businesslogic.eo.MovieRole.fetchMovieRoles(editingContext(), fullQualifier, sortOrderings);
		} else {
			results = roles();
			if (qualifier != null) {
				results = (NSArray<webobjectsexamples.businesslogic.eo.MovieRole>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
			}
			if (sortOrderings != null) {
				results = (NSArray<webobjectsexamples.businesslogic.eo.MovieRole>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
			}
		}
    	return results;
	}
  
	public void addToRoles(webobjectsexamples.businesslogic.eo.MovieRole object) {
		includeObjectIntoPropertyWithKey(object, "roles");
	}

	public void removeFromRoles(webobjectsexamples.businesslogic.eo.MovieRole object) {
		excludeObjectFromPropertyWithKey(object, "roles");
	}

	public void addToRolesRelationship(webobjectsexamples.businesslogic.eo.MovieRole object) {
		if (_Talent.log.isDebugEnabled()) {
			_Talent.log.debug("adding " + object + " to roles relationship");
		}
		if (A10GenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			addToRoles(object);
		} else {
			addObjectToBothSidesOfRelationshipWithKey(object, "roles");
		}
	}

	public void removeFromRolesRelationship(webobjectsexamples.businesslogic.eo.MovieRole object) {
		if (_Talent.log.isDebugEnabled()) {
			_Talent.log.debug("removing " + object + " from roles relationship");
		}
		if (A10GenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			removeFromRoles(object);
		} else {
			removeObjectFromBothSidesOfRelationshipWithKey(object, "roles");
		}
	}

	public webobjectsexamples.businesslogic.eo.MovieRole createRolesRelationship() {
		EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("MovieRole");
		EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
		editingContext().insertObject(eo);
		addObjectToBothSidesOfRelationshipWithKey(eo, "roles");
		return (webobjectsexamples.businesslogic.eo.MovieRole) eo;
	}

	public void deleteRolesRelationship(webobjectsexamples.businesslogic.eo.MovieRole object) {
		removeObjectFromBothSidesOfRelationshipWithKey(object, "roles");
    	editingContext().deleteObject(object);
	}

	public void deleteAllRolesRelationships() {
		Enumeration objects = roles().immutableClone().objectEnumerator();
		while (objects.hasMoreElements()) {
			deleteRolesRelationship((webobjectsexamples.businesslogic.eo.MovieRole)objects.nextElement());
		}
	}

	//********************************************************************
	//	インスタンス化
	//********************************************************************

	public static Talent createTalent(EOEditingContext editingContext
		, String firstName
		, String lastName
		)
	{
		Talent eo = (Talent) EOUtilities.createAndInsertInstance(editingContext, _Talent.ENTITY_NAME);    
		eo.setFirstName(firstName);
		eo.setLastName(lastName);

		return eo;
	}
  
	public static Talent createAndInsertInstance(EOEditingContext context) {
		if (log.isDebugEnabled())
			log.debug(ENTITY_NAME + " : createAndInsertInstance");
		
    	return (Talent)EOUtilities.createAndInsertInstance(context, ENTITY_NAME);
	}

	@Deprecated
	public static Talent newTalentInstance(EOEditingContext context) {		
		return Talent.createAndInsertInstance(context);
	}

	// ========== [検索関連] ==========
	//********************************************************************
	//	フェッチ (NSArray)
	//********************************************************************

      public static ERXFetchSpecification<Talent> fetchSpec() {
      return new ERXFetchSpecification<Talent>(_Talent.ENTITY_NAME, null, null, false, true, null);
    }
  
	public static NSArray<Talent> fetchAllTalents(EOEditingContext editingContext) {
		return _Talent.fetchAllTalents(editingContext, null);
	}

	public static NSArray<Talent> fetchAllTalents(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
		return _Talent.fetchTalents(editingContext, null, sortOrderings);
	}

	public static NSArray<Talent> fetchTalents(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		EOFetchSpecification fetchSpec = new EOFetchSpecification(_Talent.ENTITY_NAME, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);
		NSArray<Talent> eoObjects = (NSArray<Talent>)editingContext.objectsWithFetchSpecification(fetchSpec);
		return eoObjects;
	}

	//********************************************************************
	//	フェッチ (User)
	//********************************************************************
	
	public static Talent fetchTalent(EOEditingContext editingContext, String keyName, Object value) {
		return _Talent.fetchTalent(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static Talent fetchTalent(EOEditingContext editingContext, EOQualifier qualifier) {
		NSArray<Talent> eoObjects = _Talent.fetchTalents(editingContext, qualifier, null);
		Talent eoObject;
		int count = eoObjects.count();
		if (count == 0) {
			eoObject = null;
		} else if (count == 1) {
			eoObject = (Talent)eoObjects.objectAtIndex(0);
		} else {
			throw new IllegalStateException("There was more than one Talent that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static Talent fetchRequiredTalent(EOEditingContext editingContext, String keyName, Object value) {
		return _Talent.fetchRequiredTalent(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static Talent fetchRequiredTalent(EOEditingContext editingContext, EOQualifier qualifier) {
		Talent eoObject = _Talent.fetchTalent(editingContext, qualifier);
		if (eoObject == null) {
			throw new NoSuchElementException("There was no Talent that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static Talent fetchTalentByPrimaryKey(EOEditingContext context, Object value) {
	  return Talent.fetchTalent(context, new EOKeyValueQualifier("id", EOQualifier.QualifierOperatorEqual, value));
	}

	public static Talent fetchTalentByEncryptedPrimaryKey(EOEditingContext context, String value) {
	  return Talent.fetchTalentByPrimaryKey(context, ERXCrypto.blowfishDecode(value));
	}
	
	public static Talent localInstanceIn(EOEditingContext editingContext, Talent eo) {
		Talent localInstance = (eo == null) ? null : (Talent)EOUtilities.localInstanceOfObject(editingContext, eo);
		if (localInstance == null && eo != null) {
			throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
		}
		return localInstance;
	}

	// ========== [Fetch Specification] ==========

	//********************************************************************
	//	フェッチ・スペシフィケーション
	//********************************************************************

}
