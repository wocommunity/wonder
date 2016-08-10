package er.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;

import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXAssert;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXValueUtilities;
import er.grouping.DRReportModel;

/**
 * Provides a quick way to set up a {@link WRReport}. Instead of binding all those keys,
 * you can simply set up the components via a dictionary.
 * There are several modes you can supply the data:
 * <li>Via a model dictionary, a path to a model dictionary or a string defining a model dictionary<br/>
 *     A model dictionary is defined by the keys <code>GroupDef</code> and <code>AttributeDef</code>.
 * <li>Via a report dictionary. <br />
 *     A report dictionary is a dictionary with the key <code>model</code> defining
 *     a model dictionary, and a key <code>settings</code>, defining the values normally bound
 *     to the report, like <code>shouldShowNavigation</code> and the like.
 * Additionally, you can bind all values defining the report to the component itself, overriding
 * the values in the dictionary.
 */

public class WRQuickReport extends WOComponent  {
    private static final Logger log = LoggerFactory.getLogger(WRQuickReport.class);
    protected DRReportModel _model;
    protected NSDictionary _modelDictionary;
    protected NSDictionary _reportDictionary;
    protected NSDictionary _settingsDictionary;
    protected NSArray _objects;
    protected NSArray _attributeArray;
    protected NSArray _criteriaArray;
    protected String _componentName;
    
    public WRQuickReport(WOContext c){
        super(c);
        NSSelector synchModelSelector = new NSSelector("synchModel", ERXConstant.NotificationClassArray);
        NSNotificationCenter.defaultCenter().addObserver(this, synchModelSelector, DRReportModel.DRReportModelUpdateNotification, null);
        NSNotificationCenter.defaultCenter().addObserver(this, synchModelSelector, DRReportModel.DRReportModelRebuildNotification, null);
    }
    
    public String reportComponentName() {
        String name = (String) valueForBinding("reportComponentName");
        if(name == null) {
            name =  "WRReport";
        }
        return name;
    }

    public void synchModel(NSNotification notification) {
        if(_model == notification.object() && !dontSyncModel()) {
            DRReportModel model = _model;
            reset();
            if(model != null) {
                model.initWithRawRecords(objects(), criteriaArray(), attributeArray());
            }
            log.info("Model was re-set.");
            //reset();
        }
    }

    public String componentName() {
        if(_componentName == null) {
            _componentName = (String)valueForBinding("reportComponentName");
            if(_componentName == null) {
                _componentName = "WRRecordGroup";
            }
        }
        return _componentName;
    }

    @Override
    public void finalize() throws Throwable {
        NSNotificationCenter.defaultCenter().removeObserver(this);
        super.finalize();
    }

    @Override
    public void awake() {
        super.awake();
        _objects = null;
        if(false) {
            _model = null;
            _modelDictionary = null;
            _componentName = null;
            _reportDictionary = null;
            _settingsDictionary = null;
            _attributeArray = null;
            _criteriaArray = null;
            _objects = null;
        }
    }
    @Override
    public void reset() {
        super.reset();
        _model = null;
        _modelDictionary = null;
        _reportDictionary = null;
        _settingsDictionary = null;
        _attributeArray = null;
        _criteriaArray = null;
        _objects = null;
        _componentName = null;
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public String plistString() {
        if (super.hasBinding("plistString")) {
            return (String)super.valueForBinding("plistString");
        } else {
            if (hasBinding("pathString")) {
                String p = (String)super.valueForBinding("pathString");
                log.debug("p: {}", p);
                String plist = ERXStringUtilities.stringWithContentsOfFile(p);
                log.debug("plist: {}", plist);
                return plist;
            }
        }
        return null;
    }

    public NSDictionary modelDictionary() {
        if(_modelDictionary == null) {
            if (super.hasBinding("modelDictionary")) {
                _modelDictionary = (NSDictionary)super.valueForBinding("modelDictionary");
            } else {
                String plistString = plistString();

                if (plistString != null) {
                    _modelDictionary = (NSDictionary)NSPropertyListSerialization.propertyListFromString(plistString);
                } else {
                    _modelDictionary = (NSDictionary)reportDictionary().objectForKey("model");
                    if(_modelDictionary == null) {
                        log.warn("No modelDictionary found!");
                        _modelDictionary = NSDictionary.EmptyDictionary;
                    }
                }
                log.debug("plistString: {}", plistString);
                log.debug( "modelDict: {}", _modelDictionary);
            }
        }
        return _modelDictionary;
    }

    public NSDictionary reportDictionary() {
        if(_reportDictionary == null) {
            if (super.hasBinding("reportDictionary")) {
                _reportDictionary = (NSDictionary)super.valueForBinding("reportDictionary");
            } else {
                _reportDictionary = NSDictionary.EmptyDictionary;
            }
        }
        return _reportDictionary;
    }

    public NSDictionary settingsDictionary() {
        if(_settingsDictionary == null) {
            _settingsDictionary = (NSDictionary)reportDictionary().objectForKey("settings");
            if(_settingsDictionary == null) {
                _settingsDictionary = NSDictionary.EmptyDictionary;
            }
        }
        return _settingsDictionary;
    }

    public NSArray criteriaArray() {
        if(_criteriaArray == null) {
            if (super.hasBinding("criteriaArray")) {
                _criteriaArray = (NSArray)super.valueForBinding("criteriaArray");
            }  else {
                NSArray rawArray = (NSArray)modelDictionary().objectForKey("GroupDef");
                _criteriaArray = DRReportModel.masterCriteriaList(rawArray);
            }
            ERXAssert.DURING.notNull("criteriaArray", _criteriaArray);
        }
        return _criteriaArray;
    }

    public NSArray attributeArray() {
        if(_attributeArray == null) {
            if (super.hasBinding("attributeArray")) {
                _attributeArray = (NSArray)super.valueForBinding("attributeArray");
            }  else {
                NSArray rawArray = (NSArray)modelDictionary().objectForKey("AttributeDef");
                _attributeArray = DRReportModel.attributeList(rawArray);
            }
            ERXAssert.DURING.notNull("attributeArray", _attributeArray);
        }
        return _attributeArray;
    }

    public DRReportModel model() {
        if (_model == null) {
            if(super.hasBinding("model")) {
                log.info("pulling model from bindings");
                _model = (DRReportModel)super.valueForBinding("model");
            }
            if(_model == null) {
                log.info("creating model from definition");
                _model = DRReportModel.withRawRecordsCriteriaListAttributeList(objects(), criteriaArray(), attributeArray());
            }
            if(super.hasBinding("model")) {
                if(super.canSetValueForBinding("model")) {
                    log.info("setValueForBinding model: DRReportModel@{}", _model.hashCode());
                    super.setValueForBinding(_model, "model");
                }
            }
            if(log.isDebugEnabled()) {
                log.debug( "model(): DRReportModel@{}", _model.hashCode());
                log.debug( "model().records(): {}", _model.records().count());
            }
        }
        return _model;
    }

    public NSArray objects() {
        if(_objects == null) {
            if(super.hasBinding("objects")) {
                _objects = (NSArray)super.valueForBinding("objects");
            } else {
                if(super.hasBinding("dataSource")) {
                    EODataSource ds = (EODataSource)super.valueForBinding("dataSource");
                    ERXAssert.DURING.notNull("dataSource", ds);
                    _objects = ds.fetchObjects();
                }
            }
        }
        return _objects;
    }

    @Override
    public boolean hasBinding(String name) {
        boolean result = super.hasBinding(name) || settingsDictionary().objectForKey(name) != null;
        log.debug("hasBinding: {} : {}", name, result);
        return result;
    }

    @Override
    public Object valueForBinding(String name) {
        Object result;
        if(super.hasBinding(name)) {
            result = super.valueForBinding(name);
        } else {
            result = settingsDictionary().objectForKey(name);
        }
        log.debug("valueForBinding: {} : {}", name, result);
        return result;
    }

    public boolean dontSyncModel() {
        return ERXValueUtilities.booleanValue(valueForBinding("dontSyncModel"));
    }

    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        super.appendToResponse(r, c);
        //reset();
    }
}