package er.extensions.foundation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResourceManager;

/**
 * <div class="en">
 * Static Resource Object
 * This Object creates a URL just in Time for your URL with a new Syntax:
 * <pre><code>
 * static://{framework}:{filename}
 * http://{url}
 * https://{url}
 * </code></pre>
 * <h3>Reason 1 : Properties File</h3>
 * <dl>
 * <dt>normal way</dt>
 * <dd>er.xxx.xxx.framework = {framework}<br>
 * er.xxx.xxx.filename = {filename}</dd>
 * <dt>or</dt>
 * <dd>er.xxx.xxx.href = http://{url}</dd>
 * <dt>static resource</dt>
 * <dd>er.xxx.xxx.location = static://{framework}:{filename}</dd>
 * <dt>or</dt>
 * <dd>er.xxx.xxx.location = http://{url}</dd>
 * </dl>
 * <h3>Reason 2 : Bindings</h3>
 * <dl>
 * <dt>normal way</dt>
 * <dd>&lt;wo:xxxx framework="{Framework}" filename="{filename}" ... /&gt;</dd>
 * <dt>static resource</dt>
 * <dd>&lt;wo:xxxx location="static://{framework}:{filename}" ... /&gt;</dd>
 * </dl>
 * <h3>Reason 3 : CMS</h3>
 * Static Resource Object makes it easy to create Objects in CMS Systems.
 * It will also heavily used in SnoWOman and other coming Frameworks.
 * It is easy to write a URL into the Database and retrieve the Link
 * via this Object.
 * 
 * <h3>Reason 4 : D2W</h3>
 * It makes it also easy to write Rules, because sometimes you need only one Rule instead of two.
 * Will be used in the D2W Framework.
 * </div>
 * 
 * <div class="ja">
 * リソース・オブジェクト
 * フレームワークとファイル名をオブジェクト化します
 * </div>
 */
public class ERXStaticResource {
	private static final Logger log = LoggerFactory.getLogger(ERXStaticResource.class);

	//********************************************************************
	//  Constructor
	//********************************************************************

	public ERXStaticResource(WOContext aWOContext, String url) {
		this(url);
		context = aWOContext;
	}

	public ERXStaticResource(String url) {
		if(!ERXStringUtilities.stringIsNullOrEmpty(url)) {

			if((url.startsWith("http://")) || (url.startsWith("https://"))) {
				setHref(url);

			} else {
				if(url.startsWith("static://")) {
					url = url.replace("static://", "");
				}

				int i = url.indexOf(":"); // "framework:fileName" 対応
				if(i > 0) {
					setFramework(url.substring(0, i));
					setFileName(url.substring(i + 1));
				} else {
					setFileName(url);
				}
			}
		}
	}

	public ERXStaticResource(String framework, String fileName) {
		setFramework(framework);
		setFileName(fileName);
	}

	@Override
	public String toString() {
		return "<" + ERXStaticResource.class.getName() + " : href = " + href() + "; framework = " + framework() + "; name = " + fileName() + ">";
	}

	//********************************************************************
	//  Methods
	//********************************************************************

	/**
	 * <div class="en">
	 * Create a Compete URL
	 * </div>
	 * 
	 * <div class="ja">
	 * 完全な URL を作成して戻します。
	 * </div>
	 * 
	 * @return <div class="en">complete URL</div>
	 *         <div class="ja">完全な URL を戻す</div>
	 */
	public String urlForResourceNamed() {
		return urlForResourceNamed(context);
	}

	/**
	 * <div class="en">
	 * Create a Compete URL
	 * </div>
	 * 
	 * <div class="ja">
	 * 完全な URL を作成して戻します。
	 * </div>
	 * 
	 * @param aWOContext <div class="en">WOContext</div>
	 *                   <div class="ja">コンテキスト</div>
	 * @return <div class="en">complete URL</div>
	 *         <div class="ja">完全な URL を戻す</div>
	 */
	public String urlForResourceNamed(WOContext aWOContext) {

		// href ある？
		if(!ERXStringUtilities.stringIsNullOrEmpty(href())) {
			return href();
		}

		String result = "#";
		if(!ERXStringUtilities.stringIsNullOrEmpty(fileName())) {
			WOResourceManager rm = WOApplication.application().resourceManager();
			try {
				result = rm.urlForResourceNamed(fileName(), framework(), null, aWOContext.request());  
			}
			catch (Exception e) {
				log.warn("The Resource Path is not correct: {}", this);
			}
		}
		return result;
	}

	//********************************************************************
	//  Properties
	//********************************************************************

	private void setFramework(String framework) {
		_framework = framework;
	}
	public String framework() {
		return _framework;
	}
	private String _framework = "app";

	private void setFileName(String fileName) {
		_fileName = fileName;
	}
	public String fileName() {
		return _fileName;
	}
	private String _fileName = null;

	private void setHref(String href) {
		_href = href;
	}
	public String href() {
		return _href;
	}
	private String _href = null;

	/** 
	 * <div class="en">Sometimes it is Nice for Binding Reasons to have the Context inside</div>
	 * <div class="ja">バインディングできる為にコンテキストを保存する</div>
	 */
	private WOContext context = null;

	//********************************************************************
	//  Static Helper
	//********************************************************************

	/**
	 * <div class="en">
	 * Create a URL from a staticResourceUrl String
	 * without creating a Class.
	 * </div>
	 * 
	 * <div class="ja">
	 * 完全な URL を作成して戻します。
	 * クラスを作る必要ないバージョン
	 * </div>
	 * 
	 * @param context <div class="en">Context</div>
	 *                <div class="ja">コンテキスト</div>
	 * @param url <div class="en">parameter like a URL</div>
	 *            <div class="ja">URL 形式の引数</div>
	 * @return <div class="en">URL</div>
	 *         <div class="ja">完全な URL を戻す</div>
	 */
	public static String urlForResourceNamed(WOContext context, String url) {

		if(!ERXStringUtilities.stringIsNullOrEmpty(url)) {
			if((url.startsWith("http://")) || (url.startsWith("https://"))) {
				return url;
			} 

			if(url.startsWith("static://")) {
				url = url.replace("static://", "");
			}
			int i = url.indexOf(":"); // "framework:fileName" 対応

			String framework = "app";
			String fileName = null;
			if(i > 0) {
				framework = url.substring(0, i);
				fileName = url.substring(i + 1);
			} else {
				fileName = url;
			}

			if(!ERXStringUtilities.stringIsNullOrEmpty(fileName)) {
				WOResourceManager rm = WOApplication.application().resourceManager();
				try {
					return rm.urlForResourceNamed(fileName, framework, null, context.request());  
				}
				catch (Exception e) {
					log.warn("The Resource Path is not correct: {}", url);
				}
			}

		}
		return "#";
	}

}
