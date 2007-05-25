package er.bugtracker;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSTimestamp;

public class Comment extends _Comment {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Comment.class);

    public static final CommentClazz clazz = new CommentClazz();
    public static class CommentClazz extends _Comment._CommentClazz {/* more clazz methods here */}

    public final static String ENTITY = "Comment";

    public interface Key extends _Comment.Key {}

    /**
     * Intitializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
    	super.init(ec);
    	setDateSubmitted(new NSTimestamp());
        updateOriginator(People.clazz.currentUser(editingContext()));
    }

    public void updateOriginator(People people) {
        addObjectToBothSidesOfRelationshipWithKey(people, Key.ORIGINATOR);
    }
}
