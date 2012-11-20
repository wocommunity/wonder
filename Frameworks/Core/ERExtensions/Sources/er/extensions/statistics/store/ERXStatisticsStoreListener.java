package er.extensions.statistics.store;

public interface ERXStatisticsStoreListener {

    public void log(long requestTime, RequestDescription description);

    public void deadlock(int deadlocksCount);

}
