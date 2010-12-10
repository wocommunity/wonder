//
// NavigationMenu.java: Class file for WO Component 'NavigationMenu'
// Project ERMovies
//
// Created by max on Mon Mar 03 2003
//
package er.examples.movies;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;

public class NavigationMenu extends WOComponent {

    public NavigationMenu(WOContext context) {
        super(context);
    }

    public NSKeyValueCoding navigationContext() {
        NSKeyValueCoding context = null;
        if (context().page() instanceof D2WPage) {
            context = ((D2WPage)context().page()).d2wContext();
        }
        return context;
    }

    public NSTimestamp now() { return new NSTimestamp(); }

}
