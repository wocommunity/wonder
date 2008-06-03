package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;

import er.extensions.concurrency.ERXLongResponseTask;

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
    private static final Logger log = Logger.getLogger(ERD2WProgressPage.class);

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
