import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class OrderBookEntry {
    private final String symbol;
    private final Double price;
    private Double quantity; // quantity can't be final as it will be reduced when matching algorithm works
    private final OrderType orderType;
    private final Side side;
    private final LocalDateTime timestamp;
    private final long orderId;

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
