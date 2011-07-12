package er.jrexample.components;


import java.io.File;
import java.util.concurrent.Callable;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

import er.coolcomponents.CCAjaxLongResponsePage;
import er.extensions.components.ERXComponent;
import er.jrexample.businesslogic.Reports;
import er.jrexample.controllers.FileTaskDownloadController;

public class Main extends ERXComponent {
	public Main(WOContext context) {
		super(context);
	}
	
	public WOActionResults studioRevenueReportAction() {
		
		// Create the task
		Callable<File> reportTask = Reports.createStudioRevenueReportTask();
		
		// Create the long response page
		CCAjaxLongResponsePage nextPage = pageWithName(CCAjaxLongResponsePage.class);
		
		// Push the task into the long response page
		nextPage.setTask(reportTask);
		
		// Controller for handling the Callable result in the long response page
		FileTaskDownloadController nextPageController = new FileTaskDownloadController();
		
		// Hyperlink text on the "Your file is downloaded page" to get back here
		nextPageController.setReturnLinkText("Reports Menu");
		
		// The filename for the download
		nextPageController.setDownloadFileNameForClient("StudioRevenueReport.pdf");
		
		nextPage.setNextPageForResultController(nextPageController);
		
		return nextPage;
		
	}
}
