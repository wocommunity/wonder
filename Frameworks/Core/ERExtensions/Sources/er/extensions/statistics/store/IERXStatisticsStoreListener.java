package er.extensions.statistics.store;

public interface IERXStatisticsStoreListener {

    public void log(long requestTime, IERXRequestDescription description);

    public void deadlock(int deadlocksCount);

}
