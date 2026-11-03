//
// ERWorkerThredsTestCase.java
// Project ERWorkerChannel
//
// Created by tatsuya on Wed Jul 03 2002
//
package er.workerchannel;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ERWorkerThredsTestCase extends TestCase {

    public static Logger log = LoggerFactory.getLogger(ERWorkerThredsTestCase.class);

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
