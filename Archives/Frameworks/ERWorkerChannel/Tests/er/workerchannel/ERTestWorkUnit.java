//
// ERTestWorkUnit.java
// Project ERWorkerChannel
//
// Created by tatsuya on Mon Jul 29 2002
//
package er.workerchannel;

import java.util.Random;

import org.apache.log4j.Logger;

public class ERTestWorkUnit extends ERWorkUnit {

    public static Logger log = Logger.getLogger(ERTestWorkUnit.class);

    private final String _name; 
    private final int _number; 
    private static final Random random = new Random();

    public ERTestWorkUnit(String name, int number) {
        _name = name;
        _number = number;
    }

    public void execute() {
        log.info(Thread.currentThread().getName() + " executes " + this);
        try {
            Thread.sleep(random.nextInt(1000));
        } catch (InterruptedException e) {
            ;
        }
        futureResult().setResult(new ERRealResult(this.toString() + " done.", null));
    }

    public String toString() {
        return "<" + getClass().getName() 
            + "  Request from " + _name + " No." + _number 
            + ">";
    }

}
