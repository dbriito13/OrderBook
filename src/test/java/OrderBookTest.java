
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.management.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class OrderBookTest {
    private OrderBook orderBook;
    private static final int NUM_ORDERS = 1_000_000; // Large order set

    @AllArgsConstructor
    @Getter
    @ToString
    private class GCStats {
        String GCName;
        long GCCount;
        long GCTime;
    }

    @BeforeEach
    void setUp(){
        orderBook = new OrderBook();
    }

    @Test
    void testBuyOrderMatchingSellOrder(){
        orderBook.addOrder(100.0, 10.0, Side.SELL, OrderType.LIMIT);
        orderBook.addOrder(100.0, 10.0, Side.BUY, OrderType.LIMIT);

        assertTrue(orderBook.getBidBook().isEmpty());
        assertTrue(orderBook.getAskBook().isEmpty());
    }

    @Test
    void testHighLoadMatchingPerformance(){
        //Prefill the PriceLevel and OrderBookEntry pools
        orderBook.prefillPriceLevelPool(1_500_000);
        OrderBookEntryPool.prefillPool(1_500_000);
        long startTime = System.nanoTime();
        List<GCStats> gcStatsBefore = getGCStats();
        for(int i = 1; i <= NUM_ORDERS; i++){
            orderBook.addOrder(100.0, 1.0, Side.BUY, OrderType.LIMIT); // Prices vary between 100-109
        }
        for(int i = 1; i <= NUM_ORDERS; i++){
            orderBook.addOrder(100.0, 1.0, Side.SELL, OrderType.LIMIT); // Prices vary between 100-109
        }
        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        List<GCStats> gcStatsAfter = getGCStats();

        log.info("Matching 100000 Symmetric orders took: {}ms", durationMs);
        //log.info(String.valueOf(orderBook));
        assertTrue(orderBook.getBidBook().isEmpty());
        assertTrue(orderBook.getAskBook().isEmpty());

        // Log GC statistics after the test
        printGCStats(gcStatsBefore, gcStatsAfter);
    }

    @Test
    public void testMarketOrderMatching(){
        orderBook.addOrder( 100.0, 10.0, Side.SELL, OrderType.LIMIT);
        orderBook.addOrder( 110.0, 10.0, Side.SELL, OrderType.LIMIT);

        orderBook.addOrder(0.0, 10.0, Side.BUY, OrderType.MARKET);

        assertEquals(1, orderBook.getAskBook().size());
        assertTrue(orderBook.getBidBook().isEmpty());
    }

    @Test
    public void testCancelOrder() {
        orderBook.addOrder(100.0, 10.0, Side.BUY, OrderType.LIMIT);

        OrderBookEntry order = orderBook.getBidBook().first().getOrders().getFirst();
        assertEquals(10.0, order.getQuantity());
        assertEquals(100.0, order.getPrice());
        assertEquals(Side.BUY, order.getSide());
        assertEquals(OrderType.LIMIT, order.getOrderType());

        // Cancel order
        orderBook.cancelOrder(order.getOrderId());

        assertTrue(orderBook.getBidBook().isEmpty());
    }

    private List<GCStats> getGCStats() {
        List<GCStats> gcStatsList = new ArrayList<>();
        ManagementFactory.getGarbageCollectorMXBeans()
                .forEach(gc ->
                    gcStatsList.add(new GCStats(gc.getName(), gc.getCollectionCount(), gc.getCollectionTime()))
                );
        return gcStatsList;
    }

    private void printGCStats(List<GCStats> before, List<GCStats> after){
        Map<String, GCStats> startMap = before.stream()
                .collect(Collectors.toMap(GCStats::getGCName, s -> s));

        log.info("GC Stats Difference:");
        for (GCStats endStat : after) {
            GCStats startStat = startMap.get(endStat.getGCName());

            long countDiff = endStat.getGCCount() - (startStat != null ? startStat.getGCCount() : 0);
            long timeDiff = endStat.getGCTime() - (startStat != null ? startStat.getGCTime() : 0);

            log.info("GC Name: {}, Count Diff: {}, Time Diff (ms): {}",
                    endStat.getGCName(), countDiff, timeDiff);
        }
    }
}