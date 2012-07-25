package er.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2WStatelessComponent;

import er.extensions.components.ERXClickToOpenSupport;

/**
 * <span class="ja">D2W ステートレス基本コンポーネント</span>
 */
public class ERD2WStatelessComponent extends D2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WStatelessComponent(WOContext aContext) {
        super(aContext);
    }
    
    /**
     * <span class="en">
     * Returns whether or not click-to-open should be enabled for this component.  By
     * default this returns ERXClickToOpenSupport.isEnabled().
     * 
     * @param response the response
     * @param context the context
     * 
     * @return whether or not click-to-open is enabled for this component
     * </span>
     * 
     * <span class="ja">
     * このコンポーネントの click-to-open を許可するかどうかを戻します。
     * デフォルトでは ERXClickToOpenSupport.isEnabled() を戻します。
     * 
     * @param response - レスポンス
     * @param context - コンテクスト
     * 
     * @return このコンポーネントの click-to-open を許可するかどうかを戻します。
     * </span>
     */
    public boolean clickToOpenEnabled(WOResponse response, WOContext context) {
        return ERXClickToOpenSupport.isEnabled();
    }

    /**
     * <span class="en">Adds support for ClickToOpen (TM).</span>
     * <span class="ja">ClickToOpen (TM) のサポートを追加します。</span>
     */
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        boolean clickToOpenEnabled = clickToOpenEnabled(response, context); 
        ERXClickToOpenSupport.preProcessResponse(response, context, clickToOpenEnabled);
        super.appendToResponse(response, context);
        ERXClickToOpenSupport.postProcessResponse(getClass(), response, context, clickToOpenEnabled);
    }
    
}
