'use strict';
const $ = id => document.getElementById(id);
let orders = [], trades = [], side = 'buy', running = false, busy = false, timer;
const priceText = value => Number(value).toFixed(2);
const countText = value => Number(value).toLocaleString();
const symbol = () => $('symbol').value;
async function api(path, options) {
  const response = await fetch(path, { cache: 'no-store', ...options });
  if (!response.ok) {
    let error; try { error = await response.json(); } catch { /* non-JSON server error */ }
    throw new Error(error?.reject_reason || error?.error || `Server returned ${response.status}`);
  }
  return response.json();
}
async function refresh() {
  const [nextOrders, nextTrades] = await Promise.all([api('/orders'), api('/trades')]);
  orders = nextOrders; trades = nextTrades;
  $('connection').textContent = 'Connected to local engine';
  render();
}
function levels(which) {
  const grouped = new Map();
  orders.filter(o => o.symbol === symbol() && o.side === which).forEach(o => {
    const row = grouped.get(o.price) || { price: o.price, quantity: 0, count: 0 };
    row.quantity += o.remaining_quantity; row.count++; grouped.set(o.price, row);
  });
  return [...grouped.values()].sort((a, b) => which === 'buy' ? b.price - a.price : a.price - b.price);
}
function render() {
  const bids = levels('buy'), asks = levels('sell');
  const history = trades.filter(t => t.symbol === symbol());
  const maxDepth = Math.max(1, ...bids.map(l => l.quantity), ...asks.map(l => l.quantity));
  for (const [id, rows, which] of [['bids', bids, 'buy'], ['asks', asks, 'sell']]) {
    $(id).innerHTML = rows.length ? rows.slice(0, 8).map(l => `<div class="level ${which}" style="--depth:${l.quantity / maxDepth * 100}%"><span>${priceText(l.price)}</span><span>${countText(l.quantity)}</span><span>${l.count}</span></div>`).join('') : '<div class="empty">No resting orders.<br>Place an order to start the book.</div>';
  }
  $('bid').textContent = bids.length ? priceText(bids[0].price) : '—';
  $('ask').textContent = asks.length ? priceText(asks[0].price) : '—';
  $('spread').textContent = bids.length && asks.length ? priceText(asks[0].price - bids[0].price) : '—';
  $('last').textContent = history.length ? priceText(history.at(-1).price) : '—';
  $('volume').textContent = countText(history.reduce((sum, t) => sum + t.quantity, 0));
  $('trade-count').textContent = `${history.length} trades`;
  $('tape').innerHTML = history.length ? history.slice(-30).reverse().map(t => `<tr><td>${new Date(t.timestamp).toLocaleTimeString()}</td><td>${priceText(t.price)}</td><td>${countText(t.quantity)}</td><td>${Number(t.buy_account_id)}</td><td>${Number(t.sell_account_id)}</td></tr>`).join('') : '<tr><td colspan="5" class="empty">No trades yet. A buy and sell order need overlapping prices.</td></tr>';
  drawChart(history.slice(-60));
}
function drawChart(history) {
  if (!history.length) { $('chart').innerHTML = '<div class="empty">Your first match starts the chart.<br>Submit a sell at 99, then a buy at 100.</div>'; return; }
  const values = history.map(t => t.price), low = Math.min(...values), high = Math.max(...values);
  const pad = Math.max((high - low) * .2, .5), min = low - pad, max = high + pad;
  const points = history.map((t, i) => [60 + i / Math.max(1, history.length - 1) * 610, 170 - (t.price - min) / (max - min) * 145]);
  const grid = [0, 1, 2, 3].map(i => { const y = 25 + i * 145 / 3; return `<line x1="60" y1="${y}" x2="680" y2="${y}" stroke="#e8ede6"/><text x="0" y="${y + 4}">${priceText(max - i * (max - min) / 3)}</text>`; }).join('');
  $('chart').innerHTML = `<svg viewBox="0 0 700 205" role="img" aria-label="Execution prices for the last ${history.length} trades">${grid}<polyline points="${points.map(p => p.join(',')).join(' ')}" fill="none" stroke="#257559" stroke-width="2.5"/>${points.map((p, i) => `<circle cx="${p[0]}" cy="${p[1]}" r="3.5" fill="#257559"><title>Trade ${i + 1}: ${history[i].quantity} shares at ${priceText(history[i].price)}</title></circle>`).join('')}<text x="60" y="199">Earlier</text><text x="635" y="199">Latest</text></svg>`;
}
function setSide(value) {
  side = value;
  $('buy-side').setAttribute('aria-pressed', String(side === 'buy'));
  $('sell-side').setAttribute('aria-pressed', String(side === 'sell'));
  $('submit').textContent = `Submit ${side} order ↗`;
}
function pause() { running = false; clearTimeout(timer); $('play').textContent = '▶ Play'; $('sim-status').textContent = 'Paused · controls affect this browser’s generator.'; }
function fail(error) { pause(); $('connection').textContent = 'Connection or request error'; $('explanation').textContent = `${error.message}. Check that the Java server is running. Refresh before retrying an uncertain submission.`; }
async function submitOrder(which, price, quantity, account) {
  if (busy) return;
  busy = true; $('submit').disabled = true; $('step').disabled = true;
  const selected = symbol(), id = crypto.randomUUID();
  try {
    await api('/orders', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ order_id: id, account_id: account, symbol: selected, side: which, price, quantity }) });
    await refresh();
    const fills = trades.filter(t => t.buy_order_id === id || t.sell_order_id === id);
    const filled = fills.reduce((sum, t) => sum + t.quantity, 0);
    const remaining = quantity - filled;
    const details = fills.map(t => `${t.quantity} at ${priceText(t.price)}`).join(', ');
    $('explanation').textContent = `${which === 'buy' ? 'Buy' : 'Sell'} ${quantity} ${selected} at a limit of ${priceText(price)}. ` + (filled ? `${filled} shares filled (${details}). Trades execute at the resting order’s price. ` : 'No matching price on the opposite side. ') + (remaining ? `${remaining} shares now wait in the book.` : 'The order is fully filled.');
  } catch (error) { fail(error); }
  finally { busy = false; $('submit').disabled = false; $('step').disabled = false; }
}
async function step() {
  if (busy) return;
  const bids = levels('buy'), asks = levels('sell'), history = trades.filter(t => t.symbol === symbol());
  const anchor = bids.length && asks.length ? (bids[0].price + asks[0].price) / 2 : history.at(-1)?.price ?? bids[0]?.price ?? asks[0]?.price ?? 100;
  await submitOrder(Math.random() < .5 ? 'buy' : 'sell', Math.max(.01, Math.round((anchor + (Math.random() - .5) * 4) * 100) / 100), 1 + Math.floor(Math.random() * 40), 200000 + Math.floor(Math.random() * 700000));
}
async function tick() { if (!running) return; await step(); if (running) timer = setTimeout(tick, Number($('speed').value)); }
$('buy-side').onclick = () => setSide('buy');
$('sell-side').onclick = () => setSide('sell');
$('symbol').onchange = () => { render(); $('explanation').textContent = `Viewing ${symbol()}. Submit an order to follow its matching result.`; };
$('order-form').onsubmit = event => { event.preventDefault(); submitOrder(side, Number($('price').value), Number($('quantity').value), Number($('account').value)); };
$('step').onclick = step;
$('play').onclick = () => { if (running) { pause(); return; } running = true; $('play').textContent = 'Ⅱ Pause'; $('sim-status').textContent = 'Running · new orders use the selected instrument.'; tick(); };
async function poll() { if (!busy) { try { await refresh(); } catch (error) { fail(error); } } setTimeout(poll, 2000); }
poll();
