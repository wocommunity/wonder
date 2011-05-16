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
import er.rest.ERXRestContext;
import er.rest.ERXRestRequestNode;
import er.rest.format.ERXRestFormat;
import er.rest.format.ERXStringBufferRestResponse;
import er.rest.format.ERXStringRestRequest;

public class ERXRestClient {
	private String _baseURL;
	private ERXRestContext _context;
	private boolean _classDescriptionRequired;
	private HttpClient _httpClient = null;

	public ERXRestClient(String baseURL, ERXRestContext context, boolean classDescriptionRequired) {
		_baseURL = baseURL;
		_context = context;
		setClassDescriptionRequired(classDescriptionRequired);
	}

	public ERXRestClient(String baseURL, ERXRestContext context) {
		this(baseURL, context, true);
	}

	public void setBaseURL(String baseURL) {
		_baseURL = baseURL;
	}

	public String baseURL() {
		return _baseURL;
	}
	
	public void setContext(ERXRestContext context) {
        _context = context;
    }
	
	public ERXRestContext context() {
        return _context;
    }

	public void setClassDescriptionRequired(boolean classDescriptionRequired) {
		_classDescriptionRequired = classDescriptionRequired;
	}

	public boolean isClassDescriptionRequired() {
		return _classDescriptionRequired;
	}

	/* Extract the requestNode from the given method. The method will be asked for it's Content-Type header to determine the format.
	 * This is known to fail if the method doesn't have a Content-Type header.
	 */
	protected ERXRestRequestNode requestNodeWithMethod(HttpMethodBase method) throws IOException {
		ERXRestFormat format = ERXRestFormat.formatNamed(method.getResponseHeader("Content-Type").getValue());
		return requestNodeWithMethod(method, format);
	}

	/* Extract the requestNode from the given method using the given format.
	 */
	protected ERXRestRequestNode requestNodeWithMethod(HttpMethodBase method, ERXRestFormat format) throws IOException {
		ERXRestRequestNode responseNode = format.parser().parseRestRequest(new ERXStringRestRequest(method.getResponseBodyAsString()), format.delegate(), _context);
		return responseNode;
	}

	protected Object _objectWithRequestNode(ERXRestRequestNode node, String entityName) {
		Object obj;
		EOClassDescription classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(entityName);
		if (entityName != null && classDescription == null && !_classDescriptionRequired) {
			obj = node;
		}
		else {
			obj = node.objectWithFilter(entityName, ERXKeyFilter.filterWithAllRecursive(), _context);
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

	@SuppressWarnings("unchecked")
	public <T> T object(String entityName, String id, String action, ERXRestFormat format) throws HttpException, IOException {
		return (T) objectWithPath(path(entityName, id, action, format));
	}

	/* Returns the instance of HttpClient that's used for communcation with the server.
	 * Handy if you want direct access to the object which manages communication.
	 * For example, HttpClient will collect cookies and send them on subsequent requests for you.
	 */
	public HttpClient httpClient() {
		if(_httpClient == null){
			_httpClient = new HttpClient();
		}
		return _httpClient;
	}

	@SuppressWarnings("unchecked")
	public <T> T objectWithPath(String path, String entityName) throws HttpException, IOException {
		HttpClient client = httpClient();
		GetMethod fetchObjectMethod = new GetMethod(new ERXMutableURL(_baseURL).appendPath(path).toExternalForm());
		client.executeMethod(fetchObjectMethod);
		ERXRestRequestNode node = requestNodeWithMethod(fetchObjectMethod);
		return (T) _objectWithRequestNode(node, entityName);
	}

	@SuppressWarnings("unchecked")
	public <T> T objectWithPath(String path) throws HttpException, IOException {
		HttpClient client = httpClient();
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
		ERXRestRequestNode node = ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, filter, _context);
		ERXStringBufferRestResponse response = new ERXStringBufferRestResponse();
		format.writer().appendToResponse(node, response, format.delegate(), _context);

		HttpClient client = httpClient();
		PutMethod updateObjectMethod = new PutMethod(new ERXMutableURL(_baseURL).appendPath(path).toExternalForm());
		updateObjectMethod.setRequestEntity(new StringRequestEntity(response.toString()));
		client.executeMethod(updateObjectMethod);
		return requestNodeWithMethod(updateObjectMethod);
	}

	public ERXRestRequestNode create(Object obj, ERXKeyFilter filter, String entityName, String id, String action, ERXRestFormat format) throws HttpException, IOException {
		return createObjectWithPath(obj, filter, path(entityName, id, action, format), format);
	}

	public ERXRestRequestNode createObjectWithPath(Object obj, ERXKeyFilter filter, String path, ERXRestFormat format) throws HttpException, IOException {
		ERXRestRequestNode node = ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, filter, _context);
		ERXStringBufferRestResponse response = new ERXStringBufferRestResponse();
		format.writer().appendToResponse(node, response, format.delegate(), _context);

		HttpClient client = httpClient();
		PostMethod updateObjectMethod = new PostMethod(new ERXMutableURL(_baseURL).appendPath(path).toExternalForm());
		updateObjectMethod.setRequestEntity(new StringRequestEntity(response.toString()));
		client.executeMethod(updateObjectMethod);
		return requestNodeWithMethod(updateObjectMethod, format);
	}

	public ERXRestRequestNode delete(Object obj, String entityName, String id, String action, ERXRestFormat format) throws HttpException, IOException {
		return deleteObjectWithPath(obj, path(entityName, id, action, format), format);
	}

	public ERXRestRequestNode deleteObjectWithPath(Object obj, String path, ERXRestFormat format) throws HttpException, IOException {
		ERXRestRequestNode node = ERXRestRequestNode.requestNodeWithObjectAndFilter(obj, ERXKeyFilter.filterWithNone(), _context);

		HttpClient client = httpClient();
		DeleteMethod deleteObjectMethod = new DeleteMethod(new ERXMutableURL(_baseURL).appendPath(path).toExternalForm());
		client.executeMethod(deleteObjectMethod);
		return requestNodeWithMethod(deleteObjectMethod);
	}
}
