package er.rest.example.client;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.webobjects.eocontrol.EOClassDescription;

import er.extensions.eof.ERXKeyFilter;
import er.extensions.foundation.ERXMutableURL;
import er.rest.ERXRestClassDescriptionFactory;
import er.rest.ERXRestRequestNode;
import er.rest.IERXRestDelegate;
import er.rest.format.ERXRestFormat;
import er.rest.format.ERXStringBufferRestResponse;

public class ERXRestClient {
	private String _baseURL;
	private IERXRestDelegate _delegate;
	private boolean _classDescriptionRequired;

	public ERXRestClient(String baseURL, IERXRestDelegate delegate, boolean classDescriptionRequired) {
		_baseURL = baseURL;
		_delegate = delegate;
		setClassDescriptionRequired(classDescriptionRequired);
	}

	public ERXRestClient(String baseURL, IERXRestDelegate delegate) {
		this(baseURL, delegate, true);
	}

	public void setBaseURL(String baseURL) {
		_baseURL = baseURL;
	}

	public String baseURL() {
		return _baseURL;
	}

	public void setDelegate(IERXRestDelegate delegate) {
		_delegate = delegate;
	}

	public IERXRestDelegate delegate() {
		return _delegate;
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

	protected Object _objectWithRequestNode(ERXRestRequestNode node, String entityName) {
		Object obj;
		EOClassDescription classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(entityName);
		if (entityName != null && classDescription == null && !_classDescriptionRequired) {
			obj = node;
		}
		else {
			obj = node.objectWithFilter(entityName, ERXKeyFilter.filterWithAllRecursive(), _delegate);
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	public <T> T objectWithRequestNode(ERXRestRequestNode node) {
		return (T) _objectWithRequestNode(node, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T objectWithRequestNode(ERXRestRequestNode node, String entityName) {
		return (T) _objectWithRequestNode(node, entityName);
	}

	protected String path(String entityName, String id, String action, ERXRestFormat format) {
		StringBuffer sb = new StringBuffer();
		sb.append(entityName);
		if (id != null) {
			sb.append("/");
			sb.append(id);
		}
		if (action != null) {
			sb.append("/");
			sb.append(action);
		}
		if (format != null) {
			sb.append(".");
			sb.append(format.name());
		}
		return sb.toString();
	}

	public <T> T object(String entityName, String id, String action, ERXRestFormat format) throws HttpException, IOException {
		return objectWithPath(path(entityName, id, action, format));
	}

	@SuppressWarnings("unchecked")
	public <T> T objectWithPath(String path, String entityName) throws HttpException, IOException {
		HttpClient client = new HttpClient();
		GetMethod fetchObjectMethod = new GetMethod(new ERXMutableURL(_baseURL).appendPath(path).toExternalForm());
		client.executeMethod(fetchObjectMethod);
		ERXRestRequestNode node = requestNodeWithMethod(fetchObjectMethod);
		return (T) _objectWithRequestNode(node, entityName);
	}

	@SuppressWarnings("unchecked")
	public <T> T objectWithPath(String path) throws HttpException, IOException {
		HttpClient client = new HttpClient();
		GetMethod fetchObjectMethod = new GetMethod(new ERXMutableURL(_baseURL).appendPath(path).toExternalForm());
		client.executeMethod(fetchObjectMethod);
		ERXRestRequestNode node = requestNodeWithMethod(fetchObjectMethod);
		String type = node.type();
		return (T) _objectWithRequestNode(node, type);
	}

	public ERXRestRequestNode update(Object obj, ERXKeyFilter filter, String entityName, String id, String action, ERXRestFormat format) throws HttpException, IOException {
		return updateObjectWithPath(obj, filter, path(entityName, id, action, format), format);
	}

	public ERXRestRequestNode updateObjectWithPath(Object obj, ERXKeyFilter filter, String path, ERXRestFormat format) throws HttpException, IOException {
		ERXRestRequestNode node = ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, filter, _delegate);
		ERXStringBufferRestResponse response = new ERXStringBufferRestResponse();
		format.writer().appendToResponse(node, response, format.delegate());

		HttpClient client = new HttpClient();
		PutMethod updateObjectMethod = new PutMethod(new ERXMutableURL(_baseURL).appendPath(path).toExternalForm());
		updateObjectMethod.setRequestEntity(new StringRequestEntity(response.toString()));
		client.executeMethod(updateObjectMethod);
		return requestNodeWithMethod(updateObjectMethod);
	}

	public ERXRestRequestNode create(Object obj, ERXKeyFilter filter, String entityName, String id, String action, ERXRestFormat format) throws HttpException, IOException {
		return createObjectWithPath(obj, filter, path(entityName, id, action, format), format);
	}

	public ERXRestRequestNode createObjectWithPath(Object obj, ERXKeyFilter filter, String path, ERXRestFormat format) throws HttpException, IOException {
		ERXRestRequestNode node = ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, filter, _delegate);
		ERXStringBufferRestResponse response = new ERXStringBufferRestResponse();
		format.writer().appendToResponse(node, response, format.delegate());

		HttpClient client = new HttpClient();
		PostMethod updateObjectMethod = new PostMethod(new ERXMutableURL(_baseURL).appendPath(path).toExternalForm());
		updateObjectMethod.setRequestEntity(new StringRequestEntity(response.toString()));
		client.executeMethod(updateObjectMethod);
		return requestNodeWithMethod(updateObjectMethod);
	}

	public ERXRestRequestNode delete(Object obj, String entityName, String id, String action, ERXRestFormat format) throws HttpException, IOException {
		return deleteObjectWithPath(obj, path(entityName, id, action, format), format);
	}

	public ERXRestRequestNode deleteObjectWithPath(Object obj, String path, ERXRestFormat format) throws HttpException, IOException {
		ERXRestRequestNode node = ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, ERXKeyFilter.filterWithNone(), _delegate);

		HttpClient client = new HttpClient();
		DeleteMethod deleteObjectMethod = new DeleteMethod(new ERXMutableURL(_baseURL).appendPath(path).toExternalForm());
		client.executeMethod(deleteObjectMethod);
		return requestNodeWithMethod(deleteObjectMethod);
	}
}