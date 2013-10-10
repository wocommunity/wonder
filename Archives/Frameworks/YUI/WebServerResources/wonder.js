
// our own extensions 
if (typeof WONDER == "undefined") {
    var WONDER = {};
}

WONDER.namespace = function() {
    var a=arguments, o=null, i, j, d;
    for (i=0; i<a.length; ++i) {
        d=a[i].split(".");
        o=WONDER;

        // WONDER is implied, so it is ignored if it is included
        for (j=(d[0] == "WONDER") ? 1 : 0; j<d.length; ++j) {
            o[d[j]]=o[d[j]] || {};
            o=o[d[j]];
        }
    }

    return o;
};

WONDER.namespace("grid", "log");

WONDER.grid.Preprocessors = {
	"com.webobjects.foundation.NSTimestamp" : function(v) {
		if(!v) return undefined;
		var result = new Date();
		result.setTime(v.time);
		return result;
	}
};

WONDER.grid.registerPreprocessors = function(name, val) {
	WONDER.grid.Preprocessors[name] = val;
}

WONDER.grid.Postprocessors = {
};

WONDER.grid.registerPostprocessors = function(name, val) {
	WONDER.grid.Postprocessors[name] = val;
}

WONDER.grid.Renderers = {
	"com.webobjects.foundation.NSTimestamp" : function(v) {
		if(!v) return undefined;
		return "<span style='color: red;'>" + v.toLocaleString() + "</span>";
	}, 
	"java.math.BigDecimal" : function(v) {
		if(!v) return undefined;
		return "<span style='color: green; width: 100px; display: block; text-align: right;'>" + v.toLocaleString() + "</span>";
	}
};

WONDER.grid.registerRenderer = function(name, val) {
	WONDER.grid.Renderers[name] = val;
}

WONDER.grid.Editors = {
	"com.webobjects.foundation.NSTimestamp" :  "new WONDER.grid.DateEditor()",
	"java.lang.Boolean" :  "new YAHOO.ext.grid.CheckboxEditor()",
	"java.math.BigDecimal" :  "new YAHOO.ext.grid.NumberEditor({})",
	"java.lang.String" : "new YAHOO.ext.grid.TextEditor({})"
};

WONDER.grid.registerEditor = function(name, val) {
	WONDER.grid.Editors[name] = val;
}

WONDER.grid.EODataModel = function(schema) {
	var parent = {};
	parent.root = schema.root;
	parent.id = schema.id;
	parent.fields = [];
    for(var i = 0; i < schema.fields.length; i++) {
    	var item = schema.fields[i];
     	parent.fields.push(item.name);
    }
    WONDER.grid.EODataModel.superclass.constructor.call(this, parent);
    for(var i = 0; i < schema.fields.length; i++) {
    	var item = schema.fields[i];
   		if(item) {
	   		this.addPreprocessor(i, item.preprocessor);
	   	}
   		if(item) {
	   		this.addPostprocessor(i, item.postprocessor);
	   	}
    }
};

YAHOO.extendX(WONDER.grid.EODataModel, YAHOO.ext.grid.JSONDataModel, {
    loadData : function(data, callback, keepExisting){
    	var idField = this.schema.id;
    	var fields = this.schema.fields;
    	try {
        	if(this.schema.totalProperty) {
                var v = parseInt(eval('data.' + this.schema.totalProperty), 10);
                if(!isNaN(v)){
                    this.totalCount = v;
                }
            }
        	var rowData = [];
    	    var root = eval('data.' + this.schema.root);
    	    for(var i = 0; i < root.length; i++) {
    			var node = root[i]['eo'] ? root[i]['eo'] :  root[i];
    			var colData = [];
    			colData.node = node;
    			colData.id = (typeof node[idField] != 'undefined' && node[idField] !== '' ? node[idField] : String(i));
    			for(var j = 0; j < fields.length; j++) {
    			    var val = node[fields[j]];
    			    if(typeof val == 'undefined') {
    			        val = '';
    			    }
    	            if(this.preprocessors[j]) {
    	                val = this.preprocessors[j](val);
    	            }
    	            colData.push(val);
    	        }
    	        rowData.push(colData);
    		}
    		if(keepExisting !== true) {
    		  this.removeAll();
    		}
            this.addRows(rowData);
        	if(typeof callback == 'function') {
    	    	callback(this, true);
    	    }
          	this.fireLoadEvent();
    	} catch(e) {
    		this.fireLoadException(e, null);
    		if(typeof callback == 'function') {
    	    	callback(this, false);
    	    }
    	}
    },

    getRowId : function(rowIndex) {
        return this.data[rowIndex].id;
    }
});

WONDER.grid.DefaultColumnModel = function(schema){
    for(var i = 0; i < schema.length; i++) {
    	var item = schema[i];
   		if(item) {
   			if(item.id && WONDER.grid.Editors[item.id]) {
   				item.editor = WONDER.grid.Editors[item.id];
   			}
   			if(item.editor) {
   				// editors are string until here
	   			item.editor = eval(item.editor);
	   		}
	   	}
    }
    WONDER.grid.DefaultColumnModel.superclass.constructor.call(this, schema);
    for(var i = 0; i < schema.length; i++) {
    	var item = schema[i];
   		if(item) {
	   		this.setRenderer(i, item.renderer);
	   	}
    }
};

YAHOO.extendX(WONDER.grid.DefaultColumnModel, YAHOO.ext.grid.DefaultColumnModel, {
});


WONDER.grid.Grid = function(element, columnModel, dataModel, selectionModel) {
    WONDER.grid.Grid.superclass.constructor.call(this, element, columnModel, dataModel, selectionModel);
	this.autosizeColumns=true;
};

YAHOO.extendX(WONDER.grid.Grid, YAHOO.ext.grid.Grid, {
});


WONDER.grid.DateEditor = function() {
    WONDER.grid.DateEditor.superclass.constructor.call(this);
    this.format = "Y-m-d";
};

YAHOO.extendX(WONDER.grid.DateEditor, YAHOO.ext.grid.DateEditor, {
});

WONDER.grid.SelectEditor = function(element) {
    WONDER.grid.SelectEditor.superclass.constructor.call(this, element);
};

YAHOO.extendX(WONDER.grid.SelectEditor, YAHOO.ext.grid.SelectEditor, {
});

