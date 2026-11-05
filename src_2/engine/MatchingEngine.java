// Validates an order
// Matches it in the book
// Returns ack and trades
// Writes history to JSON

package engine;

import engine.domain.MatchResult;
import engine.domain.Order;
import engine.domain.OrderAck;
import engine.domain.OrderBook;
import engine.domain.OrdersJsonParser;
import engine.domain.Trade;
import serialization.JsonSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MatchingEngine
{
    private final OrderBook book = new OrderBook();
    private final List<Trade> trades = new ArrayList<>();

    public MatchingEngine() throws IOException
    {
        // Keep previous trade history when the engine starts
        Path tradesPath = Path.of("data", "trades.json");
        if (Files.exists(tradesPath))
        {
            trades.addAll(OrdersJsonParser.parseTrades(Files.readString(tradesPath)));
        }
    }

    public MatchResult place(Order order) throws IOException
    {
        // Reject bad orders before they touch the book
        String rejectReason = validate(order);
        if (rejectReason != null)
        {
            OrderAck rejected = new OrderAck(order.getOrderId(), order.getAccountId(),
                    OrderAck.Status.REJECTED, rejectReason, Instant.now());
            
            return new MatchResult(rejected, List.of());
        }

        OrderAck accepted = new OrderAck(order.getOrderId(), order.getAccountId(),
                OrderAck.Status.ACCEPTED, null, Instant.now());

        // Try to match against resting opposite orders
        List<Trade> newTrades = book.match(order);
        trades.addAll(newTrades);

        if (order.getRemainingQuantity() > 0)
        {
            book.addOrder(order);
        }

        List<Order> allOrders = new ArrayList<>();
        allOrders.addAll(book.getBuyOrders());
        allOrders.addAll(book.getSellOrders());
        Path dataDirectory = Path.of("data");
        Files.createDirectories(dataDirectory);
        Files.writeString(dataDirectory.resolve("orders.json"), JsonSerializer.ordersToJson(allOrders));
        Files.writeString(dataDirectory.resolve("trades.json"), JsonSerializer.tradesToJson(trades));

        return new MatchResult(accepted, newTrades);
    }

    private String validate(Order order)
    {
        if (order.getAccountId() < 100000 || order.getAccountId() > 999999) return "account_id must have 6 digits";
        if (order.getSymbol() == null || order.getSymbol().isBlank())       return "symbol is missing";
        if (order.getPrice() <= 0)                                          return "price must be > 0";
        if (order.getQuantity() <= 0)                                       return "quantity must be > 0";
        return null;
    }

}