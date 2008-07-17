package er.sproutcore.views.field;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class SCPasswordFieldView extends SCTextFieldView {
	public SCPasswordFieldView(String name, NSDictionary associations, WOElement element) {
		super(name, associations, element);
	}

	@Override
	protected void addProperties() {
		super.addProperties();
	}

	@Override
	public String type() {
		return "password";
	}

	@Override
	protected void doAppendToResponse(WOResponse response, WOContext context) {
		super.doAppendToResponse(response, context);
	}

}
