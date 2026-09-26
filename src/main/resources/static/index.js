// AURA GRID — Client Application Logic
document.addEventListener('DOMContentLoaded', () => {
  initNavigation();
  initAmbientSimulator();
  initModalHandlers();
  initFormSubmissions();
  initUseCaseRunner();
  loadAllData();
});

// State Store
const state = {
  poles: [],
  chargingSessions: [],
  contacts: [],
  products: [],
  accounts: [],
  journals: [],
  journalEntries: [],
  purchaseOrders: [],
  vendorBills: [],
  salesOrders: [],
  customerInvoices: [],
  budgets: [],
  currentLux: 25.0,
  activeActor: 'admin'
};

// ==========================================================================
// NAVIGATION & TABS
// ==========================================================================
function initNavigation() {
  // Main tabs
  const tabButtons = document.querySelectorAll('.nav-tab');
  tabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      tabButtons.forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.dashboard-tab-panel').forEach(p => p.classList.remove('active'));
      
      btn.classList.add('active');
      const targetId = btn.getAttribute('data-tab');
      const panel = document.getElementById(targetId);
      if (panel) panel.classList.add('active');
    });
  });

  // Master Data sub-tabs
  const subtabButtons = document.querySelectorAll('.master-subtab-btn');
  subtabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      subtabButtons.forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.master-subpanel').forEach(p => p.classList.remove('active'));

      btn.classList.add('active');
      const targetId = btn.getAttribute('data-sub');
      const panel = document.getElementById(targetId);
      if (panel) panel.classList.add('active');
    });
  });

  // Report navigation pills
  const reportNavButtons = document.querySelectorAll('.report-nav-btn');
  reportNavButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      reportNavButtons.forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.report-document-card').forEach(c => c.classList.remove('active'));

      btn.classList.add('active');
      const targetId = btn.getAttribute('data-report');
      const doc = document.getElementById(targetId);
      if (doc) doc.classList.add('active');
    });
  });

  // Role selector
  const actorSelect = document.getElementById('active-actor-select');
  if (actorSelect) {
    actorSelect.addEventListener('change', (e) => {
      state.activeActor = e.target.value;
      showToast('Switched role context: ' + e.target.options[e.target.selectedIndex].text, 'info');
    });
  }

  // Sync button
  document.getElementById('btn-refresh-all')?.addEventListener('click', () => {
    loadAllData();
    showToast('Telemetry and ledgers synchronized', 'success');
  });
}

// ==========================================================================
// DATA FETCHING & SYNCHRONIZATION
// ==========================================================================
async function loadAllData() {
  try {
    await Promise.all([
      fetchPoles(),
      fetchSessions(),
      fetchTransactions(),
      fetchMasterData(),
      fetchBudgets(),
      fetchReports()
    ]);
  } catch (err) {
    console.error('Error loading data:', err);
  }
}

async function fetchPoles() {
  try {
    const res = await fetch('/api/smartpoles/units');
    if (res.ok) {
      state.poles = await res.json();
      renderPoles(state.poles);
      updateTopKPIs();
    }
  } catch (err) {
    console.error('Error fetching smart poles:', err);
  }
}

async function fetchSessions() {
  try {
    const res = await fetch('/api/smartpoles/charge/sessions');
    if (res.ok) {
      state.chargingSessions = await res.json();
      renderSessions(state.chargingSessions);
      updateTopKPIs();
    }
  } catch (err) {
    console.error('Error fetching charging sessions:', err);
  }
}

async function fetchTransactions() {
  try {
    const [poRes, billRes, soRes, invRes, jeRes] = await Promise.all([
      fetch('/api/purchase-orders'),
      fetch('/api/vendor-bills'),
      fetch('/api/sales-orders'),
      fetch('/api/customer-invoices'),
      fetch('/api/journal-entries')
    ]);

    if (poRes.ok) state.purchaseOrders = await poRes.json();
    if (billRes.ok) state.vendorBills = await billRes.json();
    if (soRes.ok) state.salesOrders = await soRes.json();
    if (invRes.ok) state.customerInvoices = await invRes.json();
    if (jeRes.ok) state.journalEntries = await jeRes.json();

    renderTransactions();
  } catch (err) {
    console.error('Error fetching transactions:', err);
  }
}

async function fetchMasterData() {
  try {
    const [cRes, pRes, aRes, jRes] = await Promise.all([
      fetch('/api/contacts'),
      fetch('/api/products'),
      fetch('/api/accounts'),
      fetch('/api/journals')
    ]);

    if (cRes.ok) state.contacts = await cRes.json();
    if (pRes.ok) state.products = await pRes.json();
    if (aRes.ok) state.accounts = await aRes.json();
    if (jRes.ok) state.journals = await jRes.json();

    renderMasterData();
    populateSelectDropdowns();
  } catch (err) {
    console.error('Error fetching master data:', err);
  }
}

async function fetchBudgets() {
  try {
    const res = await fetch('/api/budgets');
    if (res.ok) {
      state.budgets = await res.json();
      renderBudgets(state.budgets);
    }
  } catch (err) {
    console.error('Error fetching budgets:', err);
  }
}

async function fetchReports() {
  try {
    const [bsRes, pnlRes, budRes] = await Promise.all([
      fetch('/api/reports/balance-sheet'),
      fetch('/api/reports/profit-and-loss'),
      fetch('/api/reports/budget-performance')
    ]);

    if (bsRes.ok) renderBalanceSheet(await bsRes.json());
    if (pnlRes.ok) renderProfitAndLoss(await pnlRes.json());
    if (budRes.ok) renderBudgetReport(await budRes.json());
  } catch (err) {
    console.error('Error fetching reports:', err);
  }
}

// ==========================================================================
// RENDERERS
// ==========================================================================

function updateTopKPIs() {
  const activeSessions = state.chargingSessions.filter(s => s.status === 'ACTIVE');
  const activeCountEl = document.getElementById('kpi-active-sessions');
  if (activeCountEl) activeCountEl.innerText = `${activeSessions.length} Active`;

  let totalKwh = 0;
  let totalRev = 0;
  state.chargingSessions.forEach(s => {
    totalKwh += (s.kwhDelivered || 0);
    totalRev += (s.totalAmount || 0);
  });

  const kwhDispEl = document.getElementById('kpi-kwh-dispensed');
  if (kwhDispEl) kwhDispEl.innerHTML = `${totalKwh.toFixed(1)} <span class="kpi-unit">kWh</span>`;

  const evRevEl = document.getElementById('kpi-ev-revenue');
  if (evRevEl) evRevEl.innerText = `$${totalRev.toFixed(2)} billed to fleets`;

  let sumBrightness = 0;
  state.poles.forEach(p => sumBrightness += (p.ledBrightness || 0));
  const avgBright = state.poles.length > 0 ? Math.round(sumBrightness / state.poles.length) : 80;
  
  const brightEl = document.getElementById('kpi-avg-brightness');
  if (brightEl) brightEl.innerHTML = `${avgBright}<span class="kpi-unit">% Output</span>`;

  const telemetryCountEl = document.getElementById('telemetry-active-count');
  if (telemetryCountEl) telemetryCountEl.innerText = `${state.poles.length} Poles Online`;

  const poleCountTag = document.getElementById('pole-count-tag');
  if (poleCountTag) poleCountTag.innerText = `${state.poles.length} units deployed`;
}

function renderPoles(poles) {
  const grid = document.getElementById('smart-poles-grid');
  if (!grid) return;

  if (poles.length === 0) {
    grid.innerHTML = '<div class="card-loading">No smart poles registered yet. Click "Add Smart Pole Unit" to create one.</div>';
    return;
  }

  grid.innerHTML = poles.map(p => {
    const isCharging = (p.chargerStatus || '').toUpperCase() === 'CHARGING';
    const chargerStatusClass = isCharging ? 'status-charging' : 'status-available';
    const isAutoDim = p.autoDimmingEnabled !== false;

    return `
      <div class="pole-card" data-pole-code="${p.poleCode}">
        <div class="pole-card-header">
          <div class="pole-title-group">
            <h4>${escapeHtml(p.name)}</h4>
            <span class="pole-code-badge">${escapeHtml(p.poleCode)}</span>
            <div class="pole-location-sub">📍 ${escapeHtml(p.location)} • ${escapeHtml(p.zone || 'Downtown Sector')}</div>
          </div>
        </div>

        <!-- Luminaire LED Dimming -->
        <div class="pole-luminaire-section">
          <div class="luminaire-header">
            <span class="luminaire-title">💡 Streetlight Luminaire</span>
            <span class="luminaire-pct" id="bright-val-${p.poleCode}">${p.ledBrightness}%</span>
          </div>
          <div class="luminaire-slider-wrap">
            <input type="range" min="0" max="100" value="${p.ledBrightness}" 
                   class="slider-styled pole-led-slider" 
                   data-pole-code="${p.poleCode}"
                   oninput="handlePoleBrightnessChange('${p.poleCode}', this.value)"
                   aria-label="Dimmer Slider">
          </div>
          <div style="display:flex; justify-content:space-between; margin-top:8px; font-size:11px; color:var(--text-dim);">
            <span>Schedule: ${escapeHtml(p.dimmingSchedule ? p.dimmingSchedule.substring(0, 35) + '...' : 'Auto')}</span>
            <span>Lux: ${p.ambientLightLevel || 20}</span>
          </div>
        </div>

        <!-- EV Charger -->
        <div class="pole-charger-section">
          <div class="charger-meta-row">
            <span style="font-size:12px; font-weight:700; color:var(--cyan-primary);">🔌 EV Charging Port</span>
            <span class="charger-status-pill ${chargerStatusClass}">${p.chargerStatus}</span>
          </div>
          <div class="charger-specs-grid">
            <div class="spec-item">Rating: <strong>${p.chargerPowerKw} kW</strong></div>
            <div class="spec-item">Tariff: <strong>$${(p.currentKwhRate || 0.35).toFixed(2)} / kWh</strong></div>
            <div class="spec-item">Dispensed: <strong>${(p.totalKwhDispensed || 0).toFixed(1)} kWh</strong></div>
            <div class="spec-item">Active ID: <strong>${p.activeSessionId ? '#' + p.activeSessionId : 'None'}</strong></div>
          </div>

          ${isCharging ? `
            <button class="btn-charge-action btn-stop-plug" onclick="openStopChargeModal('${p.poleCode}', ${p.activeSessionId})">
              🛑 Disconnect EV & Bill Customer
            </button>
          ` : `
            <button class="btn-charge-action btn-start-plug" onclick="openStartChargeModal('${p.poleCode}')">
              ⚡ Plug-in Vehicle / Fleet
            </button>
          `}
        </div>

        <!-- Camera Feed Simulation -->
        <div class="pole-camera-section">
          <div class="camera-feed-meta">
            <span class="cam-dot"></span>
            <span>4K Surveillance • 30 FPS</span>
          </div>
          <span class="badge-status badge-live">${p.cameraStatus || 'ONLINE'}</span>
        </div>
      </div>
    `;
  }).join('');
}

function renderSessions(sessions) {
  const tbody = document.getElementById('charging-sessions-tbody');
  if (!tbody) return;

  if (sessions.length === 0) {
    tbody.innerHTML = '<tr><td colspan="10" style="text-align:center; color:var(--text-dim);">No charging sessions recorded yet.</td></tr>';
    return;
  }

  tbody.innerHTML = sessions.map(s => {
    const isActive = s.status === 'ACTIVE';
    const statusPillClass = isActive ? 'badge-cyan' : (s.status === 'INVOICED' ? 'badge-green' : 'badge-amber');
    const startTimeFormatted = s.startTime ? s.startTime.replace('T', ' ').substring(0, 16) : '-';

    return `
      <tr>
        <td style="font-family:var(--font-mono); font-weight:700;">#${s.id}</td>
        <td><span class="pole-code-badge">${escapeHtml(s.poleCode)}</span></td>
        <td><strong>${escapeHtml(s.driverOrFleetName || s.contactName || 'Fleet EV')}</strong></td>
        <td>${startTimeFormatted}</td>
        <td>${s.durationMinutes || 0} mins</td>
        <td style="font-family:var(--font-mono); font-weight:700; color:var(--cyan-primary);">${(s.kwhDelivered || 0).toFixed(1)} kWh</td>
        <td>$${(s.ratePerKwh || 0.35).toFixed(2)}</td>
        <td style="font-family:var(--font-mono); font-weight:700; color:var(--green-primary);">$${(s.totalAmount || 0).toFixed(2)}</td>
        <td><span class="kpi-badge ${statusPillClass}">${s.status}</span></td>
        <td>
          ${isActive ? `
            <button class="btn-xs-primary" style="background:#f43f5e; color:#fff;" onclick="openStopChargeModal('${s.poleCode}', ${s.id})">
              Stop & Bill
            </button>
          ` : `
            <span style="font-size:12px; color:var(--text-dim);">Inv #${s.customerInvoiceId || 'N/A'}</span>
          `}
        </td>
      </tr>
    `;
  }).join('');
}

function renderTransactions() {
  // 1. Purchase Orders
  const poTbody = document.getElementById('po-tbody');
  if (poTbody) {
    if (state.purchaseOrders.length === 0) {
      poTbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:var(--text-dim);">No purchase orders.</td></tr>';
    } else {
      poTbody.innerHTML = state.purchaseOrders.map(po => `
        <tr>
          <td style="font-family:var(--font-mono); font-weight:700;">${po.poNumber}</td>
          <td>${escapeHtml(po.vendorName)}</td>
          <td>${po.orderDate}</td>
          <td style="font-family:var(--font-mono); font-weight:700;">$${po.totalAmount.toFixed(2)}</td>
          <td><span class="kpi-badge ${po.status === 'BILLED' ? 'badge-green' : 'badge-amber'}">${po.status}</span></td>
          <td>
            ${po.status !== 'BILLED' ? `
              <button class="btn-xs-primary" onclick="convertPoToBill(${po.id})">Convert to Bill</button>
            ` : `<span style="font-size:11px; color:var(--text-dim);">Billed</span>`}
          </td>
        </tr>
      `).join('');
    }
  }

  // 2. Vendor Bills
  const billTbody = document.getElementById('vendor-bills-tbody');
  if (billTbody) {
    if (state.vendorBills.length === 0) {
      billTbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:var(--text-dim);">No vendor bills.</td></tr>';
    } else {
      billTbody.innerHTML = state.vendorBills.map(b => `
        <tr>
          <td style="font-family:var(--font-mono); font-weight:700;">${b.billNumber}</td>
          <td>${escapeHtml(b.vendorName)}</td>
          <td>${b.dueDate || b.billDate}</td>
          <td style="font-family:var(--font-mono); font-weight:700; color:var(--rose-primary);">$${b.totalAmount.toFixed(2)}</td>
          <td><span class="kpi-badge ${b.status === 'PAID' ? 'badge-green' : 'badge-amber'}">${b.status}</span></td>
          <td>
            ${b.status !== 'PAID' ? `
              <button class="btn-xs-primary" onclick="payVendorBill(${b.id})">Pay via Bank</button>
            ` : `<span style="font-size:11px; color:var(--green-primary);">Paid</span>`}
          </td>
        </tr>
      `).join('');
    }
  }

  // 3. Sales Orders
  const soTbody = document.getElementById('so-tbody');
  if (soTbody) {
    if (state.salesOrders.length === 0) {
      soTbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:var(--text-dim);">No sales orders.</td></tr>';
    } else {
      soTbody.innerHTML = state.salesOrders.map(so => `
        <tr>
          <td style="font-family:var(--font-mono); font-weight:700;">${so.soNumber}</td>
          <td>${escapeHtml(so.customerName)}</td>
          <td>${so.orderDate}</td>
          <td style="font-family:var(--font-mono); font-weight:700;">$${so.totalAmount.toFixed(2)}</td>
          <td><span class="kpi-badge ${so.status === 'INVOICED' ? 'badge-green' : 'badge-cyan'}">${so.status}</span></td>
          <td>
            ${so.status !== 'INVOICED' ? `
              <button class="btn-xs-primary" onclick="convertSoToInvoice(${so.id})">Invoice</button>
            ` : `<span style="font-size:11px; color:var(--text-dim);">Invoiced</span>`}
          </td>
        </tr>
      `).join('');
    }
  }

  // 4. Customer Invoices
  const invTbody = document.getElementById('customer-invoices-tbody');
  if (invTbody) {
    if (state.customerInvoices.length === 0) {
      invTbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:var(--text-dim);">No customer invoices.</td></tr>';
    } else {
      invTbody.innerHTML = state.customerInvoices.map(inv => `
        <tr>
          <td style="font-family:var(--font-mono); font-weight:700;">${inv.invoiceNumber}</td>
          <td>${escapeHtml(inv.customerName)}</td>
          <td>${inv.dueDate || inv.invoiceDate}</td>
          <td style="font-family:var(--font-mono); font-weight:700; color:var(--green-primary);">$${inv.totalAmount.toFixed(2)}</td>
          <td><span class="kpi-badge ${inv.status === 'PAID' ? 'badge-green' : 'badge-cyan'}">${inv.status}</span></td>
          <td>
            ${inv.status !== 'PAID' ? `
              <button class="btn-xs-primary" onclick="payCustomerInvoice(${inv.id})">Collect Receipt</button>
            ` : `<span style="font-size:11px; color:var(--green-primary);">Collected</span>`}
          </td>
        </tr>
      `).join('');
    }
  }

  // 5. Journal Entries
  const jeTbody = document.getElementById('journal-entries-tbody');
  if (jeTbody) {
    if (state.journalEntries.length === 0) {
      jeTbody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:var(--text-dim);">No journal entries recorded.</td></tr>';
    } else {
      jeTbody.innerHTML = state.journalEntries.slice(0, 15).map(je => `
        <tr>
          <td style="font-family:var(--font-mono); font-weight:700;">${je.entryNumber}</td>
          <td><span class="tag-pill">${je.journalCode}</span></td>
          <td>${je.entryDate}</td>
          <td><span style="font-family:var(--font-mono); font-size:11px;">${je.reference || '-'}</span></td>
          <td>${escapeHtml(je.description || '-')}</td>
          <td style="font-family:var(--font-mono); font-weight:700;">$${je.totalDebit.toFixed(2)}</td>
          <td style="font-family:var(--font-mono); font-weight:700;">$${je.totalCredit.toFixed(2)}</td>
          <td><span class="kpi-badge badge-green">POSTED</span></td>
        </tr>
      `).join('');
    }
  }
}

function renderMasterData() {
  // Contacts
  const cTbody = document.getElementById('contacts-tbody');
  if (cTbody) {
    cTbody.innerHTML = state.contacts.map(c => `
      <tr>
        <td style="font-family:var(--font-mono); font-weight:700;">#${c.id}</td>
        <td><strong>${escapeHtml(c.name)}</strong></td>
        <td><span class="kpi-badge ${c.contactType === 'CUSTOMER' ? 'badge-cyan' : 'badge-amber'}">${c.contactType}</span></td>
        <td>${escapeHtml(c.roleCategory)}</td>
        <td>${escapeHtml(c.email || '-')}</td>
        <td>${escapeHtml(c.phone || '-')}</td>
        <td>${escapeHtml(c.address || '-')}</td>
        <td style="font-family:var(--font-mono); font-weight:700;">$${(c.accountBalance || 0).toFixed(2)}</td>
      </tr>
    `).join('');
  }

  // Products
  const pTbody = document.getElementById('products-tbody');
  if (pTbody) {
    pTbody.innerHTML = state.products.map(p => `
      <tr>
        <td><span class="tag-pill" style="font-family:var(--font-mono);">${p.code}</span></td>
        <td><strong>${escapeHtml(p.name)}</strong></td>
        <td><span class="kpi-badge ${p.productType === 'SERVICE' ? 'badge-cyan' : 'badge-purple'}">${p.productType}</span></td>
        <td style="font-family:var(--font-mono); font-weight:700;">$${p.unitPrice.toFixed(2)}</td>
        <td>Per ${escapeHtml(p.unitOfMeasure)}</td>
        <td style="color:var(--text-muted);">${escapeHtml(p.description || '-')}</td>
      </tr>
    `).join('');
  }

  // Chart of Accounts
  renderCoA();

  // Journals
  const jTbody = document.getElementById('journals-tbody');
  if (jTbody) {
    jTbody.innerHTML = state.journals.map(j => `
      <tr>
        <td><span class="tag-pill" style="font-family:var(--font-mono);">${j.code}</span></td>
        <td><strong>${escapeHtml(j.name)}</strong></td>
        <td>${j.journalType}</td>
        <td style="color:var(--text-muted);">Double-entry Debit/Credit Posting</td>
      </tr>
    `).join('');
  }
}

function renderCoA() {
  const assets = state.accounts.filter(a => a.accountType === 'ASSET');
  const liabilities = state.accounts.filter(a => a.accountType === 'LIABILITY');
  const incomes = state.accounts.filter(a => a.accountType === 'INCOME');
  const expenses = state.accounts.filter(a => a.accountType === 'EXPENSE');

  renderCoAList('coa-assets-list', assets);
  renderCoAList('coa-liabilities-list', liabilities);
  renderCoAList('coa-income-list', incomes);
  renderCoAList('coa-expenses-list', expenses);
}

function renderCoAList(containerId, list) {
  const container = document.getElementById(containerId);
  if (!container) return;
  container.innerHTML = list.map(a => `
    <div class="coa-item-row">
      <div>
        <span class="coa-item-code">${a.code}</span>
        <div class="coa-item-name">${escapeHtml(a.name)}</div>
      </div>
      <div class="coa-item-bal">$${(a.balance || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}</div>
    </div>
  `).join('');
}

function renderBudgets(budgets) {
  if (budgets.length === 0) return;
  const b = budgets[0];

  document.getElementById('budget-planned-rev').innerText = `$${b.totalPlannedRevenue.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  document.getElementById('budget-actual-rev').innerText = `$${b.totalActualRevenue.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  document.getElementById('budget-planned-exp').innerText = `$${b.totalPlannedExpense.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  document.getElementById('budget-actual-exp').innerText = `$${b.totalActualExpense.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  
  const marginEl = document.getElementById('budget-net-margin');
  marginEl.innerText = `${b.netActualMargin >= 0 ? '+' : ''}$${b.netActualMargin.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;

  const linesTbody = document.getElementById('budget-lines-tbody');
  if (linesTbody && b.lines) {
    linesTbody.innerHTML = b.lines.map(line => {
      const isIncome = line.accountType === 'INCOME';
      const pct = Math.min(100, Math.max(0, line.achievementRate || 0));
      const fillClass = isIncome ? (pct >= 80 ? 'fill-green' : 'fill-amber') : (pct <= 80 ? 'fill-green' : 'fill-rose');

      return `
        <tr>
          <td style="font-family:var(--font-mono);">${line.accountCode || '-'}</td>
          <td><strong>${escapeHtml(line.accountName)}</strong></td>
          <td><span class="tag-pill">${line.accountType}</span></td>
          <td style="font-family:var(--font-mono); font-weight:700;">$${line.plannedAmount.toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
          <td style="font-family:var(--font-mono); font-weight:700; color:${isIncome ? 'var(--green-primary)' : 'var(--rose-primary)'}">
            $${line.actualAmount.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </td>
          <td style="font-family:var(--font-mono);">
            ${line.variance >= 0 ? '+' : ''}$${line.variance.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </td>
          <td>
            <div style="display:flex; justify-content:space-between; font-size:11px;">
              <span>${line.achievementRate ? line.achievementRate.toFixed(1) : 0}%</span>
            </div>
            <div class="progress-bar-wrap">
              <div class="progress-bar-fill ${fillClass}" style="width: ${pct}%"></div>
            </div>
          </td>
        </tr>
      `;
    }).join('');
  }
}

function renderBalanceSheet(report) {
  if (!report) return;

  const assetsTbody = document.getElementById('bs-assets-tbody');
  if (assetsTbody) {
    assetsTbody.innerHTML = report.assets.map(a => `
      <tr>
        <td><strong>${escapeHtml(a.name)}</strong> <span style="font-size:11px; color:var(--text-dim);">(${a.code})</span></td>
        <td class="text-right" style="font-family:var(--font-mono);">$${a.balance.toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
      </tr>
    `).join('');
  }

  const liabTbody = document.getElementById('bs-liabilities-tbody');
  if (liabTbody) {
    liabTbody.innerHTML = report.liabilities.map(l => `
      <tr>
        <td><strong>${escapeHtml(l.name)}</strong> <span style="font-size:11px; color:var(--text-dim);">(${l.code})</span></td>
        <td class="text-right" style="font-family:var(--font-mono);">$${l.balance.toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
      </tr>
    `).join('');
  }

  document.getElementById('bs-total-assets').innerText = `$${report.totalAssets.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  document.getElementById('bs-retained-surplus').innerText = `$${report.retainedSurplus.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  document.getElementById('bs-total-liab-equity').innerText = `$${report.totalLiabilitiesAndEquity.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
}

function renderProfitAndLoss(report) {
  if (!report) return;

  const revTbody = document.getElementById('pnl-revenue-tbody');
  if (revTbody) {
    revTbody.innerHTML = report.revenues.map(r => `
      <tr>
        <td><strong>${escapeHtml(r.name)}</strong></td>
        <td class="text-right" style="font-family:var(--font-mono); color:var(--green-primary);">$${r.balance.toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
      </tr>
    `).join('');
  }

  const expTbody = document.getElementById('pnl-expense-tbody');
  if (expTbody) {
    expTbody.innerHTML = report.expenses.map(e => `
      <tr>
        <td><strong>${escapeHtml(e.name)}</strong></td>
        <td class="text-right" style="font-family:var(--font-mono); color:var(--rose-primary);">$${e.balance.toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
      </tr>
    `).join('');
  }

  document.getElementById('pnl-total-revenue').innerText = `$${report.totalRevenue.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  document.getElementById('pnl-total-expense').innerText = `$${report.totalExpense.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  
  const surplusEl = document.getElementById('pnl-net-surplus');
  surplusEl.innerText = `${report.netOperatingSurplus >= 0 ? '+' : ''}$${report.netOperatingSurplus.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;

  const marginBadge = document.getElementById('pnl-margin-badge');
  if (marginBadge) {
    marginBadge.innerText = `Operating Margin: ${report.marginPercent ? report.marginPercent.toFixed(1) : 0}%`;
  }

  const kpiSurplus = document.getElementById('kpi-net-surplus');
  if (kpiSurplus) {
    kpiSurplus.innerText = `${report.netOperatingSurplus >= 0 ? '+' : ''}$${report.netOperatingSurplus.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
  }
}

function renderBudgetReport(report) {
  if (!report) return;

  const tbody = document.getElementById('budget-report-tbody');
  if (tbody && report.lines) {
    tbody.innerHTML = report.lines.map(line => {
      const isIncome = line.accountType === 'INCOME';
      return `
        <tr>
          <td style="font-family:var(--font-mono);">${line.accountCode || '-'}</td>
          <td><strong>${escapeHtml(line.accountName)}</strong></td>
          <td><span class="tag-pill">${line.accountType}</span></td>
          <td class="text-right" style="font-family:var(--font-mono);">$${line.plannedAmount.toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
          <td class="text-right" style="font-family:var(--font-mono); color:${isIncome ? 'var(--green-primary)' : 'var(--rose-primary)'}">
            $${line.actualAmount.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </td>
          <td class="text-right" style="font-family:var(--font-mono);">${line.variance >= 0 ? '+' : ''}$${line.variance.toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
          <td class="text-right" style="font-weight:700;">${line.achievementRate ? line.achievementRate.toFixed(1) : 0}%</td>
        </tr>
      `;
    }).join('');
  }

  const ratioBadge = document.getElementById('budget-ratio-badge');
  if (ratioBadge) {
    ratioBadge.innerText = `Budget Achievement: ${report.performanceRatio ? report.performanceRatio.toFixed(1) : 0}%`;
  }
}

// ==========================================================================
// INTERACTIVE AMBIENT SIMULATOR & BRIGHTNESS
// ==========================================================================
function initAmbientSimulator() {
  const slider = document.getElementById('ambient-lux-slider');
  const display = document.getElementById('current-lux-display');
  const telemetryLux = document.getElementById('telemetry-lux');

  if (slider) {
    slider.addEventListener('input', async (e) => {
      const lux = parseFloat(e.target.value);
      if (display) display.innerText = `${lux} Lux`;
      if (telemetryLux) telemetryLux.innerText = `${lux} Lux`;
      
      // Send simulation to backend
      await simulateAmbientLux(lux);
    });
  }

  // Preset buttons
  document.querySelectorAll('.btn-preset').forEach(btn => {
    btn.addEventListener('click', async () => {
      document.querySelectorAll('.btn-preset').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const lux = parseFloat(btn.getAttribute('data-lux'));
      if (slider) slider.value = lux;
      if (display) display.innerText = `${lux} Lux`;
      if (telemetryLux) telemetryLux.innerText = `${lux} Lux`;

      await simulateAmbientLux(lux);
    });
  });
}

async function simulateAmbientLux(lux) {
  try {
    const res = await fetch('/api/smartpoles/simulate/ambient', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ambientLightLux: lux })
    });
    if (res.ok) {
      state.poles = await res.json();
      renderPoles(state.poles);
      updateTopKPIs();
    }
  } catch (err) {
    console.error('Error simulating ambient lux:', err);
  }
}

window.handlePoleBrightnessChange = async function(poleCode, brightness) {
  const display = document.getElementById(`bright-val-${poleCode}`);
  if (display) display.innerText = `${brightness}%`;

  try {
    await fetch(`/api/smartpoles/units/${poleCode}/dimming`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ brightness: parseInt(brightness), autoDimmingEnabled: false })
    });
  } catch (err) {
    console.error('Error changing brightness:', err);
  }
};

// ==========================================================================
// INVOICING & TRANSACTION ACTIONS
// ==========================================================================
window.convertPoToBill = async function(poId) {
  try {
    const res = await fetch(`/api/purchase-orders/${poId}/convert-to-bill`, { method: 'POST' });
    if (res.ok) {
      showToast('Purchase Order converted to Vendor Bill (Accounts Payable posted)', 'success');
      loadAllData();
    } else {
      const err = await res.text();
      showToast('Error: ' + err, 'error');
    }
  } catch (err) {
    console.error('Error converting PO:', err);
  }
};

window.payVendorBill = async function(billId) {
  try {
    const res = await fetch(`/api/vendor-bills/${billId}/pay`, { method: 'POST' });
    if (res.ok) {
      showToast('Vendor Bill paid via Bank (Cash/Bank credited)', 'success');
      loadAllData();
    } else {
      const err = await res.text();
      showToast('Error paying bill: ' + err, 'error');
    }
  } catch (err) {
    console.error('Error paying bill:', err);
  }
};

window.convertSoToInvoice = async function(soId) {
  try {
    const res = await fetch(`/api/sales-orders/${soId}/convert-to-invoice`, { method: 'POST' });
    if (res.ok) {
      showToast('Sales Order converted to Customer Invoice (AR posted)', 'success');
      loadAllData();
    } else {
      const err = await res.text();
      showToast('Error: ' + err, 'error');
    }
  } catch (err) {
    console.error('Error converting SO:', err);
  }
};

window.payCustomerInvoice = async function(invoiceId) {
  try {
    const res = await fetch(`/api/customer-invoices/${invoiceId}/pay`, { method: 'POST' });
    if (res.ok) {
      showToast('Customer payment received via Bank (AR settled)', 'success');
      loadAllData();
    } else {
      const err = await res.text();
      showToast('Error collecting payment: ' + err, 'error');
    }
  } catch (err) {
    console.error('Error collecting payment:', err);
  }
};

// ==========================================================================
// MODAL CONTROLLERS & FORMS
// ==========================================================================
function initModalHandlers() {
  document.querySelectorAll('[data-modal]').forEach(trigger => {
    trigger.addEventListener('click', () => {
      const modalId = trigger.getAttribute('data-modal');
      const modal = document.getElementById(modalId);
      if (modal) modal.classList.toggle('active');
    });
  });

  document.getElementById('btn-open-add-pole-modal')?.addEventListener('click', () => {
    document.getElementById('modal-add-pole')?.classList.add('active');
  });

  document.getElementById('btn-quick-start-charge')?.addEventListener('click', () => {
    openStartChargeModal();
  });

  document.getElementById('btn-run-usecase')?.addEventListener('click', () => {
    document.getElementById('modal-usecase-runner')?.classList.add('active');
  });
}

function populateSelectDropdowns() {
  // Charge pole dropdown
  const poleSelect = document.getElementById('charge-form-pole');
  if (poleSelect) {
    const availPoles = state.poles.filter(p => p.chargerStatus !== 'CHARGING');
    poleSelect.innerHTML = (availPoles.length > 0 ? availPoles : state.poles).map(p => `
      <option value="${p.poleCode}">${p.poleCode} — ${p.name} ($${p.currentKwhRate}/kWh)</option>
    `).join('');
  }

  // Customer dropdown
  const custSelect = document.getElementById('charge-form-customer');
  if (custSelect) {
    custSelect.innerHTML = state.contacts.filter(c => c.contactType === 'CUSTOMER').map(c => `
      <option value="${c.id}">${c.name} (${c.roleCategory})</option>
    `).join('');
  }
}

window.openStartChargeModal = function(preferredPoleCode) {
  populateSelectDropdowns();
  if (preferredPoleCode) {
    const select = document.getElementById('charge-form-pole');
    if (select) select.value = preferredPoleCode;
  }
  document.getElementById('modal-start-charge')?.classList.add('active');
};

window.openStopChargeModal = function(poleCode, sessionId) {
  document.getElementById('stop-form-session-id').value = sessionId || '';
  
  const pole = state.poles.find(p => p.poleCode === poleCode);
  const session = state.chargingSessions.find(s => s.id === sessionId);

  document.getElementById('stop-modal-pole-info').innerText = `Pole: ${poleCode} (${pole ? pole.name : ''})`;
  document.getElementById('stop-modal-fleet-info').innerText = `Vehicle: ${session ? session.driverOrFleetName : 'Commercial EV'}`;
  document.getElementById('stop-modal-rate-info').innerText = `Tariff: $${pole ? pole.currentKwhRate.toFixed(2) : '0.35'} / kWh`;

  document.getElementById('modal-stop-charge')?.classList.add('active');
};

function initFormSubmissions() {
  // Add Pole Form
  document.getElementById('form-add-pole')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      poleCode: document.getElementById('pole-form-code').value.trim(),
      name: document.getElementById('pole-form-name').value.trim(),
      location: document.getElementById('pole-form-location').value.trim(),
      zone: document.getElementById('pole-form-zone').value,
      chargerPowerKw: parseFloat(document.getElementById('pole-form-power').value),
      currentKwhRate: parseFloat(document.getElementById('pole-form-tariff').value),
      ledBrightness: parseInt(document.getElementById('pole-form-brightness').value),
      ambientLightLevel: 25.0,
      autoDimmingEnabled: true,
      chargerStatus: 'AVAILABLE',
      cameraStatus: 'ONLINE'
    };

    try {
      const res = await fetch('/api/smartpoles/units', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        showToast(`Smart Pole ${payload.poleCode} registered successfully!`, 'success');
        document.getElementById('modal-add-pole')?.classList.remove('active');
        e.target.reset();
        loadAllData();
      } else {
        const err = await res.text();
        showToast('Error: ' + err, 'error');
      }
    } catch (err) {
      console.error('Error creating pole:', err);
    }
  });

  // Start Charge Form
  document.getElementById('form-start-charge')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const poleCode = document.getElementById('charge-form-pole').value;
    const contactId = document.getElementById('charge-form-customer').value;
    const fleetName = document.getElementById('charge-form-fleet').value.trim();

    try {
      const res = await fetch('/api/smartpoles/charge/start', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          poleCode: poleCode,
          contactId: contactId ? parseInt(contactId) : null,
          driverOrFleetName: fleetName || 'Commercial Fleet EV'
        })
      });

      if (res.ok) {
        showToast(`Charging session initiated on ${poleCode}!`, 'success');
        document.getElementById('modal-start-charge')?.classList.remove('active');
        e.target.reset();
        loadAllData();
      } else {
        const err = await res.text();
        showToast('Error: ' + err, 'error');
      }
    } catch (err) {
      console.error('Error starting charge:', err);
    }
  });

  // Stop Charge Form
  document.getElementById('form-stop-charge')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const sessionId = document.getElementById('stop-form-session-id').value;
    const kwh = parseFloat(document.getElementById('stop-form-kwh').value);
    const autoinvoice = document.getElementById('stop-form-autoinvoice').checked;

    try {
      const res = await fetch('/api/smartpoles/charge/stop', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          sessionId: parseInt(sessionId),
          kwhDelivered: kwh,
          createInvoice: autoinvoice
        })
      });

      if (res.ok) {
        showToast(`Charging completed! ${kwh} kWh billed and General Ledger updated`, 'success');
        document.getElementById('modal-stop-charge')?.classList.remove('active');
        loadAllData();
      } else {
        const err = await res.text();
        showToast('Error: ' + err, 'error');
      }
    } catch (err) {
      console.error('Error stopping charge:', err);
    }
  });
}

// ==========================================================================
// 4-STEP KEY USE-CASE RUNNER AUTOMATION
// ==========================================================================
function initUseCaseRunner() {
  const execBtn = document.getElementById('btn-execute-usecase-flow');
  const logBox = document.getElementById('usecase-log-output');

  execBtn?.addEventListener('click', async () => {
    execBtn.disabled = true;
    execBtn.innerHTML = '<span>⏳</span> Executing Workflow...';

    // Reset step styles
    for (let i = 1; i <= 4; i++) {
      const card = document.getElementById(`step-card-${i}`);
      const status = document.getElementById(`step-${i}-status`);
      if (card) { card.className = 'step-card'; }
      if (status) { status.innerText = 'Pending...'; }
    }

    logBox.innerText = '⚡ [INITIATED] Running 4-Step Municipal Key Use-Case Flow...\n';

    try {
      // Step 1 UI Update
      setStepActive(1, 'Executing POST /api/smartpoles/units & registering fleet client...');
      await sleep(600);

      // Call Demo Workflow endpoint
      const res = await fetch('/api/demo/run-use-case', { method: 'POST' });
      const data = await res.json();

      // Step 1 Success
      setStepDone(1, `✓ Created Pole ${data.step1_masterData.createdPoleCode} in ${data.step1_masterData.location}`);
      logBox.innerText += `[STEP 1 SUCCESS] Pole ${data.step1_masterData.createdPoleCode} created with tariff $${data.step1_masterData.tariff}/kWh.\n`;

      // Step 2 UI Update
      await sleep(800);
      setStepActive(2, 'Processing charging session via POST /api/smartpoles/charge/start & stop...');
      logBox.innerText += `[STEP 2 ACTIVE] Connected vehicle and started metering...\n`;
      
      await sleep(800);
      setStepDone(2, `✓ Delivered ${data.step2_chargingSession.kwhDelivered} kWh • Charges: $${data.step2_chargingSession.totalCharged}`);
      logBox.innerText += `[STEP 2 SUCCESS] Delivered ${data.step2_chargingSession.kwhDelivered} kWh. Total billed: $${data.step2_chargingSession.totalCharged}.\n`;

      // Step 3 UI Update
      await sleep(800);
      setStepActive(3, 'Issuing Customer Invoice and processing Vendor Bill for LED replacements...');
      
      await sleep(800);
      setStepDone(3, `✓ Vendor Bill ${data.step3_invoicing.vendorBillNumber} paid ($${data.step3_invoicing.vendorPaymentAmount}) via Bank`);
      logBox.innerText += `[STEP 3 SUCCESS] Processed Vendor Bill ${data.step3_invoicing.vendorBillNumber} ($${data.step3_invoicing.vendorBillAmount}) & issued Bank Payment ${data.step3_invoicing.vendorPaymentNumber}.\n`;

      // Step 4 UI Update
      await sleep(800);
      setStepActive(4, 'Computing Smart Lighting P&L and Grid Budget Performance Reports...');

      await sleep(800);
      setStepDone(4, `✓ P&L Net Surplus: $${data.step4_reports.pnlNetOperatingSurplus} • Budget Achievement: ${data.step4_reports.budgetPerformanceRatio}`);
      logBox.innerText += `[STEP 4 SUCCESS] Reports compiled:\n` +
        ` - Total Revenues: $${data.step4_reports.pnlTotalRevenue}\n` +
        ` - Total Expenses: $${data.step4_reports.pnlTotalExpense}\n` +
        ` - Net Operating Surplus: $${data.step4_reports.pnlNetOperatingSurplus}\n` +
        ` - Budget Ratio: ${data.step4_reports.budgetPerformanceRatio}\n` +
        ` - Balance Sheet Balance Status: ${data.step4_reports.balanceSheetBalanced ? 'BALANCED (Assets = Liabilities + Equity)' : 'UNBALANCED'}\n\n` +
        `✨ ALL 4 KEY USE-CASE STEPS COMPLETED SUCCESSFULLY! ✨`;

      showToast('4-Step Municipal Workflow executed successfully!', 'success');
      loadAllData();

    } catch (err) {
      logBox.innerText += `\n❌ ERROR: ${err.message}`;
      showToast('Workflow execution failed', 'error');
    } finally {
      execBtn.disabled = false;
      execBtn.innerHTML = '<span>🚀</span> Execute All 4 Steps Now';
    }
  });
}

function setStepActive(num, text) {
  const card = document.getElementById(`step-card-${num}`);
  const status = document.getElementById(`step-${num}-status`);
  if (card) { card.className = 'step-card active'; }
  if (status) { status.innerText = text; }
}

function setStepDone(num, text) {
  const card = document.getElementById(`step-card-${num}`);
  const status = document.getElementById(`step-${num}-status`);
  if (card) { card.className = 'step-card done'; }
  if (status) { status.innerText = text; }
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

// ==========================================================================
// TOAST NOTIFICATIONS
// ==========================================================================
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  
  let icon = 'ℹ️';
  if (type === 'success') icon = '✅';
  if (type === 'warn') icon = '⚠️';
  if (type === 'error') icon = '❌';

  toast.innerHTML = `<span>${icon}</span><span>${escapeHtml(message)}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(20px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

function escapeHtml(text) {
  if (!text) return '';
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
