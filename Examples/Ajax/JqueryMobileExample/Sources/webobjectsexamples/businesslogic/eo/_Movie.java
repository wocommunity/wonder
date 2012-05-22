//
// _Movie.java
//
// Created with WOLips : Thanks to Mike Schrag
//
// DO NOT EDIT. 
// Make changes to Movie.java instead.
//
// Template created by ishimoto 2012-02-03
//
package webobjectsexamples.businesslogic.eo;

import webobjectsexamples.businesslogic.eo.Movie;

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
public abstract class _Movie extends  A10GenericRecord {

	/** ログ・サポート */
	private static Logger log = Logger.getLogger(_Movie.class);

	//********************************************************************
	//	コンストラクター
	//********************************************************************

	public _Movie() {
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
			if (_Movie.log.isDebugEnabled()) {
				_Movie.log.debug("addFetchSpecificationToEntity");
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
	
	/** Entity Name = Movie */
	public static final String ENTITY_NAME = "Movie";

	//********************************************************************
	//	アクセス・プロパティ
	//********************************************************************
	
	public static String ACCSESS_CREATE = "Movie.create";
	public static String ACCSESS_READ = "Movie.read";
	public static String ACCSESS_UPDATE = "Movie.update";
	public static String ACCSESS_DELETE = "Movie.delete";

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
	public static final ERXKey<String> CATEGORY = new ERXKey<String>("category");
	public static final ERXKey<NSTimestamp> DATE_RELEASED = new ERXKey<NSTimestamp>("dateReleased");
	public static final ERXKey<String> POSTER_NAME = new ERXKey<String>("posterName");
	public static final ERXKey<String> RATED = new ERXKey<String>("rated");
	public static final ERXKey<java.math.BigDecimal> REVENUE = new ERXKey<java.math.BigDecimal>("revenue");
	public static final ERXKey<String> TITLE = new ERXKey<String>("title");
	public static final ERXKey<String> TRAILER_NAME = new ERXKey<String>("trailerName");

	// Attributes
	public static final String CATEGORY_KEY = CATEGORY.key();
	public static final String DATE_RELEASED_KEY = DATE_RELEASED.key();
	public static final String POSTER_NAME_KEY = POSTER_NAME.key();
	public static final String RATED_KEY = RATED.key();
	public static final String REVENUE_KEY = REVENUE.key();
	public static final String TITLE_KEY = TITLE.key();
	public static final String TRAILER_NAME_KEY = TRAILER_NAME.key();

	//********************************************************************
	//	リレーションシップ
	//********************************************************************

	// Relationship Keys
	public static final ERXKey<webobjectsexamples.businesslogic.eo.Talent> DIRECTORS = new ERXKey<webobjectsexamples.businesslogic.eo.Talent>("directors");
	public static final ERXKey<webobjectsexamples.businesslogic.eo.PlotSummary> PLOT_SUMMARY = new ERXKey<webobjectsexamples.businesslogic.eo.PlotSummary>("plotSummary");
	public static final ERXKey<webobjectsexamples.businesslogic.eo.Review> REVIEWS = new ERXKey<webobjectsexamples.businesslogic.eo.Review>("reviews");
	public static final ERXKey<webobjectsexamples.businesslogic.eo.MovieRole> ROLES = new ERXKey<webobjectsexamples.businesslogic.eo.MovieRole>("roles");
	public static final ERXKey<webobjectsexamples.businesslogic.eo.Studio> STUDIO = new ERXKey<webobjectsexamples.businesslogic.eo.Studio>("studio");
	public static final ERXKey<webobjectsexamples.businesslogic.eo.Voting> VOTING = new ERXKey<webobjectsexamples.businesslogic.eo.Voting>("voting");

	// Relationships
	public static final String DIRECTORS_KEY = DIRECTORS.key();
	public static final String PLOT_SUMMARY_KEY = PLOT_SUMMARY.key();
	public static final String REVIEWS_KEY = REVIEWS.key();
	public static final String ROLES_KEY = ROLES.key();
	public static final String STUDIO_KEY = STUDIO.key();
	public static final String VOTING_KEY = VOTING.key();

	//********************************************************************
	//	フィールド・アクセス
	//********************************************************************

	public String category() {
		return (String) storedValueForKey(CATEGORY_KEY);
	}

	public void setCategory(String value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "updating category from " + category() + " to " + value);
		}
		takeStoredValueForKey(value, CATEGORY_KEY);
	}

	public void initializeCategory(String value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "initialize category to " + value);
		}
		takeStoredValueForKey(value, CATEGORY_KEY);
	}

	public Object validateCategory(Object value) throws NSValidation.ValidationException {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("validate category");
		}
		return A10ValidationException.validateForUserInfo(this, CATEGORY_KEY, value);
	}

	public NSTimestamp dateReleased() {
		return (NSTimestamp) storedValueForKey(DATE_RELEASED_KEY);
	}

	public void setDateReleased(NSTimestamp value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "updating dateReleased from " + dateReleased() + " to " + value);
		}
		takeStoredValueForKey(value, DATE_RELEASED_KEY);
	}

	public void initializeDateReleased(NSTimestamp value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "initialize dateReleased to " + value);
		}
		takeStoredValueForKey(value, DATE_RELEASED_KEY);
	}

	public Object validateDateReleased(Object value) throws NSValidation.ValidationException {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("validate dateReleased");
		}
		return A10ValidationException.validateForUserInfo(this, DATE_RELEASED_KEY, value);
	}

	public String posterName() {
		return (String) storedValueForKey(POSTER_NAME_KEY);
	}

	public void setPosterName(String value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "updating posterName from " + posterName() + " to " + value);
		}
		takeStoredValueForKey(value, POSTER_NAME_KEY);
	}

	public void initializePosterName(String value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "initialize posterName to " + value);
		}
		takeStoredValueForKey(value, POSTER_NAME_KEY);
	}

	public Object validatePosterName(Object value) throws NSValidation.ValidationException {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("validate posterName");
		}
		return A10ValidationException.validateForUserInfo(this, POSTER_NAME_KEY, value);
	}

	public String rated() {
		return (String) storedValueForKey(RATED_KEY);
	}

	public void setRated(String value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "updating rated from " + rated() + " to " + value);
		}
		takeStoredValueForKey(value, RATED_KEY);
	}

	public void initializeRated(String value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "initialize rated to " + value);
		}
		takeStoredValueForKey(value, RATED_KEY);
	}

	public Object validateRated(Object value) throws NSValidation.ValidationException {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("validate rated");
		}
		return A10ValidationException.validateForUserInfo(this, RATED_KEY, value);
	}

	public java.math.BigDecimal revenue() {
		return (java.math.BigDecimal) storedValueForKey(REVENUE_KEY);
	}

	public void setRevenue(java.math.BigDecimal value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "updating revenue from " + revenue() + " to " + value);
		}
		takeStoredValueForKey(value, REVENUE_KEY);
	}

	public void initializeRevenue(java.math.BigDecimal value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "initialize revenue to " + value);
		}
		takeStoredValueForKey(value, REVENUE_KEY);
	}

	public Object validateRevenue(Object value) throws NSValidation.ValidationException {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("validate revenue");
		}
		return A10ValidationException.validateForUserInfo(this, REVENUE_KEY, value);
	}

	public String title() {
		return (String) storedValueForKey(TITLE_KEY);
	}

	public void setTitle(String value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "updating title from " + title() + " to " + value);
		}
		takeStoredValueForKey(value, TITLE_KEY);
	}

	public void initializeTitle(String value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "initialize title to " + value);
		}
		takeStoredValueForKey(value, TITLE_KEY);
	}

	public Object validateTitle(Object value) throws NSValidation.ValidationException {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("validate title");
		}
		return A10ValidationException.validateForUserInfo(this, TITLE_KEY, value);
	}

	public String trailerName() {
		return (String) storedValueForKey(TRAILER_NAME_KEY);
	}

	public void setTrailerName(String value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "updating trailerName from " + trailerName() + " to " + value);
		}
		takeStoredValueForKey(value, TRAILER_NAME_KEY);
	}

	public void initializeTrailerName(String value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug( "initialize trailerName to " + value);
		}
		takeStoredValueForKey(value, TRAILER_NAME_KEY);
	}

	public Object validateTrailerName(Object value) throws NSValidation.ValidationException {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("validate trailerName");
		}
		return A10ValidationException.validateForUserInfo(this, TRAILER_NAME_KEY, value);
	}

	//====================================================================

	//********************************************************************
	//	ToOne リレーションシップ
	//********************************************************************

	public webobjectsexamples.businesslogic.eo.PlotSummary plotSummary() {
		return (webobjectsexamples.businesslogic.eo.PlotSummary)storedValueForKey(PLOT_SUMMARY_KEY);
	}

	private void setPlotSummary(webobjectsexamples.businesslogic.eo.PlotSummary value) {
		takeStoredValueForKey(value, PLOT_SUMMARY_KEY);
	}
	
	public void setPlotSummaryRelationship(webobjectsexamples.businesslogic.eo.PlotSummary value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("updating plotSummary from " + plotSummary() + " to " + value);
		}
		if (ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			setPlotSummary(value);
		} else if (value == null) {
			webobjectsexamples.businesslogic.eo.PlotSummary oldValue = plotSummary();
			if (oldValue != null) {
				removeObjectFromBothSidesOfRelationshipWithKey(oldValue, PLOT_SUMMARY_KEY);
			}
		} else {
			addObjectToBothSidesOfRelationshipWithKey(value, PLOT_SUMMARY_KEY);
		}
	}
  
	public webobjectsexamples.businesslogic.eo.Studio studio() {
		return (webobjectsexamples.businesslogic.eo.Studio)storedValueForKey(STUDIO_KEY);
	}

	private void setStudio(webobjectsexamples.businesslogic.eo.Studio value) {
		takeStoredValueForKey(value, STUDIO_KEY);
	}
	
	public void setStudioRelationship(webobjectsexamples.businesslogic.eo.Studio value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("updating studio from " + studio() + " to " + value);
		}
		if (ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			setStudio(value);
		} else if (value == null) {
			webobjectsexamples.businesslogic.eo.Studio oldValue = studio();
			if (oldValue != null) {
				removeObjectFromBothSidesOfRelationshipWithKey(oldValue, STUDIO_KEY);
			}
		} else {
			addObjectToBothSidesOfRelationshipWithKey(value, STUDIO_KEY);
		}
	}
  
	public webobjectsexamples.businesslogic.eo.Voting voting() {
		return (webobjectsexamples.businesslogic.eo.Voting)storedValueForKey(VOTING_KEY);
	}

	private void setVoting(webobjectsexamples.businesslogic.eo.Voting value) {
		takeStoredValueForKey(value, VOTING_KEY);
	}
	
	public void setVotingRelationship(webobjectsexamples.businesslogic.eo.Voting value) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("updating voting from " + voting() + " to " + value);
		}
		if (ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			setVoting(value);
		} else if (value == null) {
			webobjectsexamples.businesslogic.eo.Voting oldValue = voting();
			if (oldValue != null) {
				removeObjectFromBothSidesOfRelationshipWithKey(oldValue, VOTING_KEY);
			}
		} else {
			addObjectToBothSidesOfRelationshipWithKey(value, VOTING_KEY);
		}
	}
  
	//********************************************************************
	//	編集コンテキスト入れ替え
	//********************************************************************

	@Override
	public Movie localInstanceIn(EOEditingContext editingContext) {
		Movie localInstance = (Movie)EOUtilities.localInstanceOfObject(editingContext, this);
	    if (localInstance == null) {
	    	throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
	    }
	    return localInstance;
	}

	//********************************************************************
	//	ToMany リレーションシップ
	//********************************************************************

	public NSArray<webobjectsexamples.businesslogic.eo.Talent> directors() {
		return (NSArray<webobjectsexamples.businesslogic.eo.Talent>)storedValueForKey("directors");
	}

	public NSArray<webobjectsexamples.businesslogic.eo.Talent> directors(EOQualifier qualifier) {
		return directors(qualifier, null);
	}

	public NSArray<webobjectsexamples.businesslogic.eo.Talent> directors(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		NSArray<webobjectsexamples.businesslogic.eo.Talent> results;
			results = directors();
			if (qualifier != null) {
				results = (NSArray<webobjectsexamples.businesslogic.eo.Talent>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
			}
			if (sortOrderings != null) {
				results = (NSArray<webobjectsexamples.businesslogic.eo.Talent>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
			}
    	return results;
	}
  
	public void addToDirectors(webobjectsexamples.businesslogic.eo.Talent object) {
		includeObjectIntoPropertyWithKey(object, "directors");
	}

	public void removeFromDirectors(webobjectsexamples.businesslogic.eo.Talent object) {
		excludeObjectFromPropertyWithKey(object, "directors");
	}

	public void addToDirectorsRelationship(webobjectsexamples.businesslogic.eo.Talent object) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("adding " + object + " to directors relationship");
		}
		if (A10GenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			addToDirectors(object);
		} else {
			addObjectToBothSidesOfRelationshipWithKey(object, "directors");
		}
	}

	public void removeFromDirectorsRelationship(webobjectsexamples.businesslogic.eo.Talent object) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("removing " + object + " from directors relationship");
		}
		if (A10GenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			removeFromDirectors(object);
		} else {
			removeObjectFromBothSidesOfRelationshipWithKey(object, "directors");
		}
	}

	public webobjectsexamples.businesslogic.eo.Talent createDirectorsRelationship() {
		EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Talent");
		EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
		editingContext().insertObject(eo);
		addObjectToBothSidesOfRelationshipWithKey(eo, "directors");
		return (webobjectsexamples.businesslogic.eo.Talent) eo;
	}

	public void deleteDirectorsRelationship(webobjectsexamples.businesslogic.eo.Talent object) {
		removeObjectFromBothSidesOfRelationshipWithKey(object, "directors");
    	editingContext().deleteObject(object);
	}

	public void deleteAllDirectorsRelationships() {
		Enumeration objects = directors().immutableClone().objectEnumerator();
		while (objects.hasMoreElements()) {
			deleteDirectorsRelationship((webobjectsexamples.businesslogic.eo.Talent)objects.nextElement());
		}
	}

	public NSArray<webobjectsexamples.businesslogic.eo.Review> reviews() {
		return (NSArray<webobjectsexamples.businesslogic.eo.Review>)storedValueForKey("reviews");
	}

	public NSArray<webobjectsexamples.businesslogic.eo.Review> reviews(EOQualifier qualifier) {
		return reviews(qualifier, null, false);
	}

	public NSArray<webobjectsexamples.businesslogic.eo.Review> reviews(EOQualifier qualifier, boolean fetch) {
		return reviews(qualifier, null, fetch);
	}

	public NSArray<webobjectsexamples.businesslogic.eo.Review> reviews(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
		NSArray<webobjectsexamples.businesslogic.eo.Review> results;
		if (fetch) {
			EOQualifier fullQualifier;
      		EOQualifier inverseQualifier = new EOKeyValueQualifier(webobjectsexamples.businesslogic.eo.Review.MOVIE_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      		if (qualifier == null) {
      			fullQualifier = inverseQualifier;
      		} else {
      			NSMutableArray qualifiers = new NSMutableArray();
      			qualifiers.addObject(qualifier);
      			qualifiers.addObject(inverseQualifier);
      			fullQualifier = new EOAndQualifier(qualifiers);
      		}

      		results = webobjectsexamples.businesslogic.eo.Review.fetchReviews(editingContext(), fullQualifier, sortOrderings);
		} else {
			results = reviews();
			if (qualifier != null) {
				results = (NSArray<webobjectsexamples.businesslogic.eo.Review>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
			}
			if (sortOrderings != null) {
				results = (NSArray<webobjectsexamples.businesslogic.eo.Review>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
			}
		}
    	return results;
	}
  
	public void addToReviews(webobjectsexamples.businesslogic.eo.Review object) {
		includeObjectIntoPropertyWithKey(object, "reviews");
	}

	public void removeFromReviews(webobjectsexamples.businesslogic.eo.Review object) {
		excludeObjectFromPropertyWithKey(object, "reviews");
	}

	public void addToReviewsRelationship(webobjectsexamples.businesslogic.eo.Review object) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("adding " + object + " to reviews relationship");
		}
		if (A10GenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			addToReviews(object);
		} else {
			addObjectToBothSidesOfRelationshipWithKey(object, "reviews");
		}
	}

	public void removeFromReviewsRelationship(webobjectsexamples.businesslogic.eo.Review object) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("removing " + object + " from reviews relationship");
		}
		if (A10GenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			removeFromReviews(object);
		} else {
			removeObjectFromBothSidesOfRelationshipWithKey(object, "reviews");
		}
	}

	public webobjectsexamples.businesslogic.eo.Review createReviewsRelationship() {
		EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Review");
		EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
		editingContext().insertObject(eo);
		addObjectToBothSidesOfRelationshipWithKey(eo, "reviews");
		return (webobjectsexamples.businesslogic.eo.Review) eo;
	}

	public void deleteReviewsRelationship(webobjectsexamples.businesslogic.eo.Review object) {
		removeObjectFromBothSidesOfRelationshipWithKey(object, "reviews");
	}

	public void deleteAllReviewsRelationships() {
		Enumeration objects = reviews().immutableClone().objectEnumerator();
		while (objects.hasMoreElements()) {
			deleteReviewsRelationship((webobjectsexamples.businesslogic.eo.Review)objects.nextElement());
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
      		EOQualifier inverseQualifier = new EOKeyValueQualifier(webobjectsexamples.businesslogic.eo.MovieRole.MOVIE_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
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
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("adding " + object + " to roles relationship");
		}
		if (A10GenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
			addToRoles(object);
		} else {
			addObjectToBothSidesOfRelationshipWithKey(object, "roles");
		}
	}

	public void removeFromRolesRelationship(webobjectsexamples.businesslogic.eo.MovieRole object) {
		if (_Movie.log.isDebugEnabled()) {
			_Movie.log.debug("removing " + object + " from roles relationship");
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

	public static Movie createMovie(EOEditingContext editingContext
		, String title
		)
	{
		Movie eo = (Movie) EOUtilities.createAndInsertInstance(editingContext, _Movie.ENTITY_NAME);    
		eo.setTitle(title);

		return eo;
	}
  
	public static Movie createAndInsertInstance(EOEditingContext context) {
		if (log.isDebugEnabled())
			log.debug(ENTITY_NAME + " : createAndInsertInstance");
		
    	return (Movie)EOUtilities.createAndInsertInstance(context, ENTITY_NAME);
	}

	@Deprecated
	public static Movie newMovieInstance(EOEditingContext context) {		
		return Movie.createAndInsertInstance(context);
	}

	// ========== [検索関連] ==========
	//********************************************************************
	//	フェッチ (NSArray)
	//********************************************************************

      public static ERXFetchSpecification<Movie> fetchSpec() {
      return new ERXFetchSpecification<Movie>(_Movie.ENTITY_NAME, null, null, false, true, null);
    }
  
	public static NSArray<Movie> fetchAllMovies(EOEditingContext editingContext) {
		return _Movie.fetchAllMovies(editingContext, null);
	}

	public static NSArray<Movie> fetchAllMovies(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
		return _Movie.fetchMovies(editingContext, null, sortOrderings);
	}

	public static NSArray<Movie> fetchMovies(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		EOFetchSpecification fetchSpec = new EOFetchSpecification(_Movie.ENTITY_NAME, qualifier, sortOrderings);
		fetchSpec.setIsDeep(true);
		NSArray<Movie> eoObjects = (NSArray<Movie>)editingContext.objectsWithFetchSpecification(fetchSpec);
		return eoObjects;
	}

	//********************************************************************
	//	フェッチ (User)
	//********************************************************************
	
	public static Movie fetchMovie(EOEditingContext editingContext, String keyName, Object value) {
		return _Movie.fetchMovie(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static Movie fetchMovie(EOEditingContext editingContext, EOQualifier qualifier) {
		NSArray<Movie> eoObjects = _Movie.fetchMovies(editingContext, qualifier, null);
		Movie eoObject;
		int count = eoObjects.count();
		if (count == 0) {
			eoObject = null;
		} else if (count == 1) {
			eoObject = (Movie)eoObjects.objectAtIndex(0);
		} else {
			throw new IllegalStateException("There was more than one Movie that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static Movie fetchRequiredMovie(EOEditingContext editingContext, String keyName, Object value) {
		return _Movie.fetchRequiredMovie(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
	}

	public static Movie fetchRequiredMovie(EOEditingContext editingContext, EOQualifier qualifier) {
		Movie eoObject = _Movie.fetchMovie(editingContext, qualifier);
		if (eoObject == null) {
			throw new NoSuchElementException("There was no Movie that matched the qualifier '" + qualifier + "'.");
		}
		return eoObject;
	}

	public static Movie fetchMovieByPrimaryKey(EOEditingContext context, Object value) {
	  return Movie.fetchMovie(context, new EOKeyValueQualifier("id", EOQualifier.QualifierOperatorEqual, value));
	}

	public static Movie fetchMovieByEncryptedPrimaryKey(EOEditingContext context, String value) {
	  return Movie.fetchMovieByPrimaryKey(context, ERXCrypto.blowfishDecode(value));
	}
	
	public static Movie localInstanceIn(EOEditingContext editingContext, Movie eo) {
		Movie localInstance = (eo == null) ? null : (Movie)EOUtilities.localInstanceOfObject(editingContext, eo);
		if (localInstance == null && eo != null) {
			throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
		}
		return localInstance;
	}

	// ========== [Fetch Specification] ==========

	//********************************************************************
	//	フェッチ・スペシフィケーション
	//********************************************************************

	public static NSArray<webobjectsexamples.businesslogic.eo.Movie> fetchDeepFetchOneMovie(EOEditingContext editingContext, NSDictionary<String, Object> bindings) {
		EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("DeepFetchOneMovie", "Movie");
		fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
		return (NSArray<webobjectsexamples.businesslogic.eo.Movie>)editingContext.objectsWithFetchSpecification(fetchSpec);
	}
  
	public static NSArray<webobjectsexamples.businesslogic.eo.Movie> fetchDeepFetchOneMovie(EOEditingContext editingContext,
			Integer myMovieBinding) {
		EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("DeepFetchOneMovie", "Movie");
    	NSMutableDictionary<String, Object> bindings = new NSMutableDictionary<String, Object>();
    	bindings.takeValueForKey(myMovieBinding, "myMovie");
		fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
    	return (NSArray<webobjectsexamples.businesslogic.eo.Movie>)editingContext.objectsWithFetchSpecification(fetchSpec);
	}
  
	public static NSArray<webobjectsexamples.businesslogic.eo.Movie> fetchQualifierVariable(EOEditingContext editingContext, NSDictionary<String, Object> bindings) {
		EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("QualifierVariable", "Movie");
		fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
		return (NSArray<webobjectsexamples.businesslogic.eo.Movie>)editingContext.objectsWithFetchSpecification(fetchSpec);
	}
  
	public static NSArray<webobjectsexamples.businesslogic.eo.Movie> fetchQualifierVariable(EOEditingContext editingContext,
			java.math.BigDecimal revenueBinding,
			webobjectsexamples.businesslogic.eo.Studio studioBinding,
			String studioNameBinding,
			String titleBinding) {
		EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("QualifierVariable", "Movie");
    	NSMutableDictionary<String, Object> bindings = new NSMutableDictionary<String, Object>();
    	bindings.takeValueForKey(revenueBinding, "revenue");
    	bindings.takeValueForKey(studioBinding, "studio");
    	bindings.takeValueForKey(studioNameBinding, "studioName");
    	bindings.takeValueForKey(titleBinding, "title");
		fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
    	return (NSArray<webobjectsexamples.businesslogic.eo.Movie>)editingContext.objectsWithFetchSpecification(fetchSpec);
	}
  
	public static NSArray<NSDictionary> fetchRawFetchAllMovies(EOEditingContext editingContext, NSDictionary<String, Object> bindings) {
		EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("RawFetchAllMovies", "Movie");
		fetchSpec = fetchSpec.fetchSpecificationWithQualifierBindings(bindings);
		return (NSArray<NSDictionary>)editingContext.objectsWithFetchSpecification(fetchSpec);
	}
  
	public static NSArray<NSDictionary> fetchRawFetchAllMovies(EOEditingContext editingContext) {
		EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("RawFetchAllMovies", "Movie");
    	return (NSArray<NSDictionary>)editingContext.objectsWithFetchSpecification(fetchSpec);
	}
  
}
