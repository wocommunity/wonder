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
		String baseUrl = "http://localhost:8642/cgi-bin/WebObjects/RESTExample.woa";

		ERXRestNameRegistry.registry().setExternalNameForInternalName("Company", "ClientCompany");
		IERXRestDelegate.Factory.setDefaultDelegateClass(ERXNoOpRestDelegate.class);
		IERXRestDelegate.Factory.setDelegateForEntityNamed(ClientCompanyRestDelegate.class, "ClientCompany", ClientCompany.class);

		ERXNoOpRestDelegate delegate = new ERXNoOpRestDelegate();

		ClientCompany c = new ERXRestClient().objectWithURL(baseUrl + "/ra/Company/1.json", delegate);
		System.out.println("Client.main: single company = " + c);

		Object obj = new ERXRestClient(false).objectWithURL(baseUrl + "/ra/Pet/1.json", delegate);
		System.out.println("Client.main: unknown class = " + obj);

		NSArray<ClientCompany> comps = new ERXRestClient().objectWithURL(baseUrl + "/ra/Company.json", "ClientCompany", delegate);
		System.out.println("Client.main: array of companies = " + comps);

		c.setName("New Name");
		new ERXRestClient().updateObjectWithURL(c, ERXKeyFilter.filterWithAllRecursive(), baseUrl + "/ra/Company/1.json", ERXRestFormat.JSON, delegate);

		ClientCompany updatedCompany = new ERXRestClient().objectWithURL(baseUrl + "/ra/Company/1.json", delegate);
		System.out.println("Client.main: updated company = " + updatedCompany);
	}
}
