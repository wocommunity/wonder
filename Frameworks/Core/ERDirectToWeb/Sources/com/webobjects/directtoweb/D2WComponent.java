package com.webobjects.directtoweb;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Vector;

import com.apple.client.directtoweb.common.D2WKeyValueArchiver;
import com.apple.client.directtoweb.common.PropertyListUtilities;
import com.apple.client.directtoweb.common.Settings;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORedirect;
import com.webobjects.directtoweb.generation.DTWGeneration;
import com.webobjects.directtoweb.generation.DTWTemplate;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXEOSerializationUtilities;

public class D2WComponent extends WOComponent implements DTWGeneration {
	/**
	 * Do I need to update serialVersionUID? See section 5.6 <cite>Type Changes
	 * Affecting Serialization</cite> on page 51 of the <a
	 * href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object
	 * Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String currentObjectKey = "currentObject";

	protected D2WContext _localContext;
	protected EOEnterpriseObject _eo;

	private String _entitiesString;
	private String _dynamicPagesString;
	private boolean _readOnlyComputed;
	private boolean _entityIsReadOnly;
	private String _tasksString;

	private transient Settings _settings;
	private transient String _settingsString;

	public D2WComponent(WOContext aContext) {
		super(aContext);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		_entitiesString = (String) in.readObject();
		_dynamicPagesString = (String) in.readObject();
		_readOnlyComputed = in.readBoolean();
		_entityIsReadOnly = in.readBoolean();
		_tasksString = (String) in.readObject();
		_eo = ERXEOSerializationUtilities.readEO(in);
		_localContext = (D2WContext) in.readObject();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(_entitiesString);
		out.writeObject(_dynamicPagesString);
		out.writeBoolean(_readOnlyComputed);
		out.writeBoolean(_entityIsReadOnly);
		out.writeObject(_tasksString);
		ERXEOSerializationUtilities.writeEO(out, _eo);
		out.writeObject(_localContext);
	}

	/**
	 * @deprecated use {@link #d2wContext()}
	 */
    @Deprecated
	public D2WContext localContext() {
		return _localContext;
	}

	public D2WContext d2wContext() {
		return _localContext;
	}

	public void setLocalContext(D2WContext newValue) {
		if (_localContext == null && newValue != null)
			_localContext = newValue;
	}

	public static String keyForGenerationReplacementForVariableNamed(String variableName) {
		return (new StringBuilder()).append("replacementFor").append(Services.capitalize(variableName)).toString();
	}

	protected String generationReplacementFor(String variableName) {
		String valueFromContext = (String) d2wContext().valueForKey(
				keyForGenerationReplacementForVariableNamed(variableName));
		return valueFromContext == null ? variableName : valueFromContext;
	}

	public String generationReplacementForCurrentObject() {
		return generationReplacementFor(currentObjectKey);
	}

	public String propertyKey() {
		return (String) d2wContext().valueForKey(D2WModel.PropertyKeyKey);
	}

	public void setPropertyKey(String newValue) {
		d2wContext().takeValueForKey(newValue, D2WModel.PropertyKeyKey);
	}

	public EOEnterpriseObject object() {
		return _eo;
	}

	public void setObject(EOEnterpriseObject eo) {
		_eo = eo;
	}

	public boolean isEntityReadOnly() {
		if (!_readOnlyComputed) {
			_entityIsReadOnly = isEntityReadOnly(entity());
		}
		return _entityIsReadOnly;
	}

	public boolean isEntityReadOnly(EOEntity e) {
		return e.isReadOnly() || D2WUtils.readOnlyEntityNames(d2wContext()).containsObject(e.name());
	}

	public EOAttribute attribute() {
		return d2wContext().attribute();
	}

	public EORelationship relationship() {
		return d2wContext().relationship();
	}

	public Object property() {
		Object a = attribute();
		return a == null ? relationship() : null;
	}

	public boolean objectPropertyValueIsNonNull() {
		return objectPropertyValue() != null;
	}

	public Object objectPropertyValue() {
		String propertyKey = propertyKey();
		EOEnterpriseObject eo = object();
		return eo == null || propertyKey == null ? null : eo.valueForKeyPath(propertyKey);
	}

	public String keyWhenRelationship() {
		return (String) d2wContext().valueForKey(D2WModel.KeyWhenRelationshipKey);
	}

	public String target() {
		return (String) d2wContext().valueForKey(D2WModel.TargetKey);
	}

	public String formatter() {
		return (String) d2wContext().valueForKey(D2WModel.FormatterKey);
	}

	public String length() {
		Object result = d2wContext().valueForKey(D2WModel.LengthKey);
		return result == null ? null : result.toString();
	}

	public Integer allowCollapsing() {
		return (Integer) d2wContext().valueForKey(D2WModel.AllowCollapsingKey);
	}

	public String pageTitle() {
		if (context() == null) {
			return "DirectToWeb";
		} else {
			String title = (new StringBuilder()).append(application().name()).append(": ")
					.append(Services.capitalize(D2W.taskFromPage(context().page()))).toString();
			EOEntity e = D2W.entityFromPage(context().page());
			return (new StringBuilder()).append(title).append(" ").append(e == null ? "" : e.name()).toString();
		}
	}

	public boolean hasEntity() {
		return entity() != null;
	}

	public String task() {
		return (String) d2wContext().valueForKey(D2WModel.TaskKey);
	}

	public void setTask(String newValue) {
		d2wContext().takeValueForKey(newValue, D2WModel.TaskKey);
	}

	public EOEntity entity() {
		return (EOEntity) d2wContext().valueForKey(D2WModel.EntityKey);
	}

	public void setEntity(EOEntity newValue) {
		d2wContext().takeValueForKey(newValue, D2WModel.EntityKey);
	}

	public String entityName() {
		EOEntity e = entity();
		return e == null ? null : e.name();
	}

	public void setEntityName(String newValue) {
		setEntity(EOModelGroup.defaultGroup().entityNamed(newValue));
	}

	public String propertyValueClassName() {
		if (attribute() != null)
			return attribute().className();
		else
			return null;
	}

	public String displayNameForProperty() {
		return (String) d2wContext().valueForKey(D2WModel.DisplayNameForPropertyKey);
	}

	public boolean isPropertyAnAttribute() {
		return attribute() != null;
	}

	public boolean showBanner() {
		Object b = d2wContext().valueForKey(D2WModel.ShowBannerKey);
		return b == null || b.equals(D2WModel.One);
	}

	public boolean isNotBoldAsBoolean() {
		Object b = d2wContext().valueForKey(D2WModel.BoldKey);
		return b == null || b.equals(D2WModel.Zero);
	}

	public boolean isNotItalicAsBoolean() {
		Object b = d2wContext().valueForKey(D2WModel.ItalicKey);
		return b == null || b.equals(D2WModel.Zero);
	}

	public String color() {
		return (String) d2wContext().valueForKey(D2WModel.ColorKey);
	}

	public boolean hasNoColor() {
		return color() == null;
	}

	public boolean hasCustomKey(String customKey) {
		return D2WUtils.dataTypeForCustomKeyAndEntity(customKey, entity()) != null;
	}

	public NSArray<?> displayPropertyKeys() {
		NSArray<?> originalValue = (NSArray<?>) d2wContext().valueForKey(D2WModel.DisplayPropertyKeysKey);
		return originalValue;
	}

	public String backgroundColorForPage() {
		return (String) d2wContext().valueForKey(D2WModel.BackgroundColorForPageKey);
	}

	public String backgroundColorForHeaderRow() {
		return D2WUtils.darker(backgroundColorForTable());
	}

	public String backgroundColorForTable() {
		return (String) d2wContext().valueForKey(D2WModel.BackgroundColorForTableKey);
	}

	public String backgroundColorForTableDark() {
		return D2WUtils.darker(backgroundColorForTable());
	}

	public String backgroundColorForTableLight() {
		return D2WUtils.lighter(backgroundColorForTable());
	}

	public String defaultRowspan() {
		return String.valueOf(displayPropertyKeys().count() + 2);
	}

	public String d2wContextVisibleEntityNamesCountPlus1() {
		/*
		 * Seems odd that we add 2, given the method name... not that anyone
		 * uses this method anyway.
		 */
		return String.valueOf(visibleEntityNames().count() + 2);
	}

	public String displayNameForKeyWhenRelationship() {
		String result = null;
		if (relationship() != null)
			if (!d2wContext().propertyKeyIsKeyPath()) {
				EOEntity originalEntity = entity();
				String originalPropertyKey = propertyKey();
				EOEntity destinationEntity = relationship().destinationEntity();
				setPropertyKey(d2wContext().keyWhenRelationship());
				setEntity(destinationEntity);
				result = displayNameForProperty();
				setEntity(originalEntity);
				setPropertyKey(originalPropertyKey);
			} else {
				result = d2wContext().keyWhenRelationship();
			}
		return result;
	}

	public NSArray<String> visibleEntityNames() {
		return D2WUtils.visibleEntityNames(d2wContext());
	}

	public boolean isEditing() {
		return d2wContext().task().equals("edit");
	}

	public String submitActionName() {
		return isEditing() ? "" : null;
	}

	public WOComponent logout() {
		WOComponent redirectPage = pageWithName(WORedirect.class.getSimpleName());
		((WORedirect) redirectPage).setUrl(D2WUtils.homeHrefInContext(context()));
		session().terminate();
		return redirectPage;
	}

	public WOComponent homeClicked() {
		return D2W.factory().defaultPage(session());
	}

	public String homeHref() {
		return D2WUtils.homeHrefInContext(context());
	}

	public String resourcePathURL() {
		return D2WUtils.resourcePathURL(context());
	}

	public void setResourcePathURL(String s) {
	}

	public String tasks() {
		if (_tasksString == null) {
			Vector<String> result = new Vector<String>(16);
			result.addElement("*all*");
			String task;
			for (Enumeration<String> e = D2WModel.defaultModel().tasks(); e.hasMoreElements(); result.addElement(task)) {
				task = e.nextElement();
			}

			_tasksString = PropertyListUtilities.stringFromPropertyList(result);
		}
		return _tasksString;
	}

	public void setTasks(String s) {
	}

	public String dynamicPages() {
		if (_dynamicPagesString == null)
			_dynamicPagesString = PropertyListUtilities.stringFromPropertyList(D2WModel.defaultModel().dynamicPages());
		return _dynamicPagesString;
	}

	public void setDynamicPages(String s) {
	}

	public String allEntities() {
		if (_entitiesString == null) {
			NSArray<EOEntity> entityObjects = D2WUtils.allEntities();
			Vector<String> entityNames = new Vector<String>(entityObjects.count());
			entityNames.addElement("*all*");
			EOEntity entity;
			Enumeration<EOEntity> e;
			for (e = entityObjects.objectEnumerator(); e.hasMoreElements(); entityNames.addElement(entity.name())) {
				entity = (EOEntity) e.nextElement();
			}
			_entitiesString = PropertyListUtilities.stringFromPropertyList(entityNames);
		}
		return _entitiesString;
	}

	public void setEntities(String s) {
	}

	public String currentSettings() {
		if (_settings == null) {
			_settings = new ServerSideSettings(d2wContext());
			D2WKeyValueArchiver archiver = new D2WKeyValueArchiver(ServerArchivingDelegate.instance);
			archiver.encodeObject(_settings);
			_settingsString = archiver.persistentString();
		}
		return _settingsString;
	}

	public void setCurrentSettings(String s) {
	}

	public int applicationPort() {
		return D2WUtils.applicationPort();
	}

	public int assistantPort() {
		return D2W.factory().assistantPort();
	}

	public String sessionID() {
		return session().sessionID();
	}

	public WOComponent showWebAssistant() {
		return D2W.factory().webAssistantInContext(context());
	}

	public boolean isWebAssistantConnected() {
		return D2W.factory().isWebAssistantConnected();
	}

	public boolean isWebAssistantActive() {
		return D2W.factory().isWebAssistantActive();
	}

	public boolean isWebAssistantEnabled() {
		return D2W.factory().isWebAssistantEnabled();
	}

	/**
	 * @deprecated use {@link D2W#isWebAssistantEnabled()}
	 */
    @Deprecated
	public boolean isLiveAssistantEnabled() {
		return D2W.factory().isWebAssistantEnabled();
	}

	public String currentUrl() {
		String urlForPage = D2W.factory().urlForPage(context().page());
		urlForPage = urlForPage == null ? D2WUtils.urlFromUrlAndFormValues(context().request().uri(), context()
				.request().formValues()) : urlForPage;
		return urlForPage;
	}

	public String lastUrl() {
		return D2W.factory().lastUrl();
	}

	public WOAssociation replacementAssociationForAssociation(WOAssociation oldAssociation, String oldBinding,
			DTWTemplate aTemplate, WOContext aContext) {
		if (oldBinding.equals(D2WModel.FormatterKey) || oldBinding.equals(D2WModel.LengthKey)
				|| oldBinding.equals("uiStyle") || oldBinding.equals(D2WModel.PropertyKeyKey)
				|| oldBinding.equals("helpString") || oldBinding.equals("backgroundColorForHeaderRow")
				|| oldBinding.equals("backgroundColorForTableDark")
				|| oldBinding.equals("backgroundColorForTableLight") || oldBinding.equals("bannerFileName")
				|| oldBinding.equals("selectButton") || oldBinding.equals("submitActionName")
				|| oldBinding.equals("entity.name"))
			return WOAssociation.associationWithValue(oldAssociation.valueInComponent(this));
		if (oldBinding.equals("d2wContext.allowCollapsing"))
			return WOAssociation.associationWithValue(D2WModel.One.equals(allowCollapsing()) ? "YES" : "NO");
		if (oldBinding.equals("objectPropertyValue")) {
			WOAssociation newAssociation = WOAssociation.associationWithKeyPath((new StringBuilder())
					.append(generationReplacementFor(currentObjectKey)).append(".").append(propertyKey()).toString());
			return newAssociation;
		}
		if (oldBinding.equals(D2WModel.TargetKey)) {
			String target = target();
			if (target != null && !target.equals(""))
				return WOAssociation.associationWithValue(target);
			else
				return null;
		}
		if (oldBinding.startsWith("d2wContext."))
			return WOAssociation.associationWithValue(oldAssociation.valueInComponent(this));
		else
			return oldAssociation;
	}
}
