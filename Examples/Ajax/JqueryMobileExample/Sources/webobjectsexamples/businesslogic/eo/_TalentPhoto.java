//
// _TalentPhoto.java
//
// Created with WOLips : Thanks to Mike Schrag
//
// DO NOT EDIT. 
// Make changes to TalentPhoto.java instead.
//
// Template created by ishimoto 2012-02-03
//
package webobjectsexamples.businesslogic.eo;

import webobjectsexamples.businesslogic.eo.TalentPhoto;

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
public abstract class _TalentPhoto extends  A10GenericRecord {

	/** ログ・サポート */
	private static Logger log = Logger.getLogger(_TalentPhoto.class);

	//********************************************************************
	//	コンストラクター
	//********************************************************************

	public _TalentPhoto() {
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
			if (_TalentPhoto.log.isDebugEnabled()) {
				_TalentPhoto.log.debug("addFetchSpecificationToEntity");
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
	
	/** Entity Name = TalentPhoto */
	public static final String ENTITY_NAME = "TalentPhoto";

	//********************************************************************
	//	アクセス・プロパティ
	//********************************************************************
	
	public static String ACCSESS_CREATE = "TalentPhoto.create";
	public static String ACCSESS_READ = "TalentPhoto.read";
	public static String ACCSESS_UPDATE = "TalentPhoto.update";
	public static String ACCSESS_DELETE = "TalentPhoto.delete";

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
	public static final ERXKey<NSData> PHOTO = new ERXKey<NSData>("photo");

	// Attributes
	public static final String PHOTO_KEY = PHOTO.key();

	//********************************************************************
	//	リレーションシップ
	//********************************************************************

	// Relationship Keys
	public static final ERXKey<webobjectsexamples.businesslogic.eo.Talent> TALENT = new ERXKey<webobjectsexamples.businesslogic.eo.Talent>("talent");

	// Relationships
	public static final String TALENT_KEY = TALENT.key();

	//********************************************************************
	//	フィールド・アクセス
	//********************************************************************

	public NSData photo() {
		return (NSData) storedValueForKey(PHOTO_KEY);
	}

	public void setPhoto(NSData value) {
		if (_TalentPhoto.log.isDebugEnabled()) {
			_TalentPhoto.log.debug( "updating photo from " + photo() + " to " + value);
		}
		takeStoredValueForKey(value, PHOTO_KEY);
	}

	public void initializePhoto(NSData value) {
		if (_TalentPhoto.log.isDebugEnabled()) {
			_TalentPhoto.log.debug( "initialize photo to " + value);
		}
		takeStoredValueForKey(value, PHOTO_KEY);
	}

	public Object validatePhoto(Object value) throws NSValidation.ValidationException {
		if (_TalentPhoto.log.isDebugEnabled()) {
			_TalentPhoto.log.debug("validate photo");
		}
		return A10ValidationException.validateForUserInfo(this, PHOTO_KEY, value);
	}

	//====================================================================

	//********************************************************************
	//	ToOne リレーションシップ
	//********************************************************************

	public webobjectsexamples.businesslogic.eo.Talent talent() {
		return (webobjectsexamples.businesslogic.eo.Talent)storedValueForKey(TALENT_KEY);
	}

	private void setTalent(webobjectsexamples.businesslogic.eo.Talent value) {
		takeStoredValueForKey(value, TALENT_KEY);
	}
	
	public void setTalentRelationship(webobjectsexamples.businesslogic.eo.Talent value) {
		if (_TalentPhoto.log.isDebugEnabled()) {
			_TalentPhoto.log.debug("updating talent from " + talent() + " to " + value);
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
	public TalentPhoto localInstanceIn(EOEditingContext editingContext) {
		TalentPhoto localInstance = (TalentPhoto)EOUtilities.localInstanceOfObject(editingContext, this);
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

	public static TalentPhoto createTalentPhoto(EOEditingContext editingContext
		, webobjectsexamples.businesslogic.eo.Talent talent
		)
	{
		TalentPhoto eo = (TalentPhoto) EOUtilities.createAndInsertInstance(editingContext, _TalentPhoto.ENTITY_NAME);    
    	eo.setTalentRelationship(talent);

		return eo;
	}
  
	public static TalentPhoto createAndInsertInstance(EOEditingContext context) {
		if (log.isDebugEnabled())
			log.debug(ENTITY_NAME + " : createAndInsertInstance");
		
    	return (TalentPhoto)EOUtilities.createAndInsertInstance(context, ENTITY_NAME);
	}

	@Deprecated
	public static TalentPhoto newTalentPhotoInstance(EOEditingContext context) {		
		return TalentPhoto.createAndInsertInstance(context);
	}

	// ========== [検索関連] ==========
	//********************************************************************
	//	フェッチ (NSArray)
	//********************************************************************

      public static ERXFetchSpecification<TalentPhoto> fetchSpec() {
      return new ERXFetchSpecification<TalentPhoto>(_TalentPhoto.ENTITY_NAME, null, null, false, true, null);
    }
  
	public static NSArray<TalentPhoto> fetchAllTalentPhotos(EOEditingContext editingContext) {
		return _TalentPhoto.fetchAllTalentPhotos(editingContext, null);
	}

	public static NSArray<TalentPhoto> fetchAllTalentPhotos(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
		return _TalentPhoto.fetchTalentPhotos(editingContext, null, sortOrderings);
	}

	public static NSArray<TalentPhoto> fetchTalentPhotos(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		EOFetchSpecification fetchSpec = new EOFetchSpecification(_TalentPhoto.ENTITY_NAME, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);
		NSArray<TalentPhoto> eoObjects = (NSArray<TalentPhoto>)editingContext.objectsWithFetchSpecification(fetchSpec);
		return eoObjects;
	}

	//********************************************************************
	//	フェッチ (User)
	//********************************************************************
	
	public static TalentPhoto fetchTalentPhoto(EOEditingContext editingContext, String keyName, Object value) {
		return _TalentPhoto.fetchTalentPhoto(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static TalentPhoto fetchTalentPhoto(EOEditingContext editingContext, EOQualifier qualifier) {
		NSArray<TalentPhoto> eoObjects = _TalentPhoto.fetchTalentPhotos(editingContext, qualifier, null);
		TalentPhoto eoObject;
		int count = eoObjects.count();
		if (count == 0) {
			eoObject = null;
		} else if (count == 1) {
			eoObject = (TalentPhoto)eoObjects.objectAtIndex(0);
		} else {
			throw new IllegalStateException("There was more than one TalentPhoto that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static TalentPhoto fetchRequiredTalentPhoto(EOEditingContext editingContext, String keyName, Object value) {
		return _TalentPhoto.fetchRequiredTalentPhoto(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static TalentPhoto fetchRequiredTalentPhoto(EOEditingContext editingContext, EOQualifier qualifier) {
		TalentPhoto eoObject = _TalentPhoto.fetchTalentPhoto(editingContext, qualifier);
		if (eoObject == null) {
			throw new NoSuchElementException("There was no TalentPhoto that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static TalentPhoto fetchTalentPhotoByPrimaryKey(EOEditingContext context, Object value) {
	  return TalentPhoto.fetchTalentPhoto(context, new EOKeyValueQualifier("id", EOQualifier.QualifierOperatorEqual, value));
	}

	public static TalentPhoto fetchTalentPhotoByEncryptedPrimaryKey(EOEditingContext context, String value) {
	  return TalentPhoto.fetchTalentPhotoByPrimaryKey(context, ERXCrypto.blowfishDecode(value));
	}
	
	public static TalentPhoto localInstanceIn(EOEditingContext editingContext, TalentPhoto eo) {
		TalentPhoto localInstance = (eo == null) ? null : (TalentPhoto)EOUtilities.localInstanceOfObject(editingContext, eo);
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
