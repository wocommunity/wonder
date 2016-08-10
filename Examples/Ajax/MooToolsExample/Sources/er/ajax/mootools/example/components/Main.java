package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

public class Main extends ERXComponent {
	
	public String _pageTitle;
	private Task _task;
	
	public Main(WOContext context) {
		super(context);
	}

	public void setTask(Task task) {
		_task = task;
	}

	public Task task() {
		return _task;
	}	
	
	public static class Task extends Thread {
		int stage = 0;
		String status = "Idle";

		@Override
		public void run() {

			try {
				setStatus("Starting up");
				while (stage < 3) {
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
		}

		public String getStatus() {
			return status;
		}
	}

	public void wasteTime() {
		setTask(new Task());
		task().start();
		do {
			System.out.println(task().getStatus());
		} while(! task().getStatus().equals("Finished"));
	}
	
}
