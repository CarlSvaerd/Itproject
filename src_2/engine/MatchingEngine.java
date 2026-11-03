// Validates an order
// Matches it in the book
// Returns ack and trades
// Writes history to JSON

package engine;

import engine.domain.MatchResult;
import engine.domain.Order;
import engine.domain.OrderAck;
import engine.domain.OrderBook;
import engine.domain.Trade;

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

    public MatchResult place(Order order) throws IOException
    {
        String rejectReason = validate(order);
        if (rejectReason != null)
        {
            OrderAck rejected = new OrderAck(order.getOrderId(), order.getAccountId(),
                    OrderAck.Status.REJECTED, rejectReason, Instant.now());
            return new MatchResult(rejected, List.of());
        }

        OrderAck accepted = new OrderAck(order.getOrderId(), order.getAccountId(),
                OrderAck.Status.ACCEPTED, null, Instant.now());

        List<Trade> newTrades = book.match(order);
        trades.addAll(newTrades);

        if (order.getRemainingQuantity() > 0)
        {
            book.addOrder(order); // det som blev över läggs i boken
        }

        Files.writeString(Path.of("data", "orders.json"), ordersJson());
        Files.writeString(Path.of("data", "trades.json"), tradesJson());

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

    private String ordersJson()
    {
        List<Order> allOrders = new ArrayList<>();
        allOrders.addAll(book.getBuyOrders());
        allOrders.addAll(book.getSellOrders());

        StringBuilder json = new StringBuilder("[\n");

        for (int index = 0; index < allOrders.size(); index++)
        {
            Order order = allOrders.get(index);
            json.append("  {\n");
            json.append("    \"order_id\": \"").append(order.getOrderId()).append("\",\n");
            json.append("    \"account_id\": ").append(order.getAccountId()).append(",\n");
            json.append("    \"symbol\": \"").append(order.getSymbol()).append("\",\n");
            json.append("    \"side\": \"").append(order.getSide().name().toLowerCase()).append("\",\n");
            json.append("    \"price\": ").append(order.getPrice()).append(",\n");
            json.append("    \"quantity\": ").append(order.getQuantity()).append(",\n");
            json.append("    \"remaining_quantity\": ").append(order.getRemainingQuantity()).append(",\n");
            json.append("    \"timestamp\": \"").append(order.getTimestamp()).append("\"\n");
            json.append("  }");

            if (index < allOrders.size() - 1)
            {
                json.append(',');
            }

            json.append('\n');
        }

        json.append("]\n");

        return json.toString();
    }

    private String tradesJson()
    {
        StringBuilder json = new StringBuilder("[\n");

        for (int index = 0; index < trades.size(); index++)
        {
            Trade trade = trades.get(index);
            json.append("  {\n");
            json.append("    \"trade_id\": \"").append(trade.getTradeId()).append("\",\n");
            json.append("    \"symbol\": \"").append(trade.getSymbol()).append("\",\n");
            json.append("    \"price\": ").append(trade.getPrice()).append(",\n");
            json.append("    \"quantity\": ").append(trade.getQuantity()).append(",\n");
            json.append("    \"buy_order_id\": \"").append(trade.getBuyOrderId()).append("\",\n");
            json.append("    \"buy_account_id\": ").append(trade.getBuyAccountId()).append(",\n");
            json.append("    \"sell_order_id\": \"").append(trade.getSellOrderId()).append("\",\n");
            json.append("    \"sell_account_id\": ").append(trade.getSellAccountId()).append(",\n");
            json.append("    \"timestamp\": \"").append(trade.getTimestamp()).append("\",\n");
            json.append("    \"remaining_quantity\": ").append(trade.getRemainingQuantity()).append("\n");
            json.append("  }");

            if (index < trades.size() - 1)
            {
                json.append(',');
            }

            json.append('\n');
        }

        json.append("]\n");

        return json.toString();
    }
}