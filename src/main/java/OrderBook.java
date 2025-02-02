import lombok.Getter;

import java.util.*;

@Getter
public class OrderBook {

    private final PriorityQueue<OrderBookEntry> bidBook; // Highest Prices first
    private final PriorityQueue<OrderBookEntry> askBook; // Lowest Prices first

    public OrderBook() {
        this.bidBook = new PriorityQueue<>(Comparator.comparing(OrderBookEntry::getPrice).reversed());
        this.askBook = new PriorityQueue<>(Comparator.comparing(OrderBookEntry::getPrice));
    }

    public void addOrder(OrderBookEntry entry){
        if(entry.isBid()){
            matchOrder(entry, askBook);
            if (!entry.isFilled()) bidBook.add(entry);
        }else if(entry.isAsk()){
            matchOrder(entry, bidBook);
            if (!entry.isFilled()) askBook.add(entry);
        }
    }

    private void matchOrder(OrderBookEntry entry, PriorityQueue<OrderBookEntry> oppositeBook){
        //FIFO
        while (!oppositeBook.isEmpty() && entry.getQuantity() > 0){
            // Get First order in book
            OrderBookEntry topOrder = oppositeBook.peek();
            // Check if prices match
            if (entry.isBid() && entry.getPrice() >= topOrder.getPrice() ||
                entry.isAsk() && entry.getPrice() <= topOrder.getPrice()){
                // Start matching
                double matchQuantity = Math.min(entry.getQuantity(), topOrder.getQuantity());
                entry.reduceQuantity(matchQuantity);
                topOrder.reduceQuantity(matchQuantity);
                //Remove topOrder if fully matched
                if(topOrder.isFilled()) oppositeBook.poll();
            }
            else { // No further orders will be matched
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "Bids: " + bidBook.toString() + "Asks:" + askBook.toString();
    }
}
