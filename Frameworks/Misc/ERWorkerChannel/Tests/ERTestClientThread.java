//
// ERTestClientThread.java
// Project ERWorkerChannel
//
// Created by tatsuya on Mon Jul 29 2002
//
package er.workerchannel;

import java.util.Random;
import er.extensions.ERXLogger;

public class ERTestClientThread extends Thread {

    public static ERXLogger log = ERXLogger.getERXLogger(ERTestClientThread.class);

    private final ERWorkerChannel _channel;
    private final int _repeat;
    private static final Random random = new Random();

    public ERTestClientThread(String name, ERWorkerChannel channel, int repeat) {
        super(name);
        _channel = channel;
        _repeat = repeat; 
    }

    public void run() {
        try {
            for (int i = 0; i < _repeat; i++) {
                ERWorkUnit work = new ERTestWorkUnit(getName(), i);
                _channel.scheduleWorkUnit(work);
                log.info("Scheduled: " + work);
                Thread.sleep(random.nextInt(1000));
            }
        } catch (InterruptedException e) {
            ;
        }
    }
}
