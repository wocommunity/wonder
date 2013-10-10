package er.ajax.mootools;

import java.text.NumberFormat;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.ajax.AjaxProgress;
import er.ajax.AjaxProgressBar;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXComponentUtilities;

/**
 * @binding id the id of the update container
 * @binding progressID the id of the AjaxProgress
 * @binding progress the progress object for this progress bar (can bind a new progress back out if one is in the
 *          registry)
 * @binding startedFunction the javascript function to execute when the progress is started
 * @binding canceledFunction the javascript function to execute when the progress is canceled
 * @binding succeededFunction the javascript function to execute when the progress succeeds
 * @binding failedFunction the javascript function to execute when the progress fails
 * @binding finishedFunction the javascript function to execute when the progress finishes (succeeded, failed, or
 *          canceled)
 * @binding cancelText the text to display for the cancel link
 * @binding cancelingText the text to display when the progress is being canceled
 * @binding startingText the text to display when the progress is starting
 * @binding progressOfText the text to display for the word "of" in the "[size] of [totalsize]" string during upload
 * @binding finishedAction the action to fire when the progress finishes (cancel, failed, or succeeded)
 * @binding canceledAction the action to fire when the progress is canceled
 * @binding succeededAction the action to fire when the progress succeeded
 * @binding failedAction the action to fire when the progress fails
 * @binding started boolean of whether or not the progress has started (i.e. begin polling)
 * @binding allowCancel if true, the cancel link is visible
 * @binding visibleBeforeStart if true, the progress bar is visible before the activity is started
 * @binding visibleAfterDone if true, the progress bar is visible after the activity is done
 * @binding refreshTime the number of milliseconds to wait between refreshes
 * 
 * @author mschrag
 */

public class MTAjaxProgressBar extends WOComponent {

	private static final long serialVersionUID = 1L;

	private String _id;
	private boolean _running;
	private AjaxProgress _progress;
	private boolean _completionEventsFired;
	private boolean _fireFinishedJavascriptEvents;
	private boolean _fireStartedJavascriptEvent;	
	
	public MTAjaxProgressBar(WOContext context) {
        super(context);
    }

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public boolean fireStartedJavascriptEvent() {
		boolean fireStartedJavascriptEvent = _fireStartedJavascriptEvent;
		if (fireStartedJavascriptEvent) {
			_fireStartedJavascriptEvent = false;
		}
		return fireStartedJavascriptEvent;
	}
	
	public boolean fireFinishedJavascriptEvents() {
		boolean fireFinishedJavascriptEvents = _fireFinishedJavascriptEvents;
		if (fireFinishedJavascriptEvents) {
			_fireFinishedJavascriptEvents = false;
		}
		return fireFinishedJavascriptEvents;
	}

	public boolean progressBarVisible() {
		boolean visible = true;
		AjaxProgress progress = progress();
		if (!isStarted()) {
			if (hasBinding("visibleBeforeStart")) {
				visible = ERXComponentUtilities.booleanValueForBinding(this, "visibleBeforeStart");
			}
		}
		else if (done() && !_fireFinishedJavascriptEvents) {
			if (hasBinding("visibleAfterDone")) {
				visible = ERXComponentUtilities.booleanValueForBinding(this, "visibleAfterDone");
			}
		}
		return visible;
	}

	public String startingText() {
		String startingText = (String) valueForBinding("startingText");
		if (startingText == null) {
			startingText = "Starting ...";
		}
		return startingText;
	}

	public String cancelingText() {
		String cancelingText = (String) valueForBinding("cancelingText");
		if (cancelingText == null) {
			cancelingText = "Canceling ...";
		}
		return cancelingText;
	}

	public AjaxProgress progress() {
		if (_progress != null && _progress.shouldReset()) {
			_progress = null;
			setValueForBinding(null, "progress");
		}
		if (_progress == null) {
			_progress = (AjaxProgress) valueForBinding("progress");
			if (_progress != null && _progress.shouldReset()) {
				_progress = null;
				setValueForBinding(null, "progress");
			}
			if (_progress == null) {
				_progress = AjaxProgressBar.progress(session(), progressID());
				if (_progress != null) {
					if (_progress.shouldReset()) {
						AjaxProgressBar.unregisterProgress(session(), _progress);
						_progress = null;
					}
					setValueForBinding(_progress, "progress");
				}
			}
		}
		return _progress;
	}

	public String progressID() {
		String progressID = (String) valueForBinding("progressID");
		if (progressID == null) {
			progressID = id();
		}
		return progressID;
	}

	public String id() {
		String id = _id;
		if (id == null) {
			id = (String) valueForBinding("id");
			if (id == null) {
				id = ERXWOContext.safeIdentifierName(context(), true);
			}
			_id = id;
		}
		return id;
	}

	public String finishedClass() {
		String finishedClass;
		String percentage = percentage();
		if ("0".equals(percentage)) {
			finishedClass = "percentageUnfinished";
		}
		else {
			AjaxProgress progress = progress();
			if (progress != null && progress.isDone()) {
				finishedClass = "percentageFinished done";
			}
			else {
				finishedClass = "percentageFinished";
			}
		}
		return finishedClass;
	}

	public Object displayValue() {
		Object displayValue = valueForBinding("displayValue");
		if (displayValue == null) {
			AjaxProgress progress = progress();
			if (progress != null) {
				displayValue = String.valueOf(progress.value());
			}
			else {
				displayValue = "";
			}
		}
		return displayValue;
	}

	public Object displayMaximum() {
		Object displayMaximum = valueForBinding("displayMaximum");
		if (displayMaximum == null) {
			AjaxProgress progress = progress();
			if (progress != null) {
				displayMaximum = String.valueOf(progress.maximum());
			}
			else {
				displayMaximum = "";
			}
		}
		return displayMaximum;
	}

	public String percentage() {
		AjaxProgress progress = progress();
		String percentageStr;
		if (progress == null || progress.isSucceeded()) {
			percentageStr = "0";
		}
		else {
			
			double percentage = progress.percentage() * 100.0;
			if (percentage < 5) {
				percentageStr = "0";
			}
			else {
				percentageStr = NumberFormat.getIntegerInstance().format(percentage) + "%";
			}
		}
		return "width: " + percentageStr;
	}

	public boolean isStarted() {
		boolean started;
		if (hasBinding("started")) {
			started = ERXComponentUtilities.booleanValueForBinding(this, "started");
		}
		else {
			started = (progress() != null || _running);
		}
		return started;
	}

	public String cancelText() {
		String cancelText = (String) valueForBinding("cancelText");
		if (cancelText == null) {
			cancelText = "cancel";
		}
		return cancelText;
	}

	public String progressOfText() {
		String progressOfText = (String) valueForBinding("progressOfText");
		if (progressOfText == null) {
			progressOfText = "of";
		}
		return progressOfText;
	}
	
	public String onChange() {
		return id() + "AjaxProgress.start()";
	}

	protected void _checkForCompletion() {
		AjaxProgress progress = progress();
		if (progress != null) {
			if (progress.isDone()) {
				if (!progress.completionEventsFired()) {
					if (progress.isCanceled()) {
						progressCanceled();
					}
					else if (progress.isFailed()) {
						progressFailed();
					}
					else if (progress.isSucceeded()) {
						progressSucceeded();
					}
					progress.setCompletionEventsFired(true);
					_fireFinishedJavascriptEvents = true;
				}
			}
		}
	}

	public boolean done() {
		boolean done = false;
		AjaxProgress progress = progress();
		if (progress != null) {
			done = progress.isDone() && progress.completionEventsFired();
		}
		return done;
	}

	public String refreshTime() {
		String refreshTimeStr;
		Object refreshTime = valueForBinding("refreshTime");
		if (refreshTime == null) {
			refreshTimeStr = "1000";
		}
		else {
			refreshTimeStr = refreshTime.toString();
		}
		return refreshTimeStr;
	}
	
	public WOActionResults refreshing() {
		if (!_running) {
			_fireStartedJavascriptEvent = true;
		}
		_running = true;
		_checkForCompletion();
		return null;
	}

	public WOActionResults cancel() {
		AjaxProgress progress = progress();
		if (progress != null) {
			progress.cancel();
		}
		return null;
	}

	protected void finished() {
		if (_progress != null) {
			AjaxProgressBar.unregisterProgress(session(), _progress);
		}
		_running = false;
		valueForBinding("finishedAction");
	}

	protected void progressCanceled() {
		finished();
		valueForBinding("canceledAction");
	}

	protected void progressSucceeded() {
		AjaxProgress progress = progress();
		finished();
		valueForBinding("succeededAction");
	}

	protected void progressFailed() {
		finished();
		valueForBinding("failedAction");
	}

	public String progressBarContainerClass() {
		return valueForStringBinding("progressBarContainerClass", "progress");
	}	

}