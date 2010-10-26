package er.movies.components;


import webobjectsexamples.businesslogic.movies.common.Movie;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.components.ERXComponent;
import er.movies.Session;

public class MovieList extends ERXComponent {
    
    public Movie movieItem;
    public int loopIndex;
    public String tag;
    public String tagClass;
    
    public MovieList(WOContext context) {
        super(context);
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
        ERXResponseRewriter.addStylesheetResourceInHead(response, context, "app", "MovieList.css");
        ERXResponseRewriter.addStylesheetResourceInHead(response, context, "ERExtensions", "ERXFlickrBatchNavigation.css");
        ERXResponseRewriter.addStylesheetResourceInHead(response, context, "ERTaggable", "ERTagCloud.css");
    }
    
    public String alternateRowClass() {
        return (loopIndex % 2 == 0) ? "" : "alternate";
    }
    
    public WOActionResults selectMovie() {
        ((Session)session()).movieDisplayGroup().setSelectedObject(movieItem);
        return null;
    }
    
}