package com.ksroom.businessLogic;

import webobjectsexamples.businesslogic.movies.common.Movie;
import webobjectsexamples.businesslogic.movies.common.Talent;

import com.ksroom.components.SampleOneMovie;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSRange;

import er.extensions.components.ERXComponent;
import er.extensions.eof.ERXEC;

@SuppressWarnings("serial")
public class SampleComponentBase extends ERXComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public SampleComponentBase(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Demo Methods
  //********************************************************************

  public NSArray<Talent> talents() {
    return Talent.fetchAllTalents(ERXEC.newEditingContext(), Talent.FIRST_NAME.asc().array());
  }

  public void setOneTalent(Talent oneTalent) {
    _oneTalent = oneTalent;
  }
  public Talent oneTalent() {
    return _oneTalent;
  }
  private Talent _oneTalent;

  public NSArray<Movie> movies() {
    return Movie.fetchAllMovies(ERXEC.newEditingContext(), Movie.TITLE.asc().array());
  }

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

  //********************************************************************
  //  Action
  //********************************************************************

  public WOActionResults doShowOneMovieAction() {
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
