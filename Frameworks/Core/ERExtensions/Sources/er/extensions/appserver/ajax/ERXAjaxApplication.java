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
 * <div class="en">
 * ERXAjaxApplication is the part of ERXApplication that handles Ajax requests.
 * If you want to use the Ajax framework without using other parts of Project
 * Wonder (i.e. ERXSession or ERXApplication), you should steal all of the code
 * in ERXAjaxSession, ERXAjaxApplication, and ERXAjaxContext.
 * </div>
 * 
 * <div class="ja">
 * ERXAjaxApplication は Ajax リクエストをサポートします。
 * </div>
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
	 * <div class="en">
	 * Sets the response delegate for this application.
	 * </div>
	 * 
	 * <div class="ja">
	 * このアプリケーションのレスポンス・デリゲートをセットします。
	 * </div>
	 * 
	 * @param responseDelegate <div class="en">the response delegate</div>
	 *                         <div class="ja">レスポンス・デリゲート</div>
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
	 * Ajax links have a ?_u=xxx&amp;2309482093 in the url which makes it look like a form submission to WebObjects.
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
	 * <div class="en">
	 * Overridden to allow for redirected responses.
	 * </div>
	 * 
	 * <div class="ja">
	 * リダイレクト・リスポンスを許可するためにオーバライドします。
	 * </div>
	 * 
	 * @param request <div class="en">WORequest</div>
	 *                <div class="ja">リクエスト</div>
	 * @param context <div class="en">WOContext</div>
	 *                <div class="ja">コンテクスト</div>
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
	 * <div class="en">Allow for context.page() as a result to an ajax call. Currently for debugging.</div>
	 * <div class="ja">デバッグ専用：ajax コールの context.page() 結果を許可する</div>
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
	 * <div class="ja">
	 * ページを強制的にキャシュに登録します。
	 * </div>
	 * 
	 * @param message WOMessage
	 */
	public static void setForceStorePage(WOMessage message) {
		ERXWOContext.contextDictionary().setObjectForKey(Boolean.TRUE, ERXAjaxSession.FORCE_STORE_PAGE);
	}
	
	/**
	 * <div class="en">
	 * Checks if the page should not be stored in the cache
	 * </div>
	 * 
	 * <div class="ja">
	 * ページを強制的にキャシュに登録する必要ああるかどうかをチェックします。
	 * </div>
	 * 
	 * @param message WOMessage
	 * @return <div class="en"></div>
	 *         <div class="ja">true の場合は登録する必要がある</div>
	 */
	public static boolean forceStorePage(WOMessage message) {
		NSDictionary userInfo = NSDictionary.EmptyDictionary;
		if (message != null) {
			userInfo = ERXWOContext.contextDictionary();
		}
		return (message != null && (message.headerForKey(ERXAjaxSession.FORCE_STORE_PAGE) != null || (userInfo.objectForKey(ERXAjaxSession.FORCE_STORE_PAGE) != null)));
	}

	/**
	 * <div class="en">
	 * Checks if the page should not be stored in the cache
	 * </div>
	 * 
	 * <div class="ja">
	 * キャッシュ内にページを登録しないかどうかをチェックします。
	 * </div>
	 * 
	 * @param message 
	 * @return <div class="en">boolean</div>
	 *         <div class="ja">true の場合は登録しないように</div>
	 */
	public static boolean shouldNotStorePage(WOMessage message) {
		NSDictionary userInfo = NSDictionary.EmptyDictionary;
		if (message != null) {
			userInfo = ERXWOContext.contextDictionary();
		}
		return (message != null && (message.headerForKey(ERXAjaxSession.DONT_STORE_PAGE) != null || (userInfo.objectForKey(ERXAjaxSession.DONT_STORE_PAGE) != null)));
	}

	/**
	 * <div class="en">
	 * Removes Ajax response headers that are no longer necessary.
	 * </div>
	 * <div class="ja">
	 * 必要ない Ajax リスポンス・ヘッダーを削除します。
	 * </div>
	 * 
	 * @param response <div class="en">the response to clean up</div>
	 *                 <div class="ja">クリーンアップするリスポンス</div>
	 */
	public static void cleanUpHeaders(WOResponse response) {
		if (response != null) {
			response.removeHeadersForKey(ERXAjaxSession.DONT_STORE_PAGE);
			response.removeHeadersForKey(ERXAjaxSession.PAGE_REPLACEMENT_CACHE_LOOKUP_KEY);
		}
	}

	/**
	 * <div class="en">
	 * Checks if the page should not be stored in the cache
	 * </div>
	 * 
	 * <div class="ja">
	 * ページがキャッシュに登録する必要がないことをチェックします。
	 * </div>
	 * 
	 * @param context 
	 * @return <div class="en"></div>
	 *         <div class="ja">true の場合は登録しないように</div>
	 */
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
	 * <div class="en">
	 * Return whether or not the given request is an Ajax request.
	 * </div>
	 * 
	 * <div class="en">
	 * 指定リクエストが Ajax リクエストかどうかを戻します。
	 * </div>
	 * 
	 * @param request <div class="en">the request to check</div>
	 *                <div class="ja">チェックするリクエスト</div>
	 * @return boolean
	 */
	public static boolean isAjaxRequest(WORequest request) {
		String requestedWith = request.headerForKey("x-requested-with");
		return "XMLHttpRequest".equals(requestedWith);
	}

	/**
	 * <div class="en">
	 * Returns the form name of the partial form submission.
	 * </div>
	 * 
	 * <div class="ja">
	 * 部分フォーム・サブミットのフォーム名を戻します。
	 * </div>
	 * 
	 * @param request <div class="en">the request</div>
	 *                <div class="ja">リクエスト</div>
	 * @return <div class="en">the form name of the partial form submission</div>
	 *         <div class="ja">部分フォーム・サブミットのフォーム名</div>
	 */
	public static String partialFormSenderID(WORequest request) {
		return request.stringFormValueForKey(ERXAjaxApplication.KEY_PARTIAL_FORM_SENDER_ID);
	}

	/**
	 * <div class="en">
	 * Returns the form name of the submitting Ajax button.
	 * </div>
	 * 
	 * <div class="ja">
	 * サブミット Ajax ボタンのフォーム名を戻します。
	 * </div>
	 * 
	 * @param request <div class="en">the request</div>
	 *                <div class="ja">リクエスト</div>
	 * @return <div class="en">the form name of the submitting Ajax button</div>
	 *         <div class="ja">サブミット Ajax ボタンのフォーム名</div>
	 */
	public static String ajaxSubmitButtonName(WORequest request) {
		return request.stringFormValueForKey(ERXAjaxApplication.KEY_AJAX_SUBMIT_BUTTON);
	}

	/**
	 * <div class="en">
	 * Returns true if this is an ajax submit.
	 * </div>
	 * 
	 * <div class="ja">
	 * Ajax サブミットの場合には true が戻ります。
	 * </div>
	 * 
	 * @param request 
	 * @return boolean
	 */
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
	 * <div class="en">
	 * ERXAjaxResponseDelegate receives callbacks from within the R-R loop when
	 * certain situations occur.
	 * </div>
	 * 
	 * <div class="ja">
	 * ERXAjaxResponseDelegate はリクエスト・リスポンス・ループよりコールバックされる。
	 * </div>
	 * 
	 * @author mschrag
	 */
	public static interface ERXAjaxResponseDelegate {
		/**
		 * <div class="en">
		 * When an Ajax request generates a null result, this method is called
		 * to provide an alternative response.
		 * </div>
		 * 
		 * <div class="en">
		 * Ajax リクエストが null 結果を生成した時に呼ばれ、換わりのリスポンスを提供します。
		 * </div>
		 * 
		 * @param request <div class="en">the request</div>
		 *                <div class="ja">リクエスト</div>
		 * @param response <div class="en">the response</div>
		 *                 <div class="ja">レスポンス</div>
		 * @param context <div class="en">the context</div>
		 *                <div class="ja">コンテクスト</div>
		 * @return <div class="en">the replacement results to use</div>
		 *         <div class="ja">換わりに使用するアクション</div>
		 */
		public WOActionResults handleNullActionResults(WORequest request, WOResponse response, WOContext context);
	}
}
