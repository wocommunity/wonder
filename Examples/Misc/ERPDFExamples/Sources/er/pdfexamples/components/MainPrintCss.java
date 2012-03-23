package er.pdfexamples.components;

import com.webobjects.appserver.WOContext;
import er.extensions.components.ERXComponent;

public class MainPrintCss extends Main {
	

    public MainPrintCss(WOContext context) {
        super(context);
        this.setUsePrintCss(true);
    }
}