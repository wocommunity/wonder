package er.directtoweb;

import com.webobjects.appserver.*;

import er.extensions.*;

/**
 * Displays progress by using a ERXLongResponse.Task. Very useful for sending mail and the like.
 * You call up this page and give it a long running task. 
 * The task is currently responsible for returning the correct page for each stage.
 * Work in progress.
 * @created ak on Wed Feb 04 2004
 * @project ERDirectToWeb
 */

public class ERD2WProgressPage extends ERD2WMessagePage {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERD2WProgressPage.class,"components");

    /** holds the task */
    protected ERXLongResponseTask _longResponseTask;
    
    /** holds the completed percentage */
    protected int _percentCompleted = -1;
    
    /**
     * Public constructor
     * @param context the context
     */
    public ERD2WProgressPage(WOContext context) {
        super(context);
    }

    public ERXLongResponseTask longResponseTask() {
        return _longResponseTask;
    }
    public void setLongResponseTask(ERXLongResponseTask longResponseTask) {
        _longResponseTask = longResponseTask;
    }
    
    public boolean shouldShowProgressBar() {
    	return _percentCompleted != -1;
    }
    
    public int percentCompleted() {
    	return _percentCompleted;
    }
    public void setPercentCompleted(int value) {
    	_percentCompleted = value;
    }
}
