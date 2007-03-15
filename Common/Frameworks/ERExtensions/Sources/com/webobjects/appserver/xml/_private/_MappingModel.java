/*jadclipse*/// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) braces fieldsfirst splitstr(nl) nonlb lnc radix(10) lradix(10) 
// Source File Name:   _MappingModel.java

package com.webobjects.appserver.xml._private;

import java.util.Enumeration;

import org.xml.sax.Parser;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.xml.WOXMLException;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Bufix that synchronizes on the mapping model, as it throws when you call it from multiple threads.
 * 
 *
 * @author ak
 */
public class _MappingModel implements NSKeyValueCoding {
    public static class KeyMappingDescriptor implements NSKeyValueCoding {

        public String name;

        public String xmlTag;

        public boolean attribute;

        public boolean forceList;

        public boolean reportEmptyValues;

        public boolean codeBasedOnClass;

        public String outputTags;

        public void setAttribute(String s) {
            attribute = s != null && s.equalsIgnoreCase("YES");
        }

        public void setForceList(String s) {
            forceList = s != null && s.equalsIgnoreCase("YES");
        }

        public void setReportEmptyValues(String s) {
            reportEmptyValues = s != null && s.equalsIgnoreCase("YES");
        }

        public void setCodeBasedOn(String s) {
            codeBasedOnClass = s == null || !s.equalsIgnoreCase("TAG");
        }

        public Object valueForKey(String s) {
            return com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.valueForKey(this, s);
        }

        public void takeValueForKey(Object obj, String s) {
            com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, obj, s);
        }

        public KeyMappingDescriptor() {
            reportEmptyValues = true;
            codeBasedOnClass = true;
            outputTags = "property";
        }
    }

    public static class EntityMappingDescriptor implements NSKeyValueCoding {

        public String name;

        public String xmlTag;

        public String unmappedTagsKey;

        public String contentsKey;

        public boolean _ignoreUnmappedTags;

        private NSMutableDictionary _keyMappingDescriptorsByName;

        private NSMutableDictionary _keyMappingDescriptorsByXMLTag;

        private NSMutableArray _attributeMappingDescriptors;

        private NSMutableArray _contentsMappingDescriptors;

        public void setIgnoreUnmappedTags(String s) {
            _ignoreUnmappedTags = s != null && s.toUpperCase().equals("YES");
        }

        public boolean ignoreUnmappedTags() {
            return _ignoreUnmappedTags;
        }

        public void addKeyMappingDescriptor(KeyMappingDescriptor keymappingdescriptor) {
            if (keymappingdescriptor.name == null || keymappingdescriptor.xmlTag == null) {
                throw new WOXMLException("Missing name or XML tag in property description");
            }
            if (_keyMappingDescriptorsByName.objectForKey(keymappingdescriptor.name) != null) {
                WOApplication.application().debugString(
                        "*** Warning: Two mappings for the same property '" + keymappingdescriptor.name
                                + "'. This mapping model can't be used for coding");
            } else {
                _keyMappingDescriptorsByName.setObjectForKey(keymappingdescriptor, keymappingdescriptor.name);
            }
            if (_keyMappingDescriptorsByXMLTag.objectForKey(keymappingdescriptor.xmlTag) != null) {
                WOApplication.application().debugString(
                        "*** Warning: Two mappings for the same XML tag '" + keymappingdescriptor.xmlTag
                                + "'. This mapping model can't be used for decoding");
            } else {
                _keyMappingDescriptorsByXMLTag.setObjectForKey(keymappingdescriptor,
                        keymappingdescriptor.xmlTag);
            }
        }

        public void setKeyMappingDescriptors(NSArray nsarray) {
            _attributeMappingDescriptors = null;
            _contentsMappingDescriptors = null;
            _keyMappingDescriptorsByName.removeAllObjects();
            _keyMappingDescriptorsByXMLTag.removeAllObjects();
            for (Enumeration enumeration = nsarray.objectEnumerator(); enumeration.hasMoreElements(); addKeyMappingDescriptor((KeyMappingDescriptor) enumeration
                    .nextElement())) {
            }
        }

        public Enumeration attributeKeys() {
            if (_attributeMappingDescriptors == null) {
                _attributeMappingDescriptors = new NSMutableArray(_keyMappingDescriptorsByName.count());
                Enumeration enumeration = _keyMappingDescriptorsByName.objectEnumerator();
                do {
                    if (!enumeration.hasMoreElements()) {
                        break;
                    }
                    KeyMappingDescriptor keymappingdescriptor = (KeyMappingDescriptor) enumeration
                            .nextElement();
                    if (keymappingdescriptor.attribute) {
                        _attributeMappingDescriptors.addObject(keymappingdescriptor.name);
                    }
                } while (true);
            }
            return _attributeMappingDescriptors.objectEnumerator();
        }

        public Enumeration contentsKeys() {
            if (_contentsMappingDescriptors == null) {
                _contentsMappingDescriptors = new NSMutableArray(_keyMappingDescriptorsByName.count());
                Enumeration enumeration = _keyMappingDescriptorsByName.objectEnumerator();
                do {
                    if (!enumeration.hasMoreElements()) {
                        break;
                    }
                    KeyMappingDescriptor keymappingdescriptor = (KeyMappingDescriptor) enumeration
                            .nextElement();
                    if (!keymappingdescriptor.attribute) {
                        _contentsMappingDescriptors.addObject(keymappingdescriptor.name);
                    }
                } while (true);
            }
            return _contentsMappingDescriptors.objectEnumerator();
        }

        public Object valueForKey(String s) {
            return com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.valueForKey(this, s);
        }

        public void takeValueForKey(Object obj, String s) {
            com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, obj, s);
        }

        public NSArray keyMappingDescriptors() {
            return null;
        }

        public EntityMappingDescriptor() {
            _ignoreUnmappedTags = false;
            _keyMappingDescriptorsByName = new NSMutableDictionary();
            _keyMappingDescriptorsByXMLTag = new NSMutableDictionary();
            _attributeMappingDescriptors = null;
            _contentsMappingDescriptors = null;
        }
    }

    public static final int OUTPUT_CLASS_TAG = 1;

    public static final int OUTPUT_PROPERTY_TAG = 2;

    public static final int OUTPUT_BOTH_TAGS = 3;

    public static final int OUTPUT_NEITHER_TAG = 0;

    static final String OUTPUT_CLASS_TAG_STRING = "class";

    static final String OUTPUT_PROPERTY_TAG_STRING = "property";

    static final String OUTPUT_BOTH_TAG_STRING = "both";

    static final String OUTPUT_NEITHER_TAG_STRING = "neither";

    private NSMutableDictionary _entityMappingDescriptorsByName;

    private NSMutableDictionary _entityMappingDescriptorsByXMLTag;

    private static _WOXMLMappingDecoder _mappingModelDecoder;

    public _MappingModel() {
        _entityMappingDescriptorsByName = new NSMutableDictionary();
        _entityMappingDescriptorsByXMLTag = new NSMutableDictionary();
    }

    public static _WOXMLMappingDecoder mappingModelDecoder() {
        if (_mappingModelDecoder == null) {
            _MappingModel _lmappingmodel = new _MappingModel();
            EntityMappingDescriptor entitymappingdescriptor = new EntityMappingDescriptor();
            entitymappingdescriptor.name = "com.webobjects.appserver.xml._private._MappingModel";
            entitymappingdescriptor.xmlTag = "model";
            _lmappingmodel.registerDescriptor(entitymappingdescriptor);
            KeyMappingDescriptor keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "descriptors";
            keymappingdescriptor.xmlTag = "entity";
            keymappingdescriptor.forceList = true;
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            entitymappingdescriptor = new EntityMappingDescriptor();
            entitymappingdescriptor.name = "com.webobjects.appserver.xml._private._MappingModel$EntityMappingDescriptor";
            entitymappingdescriptor.xmlTag = "entity";
            _lmappingmodel.registerDescriptor(entitymappingdescriptor);
            keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "keyMappingDescriptors";
            keymappingdescriptor.xmlTag = "property";
            keymappingdescriptor.forceList = true;
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "name";
            keymappingdescriptor.xmlTag = "name";
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "xmlTag";
            keymappingdescriptor.xmlTag = "xmlTag";
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "unmappedTagsKey";
            keymappingdescriptor.xmlTag = "unmappedTagsKey";
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "ignoreUnmappedTags";
            keymappingdescriptor.xmlTag = "ignoreUnmappedTags";
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "contentsKey";
            keymappingdescriptor.xmlTag = "contentsKey";
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            entitymappingdescriptor = new EntityMappingDescriptor();
            entitymappingdescriptor.name = "com.webobjects.appserver.xml._private._MappingModel$KeyMappingDescriptor";
            entitymappingdescriptor.xmlTag = "property";
            keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "name";
            keymappingdescriptor.xmlTag = "name";
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "xmlTag";
            keymappingdescriptor.xmlTag = "xmlTag";
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "attribute";
            keymappingdescriptor.xmlTag = "attribute";
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "forceList";
            keymappingdescriptor.xmlTag = "forceList";
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "codeBasedOn";
            keymappingdescriptor.xmlTag = "codeBasedOn";
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "reportEmptyValues";
            keymappingdescriptor.xmlTag = "reportEmptyValues";
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            keymappingdescriptor = new KeyMappingDescriptor();
            keymappingdescriptor.name = "outputTags";
            keymappingdescriptor.xmlTag = "outputTags";
            entitymappingdescriptor.addKeyMappingDescriptor(keymappingdescriptor);
            _lmappingmodel.registerDescriptor(entitymappingdescriptor);
            _mappingModelDecoder = new _WOXMLMappingDecoder(_lmappingmodel);
        }
        return _mappingModelDecoder;
    }

    public void registerDescriptor(EntityMappingDescriptor entitymappingdescriptor) {
        if (entitymappingdescriptor.name == null) {
            throw new WOXMLException("Missing name attribute for entity xmlTag="
                    + entitymappingdescriptor.xmlTag);
        }
        _entityMappingDescriptorsByName.setObjectForKey(entitymappingdescriptor, entitymappingdescriptor.name);
        if (entitymappingdescriptor.xmlTag == null) {
            throw new WOXMLException("Missing xmlTag attribute for entity name="
                    + entitymappingdescriptor.name);
        } else {
            _entityMappingDescriptorsByXMLTag.setObjectForKey(entitymappingdescriptor,
                    entitymappingdescriptor.xmlTag);
            return;
        }
    }

    public void setDescriptors(NSArray nsarray) {
        _entityMappingDescriptorsByName.removeAllObjects();
        _entityMappingDescriptorsByXMLTag.removeAllObjects();
        for (Enumeration enumeration = nsarray.objectEnumerator(); enumeration.hasMoreElements(); registerDescriptor((EntityMappingDescriptor) enumeration
                .nextElement())) {
        }
    }

    public NSArray descriptors() {
        return null;
    }

    protected String classNameForXMLTag(String s) {
        EntityMappingDescriptor entitymappingdescriptor = (EntityMappingDescriptor) _entityMappingDescriptorsByXMLTag
                .objectForKey(s);
        return entitymappingdescriptor == null ? "com.webobjects.foundation.NSMutableDictionary"
                : entitymappingdescriptor.name;
    }

    public boolean hasMappingForXMLTag(String s) {
        return _entityMappingDescriptorsByXMLTag.objectForKey(s) != null;
    }

    protected String propertyKeyForXMLTag(String s, String s1) {
        if (s == null || s1 == null) {
            throw new WOXMLException("XMLMappingModel.propertyKeyForXMLTag: null tag or entityName");
        }
        String s2 = null;
        EntityMappingDescriptor entitymappingdescriptor = (EntityMappingDescriptor) _entityMappingDescriptorsByXMLTag
                .objectForKey(s1);
        KeyMappingDescriptor keymappingdescriptor = entitymappingdescriptor == null ? null
                : (KeyMappingDescriptor) entitymappingdescriptor._keyMappingDescriptorsByXMLTag.objectForKey(s);
        if (keymappingdescriptor != null) {
            s2 = keymappingdescriptor.name;
        } else
        if (entitymappingdescriptor == null) {
            s2 = s;
        } else
        if (entitymappingdescriptor.unmappedTagsKey == null && !entitymappingdescriptor._ignoreUnmappedTags) {
            throw new WOXMLException("Found unmapped tag '" + s + "' in container '" + s1
                    + "' for which unmappedTagsKey was not specified and ignoreUnmappedTags was NO.");
        }
        return s2;
    }

    public String xmlTagForClassNamed(String s) {
        EntityMappingDescriptor entitymappingdescriptor = (EntityMappingDescriptor) _entityMappingDescriptorsByName
                .objectForKey(s);
        return entitymappingdescriptor == null ? s : entitymappingdescriptor.xmlTag;
    }

    protected KeyMappingDescriptor keyMappingDescriptor(String s, String s1) {
        if (s == null || s1 == null) {
            throw new WOXMLException("XMLMappingModel: null propertyKey or className");
        } else {
            EntityMappingDescriptor entitymappingdescriptor = (EntityMappingDescriptor) _entityMappingDescriptorsByName
                    .objectForKey(s1);
            return entitymappingdescriptor == null ? null
                    : (KeyMappingDescriptor) entitymappingdescriptor._keyMappingDescriptorsByName.objectForKey(s);
        }
    }

    public String xmlTagForPropertyKey(String s, String s1) {
        KeyMappingDescriptor keymappingdescriptor = keyMappingDescriptor(s, s1);
        return keymappingdescriptor == null ? s : keymappingdescriptor.xmlTag;
    }

    protected boolean forceListForPropertyKey(String s, String s1) {
        KeyMappingDescriptor keymappingdescriptor = keyMappingDescriptor(s, s1);
        return keymappingdescriptor == null ? false : keymappingdescriptor.forceList;
    }

    protected boolean reportEmptyValuesForPropertyKey(String s, String s1) {
        if (s != null) {
            KeyMappingDescriptor keymappingdescriptor = keyMappingDescriptor(s, s1);
            return keymappingdescriptor == null ? false : keymappingdescriptor.reportEmptyValues;
        } else {
            return false;
        }
    }

    public boolean codeBasedOnClassForPropertyKey(String s, String s1) {
        KeyMappingDescriptor keymappingdescriptor = keyMappingDescriptor(s, s1);
        return keymappingdescriptor == null ? false : keymappingdescriptor.codeBasedOnClass;
    }

    public int outputTagsForPropertyKey(String s, String s1) {
        KeyMappingDescriptor keymappingdescriptor = keyMappingDescriptor(s, s1);
        byte byte0 = 2;
        if (keymappingdescriptor != null) {
            if (keymappingdescriptor.outputTags.equals("neither")) {
                byte0 = 0;
            } else
            if (keymappingdescriptor.outputTags.equals("both")) {
                byte0 = 3;
            } else
            if (keymappingdescriptor.outputTags.equals("property")) {
                byte0 = 2;
            } else
            if (keymappingdescriptor.outputTags.equals("class")) {
                byte0 = 1;
            }
        }
        return byte0;
    }

    public String xmlTagForPropertyKeyInXMLTag(String s, String s1) {
        if (s == null || s1 == null) {
            throw new WOXMLException("XMLMappingModel: null propertyKey or xmlTag");
        } else {
            EntityMappingDescriptor entitymappingdescriptor = (EntityMappingDescriptor) _entityMappingDescriptorsByXMLTag
                    .objectForKey(s1);
            KeyMappingDescriptor keymappingdescriptor = entitymappingdescriptor == null ? null
                    : (KeyMappingDescriptor) entitymappingdescriptor._keyMappingDescriptorsByName.objectForKey(s);
            return keymappingdescriptor == null ? s : keymappingdescriptor.xmlTag;
        }
    }

    public Enumeration attributeKeysForClassNamed(String s) {
        EntityMappingDescriptor entitymappingdescriptor = (EntityMappingDescriptor) _entityMappingDescriptorsByName
                .objectForKey(s);
        return entitymappingdescriptor == null ? null : entitymappingdescriptor.attributeKeys();
    }

    public Enumeration attributeKeysForXMLTag(String s) {
        EntityMappingDescriptor entitymappingdescriptor = (EntityMappingDescriptor) _entityMappingDescriptorsByXMLTag
                .objectForKey(s);
        return entitymappingdescriptor == null ? null : entitymappingdescriptor.attributeKeys();
    }

    public Enumeration contentsKeysForClassNamed(String s) {
        EntityMappingDescriptor entitymappingdescriptor = (EntityMappingDescriptor) _entityMappingDescriptorsByName
                .objectForKey(s);
        return entitymappingdescriptor == null ? null : entitymappingdescriptor.contentsKeys();
    }

    public Enumeration contentsKeysForXMLTag(String s) {
        EntityMappingDescriptor entitymappingdescriptor = (EntityMappingDescriptor) _entityMappingDescriptorsByXMLTag
                .objectForKey(s);
        return entitymappingdescriptor == null ? null : entitymappingdescriptor.contentsKeys();
    }

    protected String unmappedTagsKeyForXMLTag(String s) {
        EntityMappingDescriptor entitymappingdescriptor = (EntityMappingDescriptor) _entityMappingDescriptorsByXMLTag
                .objectForKey(s);
        return entitymappingdescriptor == null ? null : entitymappingdescriptor.unmappedTagsKey;
    }

    protected String contentsKeyForXMLTag(String s) {
        EntityMappingDescriptor entitymappingdescriptor = (EntityMappingDescriptor) _entityMappingDescriptorsByXMLTag
                .objectForKey(s);
        return entitymappingdescriptor == null ? null : entitymappingdescriptor.contentsKey;
    }

    protected boolean ignoreUnmappedTagsForXMLTag(String s) {
        EntityMappingDescriptor entitymappingdescriptor = (EntityMappingDescriptor) _entityMappingDescriptorsByXMLTag
                .objectForKey(s);
        return entitymappingdescriptor == null ? false : entitymappingdescriptor.ignoreUnmappedTags();
    }

    public static _MappingModel mappingModelWithXMLFile(String s) {
    	// AK: synchronizes, as the parsing can't be entered from more than one thread
    	_WOXMLMappingDecoder decoder = mappingModelDecoder();
    	synchronized (decoder) {
            Parser parser = decoder.parser();
            _MappingHandler _lmappinghandler = new _MappingHandler(decoder);
            parser.setDocumentHandler(_lmappinghandler);
            try {
                parser.parse(s);
            }
            catch (Throwable throwable) {
                throw new NSForwardException(throwable, "Error loading " + s);
            }
            return (_MappingModel) _lmappinghandler.root();
		}
    }

    public Object valueForKey(String s) {
        return com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.valueForKey(this, s);
    }

    public void takeValueForKey(Object obj, String s) {
        com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, obj, s);
    }
}
