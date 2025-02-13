# Low Latency OrderBook

Java low-latency implementation of a simple orderbook with FIFO order matching algorithm. Achieving near-zero GC execution, and efficient order addition, modification and deletion by utilizing:
- Object Pooling of pre-allocated objects ensures minimal GC activations
- Hot Start: Object pools are filled prior to execution avoiding latency on object allocation.
- Efficient Order Matching
