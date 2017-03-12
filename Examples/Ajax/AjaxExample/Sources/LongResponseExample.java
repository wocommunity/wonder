import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class LongResponseExample extends WOComponent {
	private static final Logger log = LoggerFactory.getLogger(LongResponseExample.class);

	public static class Task extends Thread {
		int stage = 0;
		String status = "Idle";

		@Override
		public void run() {

			try {
				setStatus("Starting up");
				sleep(2000);
				while (stage < 20) {
					sleep(1000);
					stage++;
					setStatus("Currently at stage: " + stage);
				}
				setStatus("Finished");
			}
			catch (InterruptedException e) {
				status = "Interrupted";
			}
		}

		private void setStatus(String value) {
			status = value;
			log.info(status);
		}

		public String getStatus() {
			return status;
		}
	}

	public Task task;

	public LongResponseExample(WOContext context) {
		super(context);
	}

	public long test() {
		return System.currentTimeMillis();
	}

	public WOComponent startTask() {
		task = new Task();
		task.start();
		return null;
	}

	public WOActionResults stopTask() {
		task.interrupt();
		return null;
	}
}
