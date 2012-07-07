package er.httpclient.test;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;

import er.httpclient.HttpGetData;


/**
 * How to send a request directly using {@link HttpClient}.
 * 
 * @since 4.0
 */
public class ClientExecuteDirect2 {

    public static void main(String[] args) throws Exception {

    	HttpGetData httpData = new HttpGetData("www.google.com");
    	httpData.setPath("/search");
    	httpData.addQueryParams("q", "httpclient");
    	httpData.addQueryParams("btnG", "Google Search");
    	httpData.addQueryParams("aq", "f");
    	httpData.addQueryParams("oq", null);

    	httpData.execute();
    	
        System.out.println("----------------------------------------");
        System.out.println(httpData.response().getStatusLine());
        Header[] headers = httpData.response().getAllHeaders();
        for (int i = 0; i < headers.length; i++) {
            System.out.println(headers[i]);
        }
        System.out.println("----------------------------------------");

        System.out.println("HTML : " + httpData.html());
    }

}
