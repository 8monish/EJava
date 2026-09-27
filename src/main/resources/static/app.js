// ==========================================================================
// AURA GRID // MUNICIPAL OS
// SCALABLE APPLICATION ENGINE
// ==========================================================================

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

document.addEventListener('DOMContentLoaded', () => {
  initCommonComponents();
});

function initCommonComponents() {
  initRoleSelector();
  initSyncButton();
  initModalCloseHandlers();
  loadGlobalTelemetry();
}

function initRoleSelector() {
  const select = document.getElementById('global-role-select');
  if (select) {
    const saved = localStorage.getItem('aura_grid_role');
    if (saved) {
      select.value = saved;
      state.activeActor = saved;
    }
    select.addEventListener('change', (e) => {
      state.activeActor = e.target.value;
      localStorage.setItem('aura_grid_role', state.activeActor);
      showToast('ROLE PROFILE CONTEXT: ' + e.target.options[e.target.selectedIndex].text.toUpperCase());
    });
  }
}

function initSyncButton() {
  const btn = document.getElementById('global-sync-btn');
  if (btn) {
    btn.addEventListener('click', async () => {
      btn.disabled = true;
      btn.classList.add('loading');
      await loadGlobalTelemetry();
      if (window.onPageDataRefresh) {
        await window.onPageDataRefresh();
      }
      btn.disabled = false;
      btn.classList.remove('loading');
      showToast('TELEMETRY AND LEDGERS SYNCHRONIZED');
    });
  }
}

function initModalCloseHandlers() {
  document.querySelectorAll('[data-close-modal]').forEach(btn => {
    btn.addEventListener('click', () => {
      const modal = btn.closest('.modal-overlay');
      if (modal) modal.classList.remove('active');
    });
  });

  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        overlay.classList.remove('active');
      }
    });
  });
}

function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add('active');
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('active');
}

async function loadGlobalTelemetry() {
  try {
    const [pRes, sRes] = await Promise.all([
      fetch('/api/smartpoles/units'),
      fetch('/api/smartpoles/charge/sessions/active')
    ]);

    if (pRes.ok) {
      state.poles = await pRes.json();
      const countEl = document.getElementById('telemetry-poles-count');
      if (countEl) countEl.innerText = `${state.poles.length} POLES ONLINE`;
    }

    if (sRes.ok) {
      const activeSessions = await sRes.json();
      const sessEl = document.getElementById('telemetry-active-sessions');
      if (sessEl) sessEl.innerText = `${activeSessions.length} EV CHARGING`;
    }
  } catch (err) {
    console.error('Failed to load global telemetry:', err);
  }
}

function showToast(message) {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.innerHTML = `
    <svg class="icon icon-sm" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
    <span>${escapeHtml(message)}</span>
  `;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translate(10px, 10px)';
    toast.style.transition = 'all 0.2s ease';
    setTimeout(() => toast.remove(), 200);
  }, 3500);
}

function escapeHtml(text) {
  if (text === null || text === undefined) return '';
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function formatCurrency(val) {
  const num = Number(val) || 0;
  return '$' + num.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

window.openModal = openModal;
window.closeModal = closeModal;
window.showToast = showToast;
window.formatCurrency = formatCurrency;
window.escapeHtml = escapeHtml;
window.state = state;

// ==========================================================================
// 1. DASHBOARD PAGE (index.html)
// ==========================================================================
window.initDashboardPage = async function() {
  window.onPageDataRefresh = loadDashboardData;
  await loadDashboardData();
};

async function loadDashboardData() {
  try {
    const [pRes, sRes, repRes] = await Promise.all([
      fetch('/api/smartpoles/units'),
      fetch('/api/smartpoles/charge/sessions'),
      fetch('/api/reports/profit-and-loss')
    ]);

    if (pRes.ok) state.poles = await pRes.json();
    if (sRes.ok) state.chargingSessions = await sRes.json();
    let pnl = null;
    if (repRes.ok) pnl = await repRes.json();

    const totalPolesEl = document.getElementById('dash-poles-total');
    if (totalPolesEl) totalPolesEl.innerText = state.poles.length;

    const activeChargeSessions = state.chargingSessions.filter(s => s.status === 'ACTIVE');
    const activeSessionsEl = document.getElementById('dash-active-sessions');
    if (activeSessionsEl) activeSessionsEl.innerText = activeChargeSessions.length;

    let totalKwh = 0;
    let totalRev = 0;
    state.chargingSessions.forEach(s => {
      totalKwh += (s.kwhDelivered || 0);
      totalRev += (s.totalAmount || 0);
    });

    const kwhEl = document.getElementById('dash-kwh-dispensed');
    if (kwhEl) kwhEl.innerHTML = `${totalKwh.toFixed(1)} <span class="stat-unit">kWh</span>`;

    const revEl = document.getElementById('dash-ev-revenue');
    if (revEl) revEl.innerText = formatCurrency(totalRev) + ' BILLED TO FLEETS';

    const surplusEl = document.getElementById('dash-ledger-surplus');
    if (surplusEl && pnl) {
      surplusEl.innerText = `${pnl.netOperatingSurplus >= 0 ? '+' : ''}${formatCurrency(pnl.netOperatingSurplus)}`;
    }

    const dashPolesTbody = document.getElementById('dash-poles-tbody');
    if (dashPolesTbody) {
      const topPoles = state.poles.slice(0, 5);
      if (topPoles.length === 0) {
        dashPolesTbody.innerHTML = '<tr><td colspan="5" class="text-center font-mono">NO POLES REGISTERED</td></tr>';
      } else {
        dashPolesTbody.innerHTML = topPoles.map(p => `
          <tr>
            <td class="font-mono"><strong>${escapeHtml(p.poleCode)}</strong></td>
            <td>${escapeHtml(p.location || 'Sector 1')}</td>
            <td>
              <span class="tag-badge ${p.currentBrightness > 0 ? 'blue' : 'dark'}">
                ${p.currentBrightness}%
              </span>
            </td>
            <td class="font-mono">${p.ambientLux || 25} lx</td>
            <td>
              <span class="tag-badge ${p.evChargerStatus === 'AVAILABLE' ? 'lime' : (p.evChargerStatus === 'CHARGING' ? 'orange' : 'dark')}">
                ${p.evChargerStatus}
              </span>
            </td>
          </tr>
        `).join('');
      }
    }

    const recentSessTbody = document.getElementById('dash-recent-sessions-tbody');
    if (recentSessTbody) {
      const recent = state.chargingSessions.slice(0, 5);
      if (recent.length === 0) {
        recentSessTbody.innerHTML = '<tr><td colspan="5" class="text-center font-mono">NO RECENT EV CHARGING ACTIVITY</td></tr>';
      } else {
        recentSessTbody.innerHTML = recent.map(s => `
          <tr>
            <td class="font-mono"><strong>${escapeHtml(s.poleCode)}</strong></td>
            <td>${escapeHtml(s.driverOrFleetName || 'Commercial EV')}</td>
            <td class="font-mono">${(s.kwhDelivered || 0).toFixed(1)} kWh</td>
            <td class="font-mono"><strong>${formatCurrency(s.totalAmount || 0)}</strong></td>
            <td><span class="tag-badge ${s.status === 'ACTIVE' ? 'lime' : 'dark'}">${s.status}</span></td>
          </tr>
        `).join('');
      }
    }
  } catch (err) {
    console.error('Error loading dashboard data:', err);
  }
}

// ==========================================================================
// 2. SMART POLES PAGE (poles.html)
// ==========================================================================
window.initPolesPage = async function() {
  window.onPageDataRefresh = loadPolesData;
  initAmbientSimulator();
  initAddPoleForm();
  await loadPolesData();
};

async function loadPolesData() {
  try {
    const res = await fetch('/api/smartpoles/units');
    if (res.ok) {
      state.poles = await res.json();
      renderPolesList(state.poles);
    }
  } catch (err) {
    console.error('Error fetching poles:', err);
  }
}

function renderPolesList(poles) {
  const container = document.getElementById('poles-cards-container');
  if (!container) return;

  if (poles.length === 0) {
    container.innerHTML = '<div class="brutalist-card"><div class="card-body text-center font-mono">NO SMART POLE UNITS DEPLOYED. CLICK [+ REGISTER SMART POLE] TO ADD ONE.</div></div>';
    return;
  }

  container.innerHTML = poles.map(p => {
    const isCharging = (p.chargerStatus || '').toUpperCase() === 'CHARGING';
    return `
      <div class="pole-card">
        <div class="pole-card-header">
          <h4>${escapeHtml(p.name)}</h4>
          <span class="pole-code">${escapeHtml(p.poleCode)}</span>
        </div>
        <div class="pole-card-body">
          <div class="pole-meta-row">
            <span class="pole-location">
              <svg class="icon icon-sm" viewBox="0 0 24 24"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
              ${escapeHtml(p.location)}
            </span>
            <span class="pole-zone font-mono">${escapeHtml(p.zone || 'DOWNTOWN SECTOR 1')}</span>
          </div>

          <!-- Luminaire Section -->
          <div class="pole-feature-section">
            <div class="feature-header">
              <span style="display:flex; align-items:center; gap:5px;">
                <svg class="icon icon-sm" viewBox="0 0 24 24"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/></svg>
                LED LUMINAIRE
              </span>
              <span class="font-mono" id="bright-display-${p.poleCode}">${p.ledBrightness}% OUTPUT</span>
            </div>
            <div class="pole-slider-wrap">
              <input type="range" min="0" max="100" value="${p.ledBrightness}"
                     oninput="handlePoleBrightness('${p.poleCode}', this.value)"
                     aria-label="Pole brightness slider">
            </div>
            <div style="display:flex; justify-content:space-between; font-size:10px; font-family:var(--font-mono);">
              <span>AMB LUX: ${p.ambientLightLevel || 25}</span>
              <span>DIMMING: ${p.autoDimmingEnabled ? 'AUTO' : 'MANUAL'}</span>
            </div>
          </div>

          <!-- EV Charger Section -->
          <div class="pole-feature-section">
            <div class="feature-header">
              <span style="display:flex; align-items:center; gap:5px;">
                <svg class="icon icon-sm" viewBox="0 0 24 24"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
                EV CHARGER PORT
              </span>
              <span class="tag-badge ${isCharging ? 'lime' : 'blue'}">${p.chargerStatus}</span>
            </div>
            <div class="spec-list">
              <div>POWER: <strong>${p.chargerPowerKw} kW</strong></div>
              <div>RATE: <strong>${formatCurrency(p.currentKwhRate)}/kWh</strong></div>
              <div>DISPENSED: <strong>${(p.totalKwhDispensed || 0).toFixed(1)} kWh</strong></div>
              <div>SESSION: <strong>${p.activeSessionId ? '#' + p.activeSessionId : 'NONE'}</strong></div>
            </div>
          </div>

          <!-- Surveillance Camera Section -->
          <div class="pole-feature-section">
            <div class="feature-header">
              <span style="display:flex; align-items:center; gap:5px;">
                <svg class="icon icon-sm" viewBox="0 0 24 24"><path d="M23 7l-7 5 7 5V7z"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/></svg>
                4K SURVEILLANCE
              </span>
              <span class="tag-badge green">${p.cameraStatus || 'ONLINE'}</span>
            </div>
            <div style="font-size:11px; font-family:var(--font-mono);">
              STREAM: 3840x2160 @ 30 FPS • LPR ACTIVE
            </div>
          </div>
        </div>

        <div class="pole-card-actions">
          ${isCharging ? `
            <button class="btn btn-secondary btn-sm" style="width:100%;" onclick="openStopSessionModalForPole('${p.poleCode}', ${p.activeSessionId})">
              <svg class="icon icon-sm" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><rect x="9" y="9" width="6" height="6"/></svg>
              DISCONNECT EV & BILL
            </button>
          ` : `
            <a href="charging.html" class="btn btn-primary btn-sm" style="width:100%; text-decoration:none;">
              <svg class="icon icon-sm" viewBox="0 0 24 24"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
              CONNECT EV VEHICLE
            </a>
          `}
        </div>
      </div>
    `;
  }).join('');
}

window.handlePoleBrightness = async function(poleCode, val) {
  const display = document.getElementById(`bright-display-${poleCode}`);
  if (display) display.innerText = `${val}% OUTPUT`;
  try {
    await fetch(`/api/smartpoles/units/${poleCode}/dimming`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ brightness: parseInt(val), autoDimmingEnabled: false })
    });
  } catch (err) {
    console.error('Error changing brightness:', err);
  }
};

function initAmbientSimulator() {
  const slider = document.getElementById('lux-slider');
  const display = document.getElementById('lux-value-display');

  if (slider) {
    slider.addEventListener('input', async (e) => {
      const lux = parseFloat(e.target.value);
      if (display) display.innerText = `${lux} LUX`;
      await simulateAmbientLux(lux);
    });
  }

  document.querySelectorAll('[data-preset-lux]').forEach(btn => {
    btn.addEventListener('click', async () => {
      const lux = parseFloat(btn.getAttribute('data-preset-lux'));
      if (slider) slider.value = lux;
      if (display) display.innerText = `${lux} LUX`;
      await simulateAmbientLux(lux);
      showToast(`AMBIENT LUX SIMULATED: ${lux} LUX`);
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
      renderPolesList(state.poles);
    }
  } catch (err) {
    console.error('Ambient simulation error:', err);
  }
}

function initAddPoleForm() {
  const form = document.getElementById('form-add-pole');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      poleCode: document.getElementById('p-code').value.trim(),
      name: document.getElementById('p-name').value.trim(),
      location: document.getElementById('p-location').value.trim(),
      zone: document.getElementById('p-zone').value,
      chargerPowerKw: parseFloat(document.getElementById('p-power').value),
      currentKwhRate: parseFloat(document.getElementById('p-tariff').value),
      ledBrightness: parseInt(document.getElementById('p-brightness').value),
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
        showToast(`SMART POLE ${payload.poleCode} REGISTERED`);
        closeModal('modal-add-pole');
        form.reset();
        await loadPolesData();
      } else {
        const err = await res.text();
        showToast('ERROR: ' + err);
      }
    } catch (err) {
      console.error('Create pole error:', err);
    }
  });
}

// ==========================================================================
// 3. CHARGING SESSIONS (charging.html)
// ==========================================================================
window.initChargingPage = async function() {
  window.onPageDataRefresh = loadChargingData;
  initStartChargeForm();
  initStopChargeForm();
  await loadChargingData();
};

async function loadChargingData() {
  try {
    const [sRes, pRes, cRes] = await Promise.all([
      fetch('/api/smartpoles/charge/sessions'),
      fetch('/api/smartpoles/units'),
      fetch('/api/contacts?type=CUSTOMER')
    ]);

    if (sRes.ok) state.chargingSessions = await sRes.json();
    if (pRes.ok) state.poles = await pRes.json();
    if (cRes.ok) state.contacts = await cRes.json();

    renderChargingSessionsTable(state.chargingSessions);
    updateChargingStats();
    populateStartChargeDropdowns();
  } catch (err) {
    console.error('Error loading charging data:', err);
  }
}

function updateChargingStats() {
  const active = state.chargingSessions.filter(s => s.status === 'ACTIVE');
  let kwh = 0;
  let rev = 0;
  state.chargingSessions.forEach(s => {
    kwh += (s.kwhDelivered || 0);
    rev += (s.totalAmount || 0);
  });

  const actEl = document.getElementById('stat-active-sessions');
  if (actEl) actEl.innerText = active.length;

  const kwhEl = document.getElementById('stat-total-kwh');
  if (kwhEl) kwhEl.innerHTML = `${kwh.toFixed(1)} <span class="stat-unit">kWh</span>`;

  const revEl = document.getElementById('stat-total-rev');
  if (revEl) revEl.innerText = formatCurrency(rev);
}

function renderChargingSessionsTable(sessions) {
  const tbody = document.getElementById('charging-sessions-tbody');
  if (!tbody) return;

  if (sessions.length === 0) {
    tbody.innerHTML = '<tr><td colspan="10" class="text-center font-mono">NO CHARGING SESSIONS RECORDED YET.</td></tr>';
    return;
  }

  tbody.innerHTML = sessions.map(s => {
    const isActive = s.status === 'ACTIVE';
    const startTimeFormatted = s.startTime ? s.startTime.replace('T', ' ').substring(0, 16) : '-';

    return `
      <tr>
        <td class="font-mono">#${s.id}</td>
        <td class="font-mono"><strong>${escapeHtml(s.poleCode)}</strong></td>
        <td>${escapeHtml(s.driverOrFleetName || s.contactName || 'Commercial Fleet EV')}</td>
        <td class="font-mono">${startTimeFormatted}</td>
        <td class="font-mono">${s.durationMinutes || 0}m</td>
        <td class="font-mono">${(s.kwhDelivered || 0).toFixed(1)} kWh</td>
        <td class="font-mono">${formatCurrency(s.ratePerKwh || 0.35)}</td>
        <td class="font-mono"><strong>${formatCurrency(s.totalAmount || 0)}</strong></td>
        <td><span class="tag-badge ${isActive ? 'lime' : 'green'}">${s.status}</span></td>
        <td>
          ${isActive ? `
            <button class="btn btn-secondary btn-sm" onclick="openStopSessionModal(${s.id}, '${s.poleCode}')">
              <svg class="icon icon-sm" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><rect x="9" y="9" width="6" height="6"/></svg>
              STOP & BILL
            </button>
          ` : `
            <span class="font-mono" style="font-size:11px;">INV #${s.customerInvoiceId || 'N/A'}</span>
          `}
        </td>
      </tr>
    `;
  }).join('');
}

function populateStartChargeDropdowns() {
  const poleSelect = document.getElementById('start-session-pole');
  if (poleSelect) {
    const availPoles = state.poles.filter(p => (p.chargerStatus || '').toUpperCase() !== 'CHARGING');
    poleSelect.innerHTML = (availPoles.length > 0 ? availPoles : state.poles).map(p => `
      <option value="${p.poleCode}">${p.poleCode} — ${p.name} (${formatCurrency(p.currentKwhRate)}/kWh)</option>
    `).join('');
  }

  const custSelect = document.getElementById('start-session-customer');
  if (custSelect) {
    custSelect.innerHTML = state.contacts.map(c => `
      <option value="${c.id}">${c.name} (${c.roleCategory})</option>
    `).join('');
  }
}

function initStartChargeForm() {
  const form = document.getElementById('form-start-charge');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      poleCode: document.getElementById('start-session-pole').value,
      contactId: parseInt(document.getElementById('start-session-customer').value),
      driverOrFleetName: document.getElementById('start-session-fleet').value.trim() || 'Commercial Fleet EV'
    };

    try {
      const res = await fetch('/api/smartpoles/charge/start', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        showToast(`SESSION STARTED ON ${payload.poleCode}`);
        closeModal('modal-start-charge');
        form.reset();
        await loadChargingData();
      } else {
        const err = await res.text();
        showToast('ERROR: ' + err);
      }
    } catch (err) {
      console.error('Start charge error:', err);
    }
  });
}

function initStopChargeForm() {
  const form = document.getElementById('form-stop-charge');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const sessionId = document.getElementById('stop-session-id').value;
    const kwh = parseFloat(document.getElementById('stop-kwh-input').value);
    const createInvoice = document.getElementById('stop-autoinvoice').checked;

    try {
      const res = await fetch('/api/smartpoles/charge/stop', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          sessionId: parseInt(sessionId),
          kwhDelivered: kwh,
          createInvoice: createInvoice
        })
      });
      if (res.ok) {
        showToast(`SESSION #${sessionId} BILLED: ${kwh} kWh`);
        closeModal('modal-stop-charge');
        await loadChargingData();
      } else {
        const err = await res.text();
        showToast('ERROR: ' + err);
      }
    } catch (err) {
      console.error('Stop charge error:', err);
    }
  });
}

window.openStopSessionModal = function(sessionId, poleCode) {
  document.getElementById('stop-session-id').value = sessionId;
  document.getElementById('stop-session-info').innerText = `SESSION #${sessionId} // POLE: ${poleCode}`;
  openModal('modal-stop-charge');
};

window.openStopSessionModalForPole = function(poleCode, sessionId) {
  if (!sessionId) {
    const s = state.chargingSessions.find(sess => sess.poleCode === poleCode && sess.status === 'ACTIVE');
    if (s) sessionId = s.id;
  }
  window.openStopSessionModal(sessionId, poleCode);
};

// ==========================================================================
// 4. BILLING & INVOICING (billing.html)
// ==========================================================================
window.initBillingPage = async function() {
  window.onPageDataRefresh = loadBillingData;
  initSubnavTabs();
  initCreatePoForm();
  initCreateSoForm();
  await loadBillingData();
};

function initSubnavTabs() {
  const buttons = document.querySelectorAll('.subnav-btn');
  buttons.forEach(btn => {
    btn.addEventListener('click', () => {
      buttons.forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.subpanel-content').forEach(p => p.classList.remove('active'));

      btn.classList.add('active');
      const targetId = btn.getAttribute('data-target');
      const panel = document.getElementById(targetId);
      if (panel) panel.classList.add('active');
    });
  });
}

async function loadBillingData() {
  try {
    const [invRes, soRes, billRes, poRes, cRes, pRes] = await Promise.all([
      fetch('/api/customer-invoices'),
      fetch('/api/sales-orders'),
      fetch('/api/vendor-bills'),
      fetch('/api/purchase-orders'),
      fetch('/api/contacts'),
      fetch('/api/products')
    ]);

    if (invRes.ok) state.customerInvoices = await invRes.json();
    if (soRes.ok) state.salesOrders = await soRes.json();
    if (billRes.ok) state.vendorBills = await billRes.json();
    if (poRes.ok) state.purchaseOrders = await poRes.json();
    if (cRes.ok) state.contacts = await cRes.json();
    if (pRes.ok) state.products = await pRes.json();

    renderInvoicesTable();
    renderSalesOrdersTable();
    renderVendorBillsTable();
    renderPurchaseOrdersTable();
    populateBillingModalDropdowns();
  } catch (err) {
    console.error('Error loading billing data:', err);
  }
}

function renderInvoicesTable() {
  const tbody = document.getElementById('invoices-tbody');
  if (!tbody) return;
  if (state.customerInvoices.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center font-mono">NO CUSTOMER INVOICES RECORDED</td></tr>';
    return;
  }
  tbody.innerHTML = state.customerInvoices.map(inv => `
    <tr>
      <td class="font-mono"><strong>${inv.invoiceNumber}</strong></td>
      <td><strong>${escapeHtml(inv.customerName)}</strong></td>
      <td class="font-mono">${inv.invoiceDate || '-'}</td>
      <td class="font-mono">${inv.dueDate || '-'}</td>
      <td class="font-mono text-right"><strong>${formatCurrency(inv.totalAmount)}</strong></td>
      <td><span class="tag-badge ${inv.status === 'PAID' ? 'green' : 'lime'}">${inv.status}</span></td>
      <td>
        ${inv.status !== 'PAID' ? `
          <button class="btn btn-primary btn-sm" onclick="payCustomerInvoice(${inv.id})">
            <svg class="icon icon-sm" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
            COLLECT PAYMENT
          </button>
        ` : `<span class="tag-badge green">SETTLED</span>`}
      </td>
    </tr>
  `).join('');
}

function renderSalesOrdersTable() {
  const tbody = document.getElementById('so-tbody');
  if (!tbody) return;
  if (state.salesOrders.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center font-mono">NO SALES ORDERS RECORDED</td></tr>';
    return;
  }
  tbody.innerHTML = state.salesOrders.map(so => `
    <tr>
      <td class="font-mono"><strong>${so.soNumber}</strong></td>
      <td>${escapeHtml(so.customerName)}</td>
      <td class="font-mono">${so.orderDate || '-'}</td>
      <td class="font-mono text-right"><strong>${formatCurrency(so.totalAmount)}</strong></td>
      <td><span class="tag-badge ${so.status === 'INVOICED' ? 'green' : 'blue'}">${so.status}</span></td>
      <td>
        ${so.status !== 'INVOICED' ? `
          <button class="btn btn-secondary btn-sm" onclick="convertSoToInvoice(${so.id})">
            <svg class="icon icon-sm" viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            CONVERT TO INVOICE
          </button>
        ` : `<span class="tag-badge green">INVOICED</span>`}
      </td>
    </tr>
  `).join('');
}

function renderVendorBillsTable() {
  const tbody = document.getElementById('vendor-bills-tbody');
  if (!tbody) return;
  if (state.vendorBills.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center font-mono">NO VENDOR BILLS RECORDED</td></tr>';
    return;
  }
  tbody.innerHTML = state.vendorBills.map(b => `
    <tr>
      <td class="font-mono"><strong>${b.billNumber}</strong></td>
      <td><strong>${escapeHtml(b.vendorName)}</strong></td>
      <td class="font-mono">${b.billDate || '-'}</td>
      <td class="font-mono">${b.dueDate || '-'}</td>
      <td class="font-mono text-right"><strong>${formatCurrency(b.totalAmount)}</strong></td>
      <td><span class="tag-badge ${b.status === 'PAID' ? 'green' : 'orange'}">${b.status}</span></td>
      <td>
        ${b.status !== 'PAID' ? `
          <button class="btn btn-primary btn-sm" onclick="payVendorBill(${b.id})">
            <svg class="icon icon-sm" viewBox="0 0 24 24"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
            PAY VIA BANK
          </button>
        ` : `<span class="tag-badge green">DISBURSED</span>`}
      </td>
    </tr>
  `).join('');
}

function renderPurchaseOrdersTable() {
  const tbody = document.getElementById('po-tbody');
  if (!tbody) return;
  if (state.purchaseOrders.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center font-mono">NO PURCHASE ORDERS RECORDED</td></tr>';
    return;
  }
  tbody.innerHTML = state.purchaseOrders.map(po => `
    <tr>
      <td class="font-mono"><strong>${po.poNumber}</strong></td>
      <td>${escapeHtml(po.vendorName)}</td>
      <td class="font-mono">${po.orderDate || '-'}</td>
      <td class="font-mono text-right"><strong>${formatCurrency(po.totalAmount)}</strong></td>
      <td><span class="tag-badge ${po.status === 'BILLED' ? 'green' : 'orange'}">${po.status}</span></td>
      <td>
        ${po.status !== 'BILLED' ? `
          <button class="btn btn-secondary btn-sm" onclick="convertPoToBill(${po.id})">
            <svg class="icon icon-sm" viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
            CONVERT TO BILL
          </button>
        ` : `<span class="tag-badge green">BILLED</span>`}
      </td>
    </tr>
  `).join('');
}

function populateBillingModalDropdowns() {
  const poVendorSelect = document.getElementById('po-form-vendor');
  if (poVendorSelect) {
    const vendors = state.contacts.filter(c => c.contactType === 'VENDOR');
    poVendorSelect.innerHTML = vendors.map(v => `<option value="${v.id}">${v.name}</option>`).join('');
  }

  const poProdSelect = document.getElementById('po-form-product');
  if (poProdSelect) {
    poProdSelect.innerHTML = state.products.map(p => `<option value="${p.id}" data-price="${p.unitPrice}">${p.code} - ${p.name} (${formatCurrency(p.unitPrice)})</option>`).join('');
  }

  const soCustomerSelect = document.getElementById('so-form-customer');
  if (soCustomerSelect) {
    const customers = state.contacts.filter(c => c.contactType === 'CUSTOMER');
    soCustomerSelect.innerHTML = customers.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  }

  const soProdSelect = document.getElementById('so-form-product');
  if (soProdSelect) {
    soProdSelect.innerHTML = state.products.map(p => `<option value="${p.id}" data-price="${p.unitPrice}">${p.code} - ${p.name} (${formatCurrency(p.unitPrice)})</option>`).join('');
  }
}

function initCreatePoForm() {
  const form = document.getElementById('form-new-po');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const vendorSelect = document.getElementById('po-form-vendor');
    const prodSelect = document.getElementById('po-form-product');
    const qty = parseFloat(document.getElementById('po-form-qty').value);
    const selectedProd = prodSelect.options[prodSelect.selectedIndex];
    const unitPrice = parseFloat(selectedProd.getAttribute('data-price')) || 100.0;

    const payload = {
      vendorId: parseInt(vendorSelect.value),
      vendorName: vendorSelect.options[vendorSelect.selectedIndex].text,
      notes: document.getElementById('po-form-notes').value.trim() || 'Municipal LED Procurement',
      items: [{
        productId: parseInt(prodSelect.value),
        quantity: qty,
        unitPrice: unitPrice
      }]
    };

    try {
      const res = await fetch('/api/purchase-orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        showToast('PURCHASE ORDER CREATED');
        closeModal('modal-new-po');
        await loadBillingData();
      } else {
        const err = await res.text();
        showToast('ERROR: ' + err);
      }
    } catch (err) {
      console.error('Create PO error:', err);
    }
  });
}

function initCreateSoForm() {
  const form = document.getElementById('form-new-so');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const custSelect = document.getElementById('so-form-customer');
    const prodSelect = document.getElementById('so-form-product');
    const qty = parseFloat(document.getElementById('so-form-qty').value);
    const selectedProd = prodSelect.options[prodSelect.selectedIndex];
    const unitPrice = parseFloat(selectedProd.getAttribute('data-price')) || 0.35;

    const payload = {
      customerId: parseInt(custSelect.value),
      customerName: custSelect.options[custSelect.selectedIndex].text,
      notes: document.getElementById('so-form-notes').value.trim() || 'Fleet Charging Quota',
      items: [{
        productId: parseInt(prodSelect.value),
        quantity: qty,
        unitPrice: unitPrice
      }]
    };

    try {
      const res = await fetch('/api/sales-orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        showToast('SALES ORDER CREATED');
        closeModal('modal-new-so');
        await loadBillingData();
      } else {
        const err = await res.text();
        showToast('ERROR: ' + err);
      }
    } catch (err) {
      console.error('Create SO error:', err);
    }
  });
}

window.convertPoToBill = async function(poId) {
  try {
    const res = await fetch(`/api/purchase-orders/${poId}/convert-to-bill`, { method: 'POST' });
    if (res.ok) {
      showToast('PO CONVERTED TO VENDOR BILL (AP POSTED)');
      await loadBillingData();
    } else {
      const err = await res.text();
      showToast('ERROR: ' + err);
    }
  } catch (err) {
    console.error('Convert PO error:', err);
  }
};

window.payVendorBill = async function(billId) {
  try {
    const res = await fetch(`/api/vendor-bills/${billId}/pay`, { method: 'POST' });
    if (res.ok) {
      showToast('VENDOR BILL PAID VIA BANK (DISBURSED)');
      await loadBillingData();
    } else {
      const err = await res.text();
      showToast('ERROR: ' + err);
    }
  } catch (err) {
    console.error('Pay bill error:', err);
  }
};

window.convertSoToInvoice = async function(soId) {
  try {
    const res = await fetch(`/api/sales-orders/${soId}/convert-to-invoice`, { method: 'POST' });
    if (res.ok) {
      showToast('SO CONVERTED TO CUSTOMER INVOICE (AR POSTED)');
      await loadBillingData();
    } else {
      const err = await res.text();
      showToast('ERROR: ' + err);
    }
  } catch (err) {
    console.error('Convert SO error:', err);
  }
};

window.payCustomerInvoice = async function(invoiceId) {
  try {
    const res = await fetch(`/api/customer-invoices/${invoiceId}/pay`, { method: 'POST' });
    if (res.ok) {
      showToast('CUSTOMER PAYMENT COLLECTED VIA BANK (RECEIPT)');
      await loadBillingData();
    } else {
      const err = await res.text();
      showToast('ERROR: ' + err);
    }
  } catch (err) {
    console.error('Pay invoice error:', err);
  }
};

// ==========================================================================
// 5. GENERAL LEDGER (ledger.html)
// ==========================================================================
window.initLedgerPage = async function() {
  window.onPageDataRefresh = loadLedgerData;
  await loadLedgerData();
};

async function loadLedgerData() {
  try {
    const [jeRes, jRes] = await Promise.all([
      fetch('/api/journal-entries'),
      fetch('/api/journals')
    ]);

    if (jeRes.ok) state.journalEntries = await jeRes.json();
    if (jRes.ok) state.journals = await jRes.json();

    renderJournalEntriesTable();
    renderJournalsMasterTable();
  } catch (err) {
    console.error('Error loading ledger data:', err);
  }
}

function renderJournalEntriesTable() {
  const tbody = document.getElementById('ledger-entries-tbody');
  if (!tbody) return;

  if (state.journalEntries.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" class="text-center font-mono">NO JOURNAL ENTRIES POSTED</td></tr>';
    return;
  }

  tbody.innerHTML = state.journalEntries.map(je => `
    <tr>
      <td class="font-mono"><strong>${je.entryNumber}</strong></td>
      <td class="font-mono"><span class="tag-badge dark">${je.journalCode}</span></td>
      <td class="font-mono">${je.entryDate || '-'}</td>
      <td class="font-mono">${escapeHtml(je.reference || '-')}</td>
      <td>${escapeHtml(je.description || '-')}</td>
      <td class="font-mono text-right">${formatCurrency(je.totalDebit)}</td>
      <td class="font-mono text-right">${formatCurrency(je.totalCredit)}</td>
      <td><span class="tag-badge green">BALANCED</span></td>
    </tr>
  `).join('');
}

function renderJournalsMasterTable() {
  const tbody = document.getElementById('journals-master-tbody');
  if (!tbody) return;

  if (state.journals.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" class="text-center font-mono">NO JOURNALS CONFIGURED</td></tr>';
    return;
  }

  tbody.innerHTML = state.journals.map(j => `
    <tr>
      <td class="font-mono"><strong>${j.code}</strong></td>
      <td><strong>${escapeHtml(j.name)}</strong></td>
      <td class="font-mono">${j.journalType}</td>
      <td class="font-mono" style="font-size:11px;">DOUBLE-ENTRY DEBIT/CREDIT POSTING</td>
    </tr>
  `).join('');
}

// ==========================================================================
// 6. CONTACTS (contacts.html)
// ==========================================================================
window.initContactsPage = async function() {
  window.onPageDataRefresh = loadContactsData;
  initAddContactForm();
  await loadContactsData();
};

async function loadContactsData() {
  try {
    const res = await fetch('/api/contacts');
    if (res.ok) {
      state.contacts = await res.json();
      renderContactsTable();
    }
  } catch (err) {
    console.error('Error loading contacts:', err);
  }
}

function renderContactsTable() {
  const tbody = document.getElementById('contacts-tbody');
  if (!tbody) return;

  if (state.contacts.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center font-mono">NO CONTACTS REGISTERED</td></tr>';
    return;
  }

  tbody.innerHTML = state.contacts.map(c => `
    <tr>
      <td class="font-mono">#${c.id}</td>
      <td><strong>${escapeHtml(c.name)}</strong></td>
      <td><span class="tag-badge ${c.contactType === 'CUSTOMER' ? 'lime' : 'orange'}">${c.contactType}</span></td>
      <td>${escapeHtml(c.roleCategory || '-')}</td>
      <td class="font-mono">${escapeHtml(c.email || '-')}</td>
      <td class="font-mono">${escapeHtml(c.phone || '-')}</td>
      <td class="font-mono text-right"><strong>${formatCurrency(c.accountBalance || 0)}</strong></td>
    </tr>
  `).join('');
}

function initAddContactForm() {
  const form = document.getElementById('form-add-contact');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      name: document.getElementById('c-name').value.trim(),
      contactType: document.getElementById('c-type').value,
      roleCategory: document.getElementById('c-role').value.trim(),
      email: document.getElementById('c-email').value.trim(),
      phone: document.getElementById('c-phone').value.trim(),
      address: document.getElementById('c-address').value.trim(),
      accountBalance: 0.0
    };

    try {
      const res = await fetch('/api/contacts', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        showToast('CONTACT REGISTERED: ' + payload.name.toUpperCase());
        closeModal('modal-add-contact');
        form.reset();
        await loadContactsData();
      } else {
        const err = await res.text();
        showToast('ERROR: ' + err);
      }
    } catch (err) {
      console.error('Add contact error:', err);
    }
  });
}

// ==========================================================================
// 7. PRODUCTS (products.html)
// ==========================================================================
window.initProductsPage = async function() {
  window.onPageDataRefresh = loadProductsData;
  initAddProductForm();
  await loadProductsData();
};

async function loadProductsData() {
  try {
    const res = await fetch('/api/products');
    if (res.ok) {
      state.products = await res.json();
      renderProductsTable();
    }
  } catch (err) {
    console.error('Error loading products:', err);
  }
}

function renderProductsTable() {
  const tbody = document.getElementById('products-tbody');
  if (!tbody) return;

  if (state.products.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center font-mono">NO PRODUCTS REGISTERED</td></tr>';
    return;
  }

  tbody.innerHTML = state.products.map(p => `
    <tr>
      <td class="font-mono"><strong>${p.code}</strong></td>
      <td><strong>${escapeHtml(p.name)}</strong></td>
      <td><span class="tag-badge ${p.productType === 'SERVICE' ? 'blue' : 'lime'}">${p.productType}</span></td>
      <td class="font-mono text-right"><strong>${formatCurrency(p.unitPrice)}</strong></td>
      <td class="font-mono">${escapeHtml(p.unitOfMeasure)}</td>
      <td>${escapeHtml(p.description || '-')}</td>
    </tr>
  `).join('');
}

function initAddProductForm() {
  const form = document.getElementById('form-add-product');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      code: document.getElementById('prod-code').value.trim(),
      name: document.getElementById('prod-name').value.trim(),
      productType: document.getElementById('prod-type').value,
      unitPrice: parseFloat(document.getElementById('prod-price').value),
      unitOfMeasure: document.getElementById('prod-uom').value.trim(),
      description: document.getElementById('prod-desc').value.trim()
    };

    try {
      const res = await fetch('/api/products', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        showToast('PRODUCT REGISTERED: ' + payload.code);
        closeModal('modal-add-product');
        form.reset();
        await loadProductsData();
      } else {
        const err = await res.text();
        showToast('ERROR: ' + err);
      }
    } catch (err) {
      console.error('Add product error:', err);
    }
  });
}

// ==========================================================================
// 8. CHART OF ACCOUNTS (coa.html)
// ==========================================================================
window.initCoaPage = async function() {
  window.onPageDataRefresh = loadCoaData;
  await loadCoaData();
};

async function loadCoaData() {
  try {
    const res = await fetch('/api/accounts');
    if (res.ok) {
      state.accounts = await res.json();
      renderCoaCategories();
    }
  } catch (err) {
    console.error('Error loading CoA accounts:', err);
  }
}

function renderCoaCategories() {
  const assets = state.accounts.filter(a => a.accountType === 'ASSET');
  const liabs = state.accounts.filter(a => a.accountType === 'LIABILITY');
  const revs = state.accounts.filter(a => a.accountType === 'INCOME');
  const exps = state.accounts.filter(a => a.accountType === 'EXPENSE');

  renderCoaAccountList('coa-assets-tbody', assets);
  renderCoaAccountList('coa-liabs-tbody', liabs);
  renderCoaAccountList('coa-revs-tbody', revs);
  renderCoaAccountList('coa-exps-tbody', exps);
}

function renderCoaAccountList(tbodyId, list) {
  const tbody = document.getElementById(tbodyId);
  if (!tbody) return;

  if (list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="3" class="text-center font-mono">NO ACCOUNTS IN CATEGORY</td></tr>';
    return;
  }

  tbody.innerHTML = list.map(a => `
    <tr>
      <td class="font-mono"><strong>${a.code}</strong></td>
      <td><strong>${escapeHtml(a.name)}</strong></td>
      <td class="font-mono text-right">${formatCurrency(a.balance || 0)}</td>
    </tr>
  `).join('');
}

// ==========================================================================
// 9. BUDGET & SECTOR ANALYTICS (budget.html)
// ==========================================================================
window.initBudgetPage = async function() {
  window.onPageDataRefresh = loadBudgetData;
  await loadBudgetData();
};

async function loadBudgetData() {
  try {
    const res = await fetch('/api/budgets');
    if (res.ok) {
      state.budgets = await res.json();
      renderBudgetOverview();
    }
  } catch (err) {
    console.error('Error loading budget data:', err);
  }
}

function renderBudgetOverview() {
  if (state.budgets.length === 0) return;
  const b = state.budgets[0];

  const planRevEl = document.getElementById('b-plan-rev');
  if (planRevEl) planRevEl.innerText = formatCurrency(b.totalPlannedRevenue);

  const actRevEl = document.getElementById('b-act-rev');
  if (actRevEl) actRevEl.innerText = formatCurrency(b.totalActualRevenue);

  const planExpEl = document.getElementById('b-plan-exp');
  if (planExpEl) planExpEl.innerText = formatCurrency(b.totalPlannedExpense);

  const actExpEl = document.getElementById('b-act-exp');
  if (actExpEl) actExpEl.innerText = formatCurrency(b.totalActualExpense);

  const marginEl = document.getElementById('b-margin');
  if (marginEl) {
    marginEl.innerText = `${b.netActualMargin >= 0 ? '+' : ''}${formatCurrency(b.netActualMargin)}`;
  }

  const tbody = document.getElementById('budget-lines-tbody');
  if (tbody && b.lines) {
    tbody.innerHTML = b.lines.map(line => `
      <tr>
        <td class="font-mono">${line.accountCode || '-'}</td>
        <td><strong>${escapeHtml(line.accountName)}</strong></td>
        <td><span class="tag-badge ${line.accountType === 'INCOME' ? 'lime' : 'orange'}">${line.accountType}</span></td>
        <td class="font-mono text-right">${formatCurrency(line.plannedAmount)}</td>
        <td class="font-mono text-right"><strong>${formatCurrency(line.actualAmount)}</strong></td>
        <td class="font-mono text-right">${line.variance >= 0 ? '+' : ''}${formatCurrency(line.variance)}</td>
        <td class="font-mono text-right"><strong>${(line.achievementRate || 0).toFixed(1)}%</strong></td>
      </tr>
    `).join('');
  }
}

// ==========================================================================
// 10. FINANCIAL REPORTS (reports.html)
// ==========================================================================
window.initReportsPage = async function() {
  window.onPageDataRefresh = loadReportsData;
  initSubnavTabs();
  await loadReportsData();
};

async function loadReportsData() {
  try {
    const [bsRes, pnlRes, budRes] = await Promise.all([
      fetch('/api/reports/balance-sheet'),
      fetch('/api/reports/profit-and-loss'),
      fetch('/api/reports/budget-performance')
    ]);

    if (bsRes.ok) renderBalanceSheetView(await bsRes.json());
    if (pnlRes.ok) renderProfitAndLossView(await pnlRes.json());
    if (budRes.ok) renderBudgetReportView(await budRes.json());
  } catch (err) {
    console.error('Error loading reports:', err);
  }
}

function renderBalanceSheetView(bs) {
  if (!bs) return;

  const assetsTbody = document.getElementById('bs-assets-tbody');
  if (assetsTbody) {
    assetsTbody.innerHTML = bs.assets.map(a => `
      <tr>
        <td><strong>${escapeHtml(a.name)}</strong> <span class="font-mono" style="font-size:11px;">(${a.code})</span></td>
        <td class="font-mono text-right">${formatCurrency(a.balance)}</td>
      </tr>
    `).join('');
  }

  const liabsTbody = document.getElementById('bs-liabs-tbody');
  if (liabsTbody) {
    liabsTbody.innerHTML = bs.liabilities.map(l => `
      <tr>
        <td><strong>${escapeHtml(l.name)}</strong> <span class="font-mono" style="font-size:11px;">(${l.code})</span></td>
        <td class="font-mono text-right">${formatCurrency(l.balance)}</td>
      </tr>
    `).join('');
  }

  const totAssetsEl = document.getElementById('bs-total-assets');
  if (totAssetsEl) totAssetsEl.innerText = formatCurrency(bs.totalAssets);

  const surplusEl = document.getElementById('bs-surplus');
  if (surplusEl) surplusEl.innerText = formatCurrency(bs.retainedSurplus);

  const totLiabEqEl = document.getElementById('bs-total-liab-equity');
  if (totLiabEqEl) totLiabEqEl.innerText = formatCurrency(bs.totalLiabilitiesAndEquity);
}

function renderProfitAndLossView(pnl) {
  if (!pnl) return;

  const revTbody = document.getElementById('pnl-revenue-tbody');
  if (revTbody) {
    revTbody.innerHTML = pnl.revenues.map(r => `
      <tr>
        <td><strong>${escapeHtml(r.name)}</strong></td>
        <td class="font-mono text-right">${formatCurrency(r.balance)}</td>
      </tr>
    `).join('');
  }

  const expTbody = document.getElementById('pnl-expenses-tbody');
  if (expTbody) {
    expTbody.innerHTML = pnl.expenses.map(e => `
      <tr>
        <td><strong>${escapeHtml(e.name)}</strong></td>
        <td class="font-mono text-right">${formatCurrency(e.balance)}</td>
      </tr>
    `).join('');
  }

  const totRevEl = document.getElementById('pnl-total-rev');
  if (totRevEl) totRevEl.innerText = formatCurrency(pnl.totalRevenue);

  const totExpEl = document.getElementById('pnl-total-exp');
  if (totExpEl) totExpEl.innerText = formatCurrency(pnl.totalExpense);

  const netSurplusEl = document.getElementById('pnl-net-surplus');
  if (netSurplusEl) {
    netSurplusEl.innerText = `${pnl.netOperatingSurplus >= 0 ? '+' : ''}${formatCurrency(pnl.netOperatingSurplus)}`;
  }

  const marginEl = document.getElementById('pnl-margin-badge');
  if (marginEl) {
    marginEl.innerText = `OPERATING MARGIN: ${(pnl.marginPercent || 0).toFixed(1)}%`;
  }
}

function renderBudgetReportView(bud) {
  if (!bud || !bud.lines) return;

  const tbody = document.getElementById('report-budget-tbody');
  if (tbody) {
    tbody.innerHTML = bud.lines.map(l => `
      <tr>
        <td class="font-mono">${l.accountCode || '-'}</td>
        <td><strong>${escapeHtml(l.accountName)}</strong></td>
        <td><span class="tag-badge ${l.accountType === 'INCOME' ? 'lime' : 'orange'}">${l.accountType}</span></td>
        <td class="font-mono text-right">${formatCurrency(l.plannedAmount)}</td>
        <td class="font-mono text-right"><strong>${formatCurrency(l.actualAmount)}</strong></td>
        <td class="font-mono text-right">${l.variance >= 0 ? '+' : ''}${formatCurrency(l.variance)}</td>
        <td class="font-mono text-right"><strong>${(l.achievementRate || 0).toFixed(1)}%</strong></td>
      </tr>
    `).join('');
  }
}

// ==========================================================================
// 11. 4-STEP WORKFLOW (workflow.html)
// ==========================================================================
window.initWorkflowPage = function() {
  const btn = document.getElementById('btn-run-workflow');
  const term = document.getElementById('terminal-output');

  if (btn && term) {
    btn.addEventListener('click', async () => {
      btn.disabled = true;
      btn.innerHTML = `
        <svg class="icon icon-sm" viewBox="0 0 24 24"><line x1="12" y1="2" x2="12" y2="6"/><line x1="12" y1="18" x2="12" y2="22"/><line x1="4.93" y1="4.93" x2="7.76" y2="7.76"/><line x1="16.24" y1="16.24" x2="19.07" y2="19.07"/><line x1="2" y1="12" x2="6" y2="12"/><line x1="18" y1="12" x2="22" y2="12"/><line x1="4.93" y1="19.07" x2="7.76" y2="16.24"/><line x1="16.24" y1="7.76" x2="19.07" y2="4.93"/></svg>
        EXECUTING WORKFLOW...
      `;

      for (let i = 1; i <= 4; i++) {
        const card = document.getElementById(`step-card-${i}`);
        const stat = document.getElementById(`step-stat-${i}`);
        if (card) { card.className = 'step-card'; }
        if (stat) { stat.innerText = 'STATUS: QUEUED'; }
      }

      term.innerText = '>> [INITIATED] EXECUTING 4-STEP MUNICIPAL SPECIFICATION WORKFLOW...\n';

      try {
        setStepState(1, 'active', 'EXECUTING: POST /api/smartpoles/units & registering clients');
        term.innerText += '>> [STEP 1] Initializing Smart Pole Master Unit & Fleet Accounts...\n';
        await sleep(600);

        const res = await fetch('/api/demo/run-use-case', { method: 'POST' });
        const data = await res.json();

        setStepState(1, 'done', `REGISTERED: ${data.step1_masterData.createdPoleCode}`);
        term.innerText += `>> [STEP 1 OK] Created Pole Unit ${data.step1_masterData.createdPoleCode} in ${data.step1_masterData.location}.\n`;

        await sleep(700);
        setStepState(2, 'active', 'PROCESSING: Charging vehicle & metering kWh');
        term.innerText += '>> [STEP 2] Simulating vehicle connection, metering 24.5 kWh...\n';

        await sleep(700);
        setStepState(2, 'done', `DELIVERED: ${data.step2_chargingSession.kwhDelivered} kWh ($${data.step2_chargingSession.totalCharged})`);
        term.innerText += `>> [STEP 2 OK] Session completed. Total Billed: $${data.step2_chargingSession.totalCharged} (Customer Invoice #${data.step2_chargingSession.customerInvoiceId}).\n`;

        await sleep(700);
        setStepState(3, 'active', 'POSTING: Vendor bill and bank payment');
        term.innerText += '>> [STEP 3] Issuing Vendor Bill for LED optics and posting Bank Disbursement...\n';

        await sleep(700);
        setStepState(3, 'done', `PAID: Bill ${data.step3_invoicing.vendorBillNumber} ($${data.step3_invoicing.vendorPaymentAmount})`);
        term.innerText += `>> [STEP 3 OK] Vendor Bill ${data.step3_invoicing.vendorBillNumber} converted & paid via Bank (${data.step3_invoicing.vendorPaymentNumber}).\n`;

        await sleep(700);
        setStepState(4, 'active', 'GENERATING: P&L and Balance Sheet');
        term.innerText += '>> [STEP 4] Compiling Balance Sheet, P&L, and Budget Analytics...\n';

        await sleep(700);
        setStepState(4, 'done', `SURPLUS: $${data.step4_reports.pnlNetOperatingSurplus} • BALANCED: YES`);
        term.innerText += `>> [STEP 4 OK] Reports Compiled Successfully:\n` +
          `   - Total Revenue: $${data.step4_reports.pnlTotalRevenue}\n` +
          `   - Total Expense: $${data.step4_reports.pnlTotalExpense}\n` +
          `   - Net Municipal Operating Surplus: $${data.step4_reports.pnlNetOperatingSurplus}\n` +
          `   - Budget Performance Ratio: ${data.step4_reports.budgetPerformanceRatio}\n` +
          `   - Balance Sheet Integrity: ${data.step4_reports.balanceSheetBalanced ? 'BALANCED (Assets = Liabilities + Equity)' : 'UNBALANCED'}\n\n` +
          `>> ALL 4 FOUNDATIONAL MUNICIPAL STEPS COMPLETED WITHOUT ERROR.`;

        showToast('4-STEP WORKFLOW EXECUTED SUCCESSFULLY');
      } catch (err) {
        term.innerText += `\n>> [ERROR] Workflow execution failed: ${err.message}`;
        showToast('WORKFLOW FAILED: ' + err.message);
      } finally {
        btn.disabled = false;
        btn.innerHTML = `
          <svg class="icon icon-sm" viewBox="0 0 24 24"><polygon points="5 3 19 12 5 21 5 3"/></svg>
          EXECUTE ALL 4 STEPS
        `;
      }
    });
  }
};

function setStepState(num, stateClass, text) {
  const card = document.getElementById(`step-card-${num}`);
  const stat = document.getElementById(`step-stat-${num}`);
  if (card) { card.className = `step-card ${stateClass}`; }
  if (stat) { stat.innerText = `STATUS: ${text.toUpperCase()}`; }
}

// ==========================================================================
// 12. UNIVERSAL NESTED TABS SWITCHER (WITH HASH ROUTING)
// ==========================================================================
function initNestedTabBar() {
  const tabBars = document.querySelectorAll('.nested-tab-bar');
  tabBars.forEach(bar => {
    const buttons = bar.querySelectorAll('.nested-tab-btn');
    buttons.forEach(btn => {
      btn.addEventListener('click', () => {
        const targetId = btn.getAttribute('data-target');
        activateNestedTab(bar, targetId);
        if (history.replaceState) {
          const cleanHash = targetId.replace(/^nested-/, '');
          history.replaceState(null, null, '#' + cleanHash);
        }
      });
    });
  });

  // Check URL hash on page load (e.g. #charging, #workflow, #ledger, #budget, etc.)
  if (window.location.hash) {
    const raw = window.location.hash.substring(1).toLowerCase();
    const btn = document.querySelector(`.nested-tab-btn[data-target="nested-${raw}"]`) ||
                document.querySelector(`.nested-tab-btn[data-target="${raw}"]`);
    if (btn) {
      const bar = btn.closest('.nested-tab-bar');
      if (bar) {
        activateNestedTab(bar, btn.getAttribute('data-target'));
      }
    }
  }
}

function activateNestedTab(bar, targetId) {
  const buttons = bar.querySelectorAll('.nested-tab-btn');
  buttons.forEach(b => b.classList.remove('active'));
  const activeBtn = bar.querySelector(`.nested-tab-btn[data-target="${targetId}"]`);
  if (activeBtn) activeBtn.classList.add('active');

  const panels = document.querySelectorAll('.nested-panel');
  panels.forEach(p => {
    const panelId = p.id;
    const isTargetOfThisBar = Array.from(buttons).some(b => b.getAttribute('data-target') === panelId);
    if (isTargetOfThisBar) {
      if (panelId === targetId) {
        p.classList.add('active');
      } else {
        p.classList.remove('active');
      }
    }
  });
}

// ==========================================================================
// 13. DOMAIN HUB PAGE INITIALIZERS
// ==========================================================================
window.initGridHubPage = async function() {
  initNestedTabBar();
  initAmbientSimulator();
  initAddPoleForm();
  initStartChargeForm();
  initStopChargeForm();
  window.initWorkflowPage();

  window.onPageDataRefresh = async () => {
    await Promise.all([loadPolesData(), loadChargingData()]);
  };
  await Promise.all([loadPolesData(), loadChargingData()]);
};

window.initFinanceHubPage = async function() {
  initNestedTabBar();
  initSubnavTabs();
  initCreatePoForm();
  initCreateSoForm();

  window.onPageDataRefresh = async () => {
    await Promise.all([loadBillingData(), loadLedgerData(), loadReportsData()]);
  };
  await Promise.all([loadBillingData(), loadLedgerData(), loadReportsData()]);
};

window.initMasterHubPage = async function() {
  initNestedTabBar();
  initAddContactForm();
  initAddProductForm();

  window.onPageDataRefresh = async () => {
    await Promise.all([loadContactsData(), loadProductsData(), loadCoaData(), loadBudgetData()]);
  };
  await Promise.all([loadContactsData(), loadProductsData(), loadCoaData(), loadBudgetData()]);
};
