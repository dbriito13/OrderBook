import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderBookEntry {
    private double price;
    private double quantity; // quantity can't be final as it will be reduced when matching algorithm works
    private OrderType orderType;
    private Side side;
    private PriceLevel priceLevel;
    private long orderId;

    public OrderBookEntry(double price, double quantity, Side side, OrderType orderType, long orderId) {
        this.quantity = quantity;
        this.price = price;
        this.side = side;
        this.orderType = orderType;
        this.orderId = orderId;
    }

    public void reset(double price, double quantity, Side side, OrderType orderType, long orderId){
        this.price = price;
        this.quantity = quantity;
        this.side = side;
        this.orderType = orderType;
        this.orderId = orderId;
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
