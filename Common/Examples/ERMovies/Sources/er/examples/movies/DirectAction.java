//
// DirectAction.java
// Project ERMovies
//
// Created by max on Thu Feb 27 2003
//
package er.examples.movies;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class DirectAction extends WODirectAction {

    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    public WOActionResults defaultAction() {
        return pageWithName("Main");
    }

}
