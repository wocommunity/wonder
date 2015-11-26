
package er.erxtest;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class ERXTestReportListener extends RunListener {

    int attempted = 0;
    int failed = 0;
    long start = 0L;

    int loop = 0;

    private ArrayList<String> listLoadedClasses(ClassLoader loader) {

        ArrayList<String> loadedClasses = new ArrayList<String>();

        Class klass = loader.getClass();
        while (klass != java.lang.ClassLoader.class) {
            klass = klass.getSuperclass();
        }
        try {
            java.lang.reflect.Field fldClasses = klass.getDeclaredField("classes");
            fldClasses.setAccessible(true);
            Vector classes = (Vector) fldClasses.get(loader);
            for (Iterator iter = classes.iterator(); iter.hasNext();) {
                loadedClasses.add(iter.next().toString());
            }
        } catch (java.lang.SecurityException e) {
            e.printStackTrace();
        } catch (java.lang.IllegalArgumentException e) {
            e.printStackTrace();
        } catch (java.lang.NoSuchFieldException e) {
            e.printStackTrace();
        } catch (java.lang.IllegalAccessException e) {
            e.printStackTrace();
        }
        return loadedClasses;
    }

    private ArrayList<String> getVersionInfo(boolean verbose) {

        ClassLoader loader = this.getClass().getClassLoader();

        ArrayList<String> found = listLoadedClasses(loader);

        ArrayList<String> fixed = new ArrayList<String>();


        java.security.MessageDigest md = null;

        try {
            md = java.security.MessageDigest.getInstance("SHA-1");
        } catch (java.security.NoSuchAlgorithmException nsae) {
        	throw new IllegalArgumentException(nsae);
        }

        for (String aClassName : found) {

            String classAsPath = aClassName.replace("interface ", "").replace("class ", "").replace('.', '/') + ".class";

            java.io.InputStream stream = loader.getResourceAsStream(classAsPath);

            int current = 0;
            int intvl = 16;

            byte bytes[] = new byte[intvl];

            try {
                int bytesRead = 1;
                while (bytesRead > 0) {
                    if (stream != null) {
                        bytesRead = stream.read(bytes, current, intvl);
                        md.update(bytes, 0, intvl);

                        if (verbose) {
                            for (int idx = 0; idx < bytesRead; idx++) {
                                String num = Integer.toHexString(bytes[idx] & 0xff);
                                while (num.length() < 2) { num = "0"+num; }
                                System.out.print(" "+num);
                            }
                            System.out.println("");
                        }
                    } else
                        bytesRead = 0;
                }
            } catch (java.io.IOException ioe) {
                System.out.println(ioe.getMessage());
            }

            byte[] digest = md.digest();

            StringBuilder str = new StringBuilder();

            for (int idx = 0; idx < digest.length; idx++) {
                String num = Integer.toHexString(digest[idx] & 0xff);
                while (num.length() < 2) { num = "0"+num; }
                str.append(num);
            }
            
            fixed.add(str+" "+aClassName);
        }
        if (verbose)
            System.out.println("system classes found # "+found.size());

        return fixed;
    }

    @Override
    public void testRunStarted(Description description) { start = System.currentTimeMillis(); }

    @Override
    public void testStarted(Description description) { }

    @Override
    public void testFailure(Failure failure) { }

    private static java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void testRunFinished(Result result) {

        String contact = System.getProperty("wo.test.emailAddress");

        if (contact != null && ! contact.equals("") && ! contact.startsWith("$")) {

            ArrayList<String> params = new ArrayList<String>();

            params.add("email="+contact);

            params.add("duration="+(System.currentTimeMillis() - start));

            params.add("when="+format.format(new java.util.Date()));

            params.add("timezone="+java.util.TimeZone.getDefault().getID());

            int failCount = 0;
            for (Failure f : result.getFailures()) { params.add("fail"+(++failCount)+"="+f); }

            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String javaVersion = System.getProperty("java.version");
            String woVersion = Application.application().getWebObjectsVersion();

            params.add("env1=OS: "+osName+" - "+osVersion);
            params.add("env2=JavaVM: "+javaVersion);
            params.add("env3=WebObjects: "+woVersion);

            String extra = System.getProperty("wo.test.extra");
            if (extra != null && ! extra .equals("") && ! extra.startsWith("$")) {
                 if (extra.length() > 64) extra = extra.substring(0,64);
                 params.add("env4="+extra);
            }

            int versCount = 0;
            for (String version : getVersionInfo(false)) { params.add("vers"+(++versCount)+"="+version); }

            StringBuilder allParams = new StringBuilder();

            for (int idx = 0; idx < params.size(); idx++) {
                //System.out.println("    param: \""+params.get(idx)+"\"");
                if (idx > 0) allParams.append("&");
                allParams.append(params.get(idx));
            }

            try {

                String url = "https://wocommunity.org/apps/WebObjects/WOTested.woa/wa/addResult";
                //String url = "http://10.0.1.6:55555/cgi-bin/WebObjects/WOTested.woa/wa/addResult";

                URL homeURL = null;

                homeURL = new URL(url);

                HttpURLConnection connection = null;

                if (url.startsWith("http:")) {

                    connection = (HttpURLConnection)homeURL.openConnection();
                }

                if (url.startsWith("https:")) {

                    final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(final X509Certificate[] chain, final String authType) { }
                        @Override
                        public void checkServerTrusted(final X509Certificate[] chain, final String authType) { }
                        @Override
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                    } };
    
                    final SSLContext sslContext = SSLContext.getInstance("SSL");

                    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                    final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
    
                    connection = (HttpURLConnection)homeURL.openConnection();

                    ((HttpsURLConnection)connection).setSSLSocketFactory(sslSocketFactory);
                }

                if (connection != null) {
                	connection.setDoOutput(true);

                	OutputStreamWriter osWriter = new OutputStreamWriter(connection.getOutputStream());

                	osWriter.write(allParams.toString());

                	osWriter.close();

                	int httpResponse = connection.getResponseCode();
                }
            } catch (java.io.IOException ioe) {
                System.out.println("Test results not submitted. No worries.\n"+ioe);
            } catch (java.security.NoSuchAlgorithmException nsae) {
                System.out.println("Test results not submitted. No worries.\n"+nsae);
            } catch (java.security.KeyManagementException kme) {
                System.out.println("Test results not submitted. No worries.\n"+kme);
            }
        }
    }
}

