package webobjectsexamples.businesslogic.movies.common;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;

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
    
    @SuppressWarnings("all")
    public String content() {
    	if (log.isDebugEnabled()) {
    		log.debug( "Movie.searchableContent: " + this.title() );
    	}
    	StringBuffer buffer = new StringBuffer();
    	
    	buffer.append(this.title());
    	buffer.append(" ");
    	
    	String studioName = (String)this.valueForKeyPath(Movie.STUDIO.dot(Studio.NameKey).toString());
    	if (studioName != null) {
    		buffer.append(studioName);
    		buffer.append(" ");
    	}
    	
		NSArray directorNames = (NSArray)this.valueForKeyPath(Movie.DIRECTORS.dot("fullName").toString());
		if (directorNames != null && directorNames.count() > 0) {
			buffer.append(directorNames.componentsJoinedByString(" "));
			buffer.append(" ");
		}
		
		NSArray talentNames = (NSArray)this.valueForKeyPath(Movie.ROLES.dot(MovieRole.TALENT).dot("fullName").toString());
		if (talentNames != null && talentNames.count() > 0) {
			String talentNamesString = talentNames.componentsJoinedByString(" ");
			NSLog.out.appendln( "Movie.searchableContent: talent names: " + talentNamesString);
			buffer.append(talentNamesString);
			buffer.append(" ");
		}
		if (log.isDebugEnabled()) {
			log.debug( "Movie.content: " + buffer );
		}
    	return buffer.toString();
    }
}
