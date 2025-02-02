import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class OrderBookEntry {
    private final String symbol;
    private final Double price;
    private Double quantity; // quantity can't be final as it will be reduced when matching algorithm works
    private final OrderType orderType;
    private final Side side;
    private final LocalDateTime timestamp;

    public OrderBookEntry(String symbol, Double price, Double quantity, Side side) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.side = side;
        this.orderType = OrderType.MARKET;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isBid(){
        return side.equals(Side.BUY);
    }

    public boolean isAsk(){
        return side.equals(Side.SELL);
    }

    public boolean isFilled(){
        return quantity <= 0.0;
    }

    public void reduceQuantity(Double quantity){
        this.quantity -= quantity;
    }

}
