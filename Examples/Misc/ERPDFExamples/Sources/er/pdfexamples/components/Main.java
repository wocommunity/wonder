package er.pdfexamples.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.components.ERXComponent;

public class Main extends ERXComponent {
	private boolean usePrintCss = false;

	public Main(WOContext context) {
		super(context);
	}

@Override
public void appendToResponse(WOResponse response, WOContext context) {
	this.session().setObjectForKey(this, "mainpage");
	super.appendToResponse(response, context);
}

	/**
	 * @return the usePrintCss
	 */
	public boolean usePrintCss() {

		return usePrintCss;
	}

	/**
	 * @param usePrintCss
	 *            the usePrintCss to set
	 */
	public void setUsePrintCss(boolean usePrintCss) {
		this.usePrintCss = usePrintCss;
	}

	public WOActionResults loadPrintCSS() {
		this.setUsePrintCss(true);
		return null;
	}

	public WOActionResults unloadPrintCSS() {
		this.setUsePrintCss(false);
		return null;
	}
}
