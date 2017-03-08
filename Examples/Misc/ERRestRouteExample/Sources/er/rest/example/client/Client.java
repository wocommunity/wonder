package er.rest.example.client;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXNoOpRestDelegate;
import er.rest.ERXRestContext;
import er.rest.ERXRestNameRegistry;
import er.rest.ERXRestRequestNode;
import er.rest.IERXRestDelegate;
import er.rest.format.ERXRestFormat;

public class Client {
	public static void main(String[] args) throws HttpException, IOException {
		String baseURL = "http://localhost:8642/cgi-bin/WebObjects/RESTExample.woa/ra";
		
		ERXRestNameRegistry.registry().setExternalNameForInternalName("Company", "ClientCompany");
		IERXRestDelegate.Factory.setDefaultDelegate(new ERXNoOpRestDelegate());
		IERXRestDelegate.Factory.setDelegateForEntityNamed(new ClientCompanyRestDelegate(), "ClientCompany", ClientCompany.class);

		ERXRestClient client = new ERXRestClient(baseURL, new ERXRestContext());
		ClientCompany c = client.objectWithPath("Company/1.json");
		System.out.println("Client.main: single company = " + c);

		Object obj = new ERXRestClient(client.baseURL(), new ERXRestContext(), false).objectWithPath("Pet/1.json");
		System.out.println("Client.main: unknown class = " + obj);

		NSArray<ClientCompany> comps = client.objectWithPath("Company.json", "ClientCompany");
		System.out.println("Client.main: array of companies = " + comps);

		c.setName("New Name");
		client.updateObjectWithPath(c, ERXKeyFilter.filterWithAllRecursive(), "Company/1.json", ERXRestFormat.json());

		ClientCompany updatedCompany = client.objectWithPath("Company/1.json");
		System.out.println("Client.main: updated company = " + updatedCompany);
		
		NSMutableDictionary<String, Object> dict = new NSMutableDictionary<>();
		dict.setObjectForKey("Schrag", "lastName");
		dict.setObjectForKey("Mike", "firstName");
		dict.setObjectForKey(new NSDictionary<String, Object>("true", "nested"), "child");
		dict.setObjectForKey(new NSArray<String>("a", "b"), "array");
		String dictJSON = ERXRestFormat.json().toString(dict);
		System.out.println("Client.main: dictionary as JSON " + dictJSON);

		NSArray<?> list = new NSArray<String>("a", "b");
		String arrayJSON = ERXRestFormat.json().toString(list);
		System.out.println("Client.main: array as JSON " + arrayJSON);

		ClientCompany newCompany = new ClientCompany();
		newCompany.setName("Peters Pickles");
		ERXRestRequestNode node = client.createObjectWithPath(newCompany, ERXKeyFilter.filterWithAllRecursive(), "Company.json", ERXRestFormat.json());
		System.out.println("Client.main: The newly created company is: " + node.toString(ERXRestFormat.json(), new ERXRestContext()));
	}
}
