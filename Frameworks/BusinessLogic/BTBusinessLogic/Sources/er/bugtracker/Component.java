/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.bugtracker;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOQualifierEvaluation;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSValidation;

import er.extensions.foundation.ERXArrayUtilities;

public class Component extends _Component {

	public String sortOrder() {
        NSMutableArray array = new NSMutableArray();
        Component p = this;
        while(p != null) {
            array.addObject(p.textDescription());
            p = p.parent();
        }
        return array.valueForKeyPath("@reverse.toString").toString();
	}

    public int level() {
		return level(0);
	}

	public int level(int safe) {
		if (safe > 10)
			return -1;
		Component parent = parent();
		return parent == null ? 0 : 1 + parent.level(safe + 1);
	}

	public String indentedDescription() {
		int level = level();
		StringBuilder sb = new StringBuilder();
		if (level == -1)
			sb.append("***");
		else
			for (int i = 0; i < level(); i++)
				sb.append('-');
		sb.append(valueForKey("textDescription"));
		return sb.toString();
	}

	public Object validateParent(Component newParent) {
		if (!okToSetParent(this, newParent))
			throw new NSValidation.ValidationException("Sorry: the parent-child relationship you are setting would create a cycle");
		return null;
	}

	public boolean okToSetParent(Component child, Component parent) {
		return parent == null ? true : okToSetParent(child, (Component) parent.valueForKey("parent"));
	}

	public static class ComponentClazz extends _ComponentClazz {

		private NSMutableDictionary _cachedComponentsByGlobalID;
		private NSMutableArray _cachedComponents;

		public synchronized NSArray orderedComponents(EOEditingContext ec) {
			NSMutableArray result = new NSMutableArray();
			if (_cachedComponentsByGlobalID == null) {
                _cachedComponents = new NSMutableArray();
                _cachedComponentsByGlobalID = new NSMutableDictionary();
				addChildrenOfComponentToArray(null, result, ec);
				int level = 0;
				for (Enumeration e = result.objectEnumerator(); e.hasMoreElements();) {
					Component component = (Component) e.nextElement();
					String sortOrder = (level < 10 ? "0" : "") + (level);
					_cachedComponentsByGlobalID.setObjectForKey(sortOrder, component.permanentGlobalID());
					_cachedComponents.addObject(component.permanentGlobalID());
					level++;
				}
			}
			result.removeAllObjects();
			for (Enumeration e = _cachedComponents.objectEnumerator(); e.hasMoreElements();) {
				EOGlobalID gid = (EOGlobalID) e.nextElement();
				Component component = (Component) ec.faultForGlobalID(gid, ec);
				if(component != null) {
					result.addObject(component);
				} else {
					log.error("No object: " + gid);
				}
			}
			return result;
		}

		private static NSArray DESCRIPTION_SORT = new NSArray(EOSortOrdering.sortOrderingWithKey("textDescription", EOSortOrdering.CompareAscending));

		public void addChildrenOfComponentToArray(Component c, NSMutableArray a, EOEditingContext ec) {
			NSArray children = c != null ? (NSArray) c.valueForKey("children") : EOUtilities.objectsMatchingKeyAndValue(ec, "Component", "parent", NSKeyValueCoding.NullValue);
			children = EOSortOrdering.sortedArrayUsingKeyOrderArray(children, DESCRIPTION_SORT);
			for (Enumeration e = children.objectEnumerator(); e.hasMoreElements();) {
				Component child = (Component) e.nextElement();
				a.addObject(child);
				addChildrenOfComponentToArray(child, a, ec);
			}
		}
	}

	public static ComponentClazz clazz = new ComponentClazz();

    public NSArray openBugs() {
        return ERXArrayUtilities.filteredArrayWithQualifierEvaluation(bugs(), new EOQualifierEvaluation() {
            public boolean evaluateWithObject(Object object) {
                State state = ((Bug)object).state();
                return !(state.equals(State.CLOSED) || state.equals(State.DOCUMENT));
            }
            
        });
    }

    public NSArray openRequirements() {
        return ERXArrayUtilities.filteredArrayWithQualifierEvaluation(requirements(), new EOQualifierEvaluation() {
            public boolean evaluateWithObject(Object object) {
                State state = ((Bug)object).state();
                return !(state.equals(State.CLOSED) || state.equals(State.DOCUMENT));
            }
            
        });
    }
}
