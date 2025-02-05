import lombok.Getter;

import java.util.Deque;
import java.util.LinkedList;

@Getter
public class PriceLevel implements Comparable<PriceLevel>{
    Deque<OrderBookEntry> orders;
    //If we don't want to keep a list, then each order could point to next? is that less memory?

    public PriceLevel(OrderBookEntry firstEntry){
        orders = new LinkedList<>();
        orders.add(firstEntry);
    }

    public void addOrder(OrderBookEntry entry){
        orders.add(entry);
    }

    public Side getLevellSide(){
        assert orders.peek() != null;
        return orders.peek().getSide();
    }

    public double getLevelPrice(){
        assert orders.peek() != null;
        return orders.peek().getPrice();
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
            if (order.isFilled()) orders.poll();
        }
    }

    @Override
    public int compareTo(PriceLevel other){
        return Double.compare(this.getLevelPrice(), other.getLevelPrice());
    }

}
