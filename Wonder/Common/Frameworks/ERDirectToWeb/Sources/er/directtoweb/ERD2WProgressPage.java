package er.directtoweb;

import com.webobjects.appserver.WOContext;

import er.extensions.ERXLogger;
import er.extensions.ERXLongResponse.Task;

/**
 * Displays progress by using a ERXLongResponse.Task. Very useful for sending mail and the like.
 * You call up this page and give it a long running task. 
 * The task is currently responsible for returning the correct page for each stage.
 * Work in progress.
 * @created ak on Wed Feb 04 2004
 * @project ERDirectToWeb
 */

public class ERD2WProgressPage extends ERD2WPage {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERD2WProgressPage.class,"components");

    /** holds the task */
    protected Task _longResponseTask;
    
    /**
     * Public constructor
     * @param context the context
     */
    public ERD2WProgressPage(WOContext context) {
        super(context);
    }

    public Task longResponseTask() {
        return _longResponseTask;
    }
    
    public void setLongResponseTask(Task longResponseTask) {
        _longResponseTask = longResponseTask;
    }
}
