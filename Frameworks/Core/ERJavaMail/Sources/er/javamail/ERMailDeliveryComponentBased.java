/*
 ERMailDeliveryComponentBased.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.foundation.NSDictionary;

/**
 * <div class="en">
 * This abstract class is the basis for all WOComponetn based deliverers.
 * </div>
 * 
 * <div class="ja">
 * この抽選クラスは WOComponent のメール対応のスーパー・クラスになります。
 * </div>
 * 
 * @author Camille Troillard &lt;tuscland@mac.com&gt;
 */
public abstract class ERMailDeliveryComponentBased extends ERMailDelivery {
	/** WOComponent used to render the HTML message. */
	protected WOComponent _component;
	protected WOComponent _alternativeComponent;

	/**
	 * <div class="en">
	 * Variable that stores the state of the session. In the case the component was instanciated with
	 * ERMailUtils.instanciatePage, the session may be new and hence, would lack its dictionary properties.
	 * </div>
	 * 
	 * <div class="ja">
	 * セッション情報を保持する変数です。例えば、ERMailUtils.instanciatePage でインスタンス化されているコンポーネント
	 * ではセッションが新しく、必要な情報がないことになります。
	 * </div>
	 */
	protected NSDictionary<String, Object> _sessionDictionary = NSDictionary.emptyDictionary();

	/** 
	 * <div class="en">
	 * Sets the WOComponent used to render the HTML message. 
	 * </div>
	 * 
	 * <div class="ja">
	 * HTML メッセージに使用される WOComponent をセットします。
	 * </div>
	 */
	public void setComponent(WOComponent component) {
		_component = component;
	}

	public WOComponent component() {
		return _component;
	}
	
	/**
	 * <div class="en">
	 * Sets the alternative view component for rendering a different mime type (text/plain, etc)
	 * </div>
	 * 
	 * <div class="ja">
	 * 他の mime タイプ (text/plain, 等) のレンダリングに使用するコンポーネント
	 * </div>
	 */
	public void setAlternativeComponent(WOComponent alternativeComponent) {
		_alternativeComponent = alternativeComponent;
	}
	
	public WOComponent alternativeComponent() {
		return _alternativeComponent;
	}

	/** Accessor for the sessionDictionary property */
	public NSDictionary<String, Object> sessionDictionary() {
		return _sessionDictionary;
	}

	/** Accessor for the sessionDictionary property */
	public void setSessionDictionary(NSDictionary<String, Object> dict) {
		_sessionDictionary = dict;
	}

	/** 
	 * <div class="en">
	 * Generates the output string used in messages
	 * </div>
	 * 
	 * <div class="ja">
	 * メッセージで使用されている出力結果を生成します
	 * </div>
	 */
	protected String componentContentString() {
		return _componentContentString(component());
	}

	/** 
	 * <div class="en">
	 * Generates the output string used in messages 
	 * </div>
	 * <div class="ja">
	 * メッセージで使用されている出力結果を生成します
	 * </div>
	 */
	protected String alternativeComponentContentString() {
		return _componentContentString(alternativeComponent());
	}

	/** 
	 * <div class="en">
	 * Generates the output string used in messages 
	 * </div>
	 * 
	 * <div class="ja">
	 * メッセージで使用されている出力結果を生成します
	 * </div>
	 */
	protected String _componentContentString(WOComponent component) {
		String contentString = null;
		if (component != null) {
			WOContext context = component.context();

			// CHECKME: It's probably not a good idea to do this here
			// since the context could also have been generating relative URLs
			// unless the context is created from scratch
			context.generateCompleteURLs();
			WOMessage response = component.generateResponse();
			contentString = response.contentString();
		}
		return contentString;
	}

}
