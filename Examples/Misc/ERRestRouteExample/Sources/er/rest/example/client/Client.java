package er.rest.example.client;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXKeyFilter;
import er.rest.ERXAbstractRestDelegate;
import er.rest.ERXNoOpRestDelegate;
import er.rest.ERXRestClassDescriptionFactory;
import er.rest.ERXRestRequestNode;
import er.rest.IERXRestDelegate;
import er.rest.format.ERXRestFormat;
import er.rest.format.ERXStringBufferRestResponse;

public class Client {
	public static class ERXRestClient {
		private boolean _classDescriptionRequired;

		public ERXRestClient(boolean classDescriptionRequired) {
			setClassDescriptionRequired(classDescriptionRequired);
		}

		public ERXRestClient() {
			this(true);
		}

		public void setClassDescriptionRequired(boolean classDescriptionRequired) {
			_classDescriptionRequired = classDescriptionRequired;
		}

		public boolean isClassDescriptionRequired() {
			return _classDescriptionRequired;
		}

		protected ERXRestRequestNode requestNodeWithMethod(HttpMethodBase method) throws IOException {
			ERXRestFormat format = ERXRestFormat.formatNamed(method.getResponseHeader("Content-Type").getValue());
			ERXRestRequestNode responseNode = format.parser().parseRestRequest(method.getResponseBodyAsString(), format.delegate());
			return responseNode;
		}

		protected Object _objectWithRequestNode(ERXRestRequestNode node, String entityName, IERXRestDelegate delegate) {
			Object obj;
			EOClassDescription classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(entityName);
			if (entityName != null && classDescription == null && !_classDescriptionRequired) {
				obj = node;
			}
			else {
				obj = node.objectWithFilter(entityName, ERXKeyFilter.filterWithAllRecursive(), delegate);
			}
			return obj;
		}

		@SuppressWarnings("unchecked")
		public <T> T objectWithRequestNode(ERXRestRequestNode node, IERXRestDelegate delegate) {
			return (T) _objectWithRequestNode(node, null, delegate);
		}

		@SuppressWarnings("unchecked")
		public <T> T objectWithRequestNode(ERXRestRequestNode node, String entityName, IERXRestDelegate delegate) {
			return (T) _objectWithRequestNode(node, entityName, delegate);
		}

		@SuppressWarnings("unchecked")
		public <T> T objectWithURL(String url, String entityName, IERXRestDelegate delegate) throws HttpException, IOException {
			HttpClient client = new HttpClient();
			GetMethod fetchObjectMethod = new GetMethod(url);
			client.executeMethod(fetchObjectMethod);
			ERXRestRequestNode node = requestNodeWithMethod(fetchObjectMethod);
			return (T) _objectWithRequestNode(node, entityName, delegate);
		}

		@SuppressWarnings("unchecked")
		public <T> T objectWithURL(String url, IERXRestDelegate delegate) throws HttpException, IOException {
			HttpClient client = new HttpClient();
			GetMethod fetchObjectMethod = new GetMethod(url);
			client.executeMethod(fetchObjectMethod);
			ERXRestRequestNode node = requestNodeWithMethod(fetchObjectMethod);
			String type = node.type();
			return (T) _objectWithRequestNode(node, type, delegate);
		}

		public ERXRestRequestNode updateObjectWithURL(Object obj, ERXKeyFilter filter, String url, ERXRestFormat format, IERXRestDelegate delegate) throws HttpException, IOException {
			ERXRestRequestNode node = ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, filter, delegate);
			ERXStringBufferRestResponse response = new ERXStringBufferRestResponse();
			format.writer().appendToResponse(node, response, format.delegate());

			HttpClient client = new HttpClient();
			PutMethod updateObjectMethod = new PutMethod(url);
			updateObjectMethod.setRequestEntity(new StringRequestEntity(response.toString()));
			client.executeMethod(updateObjectMethod);
			return requestNodeWithMethod(updateObjectMethod);
		}

		public ERXRestRequestNode createObjectWithURL(Object obj, ERXKeyFilter filter, String url, ERXRestFormat format, IERXRestDelegate delegate) throws HttpException, IOException {
			ERXRestRequestNode node = ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, filter, delegate);
			ERXStringBufferRestResponse response = new ERXStringBufferRestResponse();
			format.writer().appendToResponse(node, response, format.delegate());

			HttpClient client = new HttpClient();
			PostMethod updateObjectMethod = new PostMethod(url);
			updateObjectMethod.setRequestEntity(new StringRequestEntity(response.toString()));
			client.executeMethod(updateObjectMethod);
			return requestNodeWithMethod(updateObjectMethod);
		}
	}

	public static class ClientCompanyRestDelegate extends ERXAbstractRestDelegate {
		@Override
		public boolean shouldCreateMissingObjects() {
			return true;
		}

		@Override
		protected Object _primaryKeyForObject(EOClassDescription entity, Object obj) {
			return ((Company) obj).getId();
		}

		@Override
		protected boolean _isDelegateForEntity(EOClassDescription entity) {
			return "Company".equals(entity.entityName());
		}

		@Override
		protected Object _fetchObjectOfEntityWithID(EOClassDescription entity, Object id) {
			return null;
		}

		@Override
		protected Object _createObjectOfEntityWithID(EOClassDescription entity, Object id) {
			Company comp = new Company();
			comp.setId(String.valueOf(id));
			return comp;
		}
	}

	public static void main(String[] args) throws HttpException, IOException {
		String baseUrl = "http://localhost:62642/cgi-bin/WebObjects/RESTExample.woa";

		IERXRestDelegate.Factory.setDefaultDelegateClass(ERXNoOpRestDelegate.class);
		IERXRestDelegate.Factory.setDelegateForEntityNamed(ClientCompanyRestDelegate.class, "Company", Company.class);

		ERXNoOpRestDelegate delegate = new ERXNoOpRestDelegate();

		Company c = new ERXRestClient().objectWithURL(baseUrl + "/ra/Company/1.json", delegate);
		System.out.println("Client.main: single company = " + c);

		Object obj = new ERXRestClient(false).objectWithURL(baseUrl + "/ra/Pet/1.json", delegate);
		System.out.println("Client.main: unknown object = " + obj);

		NSArray<Company> comps = new ERXRestClient().objectWithURL(baseUrl + "/ra/Company.json", "Company", delegate);
		System.out.println("Client.main: array of companies = " + comps);

		c.setName("Something");
		new ERXRestClient().updateObjectWithURL(c, ERXKeyFilter.filterWithAllRecursive(), baseUrl + "/ra/Company/1.json", ERXRestFormat.JSON, delegate);

		System.out.println("Client.main: " + c);
	}
}
