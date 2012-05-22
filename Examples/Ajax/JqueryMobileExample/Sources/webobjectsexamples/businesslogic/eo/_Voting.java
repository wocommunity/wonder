//
// _Voting.java
//
// Created with WOLips : Thanks to Mike Schrag
//
// DO NOT EDIT. 
// Make changes to Voting.java instead.
//
// Template created by ishimoto 2012-02-03
//
package webobjectsexamples.businesslogic.eo;

import webobjectsexamples.businesslogic.eo.Voting;

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
public abstract class _Voting extends  A10GenericRecord {

	/** ログ・サポート */
	private static Logger log = Logger.getLogger(_Voting.class);

	//********************************************************************
	//	コンストラクター
	//********************************************************************

	public _Voting() {
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
			if (_Voting.log.isDebugEnabled()) {
				_Voting.log.debug("addFetchSpecificationToEntity");
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
	
	/** Entity Name = Voting */
	public static final String ENTITY_NAME = "Voting";

	//********************************************************************
	//	アクセス・プロパティ
	//********************************************************************
	
	public static String ACCSESS_CREATE = "Voting.create";
	public static String ACCSESS_READ = "Voting.read";
	public static String ACCSESS_UPDATE = "Voting.update";
	public static String ACCSESS_DELETE = "Voting.delete";

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
	public static final ERXKey<Integer> NUMBER_OF_VOTES = new ERXKey<Integer>("numberOfVotes");
	public static final ERXKey<Double> RUNNING_AVERAGE = new ERXKey<Double>("runningAverage");

	// Attributes
	public static final String NUMBER_OF_VOTES_KEY = NUMBER_OF_VOTES.key();
	public static final String RUNNING_AVERAGE_KEY = RUNNING_AVERAGE.key();

	//********************************************************************
	//	リレーションシップ
	//********************************************************************

	// Relationship Keys
	public static final ERXKey<webobjectsexamples.businesslogic.eo.Movie> MOVIE = new ERXKey<webobjectsexamples.businesslogic.eo.Movie>("movie");

	// Relationships
	public static final String MOVIE_KEY = MOVIE.key();

	//********************************************************************
	//	フィールド・アクセス
	//********************************************************************

	public Integer numberOfVotes() {
		return (Integer) storedValueForKey(NUMBER_OF_VOTES_KEY);
	}

	public void setNumberOfVotes(Integer value) {
		if (_Voting.log.isDebugEnabled()) {
			_Voting.log.debug( "updating numberOfVotes from " + numberOfVotes() + " to " + value);
		}
		takeStoredValueForKey(value, NUMBER_OF_VOTES_KEY);
	}

	public void initializeNumberOfVotes(Integer value) {
		if (_Voting.log.isDebugEnabled()) {
			_Voting.log.debug( "initialize numberOfVotes to " + value);
		}
		takeStoredValueForKey(value, NUMBER_OF_VOTES_KEY);
	}

	public Object validateNumberOfVotes(Object value) throws NSValidation.ValidationException {
		if (_Voting.log.isDebugEnabled()) {
			_Voting.log.debug("validate numberOfVotes");
		}
		return A10ValidationException.validateForUserInfo(this, NUMBER_OF_VOTES_KEY, value);
	}

	public Double runningAverage() {
		return (Double) storedValueForKey(RUNNING_AVERAGE_KEY);
	}

	public void setRunningAverage(Double value) {
		if (_Voting.log.isDebugEnabled()) {
			_Voting.log.debug( "updating runningAverage from " + runningAverage() + " to " + value);
		}
		takeStoredValueForKey(value, RUNNING_AVERAGE_KEY);
	}

	public void initializeRunningAverage(Double value) {
		if (_Voting.log.isDebugEnabled()) {
			_Voting.log.debug( "initialize runningAverage to " + value);
		}
		takeStoredValueForKey(value, RUNNING_AVERAGE_KEY);
	}

	public Object validateRunningAverage(Object value) throws NSValidation.ValidationException {
		if (_Voting.log.isDebugEnabled()) {
			_Voting.log.debug("validate runningAverage");
		}
		return A10ValidationException.validateForUserInfo(this, RUNNING_AVERAGE_KEY, value);
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
		if (_Voting.log.isDebugEnabled()) {
			_Voting.log.debug("updating movie from " + movie() + " to " + value);
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
  
	//********************************************************************
	//	編集コンテキスト入れ替え
	//********************************************************************

	@Override
	public Voting localInstanceIn(EOEditingContext editingContext) {
		Voting localInstance = (Voting)EOUtilities.localInstanceOfObject(editingContext, this);
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

	public static Voting createVoting(EOEditingContext editingContext
		, webobjectsexamples.businesslogic.eo.Movie movie
		)
	{
		Voting eo = (Voting) EOUtilities.createAndInsertInstance(editingContext, _Voting.ENTITY_NAME);    
    	eo.setMovieRelationship(movie);

		return eo;
	}
  
	public static Voting createAndInsertInstance(EOEditingContext context) {
		if (log.isDebugEnabled())
			log.debug(ENTITY_NAME + " : createAndInsertInstance");
		
    	return (Voting)EOUtilities.createAndInsertInstance(context, ENTITY_NAME);
	}

	@Deprecated
	public static Voting newVotingInstance(EOEditingContext context) {		
		return Voting.createAndInsertInstance(context);
	}

	// ========== [検索関連] ==========
	//********************************************************************
	//	フェッチ (NSArray)
	//********************************************************************

      public static ERXFetchSpecification<Voting> fetchSpec() {
      return new ERXFetchSpecification<Voting>(_Voting.ENTITY_NAME, null, null, false, true, null);
    }
  
	public static NSArray<Voting> fetchAllVotings(EOEditingContext editingContext) {
		return _Voting.fetchAllVotings(editingContext, null);
	}

	public static NSArray<Voting> fetchAllVotings(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
		return _Voting.fetchVotings(editingContext, null, sortOrderings);
	}

	public static NSArray<Voting> fetchVotings(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		EOFetchSpecification fetchSpec = new EOFetchSpecification(_Voting.ENTITY_NAME, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);
		NSArray<Voting> eoObjects = (NSArray<Voting>)editingContext.objectsWithFetchSpecification(fetchSpec);
		return eoObjects;
	}

	//********************************************************************
	//	フェッチ (User)
	//********************************************************************
	
	public static Voting fetchVoting(EOEditingContext editingContext, String keyName, Object value) {
		return _Voting.fetchVoting(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static Voting fetchVoting(EOEditingContext editingContext, EOQualifier qualifier) {
		NSArray<Voting> eoObjects = _Voting.fetchVotings(editingContext, qualifier, null);
		Voting eoObject;
		int count = eoObjects.count();
		if (count == 0) {
			eoObject = null;
		} else if (count == 1) {
			eoObject = (Voting)eoObjects.objectAtIndex(0);
		} else {
			throw new IllegalStateException("There was more than one Voting that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static Voting fetchRequiredVoting(EOEditingContext editingContext, String keyName, Object value) {
		return _Voting.fetchRequiredVoting(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static Voting fetchRequiredVoting(EOEditingContext editingContext, EOQualifier qualifier) {
		Voting eoObject = _Voting.fetchVoting(editingContext, qualifier);
		if (eoObject == null) {
			throw new NoSuchElementException("There was no Voting that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static Voting fetchVotingByPrimaryKey(EOEditingContext context, Object value) {
	  return Voting.fetchVoting(context, new EOKeyValueQualifier("id", EOQualifier.QualifierOperatorEqual, value));
	}

	public static Voting fetchVotingByEncryptedPrimaryKey(EOEditingContext context, String value) {
	  return Voting.fetchVotingByPrimaryKey(context, ERXCrypto.blowfishDecode(value));
	}
	
	public static Voting localInstanceIn(EOEditingContext editingContext, Voting eo) {
		Voting localInstance = (eo == null) ? null : (Voting)EOUtilities.localInstanceOfObject(editingContext, eo);
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
