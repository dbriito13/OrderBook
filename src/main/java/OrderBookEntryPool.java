import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class OrderBookEntryPool {
    private static final Queue<OrderBookEntry> pool = new ArrayDeque<>();

    public static OrderBookEntry get(double price, double quantity, Side side, OrderType orderType){
        OrderBookEntry entry = pool.poll();
        if(entry ==null){
            return new OrderBookEntry(price, quantity, side, orderType);
        }
        entry.reset(price, quantity, side, orderType);
        return entry;
    }

    public static void release(OrderBookEntry entry){
        pool.offer(entry);
    }

    public static void prefillPool(int numObjects){
        for(int i = 0; i < numObjects; i++){
            pool.offer(new OrderBookEntry(1.0, 1.0, Side.SELL, OrderType.LIMIT));
        }
    }
}
