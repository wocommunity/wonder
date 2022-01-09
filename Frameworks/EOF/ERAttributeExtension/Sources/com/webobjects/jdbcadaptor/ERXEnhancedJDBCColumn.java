package com.webobjects.jdbcadaptor;


import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import er.extensions.jdbc.ERXJDBCAdaptor.Channel;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class extends the {@code ERXJDBCColumn} to add support for custom types.
 */
public class ERXEnhancedJDBCColumn extends ERXJDBCColumn {
	private static final Logger log = Logger.getLogger(ERXEnhancedJDBCColumn.class);

	public ERXEnhancedJDBCColumn(EOAttribute attribute, JDBCChannel channel, int column, ResultSet rs) {
		super(attribute, channel, column, rs);
	}

	public ERXEnhancedJDBCColumn(Channel aChannel) {
		super(aChannel);
	}

	@Override
	Object _fetchValue(boolean flag) {
		if (_rs == null || _column < 1) {
			throw new JDBCAdaptorException(" *** JDBCColumn : trying to fetchValue on a null ResultSet [" + _rs + "] or unknow col [" + _column + "]!!", null);
		}
		/*
		 * Add support for custom date types
		 */
		if(_adaptorValueType == EOAttribute.AdaptorDateType) {
			Object obj = null;
			try {
				switch(_valueType) {
				case EOAttribute._VTDate:
				case EOAttribute._VTCoerceDate:
					obj = _rs.getDate(_column);
					break;
				case EOAttribute._VTTime:
					obj = _rs.getTime(_column);
					break;
				case EOAttribute._VTTimestamp:
					obj = _rs.getTimestamp(_column);
					break;
				default:
					obj = _rs.getObject(_column);
					break;
				}
				if(_rs.wasNull() || obj == null) {
					return NSKeyValueCoding.NullValue;
				}
			} catch(SQLException e) {
				throw new JDBCAdaptorException(e);
			}
			
			return _attribute.newValueForDate(obj);
		}
		/*
		 * Add support for custom number types
		 */
		if (_adaptorValueType == EOAttribute.AdaptorNumberType && _customType) {
			Object value = super._fetchValue(flag);
			if (value == NSKeyValueCoding.NullValue || value == null) {
				return value;
			}
			return _attribute.newValueForNumber(value);
		}
		try {
			return super._fetchValue(flag);
		} catch(NSForwardException ex) {
			log.error("There's an error with this attribute: " + _attribute);
			throw ex;
		}
	}
}
