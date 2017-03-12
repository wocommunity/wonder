package er.jrexample.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.components.ERXComponent;
import er.extensions.components.ERXDownloadResponse;

public class DownloadIsCompletePage extends ERXComponent {
	private static final Logger log = LoggerFactory.getLogger(DownloadIsCompletePage.class);

	private static String WO_META_REFRESH_SENDER_ID = "AutoDownload";
	private static String REFRESH_HEADER_KEY = "Refresh";

	private ERXDownloadResponse _downloadResponseComponent;
	private boolean _shouldDownloadOnRefresh = false;
	private String _returnLinkText;
	private WOComponent _referringPage;

	public DownloadIsCompletePage(WOContext context) {
		super(context);
	}

	/**
	 * We override so we can dynamically manage the META Refresh header in the
	 * response
	 */
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		if (_shouldDownloadOnRefresh) {
			response.setHeader(metaReloadResponseHeader(context), REFRESH_HEADER_KEY);

			// Toggle off so we don't download again
			_shouldDownloadOnRefresh = false;
		} // ~ if (shouldDownloadOnRefresh())
		super.appendToResponse(response, context);
		log.debug("response.headers() = {}", response.headers());
	}

	/**
	 * @return the preconfigured download component that is to be returned by
	 *         this page to make the download event occur
	 */
	public ERXDownloadResponse downloadResponseComponent() {
		return _downloadResponseComponent;
	}

	/**
	 * User action in case the meta refresh failed to download due to security block.
	 * 
	 * @return the download response
	 */
	public WOActionResults downloadTheFile() {
		return downloadResponseComponent();
	}

	/**
	 * We use our custom senderID key for the refresh to detect the refresh and
	 * we call the download action that returns the download file and we remove
	 * the refresh header
	 * 
	 */
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		if (context.senderID().equals(WO_META_REFRESH_SENDER_ID)) {
			return downloadTheFile();
		}
		return super.invokeAction(request, context);
	}

	private String metaReloadResponseHeader(WOContext aContext) {
		StringBuilder b = new StringBuilder();
		b.append("0;url=");
		b.append(aContext
				.urlWithRequestHandlerKey(WOApplication.application().componentRequestHandlerKey(), null, null));
		b.append("/");
		b.append(aContext.session().sessionID());
		b.append("/");
		b.append(aContext.contextID());
		b.append(".");
		b.append(WO_META_REFRESH_SENDER_ID);
		return b.toString();
	}

	/** @return the original referring page */
	public WOComponent referringPage() {
		return _referringPage;
	}

	/** @return what */
	public String returnLinkText() {
		if (_returnLinkText == null) {
			if (referringPage() == null) {
				_returnLinkText = "Last Page";
			}

		} // ~ if (_returnLinkText == null)
		return _returnLinkText;
	}

	public WOActionResults returnToReferringPage() {
		return referringPage();
	}

	/**
	 * @param downloadResponseComponent
	 *            the preconfigured download component that is to be returned by
	 *            this page to make the download event occur
	 */
	public void setDownloadResponseComponent(ERXDownloadResponse downloadResponseComponent) {
		_downloadResponseComponent = downloadResponseComponent;
		_shouldDownloadOnRefresh = true;
	}

	/**
	 * @param referringPage
	 *            the original referring page
	 */
	public void setReferringPage(WOComponent referringPage) {
		_referringPage = referringPage;
	}

	/**
	 * @param returnLinkText
	 *            what
	 */
	public void setReturnLinkText(String returnLinkText) {
		_returnLinkText = returnLinkText;
	}
}