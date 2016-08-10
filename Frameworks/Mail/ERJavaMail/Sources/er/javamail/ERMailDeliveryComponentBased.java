/*
 ERMailDeliveryComponentBased.java - Camille Troillard - tuscland@mac.com
 */

package er.javamail;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.foundation.NSDictionary;

/**
 * <span class="en">
 * This abstract class is the basis for all WOComponetn based deliverers.
 * </span>
 * 
 * <span class="ja">
 * この抽選クラスは WOComponent のメール対応のスーパー・クラスになります。
 * </span>
 * 
 * @author Camille Troillard <tuscland@mac.com>
 */
public abstract class ERMailDeliveryComponentBased extends ERMailDelivery {
	/** WOComponent used to render the HTML message. */
	protected WOComponent _component;
	protected WOComponent _alternativeComponent;

	/**
	 * <span class="en">
	 * Variable that stores the state of the session. In the case the component was instanciated with
	 * ERMailUtils.instanciatePage, the session may be new and hence, would lack its dictionary properties.
	 * </span>
	 * 
	 * <span class="ja">
	 * セッション情報を保持する変数です。例えば、ERMailUtils.instanciatePage でインスタンス化されているコンポーネント
	 * ではセッションが新しく、必要な情報がないことになります。
	 * </span>
	 */
	protected NSDictionary _sessionDictionary = NSDictionary.EmptyDictionary;

	/**
	 * Sets the WOComponent used to render the HTML message.
	 * 
	 * @deprecated use {@link #setComponent(WOComponent)}
	 */
	@Deprecated
	public void setWOComponentContent(WOComponent component) {
		setComponent(component);
	}

	/** 
	 * <span class="en">
	 * Sets the WOComponent used to render the HTML message. 
	 * </span>
	 * 
	 * <span class="ja">
	 * HTML メッセージに使用される WOComponent をセットします。
	 * </span>
	 */
	public void setComponent(WOComponent component) {
		_component = component;
	}

	public WOComponent component() {
		return _component;
	}
	
	/**
	 * <span class="en">
	 * Sets the alternative view component for rendering a different mime type (text/plain, etc)
	 * </span>
	 * 
	 * <span class="ja">
	 * 他の mime タイプ (text/plain, 等) のレンダリングに使用するコンポーネント
	 * </span>
	 */
	public void setAlternativeComponent(WOComponent alternativeComponent) {
		_alternativeComponent = alternativeComponent;
	}
	
	public WOComponent alternativeComponent() {
		return _alternativeComponent;
	}

	/** Accessor for the sessionDictionary property */
	public NSDictionary sessionDictionary() {
		return _sessionDictionary;
	}

	/** Accessor for the sessionDictionary property */
	public void setSessionDictionary(NSDictionary dict) {
		_sessionDictionary = dict;
	}

	/** 
	 * <span class="en">
	 * Generates the output string used in messages
	 * </span>
	 * 
	 * <span class="ja">
	 * メッセージで使用されている出力結果を生成します
	 * </span>
	 */
	protected String componentContentString() {
		return _componentContentString(component());
	}

	/** 
	 * <span class="en">
	 * Generates the output string used in messages 
	 * </span>
	 * <span class="ja">
	 * メッセージで使用されている出力結果を生成します
	 * </span>
	 */
	protected String alternativeComponentContentString() {
		return _componentContentString(alternativeComponent());
	}

	/** 
	 * <span class="en">
	 * Generates the output string used in messages 
	 * </span>
	 * 
	 * <span class="ja">
	 * メッセージで使用されている出力結果を生成します
	 * </span>
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
