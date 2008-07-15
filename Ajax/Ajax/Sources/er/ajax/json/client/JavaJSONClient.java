package er.ajax.json.client;

import java.lang.reflect.Field;

import org.jabsorb.JSONSerializer;
import org.jabsorb.client.Client;
import org.jabsorb.client.HTTPSession;
import org.jabsorb.client.TransportRegistry;

import er.ajax.json.serializer.ERXConstantSerializer;
import er.ajax.json.serializer.JSONEnterpriseObjectSerializer;
import er.ajax.json.serializer.NSArraySerializer;
import er.ajax.json.serializer.NSDataSerializer;
import er.ajax.json.serializer.NSDictionarySerializer;
import er.ajax.json.serializer.NSTimestampSerializer;

/**
 * If you are trying to communicate with a WO JSON server from another Java app, JavaJSONClient provides a factory for
 * creating the appropiate JSON Client class.
 * 
 * @author mschrag
 */
public class JavaJSONClient {
	/**
	 * Creates and returns a JSON Client.
	 * 
	 * @param jsonUrl
	 *            the JSON service URL
	 * @param useHttpClient
	 *            if true, Commons HTTPClient will be used instead of URLConnection (much better, but requires
	 *            HttpClient)
	 * @return the JSON client
	 * @throws Exception
	 *             if the client creation fails
	 */
	public static Client create(String jsonUrl, boolean useHttpClient) throws Exception {
		if (useHttpClient) {
			HTTPSession.register(TransportRegistry.i());
		}
		Client client = new Client(TransportRegistry.i().createSession(jsonUrl));
		Field serializerField = client.getClass().getDeclaredField("serializer");
		serializerField.setAccessible(true);
		JSONSerializer serializer = (JSONSerializer) serializerField.get(client);
		serializer.registerSerializer(new JSONEnterpriseObjectSerializer());
		serializer.registerSerializer(new NSArraySerializer());
		serializer.registerSerializer(new NSDictionarySerializer());
		serializer.registerSerializer(new NSTimestampSerializer());
		serializer.registerSerializer(new NSDataSerializer());
		serializer.registerSerializer(new ERXConstantSerializer());
		return client;
	}
}
