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

    public Side getLevellSide(){
        assert orders.peek() != null;
        return orders.peek().getSide();
    }

    public double getLevelPrice(){
        assert orders.peek() != null;
        return orders.peek().getPrice();
    }

    @Override
    public int compareTo(PriceLevel other){
        return Double.compare(this.getLevelPrice(), other.getLevelPrice());
    }

}
