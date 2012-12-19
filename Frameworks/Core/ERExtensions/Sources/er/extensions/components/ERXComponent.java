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
 * <span class="en">
 * ERXComponent provides a common base class for WOComponents along with a bunch
 * of miscellaneous handy features.
 * </span>
 * 
 * <span class="ja">
 * ERXComponent は WOComponents のサブクラスで、多数の便利なコマンドを用意しています
 * </span>
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
	 * <span class="en">
	 * This variant of pageWithName provides a Java5 genericized version of the
	 * original pageWithName. You would call it with:
	 * 
	 * MyNextPage nextPage = pageWithName(MyNextPage.class);
	 * 
	 * @param <T> - the type of component to create
	 * @param componentClass - the Class of the component to load
	 * 
	 * @return an instance of the requested component class
	 * </span>
	 * 
	 * <span class="ja">
   * Java5 の新しい pageWithName コマンド。
   * 次の様に呼ばれます：
   * 
   * MyNextPage nextPage = pageWithName(MyNextPage.class);
   * 
   * @param <T> - 作成するコンポーネント・タイプ
   * @param componentClass - ロードするコンポーネントのクラス
   * 
   * @return コンポーネント・クラスのインスタンス
	 * </span>
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
	 * <span class="en">
	 * _checkAccess is called prior to all three phases of the R-R loop to
	 * ensure that the user has permission to access this component. You should
	 * override checkAccess to implement addition security checks.
	 * 
	 * @throws SecurityException
	 *             if the user does not have permission
	 * </span>
	 * 
	 * <span class="ja">
   * 各リスポンス・リクエスト・ループの最初に _checkAccess 実行され、ユーザがこのコンポーネントへのアクセスが可能かどうかをチェックします。
   * 
   * 他のセキュリティ・チェックを行う時には checkAccess をオーバライドすると良い
   * 
   * @throws SecurityException - ユーザがアクセス権を持っていない場合
	 * </span>
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
	 * <span class="en">
	 * Calls _checkAccess prior to super.takeValuesFromRequest.
	 * 
	 * @param request the current request with the WOComponent object
	 * @param context context of a transaction
	 * </span>
	 * 
	 * <span class="ja">
	 * スーパーの前に _checkAccess のセキュリティ・チェックを行います
	 * 
   * @param request - リクエスト
   * @param context - トランスアクションのコンテキスト
	 * </span>
	 */
	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		_checkAccess();
		super.takeValuesFromRequest(request, context);
	}

	/**
	 * <span class="en">
	 * Calls _checkAccess prior to super.invokeAction.
	 * 
	 * @param request the request
	 * @param context context of the transaction
	 * 
	 * @return a WOActionResults containing the result of the request
	 * </span>
	 * 
	 * <span class="ja">
	 * スーパーの前に _checkAccess のセキュリティ・チェックを行います
	 * 
   * @param request - リクエスト
   * @param context - トランスアクションのコンテキスト
   * 
   * @return リクエスト結果を含む WOActionResults
	 * </span>
	 */
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		_checkAccess();
		return super.invokeAction(request, context);
	}

	/**
	 * <span class="en">
	 * Calls _checkAccess prior to super.appendToResponse and adds support for
	 * ClickToOpen (TM).
	 * 
	 * @param response - the HTTP response that an application returns to a Web server to complete a cycle of the request-response loop
	 * @param context -  context of a transaction
	 * </span>
	 * 
	 * <span class="ja">
	 * スーパーの前にセキュリティ・チェックし、ClickToOpen (TM) サポート
	 * 
   * @param response - RR ループの最後のリスポンス結果を戻す
   * @param context - トランスアクションのコンテキスト
	 * </span>
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
	 * <span class="en">
	 * Returns whether or not click-to-open should be enabled for this
	 * component. By default this returns ERXClickToOpenSupport.isEnabled().
	 * 
	 * @param response - the response
	 * @param context - the context
	 * 
	 * @return whether or not click-to-open is enabled for this component
	 * </span>
	 * 
	 * <span class="ja">
	 * このコンポーネントで click-to-open をサポートしているかどうかを戻します。
   * デフォルトでは ERXClickToOpenSupport.isEnabled() を戻します。
   * 
   * @param response - リスポンス
   * @param context - コンテクスト
   * 
   * @return このコンポーネントで click-to-open をサポートしているかどうか
	 * </span>
	 */
	public boolean clickToOpenEnabled(WOResponse response, WOContext context) {
		return ERXClickToOpenSupport.isEnabled();
	}

	/**
	 * <span class="en">
	 * Resolves a given binding as a int value. Useful for image sizes and the
	 * like.
	 * 
	 * @param binding - binding to be resolved as a int value.
	 * @param defaultValue - default int value to be used if the binding is not bound.
	 * 
	 * @return result of evaluating binding as a int.
	 * </span>
	 * 
	 * <span class="ja">
   * 指定されているバインディングを int 値に変換して戻します。<br>
   * イメージ・サイズなどに有効です
   * 
   * @param binding - int 値として戻すバインディング
   * @param defaultValue - バインディングが見つからない場合のデフォルト int 値
   * 
   * @return result - バインディング結果の int 値
   * </span>
	 */
	protected int intValueForBinding(String binding, int defaultValue) {
		return ERXValueUtilities.intValueWithDefault(valueForBinding(binding), defaultValue);
	}

	/**
	 * <span class="en">
	 * Resolves a given binding as a float value.
	 * 
	 * @param binding
	 *            binding to be resolved as a float value.
	 * @param defaultValue
	 *            default float value to be used if the binding is not bound.
	 * @return result of evaluating binding as a float.
	 * </span>
	 * 
	 * <span class="ja">
   * 指定されているバインディングを float 値に変換して戻します。<br>
   * 
   * @param binding - float 値として戻すバインディング
   * @param defaultValue - バインディングが見つからない場合のデフォルト float 値
   * 
   * @return result - バインディング結果の float 値
   * </span>
	 */
	protected float floatValueForBinding(String binding, float defaultValue) {
		return ERXValueUtilities.floatValueWithDefault(valueForBinding(binding), defaultValue);
	}

	/**
	 * <span class="en">
	 * Resolves a given binding as a boolean value. Defaults to false.
	 * 
	 * @param binding - binding to be resolved as a boolean value.
	 * 
	 * @return result of evaluating binding as a boolean.
	 * </span>
	 * 
	 * <span class="ja">
   * 指定されているバインディングを boolean 値に変換して戻します。<br>
   * デフォルトは false です
   * 
   * @param binding - boolean 値として戻すバインディング
   * 
   * @return result - バインディング結果の boolean 値
   * </span>
	 */
	protected boolean booleanValueForBinding(String binding) {
		return booleanValueForBinding(binding, false);
	}

	/**
	 * <span class="en">
	 * Resolves a given binding as a boolean value.
	 * 
	 * @param binding - binding to be resolved as a boolean value.
	 * @param defaultValue - default boolean value to be used if the binding is not bound.
	 * 
	 * @return result of evaluating binding as a boolean.
	 * </span>
	 * 
	 * <span class="ja">
   * 指定されているバインディングを boolean 値に変換して戻します。
   * 
   * @param binding - boolean 値として戻すバインディング
   * @param defaultValue - バインディングが見つからない場合のデフォルト boolean 値
   * 
   * @return result - バインディング結果の boolean 値
   * </span>
	 */
	protected boolean booleanValueForBinding(String binding, boolean defaultValue) {
		return ERXComponentUtilities.booleanValueForBinding(this, binding, defaultValue);
	}

	/**
	 * <span class="en">
	 * Resolves a given binding as a boolean value with the option of specifing
	 * a boolean operator as the default value.
	 * 
	 * @param binding - name of the component binding.
	 * @param defaultValue - boolean operator to be evaluated if the binding is not present.
	 * 
	 * @return result of evaluating binding as a boolean.
	 * </span>
	 * 
	 * <span class="ja">
   * 指定されているバインディングを boolean 値に変換して戻します。<br>
   * boolean operator を指定することが可能です
   * 
   * @param binding - boolean 値として戻すバインディング
   * @param defaultValue - バインディングが見つからない場合のデフォルト boolean operator
   * 
   * @return result - バインディング結果の boolean 値
	 * </span>
	 */
	protected boolean booleanValueForBinding(String binding, ERXUtilities.BooleanOperation defaultValue) {
		if (hasBinding(binding)) {
			return booleanValueForBinding(binding, false);
		}
		return defaultValue.value();
	}

	/**
	 * <span class="en">
	 * Resolves a given binding as an object in the normal fashion of calling
	 * <code>valueForBinding</code>. This has the one advantage of being able to
	 * resolve the resulting object as a {link ERXUtilities$Operation} if it is
	 * an Operation and then returning the result as the evaluation of that
	 * operation.
	 * 
	 * @param binding - name of the component binding.
	 * 
	 * @return the object for the given binding and in the case that it is an
	 *         instance of an Operation the value of that operation.
	 * </span>
	 * 
	 * <span class="ja">
   * 指定されているバインディングを object として戻します。<br>
   * <code>valueForBinding</code>と違って、結果オブジェクトを{link ERXUtilities$Operation}
   * として戻され、処理が含まれるの場合、処理の結果を取得します。
   * 
   * @param binding - コンポーネントのバインディング名
   * 
   * @return 指定されているバインディングのオブジェクト（処理の値のインスタンス）
   * </span>
	 */
	protected Object objectValueForBinding(String binding) {
		return objectValueForBinding(binding, null);
	}

	/**
	 * <span class="en">
	 * Resolves a given binding as an object in the normal fashion of calling
	 * <code>valueForBinding</code>. This has the one advantage of being able to
	 * resolve the resulting object as a {link ERXUtilities$Operation} if it is
	 * an Operation and then returning the result as the evaluation of that
	 * operation.
	 * 
	 * @param binding - name of the component binding.
	 * @param defaultValue - value to be used if <code>valueForBinding</code> returns null.
	 * 
	 * @return the object for the given binding and in the case that it is an
	 *         instance of an Operation the value of that operation.
	 * </span>
	 * 
	 * <span class="ja">
   * 指定されているバインディングを object として戻します。<br>
   * <code>valueForBinding</code>と違って、結果オブジェクトを{link ERXUtilities$Operation}
   * として戻され、処理が含まれるの場合、処理の結果を取得します。
   * 
   * @param binding - コンポーネントのバインディング名
   * @param defaultValue - <code>valueForBinding</code> が null を戻す場合のデフォルト値
   * 
   * @return 指定されているバインディングのオブジェクト（処理の値のインスタンス）
   * </span>
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
	 * <span class="en">
	 * Retrieves a given binding and if it is not null then returns
	 * <code>toString</code> called on the bound object.
	 * 
	 * @param binding - to be resolved
	 * 
	 * @return resolved binding in string format
	 * </span>
	 * 
	 * <span class="ja">
   * 指定されているバインディングのオブジェクトが nullでなければ、<br>
   * <code>toString</code> が呼ばれ、結果を戻します。
   * 
   * @param binding - コンポーネントのバインディング名
   * 
   * @return 指定されているバインディングの文字列表現
   * </span>
	 */
	protected String stringValueForBinding(String binding) {
		return stringValueForBinding(binding, null);
	}

	/**
	 * <span class="en">
	 * Retrieves a given binding and if it is not null then returns
	 * <code>toString</code> called on the bound object.
	 * 
	 * @param binding - to be resolved
	 * @param defaultValue - 
	 *            value to be used if <code>valueForBinding</code> returns null.
	 *            
	 * @return resolved binding in string format
	 * </span>
	 * 
	 * <span class="ja">
   * 指定されているバインディングのオブジェクトが nullでなければ、<br>
   * <code>toString</code> が呼ばれ、結果を戻します。
   * 
   * @param binding - コンポーネントのバインディング名
   * @param defaultValue - <code>valueForBinding</code> が null を戻す場合のデフォルト値
   * 
   * @return 指定されているバインディングの文字列表現
   * </span>
	 */
	protected String stringValueForBinding(String binding, String defaultValue) {
		Object v = objectValueForBinding(binding, defaultValue);
		return v != null ? v.toString() : null;
	}
	
	/**
	 * <span class="en">
	 * Resolves a given binding as an NSArray object.
	 * 
	 * @param binding
	 *            binding to be resolved as an NSArray.
	 *            
	 * @return result of evaluating binding as an NSArray.
	 * </span>
	 * 
	 * <span class="ja">
   * 指定されているバインディングを NSArray 値に変換して戻します。
   * 
   * @param binding - NSArray 値として戻すバインディング
   * 
   * @return result - バインディング結果の NSArray 値
   * </span>
	 */
	protected <T> NSArray<T> arrayValueForBinding(String binding) {
		return arrayValueForBinding(binding, null);
	}

	/**
	 * <span class="en">
	 * Resolves a given binding as an NSArray object.
	 * 
	 * @param binding - binding to be resolved as an NSArray.
	 * @param defaultValue - 
	 *            default NSArray value to be used if the binding is not bound.
	 *            
	 * @return result of evaluating binding as an NSArray.
	 * </span>
	 * 
	 * <span class="ja">
   * 指定されているバインディングを NSArray 値に変換して戻します。
   * 
   * @param binding - NSArray 値として戻すバインディング
   * @param defaultValue - バインディングが見つからない場合のデフォルト NSArray 値
   * 
   * @return result - バインディング結果の NSArray 値
   * </span>
	 */
	@SuppressWarnings("unchecked")
	protected <T> NSArray<T> arrayValueForBinding(String binding, NSArray<T> defaultValue) {
		return ERXValueUtilities.arrayValueWithDefault(valueForBinding(binding), defaultValue);
	}

	/**
	 * <span class="en">
	 * Convenience method to get the localizer.
	 * 
	 * @return the current localizer 
	 * </span>
	 * <span class="ja">
   * ローカライザーを取得する為の便利なメソッド
   * 
   * @return カレント・ローカライザー
	 * </span>
	 */
	public ERXLocalizer localizer() {
		return ERXLocalizer.currentLocalizer();
	}

  /** 
   * <span class="en">
   * Convenience method to get the browser.
   * 
   * @return the current browser 
   * </span>
   * 
   * <span class="ja">
   * browser オブジェクトを戻します。基本的には session にも directaction にも browser オブジェクトへのアクセスがありますが、
   * session 又は directaction 内にあるかどうか分からない時にはこのコマンドが便利です。
   * 
   * @return browser オブジェクト
   * </span>
   */
  public ERXBrowser browser() {
    ERXRequest request = (ERXRequest) context().request();
    return request.browser();
  }

	/**
	 * <span class="en">
	 * Lazily initialized dictionary which can be used for the 'item' binding in
	 * a repetition for example: 'item = dynamicBindings.myVariable'. Useful in
	 * rapid turnaround modes where adding a iVar would cause hot code swapping
	 * to stop working.
	 * 
	 * @return a dictionay for use with dynamic bindings 
	 * </span>
	 * 
	 * <span class="ja">
   * ダイナミック・バインディング用ディクショナリー
   * repetition内、バインディングの 'item' として使用できます。<br>
   * 例えば、 'item = dynamicBindings.myVariable'
   * 
   * @return NSMutableDictionary ダイナミック・バインディング・ディクショナリー
   * </span>
	 */
	public NSMutableDictionary dynamicBindings() {
		if (_dynamicBindings == null) {
			_dynamicBindings = new NSMutableDictionary();
		}
		return _dynamicBindings;
	}

  /**
   * <span class="ja">
   * このメソッドは、指定されているコンテクストのオブジェクトに対する、
   * ステートレス・コンポーネントの一時的リファレンスをリセットもしくは削除します。
   * あるコンポーネントの共有化されたインスタンスが、他のセッションによって再利用されるとき、
   * このメソッドを利用し、各コンポーネントのインスタンス変数を解放します。
   * </span>
   */
	@Override
	public void reset() {
		super.reset();
		if (_dynamicBindings != null) {
			_dynamicBindings.removeAllObjects();
		}
	}

	/**
	 * <span class="en">
	 * Returns the name of this component without the package name.
	 * 
	 * @return the name of this component without the package name
	 * </span>
	 * 
	 * <span class="ja">
   * このコンポーネントの名前を戻します。（パッケージ無し）
   * 
   * @return このコンポーネントの名前
   * </span>
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
	 * <span class="en">
	 * <p>
	 * Injects per-component CSS dependencies into the head tag based on the
	 * definitions in useDefaultComponentCSS(), defaultCSSPath(),
	 * primaryCSSFile(), and additionalCSSFiles().
	 * </p>
	 * <p>
	 * If you return true for useDefaultComponentCSS (and do not override
	 * primaryCSSFile), this component will inject a reference to
	 * defaultCSSPath() + /YourComponentName.css. For instance, if your
	 * component is named HeaderFooter, useDefaultComponentCSS will
	 * automatically add a reference to defaultCSSPath() + /HeaderFooter.css for
	 * you. This allows you to very easily specify per-component CSS files
	 * without upper-level components knowing about them. Currently
	 * _includeCSSResources does not try to do anything fancy in terms of
	 * recombinding CSS files.
	 * </p>
	 * <p>
	 * Override defaultCSSPath to provide the base path relative to
	 * WebServerResources that contains your CSS files. If all of your CSS is in
	 * WebServerResources/css, you would return "css" from defaultCSSPath().
	 * </p>
	 * <p>
	 * If you do not want to use the component's name as the name of the CSS
	 * file, you can optionally override primaryCSSFile() to return the name of
	 * a specific CSS file, as well as additionalCSSFiles() to return an NSArray
	 * of CSS files. All of these file names will be prepended with the
	 * defaultCSSPath if it is set.
	 * </p>
	 * 
	 * @param response - the response to write into
	 * @param context - the current context
	 * </span>
	 * 
	 * <span class="ja">
   * <p>
   * useDefaultComponentCSS(), defaultCSSPath(),primaryCSSFile(), と additionalCSSFiles()
   * で定義されているコンポーネントに依存した CSS をヘッダー内に挿入します。
   * </p>
   * <p>
   * useDefaultComponentCSS で true を戻す場合　（primaryCSSFileをオーバライドしないで）
   * このコンポーネントは defaultCSSPath() + /YourComponentName.css をヘッダーに挿入します。
   * 例えば、コンポーネントの名前が HeaderFooter とし、useDefaultComponentCSS は自動的に
   * defaultCSSPath() + /HeaderFooter.css へのレファレンスを作成します。
   * この機能を使うことで、簡単にコンポーネントに属している CSS ファイルを作成でき、上位のコンポーネントを
   * 意識する必要がありません。
   * 現在では _includeCSSResources は CSS ファイルの結合などを行いません。
   * </p>
   * <p>
   * defaultCSSPath をオーバライドすること、CSS ファイルを含む WebServerResources への元パスを提供します。
   * すべての CSS が WebServerResources/css 内にある場合、defaultCSSPath() として "css" を戻します。
   * </p>
   * <p>
   * CSS ファイルとしてコンポーネント名を使用したく無い場合、primaryCSSFile() をオーバライドすることで、
   * ある CSS ファイル名を戻します。それとも、additionalCSSFiles() で CSS ファイルの NSArray を戻します。
   * すべてのファイル名は defaultCSSPath を先頭に追加されるのです。
   * </p>
   * 
   * @param response - 書き込みするリスポンス
   * @param context - カレント・コンテクスト
   * </span>
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
	 * <span class="en">
	 * <p>
	 * Injects per-component javascript dependencies into the head tag based on
	 * the definitions in useDefaultComponentJavascript(),
	 * defaultJavascriptPath(), primaryJavascriptFile(), and
	 * additionalJavascriptFiles().
	 * </p>
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
	 * </p>
	 * <p>
	 * Override defaultJavascriptPath to provide the base path relative to
	 * WebServerResources that contains your Javascript files. If all of your
	 * Javascript is in WebServerResources/scripts, you would return "scripts"
	 * from defaultJavascriptPath().
	 * </p>
	 * <p>
	 * If you do not want to use the component's name as the name of the
	 * Javascript file, you can optionally override primaryJavascriptFile() to
	 * return the name of a specific Javascript file, as well as
	 * additionalJavascriptFiles() to return an NSArray of Javascript files. All
	 * of these file names will be prepended with the defaultJavascriptPath if
	 * it is set.
	 * </p>
	 * 
	 * @param response - the response to write into
	 * @param context - the current context
	 * </span>
	 * 
	 * <span class="ja">
   * <p>
   * useDefaultComponentJavascript(), defaultJavascriptPath(), primaryJavascriptFile(), と additionalJavascriptFiles()
   * で定義されているコンポーネントに依存した javascript をヘッダー内に挿入します。
   * </p>
   * <p>
   * useDefaultComponentJavascript で true を戻す場合　（primaryJavascriptFileをオーバライドしないで）
   * このコンポーネントは defaultJavascriptPath() + /YourComponentName.js をヘッダーに挿入します。
   * 例えば、コンポーネントの名前が HeaderFooter とし、useDefaultComponentJavascript は自動的に
   * defaultJavascriptPath() + /HeaderFooter.js へのレファレンスを作成します。
   * この機能を使うことで、簡単にコンポーネントに属している Javascript ファイルを作成でき、上位のコンポーネントを
   * 意識する必要がありません。
   * 現在では _includeJavascriptResources は Javascript ファイルの結合などを行いません。
   * </p>
   * <p>
   * defaultJavascriptPath をオーバライドすること、Javascript ファイルを含む WebServerResources への元パスを提供します。
   * すべての Javascript が WebServerResources/scripts 内にある場合、defaultJavascriptPath() として "scripts" を戻します。
   * </p>
   * <p>
   * Javascript ファイルとしてコンポーネント名を使用したく無い場合、primaryJavascriptFile() をオーバライドすることで、
   * ある Javascript ファイル名を戻します。それとも、additionalJavascriptFiles() で Javascript ファイルの NSArray を戻します。
   * すべてのファイル名は defaultJavascriptPath を先頭に追加されるのです。
   * </p>
   * 
   * @param response - 書き込みするリスポンス
   * @param context - カレント・コンテクスト
	 * </span>
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
	 * <span class="en">
	 * Returns the name of this component's framework or "app" if
	 * frameworkName() returns null.
	 * 
	 * @return the name of this component's framework
	 * </span>
	 * 
	 * <span class="ja">
   * このコンポーネントのフレームワークを戻します。
   * frameworkName() が null の場合には "app" が戻ります。
   * 
   * @return このコンポーネントのフレームワーク
   * </span>
	 */
	protected String _frameworkName() {
		String frameworkName = super.frameworkName();
		if (frameworkName == null) {
			frameworkName = "app";
		}
		return frameworkName;
	}

	/**
	 * <span class="en">
	 * Returns true if this component provides a default CSS file that has the
	 * same name as the component itself.
	 * 
	 * @return true if this component provides a default-named CSS
	 * </span>
	 * 
	 * <span class="ja">
   * コンポーネントと同じ名前を持つデフォルト CSS ファイルがある場合には true が戻ります。
   * 
   * @return デフォルト CSS ファイルがある場合には true
   * </span>
	 */
	protected boolean useDefaultComponentCSS() {
		return false;
	}

	/**
	 * <span class="en">
	 * Returns the default path prefix for CSS, which will be prepended to all
	 * required CSS files for this component. The default is "".
	 * 
	 * @return the default CSS path.
	 * </span>
	 * 
	 * <span class="ja">
   * CSS へのデフォルトパスの先頭を戻します。このコンポーネントの全 CSS ファイルに適用します。
   * デフォルトは ""
   * 
   * @return デフォルト CSS パス
   * </span>
	 */
	protected String defaultCSSPath() {
		return "";
	}

	/**
	 * <span class="en">
	 * Returns the primary CSS file for this component, or null if there isn't
	 * one. This path will be prepended with defaultCSSPath().
	 * 
	 * @return the primary CSS file for this component
	 * </span>
	 * 
	 * <span class="ja">
   * このコンポーネントのメイン CSS ファイルを戻します。無ければ、 null が戻ります。
   * このパスは defaultCSSPath() と合成されます。
   * 
   * @return このコンポーネントのメイン CSS ファイル
   * </span>
	 */
	protected String primaryCSSFile() {
		return null;
	}

	/**
	 * <span class="en">
	 * Returns an array of additional CSS files for this component, or null (or
	 * empty array) if there aren't any. Each path will be prepended with
	 * defaultCSSPath().
	 * 
	 * @return an array of additional CSS files for this component.
	 * </span>
	 * 
	 * <span class="ja">
   * このコンポーネントに必要なオプション CSS ファイルの配列。無ければ、null (empty array)
   * 各自のパスは defaultCSSPath() と合成されます。
   * 
   * @return このコンポーネントに必要なオプション CSS ファイルの配列
   * </span>
	 */
	protected NSArray<String> additionalCSSFiles() {
		return null;
	}

	/**
	 * <span class="en">
	 * Returns true if this component provides a default Javascript file that
	 * has the same name as the component itself.
	 * 
	 * @return true if this component provides a default-named Javascript
	 * </span>
	 * 
	 * <span class="ja">
   * コンポーネントと同じ名前を持つデフォルト Javascript ファイルがある場合には true が戻ります。
   * 
   * @return デフォルト Javascript ファイルがある場合には true
   * </span>
	 */
	protected boolean useDefaultComponentJavascript() {
		return false;
	}

	/**
	 * <span class="en">
	 * Returns the default path prefix for Javascript, which will be prepended
	 * to all required Javascript files for this component. The default is "".
	 * 
	 * @return the default Javascript path.
	 * </span>
	 * <span class="ja">
   * Javascript へのデフォルトパスの先頭を戻します。このコンポーネントの全 Javascript ファイルに適用します。
   * デフォルトは ""
   * 
   * @return デフォルト Javascript パス
	 * </span>
	 */
	protected String defaultJavascriptPath() {
		return "";
	}

	/**
	 * <span class="en">
	 * Returns the primary Javascript file for this component, or null if there
	 * isn't one. This path will be prepended with defaultJavascriptPath().
	 * 
	 * @return the primary Javascript file for this component
	 * </span>
	 * 
	 * <span class="ja">
   * このコンポーネントのメイン Javascript ファイルを戻します。無ければ、 null が戻ります。
   * このパスは defaultJavascriptPath() と合成されます。
   * 
   * @return このコンポーネントのメイン Javascript ファイル
   * </span>
	 */
	protected String primaryJavascriptFile() {
		return null;
	}

	/**
	 * <span class="en">
	 * Returns an array of additional Javascript files for this component, or
	 * null (or empty array) if there aren't any. Each path will be prepended
	 * with defaultJavascriptPath().
	 * 
	 * @return an array of additional Javascript files for this component.
	 * </span>
	 * 
	 * <span class="ja">
   * このコンポーネントに必要なオプション Javascript ファイルの配列。無ければ、null (empty array)
   * 各自のパスは defaultJavascriptPath() と合成されます。
   * 
   * @return このコンポーネントに必要なオプション Javascript ファイルの配列
   * </span>
	 */
	protected NSArray<String> additionalJavascriptFiles() {
		return null;
	}

	/**
	 * <span class="en">
	 * Override and return true for any components to which you would like to
	 * allow page level access.
	 * 
	 * @return true by default
	 * </span>
	 * 
	 * <span class="ja">
   * ページ・レベル・アクセス
   * true はページへのアクセスが可能です
   * 
   * @return デフォルトは true
   * </span>
	 */
	protected boolean isPageAccessAllowed() {
		return true;
	}

	/**
	 * <span class="en">
	 * Override to provide custom security checks. It is not necessary to call
	 * super on this method.
	 * 
	 * @throws SecurityException
	 *             if the security check fails
	 * </span>
	 * 
	 * <span class="ja">
   * カスタム・セキュリティ・チェックを行う場合にはオーバライドするといいのです。
   * スーパーを呼ぶ必要ありません。
   * 
   * @throws SecurityException - セキュリティ・チェックが失敗した場合
   * </span>
	 */
	protected void checkAccess() throws SecurityException {
	}

	/**
	 * <span class="en">
	 * Override to hook into appendToResponse after security checks but before
	 * the super.appendToResponse. It is not necessary to call super on this
	 * method.
	 * 
	 * @param response - the current response
	 * @param context - the current context
	 * </span>
	 * 
	 * <span class="ja">
   * セキュリティ・チェックの後で、super.appendToResponse を呼ぶ前の横取り
   * このフェーズに何がする必要がある場合は、オーバライドするといいのです
   * この中ではスーパーを呼ぶ必要ありません。
   * 
   * @param response - カレント・リスポンス
   * @param context - カレント・コンテクスト
   * </span>
	 */
	protected void preAppendToResponse(WOResponse response, WOContext context) {
	}

	/**
	 * <span class="en">
	 * Override to hook into appendToResponse after super.appendToResponse. It
	 * is not necessary to call super on this method.
	 * 
	 * @param response - the current response
	 * @param context - the current context
	 * </span>
	 * 
	 * <span class="ja">
   * super.appendToResponse を直後の横取り
   * このフェーズに何がする必要がある場合は、オーバライドするといいのです
   * この中ではスーパーを呼ぶ必要ありません。
   * 
   * @param response - カレント・リスポンス
   * @param context - カレント・コンテクスト
   * </span>
	 */
	protected void postAppendToResponse(WOResponse response, WOContext context) {
	}
}
