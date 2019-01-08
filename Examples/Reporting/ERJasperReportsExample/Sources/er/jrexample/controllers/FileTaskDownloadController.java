package er.jrexample.controllers;

import java.io.File;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;

import er.extensions.appserver.ERXNextPageForResultWOAction;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXDownloadResponse;
import er.jrexample.components.DownloadIsCompletePage;

/**
 * A generic controller for handling a long response that generates a file to download.
 * 
 * The responsibility of this class is to pass control from the long response page at the end of
 * the execution of the long response task to the next page, which in this case is the "Your File is being downloaded" page.
 * 	
 * @author kieran
 *
 */
public class FileTaskDownloadController extends ERXNextPageForResultWOAction {
	private String _returnLinkText;
	private final WOComponent _senderPage;
	
	
	public FileTaskDownloadController() {
		super();
		_senderPage = ERXWOContext.currentContext().page();
	}
	
	/** @return the link text for the link/button that brings the user back to the original page that initiated the long response file download */
	public String returnLinkText() {
		return _returnLinkText;
	}
	
	/** @param returnLinkText the link text for the link/button that brings the user back to the original page that initiated the long response file download */
	public void setReturnLinkText(String returnLinkText){
		_returnLinkText = returnLinkText;
	}
	
	@Override
	public WOActionResults performAction() {
		if (_result instanceof File) {
			File file = (File) _result;
			
			ERXDownloadResponse dl = pageWithName(ERXDownloadResponse.class);
			dl.setFileToDownload(file);
			dl.setDownloadFilename(downloadFileNameForClient());
			
			DownloadIsCompletePage nextPage = pageWithName(DownloadIsCompletePage.class);
			nextPage.setDownloadResponseComponent(dl);
			nextPage.setReturnLinkText(returnLinkText());
			nextPage.setReferringPage(_senderPage);
			
			return nextPage;

		} else {
			throw new RuntimeException("Unknown result type: " + (_result == null ? "null" : _result.toString()));
		}
	}
	
	private String _downloadFileNameForClient;
	
	/** @return what */
	public String downloadFileNameForClient() {
		return _downloadFileNameForClient;
	}
	
	/** @param downloadFileNameForClient what */
	public void setDownloadFileNameForClient(String downloadFileNameForClient){
		_downloadFileNameForClient = downloadFileNameForClient;
	}
	
}
