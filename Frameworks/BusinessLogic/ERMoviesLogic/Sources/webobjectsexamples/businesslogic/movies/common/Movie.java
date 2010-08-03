package webobjectsexamples.businesslogic.movies.common;

import org.apache.log4j.Logger;

import er.taggable.ERTaggable;
import er.taggable.ERTaggableEntity;

public class Movie extends _Movie {
  private static Logger log = Logger.getLogger(Movie.class);
  
	public ERTaggable<Movie> taggable() {
        return ERTaggable.taggable(this);
    }
    
    public static ERTaggableEntity<Movie> taggableEntity() {
        return ERTaggableEntity.taggableEntity(Movie.ENTITY_NAME);
    }
}
