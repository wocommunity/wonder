package er.movies.components;


import webobjectsexamples.businesslogic.movies.common.Movie;
import webobjectsexamples.businesslogic.movies.common.MovieRole;
import webobjectsexamples.businesslogic.movies.common.Talent;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORedirect;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.components.ERXComponent;
import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXStringUtilities;
import er.movies.Session;
import er.taggable.ERTaggable;

public class MovieDetail extends ERXComponent {
    
    public Talent directorItem;
    public MovieRole movieRoleItem;
    private EOEditingContext editingContext;
    
    public MovieDetail(WOContext context) {
        super(context);
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
        ERXResponseRewriter.addStylesheetResourceInHead(response, context, "app", "MovieDetail.css");
    }
    
    public EOEditingContext editingContext() {
        if (editingContext == null) 
            editingContext = ERXEC.newEditingContext();
        return editingContext;
    }
    
    public Movie movie() {
        return ((Session)session()).movieDisplayGroup().selectedObject().localInstanceIn(editingContext());
    }
    
    public ERTaggable<Movie> movieTaggable() {
        return movie().taggable();
    }

    public WOActionResults returnToList() {
        editingContext = null;
        ((Session)session()).movieDisplayGroup().setSelectedObject(null);
        return null;
    }

    public NSArray<MovieRole> movieRolesSorted() {
        return movie().roles(null, MovieRole.TALENT.dot(Talent.LAST_NAME).ascInsensitives(), false);
    }

    public WOActionResults saveChanges() {
        editingContext().saveChanges();
        return null;
    }

    public WOActionResults discardChanges() {
        editingContext().revert();
        return null;
    }

    public WOActionResults imageBrowser() {
        WORedirect redirect = new WORedirect(context());
        redirect.setUrl("http://images.google.com/images?q=movie+" + ERXStringUtilities.urlEncode(movie().title()));
        return redirect;
    }

}