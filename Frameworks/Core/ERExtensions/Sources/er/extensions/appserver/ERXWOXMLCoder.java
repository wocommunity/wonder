package er.extensions.appserver;

import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.Enumeration;

import org.apache.commons.lang.CharEncoding;

import com.webobjects.appserver.xml.WOXMLCoder;
import com.webobjects.appserver.xml.WOXMLDecoder;
import com.webobjects.appserver.xml.WOXMLException;
import com.webobjects.appserver.xml._private._MappingModel;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXStringUtilities;

/**
 * WOXMLMappingCoder which adds sorting to attributes.
 * 
 *
 * @author ak
 */
public class ERXWOXMLCoder extends WOXMLCoder {

    private _MappingModel _mappingModel;

    /**
     * Quick and dirty class to en- and decode the generic xml data to full-fledged objects that 
     * can be bound in the edit interface.
     * 
     *
     * @author ak
     */
    public static class XMLData extends NSMutableDictionary {
    	/**
    	 * Do I need to update serialVersionUID?
    	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
    	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
    	 */
    	private static final long serialVersionUID = 1L;

        public XMLData() {
        }
        
        public void completeDecoding() {
        }
        
        public void prepareForCoding() {
        }
        
        protected void takeValueForKeyPathIfNotPresent(Object object, String key) {
            if(valueForKeyPath(key) == null) {
                takeValueForKeyPath(object, key);
            }
        }

        protected void clearEmptyValueForKeyPath(String key) {
            if(valueForKeyPath(key) != null && ((String)valueForKeyPath(key)).length() == 0) {
                takeValueForKeyPath(null, key);
            }
        }

        protected void clearParentOnEmptyValueForKeyPath(String key) {
            if(valueForKeyPath(key) != null && ((String)valueForKeyPath(key)).length() == 0) {
                takeValueForKeyPath(null, ERXStringUtilities.keyPathWithoutLastProperty(key));
            }
        }

        /**
         * This works around a bug when the decoder reaches an empty tag an tries to create a dictionary from it.
         * @param aValue value
         * @param aKey key
         */
        @Override
        public void takeValueForKey(Object aValue, String aKey) {
            if(aValue instanceof NSDictionary && ((NSDictionary)aValue).count() == 0) {
                if(aValue.getClass() == NSMutableDictionary.class) {
                    aValue = null;
                }
            }
            super.takeValueForKey(aValue, aKey);
        }

        /**
         * Serializes to an XML string for the given data object conforming to the supplied model.
         * @param data 
         * @param rootTag 
         * @param mappingUrl 
         * @return string representation
         */
        public static String stringForData(XMLData data, String rootTag, String mappingUrl) {
            data.prepareForCoding();
            WOXMLCoder coder = new ERXWOXMLCoder(mappingUrl);
            String result = coder.encodeRootObjectForKey(data, rootTag);
            data.completeDecoding();
            return result;
        }
        
        /**
         * Deserializes the given string to an instance of XMLData.
         * @param string
         * @param mappingUrl
         * @return xml data object
         */
        public static XMLData dataForString(String string, String mappingUrl) {
            WOXMLDecoder decoder = WOXMLDecoder.decoderWithMapping(mappingUrl);
            XMLData data;
            try {
            	data = (XMLData) decoder.decodeRootObject(new NSData(string.getBytes(CharEncoding.UTF_8)));
            } catch (UnsupportedEncodingException e) {
                throw NSForwardException._runtimeExceptionForThrowable(e);
            }
            data.completeDecoding();
            return data;
        }
    }
    
    public ERXWOXMLCoder(String s) {
        _mappingModel = _MappingModel.mappingModelWithXMLFile(s);
    }

    @Override
    public String xmlTagForClassNamed(String className) {
        return _mappingModel.xmlTagForClassNamed(className);
    }

    @Override
    public String xmlTagForPropertyKey(String key, String className) {
        return _mappingModel.xmlTagForPropertyKey(key, className);
    }

    protected void _encodeEO(EOEnterpriseObject eoenterpriseobject) {
        NSArray arr = sortedArray(eoenterpriseobject.attributeKeys());
        for (Enumeration e = arr.objectEnumerator(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            encodeObjectForKey(eoenterpriseobject.valueForKey(s), s);
            if (e.hasMoreElements())
                cr();
        }
    }

    protected Enumeration sortedEnumeration(Enumeration e) {
        if(e != null) {
            NSMutableArray arr = new NSMutableArray();
            for (; e.hasMoreElements();) {
                String element = (String) e.nextElement();
                arr.addObject(element);
            }
            e = sortedArray(arr).objectEnumerator();
        }
        return e;
    }
    
    protected NSArray sortedArray(NSArray arr) {
        if(arr != null) {
            arr = ERXArrayUtilities.sortedArraySortedWithKey(arr, "toString");
        }
        return arr;
    }

    protected void encodeDictionaryWithXMLTag(NSDictionary dict, String tag) {
        NSArray nsarray = sortedArray(dict.allKeys());
        for (Enumeration e = nsarray.objectEnumerator(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String tagForKey = _mappingModel.xmlTagForPropertyKeyInXMLTag(key, tag);
            encodeObjectWithXMLTag(dict.objectForKey(key), tagForKey, false, _MappingModel.OUTPUT_PROPERTY_TAG);
            if (e.hasMoreElements())
                cr();
        }
    }

    protected void encodeArrayWithXMLTag(NSArray arr, String tag, boolean codeBasedOnClass, int outputTags) {
        for(Enumeration e = arr.objectEnumerator(); e.hasMoreElements(); ) {
            encodeObjectWithXMLTag(e.nextElement(), tag, codeBasedOnClass, outputTags);
            if (e.hasMoreElements())
                cr();
        }
    }

    @Override
    public void encodeObjectForKey(Object obj, String key) {
        String tag = _mappingModel.xmlTagForPropertyKey(key, encodedClassName());
        encodeObjectWithXMLTag(obj, tag, false, _MappingModel.OUTPUT_PROPERTY_TAG);
    }

    public void encodeObjectWithXMLTag(Object obj, String baseTag, boolean codeBasedOnClass, int outputTags) {
        if (obj instanceof NSArray) {
            encodeArrayWithXMLTag((NSArray) obj, baseTag, codeBasedOnClass, outputTags);
        } else {
            boolean isBaseType = (obj instanceof String) || (obj instanceof Boolean) || (obj instanceof Date) || (obj instanceof Number);
            String className = obj == null ? null : obj.getClass().getName();
            String tagForClassName = className == null ? "" : xmlTagForClassNamed(className);
            String tag = null;
            if (outputTags != _MappingModel.OUTPUT_NEITHER_TAG && (!isBaseType || outputTags != _MappingModel.OUTPUT_CLASS_TAG)) {
                tag = (outputTags & _MappingModel.OUTPUT_CLASS_TAG) == 0 || isBaseType ? baseTag : tagForClassName;
                if (outputTags == _MappingModel.OUTPUT_BOTH_TAGS) {
                    _buffer.append('<');
                    _buffer.append(baseTag);
                    _buffer.append('>');
                }
                _buffer.append('<');
                _buffer.append(tag);
                if (obj != null) {
                    Enumeration attributes = codeBasedOnClass ? _mappingModel.attributeKeysForClassNamed(className) : _mappingModel.attributeKeysForXMLTag(baseTag);
                    attributes = sortedEnumeration(attributes);
                    if (attributes != null)
                        for (; attributes.hasMoreElements();) {
                            String key = (String) attributes.nextElement();
                            _buffer.append(' ');
                            _buffer.append(codeBasedOnClass ? xmlTagForPropertyKey(key, className) : _mappingModel.xmlTagForPropertyKeyInXMLTag(key, baseTag));
                            _buffer.append('=');
                            _buffer.append('"');
                            Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(obj, key);
                            _buffer.append(value == null ? "" : escapeString(value.toString()));
                            _buffer.append('"');
                        }

                }
                _buffer.append('>');
            }
            if (obj != null) {
                if (obj instanceof String) {
                    _buffer.append(escapeString((String) obj));
                } else if (obj instanceof NSTimestamp) {
                    _buffer.append(obj);
                } else if (obj instanceof Boolean) {
                    _buffer.append(obj);
                } else if (obj instanceof Number) {
                    // FIXME AK: this will break when using BigDecimals and JDK 1.5
                    _buffer.append(obj);
                } else if (codeBasedOnClass || _mappingModel.hasMappingForXMLTag(baseTag)) {
                    Enumeration contentKeys = codeBasedOnClass ? _mappingModel.contentsKeysForClassNamed(className) : _mappingModel.contentsKeysForXMLTag(baseTag);
                    contentKeys = sortedEnumeration(contentKeys);
                    if (contentKeys != null) {
                        _encodedClasses.push(className);
                        boolean hadContent = false;
                        for (; contentKeys.hasMoreElements(); ) {
                            cr();
                            hadContent = true;
                            String key = (String) contentKeys.nextElement();
                            Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(obj, key);
                            String propertyTag = codeBasedOnClass ? xmlTagForPropertyKey(key, className) : _mappingModel.xmlTagForPropertyKeyInXMLTag(key, baseTag);
                            boolean codeBasedOnClassForPropertyKey = _mappingModel.codeBasedOnClassForPropertyKey(key, className);
                            int outputTagsForPropertyKey = _mappingModel.outputTagsForPropertyKey(key, className);
                            encodeObjectWithXMLTag(value, propertyTag, codeBasedOnClassForPropertyKey, outputTagsForPropertyKey);
                        }

                        _encodedClasses.pop();
                        if (hadContent)
                            cr();
                    }
                } else if (obj instanceof NSDictionary) {
                    encodeDictionaryWithXMLTag((NSDictionary) obj, baseTag);
                } else if (obj instanceof EOEnterpriseObject) {
                    _encodeEO((EOEnterpriseObject) obj);
                } else {
                    throw new WOXMLException("Unable to encode in XML objects of class " + className);
                }
            }
            if (outputTags != _MappingModel.OUTPUT_NEITHER_TAG && (!isBaseType || outputTags != _MappingModel.OUTPUT_CLASS_TAG)) {
                _buffer.append('<');
                _buffer.append('/');
                _buffer.append(tag);
                _buffer.append('>');
                if (outputTags == _MappingModel.OUTPUT_BOTH_TAGS && !isBaseType) {
                    _buffer.append('<');
                    _buffer.append('/');
                    _buffer.append(baseTag);
                    _buffer.append('>');
                }
            }
        }
    }

    @Override
    public void encodeBooleanForKey(boolean flag, String s) {
        encodeStringInTag(flag ? "True" : "False", xmlTagForPropertyKey(s, encodedClassName()), "boolean");
    }

    @Override
    public void encodeIntForKey(int i, String s) {
        encodeStringInTag(Integer.toString(i), xmlTagForPropertyKey(s, encodedClassName()), "int");
    }

    @Override
    public void encodeFloatForKey(float f, String s) {
        encodeStringInTag(Float.toString(f), xmlTagForPropertyKey(s, encodedClassName()), "float");
    }

    @Override
    public void encodeDoubleForKey(double d, String s) {
        encodeStringInTag(Double.toString(d), xmlTagForPropertyKey(s, encodedClassName()), "double");
    }

    @Override
    protected void _encodeNullForKey(String s) {
        encodeStringInTag("null", s, "?");
    }

    @Override
    public synchronized String encodeRootObjectForKey(Object obj, String s) {
        if (obj != null) {
            _buffer = new StringBuffer(1024);
            _buffer.append(xmlDeclaration);
            encodeObjectWithXMLTag(obj, "ignored", true, _MappingModel.OUTPUT_CLASS_TAG);
            return _buffer.toString();
        }
        return null;
    }
}
