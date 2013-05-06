//
// ERWorkerThredsTestCase.java
// Project ERWorkerChannel
//
// Created by tatsuya on Wed Jul 03 2002
//
package er.workerchannel;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class ERWorkerThredsTestCase extends TestCase {

    public static Logger log = Logger.getLogger(ERWorkerThredsTestCase.class);

    public ERWorkerThredsTestCase(String name) { 
        super(name); 
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWorkerThreds() {
        ERWorkerChannel channel = new ERLocalWorkerChannel(2, 25);   
        channel.startWorkers();
        new ERTestClientThread("Alice", channel, 25).start();
        new ERTestClientThread("Bobby", channel, 30).start();
        new ERTestClientThread("Chris", channel, 40).start();
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            ;
        } finally {
            channel.shutdownWorkers();
        }
    }
}
