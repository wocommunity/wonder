//
// EditOrCreateDirectors.java: Class file for WO Component 'EditOrCreateDirectors'
// Project ERMovies
//
// Created by max on Mon Mar 10 2003
//
package er.examples.movies;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.directtoweb.ERDEditPageInterface;
import er.directtoweb.ERDObjectSaverInterface;
import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXUtilities;
import er.extensions.logging.ERXLogger;

public class EditOrCreateDirectors extends WOComponent {

    public EditOrCreateDirectors(WOContext context) {
        super(context);
    }

    @Override
    public boolean isStateless() {
        return true;
    }

    public EOEnterpriseObject movie() {
        return (EOEnterpriseObject)valueForBinding("object");
    }
    
    public WOComponent newDirector() {
        // Could use a child context if we didn't want the talent to go to the database
        EOEditingContext ec = ERXEC.newEditingContext();
        EOEnterpriseObject director = ERXUtilities.createEO("Talent", ec);
        
        // Let's throw to a regular edit page
        EditPageInterface epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed("EditTalent", session());
        epi.setObject(director);
        epi.setNextPageDelegate(new CreateDirectorDelegate(movie(), context().page()));
        return (WOComponent)epi;
    }

    public static class CreateDirectorDelegate implements NextPageDelegate {

        public static final ERXLogger log = ERXLogger.getERXLogger(CreateDirectorDelegate.class);
        
        protected EOEnterpriseObject movie;
        protected WOComponent nextPage;

        public CreateDirectorDelegate(EOEnterpriseObject movie, WOComponent nextPage) {
            this.movie = movie;
            this.nextPage = nextPage;
        }

        public WOComponent nextPage(WOComponent sender) {
            if (sender instanceof ERDObjectSaverInterface) {
                if (((ERDObjectSaverInterface)sender).objectWasSaved()) {
                    // The user saved the object instead of hitting cancel, better hook it up
                    // to the releationship
                    // Have to use our interface as EditPageInterface doesn't have object().
                    EOEnterpriseObject talent = EOUtilities.localInstanceOfObject(movie.editingContext(),
                                                               ((ERDEditPageInterface)sender).object());
                    movie.addObjectToBothSidesOfRelationshipWithKey(talent, "directors");
                }
            } else {
                log.error("Using CreateDirectorDelegate with a sender that does not implement ERDObjectSaverInterface! Sender class: " + sender.getClass().getName());
            }
            return nextPage;
        }
    }
    
}
