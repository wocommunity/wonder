package com.webobjects.appserver._private;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * This class supersedes the WebObjects version to replace references to
 * {@code org.apache.log4j.Logger} with {@code org.slf4j.Logger}.
 */
public class WOCGIFormValues {
	protected String _woURLEncoding;
	protected static WOCGIFormValues _instance;
	public static final String WOURLEncoding = "WOURLEncoding";

	public static class Encoder {
		private static Logger logger = LoggerFactory.getLogger(Encoder.class);

		public String encodeAsCGIFormValues(Map<String, Object> values, String encoding) {
			return encodeAsCGIFormValues(values, encoding, false);
		}

		public String encodeAsCGIFormValues(Map<String, Object> values, String encoding, boolean entityEscapeAmpersand) {
			NSMutableArray<Object> aList = new NSMutableArray<>();

			if (values != null) {
				for (Iterator<String> iterator = values.keySet().iterator(); iterator.hasNext();) {
					String key = iterator.next();
					aList.addObjectsFromArray(encodeObject(values.get(key), encode(key, encoding), encoding));
				}
			}

			if (entityEscapeAmpersand) {
				return aList.componentsJoinedByString("&amp;");
			}

			return aList.componentsJoinedByString("&");
		}

		public NSArray<Object> encodeObject(Object value, String path, String encoding) {
			NSArray<Object> aList = NSArray.emptyArray();

			if (value != null) {
				if (value instanceof NSDictionary) {
					aList = encodeDictionary((NSDictionary) value, path, encoding);
				}
				else if (value instanceof NSArray) {
					aList = encodeArray((NSArray) value, path, encoding);
				}
				else {
					aList = encodeString(value.toString(), path, encoding);
				}
			}

			return aList;
		}

		public NSArray<Object> encodeDictionary(NSDictionary values, String path, String encoding) {
			NSMutableArray<Object> aList = new NSMutableArray<>();

			if (values != null) {
				for (Enumeration<String> enumeration = values.keyEnumerator(); enumeration.hasMoreElements();) {
					String key = enumeration.nextElement();
					String keyPath = ((path != null && path.length() > 0) ? (path + ".") : "") + encode(key, encoding);
					aList.addObjectsFromArray(encodeObject(values.objectForKey(key), keyPath, encoding));
				}
			}

			return aList;
		}

		public NSArray<Object> encodeArray(NSArray values, String path, String encoding) {
			NSMutableArray<Object> aList = new NSMutableArray<>();

			if (values != null) {
				for (Enumeration enumeration = values.objectEnumerator(); enumeration.hasMoreElements();) {
					aList.addObjectsFromArray(encodeObject(enumeration.nextElement(), path, encoding));
				}
			}

			return aList;
		}

		public NSArray<Object> encodeString(String value, String path, String encoding) {
			if (value != null) {
				return new NSArray(path + "=" + encode(value, encoding));
			}

			return NSArray.emptyArray();
		}

		protected String encode(String value, String encoding) {
			try {
				return URLEncoder.encode(value, encoding);
			}
			catch (Exception exception) {
				if (logger.isDebugEnabled()) {
					logger.debug("encode() exception ", exception);
				}
				return value;
			}
		}
	}

	public static class Decoder {
		public NSDictionary<String, NSArray<Object>> decodeCGIFormValues(String value, String encoding) {
			NSMutableDictionary<String, NSMutableArray<Object>> formValues = new NSMutableDictionary<>();
			NSArray<String> aList = NSArray.componentsSeparatedByString(value, "&");

			if (aList != null && aList.count() > 0) {
				for (Iterator<String> iterator1 = aList.iterator(); iterator1.hasNext();) {
					decodeObject(formValues, iterator1.next(), encoding);
				}
			}

			NSMutableDictionary<String, NSArray<Object>> result = new NSMutableDictionary<>();

			for (Iterator<String> iterator = formValues.keySet().iterator(); iterator.hasNext();) {
				String key = iterator.next();
				result.setObjectForKey(((NSMutableArray) formValues.objectForKey(key)).immutableClone(), key);
			}

			return result.immutableClone();
		}

		public void decodeObject(NSMutableDictionary<String, NSMutableArray<Object>> formValues, String encodedValue, String encoding) {
			if (encodedValue.length() > 0) {
				int indexEqual = encodedValue.indexOf("=");
				String encodedKeyPath = getKeyPath(encodedValue, indexEqual);
				String value = getValue(encodedValue, indexEqual);

				setObjectForKeyInDictionary(decode(value, encoding), decode(encodedKeyPath, encoding), formValues);
			}
		}

		protected void setObjectForKeyInDictionary(Object value, String key, NSMutableDictionary<String, NSMutableArray<Object>> dictionary) {
			NSMutableArray<Object> currentValue = dictionary.objectForKey(key);

			if (currentValue == null) {
				currentValue = new NSMutableArray();
				dictionary.setObjectForKey(currentValue, key);
			}

			currentValue.add(value);
		}

		protected String getRootKeyPath(String encodedKeyPath, int indexDot) {
			if (indexDot <= 0) {
				return "";
			}

			return encodedKeyPath.substring(0, indexDot);
		}

		protected String getRemainingKeyPath(String encodedKeyPath, int indexDot) {
			if (indexDot == -1) {
				return encodedKeyPath;
			}

			if (indexDot + 1 < encodedKeyPath.length()) {
				return encodedKeyPath.substring(indexDot + 1);
			}

			return "";
		}

		protected String getKeyPath(String encodedValue, int indexEqual) {
			if (indexEqual <= 0) {
				return "WOIsmapCoords";
			}

			return encodedValue.substring(0, indexEqual);
		}

		protected String getValue(String encodedValue, int indexEqual) {
			if (indexEqual == -1) {
				return encodedValue;
			}

			if (indexEqual + 1 < encodedValue.length()) {
				return encodedValue.substring(indexEqual + 1);
			}

			return "";
		}

		protected String decode(String value, String encoding) {
			try {
				return URLDecoder.decode(value, encoding);
			}
			catch (Exception exception) {
				return value;
			}
		}
	}

	public static WOCGIFormValues getInstance() {
		if (_instance == null) {
			_instance = new WOCGIFormValues();
		}

		return _instance;
	}

	public Encoder encoder() {
		return new Encoder();
	}

	public Decoder decoder() {
		return new Decoder();
	}

	public String encodeAsCGIFormValues(Map<String, Object> values) {
		return encodeAsCGIFormValues(values, false);
	}

	public String encodeAsCGIFormValues(Map<String, Object> values, boolean entityEscapeAmpersand) {
		String encoding = (String) values.get("WOURLEncoding");

		if (encoding == null) {
			encoding = urlEncoding();
		}

		return encoder().encodeAsCGIFormValues(values, encoding, entityEscapeAmpersand);
	}

	public NSDictionary decodeCGIFormValues(String value) {
		return decodeCGIFormValues(value, getWOURLEncoding(value));
	}

	public NSDictionary decodeCGIFormValues(String value, String encoding) {
		return decoder().decodeCGIFormValues(value, encoding);
	}

	public NSDictionary decodeDataFormValues(String value, String encoding) {
		return decoder().decodeCGIFormValues(value, encoding);
	}

	public String getWOURLEncoding(String value) {
		int i = value.indexOf("WOURLEncoding=");

		if (i >= 0) {
			int j = i + "WOURLEncoding".length() + 1;
			int k = value.indexOf('&', j + 1);

			if (k >= 0) {
				return value.substring(j, k);
			}

			return value.substring(j);
		}

		return urlEncoding();
	}

	public void setUrlEncoding(String value) {
		this._woURLEncoding = value;
	}

	public String urlEncoding() {
		return (this._woURLEncoding != null) ? this._woURLEncoding : "UTF-8";
	}
}