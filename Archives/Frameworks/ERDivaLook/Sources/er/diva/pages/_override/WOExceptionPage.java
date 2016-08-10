package er.diva.pages._override;

import com.webobjects.appserver.WOContext;

/**
 * Ajax replacement for standard WO exception page.
 * To use: in your app order ERDivaLook framework before JavaWOExtensions
 * 
 * @author mendis
 *
 */
public class WOExceptionPage extends com.webobjects.woextensions.WOExceptionPage {

	public WOExceptionPage(WOContext aContext) {
		super(aContext);
	}
}
