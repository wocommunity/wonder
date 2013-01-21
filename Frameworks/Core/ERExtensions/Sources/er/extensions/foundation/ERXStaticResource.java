package er.extensions.foundation;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResourceManager;

/**
 * <span class="en">
 * Static Resource Object
 * This Object creates a URL just in Time for your URL with a new Syntax :
 * 
 * static://{framework}:{filename}
 * http://{url}
 * https://{url}
 * 
 * Reason 1 : Properties File
 * normal way
 *    er.xxx.xxx.framework = {framework}
 *    er.xxx.xxx.filename = {filename}
 * or
 *    er.xxx.xxx.href = http://{url}
 *       
 * static resource
 *    er.xxx.xxx.location = static://{framework}:{filename}
 * or 
 *    er.xxx.xxx.location = http://{url}
 * 
 * Reason 2 : Bindings
 * normal way
 *    <wo:xxxx framework="{Framework}" filename="{filename}" ... />
 * 
 * static resource
 *    <wo:xxxx location="static://{framework}:{filename}" ... />
 * 
 * Reason 3 : CMS
 * Static Resource Object makes it easy to create Objects in CMS Systems.
 * It will also heavily used in SnoWOman and other coming Frameworks.
 * It is easy to write a URL into the Database and retrieve the Link
 * via this Object.
 * 
 * Reason 4 : D2W
 * It makes it also easy to write Rules, because sometimes you need only one Rule insteed of two.
 * Will be used in the D2W Framework.
 * 
 * </span>
 * 
 * <span class="ja">
 * リソース・オブジェクト
 * フレームワークとファイル名をオブジェクト化します
 * </span>
 */
public class ERXStaticResource {

	protected static final Logger log = Logger.getLogger(ERXStaticResource.class);

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
	 * <span class="en">
	 * Create a Compete URL
	 * 
	 * @return complete URL
	 * </span>
	 * 
	 * <span class="ja">
	 * 完全な URL を作成して戻します。
	 * 
	 * @return 完全な URL を戻す
	 * </span>
	 */
	@SuppressWarnings("javadoc")
	public String urlForResourceNamed() {
		return urlForResourceNamed(context);
	}

	/**
	 * <span class="en">
	 * Create a Compete URL
	 * 
	 * @param aWOContext - WOContext
	 * 
	 * @return complete URL
	 * </span>
	 * 
	 * <span class="ja">
	 * 完全な URL を作成して戻します。
	 * 
	 * @param aWOContext - コンテキスト
	 * 
	 * @return 完全な URL を戻す
	 * </span>
	 */
	@SuppressWarnings("javadoc")
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
				log.warn("The Resource Path is not correct : " + this);
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
	 * <span class="en">Sometimes it is Nice for Binding Reasons to have the Context inside</span>
	 * <span class="ja">バインディングできる為にコンテキストを保存する</span>
	 */
	private WOContext context = null;

	//********************************************************************
	//  Static Helper
	//********************************************************************

	/**
	 * <span class="en">
	 * Create a URL from a staticResourceUrl String
	 * without creating a Class.
	 * 
	 * @param context - Context
	 * @param url - parameter like a URL
	 * 
	 * @return URL
	 * </span>
	 * 
	 * <span class="ja">
	 * 完全な URL を作成して戻します。
	 * クラスを作る必要ないバージョン
	 * 
	 * @param context - コンテキスト
	 * @param url - URL 形式の引数
	 * 
	 * @return 完全な URL を戻す
	 * </span>
	 */
	@SuppressWarnings("javadoc")
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
					log.warn("The Resource Path is not correct : " + url);
				}
			}

		}
		return "#";
	}

}
