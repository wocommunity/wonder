package er.neo4jadaptor.query.neo4j_by_pk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;

import er.extensions.eof.qualifiers.ERXInQualifier;
import er.neo4jadaptor.ersatz.webobjects.NSTranslator;

public class ValueMap {
	private final Map<EOAttribute, List<Object>> map = new HashMap<EOAttribute, List<Object>>();
	
	public ValueMap(EOEntity entity, EOQualifier q) {
		collectPossibleValues(entity, q);
	}

	private void collectPossibleValues(EOEntity entity, EOQualifier q) {
		if (q == null) {
			return;
		}
		if (q instanceof EOKeyValueQualifier) {
			EOKeyValueQualifier kvQualifier = (EOKeyValueQualifier) q;
			
			if (kvQualifier.selector().equals(EOKeyValueQualifier.QualifierOperatorEqual)) {
				String key = kvQualifier.key();
				EOAttribute att = entity.attributeNamed(key);
				
				if (att != null) {
					if (q instanceof ERXInQualifier) {
						for (Object v : ((ERXInQualifier) q).values()) {
							Object value = NSTranslator.instance.toNeutralValue(v, att);
							
							add(att, value);							
						}
					} else {
						Object value = NSTranslator.instance.toNeutralValue(kvQualifier.value(), att);
					
						add(att, value);
					}
				}
			}
			return;
		}
		if (q instanceof EOAndQualifier) {
			for (EOQualifier qp : ((EOAndQualifier) q).qualifiers()) {
				collectPossibleValues(entity, qp);
			}
		}
		if (q instanceof EOOrQualifier) {
			for (EOQualifier qp : ((EOOrQualifier) q).qualifiers()) {
				collectPossibleValues(entity, qp);
			}
		}
	}
	
	private void add(EOAttribute att, Object value) {
		List<Object> list = map.get(att);
		
		if (list == null) {
			list = new ArrayList<>();
			map.put(att, list);
		}
		list.add(value);
	}
	
	public List<Object> getValuesForAttribute(EOAttribute att) {
		return map.get(att);
	}
	
	public Collection<EOAttribute> getAttributes() {
		return map.keySet();
	}
	
	public EOAttribute getMostFrequentAttribute() {
		Map.Entry<EOAttribute, List<Object>> entry = null;
		
		for (Map.Entry<EOAttribute, List<Object>> e : map.entrySet()) {
			if (entry == null) {
				entry = e;
			} else {
				if (e.getValue().size() > entry.getValue().size()) {
					entry = e;
				}
			}
		}
		if (entry == null) {
			return null;
		} else {
			return entry.getKey();
		}
	}
}
