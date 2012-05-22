//
// _Studio.java
//
// Created with WOLips : Thanks to Mike Schrag
//
// DO NOT EDIT. 
// Make changes to Studio.java instead.
//
// Template created by ishimoto 2012-02-03
//
package webobjectsexamples.businesslogic.eo;

import webobjectsexamples.businesslogic.eo.Studio;

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
public abstract class _Studio extends  A10GenericRecord {

	/** ログ・サポート */
	private static Logger log = Logger.getLogger(_Studio.class);

	//********************************************************************
	//	コンストラクター
	//********************************************************************

	public _Studio() {
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
			if (_Studio.log.isDebugEnabled()) {
				_Studio.log.debug("addFetchSpecificationToEntity");
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
	
	/** Entity Name = Studio */
	public static final String ENTITY_NAME = "Studio";

	//********************************************************************
	//	アクセス・プロパティ
	//********************************************************************
	
	public static String ACCSESS_CREATE = "Studio.create";
	public static String ACCSESS_READ = "Studio.read";
	public static String ACCSESS_UPDATE = "Studio.update";
	public static String ACCSESS_DELETE = "Studio.delete";

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
	public static final ERXKey<java.math.BigDecimal> BUDGET = new ERXKey<java.math.BigDecimal>("budget");
	public static final ERXKey<String> NAME = new ERXKey<String>("name");

	// Attributes
	public static final String BUDGET_KEY = BUDGET.key();
	public static final String NAME_KEY = NAME.key();

	//********************************************************************
	//	リレーションシップ
	//********************************************************************

	// Relationship Keys
	public static final ERXKey<webobjectsexamples.businesslogic.eo.Movie> MOVIES = new ERXKey<webobjectsexamples.businesslogic.eo.Movie>("movies");

	// Relationships
	public static final String MOVIES_KEY = MOVIES.key();

	//********************************************************************
	//	フィールド・アクセス
	//********************************************************************

	public java.math.BigDecimal budget() {
		return (java.math.BigDecimal) storedValueForKey(BUDGET_KEY);
	}

	public void setBudget(java.math.BigDecimal value) {
		if (_Studio.log.isDebugEnabled()) {
			_Studio.log.debug( "updating budget from " + budget() + " to " + value);
		}
		takeStoredValueForKey(value, BUDGET_KEY);
	}

	public void initializeBudget(java.math.BigDecimal value) {
		if (_Studio.log.isDebugEnabled()) {
			_Studio.log.debug( "initialize budget to " + value);
		}
		takeStoredValueForKey(value, BUDGET_KEY);
	}

	public Object validateBudget(Object value) throws NSValidation.ValidationException {
		if (_Studio.log.isDebugEnabled()) {
			_Studio.log.debug("validate budget");
		}
		return A10ValidationException.validateForUserInfo(this, BUDGET_KEY, value);
	}

	public String name() {
		return (String) storedValueForKey(NAME_KEY);
	}

	public void setName(String value) {
		if (_Studio.log.isDebugEnabled()) {
			_Studio.log.debug( "updating name from " + name() + " to " + value);
		}
		takeStoredValueForKey(value, NAME_KEY);
	}

	public void initializeName(String value) {
		if (_Studio.log.isDebugEnabled()) {
			_Studio.log.debug( "initialize name to " + value);
		}
		takeStoredValueForKey(value, NAME_KEY);
	}

	public Object validateName(Object value) throws NSValidation.ValidationException {
		if (_Studio.log.isDebugEnabled()) {
			_Studio.log.debug("validate name");
		}
		return A10ValidationException.validateForUserInfo(this, NAME_KEY, value);
	}

	//====================================================================

	//********************************************************************
	//	ToOne リレーションシップ
	//********************************************************************

	//********************************************************************
	//	編集コンテキスト入れ替え
	//********************************************************************

	@Override
	public Studio localInstanceIn(EOEditingContext editingContext) {
		Studio localInstance = (Studio)EOUtilities.localInstanceOfObject(editingContext, this);
	    if (localInstance == null) {
	    	throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
	    }
	    return localInstance;
	}

	//********************************************************************
	//	ToMany リレーションシップ
	//********************************************************************

	public NSArray<webobjectsexamples.businesslogic.eo.Movie> movies() {
		return (NSArray<webobjectsexamples.businesslogic.eo.Movie>)storedValueForKey("movies");
	}

	public NSArray<webobjectsexamples.businesslogic.eo.Movie> movies(EOQualifier qualifier) {
		return movies(qualifier, null, false);
	}

	public NSArray<webobjectsexamples.businesslogic.eo.Movie> movies(EOQualifier qualifier, boolean fetch) {
		return movies(qualifier, null, fetch);
	}

	public NSArray<webobjectsexamples.businesslogic.eo.Movie> movies(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
		NSArray<webobjectsexamples.businesslogic.eo.Movie> results;
		if (fetch) {
			EOQualifier fullQualifier;
      		EOQualifier inverseQualifier = new EOKeyValueQualifier(webobjectsexamples.businesslogic.eo.Movie.STUDIO_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      		if (qualifier == null) {
      			fullQualifier = inverseQualifier;
      		} else {
      			NSMutableArray qualifiers = new NSMutableArray();
      			qualifiers.addObject(qualifier);
      			qualifiers.addObject(inverseQualifier);
      			fullQualifier = new EOAndQualifier(qualifiers);
      		}

      		results = webobjectsexamples.businesslogic.eo.Movie.fetchMovies(editingContext(), fullQualifier, sortOrderings);
		} else {
			results = movies();
			if (qualifier != null) {
				results = (NSArray<webobjectsexamples.businesslogic.eo.Movie>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
			}
			if (sortOrderings != null) {
				results = (NSArray<webobjectsexamples.businesslogic.eo.Movie>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
			}
		}
    	return results;
	}
  
	public void addToMovies(webobjectsexamples.businesslogic.eo.Movie object) {
		includeObjectIntoPropertyWithKey(object, "movies");
	}

	public void removeFromMovies(webobjectsexamples.businesslogic.eo.Movie object) {
		excludeObjectFromPropertyWithKey(object, "movies");
	}

	public void addToMoviesRelationship(webobjectsexamples.businesslogic.eo.Movie object) {
		if (_Studio.log.isDebugEnabled()) {
			_Studio.log.debug("adding " + object + " to movies relationship");
		}
		if (A10GenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			addToMovies(object);
		} else {
			addObjectToBothSidesOfRelationshipWithKey(object, "movies");
		}
	}

	public void removeFromMoviesRelationship(webobjectsexamples.businesslogic.eo.Movie object) {
		if (_Studio.log.isDebugEnabled()) {
			_Studio.log.debug("removing " + object + " from movies relationship");
		}
		if (A10GenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			removeFromMovies(object);
		} else {
			removeObjectFromBothSidesOfRelationshipWithKey(object, "movies");
		}
	}

	public webobjectsexamples.businesslogic.eo.Movie createMoviesRelationship() {
		EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Movie");
		EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
		editingContext().insertObject(eo);
		addObjectToBothSidesOfRelationshipWithKey(eo, "movies");
		return (webobjectsexamples.businesslogic.eo.Movie) eo;
	}

	public void deleteMoviesRelationship(webobjectsexamples.businesslogic.eo.Movie object) {
		removeObjectFromBothSidesOfRelationshipWithKey(object, "movies");
    	editingContext().deleteObject(object);
	}

	public void deleteAllMoviesRelationships() {
		Enumeration objects = movies().immutableClone().objectEnumerator();
		while (objects.hasMoreElements()) {
			deleteMoviesRelationship((webobjectsexamples.businesslogic.eo.Movie)objects.nextElement());
		}
	}

	//********************************************************************
	//	インスタンス化
	//********************************************************************

	public static Studio createStudio(EOEditingContext editingContext
		, java.math.BigDecimal budget
		, String name
		)
	{
		Studio eo = (Studio) EOUtilities.createAndInsertInstance(editingContext, _Studio.ENTITY_NAME);    
		eo.setBudget(budget);
		eo.setName(name);

		return eo;
	}
  
	public static Studio createAndInsertInstance(EOEditingContext context) {
		if (log.isDebugEnabled())
			log.debug(ENTITY_NAME + " : createAndInsertInstance");
		
    	return (Studio)EOUtilities.createAndInsertInstance(context, ENTITY_NAME);
	}

	@Deprecated
	public static Studio newStudioInstance(EOEditingContext context) {		
		return Studio.createAndInsertInstance(context);
	}

	// ========== [検索関連] ==========
	//********************************************************************
	//	フェッチ (NSArray)
	//********************************************************************

      public static ERXFetchSpecification<Studio> fetchSpec() {
      return new ERXFetchSpecification<Studio>(_Studio.ENTITY_NAME, null, null, false, true, null);
    }
  
	public static NSArray<Studio> fetchAllStudios(EOEditingContext editingContext) {
		return _Studio.fetchAllStudios(editingContext, null);
	}

	public static NSArray<Studio> fetchAllStudios(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
		return _Studio.fetchStudios(editingContext, null, sortOrderings);
	}

	public static NSArray<Studio> fetchStudios(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		EOFetchSpecification fetchSpec = new EOFetchSpecification(_Studio.ENTITY_NAME, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);
		NSArray<Studio> eoObjects = (NSArray<Studio>)editingContext.objectsWithFetchSpecification(fetchSpec);
		return eoObjects;
	}

	//********************************************************************
	//	フェッチ (User)
	//********************************************************************
	
	public static Studio fetchStudio(EOEditingContext editingContext, String keyName, Object value) {
		return _Studio.fetchStudio(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static Studio fetchStudio(EOEditingContext editingContext, EOQualifier qualifier) {
		NSArray<Studio> eoObjects = _Studio.fetchStudios(editingContext, qualifier, null);
		Studio eoObject;
		int count = eoObjects.count();
		if (count == 0) {
			eoObject = null;
		} else if (count == 1) {
			eoObject = (Studio)eoObjects.objectAtIndex(0);
		} else {
			throw new IllegalStateException("There was more than one Studio that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static Studio fetchRequiredStudio(EOEditingContext editingContext, String keyName, Object value) {
		return _Studio.fetchRequiredStudio(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static Studio fetchRequiredStudio(EOEditingContext editingContext, EOQualifier qualifier) {
		Studio eoObject = _Studio.fetchStudio(editingContext, qualifier);
		if (eoObject == null) {
			throw new NoSuchElementException("There was no Studio that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static Studio fetchStudioByPrimaryKey(EOEditingContext context, Object value) {
	  return Studio.fetchStudio(context, new EOKeyValueQualifier("id", EOQualifier.QualifierOperatorEqual, value));
	}

	public static Studio fetchStudioByEncryptedPrimaryKey(EOEditingContext context, String value) {
	  return Studio.fetchStudioByPrimaryKey(context, ERXCrypto.blowfishDecode(value));
	}
	
	public static Studio localInstanceIn(EOEditingContext editingContext, Studio eo) {
		Studio localInstance = (eo == null) ? null : (Studio)EOUtilities.localInstanceOfObject(editingContext, eo);
		if (localInstance == null && eo != null) {
			throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
		}
		return localInstance;
	}

	// ========== [Fetch Specification] ==========

	//********************************************************************
	//	フェッチ・スペシフィケーション
	//********************************************************************

	public static NSArray<NSDictionary> fetchRawFetchAllStudios(EOEditingContext editingContext, NSDictionary<String, Object> bindings) {
		EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("RawFetchAllStudios", "Studio");
		fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
		return (NSArray<NSDictionary>)editingContext.objectsWithFetchSpecification(fetchSpec);
	}
  
	public static NSArray<NSDictionary> fetchRawFetchAllStudios(EOEditingContext editingContext) {
		EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("RawFetchAllStudios", "Studio");
    	return (NSArray<NSDictionary>)editingContext.objectsWithFetchSpecification(fetchSpec);
	}
  
}
