package er.examples.ui;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

public class ExcelGenerationPage extends ERXComponent {
    public ExcelGenerationPage(WOContext context) {
        super(context);
    }

    public WOComponent showSampleAction() {
    	ExcelGeneratedPage nextPage = pageWithName(ExcelGeneratedPage.class);
    	return nextPage;
    }

    public WOComponent showExcelAction() {
    	ExcelGeneratedPage nextPage = pageWithName(ExcelGeneratedPage.class);
    	nextPage.enabled = true;
    	return nextPage;
    }
}