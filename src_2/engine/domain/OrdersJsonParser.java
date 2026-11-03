package engine.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class OrdersJsonParser {
    private final String json;
    private int index;

    private OrdersJsonParser(String json) {
        this.json = json;
    }

    static List<Order> parse(String json) {
        return new OrdersJsonParser(json).parseOrders();
    }

    public static Order parseNewOrder(String json) {
        OrdersJsonParser parser = new OrdersJsonParser(json);
        parser.skipWhitespace();
        Order order = parser.parseNewOrder();
        parser.skipWhitespace();
        if (parser.index != json.length()) {
            throw parser.error("Unexpected content after order");
        }
        return order;
    }
    public static List<Trade> parseTrades(String json) {
        OrdersJsonParser parser = new OrdersJsonParser(json);
        List<Trade> trades = new ArrayList<>();
        parser.skipWhitespace();
        parser.expect('[');
        parser.skipWhitespace();
        if (!parser.consume(']')) {
            do {
                parser.skipWhitespace();
                trades.add(toTrade(parser.parseOrderFields()));
                parser.skipWhitespace();
            } while (parser.consume(','));
            parser.expect(']');
        }
        parser.skipWhitespace();
        if (parser.index != json.length()) {
            throw parser.error("Unexpected content after trades array");
        }
        return trades;
    }

    private static Trade toTrade(Map<String, Value> fields) {
        return new Trade(
                UUID.fromString(stringField(fields, "trade_id")),
                stringField(fields, "symbol"),
                Double.parseDouble(numberField(fields, "price")),
                Integer.parseInt(numberField(fields, "quantity")),
                UUID.fromString(stringField(fields, "buy_order_id")),
                Integer.parseInt(numberField(fields, "buy_account_id")),
                UUID.fromString(stringField(fields, "sell_order_id")),
                Integer.parseInt(numberField(fields, "sell_account_id")),
                Instant.parse(stringField(fields, "timestamp")),
                Integer.parseInt(numberField(fields, "remaining_quantity")));
    }
    private List<Order> parseOrders() {
        List<Order> orders = new ArrayList<>();
        skipWhitespace();
        expect('[');
        skipWhitespace();
        if (!consume(']')) {
            do {
                skipWhitespace();
                orders.add(parseOrder());
                skipWhitespace();
            } while (consume(','));
            expect(']');
        }
        skipWhitespace();
        if (index != json.length()) {
            throw error("Unexpected content after orders array");
        }
        return orders;
    }

    private Order parseOrder() {
        Map<String, Value> fields = parseOrderFields();
        return new Order(
                UUID.fromString(stringField(fields, "order_id")),
                Integer.parseInt(numberField(fields, "account_id")),
                stringField(fields, "symbol"),
                Order.Side.valueOf(stringField(fields, "side").toUpperCase(Locale.ROOT)),
                Double.parseDouble(numberField(fields, "price")),
                Integer.parseInt(numberField(fields, "quantity")),
                Integer.parseInt(numberField(fields, "remaining_quantity")),
                Instant.parse(stringField(fields, "timestamp")));
    }

    private Order parseNewOrder() {
        Map<String, Value> fields = parseOrderFields();
        int quantity = Integer.parseInt(numberField(fields, "quantity"));
        Value orderId = fields.get("order_id");
        Value remainingQuantity = fields.get("remaining_quantity");
        Value timestamp = fields.get("timestamp");
        return new Order(
                orderId == null ? UUID.randomUUID() : UUID.fromString(stringValue(orderId, "order_id")),
                Integer.parseInt(numberField(fields, "account_id")),
                stringField(fields, "symbol"),
                Order.Side.valueOf(stringField(fields, "side").toUpperCase(Locale.ROOT)),
                Double.parseDouble(numberField(fields, "price")),
                quantity,
                remainingQuantity == null ? quantity : Integer.parseInt(numberValue(remainingQuantity, "remaining_quantity")),
                timestamp == null ? Instant.now() : Instant.parse(stringValue(timestamp, "timestamp")));
    }

    private Map<String, Value> parseOrderFields() {
        Map<String, Value> fields = new HashMap<>();
        expect('{');
        skipWhitespace();
        if (!consume('}')) {
            do {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                skipWhitespace();
                if (fields.putIfAbsent(key, parseValue()) != null) {
                    throw error("Duplicate field: " + key);
                }
                skipWhitespace();
            } while (consume(','));
            expect('}');
        }
        return fields;
    }

    private Value parseValue() {
        if (index < json.length() && json.charAt(index) == '"') {
            return new Value(parseString(), true);
        }
        int start = index;
        while (index < json.length() && ",}] \t\r\n".indexOf(json.charAt(index)) == -1) {
            index++;
        }
        if (start == index) {
            throw error("Expected a field value");
        }
        String value = json.substring(start, index);
        if (!value.matches("-?(0|[1-9]\\d*)(\\.\\d+)?([eE][+-]?\\d+)?")) {
            throw error("Expected a string or number");
        }
        return new Value(value, false);
    }

    private String parseString() {
        expect('"');
        StringBuilder value = new StringBuilder();
        while (index < json.length()) {
            char current = json.charAt(index++);
            if (current == '"') {
                return value.toString();
            }
            if (current == '\\') {
                if (index == json.length()) {
                    throw error("Unterminated escape sequence");
                }
                char escaped = json.charAt(index++);
                switch (escaped) {
                    case '"':
                    case '\\':
                    case '/':
                        value.append(escaped);
                        break;
                    case 'b':
                        value.append('\b');
                        break;
                    case 'f':
                        value.append('\f');
                        break;
                    case 'n':
                        value.append('\n');
                        break;
                    case 'r':
                        value.append('\r');
                        break;
                    case 't':
                        value.append('\t');
                        break;
                    case 'u':
                        value.append(parseUnicodeEscape());
                        break;
                    default:
                        throw error("Invalid escape sequence");
                }
            } else if (current < 0x20) {
                throw error("Unescaped control character in string");
            } else {
                value.append(current);
            }
        }
        throw error("Unterminated string");
    }

    private char parseUnicodeEscape() {
        if (index + 4 > json.length()) {
            throw error("Incomplete Unicode escape");
        }
        try {
            char value = (char) Integer.parseInt(json.substring(index, index + 4), 16);
            index += 4;
            return value;
        } catch (NumberFormatException e) {
            throw error("Invalid Unicode escape");
        }
    }

    private static String stringField(Map<String, Value> fields, String name) {
        Value value = required(fields, name);
        if (!value.string()) {
            throw new IllegalArgumentException("Expected string field: " + name);
        }
        return value.text();
    }

    private static String numberField(Map<String, Value> fields, String name) {
        Value value = required(fields, name);
        return numberValue(value, name);
    }

    private static String stringValue(Value value, String name) {
        if (!value.string()) {
            throw new IllegalArgumentException("Expected string field: " + name);
        }
        return value.text();
    }

    private static String numberValue(Value value, String name) {
        if (value.string()) {
            throw new IllegalArgumentException("Expected numeric field: " + name);
        }
        return value.text();
    }

    private static Value required(Map<String, Value> fields, String name) {
        Value value = fields.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Missing order field: " + name);
        }
        return value;
    }

    private void skipWhitespace() {
        while (index < json.length() && " \t\r\n".indexOf(json.charAt(index)) >= 0) {
            index++;
        }
    }

    private boolean consume(char expected) {
        if (index < json.length() && json.charAt(index) == expected) {
            index++;
            return true;
        }
        return false;
    }

    private void expect(char expected) {
        if (!consume(expected)) {
            throw error("Expected '" + expected + "'");
        }
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " at position " + index);
    }

    private static final class Value {
        private final String text;
        private final boolean string;

        private Value(String text, boolean string) {
            this.text = text;
            this.string = string;
        }

        private String text() {
            return text;
        }

        private boolean string() {
            return string;
        }
    }
}
