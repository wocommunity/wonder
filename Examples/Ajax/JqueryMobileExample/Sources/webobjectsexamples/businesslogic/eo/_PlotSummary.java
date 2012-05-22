//
// _PlotSummary.java
//
// Created with WOLips : Thanks to Mike Schrag
//
// DO NOT EDIT. 
// Make changes to PlotSummary.java instead.
//
// Template created by ishimoto 2012-02-03
//
package webobjectsexamples.businesslogic.eo;

import webobjectsexamples.businesslogic.eo.PlotSummary;

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
public abstract class _PlotSummary extends  A10GenericRecord {

	/** ログ・サポート */
	private static Logger log = Logger.getLogger(_PlotSummary.class);

	//********************************************************************
	//	コンストラクター
	//********************************************************************

	public _PlotSummary() {
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
			if (_PlotSummary.log.isDebugEnabled()) {
				_PlotSummary.log.debug("addFetchSpecificationToEntity");
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
	
	/** Entity Name = PlotSummary */
	public static final String ENTITY_NAME = "PlotSummary";

	//********************************************************************
	//	アクセス・プロパティ
	//********************************************************************
	
	public static String ACCSESS_CREATE = "PlotSummary.create";
	public static String ACCSESS_READ = "PlotSummary.read";
	public static String ACCSESS_UPDATE = "PlotSummary.update";
	public static String ACCSESS_DELETE = "PlotSummary.delete";

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
	public static final ERXKey<String> SUMMARY = new ERXKey<String>("summary");

	// Attributes
	public static final String SUMMARY_KEY = SUMMARY.key();

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

	public String summary() {
		return (String) storedValueForKey(SUMMARY_KEY);
	}

	public void setSummary(String value) {
		if (_PlotSummary.log.isDebugEnabled()) {
			_PlotSummary.log.debug( "updating summary from " + summary() + " to " + value);
		}
		takeStoredValueForKey(value, SUMMARY_KEY);
	}

	public void initializeSummary(String value) {
		if (_PlotSummary.log.isDebugEnabled()) {
			_PlotSummary.log.debug( "initialize summary to " + value);
		}
		takeStoredValueForKey(value, SUMMARY_KEY);
	}

	public Object validateSummary(Object value) throws NSValidation.ValidationException {
		if (_PlotSummary.log.isDebugEnabled()) {
			_PlotSummary.log.debug("validate summary");
		}
		return A10ValidationException.validateForUserInfo(this, SUMMARY_KEY, value);
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
		if (_PlotSummary.log.isDebugEnabled()) {
			_PlotSummary.log.debug("updating movie from " + movie() + " to " + value);
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
	public PlotSummary localInstanceIn(EOEditingContext editingContext) {
		PlotSummary localInstance = (PlotSummary)EOUtilities.localInstanceOfObject(editingContext, this);
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

	public static PlotSummary createPlotSummary(EOEditingContext editingContext
		, webobjectsexamples.businesslogic.eo.Movie movie
		)
	{
		PlotSummary eo = (PlotSummary) EOUtilities.createAndInsertInstance(editingContext, _PlotSummary.ENTITY_NAME);    
    	eo.setMovieRelationship(movie);

		return eo;
	}
  
	public static PlotSummary createAndInsertInstance(EOEditingContext context) {
		if (log.isDebugEnabled())
			log.debug(ENTITY_NAME + " : createAndInsertInstance");
		
    	return (PlotSummary)EOUtilities.createAndInsertInstance(context, ENTITY_NAME);
	}

	@Deprecated
	public static PlotSummary newPlotSummaryInstance(EOEditingContext context) {		
		return PlotSummary.createAndInsertInstance(context);
	}

	// ========== [検索関連] ==========
	//********************************************************************
	//	フェッチ (NSArray)
	//********************************************************************

      public static ERXFetchSpecification<PlotSummary> fetchSpec() {
      return new ERXFetchSpecification<PlotSummary>(_PlotSummary.ENTITY_NAME, null, null, false, true, null);
    }
  
	public static NSArray<PlotSummary> fetchAllPlotSummaries(EOEditingContext editingContext) {
		return _PlotSummary.fetchAllPlotSummaries(editingContext, null);
	}

	public static NSArray<PlotSummary> fetchAllPlotSummaries(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
		return _PlotSummary.fetchPlotSummaries(editingContext, null, sortOrderings);
	}

	public static NSArray<PlotSummary> fetchPlotSummaries(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		EOFetchSpecification fetchSpec = new EOFetchSpecification(_PlotSummary.ENTITY_NAME, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);
		NSArray<PlotSummary> eoObjects = (NSArray<PlotSummary>)editingContext.objectsWithFetchSpecification(fetchSpec);
		return eoObjects;
	}

	//********************************************************************
	//	フェッチ (User)
	//********************************************************************
	
	public static PlotSummary fetchPlotSummary(EOEditingContext editingContext, String keyName, Object value) {
		return _PlotSummary.fetchPlotSummary(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static PlotSummary fetchPlotSummary(EOEditingContext editingContext, EOQualifier qualifier) {
		NSArray<PlotSummary> eoObjects = _PlotSummary.fetchPlotSummaries(editingContext, qualifier, null);
		PlotSummary eoObject;
		int count = eoObjects.count();
		if (count == 0) {
			eoObject = null;
		} else if (count == 1) {
			eoObject = (PlotSummary)eoObjects.objectAtIndex(0);
		} else {
			throw new IllegalStateException("There was more than one PlotSummary that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static PlotSummary fetchRequiredPlotSummary(EOEditingContext editingContext, String keyName, Object value) {
		return _PlotSummary.fetchRequiredPlotSummary(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static PlotSummary fetchRequiredPlotSummary(EOEditingContext editingContext, EOQualifier qualifier) {
		PlotSummary eoObject = _PlotSummary.fetchPlotSummary(editingContext, qualifier);
		if (eoObject == null) {
			throw new NoSuchElementException("There was no PlotSummary that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static PlotSummary fetchPlotSummaryByPrimaryKey(EOEditingContext context, Object value) {
	  return PlotSummary.fetchPlotSummary(context, new EOKeyValueQualifier("id", EOQualifier.QualifierOperatorEqual, value));
	}

	public static PlotSummary fetchPlotSummaryByEncryptedPrimaryKey(EOEditingContext context, String value) {
	  return PlotSummary.fetchPlotSummaryByPrimaryKey(context, ERXCrypto.blowfishDecode(value));
	}
	
	public static PlotSummary localInstanceIn(EOEditingContext editingContext, PlotSummary eo) {
		PlotSummary localInstance = (eo == null) ? null : (PlotSummary)EOUtilities.localInstanceOfObject(editingContext, eo);
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
