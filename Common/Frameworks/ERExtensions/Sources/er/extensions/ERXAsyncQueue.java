package er.extensions;

import java.util.LinkedList;

/**
 * Simple queue class that runs in a thread, waits and pulls jobs from a queue.
 * To use it, implement the process(Object) method in your subclass.
 * @author ak
 *
 */
public abstract class ERXAsyncQueue extends Thread {

	private LinkedList _jobs = new LinkedList();

	public void enqueue(Object o) {
		synchronized (_jobs) {
			_jobs.addFirst(o);
			_jobs.notify();
		}
	}
	
	public final void run() {
		boolean done = false;
		try {
			while(!done) {
				Object o = null;
				synchronized (_jobs) {
				    if(!_jobs.isEmpty()) {
				        o = _jobs.removeLast();
				    } else {
				        _jobs.wait();
				    }
				}
				if(o != null) {
					process(o);
				}
			}
		} catch (InterruptedException e) {
			done = true;
		}
	}

	public abstract void process(Object object);
}
