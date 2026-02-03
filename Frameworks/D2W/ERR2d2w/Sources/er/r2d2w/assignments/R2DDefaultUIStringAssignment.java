package er.r2d2w.assignments;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.eocontrol.EOKeyValueArchiver;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

import er.directtoweb.assignments.ERDAssignment;
import er.directtoweb.assignments.ERDLocalizableAssignmentInterface;
import er.extensions.localization.ERXLocalizer;


public class R2DDefaultUIStringAssignment extends ERDAssignment implements ERDLocalizableAssignmentInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String ATTRIBUTE_ABBR = "attributeAbbr";
	public static final String TOOLTIP = "tooltip";
	public static final String TABLE_CAPTION = "tableCaption";
	public static final String TABLE_SUMMARY = "tableSummary";
	public static final String DYNAMIC_PAGE_PREFIX = "Page.";
	
	private static final NSArray<String> attributeAbbrDependentKeys = 
		new NSArray<String>(new String[] { D2WModel.PropertyKeyKey, D2WModel.EntityKey });
	private static final NSArray<String> tooltipDependentKeys = 
		new NSArray<String>(new String[] { D2WModel.PropertyKeyKey, D2WModel.EntityKey, "branch" });
	private static final NSArray<String> tableCaptionDependentKeys = 
		new NSArray<String>(new String[] { D2WModel.DynamicPageKey });
	private static final NSArray<String> tableSummaryDependentKeys = 
		new NSArray<String>(new String[] { D2WModel.DynamicPageKey });
	
	private static final NSArray<NSArray<String>> allKeys = 
		new NSArray<NSArray<String>>(attributeAbbrDependentKeys)
		.arrayByAddingObject(tooltipDependentKeys)
		.arrayByAddingObject(tableCaptionDependentKeys)
		.arrayByAddingObject(tableSummaryDependentKeys);
	
	private static final NSDictionary<String, NSArray<String>> dependentKeyDict = 
		new NSDictionary<String, NSArray<String>>(allKeys, new NSArray<String>(
				new String[] {ATTRIBUTE_ABBR,
						TOOLTIP,
						TABLE_CAPTION,
						TABLE_SUMMARY}));

	public R2DDefaultUIStringAssignment(EOKeyValueUnarchiver u) {
		super(u);
	}

	public R2DDefaultUIStringAssignment(String key, Object value) {
		super(key, value);
	}

	public void encodeWithKeyValueArchiver(EOKeyValueArchiver archiver) {
		super.encodeWithKeyValueArchiver(archiver);
	}

	public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver unarchiver) {
		return new R2DDefaultUIStringAssignment(unarchiver);
	}
	
	public NSArray<String> dependentKeys(String keyPath) {
		return dependentKeyDict.get(keyPath);
	}
	
	public String attributeAbbr(D2WContext c) {
		String result = null;
		if(c.propertyKey() != null && c.entity() != null) {
			StringBuilder sb = new StringBuilder(100);
			String key = sb.append(c.entity().name()).append(NSKeyValueCodingAdditions._KeyPathSeparatorChar).append(c.propertyKey()).append(NSKeyValueCodingAdditions._KeyPathSeparatorChar).append(ATTRIBUTE_ABBR).toString();
			result = localizedStringForKey(key, c);
		}
		return result;
	}
	
	public String tooltip(D2WContext c) {
		String result = null;
		String branchName = (String)c.valueForKeyPath("branch.branchName");
		if(branchName != null) {
			StringBuilder sb = new StringBuilder(50);
			String key = sb.append("Branch.").append(branchName).append(NSKeyValueCodingAdditions._KeyPathSeparatorChar).append(TOOLTIP).toString();
			result = localizedStringForKey(key, c);
		} else if(c.propertyKey() != null && c.entity() != null) {
			StringBuilder sb = new StringBuilder(100);
			String key = sb.append(c.entity().name()).append(NSKeyValueCodingAdditions._KeyPathSeparatorChar).append(c.task()).append(NSKeyValueCodingAdditions._KeyPathSeparatorChar).append(c.propertyKey()).append(NSKeyValueCodingAdditions._KeyPathSeparatorChar).append(TOOLTIP).toString();
			result = localizedStringForKey(key, c);
		}
		return result;
	}
	
	public String tableCaption(D2WContext c) {
		String result = null;
		if(c.dynamicPage() != null && c.dynamicPage().length() > 0) {
			StringBuilder sb = new StringBuilder(100);
			sb.append(DYNAMIC_PAGE_PREFIX);
			sb.append(c.dynamicPage());
			sb.append(NSKeyValueCodingAdditions._KeyPathSeparatorChar);
			sb.append(TABLE_CAPTION);
			String key = sb.toString();
			result = localizedStringForKey(key, c);
		}
		if(result == null) {
			result = (String)c.valueForKey("displayNameForEntity");
		}
		return result;
	}
	
	public String tableSummary(D2WContext c) {
		String result = null;
		if(c.dynamicPage() != null && c.dynamicPage().length() > 0) {
			StringBuilder entityNameBuilder = new StringBuilder(100);
			String key = entityNameBuilder.append(DYNAMIC_PAGE_PREFIX).append(c.dynamicPage()).append(NSKeyValueCodingAdditions._KeyPathSeparatorChar).append(TABLE_SUMMARY).toString();
			result = localizedStringForKey(key, c);
		}
		return result;
	}
	
	public String localizedStringForKey(String key, D2WContext c) {
		String template = ERXLocalizer.currentLocalizer().localizedStringForKey(key);
		if(template == null) {
			template = "";
			ERXLocalizer.currentLocalizer().takeValueForKey(template, key);
		}
		if(template.length() == 0) { return null; }
		return ERXLocalizer.currentLocalizer().localizedTemplateStringForKeyWithObject(template, c);
	}
}
