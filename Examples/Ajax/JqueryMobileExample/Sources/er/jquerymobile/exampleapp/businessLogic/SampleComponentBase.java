package er.jquerymobile.exampleapp.businessLogic;

import webobjectsexamples.businesslogic.eo.Movie;
import webobjectsexamples.businesslogic.eo.Talent;
import webobjectsexamples.businesslogic.eo._Movie;
import webobjectsexamples.businesslogic.eo._Talent;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSRange;

import er.extensions.components.ERXComponent;
import er.extensions.eof.ERXEC;
import er.jquerymobile.exampleapp.components.SampleOneMovie;

public class SampleComponentBase extends ERXComponent {

	private static final long serialVersionUID = 1L;

	// ********************************************************************
	// Constructor : コンストラクタ
	// ********************************************************************

	public SampleComponentBase(WOContext aContext) {
		super(aContext);
	}

	// ********************************************************************
	// Methods : メソッド
	// ********************************************************************

	public synchronized NSArray<Talent> talents() {
		if (_talents == null) {
			_talents = _Talent.fetchAllTalents(ERXEC.newEditingContext(), _Talent.FIRST_NAME.asc().array());
		}
		return _talents;
	}

	private static NSArray<Talent> _talents = null;

	public void setOneTalent(Talent oneTalent) {
		_oneTalent = oneTalent;
	}

	public Talent oneTalent() {
		return _oneTalent;
	}

	private Talent _oneTalent;

	public WODisplayGroup talentDisplayGroup() {
		if (_talentDisplayGroup == null) {
			EOArrayDataSource resultDataSource = new EOArrayDataSource(EOClassDescription.classDescriptionForEntityName(_Talent.ENTITY_NAME),
					session().defaultEditingContext());
			resultDataSource.setArray(talents());

			_talentDisplayGroup = new WODisplayGroup();
			_talentDisplayGroup.setNumberOfObjectsPerBatch(10);
			_talentDisplayGroup.setDataSource(resultDataSource);
			_talentDisplayGroup.qualifyDataSource();
			_talentDisplayGroup.setCurrentBatchIndex(1);
		}
		return _talentDisplayGroup;
	}

	private WODisplayGroup _talentDisplayGroup = null;

	public synchronized NSArray<Movie> movies() {
		if (_movies == null) {
			_movies = _Movie.fetchAllMovies(ERXEC.newEditingContext(), _Movie.TITLE.asc().array());
		}
		return _movies;
	}

	private static NSArray<Movie> _movies = null;

	public NSArray<Movie> onlyFiveMovies() {
		NSRange range = new NSRange(0, 5);
		return movies().subarrayWithRange(range);
	}

	public void setOneMovie(Movie oneMovie) {
		_oneMovie = oneMovie;
	}

	public Movie oneMovie() {
		return _oneMovie;
	}

	private Movie _oneMovie;

	// ********************************************************************
	// Actions : アクション
	// ********************************************************************

	public WOActionResults doShowOneMovieAction() {

		System.err.println("doShowOneMovieAction");

		SampleOneMovie nextPage = pageWithName(SampleOneMovie.class);
		nextPage.setOneMovie(oneMovie());
		return nextPage;
	}

	public WOActionResults doShowOneTalentAction() {
		SampleOneMovie nextPage = pageWithName(SampleOneMovie.class);
		nextPage.setOneTalent(oneTalent());
		return nextPage;
	}

}
