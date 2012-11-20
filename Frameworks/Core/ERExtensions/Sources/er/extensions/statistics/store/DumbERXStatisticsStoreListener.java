package er.extensions.statistics.store;

public class DumbERXStatisticsStoreListener implements ERXStatisticsStoreListener {

    public void log(long requestTime, RequestDescription description) {}

    public void deadlock(int deadlocksCount) {}
}
