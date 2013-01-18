package er.reporting;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXValueUtilities;
import er.grouping.DRAttribute;
import er.grouping.DRGroup;
import er.grouping.DRReportModel;

public class WRAttributeGroup extends WOComponent {

    protected DRReportModel _model;
    protected Boolean _totalsOnly;
    protected NSArray _groups;

    public int depth;
    public DRAttribute attrib;
    public DRGroup aGroup;

    public WRAttributeGroup(WOContext c){
        super(c);
    }

    @Override
    public void reset() {
        _totalsOnly = null;
        _model = null;
        _groups = null;
        super.reset();
    }

    @Override
    public boolean isStateless() {
        return true;
    }

    @Override
    public void awake() {
        this.reset();
    }

    public DRReportModel model() {
        if (_model == null) {
            _model = (DRReportModel)this.valueForBinding("model");
        }
        return _model;
    }


    public boolean totalsOnly() {
        if (_totalsOnly == null) {
            Object v = (Object)this.valueForBinding("totalsOnly");
            if (ERXValueUtilities.booleanValue(v)) {
                _totalsOnly = Boolean.TRUE;
            } else {
                _totalsOnly = Boolean.FALSE;
            }
        }
        return _totalsOnly.booleanValue();
    }

    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        this.reset();
        super.appendToResponse(r, c);
    }


    public NSDictionary attributeListDict() {
        if (this.totalsOnly()) {
            return this.model().flatAttributeListTotalDict();
        }

        return this.model().flatAttributeDepthDict();
    }


    public int depthCount() {
        //log.debug( "attributeListDict():"+attributeListDict());
        return this.attributeListDict().allKeys().count();
    }


    public NSArray attributeListAtDepth() {
        Number ky = Integer.valueOf(depth);
        return (NSArray)this.attributeListDict().objectForKey(ky);
    }


    public int colSpan() {
        int cls;

        if (this.totalsOnly()) {
            cls = attrib.flatAttributesTotal().count();
        } else {
            cls = attrib.flatAttributes().count();
        }

        if (attrib.showTotal()) {
            return cls+1;
        }

        return cls;
    }


    public int rowSpan() {

        if (attrib.isGroup()) {
            return 1;
        }

        return this.depthCount()-depth;
    }


    public String attribLabel() {
        return attrib.label();
    }

    public NSArray groups() {
        if (_groups == null) {
            _groups = (NSArray)this.valueForBinding("groups");

            if (_groups == null) {
                _groups = new NSArray();
            }

        }

        return _groups;
    }

    @Override
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        //Abort call to super to save all this processing time
    }
}
