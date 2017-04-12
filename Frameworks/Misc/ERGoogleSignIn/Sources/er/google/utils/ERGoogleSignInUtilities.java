package er.google.utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXSystem;

public class ERGoogleSignInUtilities {
	public static String clientID() {
		String id = ERXSystem.getProperty("ga.Client_ID");
		if (ERXStringUtilities.stringIsNullOrEmpty(id)) {
			throw new RuntimeException("Your client ID is not set. Set it in properties with \"ga.Client_ID\"");
		}
		return id;
	}
	
	public static String clientID(String name) {
		if (ERXStringUtilities.stringIsNullOrEmpty(name)) {
			return clientID();
		}
		
		String id = ERXSystem.getProperty("ga." + name + "_Client_ID");
		if (ERXStringUtilities.stringIsNullOrEmpty(id)) {
			throw new RuntimeException("No client ID found with the name \"" + name + "\". Set it properties with \"ga." + name + "_Client_ID\".");
		}
		return id;
	}
	
	public static Collection<String> clientIDs() {
		String clientNames = ERXSystem.getProperty("ga.Client_ID_Names");
		
		if (clientNames == null) {
			return Collections.singletonList(clientID());
		}
		else {
			String[] names = clientNames.split(", |,");
			
			ArrayList<String> result = new ArrayList<>();
			
			for (int i = 0; i < names.length; i++) {
				result.add(clientID(names[i]));
			}
			
			return result;
		}
	}
	
	public static GoogleIdTokenVerifier googleVerifier() {
		HttpTransport transport = new NetHttpTransport();
		JsonFactory jsonFactory = new GsonFactory();
		return new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
				.setAudience(clientIDs()).build();
	}
	
	public static Payload payloadFromToken(String googleToken) throws IOException, GeneralSecurityException {
		return googleVerifier().verify(googleToken).getPayload();
	}
}
