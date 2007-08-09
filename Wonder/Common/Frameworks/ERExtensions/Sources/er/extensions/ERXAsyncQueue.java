package er.extensions;

import java.util.LinkedList;

/**
 * Simple queue class that runs in a thread, waits and pulls jobs from a queue.
 * To use it, implement the process(Object) method in your subclass.
 * 
 * @author ak
 * 
 */
public abstract class ERXAsyncQueue<T> extends Thread {
	private LinkedList<T> _jobs = new LinkedList<T>();

  public ERXAsyncQueue() {
	  super("ERXAsyncQueue");
  }
  
	public ERXAsyncQueue(String name) {
	  super(name);
	}
	
	public void enqueue(T o) {
		synchronized (_jobs) {
			_jobs.addFirst(o);
			_jobs.notifyAll();
		}
	}

	@Override
	public final void run() {
		boolean done = false;
		try {
			while (!done) {
				T o = null;
				synchronized (_jobs) {
					if (!_jobs.isEmpty()) {
						o = _jobs.removeLast();
					}
					else {
						_jobs.wait();
					}
				}
				if (o != null) {
					process(o);
				}
			}
		}
		catch (InterruptedException e) {
			done = true;
		}
	}

	public abstract void process(T object);
}
