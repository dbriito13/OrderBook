import lombok.Getter;

import java.util.*;

@Getter
public class OrderBook {

    private final TreeSet<PriceLevel> bidBook; // Highest Prices first
    private final TreeSet<PriceLevel> askBook; // Lowest Prices first

    public OrderBook() {
        this.bidBook = new TreeSet<>(Comparator.reverseOrder());
        this.askBook = new TreeSet<>();
    }

    public void addOrder(OrderBookEntry entry){
        matchOrder(entry, entry.isBid() ? askBook: bidBook);
        if (!entry.isFilled()){
            PriceLevel level = new PriceLevel(entry);
            TreeSet<PriceLevel> book = entry.isBid()? bidBook: askBook;
            if (book.contains(level)){
                Objects.requireNonNull(book.floor(level)).addOrder(entry);
            } else {
                // Add new PriceLevel to corresponding book
                book.add(level);
            }
        }
    }

    private void matchOrder(OrderBookEntry entry, TreeSet<PriceLevel> oppositeBook){
        //FIFO
        while (!oppositeBook.isEmpty() && entry.getQuantity() > 0){
            // Get First Price Level in book
            PriceLevel topLevel = oppositeBook.first();
            // Check if prices don't match
            if (entry.isBid() && entry.getPrice() < topLevel.getLevelPrice() ||
                entry.isAsk() && entry.getPrice() > topLevel.getLevelPrice()) {
                break;
            }
                // Reduce both incoming entry and available price level by matching amount
                topLevel.reduceQuantity(entry);

                //Remove level if fully matched
                if(topLevel.isEmpty()) oppositeBook.pollFirst();
        }
    }

    @Override
    public String toString() {
        return "Bids: " + bidBook.toString() + "Asks:" + askBook.toString();
    }
}
