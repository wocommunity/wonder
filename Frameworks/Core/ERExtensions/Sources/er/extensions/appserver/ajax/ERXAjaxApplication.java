package er.extensions.appserver.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXWOContext;
import er.extensions.foundation.ERXProperties;

/**
 * <span class="en">
 * ERXAjaxApplication is the part of ERXApplication that handles Ajax requests.
 * If you want to use the Ajax framework without using other parts of Project
 * Wonder (i.e. ERXSession or ERXApplication), you should steal all of the code
 * in ERXAjaxSession, ERXAjaxApplication, and ERXAjaxContext.
 * </span>
 * 
 * <span class="ja">
 * ERXAjaxApplication は Ajax リクエストをサポートします。
 * </span>
 * 
 * @property er.extensions.ERXAjaxApplication.allowContextPageResponse
 *
 * @author mschrag
 */
public abstract class ERXAjaxApplication extends WOApplication {
	
	public static final String KEY_AJAX_SUBMIT_BUTTON = "AJAX_SUBMIT_BUTTON_NAME";
	public static final String KEY_PARTIAL_FORM_SENDER_ID = "_partialSenderID";
	public static final String KEY_UPDATE_CONTAINER_ID = "_u";
	public static final String KEY_REPLACED = "_r";

	private ERXAjaxResponseDelegate _responseDelegate;

	/**
	 * <span class="en">
	 * Sets the response delegate for this application.
	 * 
	 * @param responseDelegate
	 *            the response delegate
	 * </span>
	 * 
	 * <span class="ja">
	 * このアプリケーションのレスポンス・デリゲートをセットします。
	 * 
	 * @param responseDelegate - レスポンス・デリゲート
	 * </span>
	 */
	public void setResponseDelegate(ERXAjaxResponseDelegate responseDelegate) {
		_responseDelegate = responseDelegate;
	}

	public static boolean shouldIgnoreResults(WORequest request, WOContext context, WOActionResults results) {
		boolean shouldIgnoreResults = false;
		if (results == context.page() && !ERXAjaxApplication.isAjaxReplacement(request)) {
			WOApplication application = WOApplication.application();
			if (application instanceof ERXAjaxApplication) {
				shouldIgnoreResults = !((ERXAjaxApplication)application).allowContextPageResponse(); 
			}
			else {
				shouldIgnoreResults = true;
			}
		}
		return shouldIgnoreResults;
	}
	
	/**
	 * Ajax links have a ?_u=xxx&2309482093 in the url which makes it look like a form submission to WebObjects.
	 * Therefore takeValues is called on every update even though many many updates aren't submits.  This method
	 * checks to see if all you have is a _u or _r and an ismap (the #) param for form values.  If so, it's not 
	 * a form submission and takeValues can be skipped.
	 *
	 * @see com.webobjects.appserver.WOApplication#takeValuesFromRequest(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)
	 *
	 * @param request
	 *            the current request
	 * @param context
	 *            the context
	 */
	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		boolean shouldTakeValuesFromRequest = true;
		if (!request.isMultipartFormData() && ERXAjaxApplication.isAjaxRequest(request)) {
			NSDictionary formValues = request.formValues();
			int formValuesCount = formValues.count();
			if (formValuesCount == 2 && (formValues.containsKey(ERXAjaxApplication.KEY_UPDATE_CONTAINER_ID) || 
					                     formValues.containsKey(ERXAjaxApplication.KEY_REPLACED)) && 
					                     formValues.containsKey(WORequest._IsmapCoords)) {
				shouldTakeValuesFromRequest = false;
			}
		}
		if (shouldTakeValuesFromRequest) {
			super.takeValuesFromRequest(request, context);
		}
	}
	
	
	/**
	 * <span class="en">
	 * Overridden to allow for redirected responses.
	 * 
	 * @param request - WORequest
	 * @param context - WOContext
	 * </span>
	 * 
	 * <span class="ja">
	 * リダイレクト・リスポンスを許可するためにオーバライドします。
	 * 
	 * @param request - リクエスト
	 * @param context - コンテクスト
	 * </span>
	 */
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults results = super.invokeAction(request, context);
		// MS: This is to support AjaxUpdateContainer.
		// MS: Note that if results == context.page() something probably went
		// wrong
		if (ERXAjaxApplication.shouldNotStorePage(context)) {
			if (shouldIgnoreResults(request, context, results)) {
				results = null;
			}
			if (results == null && !ERXAjaxApplication.isAjaxReplacement(request)) {
				WOResponse response = context.response();

				if (_responseDelegate != null) {
					results = _responseDelegate.handleNullActionResults(request, response, context);
					response = context.response();
				}

				// MS: We were removing headers, and I really don't know WHY.  It was causing AUC's
				// inside of AjaxModalDialogs to break ... ERXAjaxSession is already cleaning up 
				// headers at the end, so this seems very odd to me.  If you remove headers here,
				// it causes ERXAjaxSession to not treat the response like an Ajax response.
				//ERXAjaxApplication.cleanUpHeaders(response);
				results = response;
			}
		}
		return results;
	}
	
	/**
	 * <span class="en">Allow for context.page() as a result to an ajax call. Currently for debugging.</span>
	 * <span class="ja">デバッグ専用：ajax コールの context.page() 結果を許可する</span>
	 */
	// AK: REMOVEME if WOGWT doesn't work out...
	private Boolean _allowContextPageResponse;
	private boolean allowContextPageResponse() {
		if(_allowContextPageResponse == null) {
			_allowContextPageResponse = ERXProperties.booleanForKey("er.extensions.ERXAjaxApplication.allowContextPageResponse");
		}
		return _allowContextPageResponse;
	}

	/**
	 * <span class="ja">
	 * ページを強制的にキャシュに登録します。
	 * 
	 * @param message - WOMessage
	 * </span>
	 */
	public static void setForceStorePage(WOMessage message) {
		ERXWOContext.contextDictionary().setObjectForKey(Boolean.TRUE, ERXAjaxSession.FORCE_STORE_PAGE);
	}
	
	/**
	 * <span class="en">
	 * Checks if the page should not be stored in the cache
	 * 
	 * @param message 
	 * 
	 * @return 
	 * </span>
	 * 
	 * <span class="ja">
	 * ページを強制的にキャシュに登録する必要ああるかどうかをチェックします。
	 * 
	 * @param message - WOMessage
	 * 
	 * @return true の場合は登録する必要がある
	 * </span>
	 */
	@SuppressWarnings("javadoc")
	public static boolean forceStorePage(WOMessage message) {
		NSDictionary userInfo = NSDictionary.EmptyDictionary;
		if (message != null) {
			userInfo = ERXWOContext.contextDictionary();
		}
		return (message != null && (message.headerForKey(ERXAjaxSession.FORCE_STORE_PAGE) != null || (userInfo.objectForKey(ERXAjaxSession.FORCE_STORE_PAGE) != null)));
	}

	/**
	 * <span class="en">
	 * Checks if the page should not be stored in the cache
	 * 
	 * @param message 
	 * 
	 * @return boolean
	 * </span>
	 * 
	 * <span class="ja">
	 * キャッシュ内にページを登録しないかどうかをチェックします。
	 * 
	 * @param message 
	 * 
	 * @return true の場合は登録しないように
	 * </span>
	 */
	@SuppressWarnings("javadoc")
	public static boolean shouldNotStorePage(WOMessage message) {
		NSDictionary userInfo = NSDictionary.EmptyDictionary;
		if (message != null) {
			userInfo = ERXWOContext.contextDictionary();
		}
		return (message != null && (message.headerForKey(ERXAjaxSession.DONT_STORE_PAGE) != null || (userInfo.objectForKey(ERXAjaxSession.DONT_STORE_PAGE) != null)));
	}

	/**
	 * <span class="en">
	 * Removes Ajax response headers that are no longer necessary.
	 * 
	 * @param response
	 *            the response to clean up
	 * </span>
	 * <span class="ja">
	 * 必要ない Ajax リスポンス・ヘッダーを削除します。
	 * 
	 * @param response - クリーンアップするリスポンス
	 * </span>
	 */
	public static void cleanUpHeaders(WOResponse response) {
		if (response != null) {
			response.removeHeadersForKey(ERXAjaxSession.DONT_STORE_PAGE);
			response.removeHeadersForKey(ERXAjaxSession.PAGE_REPLACEMENT_CACHE_LOOKUP_KEY);
		}
	}

	/**
	 * <span class="en">
	 * Checks if the page should not be stored in the cache
	 * 
	 * @param context 
	 * 
	 * @return 
	 * </span>
	 * 
	 * <span class="ja">
	 * ページがキャッシュに登録する必要がないことをチェックします。
	 * 
	 * @param context 
	 * 
	 * @return true の場合は登録しないように
	 * </span>
	 */
	@SuppressWarnings("javadoc")
	public static boolean shouldNotStorePage(WOContext context) {
		WORequest request = context.request();
		WOResponse response = context.response();
		// MS: The "AJAX_SUBMIT_BUTTON_NAME" check is a total hack, but if your
		// page structure changes such that the form that
		// is being submitted to is hidden, it ends up not notifying the system
		// not to cache the page.
		boolean shouldNotStorePage = (shouldNotStorePage(response) || shouldNotStorePage(request) || isAjaxSubmit(request)) && !forceStorePage(response);
		return shouldNotStorePage;
	}

	/**
	 * Set flag on current context to not store the current page.
	 */
	public static void enableShouldNotStorePage() {
		ERXWOContext.contextDictionary().takeValueForKey(ERXAjaxSession.DONT_STORE_PAGE, ERXAjaxSession.DONT_STORE_PAGE);
	}

	/**
	 * <span class="en">
	 * Return whether or not the given request is an Ajax request.
	 * 
	 * @param request
	 *            the request the check
	 *            
	 * @return boolean
	 * </span>
	 * 
	 * <span class="en">
	 * 指定リクエストが Ajax リクエストかどうかを戻します。
	 * 
	 * @param request - チェックするリクエスト
	 * 
	 * @return boolean
	 * </span>
	 */
	@SuppressWarnings("javadoc")
	public static boolean isAjaxRequest(WORequest request) {
		String requestedWith = request.headerForKey("x-requested-with");
		return "XMLHttpRequest".equals(requestedWith);
	}

	/**
	 * <span class="en">
	 * Returns the form name of the partial form submission.
	 * 
	 * @param request - the request
	 * 
	 * @return the form name of the partial form submission
	 * </span>
	 * 
	 * <span class="ja">
	 * 部分フォーム・サブミットのフォーム名を戻します。
	 * 
	 * @param request - リクエスト
	 * 
	 * @return 部分フォーム・サブミットのフォーム名
	 * </span>
	 */
	@SuppressWarnings("javadoc")
	public static String partialFormSenderID(WORequest request) {
		return request.stringFormValueForKey(ERXAjaxApplication.KEY_PARTIAL_FORM_SENDER_ID);
	}

	/**
	 * <span class="en">
	 * Returns the form name of the submitting Ajax button.
	 * 
	 * @param request - the request
	 * 
	 * @return the form name of the submitting Ajax button
	 * </span>
	 * 
	 * <span class="ja">
	 * サブミット Ajax ボタンのフォーム名を戻します。
	 * 
	 * @param request - リクエスト
	 * 
	 * @return サブミット Ajax ボタンのフォーム名
	 * </span>
	 */
	@SuppressWarnings("javadoc")
	public static String ajaxSubmitButtonName(WORequest request) {
		return request.stringFormValueForKey(ERXAjaxApplication.KEY_AJAX_SUBMIT_BUTTON);
	}

	/**
	 * <span class="en">
	 * Returns true if this is an ajax submit.
	 * 
	 * @param request 
	 * 
	 * @return boolean
	 * </span>
	 * 
	 * <span class="ja">
	 * Ajax サブミットの場合には true が戻ります。
	 * 
	 * @param request 
	 * 
	 * @return boolean
	 * </span>
	 */
	@SuppressWarnings("javadoc")
	public static boolean isAjaxSubmit(WORequest request) {
		return (ERXAjaxApplication.ajaxSubmitButtonName(request) != null);
	}

	/**
	 * Returns true if this is an Ajax replacement (_r key is set).
	 * 
	 * @param request - WORequest
	 * 
	 * @return boolean
	 */
	public static boolean isAjaxReplacement(WORequest request) {
		return request.formValueForKey(ERXAjaxApplication.KEY_REPLACED) != null;
	}
	
	/**
	 * Returns true if this request will update an AjaxUpdateContainer.
	 * 
	 * @param request - WORequest
	 * 
	 * @return boolean
	 */
	public static boolean isAjaxUpdate(WORequest request) {
		return request.formValueForKey(KEY_UPDATE_CONTAINER_ID) != null;
	}

	/**
	 * <span class="en">
	 * ERXAjaxResponseDelegate receives callbacks from within the R-R loop when
	 * certain situations occur.
	 * </span>
	 * 
	 * <span class="ja">
	 * ERXAjaxResponseDelegate はリクエスト・リスポンス・ループよりコールバックされる。
	 * </span>
	 * 
	 * @author mschrag
	 */
	public static interface ERXAjaxResponseDelegate {
		/**
		 * <span class="en">
		 * When an Ajax request generates a null result, this method is called
		 * to provide an alternative response.
		 * 
		 * @param request - the request
		 * @param response - the response
		 * @param context - the context
		 * 
		 * @return the replacement results to use
		 * </span>
		 * 
		 * <span class="en">
		 * Ajax リクエストが null 結果を生成した時に呼ばれ、換わりのリスポンスを提供します。
		 * 
		 * @param request - リクエスト
		 * @param response - レスポンス
		 * @param context - コンテクスト
		 * 
		 * @return - 換わりに使用するアクション
		 * </span>
		 */
		@SuppressWarnings("javadoc")
		public WOActionResults handleNullActionResults(WORequest request, WOResponse response, WOContext context);
	}
}
