//
// _MovieRole.java
//
// Created with WOLips : Thanks to Mike Schrag
//
// DO NOT EDIT. 
// Make changes to MovieRole.java instead.
//
// Template created by ishimoto 2012-02-03
//
package webobjectsexamples.businesslogic.eo;

import webobjectsexamples.businesslogic.eo.MovieRole;

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
public abstract class _MovieRole extends  A10GenericRecord {

	/** ログ・サポート */
	private static Logger log = Logger.getLogger(_MovieRole.class);

	//********************************************************************
	//	コンストラクター
	//********************************************************************

	public _MovieRole() {
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
			if (_MovieRole.log.isDebugEnabled()) {
				_MovieRole.log.debug("addFetchSpecificationToEntity");
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
	
	/** Entity Name = MovieRole */
	public static final String ENTITY_NAME = "MovieRole";

	//********************************************************************
	//	アクセス・プロパティ
	//********************************************************************
	
	public static String ACCSESS_CREATE = "MovieRole.create";
	public static String ACCSESS_READ = "MovieRole.read";
	public static String ACCSESS_UPDATE = "MovieRole.update";
	public static String ACCSESS_DELETE = "MovieRole.delete";

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
	public static final ERXKey<String> ROLE_NAME = new ERXKey<String>("roleName");

	// Attributes
	public static final String ROLE_NAME_KEY = ROLE_NAME.key();

	//********************************************************************
	//	リレーションシップ
	//********************************************************************

	// Relationship Keys
	public static final ERXKey<webobjectsexamples.businesslogic.eo.Movie> MOVIE = new ERXKey<webobjectsexamples.businesslogic.eo.Movie>("movie");
	public static final ERXKey<webobjectsexamples.businesslogic.eo.Talent> TALENT = new ERXKey<webobjectsexamples.businesslogic.eo.Talent>("talent");

	// Relationships
	public static final String MOVIE_KEY = MOVIE.key();
	public static final String TALENT_KEY = TALENT.key();

	//********************************************************************
	//	フィールド・アクセス
	//********************************************************************

	public String roleName() {
		return (String) storedValueForKey(ROLE_NAME_KEY);
	}

	public void setRoleName(String value) {
		if (_MovieRole.log.isDebugEnabled()) {
			_MovieRole.log.debug( "updating roleName from " + roleName() + " to " + value);
		}
		takeStoredValueForKey(value, ROLE_NAME_KEY);
	}

	public void initializeRoleName(String value) {
		if (_MovieRole.log.isDebugEnabled()) {
			_MovieRole.log.debug( "initialize roleName to " + value);
		}
		takeStoredValueForKey(value, ROLE_NAME_KEY);
	}

	public Object validateRoleName(Object value) throws NSValidation.ValidationException {
		if (_MovieRole.log.isDebugEnabled()) {
			_MovieRole.log.debug("validate roleName");
		}
		return A10ValidationException.validateForUserInfo(this, ROLE_NAME_KEY, value);
	}

	//====================================================================

	//********************************************************************
	//	ToOne リレーションシップ
	//********************************************************************

	public webobjectsexamples.businesslogic.eo.Movie movie() {
		return (webobjectsexamples.businesslogic.eo.Movie)storedValueForKey(MOVIE_KEY);
	}

	private void setMovie(webobjectsexamples.businesslogic.eo.Movie value) {
		takeStoredValueForKey(value, MOVIE_KEY);
	}
	
	public void setMovieRelationship(webobjectsexamples.businesslogic.eo.Movie value) {
		if (_MovieRole.log.isDebugEnabled()) {
			_MovieRole.log.debug("updating movie from " + movie() + " to " + value);
		}
		if (ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			setMovie(value);
		} else if (value == null) {
			webobjectsexamples.businesslogic.eo.Movie oldValue = movie();
			if (oldValue != null) {
				removeObjectFromBothSidesOfRelationshipWithKey(oldValue, MOVIE_KEY);
			}
		} else {
			addObjectToBothSidesOfRelationshipWithKey(value, MOVIE_KEY);
		}
	}
  
	public webobjectsexamples.businesslogic.eo.Talent talent() {
		return (webobjectsexamples.businesslogic.eo.Talent)storedValueForKey(TALENT_KEY);
	}

	private void setTalent(webobjectsexamples.businesslogic.eo.Talent value) {
		takeStoredValueForKey(value, TALENT_KEY);
	}
	
	public void setTalentRelationship(webobjectsexamples.businesslogic.eo.Talent value) {
		if (_MovieRole.log.isDebugEnabled()) {
			_MovieRole.log.debug("updating talent from " + talent() + " to " + value);
		}
		if (ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			setTalent(value);
		} else if (value == null) {
			webobjectsexamples.businesslogic.eo.Talent oldValue = talent();
			if (oldValue != null) {
				removeObjectFromBothSidesOfRelationshipWithKey(oldValue, TALENT_KEY);
			}
		} else {
			addObjectToBothSidesOfRelationshipWithKey(value, TALENT_KEY);
		}
	}
  
	//********************************************************************
	//	編集コンテキスト入れ替え
	//********************************************************************

	@Override
	public MovieRole localInstanceIn(EOEditingContext editingContext) {
		MovieRole localInstance = (MovieRole)EOUtilities.localInstanceOfObject(editingContext, this);
	    if (localInstance == null) {
	    	throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
	    }
	    return localInstance;
	}

	//********************************************************************
	//	ToMany リレーションシップ
	//********************************************************************

	//********************************************************************
	//	インスタンス化
	//********************************************************************

	public static MovieRole createMovieRole(EOEditingContext editingContext
		, webobjectsexamples.businesslogic.eo.Movie movie
		, webobjectsexamples.businesslogic.eo.Talent talent
		)
	{
		MovieRole eo = (MovieRole) EOUtilities.createAndInsertInstance(editingContext, _MovieRole.ENTITY_NAME);    
    	eo.setMovieRelationship(movie);
    	eo.setTalentRelationship(talent);

		return eo;
	}
  
	public static MovieRole createAndInsertInstance(EOEditingContext context) {
		if (log.isDebugEnabled())
			log.debug(ENTITY_NAME + " : createAndInsertInstance");
		
    	return (MovieRole)EOUtilities.createAndInsertInstance(context, ENTITY_NAME);
	}

	@Deprecated
	public static MovieRole newMovieRoleInstance(EOEditingContext context) {		
		return MovieRole.createAndInsertInstance(context);
	}

	// ========== [検索関連] ==========
	//********************************************************************
	//	フェッチ (NSArray)
	//********************************************************************

      public static ERXFetchSpecification<MovieRole> fetchSpec() {
      return new ERXFetchSpecification<MovieRole>(_MovieRole.ENTITY_NAME, null, null, false, true, null);
    }
  
	public static NSArray<MovieRole> fetchAllMovieRoles(EOEditingContext editingContext) {
		return _MovieRole.fetchAllMovieRoles(editingContext, null);
	}

	public static NSArray<MovieRole> fetchAllMovieRoles(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
		return _MovieRole.fetchMovieRoles(editingContext, null, sortOrderings);
	}

	public static NSArray<MovieRole> fetchMovieRoles(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		EOFetchSpecification fetchSpec = new EOFetchSpecification(_MovieRole.ENTITY_NAME, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);
		NSArray<MovieRole> eoObjects = (NSArray<MovieRole>)editingContext.objectsWithFetchSpecification(fetchSpec);
		return eoObjects;
	}

	//********************************************************************
	//	フェッチ (User)
	//********************************************************************
	
	public static MovieRole fetchMovieRole(EOEditingContext editingContext, String keyName, Object value) {
		return _MovieRole.fetchMovieRole(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static MovieRole fetchMovieRole(EOEditingContext editingContext, EOQualifier qualifier) {
		NSArray<MovieRole> eoObjects = _MovieRole.fetchMovieRoles(editingContext, qualifier, null);
		MovieRole eoObject;
		int count = eoObjects.count();
		if (count == 0) {
			eoObject = null;
		} else if (count == 1) {
			eoObject = (MovieRole)eoObjects.objectAtIndex(0);
		} else {
			throw new IllegalStateException("There was more than one MovieRole that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static MovieRole fetchRequiredMovieRole(EOEditingContext editingContext, String keyName, Object value) {
		return _MovieRole.fetchRequiredMovieRole(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static MovieRole fetchRequiredMovieRole(EOEditingContext editingContext, EOQualifier qualifier) {
		MovieRole eoObject = _MovieRole.fetchMovieRole(editingContext, qualifier);
		if (eoObject == null) {
			throw new NoSuchElementException("There was no MovieRole that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static MovieRole fetchMovieRoleByPrimaryKey(EOEditingContext context, Object value) {
	  return MovieRole.fetchMovieRole(context, new EOKeyValueQualifier("id", EOQualifier.QualifierOperatorEqual, value));
	}

	public static MovieRole fetchMovieRoleByEncryptedPrimaryKey(EOEditingContext context, String value) {
	  return MovieRole.fetchMovieRoleByPrimaryKey(context, ERXCrypto.blowfishDecode(value));
	}
	
	public static MovieRole localInstanceIn(EOEditingContext editingContext, MovieRole eo) {
		MovieRole localInstance = (eo == null) ? null : (MovieRole)EOUtilities.localInstanceOfObject(editingContext, eo);
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
