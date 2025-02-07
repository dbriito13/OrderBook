
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.management.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class OrderBookTest {
    private OrderBook orderBook;
    private static final int NUM_ORDERS = 100_000; // Large order set

    @BeforeEach
    void setUp(){
        orderBook = new OrderBook();
    }

    @Test
    void testBuyOrderMatchingSellOrder(){
        OrderBookEntry sellOrder = new OrderBookEntry(100.0, 10.0, Side.SELL, OrderType.LIMIT);
        OrderBookEntry buyOrder = new OrderBookEntry( 100.0, 10.0, Side.BUY, OrderType.LIMIT);
        orderBook.addOrder(sellOrder);
        orderBook.addOrder(buyOrder);

        assertTrue(orderBook.getBidBook().isEmpty());
        assertTrue(orderBook.getAskBook().isEmpty());
    }

    @Test
    void testHighLoadMatchingPerformance(){
        long startTime = System.nanoTime();
        for(int i = 1; i <= NUM_ORDERS; i++){
            OrderBookEntry sellOrder = new OrderBookEntry( 100.0, 1.0, Side.BUY, OrderType.LIMIT);
            orderBook.addOrder(sellOrder); // Prices vary between 100-109
        }
        for(int i = 1; i <= NUM_ORDERS; i++){
            OrderBookEntry buyOrder = new OrderBookEntry( 100.0, 1.0, Side.SELL, OrderType.LIMIT);
            orderBook.addOrder(buyOrder); // Prices vary between 100-109
        }
        long durationMs = (System.nanoTime() - startTime) / 1_000_000;

        log.info("Matching 100000 Symmetric orders took: {}ms", durationMs);
        //log.info(String.valueOf(orderBook));
        assertTrue(orderBook.getBidBook().isEmpty());
        assertTrue(orderBook.getAskBook().isEmpty());

        // Log GC statistics after the test
        logGCStats();
    }

    @Test
    public void testMarketOrderMatching(){
        orderBook.addOrder(new OrderBookEntry( 100.0, 10.0, Side.SELL, OrderType.LIMIT));
        orderBook.addOrder(new OrderBookEntry( 110.0, 10.0, Side.SELL, OrderType.LIMIT));

        OrderBookEntry marketOrder = new OrderBookEntry( 0.0, 10.0, Side.BUY, OrderType.MARKET);
        orderBook.addOrder(marketOrder);

        assertEquals(1, orderBook.getAskBook().size());
        assertTrue(marketOrder.isFilled());
    }

    @Test
    public void testCancelOrder() {
        OrderBookEntry order = new OrderBookEntry(100.0, 10.0, Side.BUY, OrderType.LIMIT);
        orderBook.addOrder(order);

        assertTrue(orderBook.getBidBook().first().getOrders().contains(order));

        // Cancel order
        orderBook.cancelOrder(order.getOrderId());

        assertTrue(orderBook.getBidBook().isEmpty());
    }

    private void logGCStats() {
        System.out.println("GC Stats at the end of the test:");
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.println("GC Name: " + gcBean.getName());
            System.out.println("GC Collection Count: " + gcBean.getCollectionCount());
            System.out.println("GC Collection Time: " + gcBean.getCollectionTime() + " ms");
        }
    }
}