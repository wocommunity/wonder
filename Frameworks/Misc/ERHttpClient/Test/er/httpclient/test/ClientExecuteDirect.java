package er.httpclient.test;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;

import er.httpclient.HttpGetData;


/**
 * How to send a request directly using {@link HttpClient}.
 * 
 * @since 4.0
 */
public class ClientExecuteDirect {

  public static void main(String[] args) throws Exception {

    boolean utf8Test = false;

    // 簡単な HTTP アクセス
    // HTTP Simple Test
    HttpGetData httpData;
    if(utf8Test) {
      // UTF-8 を使用しているサイト (A Site with UTF8)
      httpData = new HttpGetData("www.ksroom.com"); 
      httpData.setPath("/App/WebObjects/Kisa.woa");
      httpData.setSendEncoding(HttpGetData.ENCODING_UTF8);
      httpData.setReceiveEncoding(HttpGetData.ENCODING_UTF8);
    } else {
      // Shift_JIS を使用しているサイト (A Site with Shift_JIS)
      httpData = new HttpGetData("e-words.jp"); // 
      httpData.setPath("/w/SJIS.html");
      httpData.setSendEncoding(HttpGetData.ENCODING_SJIS);
      httpData.setReceiveEncoding(HttpGetData.ENCODING_SJIS);
    }
    httpData.execute();

    System.out.println("----------------------------------------");

    System.err.println(httpData.response());

    System.out.println(httpData.response().getStatusLine());
    Header[] headers = httpData.response().getAllHeaders();
    for (int i = 0; i < headers.length; i++) {
      System.out.println(headers[i]);
    }
    System.out.println("----------------------------------------");

    System.out.println("HTML : " + httpData.html());
  }

}
