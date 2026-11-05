package engine.domain;

import java.util.List;

/**
 * MatchResult – everything MatchingEngine.place() returns for one order.
 *
 * Contains:
 *   - ack:    the order acknowledgement (spec section 2), ACCEPTED or REJECTED
 *   - trades: the trades this order produced (spec section 3)
 *
 * The trades list is empty if the order was rejected,
 * or if it was accepted but found no matching counterparty
 * (in that case the order now rests in the book).
 *
 * Used by Main to print results and by api/Orders to build the HTTP response.
 */
public class MatchResult {
    private final OrderAck ack;          // accepted or rejected, with reason if rejected
    private final List<Trade> trades;    // only the NEW trades from this order, not the full history

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