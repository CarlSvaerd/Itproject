// Fetches the latest traded price for a symbol from Yahoo Finance.
// Yahoo's chart endpoint is the same public source that yfinance uses.
// If Yahoo cannot be reached, a random price between 95 and 105 is used instead.

package simulator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;

public class MarketPrice
{
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Random random = new Random();


    // Latest price, or a random price between 95 and 105 if Yahoo Finance cannot be reached
    public double getPriceOfStock(String symbol)
    {
        double price;
        try
        {
            price = fetchPriceFromYahoo(symbol);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            price = randomPrice(symbol);
        }
        catch (IOException | RuntimeException e)
        {
            price = randomPrice(symbol);
        }

        double priceRoundedToTwoDecimals = Math.round(price * 100.0) / 100.0;

        return priceRoundedToTwoDecimals;
    }


    private double fetchPriceFromYahoo(String symbol) throws IOException, InterruptedException
    {
        String yahooSymbol = yahooSymbolFor(symbol);
        String json = downloadChartJson(yahooSymbol);
        double price = readPrice(json);
        
        return price;
    }


    // Stockholm names need the Yahoo exchange suffix. Other symbols are used as they are.
    private String yahooSymbolFor(String symbol)
    {
        if (symbol.equals("VOLV-B") || symbol.equals("ERIC-B"))
        {
            return symbol + ".ST";
        }

        return symbol;
    }


    private String downloadChartJson(String yahooSymbol) throws IOException, InterruptedException
    {
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + yahooSymbol;
        
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .timeout(Duration.ofSeconds(5))
                .build();

        String chartJson = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        
        return chartJson;
    }


    // The answer contains ..."regularMarketPrice":342.37,...
    private double readPrice(String json) throws IOException
    {
        String key = "\"regularMarketPrice\":";
        int start = json.indexOf(key);
        if (start < 0)
        {
            throw new IOException("Price is missing");
        }

        start = start + key.length();
        int end = json.indexOf(',', start);
        String priceText = json.substring(start, end);
        double price = Double.parseDouble(priceText);
        
        return price;
    }


    private double randomPrice(String symbol)
    {
        System.out.println("Could not fetch price for " + symbol + ". Using a price between 95 and 105.");
        
        double priceBetween95And105 = 95 + 10 * random.nextDouble();
        
        return priceBetween95And105;
    }
}
