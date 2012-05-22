//
// _Review.java
//
// Created with WOLips : Thanks to Mike Schrag
//
// DO NOT EDIT. 
// Make changes to Review.java instead.
//
// Template created by ishimoto 2012-02-03
//
package webobjectsexamples.businesslogic.eo;

import webobjectsexamples.businesslogic.eo.Review;

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
public abstract class _Review extends  A10GenericRecord {

	/** ログ・サポート */
	private static Logger log = Logger.getLogger(_Review.class);

	//********************************************************************
	//	コンストラクター
	//********************************************************************

	public _Review() {
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
			if (_Review.log.isDebugEnabled()) {
				_Review.log.debug("addFetchSpecificationToEntity");
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
	
	/** Entity Name = Review */
	public static final String ENTITY_NAME = "Review";

	//********************************************************************
	//	アクセス・プロパティ
	//********************************************************************
	
	public static String ACCSESS_CREATE = "Review.create";
	public static String ACCSESS_READ = "Review.read";
	public static String ACCSESS_UPDATE = "Review.update";
	public static String ACCSESS_DELETE = "Review.delete";

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
	public static final ERXKey<String> REVIEW = new ERXKey<String>("review");
	public static final ERXKey<String> REVIEWER = new ERXKey<String>("reviewer");

	// Attributes
	public static final String REVIEW_KEY = REVIEW.key();
	public static final String REVIEWER_KEY = REVIEWER.key();

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

	public String review() {
		return (String) storedValueForKey(REVIEW_KEY);
	}

	public void setReview(String value) {
		if (_Review.log.isDebugEnabled()) {
			_Review.log.debug( "updating review from " + review() + " to " + value);
		}
		takeStoredValueForKey(value, REVIEW_KEY);
	}

	public void initializeReview(String value) {
		if (_Review.log.isDebugEnabled()) {
			_Review.log.debug( "initialize review to " + value);
		}
		takeStoredValueForKey(value, REVIEW_KEY);
	}

	public Object validateReview(Object value) throws NSValidation.ValidationException {
		if (_Review.log.isDebugEnabled()) {
			_Review.log.debug("validate review");
		}
		return A10ValidationException.validateForUserInfo(this, REVIEW_KEY, value);
	}

	public String reviewer() {
		return (String) storedValueForKey(REVIEWER_KEY);
	}

	public void setReviewer(String value) {
		if (_Review.log.isDebugEnabled()) {
			_Review.log.debug( "updating reviewer from " + reviewer() + " to " + value);
		}
		takeStoredValueForKey(value, REVIEWER_KEY);
	}

	public void initializeReviewer(String value) {
		if (_Review.log.isDebugEnabled()) {
			_Review.log.debug( "initialize reviewer to " + value);
		}
		takeStoredValueForKey(value, REVIEWER_KEY);
	}

	public Object validateReviewer(Object value) throws NSValidation.ValidationException {
		if (_Review.log.isDebugEnabled()) {
			_Review.log.debug("validate reviewer");
		}
		return A10ValidationException.validateForUserInfo(this, REVIEWER_KEY, value);
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
		if (_Review.log.isDebugEnabled()) {
			_Review.log.debug("updating movie from " + movie() + " to " + value);
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
	public Review localInstanceIn(EOEditingContext editingContext) {
		Review localInstance = (Review)EOUtilities.localInstanceOfObject(editingContext, this);
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

	public static Review createReview(EOEditingContext editingContext
		, webobjectsexamples.businesslogic.eo.Movie movie
		)
	{
		Review eo = (Review) EOUtilities.createAndInsertInstance(editingContext, _Review.ENTITY_NAME);    
    	eo.setMovieRelationship(movie);

		return eo;
	}
  
	public static Review createAndInsertInstance(EOEditingContext context) {
		if (log.isDebugEnabled())
			log.debug(ENTITY_NAME + " : createAndInsertInstance");
		
    	return (Review)EOUtilities.createAndInsertInstance(context, ENTITY_NAME);
	}

	@Deprecated
	public static Review newReviewInstance(EOEditingContext context) {		
		return Review.createAndInsertInstance(context);
	}

	// ========== [検索関連] ==========
	//********************************************************************
	//	フェッチ (NSArray)
	//********************************************************************

      public static ERXFetchSpecification<Review> fetchSpec() {
      return new ERXFetchSpecification<Review>(_Review.ENTITY_NAME, null, null, false, true, null);
    }
  
	public static NSArray<Review> fetchAllReviews(EOEditingContext editingContext) {
		return _Review.fetchAllReviews(editingContext, null);
	}

	public static NSArray<Review> fetchAllReviews(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
		return _Review.fetchReviews(editingContext, null, sortOrderings);
	}

	public static NSArray<Review> fetchReviews(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		EOFetchSpecification fetchSpec = new EOFetchSpecification(_Review.ENTITY_NAME, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);
		NSArray<Review> eoObjects = (NSArray<Review>)editingContext.objectsWithFetchSpecification(fetchSpec);
		return eoObjects;
	}

	//********************************************************************
	//	フェッチ (User)
	//********************************************************************
	
	public static Review fetchReview(EOEditingContext editingContext, String keyName, Object value) {
		return _Review.fetchReview(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static Review fetchReview(EOEditingContext editingContext, EOQualifier qualifier) {
		NSArray<Review> eoObjects = _Review.fetchReviews(editingContext, qualifier, null);
		Review eoObject;
		int count = eoObjects.count();
		if (count == 0) {
			eoObject = null;
		} else if (count == 1) {
			eoObject = (Review)eoObjects.objectAtIndex(0);
		} else {
			throw new IllegalStateException("There was more than one Review that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static Review fetchRequiredReview(EOEditingContext editingContext, String keyName, Object value) {
		return _Review.fetchRequiredReview(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static Review fetchRequiredReview(EOEditingContext editingContext, EOQualifier qualifier) {
		Review eoObject = _Review.fetchReview(editingContext, qualifier);
		if (eoObject == null) {
			throw new NoSuchElementException("There was no Review that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static Review fetchReviewByPrimaryKey(EOEditingContext context, Object value) {
	  return Review.fetchReview(context, new EOKeyValueQualifier("id", EOQualifier.QualifierOperatorEqual, value));
	}

	public static Review fetchReviewByEncryptedPrimaryKey(EOEditingContext context, String value) {
	  return Review.fetchReviewByPrimaryKey(context, ERXCrypto.blowfishDecode(value));
	}
	
	public static Review localInstanceIn(EOEditingContext editingContext, Review eo) {
		Review localInstance = (eo == null) ? null : (Review)EOUtilities.localInstanceOfObject(editingContext, eo);
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
