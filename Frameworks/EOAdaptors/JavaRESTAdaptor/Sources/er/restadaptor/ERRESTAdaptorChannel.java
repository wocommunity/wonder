package er.restadaptor;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOStoredProcedure;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.foundation.ERXMutableURL;
import er.extensions.localization.ERXLocalizer;

public class ERRESTAdaptorChannel extends EOAdaptorChannel {
  private static final NSTimestampFormatter restDateFormat = new NSTimestampFormatter("%Y-%m-%dT%H:%M:%SZ");

  private NSArray<EOAttribute> _attributes;
  private NSMutableArray<NSMutableDictionary<String, Object>> _fetchedRows;
  private int _fetchIndex;
  private boolean _open;

  public ERRESTAdaptorChannel(ERRESTAdaptorContext context) {
    super(context);
    _fetchIndex = -1;
  }

  public ERRESTAdaptorContext context() {
    return (ERRESTAdaptorContext) _context;
  }

  @Override
  public NSArray<EOAttribute> attributesToFetch() {
    return _attributes;
  }

  @Override
  public void cancelFetch() {
    _fetchedRows = null;
    _fetchIndex = -1;
  }

  @Override
  public void closeChannel() {
    _open = false;
  }

  @Override
  public NSArray describeResults() {
    return _attributes;
  }

  @Override
  public NSArray describeTableNames() {
    return NSArray.EmptyArray;
  }

  @Override
  public EOModel describeModelWithTableNames(NSArray anArray) {
    return null;
  }

  @Override
  public void evaluateExpression(EOSQLExpression anExpression) {
    throw new UnsupportedOperationException("ERRESTAdaptorChannel.evaluateExpression");
  }

  @Override
  public void executeStoredProcedure(EOStoredProcedure aStoredProcedure, NSDictionary someValues) {
    throw new UnsupportedOperationException("ERRESTAdaptorChannel.executeStoredProcedure");
  }

  @Override
  public NSMutableDictionary fetchRow() {
    NSMutableDictionary row = null;
    if (_fetchedRows != null && _fetchIndex < _fetchedRows.count()) {
      row = _fetchedRows.objectAtIndex(_fetchIndex++);
    }
    return row;
  }

  @Override
  public boolean isFetchInProgress() {
    return _fetchedRows != null && _fetchIndex < _fetchedRows.count();
  }

  @Override
  public boolean isOpen() {
    return _open;
  }

  @Override
  public void openChannel() {
    if (!_open) {
      _open = true;
    }
  }

  @Override
  public NSDictionary returnValuesForLastStoredProcedureInvocation() {
    throw new UnsupportedOperationException("ERRESTAdaptorChannel.returnValuesForLastStoredProcedureInvocation");
  }

  public NSArray<String> urlPrefixes(EOEntity entity) {
    String urlPrefix = null;
    String externalName = entity.externalName();
    if (externalName != null) {
      String[] externalNames = externalName.split(",");
      if (externalNames.length > 0 && externalNames[0].contains("/")) {
        urlPrefix = externalNames[0];
      }
    }

    if (urlPrefix == null) {
      urlPrefix = "/" + pluralName(entity);
    }

    return new NSArray<String>(urlPrefix.split("\\|"));
  }

  public String pluralName(EOEntity entity) {
    String pluralName = null;

    String externalName = entity.externalName();
    if (externalName != null) {
      String[] externalNames = externalName.split(",");
      if (externalNames.length > 0) {
        if (externalNames[0].contains("/")) {
          if (externalNames.length > 2) {
            pluralName = externalNames[2];
          }
        }
        else if (externalNames.length > 1) {
          pluralName = externalNames[1];
        }
      }
    }

    if (pluralName == null) {
      pluralName = ERXLocalizer.defaultLocalizer().plurifiedString(singularName(entity), 2);
    }

    return pluralName;
  }

  public String singularName(EOEntity entity) {
    String singularName = null;

    String externalName = entity.externalName();
    if (externalName != null) {
      String[] externalNames = externalName.split(",");
      if (externalNames.length > 0) {
        if (externalNames[0].contains("/")) {
          if (externalNames.length > 1) {
            singularName = externalNames[1];
          }
        }
        else {
          singularName = externalNames[0];
        }
      }
    }

    if (singularName == null) {
      singularName = entity.name();
    }

    return singularName;
  }

  public String textValue(Element element) {
    NodeList children = element.getChildNodes();
    String textValue = null;
    for (int i = 0; i < children.getLength(); i++) {
      String value = children.item(i).getNodeValue();
      if (value != null) {
        value = value.trim();
        textValue = value;
        break;
      }
    }
    return textValue;
  }

  public Object convertValue(String strValue, EOAttribute attribute) {
    return strValue;
  }

  @SuppressWarnings( { "cast", "unchecked" })
  public String urlForQualifier(EOEntity entity, EOQualifier qualifier, NSMutableDictionary<String, Object> attributesFromQualifier) {
    StringBuffer urlBuffer = new StringBuffer();

    NSDictionary connectionDictionary = adaptorContext().adaptor().connectionDictionary();
    String url = (String) connectionDictionary.objectForKey("URL");
    if (url == null || url.length() == 0) {
      throw new IllegalArgumentException("There is no URL specified for the connection dictionary " + connectionDictionary + ".");
    }

    if (url.endsWith("/")) {
      urlBuffer.append(url.substring(0, url.length() - 1));
    }
    else {
      urlBuffer.append(url);
    }

    NSArray<String> urlPrefixes = urlPrefixes(entity);

    if (urlPrefixes.count() == 1) {
      urlBuffer.append(urlPrefixes.objectAtIndex(0));
    }
    else {
      String bestUrlPrefix = null;
      int bestMatchCount = 0;

      NSSet<String> qualifierKeys = (NSSet<String>) qualifier.allQualifierKeys();
      for (String urlPrefix : urlPrefixes) {
        NSSet<String> urlVariableNames = variableNamesForUrl(urlPrefix);
        if (urlVariableNames.count() >= bestMatchCount && urlVariableNames.isSubsetOfSet(qualifierKeys)) {
          bestUrlPrefix = urlPrefix;
          bestMatchCount = urlVariableNames.count();
        }
      }

      if (bestUrlPrefix == null) {
        throw new IllegalArgumentException("The qualifier " + qualifier + " was insufficient to fetch " + entity.name() + " objects with one of the URLs " + urlPrefixes + ".");
      }

      urlBuffer.append(bestUrlPrefix);
    }

    if (qualifier != null) {
      processQualifier(entity, qualifier, urlBuffer, attributesFromQualifier);
    }

    urlBuffer.append(".xml");

    System.out.println("ERRESTAdaptorChannel.urlForQualifier: " + urlBuffer);

    return urlBuffer.toString();
  }

  protected NSSet<String> variableNamesForUrl(String url) {
    NSMutableSet<String> variableNames = new NSMutableSet<String>();
    Matcher matcher = Pattern.compile("\\[([^]]+)\\]").matcher(url);
    while (matcher.find()) {
      variableNames.addObject(matcher.group(1));
    }
    return variableNames;
  }

  @SuppressWarnings( { "cast", "unchecked" })
  protected void processQualifier(EOEntity entity, EOQualifier qualifier, StringBuffer urlBuffer, NSMutableDictionary<String, Object> attributesFromQualifier) {
    if (qualifier instanceof EOKeyValueQualifier) {
      EOKeyValueQualifier eokvq = (EOKeyValueQualifier) qualifier;
      String key = eokvq.key();
      Object value = eokvq.value();
      EOAttribute keyAttribute = entity.attributeNamed(key);
      if (entity.primaryKeyAttributes().containsObject(keyAttribute)) {
        urlBuffer.append("/");
        urlBuffer.append(value);
      }
      else {
        String var = "[" + key + "]";
        int varIndex = urlBuffer.indexOf(var);
        if (varIndex != -1) {
          urlBuffer.replace(varIndex, varIndex + var.length(), value.toString());
        }
      }

      if (value != null) {
        attributesFromQualifier.setObjectForKey(value, key);
      }
    }
    else if (qualifier instanceof EOAndQualifier) {
      NSArray<EOQualifier> childQualifiers = (NSArray<EOQualifier>) ((EOAndQualifier) qualifier).qualifiers();
      for (EOQualifier childQualifier : childQualifiers) {
        processQualifier(entity, childQualifier, urlBuffer, attributesFromQualifier);
      }
    }
    else if (qualifier instanceof EOOrQualifier) {
      NSArray<EOQualifier> childQualifiers = (NSArray<EOQualifier>) ((EOOrQualifier) qualifier).qualifiers();
      if (childQualifiers.count() == 1) {
        processQualifier(entity, childQualifiers.objectAtIndex(0), urlBuffer, attributesFromQualifier);
      }
    }
  }

  @Override
  public void selectAttributes(NSArray attributesToFetch, EOFetchSpecification fetchSpecification, boolean shouldLock, EOEntity entity) {
    if (entity == null) {
      throw new IllegalArgumentException("null entity.");
    }
    if (attributesToFetch == null) {
      throw new IllegalArgumentException("null attributes.");
    }
    setAttributesToFetch(attributesToFetch);

    try {
      _fetchIndex = 0;

      NSMutableDictionary<String, Object> attributesFromQualifier = new NSMutableDictionary<String, Object>();
      ERXMutableURL url = new ERXMutableURL(urlForQualifier(entity, fetchSpecification.qualifier(), attributesFromQualifier));
      InputStream urlStream = new BufferedInputStream(url.toURL().openStream());
      Document document;
      try {
        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(urlStream);
      }
      finally {
        urlStream.close();
      }
      document.normalize();

      _fetchedRows = new NSMutableArray<NSMutableDictionary<String, Object>>();
      NodeList rowElements = document.getElementsByTagName(singularName(entity));
      for (int rowNum = 0; rowNum < rowElements.getLength(); rowNum++) {
        NSMutableDictionary<String, Object> row = new NSMutableDictionary<String, Object>();
        Element rowElement = (Element) rowElements.item(rowNum);
        for (int attributeNum = 0; attributeNum < attributesToFetch.count(); attributeNum++) {
          EOAttribute attribute = (EOAttribute) attributesToFetch.objectAtIndex(attributeNum);
          String columnName = attribute.columnName();
          NodeList attributeElements = rowElement.getElementsByTagName(columnName);

          Object value;
          if (attributeElements.getLength() == 0) {
            if (rowElement.hasAttribute(columnName)) {
              Attr columnAttribute = rowElement.getAttributeNode(columnName);
              value = convertValue(columnAttribute.getValue(), attribute);
            }
            else {
              value = null;
            }
          }
          else if (attributeElements.getLength() > 1) {
            throw new EOGeneralAdaptorException("There was more than one column named '" + columnName + "'.");
          }
          else {
            Element attributeElement = (Element) attributeElements.item(0);
            if ("true".equals(attributeElement.getAttribute("nil"))) {
              value = null;
            }
            else {
              String strValue = textValue(attributeElement);
              if (attributeElement.hasAttribute("type")) {
                String type = attributeElement.getAttribute("type");
                if ("bigit".equals(type)) {
                  value = new BigDecimal(strValue);
                }
                else if ("boolean".equals(type)) {
                  value = Boolean.parseBoolean(strValue);
                }
                else if ("datetime".equals(type)) {
                  value = ERRESTAdaptorChannel.restDateFormat.parseObject(strValue);
                }
                else if ("double".equals(type)) {
                  value = Double.parseDouble(strValue);
                }
                else if ("float".equals(type)) {
                  value = Float.parseFloat(strValue);
                }
                else if ("integer".equals(type)) {
                  value = Integer.parseInt(strValue);
                }
                else if ("short".equals(type)) {
                  value = Short.parseShort(strValue);
                }
                else {
                  throw new IllegalArgumentException("Unknown type '" + type + "'.");
                }
              }
              else {
                value = convertValue(strValue, attribute);
              }
            }
          }

          if (value != null) {
            row.setObjectForKey(value, attribute.name());
          }
        }

        // Just in case you qualified against attributes that ended up not
        // being returned in the results, we augment the resulting row
        // with the key-value pairs from your qualifier.
        if (attributesFromQualifier.count() > 0) {
          for (String qualifierAttributeName : attributesFromQualifier.allKeys()) {
            if (!row.containsKey(qualifierAttributeName)) {
              EOAttribute qualifierAttribute = entity.attributeNamed(qualifierAttributeName);
              if (qualifierAttribute != null && attributesToFetch.containsObject(qualifierAttribute)) {
                row.setObjectForKey(attributesFromQualifier.objectForKey(qualifierAttributeName), qualifierAttributeName);
              }
            }
          }
        }

        // Because we can only interpret parts of your qualifier, we now run the
        // fetched dictionaries through the in-memory qualifier and let it weed
        // out results using the more complicatd qualifiers.
        EOQualifier qualifier = fetchSpecification.qualifier();
        if (qualifier == null || qualifier.evaluateWithObject(row)) {
          _fetchedRows.addObject(row);
        }
        else {
          System.out.println("ERRESTAdaptorChannel.selectAttributes: skipping " + row + " (" + qualifier + ")");
        }
      }
    }
    catch (EOGeneralAdaptorException e) {
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new EOGeneralAdaptorException("Failed to fetch '" + entity.name() + "' with fetch specification '" + fetchSpecification + "': " + e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setAttributesToFetch(NSArray attributesToFetch) {
    if (attributesToFetch == null) {
      throw new IllegalArgumentException("ERMemoryAdaptorChannel.setAttributesToFetch: null attributes.");
    }
    _attributes = attributesToFetch;
  }

  @Override
  public int updateValuesInRowsDescribedByQualifier(NSDictionary updatedRow, EOQualifier qualifier, EOEntity entity) {
    try {
      throw new EOGeneralAdaptorException("ERRESTAdaptorChannel.updateValuesInRowsDescribedByQualifier not supported.");
    }
    catch (EOGeneralAdaptorException e) {
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new EOGeneralAdaptorException("Failed to update '" + entity.name() + "' row " + updatedRow + " with qualifier " + qualifier + ": " + e.getMessage());
    }
  }

  @Override
  public void insertRow(NSDictionary row, EOEntity entity) {
    try {
      throw new EOGeneralAdaptorException("ERRESTAdaptorChannel.insertRow not supported.");
    }
    catch (EOGeneralAdaptorException e) {
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new EOGeneralAdaptorException("Failed to insert '" + entity.name() + "' with row " + row + ": " + e.getMessage());
    }
  }

  @Override
  public int deleteRowsDescribedByQualifier(EOQualifier qualifier, EOEntity entity) {
    try {
      throw new EOGeneralAdaptorException("ERRESTAdaptorChannel.deleteRowsDescribedByQualifier not supported.");
    }
    catch (EOGeneralAdaptorException e) {
      throw e;
    }
    catch (Throwable e) {
      e.printStackTrace();
      throw new EOGeneralAdaptorException("Failed to delete '" + entity.name() + "' with qualifier " + qualifier + ": " + e.getMessage());
    }
  }
}
