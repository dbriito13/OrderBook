import java.time.LocalDateTime;

public class OrderBookEntry {
    private Double price;
    private Double quantity;
    private OrderType orderType;
    private Side side;
    private LocalDateTime timestamp;
    private long orderId;

    public OrderBookEntry(){

    }
}
