package serialization;

import engine.domain.Order;
import engine.domain.OrderAck;
import engine.domain.Trade;
import java.util.List;
import java.util.Locale;

public final class JsonSerializer {
    private JsonSerializer() {
    }

    public static String orderToJson(Order order) {
        return "{"
                + "\"order_id\":\"" + order.getOrderId() + "\","
                + "\"account_id\":" + order.getAccountId() + ","
                + "\"symbol\":" + quote(order.getSymbol()) + ","
                + "\"side\":\"" + order.getSide().name().toLowerCase(Locale.ROOT) + "\","
                + "\"price\":" + order.getPrice() + ","
                + "\"quantity\":" + order.getQuantity() + ","
                + "\"remaining_quantity\":" + order.getRemainingQuantity() + ","
                + "\"timestamp\":" + quote(order.getTimestamp().toString())
                + "}";
    }

    public static String ordersToJson(List<Order> orders) {
        StringBuilder json = new StringBuilder("[\n");
        for (int index = 0; index < orders.size(); index++) {
            Order order = orders.get(index);
            json.append("  {\n");
            json.append("    \"order_id\": \"").append(order.getOrderId()).append("\",\n");
            json.append("    \"account_id\": ").append(order.getAccountId()).append(",\n");
            json.append("    \"symbol\": ").append(quote(order.getSymbol())).append(",\n");
            json.append("    \"side\": \"").append(order.getSide().name().toLowerCase(Locale.ROOT)).append("\",\n");
            json.append("    \"price\": ").append(order.getPrice()).append(",\n");
            json.append("    \"quantity\": ").append(order.getQuantity()).append(",\n");
            json.append("    \"remaining_quantity\": ").append(order.getRemainingQuantity()).append(",\n");
            json.append("    \"timestamp\": ").append(quote(order.getTimestamp().toString())).append('\n');
            json.append("  }");
            if (index < orders.size() - 1) {
                json.append(',');
            }
            json.append('\n');
        }
        return json.append("]\n").toString();
    }

    public static String tradesToJson(List<Trade> trades) {
        StringBuilder json = new StringBuilder("[\n");
        for (int index = 0; index < trades.size(); index++) {
            Trade trade = trades.get(index);
            json.append("  {\n");
            json.append("    \"trade_id\": \"").append(trade.getTradeId()).append("\",\n");
            json.append("    \"symbol\": ").append(quote(trade.getSymbol())).append(",\n");
            json.append("    \"price\": ").append(trade.getPrice()).append(",\n");
            json.append("    \"quantity\": ").append(trade.getQuantity()).append(",\n");
            json.append("    \"buy_order_id\": \"").append(trade.getBuyOrderId()).append("\",\n");
            json.append("    \"buy_account_id\": ").append(trade.getBuyAccountId()).append(",\n");
            json.append("    \"sell_order_id\": \"").append(trade.getSellOrderId()).append("\",\n");
            json.append("    \"sell_account_id\": ").append(trade.getSellAccountId()).append(",\n");
            json.append("    \"timestamp\": ").append(quote(trade.getTimestamp().toString())).append(",\n");
            json.append("    \"remaining_quantity\": ").append(trade.getRemainingQuantity()).append('\n');
            json.append("  }");
            if (index < trades.size() - 1) {
                json.append(',');
            }
            json.append('\n');
        }
        return json.append("]\n").toString();
    }

    public static String orderAckToJson(OrderAck ack) {
        return "{\n"
                + "  \"order_id\": \"" + ack.getOrderId() + "\",\n"
                + "  \"account_id\": " + ack.getAccountId() + ",\n"
                + "  \"status\": \"" + ack.getStatus().name().toLowerCase(Locale.ROOT) + "\",\n"
                + "  \"reject_reason\": " + (ack.getRejectReason() == null ? "null" : quote(ack.getRejectReason())) + ",\n"
                + "  \"timestamp\": " + quote(ack.getTimestamp().toString()) + "\n"
                + "}\n";
    }

    public static String errorToJson(String message) {
        return "{\"error\":" + quote(message) + "}";
    }

    private static String quote(String value) {
        StringBuilder json = new StringBuilder(value.length() + 2).append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"':
                    json.append("\\\"");
                    break;
                case '\\':
                    json.append("\\\\");
                    break;
                case '\b':
                    json.append("\\b");
                    break;
                case '\f':
                    json.append("\\f");
                    break;
                case '\n':
                    json.append("\\n");
                    break;
                case '\r':
                    json.append("\\r");
                    break;
                case '\t':
                    json.append("\\t");
                    break;
                default:
                    if (character < 0x20) {
                        json.append(String.format("\\u%04x", (int) character));
                    } else {
                        json.append(character);
                    }
            }
        }
        return json.append('"').toString();
    }
}
