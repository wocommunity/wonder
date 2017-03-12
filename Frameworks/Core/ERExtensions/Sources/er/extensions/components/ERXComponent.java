package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXBrowser;
import er.extensions.appserver.ERXRequest;
import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.foundation.ERXUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * <div class="en">
 * ERXComponent provides a common base class for WOComponents along with a bunch
 * of miscellaneous handy features.
 * </div>
 * 
 * <div class="ja">
 * ERXComponent は WOComponents のサブクラスで、多数の便利なコマンドを用意しています
 * </div>
 * 
 * @author mschrag
 */
public abstract class ERXComponent extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	protected NSMutableDictionary _dynamicBindings = null;
	
	/**
	 * Constructs a new ERXComponent.
	 * 
	 * @param context
	 *            the WOContext
	 */
	public ERXComponent(WOContext context) {
		super(context);
	}

	/**
	 * <div class="en">
	 * This variant of pageWithName provides a Java5 genericized version of the
	 * original pageWithName. You would call it with:
	 * 
	 * MyNextPage nextPage = pageWithName(MyNextPage.class);
	 * </div>
	 * 
	 * <div class="ja">
   * Java5 の新しい pageWithName コマンド。
   * 次の様に呼ばれます：
   * 
   * MyNextPage nextPage = pageWithName(MyNextPage.class);
	 * </div>
   * 
	 * @param <T> <div class="en">the type of component to create</div>
	 *            <div class="ja">作成するコンポーネント・タイプ</div>
	 * @param componentClass <div class="en">the Class of the component to load</div>
	 *                       <div class="ja">ロードするコンポーネントのクラス</div>
	 * 
	 * @return <div class="en">an instance of the requested component class</div>
	 *         <div class="ja">コンポーネント・クラスのインスタンス</div>
	 */
	@SuppressWarnings("unchecked")
	public <T extends WOComponent> T pageWithName(Class<T> componentClass) {
		return (T) super.pageWithName(componentClass.getName());
	}

	@Override
	public void _awakeInContext(WOContext aArg0) {
		super._awakeInContext(aArg0);
		if (isStateless()) {
			_dynamicBindings = null;
		}
	}

	/**
	 * <div class="en">
	 * _checkAccess is called prior to all three phases of the R-R loop to
	 * ensure that the user has permission to access this component. You should
	 * override checkAccess to implement addition security checks.
	 * </div>
	 * 
	 * <div class="ja">
   * 各リスポンス・リクエスト・ループの最初に _checkAccess 実行され、ユーザがこのコンポーネントへのアクセスが可能かどうかをチェックします。
   * 
   * 他のセキュリティ・チェックを行う時には checkAccess をオーバライドすると良い
	 * </div>
	 * 
	 * @throws SecurityException <div class="en">if the user does not have permission</div>
	 *                           <div class="ja">ユーザがアクセス権を持っていない場合</div>
	 */
	protected void _checkAccess() throws SecurityException {
		if (!isPageAccessAllowed() && _isPage()) {
			throw new SecurityException("You are not allowed to directly access the component '" + name() + "'.");
		}
		if (shouldCheckAccess()) {
			checkAccess();
		}
	}
	
	/**
	 * Returns whether or not this component should check access before processing any of the request-response loop.
	 * The default implementation just returns _isPage().
	 * 
	 * @return whether or not this component should check access
	 */
	protected boolean shouldCheckAccess() {
		return _isPage();
	}

	/**
	 * <div class="en">
	 * Calls _checkAccess prior to super.takeValuesFromRequest.
	 * </div>
	 * 
	 * <div class="ja">
	 * スーパーの前に _checkAccess のセキュリティ・チェックを行います
	 * </div>
	 * 
	 * @param request <div class="en">the current request with the WOComponent object</div>
	 *                <div class="ja">リクエスト</div>
	 * @param context <div class="en">context of a transaction</div>
	 *                <div class="ja">トランスアクションのコンテキスト</div>
	 */
	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		_checkAccess();
		super.takeValuesFromRequest(request, context);
	}

	/**
	 * <div class="en">
	 * Calls _checkAccess prior to super.invokeAction.
	 * </div>
	 * 
	 * <div class="ja">
	 * スーパーの前に _checkAccess のセキュリティ・チェックを行います
	 * </div>
	 * 
	 * @param request <div class="en">the current request with the WOComponent object</div>
	 *                <div class="ja">リクエスト</div>
	 * @param context <div class="en">context of a transaction</div>
	 *                <div class="ja">トランスアクションのコンテキスト</div>
	 * 
	 * @return <div class="en">a WOActionResults containing the result of the request</div>
	 *         <div class="ja">リクエスト結果を含む WOActionResults</div>
	 */
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		_checkAccess();
		return super.invokeAction(request, context);
	}

	/**
	 * <div class="en">
	 * Calls _checkAccess prior to super.appendToResponse and adds support for
	 * ClickToOpen (TM).
	 * </div>
	 * 
	 * <div class="ja">
	 * スーパーの前にセキュリティ・チェックし、ClickToOpen (TM) サポート
	 * </div>
	 * 
	 * @param response <div class="en">the HTTP response that an application returns to a Web server to complete a cycle of the request-response loop</div>
	 *                 <div class="ja">RR ループの最後のリスポンス結果を戻す</div>
	 * @param context <div class="en">context of a transaction</div>
	 *                <div class="ja">トランスアクションのコンテキスト</div>
	 */
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		_checkAccess();
		preAppendToResponse(response, context);

		boolean clickToOpenEnabled = clickToOpenEnabled(response, context);
		ERXClickToOpenSupport.preProcessResponse(response, context, clickToOpenEnabled);
		try {
			super.appendToResponse(response, context);
		}
		finally {
			ERXClickToOpenSupport.postProcessResponse(getClass(), response, context, clickToOpenEnabled);
		}

		postAppendToResponse(response, context);

		_includeCSSResources(response, context);
		_includeJavascriptResources(response, context);
	}

	/**
	 * <div class="en">
	 * Returns whether or not click-to-open should be enabled for this
	 * component. By default this returns ERXClickToOpenSupport.isEnabled().
	 * </div>
	 * 
	 * <div class="ja">
	 * このコンポーネントで click-to-open をサポートしているかどうかを戻します。
   * デフォルトでは ERXClickToOpenSupport.isEnabled() を戻します。
	 * </div>
	 * 
	 * @param response <div class="en">the response</div>
	 *                 <div class="ja">リスポンス</div>
	 * @param context <div class="en">the context</div>
	 *                <div class="ja">コンテクスト</div>
	 * 
	 * @return <div class="en">whether or not click-to-open is enabled for this component</div>
	 *         <div class="ja">このコンポーネントで click-to-open をサポートしているかどうか</div>
	 */
	public boolean clickToOpenEnabled(WOResponse response, WOContext context) {
		return ERXClickToOpenSupport.isEnabled();
	}

	/**
	 * <div class="en">
	 * Resolves a given binding as a int value. Useful for image sizes and the
	 * like.
	 * </div>
	 * 
	 * <div class="ja">
   * 指定されているバインディングを int 値に変換して戻します。<br>
   * イメージ・サイズなどに有効です
   * </div>
	 * 
	 * @param binding <div class="en">binding to be resolved as a int value.</div>
	 *                <div class="ja">int 値として戻すバインディング</div>
	 * @param defaultValue <div class="en">default int value to be used if the binding is not bound.</div>
	 *                     <div class="ja">バインディングが見つからない場合のデフォルト int 値</div>
	 * 
	 * @return <div class="en">result of evaluating binding as a int.</div>
	 *         <div class="ja">バインディング結果の int 値</div>
	 */
	protected int intValueForBinding(String binding, int defaultValue) {
		return ERXValueUtilities.intValueWithDefault(valueForBinding(binding), defaultValue);
	}

	/**
	 * <div class="en">
	 * Resolves a given binding as a float value.
	 * </div>
	 * 
	 * <div class="ja">
   * 指定されているバインディングを float 値に変換して戻します。<br>
   * </div>
	 * 
	 * @param binding <div class="en">binding to be resolved as a float value.</div>
	 *                <div class="ja">float 値として戻すバインディング</div>
	 * @param defaultValue <div class="en">default float value to be used if the binding is not bound.</div>
	 *                     <div class="ja">バインディングが見つからない場合のデフォルト float 値</div>
	 * 
	 * @return <div class="en">result of evaluating binding as a float.</div>
	 *         <div class="ja">バインディング結果の float 値</div>
	 */
	protected float floatValueForBinding(String binding, float defaultValue) {
		return ERXValueUtilities.floatValueWithDefault(valueForBinding(binding), defaultValue);
	}

	/**
	 * <div class="en">
	 * Resolves a given binding as a boolean value. Defaults to false.
	 * </div>
	 * 
	 * <div class="ja">
   * 指定されているバインディングを boolean 値に変換して戻します。<br>
   * デフォルトは false です
   * </div>
	 * 
	 * @param binding <div class="en">binding to be resolved as a boolean value.</div>
	 *                <div class="ja">boolean 値として戻すバインディング</div>
	 * 
	 * @return <div class="en">result of evaluating binding as a boolean.</div>
	 *         <div class="ja">バインディング結果の boolean 値</div>
	 */
	protected boolean booleanValueForBinding(String binding) {
		return booleanValueForBinding(binding, false);
	}

	/**
	 * <div class="en">
	 * Resolves a given binding as a boolean value.
	 * </div>
	 * 
	 * <div class="ja">
   * 指定されているバインディングを boolean 値に変換して戻します。
   * </div>
	 * 
	 * @param binding <div class="en">binding to be resolved as a boolean value.</div>
	 *                <div class="ja">boolean 値として戻すバインディング</div>
	 * @param defaultValue <div class="en">default boolean value to be used if the binding is not bound.</div>
	 *                     <div class="ja">バインディングが見つからない場合のデフォルト boolean 値</div>
	 * 
	 * @return <div class="en">result of evaluating binding as a boolean.</div>
	 *         <div class="ja">バインディング結果の boolean 値</div>
	 */
	protected boolean booleanValueForBinding(String binding, boolean defaultValue) {
		return ERXComponentUtilities.booleanValueForBinding(this, binding, defaultValue);
	}

	/**
	 * <div class="en">
	 * Resolves a given binding as a boolean value with the option of specifying
	 * a boolean operator as the default value.
	 * </div>
	 * 
	 * <div class="ja">
   * 指定されているバインディングを boolean 値に変換して戻します。<br>
   * boolean operator を指定することが可能です
	 * </div>
	 * 
	 * @param binding <div class="en">binding to be resolved as a boolean value.</div>
	 *                <div class="ja">boolean 値として戻すバインディング</div>
	 * @param defaultValue <div class="en">boolean operator to be evaluated if the binding is not present.</div>
	 *                     <div class="ja">バインディングが見つからない場合のデフォルト boolean operator</div>
	 * 
	 * @return <div class="en">result of evaluating binding as a boolean.</div>
	 *         <div class="ja">バインディング結果の boolean 値</div>
	 */
	protected boolean booleanValueForBinding(String binding, ERXUtilities.BooleanOperation defaultValue) {
		if (hasBinding(binding)) {
			return booleanValueForBinding(binding, false);
		}
		return defaultValue.value();
	}

	/**
	 * <div class="en">
	 * Resolves a given binding as an object in the normal fashion of calling
	 * <code>valueForBinding</code>. This has the one advantage of being able to
	 * resolve the resulting object as a {link ERXUtilities$Operation} if it is
	 * an Operation and then returning the result as the evaluation of that
	 * operation.
	 * </div>
	 * 
	 * <div class="ja">
   * 指定されているバインディングを object として戻します。<br>
   * <code>valueForBinding</code>と違って、結果オブジェクトを{link ERXUtilities$Operation}
   * として戻され、処理が含まれるの場合、処理の結果を取得します。
   * </div>
	 * 
	 * @param binding <div class="en">name of the component binding.</div>
	 *                <div class="ja">コンポーネントのバインディング名</div>
	 * 
	 * @return <div class="en">the object for the given binding and in the case that it is an
	 *         instance of an Operation the value of that operation.</div>
	 *         <div class="ja">指定されているバインディングのオブジェクト（処理の値のインスタンス）</div>
	 */
	protected Object objectValueForBinding(String binding) {
		return objectValueForBinding(binding, null);
	}

	/**
	 * <div class="en">
	 * Resolves a given binding as an object in the normal fashion of calling
	 * <code>valueForBinding</code>. This has the one advantage of being able to
	 * resolve the resulting object as a {link ERXUtilities$Operation} if it is
	 * an Operation and then returning the result as the evaluation of that
	 * operation.
	 * </div>
	 * 
	 * <div class="ja">
   * 指定されているバインディングを object として戻します。<br>
   * <code>valueForBinding</code>と違って、結果オブジェクトを{link ERXUtilities$Operation}
   * として戻され、処理が含まれるの場合、処理の結果を取得します。
   * </div>
	 * 
	 * @param binding <div class="en">name of the component binding.</div>
	 *                <div class="ja">コンポーネントのバインディング名</div>
	 * @param defaultValue <div class="en">value to be used if <code>valueForBinding</code> returns null.</div>
	 *                     <div class="ja"><code>valueForBinding</code> が null を戻す場合のデフォルト値</div>
	 * 
	 * @return <div class="en">the object for the given binding and in the case that it is an
	 *         instance of an Operation the value of that operation.</div>
	 *         <div class="ja">指定されているバインディングのオブジェクト（処理の値のインスタンス）</div>
	 */
	protected Object objectValueForBinding(String binding, Object defaultValue) {
		Object result = null;
		if (hasBinding(binding)) {
			Object o = valueForBinding(binding);
			result = (o == null) ? defaultValue : o;
		}
		else {
			result = defaultValue;
		}
		if (result instanceof ERXUtilities.Operation) {
			result = ((ERXUtilities.Operation) result).value();
		}
		return result;
	}

	/**
	 * <div class="en">
	 * Retrieves a given binding and if it is not null then returns
	 * <code>toString</code> called on the bound object.
	 * </div>
	 * 
	 * <div class="ja">
   * 指定されているバインディングのオブジェクトが nullでなければ、<br>
   * <code>toString</code> が呼ばれ、結果を戻します。
   * </div>
	 * 
	 * @param binding <div class="en">name of the component binding.</div>
	 *                <div class="ja">コンポーネントのバインディング名</div>
	 * 
	 * @return <div class="en">resolved binding in string format</div>
	 *         <div class="ja">指定されているバインディングの文字列表現</div>
	 */
	protected String stringValueForBinding(String binding) {
		return stringValueForBinding(binding, null);
	}

	/**
	 * <div class="en">
	 * Retrieves a given binding and if it is not null then returns
	 * <code>toString</code> called on the bound object.
	 * </div>
	 * 
	 * <div class="ja">
   * 指定されているバインディングのオブジェクトが nullでなければ、<br>
   * <code>toString</code> が呼ばれ、結果を戻します。
   * </div>
	 * 
	 * @param binding <div class="en">name of the component binding.</div>
	 *                <div class="ja">コンポーネントのバインディング名</div>
	 * @param defaultValue <div class="en">value to be used if <code>valueForBinding</code> returns null.</div>
	 *                     <div class="ja"><code>valueForBinding</code> が null を戻す場合のデフォルト値</div>
	 * 
	 * @return <div class="en">resolved binding in string format</div>
	 *         <div class="ja">指定されているバインディングの文字列表現</div>
	 */
	protected String stringValueForBinding(String binding, String defaultValue) {
		Object v = objectValueForBinding(binding, defaultValue);
		return v != null ? v.toString() : null;
	}
	
	/**
	 * <div class="en">
	 * Resolves a given binding as an NSArray object.
	 * </div>
	 * 
	 * <div class="ja">
   * 指定されているバインディングを NSArray 値に変換して戻します。
   * </div>
	 * 
	 * @param <T> type of array elements
	 * @param binding <div class="en">name of the component binding.</div>
	 *                <div class="ja">NSArray 値として戻すバインディング</div>
	 * 
	 * @return <div class="en">result of evaluating binding as an NSArray.</div>
	 *         <div class="ja">バインディング結果の NSArray 値</div>
	 */
	protected <T> NSArray<T> arrayValueForBinding(String binding) {
		return arrayValueForBinding(binding, null);
	}

	/**
	 * <div class="en">
	 * Resolves a given binding as an NSArray object.
	 * </div>
	 * 
	 * <div class="ja">
   * 指定されているバインディングを NSArray 値に変換して戻します。
   * </div>
	 * 
	 * @param <T> type of array elements
	 * @param binding <div class="en">name of the component binding.</div>
	 *                <div class="ja">NSArray 値として戻すバインディング</div>
	 * @param defaultValue <div class="en">value to be used if <code>valueForBinding</code> returns null.</div>
	 *                     <div class="ja">バインディングが見つからない場合のデフォルト NSArray 値</div>
	 * 
	 * @return <div class="en">result of evaluating binding as an NSArray.</div>
	 *         <div class="ja">バインディング結果の NSArray 値</div>
	 */
	@SuppressWarnings("unchecked")
	protected <T> NSArray<T> arrayValueForBinding(String binding, NSArray<T> defaultValue) {
		return ERXValueUtilities.arrayValueWithDefault(valueForBinding(binding), defaultValue);
	}

	/**
	 * <div class="en">
	 * Convenience method to get the localizer.
	 * </div>
	 * 
	 * <div class="ja">
   * ローカライザーを取得する為の便利なメソッド
	 * </div>
	 * 
	 * @return <div class="en">the current localizer</div>
	 *         <div class="ja">カレント・ローカライザー</div>
	 */
	public ERXLocalizer localizer() {
		return ERXLocalizer.currentLocalizer();
	}

  /** 
   * <div class="en">
   * Convenience method to get the browser.
   * </div>
   * 
   * <div class="ja">
   * browser オブジェクトを戻します。基本的には session にも directaction にも browser オブジェクトへのアクセスがありますが、
   * session 又は directaction 内にあるかどうか分からない時にはこのコマンドが便利です。
   * </div>
	 * 
	 * @return <div class="en">the current browser</div>
	 *         <div class="ja">browser オブジェクト</div>
   */
  public ERXBrowser browser() {
    ERXRequest request = (ERXRequest) context().request();
    return request.browser();
  }

	/**
	 * <div class="en">
	 * Lazily initialized dictionary which can be used for the 'item' binding in
	 * a repetition for example: 'item = dynamicBindings.myVariable'. Useful in
	 * rapid turnaround modes where adding a iVar would cause hot code swapping
	 * to stop working.
	 * </div>
	 * 
	 * <div class="ja">
   * ダイナミック・バインディング用ディクショナリー
   * repetition内、バインディングの 'item' として使用できます。<br>
   * 例えば、 'item = dynamicBindings.myVariable'
   * </div>
	 * 
	 * @return <div class="en">a dictionay for use with dynamic bindings</div>
	 *         <div class="ja">NSMutableDictionary ダイナミック・バインディング・ディクショナリー</div>
	 */
	public NSMutableDictionary dynamicBindings() {
		if (_dynamicBindings == null) {
			_dynamicBindings = new NSMutableDictionary();
		}
		return _dynamicBindings;
	}

  /**
   * <div class="ja">
   * このメソッドは、指定されているコンテクストのオブジェクトに対する、
   * ステートレス・コンポーネントの一時的リファレンスをリセットもしくは削除します。
   * あるコンポーネントの共有化されたインスタンスが、他のセッションによって再利用されるとき、
   * このメソッドを利用し、各コンポーネントのインスタンス変数を解放します。
   * </div>
   */
	@Override
	public void reset() {
		super.reset();
		if (_dynamicBindings != null) {
			_dynamicBindings.removeAllObjects();
		}
	}

	/**
	 * <div class="en">
	 * Returns the name of this component without the package name.
	 * </div>
	 * 
	 * <div class="ja">
   * このコンポーネントの名前を戻します。（パッケージ無し）
   * </div>
	 * 
	 * @return <div class="en">the name of this component without the package name</div>
	 *         <div class="ja">このコンポーネントの名前</div>
	 */
	public String componentName() {
		String componentName = name();
		if (componentName != null) {
			int lastDotIndex = componentName.lastIndexOf('.');
			if (lastDotIndex != -1) {
				componentName = componentName.substring(lastDotIndex + 1);
			}
		}
		return componentName;
	}

	/**
	 * <div class="en">
	 * Injects per-component CSS dependencies into the head tag based on the
	 * definitions in useDefaultComponentCSS(), defaultCSSPath(),
	 * primaryCSSFile(), and additionalCSSFiles().
	 * <p>
	 * If you return true for useDefaultComponentCSS (and do not override
	 * primaryCSSFile), this component will inject a reference to
	 * defaultCSSPath() + /YourComponentName.css. For instance, if your
	 * component is named HeaderFooter, useDefaultComponentCSS will
	 * automatically add a reference to defaultCSSPath() + /HeaderFooter.css for
	 * you. This allows you to very easily specify per-component CSS files
	 * without upper-level components knowing about them. Currently
	 * _includeCSSResources does not try to do anything fancy in terms of
	 * recombining CSS files.
	 * <p>
	 * Override defaultCSSPath to provide the base path relative to
	 * WebServerResources that contains your CSS files. If all of your CSS is in
	 * WebServerResources/css, you would return "css" from defaultCSSPath().
	 * <p>
	 * If you do not want to use the component's name as the name of the CSS
	 * file, you can optionally override primaryCSSFile() to return the name of
	 * a specific CSS file, as well as additionalCSSFiles() to return an NSArray
	 * of CSS files. All of these file names will be prepended with the
	 * defaultCSSPath if it is set.
	 * </div>
	 * 
	 * <div class="ja">
   * useDefaultComponentCSS(), defaultCSSPath(),primaryCSSFile(), と additionalCSSFiles()
   * で定義されているコンポーネントに依存した CSS をヘッダー内に挿入します。
   * <p>
   * useDefaultComponentCSS で true を戻す場合　（primaryCSSFileをオーバライドしないで）
   * このコンポーネントは defaultCSSPath() + /YourComponentName.css をヘッダーに挿入します。
   * 例えば、コンポーネントの名前が HeaderFooter とし、useDefaultComponentCSS は自動的に
   * defaultCSSPath() + /HeaderFooter.css へのレファレンスを作成します。
   * この機能を使うことで、簡単にコンポーネントに属している CSS ファイルを作成でき、上位のコンポーネントを
   * 意識する必要がありません。
   * 現在では _includeCSSResources は CSS ファイルの結合などを行いません。
   * <p>
   * defaultCSSPath をオーバライドすること、CSS ファイルを含む WebServerResources への元パスを提供します。
   * すべての CSS が WebServerResources/css 内にある場合、defaultCSSPath() として "css" を戻します。
   * <p>
   * CSS ファイルとしてコンポーネント名を使用したく無い場合、primaryCSSFile() をオーバライドすることで、
   * ある CSS ファイル名を戻します。それとも、additionalCSSFiles() で CSS ファイルの NSArray を戻します。
   * すべてのファイル名は defaultCSSPath を先頭に追加されるのです。
   * </div>
   * 
	 * @param response <div class="en">the response to write into</div>
	 *                 <div class="ja">書き込みするリスポンス</div>
	 * @param context <div class="en">the current context</div>
	 *                <div class="ja">カレント・コンテクスト</div>
	 */
	protected void _includeCSSResources(WOResponse response, WOContext context) {
		String primaryCSSFile = primaryCSSFile();
		if (primaryCSSFile == null && useDefaultComponentCSS()) {
			String componentName = componentName();
			primaryCSSFile = componentName + ".css";
		}
		if (primaryCSSFile != null) {
			String defaultCSSPath = defaultCSSPath();
			if (defaultCSSPath != null && defaultCSSPath.length() > 0 && !defaultCSSPath.endsWith("/")) {
				defaultCSSPath += "/";
			}
			String frameworkName = _frameworkName();
			ERXResponseRewriter.addStylesheetResourceInHead(response, context, frameworkName, defaultCSSPath + primaryCSSFile);
		}

		NSArray<String> additionalCSSFiles = additionalCSSFiles();
		if (additionalCSSFiles != null) {
			String defaultCSSPath = defaultCSSPath();
			if (defaultCSSPath != null && defaultCSSPath.length() > 0 && !defaultCSSPath.endsWith("/")) {
				defaultCSSPath += "/";
			}
			String frameworkName = _frameworkName();
			for (String additionalCSSFile : additionalCSSFiles) {
				ERXResponseRewriter.addStylesheetResourceInHead(response, context, frameworkName, defaultCSSPath + additionalCSSFile);
			}
		}
	}

	/**
	 * <div class="en">
	 * Injects per-component javascript dependencies into the head tag based on
	 * the definitions in useDefaultComponentJavascript(),
	 * defaultJavascriptPath(), primaryJavascriptFile(), and
	 * additionalJavascriptFiles().
	 * <p>
	 * If you return true for useDefaultComponentJavascript (and do not override
	 * primaryJavascriptFile), this component will inject a reference to
	 * defaultJavascriptPath() + /YourComponentName.js. For instance, if your
	 * component is named HeaderFooter, useDefaultComponentJavascript will
	 * automatically add a reference to defaultJavascriptPath() +
	 * /HeaderFooter.js for you. This allows you to very easily specify
	 * per-component Javascript files without upper-level components knowing
	 * about them. Currently _includeJavascriptResources does not try to do
	 * anything fancy in terms of recombinding Javascript files.
	 * <p>
	 * Override defaultJavascriptPath to provide the base path relative to
	 * WebServerResources that contains your Javascript files. If all of your
	 * Javascript is in WebServerResources/scripts, you would return "scripts"
	 * from defaultJavascriptPath().
	 * <p>
	 * If you do not want to use the component's name as the name of the
	 * Javascript file, you can optionally override primaryJavascriptFile() to
	 * return the name of a specific Javascript file, as well as
	 * additionalJavascriptFiles() to return an NSArray of Javascript files. All
	 * of these file names will be prepended with the defaultJavascriptPath if
	 * it is set.
	 * </div>
	 * 
	 * <div class="ja">
   * useDefaultComponentJavascript(), defaultJavascriptPath(), primaryJavascriptFile(), と additionalJavascriptFiles()
   * で定義されているコンポーネントに依存した javascript をヘッダー内に挿入します。
   * <p>
   * useDefaultComponentJavascript で true を戻す場合　（primaryJavascriptFileをオーバライドしないで）
   * このコンポーネントは defaultJavascriptPath() + /YourComponentName.js をヘッダーに挿入します。
   * 例えば、コンポーネントの名前が HeaderFooter とし、useDefaultComponentJavascript は自動的に
   * defaultJavascriptPath() + /HeaderFooter.js へのレファレンスを作成します。
   * この機能を使うことで、簡単にコンポーネントに属している Javascript ファイルを作成でき、上位のコンポーネントを
   * 意識する必要がありません。
   * 現在では _includeJavascriptResources は Javascript ファイルの結合などを行いません。
   * <p>
   * defaultJavascriptPath をオーバライドすること、Javascript ファイルを含む WebServerResources への元パスを提供します。
   * すべての Javascript が WebServerResources/scripts 内にある場合、defaultJavascriptPath() として "scripts" を戻します。
   * <p>
   * Javascript ファイルとしてコンポーネント名を使用したく無い場合、primaryJavascriptFile() をオーバライドすることで、
   * ある Javascript ファイル名を戻します。それとも、additionalJavascriptFiles() で Javascript ファイルの NSArray を戻します。
   * すべてのファイル名は defaultJavascriptPath を先頭に追加されるのです。
	 * </div>
	 * 
	 * @param response <div class="en">the response to write into</div>
	 *                 <div class="ja">書き込みするリスポンス</div>
	 * @param context <div class="en">the current context</div>
	 *                <div class="ja">カレント・コンテクスト</div>
	 */
	protected void _includeJavascriptResources(WOResponse response, WOContext context) {
		String primaryJavascriptFile = primaryJavascriptFile();
		if (primaryJavascriptFile == null && useDefaultComponentJavascript()) {
			String componentName = componentName();
			primaryJavascriptFile = componentName + ".js";
		}
		if (primaryJavascriptFile != null) {
			String defaultJavascriptPath = defaultJavascriptPath();
			if (defaultJavascriptPath != null && defaultJavascriptPath.length() > 0 && !defaultJavascriptPath.endsWith("/")) {
				defaultJavascriptPath += "/";
			}
			String frameworkName = _frameworkName();
			ERXResponseRewriter.addScriptResourceInHead(response, context, frameworkName, defaultJavascriptPath + primaryJavascriptFile);
		}

		NSArray<String> additionalJavascriptFiles = additionalJavascriptFiles();
		if (additionalJavascriptFiles != null) {
			String defaultJavascriptPath = defaultJavascriptPath();
			if (defaultJavascriptPath != null && defaultJavascriptPath.length() > 0 && !defaultJavascriptPath.endsWith("/")) {
				defaultJavascriptPath += "/";
			}
			String frameworkName = _frameworkName();
			for (String additionalJavascriptFile : additionalJavascriptFiles) {
				ERXResponseRewriter.addScriptResourceInHead(response, context, frameworkName, defaultJavascriptPath + additionalJavascriptFile);
			}
		}
	}

	/**
	 * <div class="en">
	 * Returns the name of this component's framework or "app" if
	 * frameworkName() returns null.
	 * </div>
	 * 
	 * <div class="ja">
   * このコンポーネントのフレームワークを戻します。
   * frameworkName() が null の場合には "app" が戻ります。
   * </div>
   * 
	 * @return <div class="en">the name of this component's framework</div>
	 *         <div class="ja">このコンポーネントのフレームワーク</div>
	 */
	protected String _frameworkName() {
		String frameworkName = super.frameworkName();
		if (frameworkName == null) {
			frameworkName = "app";
		}
		return frameworkName;
	}

	/**
	 * <div class="en">
	 * Returns true if this component provides a default CSS file that has the
	 * same name as the component itself.
	 * </div>
	 * 
	 * <div class="ja">
   * コンポーネントと同じ名前を持つデフォルト CSS ファイルがある場合には true が戻ります。
   * </div>
   * 
	 * @return <div class="en">true if this component provides a default-named CSS</div>
	 *         <div class="ja">デフォルト CSS ファイルがある場合には true</div>
	 */
	protected boolean useDefaultComponentCSS() {
		return false;
	}

	/**
	 * <div class="en">
	 * Returns the default path prefix for CSS, which will be prepended to all
	 * required CSS files for this component. The default is "".
	 * </div>
	 * 
	 * <div class="ja">
   * CSS へのデフォルトパスの先頭を戻します。このコンポーネントの全 CSS ファイルに適用します。
   * デフォルトは ""
   * </div>
   * 
	 * @return <div class="en">the default CSS path.</div>
	 *         <div class="ja">デフォルト CSS パス</div>
	 */
	protected String defaultCSSPath() {
		return "";
	}

	/**
	 * <div class="en">
	 * Returns the primary CSS file for this component, or null if there isn't
	 * one. This path will be prepended with defaultCSSPath().
	 * </div>
	 * 
	 * <div class="ja">
   * このコンポーネントのメイン CSS ファイルを戻します。無ければ、 null が戻ります。
   * このパスは defaultCSSPath() と合成されます。
   * </div>
   * 
	 * @return <div class="en">the primary CSS file for this component</div>
	 *         <div class="ja">このコンポーネントのメイン CSS ファイル</div>
	 */
	protected String primaryCSSFile() {
		return null;
	}

	/**
	 * <div class="en">
	 * Returns an array of additional CSS files for this component, or null (or
	 * empty array) if there aren't any. Each path will be prepended with
	 * defaultCSSPath().
	 * </div>
	 * 
	 * <div class="ja">
   * このコンポーネントに必要なオプション CSS ファイルの配列。無ければ、null (empty array)
   * 各自のパスは defaultCSSPath() と合成されます。
   * </div>
   * 
	 * @return <div class="en">an array of additional CSS files for this component.</div>
	 *         <div class="ja">このコンポーネントに必要なオプション CSS ファイルの配列</div>
	 */
	protected NSArray<String> additionalCSSFiles() {
		return null;
	}

	/**
	 * <div class="en">
	 * Returns true if this component provides a default Javascript file that
	 * has the same name as the component itself.
	 * </div>
	 * 
	 * <div class="ja">
   * コンポーネントと同じ名前を持つデフォルト Javascript ファイルがある場合には true が戻ります。
   * </div>
   * 
	 * @return <div class="en">true if this component provides a default-named Javascript</div>
	 *         <div class="ja">デフォルト Javascript ファイルがある場合には true</div>
	 */
	protected boolean useDefaultComponentJavascript() {
		return false;
	}

	/**
	 * <div class="en">
	 * Returns the default path prefix for Javascript, which will be prepended
	 * to all required Javascript files for this component. The default is "".
	 * </div>
	 * 
	 * <div class="ja">
   * Javascript へのデフォルトパスの先頭を戻します。このコンポーネントの全 Javascript ファイルに適用します。
   * デフォルトは ""
	 * </div>
   * 
	 * @return <div class="en">the default Javascript path.</div>
	 *         <div class="ja">デフォルト Javascript パス</div>
	 */
	protected String defaultJavascriptPath() {
		return "";
	}

	/**
	 * <div class="en">
	 * Returns the primary Javascript file for this component, or null if there
	 * isn't one. This path will be prepended with defaultJavascriptPath().
	 * </div>
	 * 
	 * <div class="ja">
   * このコンポーネントのメイン Javascript ファイルを戻します。無ければ、 null が戻ります。
   * このパスは defaultJavascriptPath() と合成されます。
   * </div>
   * 
	 * @return <div class="en">the primary Javascript file for this component</div>
	 *         <div class="ja">このコンポーネントのメイン Javascript ファイル</div>
	 */
	protected String primaryJavascriptFile() {
		return null;
	}

	/**
	 * <div class="en">
	 * Returns an array of additional Javascript files for this component, or
	 * null (or empty array) if there aren't any. Each path will be prepended
	 * with defaultJavascriptPath().
	 * </div>
	 * 
	 * <div class="ja">
   * このコンポーネントに必要なオプション Javascript ファイルの配列。無ければ、null (empty array)
   * 各自のパスは defaultJavascriptPath() と合成されます。
   * </div>
   * 
	 * @return <div class="en">an array of additional Javascript files for this component.</div>
	 *         <div class="ja">このコンポーネントに必要なオプション Javascript ファイルの配列</div>
	 */
	protected NSArray<String> additionalJavascriptFiles() {
		return null;
	}

	/**
	 * <div class="en">
	 * Override and return true for any components to which you would like to
	 * allow page level access.
	 * </div>
	 * 
	 * <div class="ja">
   * ページ・レベル・アクセス
   * true はページへのアクセスが可能です
   * </div>
   * 
	 * @return <div class="en">true by default</div>
	 *         <div class="ja">デフォルトは true</div>
	 */
	protected boolean isPageAccessAllowed() {
		return true;
	}

	/**
	 * <div class="en">
	 * Override to provide custom security checks. It is not necessary to call
	 * super on this method.
	 * </div>
	 * 
	 * <div class="ja">
   * カスタム・セキュリティ・チェックを行う場合にはオーバライドするといいのです。
   * スーパーを呼ぶ必要ありません。
   * </div>
   * 
   * @throws SecurityException <div class="en">if the security check fails</div>
	 *                         <div class="ja">セキュリティ・チェックが失敗した場合</div>
	 */
	protected void checkAccess() throws SecurityException {
	}

	/**
	 * <div class="en">
	 * Override to hook into appendToResponse after security checks but before
	 * the super.appendToResponse. It is not necessary to call super on this
	 * method.
	 * </div>
	 * 
	 * <div class="ja">
   * セキュリティ・チェックの後で、super.appendToResponse を呼ぶ前の横取り
   * このフェーズに何がする必要がある場合は、オーバライドするといいのです
   * この中ではスーパーを呼ぶ必要ありません。
   * </div>
   * 
	 * @param response <div class="en">the current response</div>
	 *                 <div class="ja">カレント・リスポンス</div>
	 * @param context <div class="en">the current context</div>
	 *                <div class="ja">カレント・コンテクスト</div>
	 */
	protected void preAppendToResponse(WOResponse response, WOContext context) {
	}

	/**
	 * <div class="en">
	 * Override to hook into appendToResponse after super.appendToResponse. It
	 * is not necessary to call super on this method.
	 * </div>
	 * 
	 * <div class="ja">
   * super.appendToResponse を直後の横取り
   * このフェーズに何がする必要がある場合は、オーバライドするといいのです
   * この中ではスーパーを呼ぶ必要ありません。
   * </div>
   * 
	 * @param response <div class="en">the current response</div>
	 *                 <div class="ja">カレント・リスポンス</div>
	 * @param context <div class="en">the current context</div>
	 *                <div class="ja">カレント・コンテクスト</div>
	 */
	protected void postAppendToResponse(WOResponse response, WOContext context) {
	}
}
