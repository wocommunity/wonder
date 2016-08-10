package er.examples.textsearchdemo.components.sections;

import webobjectsexamples.businesslogic.movies.common.Movie;
import webobjectsexamples.businesslogic.movies.common.Talent;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.examples.textsearchdemo.components.shared.TSCommonComponent;

public class MovieDetails extends TSCommonComponent {
	
	private Movie _movie;
	
    public MovieDetails(WOContext context) {
        super(context);
    }

	/**
	 * @return the movie
	 */
	public Movie movie() {
		return _movie;
	}

	/**
	 * @param movie the movie to set
	 */
	public void setMovie(Movie movie) {
		_movie = movie;
	}

	@SuppressWarnings("unchecked")
	public String movieTalent() {
		StringBuffer talent = new StringBuffer();
		NSArray<Talent> actors = (NSArray<Talent>)valueForKeyPath("movie.roles.talent");
		if (actors != null) {
			for (Talent actor : actors) {
				if (talent.length() != 0) {
					talent.append(", ");
				}
				talent.append(actor.fullName());
			}
		}
		return talent.toString();
	}
	
	@SuppressWarnings("unchecked")
	public String movieDirectors() {
		StringBuffer talent = new StringBuffer();
		NSArray<Talent> directors = (NSArray<Talent>)valueForKeyPath("movie.directors");
		if (directors != null) {
			for (Talent actor : directors) {
				if (talent.length() != 0) {
					talent.append(", ");
				}
				talent.append(actor.fullName());
			}
		}
		return talent.toString();
	}
    
}