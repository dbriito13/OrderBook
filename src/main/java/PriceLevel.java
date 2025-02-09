import lombok.Getter;
import lombok.ToString;

import java.util.*;

@Getter
@ToString
public class PriceLevel implements Comparable<PriceLevel>{
    Deque<OrderBookEntry> orders;
    //If we don't want to keep a list, then each order could point to next? is that less memory?

    public PriceLevel(OrderBookEntry firstEntry){
        orders = new LinkedList<>();
        addOrder(firstEntry);
    }

    public void addOrder(OrderBookEntry entry){
        orders.add(entry);
        entry.setPriceLevel(this);
    }

    public void removeOrder(OrderBookEntry entry){
        orders.remove(entry);
    }

    public void reset(OrderBookEntry entry){
        orders.clear();
        orders.add(entry);
    }

    public double getPrice(){
        return Objects.requireNonNull(orders.peek()).getPrice();
    }

    public boolean isEmpty(){
        return orders.isEmpty();
    }

    public void reduceQuantity(OrderBookEntry matchEntry){
        while(matchEntry.getQuantity() > 0.0 && !orders.isEmpty()){
            OrderBookEntry order = orders.peek();
            double matchQuantity = Math.min(matchEntry.getQuantity(), order.getQuantity());
            order.reduceQuantity(matchQuantity);
            matchEntry.reduceQuantity(matchQuantity);
            if (order.isFilled()) {
                orders.poll();
                OrderBookEntryPool.release(order);
            }
        }
    }

    @Override
    public int compareTo(PriceLevel other){
        return Double.compare(this.getPrice(), other.getPrice());
    }

}
