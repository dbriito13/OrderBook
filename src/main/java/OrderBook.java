import lombok.Getter;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class OrderBook {

    private final TreeSet<PriceLevel> bidBook; // Highest Prices first
    private final TreeSet<PriceLevel> askBook; // Lowest Prices first
    private final Map<Long, OrderBookEntry> orderMap;
    private final Deque<PriceLevel> priceLevelPool;

    public OrderBook() {
        this.bidBook = new TreeSet<>(Comparator.reverseOrder());
        this.askBook = new TreeSet<>();
        this.orderMap = new HashMap<>();
        this.priceLevelPool = new ArrayDeque<>();
    }

    private PriceLevel getPriceLevelFromPool(OrderBookEntry entry){
        PriceLevel priceLevel = priceLevelPool.poll();
        if (priceLevel == null){
            priceLevel = new PriceLevel(entry);
        } else {
            priceLevel.reset(entry);
        }
        return priceLevel;
    }

    public void prefillPriceLevelPool(int num){
        for(int i = 0; i < num; i++){
            PriceLevel level = new PriceLevel(new OrderBookEntry(1.0, 1.0, Side.SELL, OrderType.LIMIT, 0));
            priceLevelPool.offer(level);
        }
    }

    public void addOrder(double price, double quantity, Side side, OrderType orderType){
        OrderBookEntry entry = OrderBookEntryPool.get(price, quantity, side, orderType);
        matchOrder(entry, entry.isBid() ? askBook: bidBook);
        matchAndAdd(entry);
    }

    private void matchAndAdd(OrderBookEntry entry){
        matchOrder(entry, entry.isBid() ? askBook: bidBook);
        if (!entry.isFilled() && entry.getOrderType().equals(OrderType.LIMIT)){
            PriceLevel level = getPriceLevelFromPool(entry);
            TreeSet<PriceLevel> book = entry.isBid()? bidBook: askBook;
            PriceLevel existingLevel = book.floor(level);
            if (existingLevel != null){
                Objects.requireNonNull(existingLevel).addOrder(entry);
                orderMap.put(entry.getOrderId(), entry);
                priceLevelPool.offer(level);
            } else {
                // Add new PriceLevel to corresponding book
                book.add(level);
                orderMap.put(entry.getOrderId(), entry);
            }
        } else if (entry.isFilled()){
            // Release filled orderbookentry object back to pool
            OrderBookEntryPool.release(entry);
        }
    }

    private void modifyOrder(UUID id, double newQuantity, double newPrice){
        OrderBookEntry entry = orderMap.get(id);
        if (entry == null) return;
        TreeSet<PriceLevel> book = entry.isBid() ? bidBook : askBook;

        if (entry.getPrice() == newPrice){
            // No need to remove entry from PriceLevel
            entry.setQuantity(newQuantity);
        } else {
            // Price changed, need to move from PriceLevel
            PriceLevel priceLevel = entry.getPriceLevel();
            if (priceLevel.orders.size() == 1){
                // Remove pricelevel
                book.remove(priceLevel);
                priceLevelPool.offer(priceLevel);
            } else {
                priceLevel.removeOrder(entry);
            }
            // Call match in case modify caused order to be matched
            matchAndAdd(entry);
        }

    }

    public void cancelOrder(long id){
        OrderBookEntry removedOrder = orderMap.remove(id);
        if (removedOrder != null){
            PriceLevel level = removedOrder.getPriceLevel();
            if (level.orders.size()==1){
                TreeSet<PriceLevel> book = removedOrder.isBid() ? bidBook : askBook;
                book.remove(level);
                priceLevelPool.offer(level);
            } else {
                removedOrder.getPriceLevel().removeOrder(removedOrder);
            }
            OrderBookEntryPool.release(removedOrder);
        }
    }

    private void matchOrder(OrderBookEntry entry, TreeSet<PriceLevel> oppositeBook){
        //FIFO
        if(entry.getOrderType().equals(OrderType.LIMIT)){
            while (!oppositeBook.isEmpty() && entry.getQuantity() > 0){
                // Get First Price Level in book
                PriceLevel topLevel = oppositeBook.first();
                // Check if prices don't match
                if (entry.isBid() && entry.getPrice() < topLevel.getPrice() ||
                    entry.isAsk() && entry.getPrice() > topLevel.getPrice()) {
                    break;
                }
                // Reduce both incoming entry and available price level by matching amount
                topLevel.reduceQuantity(entry);

                //Remove level if fully matched
                if(topLevel.isEmpty()) {
                    oppositeBook.pollFirst();
                    priceLevelPool.offer(topLevel);
                }
            }
        } else if (entry.getOrderType().equals(OrderType.MARKET)){
            // Market orders get filled against the best price available, any unfilled quantity gets left unfilled
            while (!oppositeBook.isEmpty() && entry.getQuantity() > 0){
                PriceLevel topLevel = oppositeBook.first();
                topLevel.reduceQuantity(entry);
                if(topLevel.isEmpty()){
                    oppositeBook.pollFirst();
                    priceLevelPool.offer(topLevel);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Bids: " + bidBook.toString() + "Asks:" + askBook.toString();
    }
}
