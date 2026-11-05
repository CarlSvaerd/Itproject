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

/**
 * MatchingEngine – the core of the exchange simulator.
 *
 * Responsibilities:
 *   1. Validate incoming orders (spec section 1).
 *   2. Match them against the order book using price-time priority.
 *   3. Return an acknowledgement (spec section 2) and any resulting trades (spec section 3).
 *   4. Persist the order book and the full trade history (spec section 4) to data/*.json.
 *
 */
public class MatchingEngine
{
    private final OrderBook book = new OrderBook();// loads data/orders.json on creation
    private final List<Trade> trades = new ArrayList<>();

    public MatchingEngine() throws IOException
    {
        // Load previous trades so the history survives restarts
        Path tradesPath = Path.of("data", "trades.json");
        if (Files.exists(tradesPath))
        {
            trades.addAll(OrdersJsonParser.parseTrades(Files.readString(tradesPath)));
        }
    }

    /**
     * Places an order in the market.
     *
     * @param order the incoming order
     * @return the acknowledgement (ACCEPTED or REJECTED) and the trades this order produced
     * @throws IOException if the JSON files cannot be written
     */
    public MatchResult place(Order order) throws IOException
    {
        // 1. Validate – invalid orders are rejected and never reach the book
        String rejectReason = validate(order);
        if (rejectReason != null)
        {
            OrderAck rejected = new OrderAck(order.getOrderId(), order.getAccountId(),
                    OrderAck.Status.REJECTED, rejectReason, Instant.now());

            return new MatchResult(rejected, List.of());
        }

        OrderAck accepted = new OrderAck(order.getOrderId(), order.getAccountId(),
                OrderAck.Status.ACCEPTED, null, Instant.now());

        // 2. Match against the opposite side of the book.
        //    This also reduces remaining quantity on both orders
        //    and removes fully filled resting orders from the book.
        List<Trade> newTrades = book.match(order);
        trades.addAll(newTrades);

        // 3. Whatever could not be matched rests in the book and waits for a counterparty
        if (order.getRemainingQuantity() > 0)
        {
            book.addOrder(order);
        }

        // 4. Persist state. Note: the files are rewritten after every order,
        //    which is simple but gets slower as the history grows.
        List<Order> allOrders = new ArrayList<>();
        allOrders.addAll(book.getBuyOrders());
        allOrders.addAll(book.getSellOrders());
        Path dataDirectory = Path.of("data");
        Files.createDirectories(dataDirectory);
        Files.writeString(dataDirectory.resolve("orders.json"), JsonSerializer.ordersToJson(allOrders));
        Files.writeString(dataDirectory.resolve("trades.json"), JsonSerializer.tradesToJson(trades));

        return new MatchResult(accepted, newTrades);
    }

    /**
     * Checks the order against the spec.
     *
     * @return a reject reason, or null if the order is valid
     */
    private String validate(Order order)
    {
        if (order.getAccountId() < 100000 || order.getAccountId() > 999999) return "account_id must have 6 digits";
        if (order.getSymbol() == null || order.getSymbol().isBlank())       return "symbol is missing";
        if (order.getPrice() <= 0)                                          return "price must be > 0";
        if (order.getQuantity() <= 0)                                       return "quantity must be > 0";
        return null;
    }
}
