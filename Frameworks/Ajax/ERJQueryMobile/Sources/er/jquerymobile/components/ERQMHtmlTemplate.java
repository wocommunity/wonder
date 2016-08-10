package er.jquerymobile.components;

import java.util.Enumeration;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WODynamicGroup;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.components.conditionals.ERXWOTemplate;
import er.extensions.foundation.ERXHyperlinkResource;
import er.extensions.foundation.ERXProperties;

@SuppressWarnings("serial")
public class ERQMHtmlTemplate extends ERXStatelessComponent {

  //********************************************************************
  //  Constructor
  //********************************************************************

  public ERQMHtmlTemplate(WOContext aContext) {
    super(aContext);
  }

  //********************************************************************
  //  Methods
  //********************************************************************

  public String styleSheetUrl() {
    String uri;

    if(ERXProperties.booleanForKeyWithDefault("er.jquerymobile.css.use.odn", true)) {
      uri = ERXProperties.stringForKey("er.jquerymobile.css.odn.location");
    } else {
      uri = ERXProperties.stringForKey("er.jquerymobile.css.local.location");
    }
    return ERXHyperlinkResource.urlForHyperlinkResource(context(), uri);
  }

  public String jQueryUrl() {
    String uri;

    if(ERXProperties.booleanForKeyWithDefault("er.jquerymobile.jquery.use.odn", true)) {
      uri = ERXProperties.stringForKey("er.jquerymobile.jquery.odn.location");
    } else {
      uri = ERXProperties.stringForKey("er.jquerymobile.jquery.local.location");
    }
    return ERXHyperlinkResource.urlForHyperlinkResource(context(), uri);
  }

  public String javascriptUrl() {
    String uri;

    if(ERXProperties.booleanForKeyWithDefault("er.jquerymobile.javascript.use.odn", true)) {
      uri = ERXProperties.stringForKey("er.jquerymobile.javascript.odn.location");
    } else {
      uri = ERXProperties.stringForKey("er.jquerymobile.javascript.local.location");
    }
    return ERXHyperlinkResource.urlForHyperlinkResource(context(), uri);
  }

  //********************************************************************
  //  コンテント・フローのヘルパー
  //********************************************************************

  /**
   * <span class="ja">
   * 新しいテンプレート方式を使用するか旧方式で実行するかを決定する為のコード
   * 
   * @return true の場合には最新テンプレート方式
   * </span>
   */
  public boolean hastTemplateInComponent() {
    boolean result = false;

    WOElement content =  _childTemplate();
    if (content instanceof WODynamicGroup) {
      WODynamicGroup group = (WODynamicGroup) content;
      for(Enumeration<WOElement> e = group.childrenElements().objectEnumerator(); e.hasMoreElements() && !result ; ) {
        WOElement current = e.nextElement();
        if(current instanceof ERXWOTemplate) {
          result = true;
        }
      }
    } else if (content instanceof ERXWOTemplate) {
      result = true;
    }
    return result;
  }

}
