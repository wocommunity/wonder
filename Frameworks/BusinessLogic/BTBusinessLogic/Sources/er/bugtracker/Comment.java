package er.bugtracker;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSTimestamp;

public class Comment extends _Comment {
    public static final CommentClazz clazz = new CommentClazz();
    public static class CommentClazz extends _Comment._CommentClazz {/* more clazz methods here */}

    public final static String ENTITY = "Comment";

    public interface Key extends _Comment.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    @Override
    public void init(EOEditingContext ec) {
    	super.init(ec);
    	setDateSubmitted(new NSTimestamp());
        setOriginator(People.clazz.currentUser(editingContext()));
    }
}
