package er.rest.example.client;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXNoOpRestDelegate;
import er.rest.ERXRestNameRegistry;
import er.rest.IERXRestDelegate;
import er.rest.format.ERXRestFormat;

public class Client {
	public static void main(String[] args) throws HttpException, IOException {
		String baseURL = "http://localhost:8642/cgi-bin/WebObjects/RESTExample.woa/ra";
		
		ERXRestNameRegistry.registry().setExternalNameForInternalName("Company", "ClientCompany");
		IERXRestDelegate.Factory.setDefaultDelegateClass(ERXNoOpRestDelegate.class);
		IERXRestDelegate.Factory.setDelegateForEntityNamed(ClientCompanyRestDelegate.class, "ClientCompany", ClientCompany.class);

		ERXNoOpRestDelegate delegate = new ERXNoOpRestDelegate();

		ERXRestClient client = new ERXRestClient(baseURL, delegate);
		ClientCompany c = client.objectWithPath("Company/1.json");
		System.out.println("Client.main: single company = " + c);

		Object obj = new ERXRestClient(client.baseURL(), delegate, false).objectWithPath("Pet/1.json");
		System.out.println("Client.main: unknown class = " + obj);

		NSArray<ClientCompany> comps = client.objectWithPath("Company.json", "ClientCompany");
		System.out.println("Client.main: array of companies = " + comps);

		c.setName("New Name");
		client.updateObjectWithPath(c, ERXKeyFilter.filterWithAllRecursive(), "Company/1.json", ERXRestFormat.JSON);

		ClientCompany updatedCompany = client.objectWithPath("Company/1.json");
		System.out.println("Client.main: updated company = " + updatedCompany);
		
		NSMutableDictionary<String, Object> dict = new NSMutableDictionary<String, Object>();
		dict.setObjectForKey("Schrag", "lastName");
		dict.setObjectForKey("Mike", "firstName");
		dict.setObjectForKey(new NSDictionary<String, Object>("true", "nested"), "child");
		dict.setObjectForKey(new NSArray<String>("a", "b"), "array");
		String dictJSON = ERXRestFormat.JSON.toString(dict);
		System.out.println("Client.main: dictionary as JSON " + dictJSON);

		NSArray<?> list = new NSArray<String>("a", "b");
		String arrayJSON = ERXRestFormat.JSON.toString(list);
		System.out.println("Client.main: array as JSON " + arrayJSON);

	}
}
