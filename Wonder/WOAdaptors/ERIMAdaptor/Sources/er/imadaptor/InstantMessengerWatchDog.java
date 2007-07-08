package er.imadaptor;

public class InstantMessengerWatchDog {
	private IInstantMessenger _watchedInstantMessenger;
	private String _watcherScreenName;
	private String _watcherPassword;
	private IInstantMessenger _watcherInstantMessenger;
	private Thread _watcherThread;
	private IMConnectionTester _watcherTester;
	private Thread _watchedThread;
	private IMConnectionTester _watchedTester;

	public InstantMessengerWatchDog(IInstantMessenger watchedInstantMessenger, IInstantMessenger watcherInstantMessenger) {
		_watchedInstantMessenger = watchedInstantMessenger;
		_watcherInstantMessenger = watcherInstantMessenger;
	}

	public void start() {
		try {
			_watcherInstantMessenger.connect();

			_watcherTester = new IMConnectionTester(_watcherInstantMessenger, _watchedInstantMessenger, 60000, 30000);
			_watcherThread = new Thread(_watcherTester);
			_watcherThread.start();

			_watchedTester = new IMConnectionTester(_watchedInstantMessenger, _watcherInstantMessenger, 60000, 30000);
			_watchedThread = new Thread(_watchedTester);
			_watchedThread.start();
		}
		catch (Throwable e) {
			InstantMessengerAdaptor.log.debug("Failed to connect watcher to provider.", e);
		}
	}

	public void stop() {
		if (_watchedTester != null) {
			_watchedTester.stop();
			_watchedTester = null;
		}
		if (_watcherTester != null) {
			_watcherTester.stop();
			_watcherTester = null;
		}
		_watcherInstantMessenger.disconnect();
	}
}
