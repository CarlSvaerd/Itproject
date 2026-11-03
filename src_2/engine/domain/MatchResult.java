package engine.domain;

import java.util.List;

public class MatchResult {
    private final OrderAck ack;
    private final List<Trade> trades;

    public MatchResult(OrderAck ack, List<Trade> trades) {
        this.ack = ack;
        this.trades = trades;
    }

    public OrderAck getAck() {
        return ack;
    }

    public List<Trade> getTrades() {
        return trades;
    }
}