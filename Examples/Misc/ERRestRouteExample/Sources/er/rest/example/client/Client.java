package er.rest.example.client;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;

import com.webobjects.foundation.NSArray;

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
	}
}
