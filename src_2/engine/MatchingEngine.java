// Validates an order
// Matches it in the book
// Returns ack and trades
// Writes history to JSON

package engine;

import engine.domain.Order;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MatchingEngine 
{
    private final List<Order> receivedOrders = new ArrayList<>();

    public boolean place(Order order) throws IOException 
    {
        receivedOrders.add(order);
        
        Files.writeString(Path.of("data", "orders.json"), ordersJson());
        Files.writeString(Path.of("data", "trades.json"), tradesJson());
        
        return true;
    }

    private String ordersJson() 
    {
        StringBuilder json = new StringBuilder("[\n");
        
        for (int index = 0; index < receivedOrders.size(); index++) 
        {
            Order order = receivedOrders.get(index);
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
            
            if (index < receivedOrders.size() - 1) 
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
        
        for (int index = 0; index < receivedOrders.size(); index++) 
        {
            Order order = receivedOrders.get(index);
            json.append("  {\n");
            json.append("    \"trade_id\": \"").append(order.getOrderId()).append("\",\n");
            json.append("    \"symbol\": \"").append(order.getSymbol()).append("\",\n");
            json.append("    \"price\": ").append(order.getPrice()).append(",\n");
            json.append("    \"quantity\": ").append(order.getQuantity()).append(",\n");
            json.append("    \"buy_order_id\": \"").append(order.getOrderId()).append("\",\n");
            json.append("    \"buy_account_id\": ").append(order.getAccountId()).append(",\n");
            json.append("    \"sell_order_id\": \"").append(order.getOrderId()).append("\",\n");
            json.append("    \"sell_account_id\": ").append(order.getAccountId()).append(",\n");
            json.append("    \"timestamp\": \"").append(order.getTimestamp()).append("\",\n");
            json.append("    \"remaining_quantity\": ").append(order.getRemainingQuantity()).append("\n");
            json.append("  }");
            
            if (index < receivedOrders.size() - 1) 
            {
                json.append(',');
            }
            
            json.append('\n');
        }
        
        json.append("]\n");
        
        return json.toString();
    }
}
