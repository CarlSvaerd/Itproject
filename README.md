# Exchange Simulator

En handelssimulator.

## Projektstruktur

```
exchange-simulator/
├── src_2/
│   ├── Main.java                 # Startpunkt (server, batch, demo)
│   ├── api/                      # HTTP-API
│   │   ├── Exchange.java         # Server på port 8080
│   │   ├── Orders.java           # POST /orders
│   │   └── Trades.java           # GET /trades
│   ├── engine/
│   │   ├── MatchingEngine.java   # Validering, matchning, persistens
│   │   └── domain/               # Order, OrderBook, Trade, JSON-parser
│   ├── simulator/
│   │   └── OrderSimulator.java   # Slumpordrar (direkt eller via HTTP)
│   └── serialization/
│       └── JsonSerializer.java
├── data/
│   ├── orders.json               # Aktiva ordrar i orderboken
│   └── trades.json               # Affärshistorik
└── out/                          # Kompilerade klasser (skapas vid build)
```

## Bygga

Kör från projektroten `exchange-simulator/`:

```bash
javac -d out \
  src_2/engine/domain/*.java \
  src_2/engine/*.java \
  src_2/serialization/*.java \
  src_2/api/*.java \
  src_2/simulator/*.java \
  src_2/Main.java
```

## Köra

Alla kommandon använder classpath `out`:

| Kommando | Vad det gör |
|----------|-------------|
| `java -cp out Main` | Startar HTTP-server på [http://localhost:8080](http://localhost:8080) |
| `java -cp out Main 10` | Genererar 10 slumpordrar direkt mot motorn (ingen server) |
| `java -cp out Main --simulate 10 1000` | Startar servern, skickar ordrar via HTTP i 10 s (1 order/sek) och stänger |

### API

**Hur man kan testa att lägga en order** – `POST /orders`:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"order_id":"550e8400-e29b-41d4-a716-446655440000","account_id":100001,"symbol":"AAPL","side":"buy","price":100,"quantity":10}'
```

Svar: Ordern utskriven.

**Hämta affärer** – `GET /trades`:

```bash
curl http://localhost:8080/trades
```

Svar: Skriver ut alla trades.


## Testa

Projektet har inget separat testramverk (JUnit m.m.). Verifiera manuellt så här:

1. **Bygg** enligt kommandot ovan.
2. **Batch:** `java -cp out Main 10` – kontrollera att rader skrivs ut med `ACCEPTED` och att `data/orders.json` och `data/trades.json` uppdateras.
3. **Server + demo:** `java -cp out Main --simulate 5 1000` – ska avslutas utan fel och logga `Sent order … HTTP 200`.
4. **API:** Starta `java -cp out Main` i en terminal. I en annan terminal:
   - `curl http://localhost:8080/trades` (tom lista eller befintlig historik)
   - Skicka en köp- och en säljorder med samma symbol och överlappande pris
   - Hämta `/trades` igen och bekräfta att en affär tillkommit