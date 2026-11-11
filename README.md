# Exchange Simulator / Exchange Lab

A Java exchange simulator with a local browser dashboard for inspecting limit orders, matching, and trades.

## Requirements

- JDK 17 or newer (`java` and `javac` available on your PATH).
- Git to clone the repository, and a modern browser.
- No Node.js, npm, frontend libraries, or database installation required.

## Download

Clone this repository using its GitHub **Code** button URL, then open a terminal in the cloned folder (the folder containing `start.ps1`, `src_2`, and `web`). Alternatively, download and extract the ZIP.

## Run on Windows

From PowerShell in the project folder:

```powershell
powershell -ExecutionPolicy Bypass -File .\start.ps1
```

The script compiles the Java sources and starts the server. Open **http://localhost:8080**. Keep the terminal running; press **Ctrl+C** to stop.

## Run on macOS or Linux

From the project folder:

```bash
javac -d out src_2/engine/domain/*.java src_2/engine/*.java src_2/serialization/*.java src_2/api/*.java src_2/simulator/*.java src_2/Main.java
java -cp out Main
```

Open **http://localhost:8080**. Always start the application from the project root so it can find `web/` and `data/`.

## Verify the dashboard

A fresh clone starts with an empty market. Select **AAPL** and leave the browser simulation paused for this example:

1. Select **Sell**, enter price **99**, quantity **12**, and account **100001**. Submit. The sell book should show 12 shares at 99.
2. Select **Buy**, enter price **100**, quantity **20**, and account **100002**. Submit.
3. Confirm that **12 shares execute at 99**, the sell order disappears, and **8 shares remain bid at 100**.
4. The last trade should be **99.00**, shares traded **12**, and the trade tape should contain one execution. The execution details should describe the partial fill.
5. Try **Step once** to generate one order, then **Play**, change the order interval, and **Pause**. Switch between AAPL, VOLV-B, and ERIC-B to inspect each instrument.
6. Stop and restart the server. Resting orders and trade history should remain.

Matching uses price–time priority and executes at the resting order's price. Partial fills are supported. The dashboard shows eight price levels per side, the last 60 executions in the chart, and the last 30 trades in the tape. Summary statistics include all stored trades for the selected instrument. Chart points are spaced by trade sequence, not elapsed time.

The dashboard refreshes every two seconds and after its own submissions. The execution-details panel describes orders submitted through this browser; external demo orders update the book, chart, and tape.

## Generate demo orders

Two existing Java demo modes are available after compiling. **Stop the normal server before running either command**: the HTTP demo starts its own server, and batch mode writes the same saved market files.

```bash
# Generate 10 orders directly, then exit
java -cp out Main 10

# Start a server, send orders for 20 seconds at a nominal 750 ms interval, then stop
java -cp out Main --simulate 20 750

# Start the normal server again to inspect the saved results
java -cp out Main
```

The Java demos fetch reference prices from Yahoo Finance, falling back to random prices between 95 and 105 if retrieval fails. Network delays can reduce the number of orders generated. Random orders do not guarantee matches.

The dashboard's Play/Step generator instead uses prices around the current book or last trade, starting at 100 for an empty market. It runs while the page is open; Pause affects only that browser's generator. An order already submitted can still complete. Combining manual examples and Yahoo-based demo orders can cause large jumps in the chart. Trades are simulated, not real financial transactions.

## Saved data and a clean test

The app creates `data/orders.json` and `data/trades.json`. These local runtime files are ignored by Git, so sample data is not included in a fresh clone.

To start a clean test without losing old results, stop the server and rename `data/` to an unused backup name, then restart. The app will create new files as orders arrive. Do not edit or move the data while the server is running.

## HTTP API

| Method | Endpoint | Result |
| --- | --- | --- |
| GET | `/orders` | Resting orders as JSON; `[]` for an empty market |
| POST | `/orders` | Submit a limit order; returns an acknowledgement |
| GET | `/trades` | Stored trade history as JSON |

Example in PowerShell while the server is running:

```powershell
$order = @{
    order_id = [guid]::NewGuid().ToString()
    account_id = 100001
    symbol = 'AAPL'
    side = 'buy'
    price = 100
    quantity = 10
} | ConvertTo-Json
Invoke-RestMethod http://localhost:8080/orders -Method Post -ContentType 'application/json' -Body $order
Invoke-RestMethod http://localhost:8080/orders
Invoke-RestMethod http://localhost:8080/trades
```

Use a new UUID for each submitted order. Rejected orders return HTTP 400 with a reason or error.

## Project layout

```text
src_2/
  Main.java              Server and demo entry point
  api/                   HTTP API and dashboard asset serving
  engine/                Validation, matching, and persistence
  engine/domain/         Orders, order book, trades, and JSON parsing
  simulator/             Random order sender and Yahoo reference prices
  serialization/         JSON output
web/
  index.html             Dashboard markup
  style.css              Visual presentation
  app.js                 Dashboard interaction and API integration
start.ps1                Windows build-and-run script
out/                     Compiled classes (generated, ignored)
data/                    Local saved market (generated, ignored)
```

## Troubleshooting

- **java/javac not found:** install a JDK (not only a JRE), add its `bin` directory to PATH, and reopen the terminal.
- **Address already in use:** stop the other process using port 8080, often an earlier simulator terminal. Only one server should run at a time.
- **Page not found:** run from the repository root and ensure the `web/` folder is present.
- **Connection error:** ensure the Java terminal is still running, then reload the page.
- **No trades:** orders must use the same symbol and overlapping buy/sell prices. Try the deterministic example above on an empty market.

This is an educational local application, with no authentication or production deployment setup. There is no separate automated test framework; use the verification steps above to check a fresh installation.
