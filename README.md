# Exchange Simulator

En handelssimulator.

## Projektstruktur

```
exchange-simulator/
├── src_2/
│   ├── Main.java                 # Startpunkt (server, batch, demo)
│   ├── api/                      # HTTP-API
│   │   ├── Exchange.java         # Server på port 8080
│   │   ├── Orders.java           # POST /orders och GET /orders
│   │   └── Trades.java           # GET /trades
│   ├── engine/
│   │   ├── MatchingEngine.java   # Validering, matchning, persistens
│   │   └── domain/               # Order, OrderBook, Trade, JSON-parser
│   ├── simulator/
│   │   ├── OrderSimulator.java   # Slumpordrar (direkt eller via HTTP)
│   │   └── MarketPrice.java      # Senaste pris från Yahoo Finance
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
| `java -cp out Main 10` | Genererar 10 slumpordrar direkt mot motorn (ingen server). Priset är senaste kursen från Yahoo Finance. |
| `java -cp out Main --simulate 10 1000` | Startar servern, skickar ordrar via HTTP i 10 s (1 order/sek) och stänger |

Priset hämtas från Yahoo Finance. Om anropet misslyckas slumpas priset mellan 95 och 105.

### API

**Hur man kan testa att lägga en order** – `POST /orders`:

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"order_id":"550e8400-e29b-41d4-a716-446655440000","account_id":100001,"symbol":"AAPL","side":"buy","price":100,"quantity":10}'
```

Svar: Ordern utskriven.

**Hämta orderboken** – `GET /orders`:

```bash
curl http://localhost:8080/orders
```

Svar: Skriver ut orderboken i JSON. Om ingen order ligger i boken returneras `[]`.

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
4. **API:** Starta `java -cp out Main` i en terminal. 

    I en annan terminal:
   - `curl http://localhost:8080/orders` (aktiva ordrar, eller `[]`)
   - `curl http://localhost:8080/trades` (tom lista eller befintlig historik)
   - Skicka en köp- och en säljorder med samma symbol och överlappande pris
   - Hämta `/trades` igen och bekräfta att en affär tillkommit

## Visualisering – Exchange Lab

Projektet har nu ett webbgränssnitt som använder samma Java-server och matchningsmotor.
Gränssnittet visar orderbok med volymstaplar, senaste avslutspriser, affärshistorik och
marknadsöversikt för AAPL, VOLV-B och ERIC-B. Du kan lägga köp- och säljorder manuellt
och se en förklaring av avslut och kvarvarande antal.

### Hämta och starta

Kräver JDK 17 eller senare, med `java` och `javac` på PATH, samt en modern webbläsare.
Inga frontendberoenden, Node.js eller separat databas behövs.

```bash
git clone https://github.com/CarlSvaerd/Itproject.git
cd Itproject
```

På Windows, kör från PowerShell i projektmappen:

```powershell
powershell -ExecutionPolicy Bypass -File .\start.ps1
```

Skriptet bygger och startar servern. På macOS/Linux används byggkommandot ovan och
`java -cp out Main`. Projektroten är här mappen `Itproject/` som innehåller `src_2/` och `web/`.

Öppna [http://localhost:8080](http://localhost:8080). Låt terminalen vara igång och
avsluta med Ctrl+C. Om port 8080 redan används, stoppa den tidigare servern först.

### Kontroller och visning

- **Buy/Sell:** lägg en limitorder med pris, antal och ett sexsiffrigt konto-ID.
- **Step once:** generera en order via API:t.
- **Play/Pause:** starta eller pausa webbläsarens ordergenerator.
- **Order interval:** välj hur ofta nya order genereras.
- **Instrument:** växla mellan aktiernas orderböcker och affärer.

Orderboken visar högst åtta prisnivåer per sida, diagrammet de senaste 60 avsluten
och affärslistan de senaste 30. Sammanställningen omfattar alla sparade affärer för
vald aktie. Diagrammets punkter är jämnt fördelade i avslutsordning, inte efter tidsavstånd.
Data uppdateras varannan sekund och efter egna order.

Webbläsarens generator utgår från orderboken eller senaste avslutet, med startpris 100
på en tom marknad. Java-demon ovan använder däremot Yahoo-priser eller reservpriser
mellan 95 och 105. Att blanda dessa med manuella testpriser kan ge stora hopp i diagrammet.
Paus stoppar bara denna webbläsares generator; en redan skickad order kan slutföras.
Förklaringen i **Execution details** gäller order som skickats från webbläsaren.

### Testa en delvis fylld order

Börja med en tom orderbok och pausad simulering. Välj AAPL:

1. Sälj **12** aktier till pris **99**, konto **100001**.
2. Köp **20** aktier med limitpris **100**, konto **100002**.
3. Kontrollera att **12 aktier avslutas till 99**, att säljordern försvinner och
   att **8 aktier ligger kvar som köporder till 100**.
4. Kontrollera att diagrammet, affärslistan och förklaringen uppdateras.
5. Starta om servern och kontrollera att order och affärer finns kvar.

Order och affärer sparas lokalt i `data/`, som inte följer med Git. En ny kloning
börjar därför utan våra tidigare testorder. För ett nytt tomt test: stoppa servern,
byt namn på `data/` till ett ledigt säkerhetskopienamn och starta igen.

Stoppa den vanliga servern innan batchläget eller `--simulate` körs: de använder samma
datafiler och HTTP-demon startar en egen server. Starta sedan `java -cp out Main` igen
för att se resultatet. Detta är en lokal utbildningssimulator utan autentisering.

### Nya filer

- `web/index.html` – gränssnittets struktur och texter.
- `web/style.css` – layout, färger och typografi.
- `web/app.js` – gränssnittets API-koppling och interaktioner.
- `src_2/api/Dashboard.java` – serverar webbgränssnittets filer.
- `start.ps1` – bygger och startar projektet på Windows.

`Exchange.java` har också fått en route för webbgränssnittet. Den befintliga
matchningsmotorn och dess handelslogik är oförändrade.