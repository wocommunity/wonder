/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.ERDirectToWeb;
import er.directtoweb.pages.ERD2WPage;
import er.extensions.components.ERXNonSynchronizingComponent;
import er.extensions.eof.ERXConstant;
import er.extensions.validation.ERXExceptionHolder;

/**
 * <div class="en">
 * Base class of many custom components.
 * <p>
 * Has a lot of nifty features including resolving bindings against the rule system and inherits all the value pulling methods from {@link ERXNonSynchronizingComponent}.
 * Subclasses should be able to run stand alone without a D2W context. This is achieved by pulling values first from the bindings, then from the d2wContext and finally from an "extraBindings" binding.
 * </div>
 * 
 * <div class="ja">
 * たくさんのカスタム・コンポーネントのベース・クラスである
 * 
 * ルール・システムへのバインディングや {@link ERXNonSynchronizingComponent} の値バインディング取得機能等の必要な処理をたくさん含みます。
 * 
 * サブクラスは D2W コンテキスト無しでスタンドアロンで実行可能です。最初はコンポーネント・バインディングを優先で取得を試し、
 * だめなら、 d2wContext と後は "extraBindings" バインディングより。
 * 
 * @d2wKey localContext - d2wContext (deprecated)
 * @d2wKey d2wContext - d2wContext
 * @d2wKey key - プロパティ・キー
 * @d2wKey extraBindings - オプション・バインディング
 * @d2wKey propertyKey - プロパティ・キー
 * </div>
 */
public abstract class ERDCustomComponent extends ERXNonSynchronizingComponent implements ERXExceptionHolder {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static interface Keys {
        public static final String key = "key";
        public static final String localContext = "localContext";
        public static final String d2wContext = "d2wContext";
        public static final String extraBindings = "extraBindings";
        public static final String propertyKey = "propertyKey";
    }
    
    /** logging support */
    public final static Logger log = Logger.getLogger(ERDCustomComponent.class);
    
    /** Designated constructor */
    public ERDCustomComponent(WOContext context) {
        super(context);
    }

    /** Holds the {@link D2WContext}. */
    private D2WContext d2wContext;

    /** Holds the current D2W task. */
    private String task;

    /** Holds the property key. */
    private String key;

    /** Holds the extra bindings. */
    protected Object extraBindings;

    //CHECKME ak: who needs this?
    protected static final Integer TRUE = ERXConstant.OneInteger;
    protected static final Integer FALSE = ERXConstant.ZeroInteger;

    /** Sets the {@link D2WContext}. Applies when used inside a D2WCustomComponent.*/
    public void setLocalContext(D2WContext value) {
        setD2wContext(value);
    }

    /** Sets the {@link D2WContext}. Applies when used inside a property key repetition.*/
    public void setD2wContext(D2WContext value) {
        d2wContext = value;
    }

    /** The active {@link D2WContext}. Simply calls to {@link #d2wContext()}*/
    public D2WContext localContext() {
        return d2wContext();
    }

    /** The active {@link D2WContext}.*/
    public D2WContext d2wContext() {
        return d2wContextFromBindings();
    }

    /** 
     * <span class="en">
     * Returns the active d2wContext. If the value was not set via KVC, tries to get the value from the bindings if the component is non-syncing 
     * </span>
     * 
     * <span class="ja">
     * アクティブな d2wContext を戻します。
     * KVC で設定されていなければ、非シンクロナイズ・コンポーネントのバインディングより取得を試し見る
     * </span>
     */
    protected D2WContext d2wContextFromBindings() {
        if (d2wContext == null && !synchronizesVariablesWithBindings()) {
            d2wContext = (D2WContext)super.valueForBinding(Keys.localContext);
            if(d2wContext == null) {
                d2wContext = (D2WContext)super.valueForBinding(Keys.d2wContext);
            }
        }
        return d2wContext;
    }

    /**
     * Gets the current D2W task.
     */
    public String task() {
        if (task == null) {
            task = (String)valueForBinding("task");
        }
        return task;
    }
    
    public boolean taskIsEdit() { return "edit".equals(task()); }
    public boolean taskIsInspect() { return "inspect".equals(task()); }
    public boolean taskIsList() { return "list".equals(task()); }

    /** 
     * <span class="en">Validation Support. Passes errors to the parent. </span>
     * <span class="ja">Validation Support. エラーを親コンポーネントに渡す</span>
     */
    @Override
    public void validationFailedWithException (Throwable e, Object value, String keyPath) {
        parent().validationFailedWithException(e,value,keyPath);
    }

    /** 
     * <span class="en">Implementation of the {@link ERXExceptionHolder} interface. Clears exceptions in the parent if possible.</span>
     * <span class="ja">{@link ERXExceptionHolder} インタフェース実装。可能であれば、親のエラーをクリアします。</span>
     */
    public void clearValidationFailed() {
        // Since this component can be used stand alone, we might not necessarily
        // have an exception holder as our parent --> testing
        if (parent() instanceof ERXExceptionHolder)
            ((ERXExceptionHolder)parent()).clearValidationFailed();
    }

    // CHECKME ak who needs this?
    public Integer integerBooleanForBinding(String binding) {
        return booleanValueForBinding(binding) ? ERDCustomComponent.TRUE : ERDCustomComponent.FALSE;
    }

    /** 
     * <span class="en">
     * Checks if the binding can be pulled. If the component is synching, throws an Exception. Otherwise checks the superclass and if the value for the binding is not null.
     * </span>
     * 
     * <span class="ja">
     * バインディングが取得可能かどうかをチェックします。シンクロナイズ・コンポーネントの場合にはエラーを発行します。
     * そうでなければ、スーパークラスをバインディングが非 null であるようにチェックします
     * </span>
     */
    @Override
    public boolean hasBinding(String binding) {
        // FIXME:  Turn this check off in production
        if (synchronizesVariablesWithBindings()) {
            throw new IllegalStateException("HasBinding being used in an object of class " + getClass().getName() + " that synchronizesVariablesWithBindings == true");
        }
        return (super.hasBinding(binding) || valueForBinding(binding) != null);
    }

    /** Utility to dump some debug info about this component and its parent */
    protected void logDebugInfo() {
        if (log.isDebugEnabled()) {
            log.debug("***** ERDCustomComponent: this: " + getClass().getName());
            log.debug("***** ERDCustomComponent: parent(): + (" + ((parent() == null) ? "null" : parent().getClass().getName()) + ")");
            log.debug("                      " + parent());
            log.debug("***** ERDCustomComponent: parent() instanceof ERDCustomComponent == " + (parent() instanceof ERDCustomComponent));
            log.debug("***** ERDCustomComponent: parent() instanceof D2WCustomComponentWithArgs == " + (parent() instanceof ERD2WCustomComponentWithArgs));
            log.debug("***** ERDCustomComponent: parent() instanceof D2WStatelessCustomComponentWithArgs == " + (parent() instanceof ERD2WStatelessCustomComponentWithArgs));
            log.debug("***** ERDCustomComponent: parent() instanceof D2WCustomQueryComponentWithArgs == " + (parent() instanceof ERDCustomQueryComponentWithArgs));
        }
    }

    /** 
     * <span class="en">Utility to pull the value from the components parent, if the parent is a D2W wrapper component.</span>
     * <span class="ja">親コンポーネントが D2W ラパー・コンポーネントの場合のバインディング取得ユーティリティ</span>
     */
    protected Object parentValueForBinding(String binding) {
        WOComponent parent = parent();
        if (parent instanceof ERDCustomComponent ||
            parent instanceof ERD2WCustomComponentWithArgs ||
            parent instanceof ERD2WStatelessCustomComponentWithArgs) {
            log.debug("inside the parent instanceof branch");
            // this will eventually bubble up to a D2WCustomComponentWithArgs, where it will (depending on the actual binding)
            // go to the d2wContext
            return parent.valueForBinding(binding);
        }
        return null;
    }

    /** 
     * <span class="en">Utility to pull the value from the components actual bindings. </span>
     * <span class="ja">コンポーネントのバインディングから取得するユーティリティ</span>
     */
    protected Object originalValueForBinding(String binding) {
        return super.valueForBinding(binding);
    }

    /** 
     * <span class="en">Utility to pull the value from the {@link D2WContext}. </span>
     * <span class="ja">{@link D2WContext} から値を取得するユーティリティ</span>
     */
    protected Object d2wContextValueForBinding(String binding) {
        return d2wContextFromBindings().valueForKey(binding);
    }

    /** Utility to pull the value from the extra bindings if supplied. */
    protected Object extraBindingsValueForBinding(String binding) {
        if(extraBindings() instanceof NSDictionary)
            return ((NSDictionary)extraBindings()).objectForKey(binding);
        return null;
    }

    /**
     * <span class="en">
     * Fetches an object from the bindings.
     * Tries the actual supplied bindings, the supplied d2wContext, the parent and finally the extra bindings dictionary.
     * </span>
     * 
     * <span class="ja">
     * バインディングを使って、オブジェクトをフェッチする
     * バインディングを次の順で取得を試す：コンポーネント、d2wContext、親コンポーネント、オプション・バインディング・ディクショナリー
     * </span>
     */
    @Override
    public Object valueForBinding(String binding) {
        Object value=null;
        logDebugInfo();
        if (super.hasBinding(binding)) {
        		if (log.isDebugEnabled())
        			log.debug("super.hasBinding(binding) == true for binding "+binding);
            value = originalValueForBinding(binding);
        } else if(d2wContextFromBindings() != null) {
    			if (log.isDebugEnabled())
    				log.debug("has d2wContext == true for binding "+binding);
            value = d2wContextValueForBinding(binding);
        } else {
            value = parentValueForBinding(binding);
        }
        if (value == null && binding != null && extraBindings() != null) {
    			if (log.isDebugEnabled())
    				log.debug("inside the extraBindings branch for binding "+binding);
            value = extraBindingsValueForBinding(binding);
        }
        if (log.isDebugEnabled()) {
            if (value != null)
                log.debug("returning " + value.getClass().getName() + ": " + value+" for binding "+binding);
            else
                log.debug("returning value: null for binding "+binding);
        }
        return value;
    }

    /** Used by stateful but non-synching subclasses */
    @Override
    public void resetCachedBindingsInStatefulComponent() {
        super.resetCachedBindingsInStatefulComponent();
        extraBindings = null;
        key = null;
        d2wContext = null;
        task = null;
    }

    /** Used by stateless subclasses. */
    @Override
    public void reset() {
        super.reset();
        extraBindings = null;
        key = null;
        d2wContext = null;
        task = null;
    }

    /** Sets the extra bindings. */
    public void setExtraBindings(Object value) { extraBindings = value; }

    /** Extra bindings supplied to the component. If this is a dictionary, it will be used for additional bindings.*/
    public Object extraBindings() {
        if (extraBindings == null && !synchronizesVariablesWithBindings())
            extraBindings = super.valueForBinding(Keys.extraBindings);
        return extraBindings;
    }

    /** Sets the property key. */
    public void setKey(String newKey) { key=newKey; }

    /** The active property key. */
    public String key() {
      //FIXME : バグフィックス：複数のカスタム・コンポーネントを複数のタブに設定すると正しく設定されず。
      //if(synchronizesVariablesWithBindings()) {
      //  key = null;
      //}

      if(!synchronizesVariablesWithBindings()) {
            if (key==null) {
                key=(String)super.valueForBinding(Keys.key);
            }
        }
        if (key==null && d2wContext() != null) {
            key=(String)d2wContext().valueForKey(Keys.propertyKey);
        }
        return key;
    }

    /** Overridden from superclass to turn on component synching, which is the default. */
    @Override
    public boolean synchronizesVariablesWithBindings() { return true; } // CHECKME why then does this class subclass ERXNonSynchronizingComponent?

    /** Is D2W debugging enabled. */
    public boolean d2wDebuggingEnabled() {
        return ERDirectToWeb.d2wDebuggingEnabled(session());
    }

    /** 
     * <span class="en">Should the component name be shown. </span>
     * <span class="ja">コンポーネント名を表示する？</span>
     */
    public boolean d2wComponentNameDebuggingEnabled() {
        return ERDirectToWeb.d2wComponentNameDebuggingEnabled(session());
    }

    /** 
     * <span class="en">Should the property keys be shown.</span>
     * <span class="ja">プロパティ・キーを表示する？</span>
     */
    public boolean d2wPropertyKeyDebuggingEnabled() {
        return ERDirectToWeb.d2wPropertyKeyDebuggingEnabled(session());
    }

    /**
     * <span class="en">
     * Finds the containing D2WPage, if possible.  There are certain situations when having a 
     * reference to the containing D2W page is useful, e.g., when needing to use the userInfo 
     * dictionary of {@link ERD2WPage} to pass information between subcomponents.
     * 
     * @return the containing D2WPage
     * </span>
     * 
     * <span class="ja">
     * 可能であれば、D2WPage の親コンポーネントを戻します。
     * 場合によって、親 D2W ページへのアクセスは必要です。
     * ユーザ情報ディクショナリーへのアクセス等でサブコンポーネントの情報交換が可能です。
     * 
     * @return 親 D2WPage
     * </span>
     */
    public D2WPage d2wPage() {
        // Can't just use context().page(), because the d2wPage isn't necessarily the top-level
        // component.
        WOComponent component = this;

        do {
            component = component.parent();
        } while( component != null && !(component instanceof D2WPage) );

        return (D2WPage)component;
    }

    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        if(!ERDirectToWeb.shouldRaiseException(false)) {
            // in the case where we are non-synchronizing but not stateless, make sure we pull again
            if (!synchronizesVariablesWithBindings() && !isStateless()) {
                reset();
            }
            super.appendToResponse(r,c);
        } else {
            try {
                // in the case where we are non-synchronizing but not stateless, make sure we pull again
                if (!synchronizesVariablesWithBindings() && !isStateless()) {
                    reset();
                }
                super.appendToResponse(r,c);
            } catch(Exception ex) {
                ERDirectToWeb.reportException(ex, d2wContext());
            }
        }
    }
}
