package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

import er.jquerymobile.exampleapp.businessLogic.SampleComponentBase;

public class UpdateContainer extends SampleComponentBase
{
	private static final long serialVersionUID = 1L;

	public UpdateContainer(WOContext context)
	{
		super(context);
	}

	public String testString = null;
	public String submitText = "";

	public WOActionResults doSubmitAction()
	{
		submitText = "You use the submit button";
		return _doAction("doSubmitAction");
	}

	public WOActionResults doLinkAction()
	{
		submitText = "You use the link";
		return _doAction("**doLinkAction**");
	}

	private WOActionResults _doAction(String name)
	{
		System.err.println("**" + name + "**");
		System.err.println(" testString = " + testString);

		return null;
	}
}