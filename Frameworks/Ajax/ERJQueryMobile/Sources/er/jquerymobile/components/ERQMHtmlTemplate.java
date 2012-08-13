package er.jquerymobile.components;

import org.apache.log4j.Logger;


import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.foundation.ERXHyperlinkResource;
import er.extensions.foundation.ERXProperties;

@SuppressWarnings("serial")
public class ERQMHtmlTemplate extends ERXStatelessComponent {

  protected static final Logger log = Logger.getLogger(ERQMHtmlTemplate.class);

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

}
