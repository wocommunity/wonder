package er.extensions.components;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang3.CharEncoding;

import com.webobjects.appserver.WOHTTPConnection;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXProperties;

public class ERXGMapUtilities {
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_ACCURACY = "accuracy";
	public static final String KEY_STATUS = "status";

	public static final String[] GEOCODER_RESPONSE_KEYS = new String[] { KEY_STATUS, KEY_ACCURACY, KEY_LATITUDE, KEY_LONGITUDE };

	public static String apiKey() {
		return ERXProperties.stringForKey("ajax.google.maps.apiKey");
	}

	public static void setApiKey(String apiKey) {
		ERXProperties.setStringForKey(apiKey, "ajax.google.maps.apiKey");
	}

	public static NSDictionary resolveAddress(String address) {
		NSDictionary result = null;

		/*
		 * http://www.google.com/apis/maps/documentation/#Geocoding_HTTP_Request
		 * 
		 * To access the Maps API geocoder directly using server-side scripting,
		 * send a request to http://maps.google.com/maps/geo? with the following
		 * parameters in the URI:
		 * 
		 * q -- The address that you want to geocode.
		 * 
		 * key -- Your API key.
		 * 
		 * output -- The format in which the output should be generated. The
		 * options are xml, kml, csv, or json.
		 */

		WOHTTPConnection connection = new WOHTTPConnection("maps.google.com", 80);
		String encodedAddress = "";
		try {
			encodedAddress = URLEncoder.encode(address, CharEncoding.UTF_8);
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = "/maps/geo?q=" + encodedAddress + "&output=csv&key=" + apiKey();
		WORequest request = new WORequest("GET", url, "HTTP/1.0", null, null, null);
		boolean success = connection.sendRequest(request);
		if (success) {

			/*
			 * A reply returned in the csv format consists of four numbers,
			 * separated by commas. The first number is the status code, the
			 * second is the accuracy, the third is the latitude, while the
			 * fourth one is the longitude.
			 */

			String responseText = connection.readResponse().contentString();
			result = new NSDictionary(responseText.split(","), GEOCODER_RESPONSE_KEYS);
		}
		return result;
	}

	public static Coordinate coordinateForAddress(String address) {
		Coordinate result = null;
		if (address != null) {
			try {
				NSDictionary dictionary = resolveAddress(address);
				if ("200".equals(dictionary.valueForKey(KEY_STATUS))) {
					result = new Coordinate(
							Double.parseDouble((String) dictionary.valueForKey(KEY_LATITUDE)), 
							Double.parseDouble((String) dictionary.valueForKey(KEY_LONGITUDE)));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static class Coordinate {
		public static final char UNIT_STATUTE_MILES = 'M';
		public static final char UNIT_NAUTICAL_MILES = 'N';
		public static final char UNIT_KILOMETERS = 'K';
		
		private double latitude, longitude;

		public Coordinate(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}

		public double latitude() {
			return latitude;
		}

		public double longitude() {
			return longitude;
		}

		@Override
		public String toString() {
			return "(longitude: " + longitude + ", latitude: " + latitude + ")";
		}

		public double distanceTo(Coordinate other, char unit) {
			if (other == null) {
				throw new IllegalArgumentException("Other coordinate must not be null.");
			}
			return distance(latitude(), longitude(), other.latitude(), other.longitude(), unit);
		}
		
		/**
		 * Calculate distance between two coordinates.
		 * 
		 * South latitudes are negative, east longitudes are positive.
		 * 
		 * Based on code from http://www.zipcodeworld.com/developers.htm
		 * 
		 * @param lat1
		 *            Latitude of point 1 (in decimal degrees)
		 * @param lon1
		 *            Longitude of point 1 (in decimal degrees)
		 * @param lat2
		 *            Latitude of point 2 (in decimal degrees)
		 * @param lon2
		 *            Longitude of point 2 (in decimal degrees)
		 * @param unit
		 *            one of UNIT_STATUTE_MILES, UNIT_NAUTICAL_MILES or
		 *            UNIT_KILOMETERS
		 * @return distance between the two coordinates
		 */
		public static double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
			double theta = lon1 - lon2;
			double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
			dist = Math.acos(dist);
			dist = rad2deg(dist);
			dist = dist * 60 * 1.1515;
			if (unit == UNIT_KILOMETERS) {
				dist = dist * 1.609344;
			}
			else if (unit == UNIT_NAUTICAL_MILES) {
				dist = dist * 0.8684;
			}
			return (dist);
		}

		private static double deg2rad(double deg) {
			return (deg * Math.PI / 180.0);
		}

		private static double rad2deg(double rad) {
			return (rad * 180 / Math.PI);
		}
	}

}
