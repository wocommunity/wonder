package wowodc.eof;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSValidation;

import er.extensions.validation.ERXValidationException;
import er.extensions.validation.ERXValidationFactory;

public class ResultItem extends _ResultItem {
	// The first saved workflow state is after initial creation and prime check has been performed
	public static String WORKFLOW_PRIME_CHECKED = "Prime Checked";
	
	// The next saved state is the transient state while we are performing the factorial check
	public static String WORKFLOW_CHECKING_FACTORIAL = "Checking Factorial";
	
	// The next saved state is the completed all processing state while we are performing the factorial check
	public static String WORKFLOW_PROCESSING_COMPLETE = "Factorial Checked";
	
	private static NSArray<String> WORKFLOW_STATES = new NSArray( new String[] {
			WORKFLOW_PRIME_CHECKED,
			WORKFLOW_CHECKING_FACTORIAL,
			WORKFLOW_PROCESSING_COMPLETE
	});
	
	@Override
	public void awakeFromInsertion(EOEditingContext editingContext) {
		super.awakeFromInsertion(editingContext);
		
		setIsFactorialPrime(Boolean.FALSE);
		setWorkflowState(WORKFLOW_PRIME_CHECKED);
	}
	
	@Override
	public void willInsert() {
		super.willInsert();
		setModificationTime(new NSTimestamp());
	}
	
	@Override
	public void willUpdate() {
		super.willUpdate();
		setModificationTime(new NSTimestamp());
	}
	
	
	public String validateWorkflowState(Object aValue) throws NSValidation.ValidationException {
		String strValue = ObjectUtils.toString(aValue, null);
		
		if(StringUtils.isEmpty(strValue)) {
			throw new ERXValidationFactory().createCustomException(this, KEY_WORKFLOW_STATE, strValue, ERXValidationException.NullPropertyException);
		}
		
		// Must be one of acceptable values
		if (!WORKFLOW_STATES.contains(strValue)) {
			throw new ERXValidationFactory().createCustomException(this, KEY_WORKFLOW_STATE, strValue, ERXValidationException.InvalidValueException);
		}
		
		return strValue;
	}
}
