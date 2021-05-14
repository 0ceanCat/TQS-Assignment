package tqs.assignment.component;

import lombok.extern.slf4j.Slf4j;
import tqs.assignment.entity.Record;
import tqs.assignment.entity.AirQResponse;
import tqs.assignment.entity.Statistics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wy
 * @date 2021/4/24 20:56
 */
@Slf4j
public class Cache {
    private final AtomicInteger hits = new AtomicInteger(0);
    private final AtomicInteger misses = new AtomicInteger(0);
    private final Map<String, Record> cachedData = new ConcurrentHashMap<>();
    private final ExpiredDataCollector cacheCollector = new ExpiredDataCollector("CacheCollector");
    private final Integer ttl; // time to live
    private final Integer collectionInterval;
    private volatile static Cache SINGLETON;

    /**
     * If there is no created instance, creat it, else return it.
     * Should be an unique instance of Cache for entire project.
     * @param ttl time to live
     * @param collectorInterval collect interval
     * @return cache instance
     */
    public static Cache getOrCreate(int ttl, int collectorInterval){
        if (SINGLETON == null){
            synchronized (Cache.class){
                if (SINGLETON == null){
                    if (ttl < 0 || collectorInterval < 0){
                        throw new IllegalArgumentException("Values of ttl and collector interval for cache must be > 0");
                    }
                    SINGLETON = new Cache(ttl, collectorInterval);
                }
            }
        }
        return SINGLETON;
    }


    private Cache(int ttl, int collectorInterval) {
        if (ttl < 0 || collectorInterval < 0){
            throw new IllegalArgumentException("Values of ttl and collector interval for cache must be > 0");
        }
        this.ttl = ttl;
        this.collectionInterval = collectorInterval;
        start();
    }

    /**
     * put the data in cache
     * @param param can be city, country or geo coordinates with format 'lat:lng'
     * @param airQResponse air quality data, got from external api
     */
    public void put(String param, AirQResponse airQResponse) {
        Record newRecord = new Record();
        newRecord.setLiveUtil(newRecord.getCreation().plusSeconds(ttl));
        newRecord.setAirQResponse(airQResponse);
        cachedData.put(param, newRecord);
      //  log.info("Cached data {} for {}", newRecord, param);
    }

    /**
     * get air quality given a param
     * @param param can be city, country or geo coordinates with format 'lat:lng'
     */
    public AirQResponse get(String param) {
        Record cached = cachedData.get(param);

        if (cached != null && !cached.isExpired()) {
            hits.incrementAndGet();
       //     log.info("Cached data for {} found: {}", param, cached.getResponse());
            return cached.getAirQResponse();
        }

        misses.incrementAndGet();

        return null;
    }

    public int size() {
        return cachedData.size();
    }

    public int hits() {
        return hits.get();
    }

    public int misses(){
        return misses.get();
    }

    public int collected(){
        return cacheCollector.collected;
    }

    public int ttl(){
        return ttl;
    }

    public int interval(){
        return collectionInterval;
    }

    /**
     * Manually call the cache collector to collect expired data
      */
    public void collector(){
        if (cacheCollector.isSleeping()){
            cacheCollector.interrupt();
        }
    }

    public Statistics getStatistics(){
        return new Statistics(size(), hits(), misses(), collected());
    }


    private void start(){
        cacheCollector.start();
        log.info("Cache collector is running....");
    }

    private class ExpiredDataCollector extends Thread {
        private int collected;
        private boolean sleeping;

        ExpiredDataCollector(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sleeping(); // set sleeping = true
                    TimeUnit.SECONDS.sleep(collectionInterval);
                    waking(); // set sleeping = false
                    collect(); // collect expired data
                } catch (InterruptedException e) {
                    log.info("Cache collector is manually called.");
                    collect();
                }
            }
        }

        // collect expired data
        private void collect(){
            int before = collected;

            cachedData.forEach((k, v) -> {
                if (v.isExpired()){
                    cachedData.remove(k);
                    collected++;
                }
            });

            log.info("Cache collector collected {} expired data", collected - before);
        }

        private void waking(){
            sleeping = false;
        }

        private void sleeping(){
            sleeping = true;
        }

        private boolean isSleeping() {
            return sleeping;
        }
    }

}
