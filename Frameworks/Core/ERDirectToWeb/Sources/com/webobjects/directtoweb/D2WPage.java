package com.webobjects.directtoweb;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.generation.DTWTemplate;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Re-implemented D2WPage component to fix the following problems
 * 
 * D2WPage was not serializable due to the use of a non-serializable object
 * lock. That has been removed since I can't see how a static reference to
 * pageWrapperName even makes any sense.
 * 
 * The finalize method created a garbage collection deadlock, preventing garbage
 * collection of D2WPages. Since what was happening in the finalize method was
 * not even necessary, it was removed entirely.
 * 
 * The extraBindings dictionary needs to be transient as it sometimes contains
 * EOs that cannot be serialized correctly. Since the bindings should be set by
 * the time serialization takes place, the dictionary should be superfluous.
 */
public class D2WPage extends D2WComponent {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(D2WPage.class);

	private String _pageWrapperName = null;
	private WOComponent _nextPage;
	private NextPageDelegate _nextPageDelegate;
	private EODataSource _dataSource;
	/*
	 * Made transient to prevent errors in deserialization
	 */
	private transient NSMutableDictionary<String, Object> _extraBindings;

	public D2WPage(WOContext aContext) {
		super(aContext);
	}

	public WOComponent nextPage() {
		return _nextPage;
	}

	public void setNextPage(WOComponent nextPage) {
		_nextPage = nextPage;
	}

	public NextPageDelegate nextPageDelegate() {
		return _nextPageDelegate;
	}

	public void setNextPageDelegate(NextPageDelegate nextPageDelegate) {
		_nextPageDelegate = nextPageDelegate;
	}

	public boolean showCancel() {
		return (nextPageDelegate() != null) || (nextPage() != null);
	}

	public EODataSource dataSource() {
		return _dataSource;
	}

	public void setDataSource(EODataSource dataSource) {
		_dataSource = dataSource;
	}

	public boolean alternateRowColor() {
		Object b = d2wContext().valueForKey("alternateRowColor");
		return (b == null) || (b.equals(D2WModel.One));
	}

	@Override
	public WOAssociation replacementAssociationForAssociation(WOAssociation oldAssociation, String oldBinding,
			DTWTemplate aTemplate, WOContext aContext) {
		if ((oldBinding.equals("border")) || (oldBinding.equals("d2wContext.border"))
				|| (oldBinding.equals("backgroundColorForTable"))
				|| (oldBinding.equals("d2wContext.backgroundColorForTable"))) {
			return WOAssociation.associationWithValue(oldAssociation.valueInComponent(this));
		}
		return super.replacementAssociationForAssociation(oldAssociation, oldBinding, aTemplate, aContext);
	}

	@Override
	public String descriptionForResponse(WOResponse r, WOContext c) {
		return "D2W-" + pageTitle();
	}

	public NSMutableDictionary<String, Object> extraBindings() {
		if (_extraBindings != null) {
			NSMutableDictionary<String, Object> oldBindings = _extraBindings;
			_extraBindings = new NSMutableDictionary<String, Object>(16);

			for (Enumeration<String> e = oldBindings.keyEnumerator(); e.hasMoreElements();) {
				String key = e.nextElement();
				Object newValue = valueForKey(key);
				if (newValue != null) {
					_extraBindings.setObjectForKey(newValue, key);
				}
			}
		}

		return _extraBindings;
	}

	public void setExtraBindings(NSMutableDictionary<String, Object> extraBindings) {
		_extraBindings = extraBindings;

		for (Enumeration<String> e = extraBindings.keyEnumerator(); e.hasMoreElements();) {
			String key = e.nextElement();
			Object newValue = extraBindings.objectForKey(key);

			if (newValue == NSKeyValueCoding.NullValue) {
				newValue = null;
			}
			takeValueForKey(newValue, key);
		}
	}

	public String pageWrapperName() {
		if (d2wContext().frame()) {
			return "D2WEmptyWrapper";
		}
		if (d2wContext().isGenerating()) {
			return "D2WGenerationWrapper";
		}

		if (_pageWrapperName == null) {
			try {
				String name = (String) d2wContext().valueForKey("pageWrapperName");
				if (name == null) {
					name = "PageWrapper";
				}
				WOApplication.application().pageWithName(name, context());
				_pageWrapperName = name;
			} catch (Throwable e) {
				_pageWrapperName = "D2WEmptyWrapper";
				log.info("** DirectToWeb using D2WEmptyWrapper, create a PageWrapper component in your project if you need a different layout");
			}
		}

		return _pageWrapperName;
	}
}