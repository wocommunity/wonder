package ognl.helperfunction;

import java.util.Enumeration;

import ognl.webobjects.WOOgnl;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODeclaration;
import com.webobjects.appserver._private.WODeclarationFormatException;
import com.webobjects.appserver._private.WODeclarationParser;
import com.webobjects.appserver._private.WOHTMLCommentString;
import com.webobjects.appserver._private.WOHTMLFormatException;
import com.webobjects.appserver._private.WOHTMLWebObjectTag;
import com.webobjects.appserver._private.WOKeyValueAssociation;
import com.webobjects.appserver._private.WOParser;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public class WOHelperFunctionHTMLTemplateParser extends WOParser implements WOHelperFunctionHTMLParserDelegate {
  public static Logger log = Logger.getLogger(WOHelperFunctionHTMLTemplateParser.class);

  protected WOHTMLWebObjectTag _currentWebObjectTag;
  protected NSDictionary _declarations;

  public WOHelperFunctionHTMLTemplateParser(String htmlString, String declarationString, NSArray languages) {
    super(htmlString, declarationString, languages);
    _declarations = null;
    _currentWebObjectTag = new WOHTMLWebObjectTag();
  }

  public void didParseOpeningWebObjectTag(String s, WOHelperFunctionHTMLParser htmlParser) throws WOHTMLFormatException {
    _currentWebObjectTag = new WOHTMLWebObjectTag(s, _currentWebObjectTag);
    if (log.isDebugEnabled()) {
      log.debug("inserted WebObject with Name '" + _currentWebObjectTag.name() + "'.");
    }
  }

  public void didParseClosingWebObjectTag(String s, WOHelperFunctionHTMLParser htmlParser) throws WODeclarationFormatException, WOHTMLFormatException, ClassNotFoundException {
    WOHTMLWebObjectTag webobjectTag = _currentWebObjectTag.parentTag();
    if (_currentWebObjectTag == null || webobjectTag == null) {
      throw new WOHTMLFormatException("<" + getClass().getName() + "> Unbalanced WebObject tags. Either there is an extra closing </WEBOBJECT> tag in the html template, or one of the opening <WEBOBJECT ...> tag has a typo (extra spaces between a < sign and a WEBOBJECT tag ?).");
    }
    WOElement element = _currentWebObjectTag.dynamicElement(_declarations, _languages);
    _currentWebObjectTag = webobjectTag;
    _currentWebObjectTag.addChildElement(element);
  }

  public void didParseComment(String comment, WOHelperFunctionHTMLParser htmlParser) {
    WOHTMLCommentString wohtmlcommentstring = new WOHTMLCommentString(comment);
    _currentWebObjectTag.addChildElement(wohtmlcommentstring);
  }

  public void didParseText(String text, WOHelperFunctionHTMLParser htmlParser) {
    _currentWebObjectTag.addChildElement(text);
  }

  private void parseDeclarations() throws WODeclarationFormatException {
    if (_declarations == null && _declarationString != null) {
      _declarations = WODeclarationParser.declarationsWithString(_declarationString);
    }
  }

  private WOElement parseHTML() throws WOHTMLFormatException, WODeclarationFormatException, ClassNotFoundException {
    WOElement currentWebObjectTemplate = null;
    if (_HTMLString != null && _declarations != null) {
      WOHelperFunctionHTMLParser htmlParser = new WOHelperFunctionHTMLParser(this, _HTMLString);
      htmlParser.parseHTML();
      String webobjectTagName = _currentWebObjectTag.name();
      if (webobjectTagName != null) {
        throw new WOHTMLFormatException("There is an unbalanced WebObjects tag named '" + webobjectTagName + "'.");
      }
      currentWebObjectTemplate = _currentWebObjectTag.template();
    }
    return currentWebObjectTemplate;
  }

  public WOElement parse() throws WODeclarationFormatException, WOHTMLFormatException, ClassNotFoundException {
    parseDeclarations();
    for (Enumeration e = declarations().objectEnumerator(); e.hasMoreElements();) {
      WODeclaration declaration = (WODeclaration) e.nextElement();
      NSMutableDictionary associations = (NSMutableDictionary) declaration.associations();
      Enumeration bindingNameEnum = associations.keyEnumerator();
      while (bindingNameEnum.hasMoreElements()) {
        String bindingName = (String) bindingNameEnum.nextElement();
        WOAssociation association = (WOAssociation) associations.valueForKey(bindingName);
        WOAssociation helperAssociation = parserHelperAssociation(association);
        if (helperAssociation != association) {
          associations.setObjectForKey(helperAssociation, bindingName);
        }
      }
      // This will replace constant associations with ognl associations when needed.
      WOOgnl.factory().convertOgnlConstantAssociations(associations);
    }
    parseDeclarations();
    WOElement woelement = parseHTML();
    return woelement;
  }

  protected WOAssociation parserHelperAssociation(WOAssociation originalAssociation) {
    WOAssociation association = originalAssociation;
    String originalKeyPath = null;
    if (association instanceof WOKeyValueAssociation) {
      WOKeyValueAssociation kvAssociation = (WOKeyValueAssociation) association;
      originalKeyPath = kvAssociation.keyPath();
    }
    //    else if (association instanceof WOConstantValueAssociation) {
    //      WOConstantValueAssociation constantAssociation = (WOConstantValueAssociation) association;
    //      Object constantValue = constantAssociation.valueInComponent(null);
    //      if (constantValue instanceof String) {
    //        originalKeyPath = (String) constantValue;
    //      }
    //    }

    if (originalKeyPath != null) {
      int pipeIndex = originalKeyPath.indexOf('|');
      if (pipeIndex != -1) {
        String targetKeyPath = originalKeyPath.substring(0, pipeIndex).trim();
        String frameworkName = WOHelperFunctionRegistry.APP_FRAMEWORK_NAME;
        String helperFunctionName = originalKeyPath.substring(pipeIndex + 1).trim();
        String otherParams = null;
        int openParenIndex = helperFunctionName.indexOf('(');
        if (openParenIndex != -1) {
          int closeParenIndex = helperFunctionName.indexOf(')', openParenIndex + 1);
          otherParams = helperFunctionName.substring(openParenIndex + 1, closeParenIndex);
          helperFunctionName = helperFunctionName.substring(0, openParenIndex);
        }
        int helperFunctionDotIndex = helperFunctionName.indexOf('.');
        if (helperFunctionDotIndex != -1) {
          frameworkName = helperFunctionName.substring(0, helperFunctionDotIndex);
          helperFunctionName = helperFunctionName.substring(helperFunctionDotIndex + 1);
        }
        StringBuffer ognlKeyPath = new StringBuffer();
        ognlKeyPath.append("~");
        ognlKeyPath.append("@" + WOHelperFunctionRegistry.class.getName() + "@registry()._helperInstanceForFrameworkNamed(");
        ognlKeyPath.append(targetKeyPath);
        ognlKeyPath.append(", \"");
        ognlKeyPath.append(frameworkName);
        ognlKeyPath.append("\").");
        ognlKeyPath.append(helperFunctionName);
        ognlKeyPath.append("(");
        ognlKeyPath.append(targetKeyPath);
        if (otherParams != null) {
          ognlKeyPath.append(",");
          ognlKeyPath.append(otherParams);
        }
        ognlKeyPath.append(")");
        if (log.isDebugEnabled()) {
        	log.debug("Converted " + originalKeyPath + " into " + ognlKeyPath);
        }
        association = new WOConstantValueAssociation(ognlKeyPath.toString());
      }
    }
    return association;
  }

  public NSDictionary declarations() {
    return _declarations;
  }

  public void setDeclarations(NSDictionary value) {
    _declarations = value;
  }

  public String declarationString() {
    return _declarationString;
  }

  public void setDeclarationString(String value) {
    _declarationString = value;
  }
}
