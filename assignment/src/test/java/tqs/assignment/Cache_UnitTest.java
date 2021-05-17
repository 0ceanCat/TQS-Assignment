package tqs.assignment;

import org.junit.jupiter.api.*;
import tqs.assignment.component.Cache;
import tqs.assignment.entity.AirQResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

/**
 * @author wy
 * @date 2021/4/24 12:33
 */

@TestMethodOrder(OrderAnnotation.class)
class Cache_UnitTest {

    private final static Cache cache = Cache.getOrCreate(2, 3);
    private final static AirQResponse AIR_Q_RESPONSE = new AirQResponse();

    @BeforeEach
    void init() {
        Stream.of("A", "B", "C", "D").forEach(param -> cache.put(param, AIR_Q_RESPONSE));
    }

    @Test
    @Order(1)
    void getSize() {
        assertEquals(4, cache.size());
    }

    @Test
    @Order(2)
    void get4CachedData() {
        Stream.of("A", "B", "C", "D")
                .forEach(param -> assertEquals(AIR_Q_RESPONSE, cache.get(param)));
    }

    @Test
    @Order(3)
    void sleep3seconds_thenSizeZero(){
        assertEquals(4, cache.size());

        assertDoesNotThrow(()->{
            // wait for data to be expired and then cache collector will collect them
            TimeUnit.SECONDS.sleep(3);

            // wait for cache collector to finish it's work. 100ms
            TimeUnit.MILLISECONDS.sleep(100);

            assertEquals(0, cache.size());
        });
    }

    @Test
    @Order(4)
    void sleep2seconds_callCollector_thenSizeZero() {
        assertEquals(4, cache.size());

        assertDoesNotThrow(()->{
            // wait for data to be expired. 2s
            TimeUnit.SECONDS.sleep(2);

            // manually call the cache collector to collect expired data
            cache.collector();

            // wait for cache collector to finish it's work. 100ms
            TimeUnit.MILLISECONDS.sleep(100);

            assertEquals(0, cache.size());
        });
    }

    @Test
    @Order(5)
    void sleep2seconds_callCollector_thenGetNull() {
        assertEquals(4, cache.size());

        assertDoesNotThrow(()->{
            // wait for data to be expired. 2s
            TimeUnit.SECONDS.sleep(2);

            // manually call the cache collector to collect expired data
            cache.collector();

            // wait for cache collector to finish it's work, 100 ms
            TimeUnit.MILLISECONDS.sleep(100);

            // all of them were collected, so we get Null
            Stream.of("A", "B", "C", "D")
                    .forEach(param -> assertNull(cache.get(param)));
        });
    }

    @Test
    @Order(6)
    void sleep2seconds_callCollector_thenMore4Collected(){
        int currentCollect = cache.collected();

        assertDoesNotThrow(()->{
            TimeUnit.SECONDS.sleep(2);

            cache.collector();

            TimeUnit.MILLISECONDS.sleep(100);

            assertEquals(currentCollect + 4, cache.collected());
        });

    }

    @Test
    @Order(7)
    void manyThreadsPut10000Times() throws InterruptedException {
        CountDownLatch count = new CountDownLatch(5);
        assertDoesNotThrow(() -> {

            for (int i = 0; i < 5; i++) {
                int iCopy = i;
                new Thread(()->{
                    for (int k = 0; k < 10_000; k++) {
                        cache.put(iCopy  + "_" + k, AIR_Q_RESPONSE);
                    }
                    count.countDown();
                }).start();
            }
        });

        // wait for all threads to finish it's work
        count.await();
    }

    @Test
    @Order(8)
    void manyThreadsGet10000Times() throws InterruptedException {
        CountDownLatch count = new CountDownLatch(5);
        assertDoesNotThrow(()->{
            for (int i = 0; i < 5; i++) {
                new Thread(()->{
                    for (int k = 0; k < 10_000; k++) {
                        cache.get("A");
                    }
                    count.countDown();
                }).start();
            }
        });

        // wait for all threads to finish it's work
        count.await();

    }

    @Test
    @Order(9)
    void get3timesNotCachedData_thenHitsZeroMisses3(){
        int currentHits = cache.hits();
        int currentMisses = cache.misses();

        Stream.of("E", "E", "E")
                .forEach(cache::get);

        assertEquals(currentHits, cache.hits());
        assertEquals(currentMisses + 3, cache.misses());
    }

    @Test
    @Order(10)
    void get3timesCachedData_thenHit3MissesZero(){
        int currentHits = cache.hits();
        int currentMisses = cache.misses();

        Stream.of("A", "B", "C")
                .forEach(cache::get);

        assertEquals(currentHits + 3, cache.hits());
        assertEquals(currentMisses, cache.misses());
    }
}