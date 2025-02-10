import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class OrderBookEntry {
    private double price;
    private double quantity; // quantity can't be final as it will be reduced when matching algorithm works
    private OrderType orderType;
    private Side side;
    private final LocalDateTime timestamp;
    private PriceLevel priceLevel;
    private UUID orderId;

    public OrderBookEntry(double price, double quantity, Side side, OrderType orderType) {
        this.quantity = quantity;
        this.price = price;
        this.side = side;
        this.orderType = orderType;
        this.timestamp = LocalDateTime.now();
        this.orderId = UUID.randomUUID();
    }

    public void reset(double price, double quantity, Side side, OrderType orderType){
        this.price = price;
        this.quantity = quantity;
        this.side = side;
        this.orderType = orderType;
        this.orderId = UUID.randomUUID();
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

    public void reduceQuantity(double quantity){
        this.quantity -= quantity;
    }

}
