(() => {
  const { api, escapeHtml, formatDate, statusPill, toast } = window.GymFlex;
  const table = document.getElementById("expiring-table");
  let expiringMemberships = [];
  async function load() {
    try {
      expiringMemberships = await api("/api/memberships/expiring-soon");
      document.getElementById("expiring-total").textContent = String(expiringMemberships.length).padStart(2, "0");
      table.innerHTML = expiringMemberships.length ? expiringMemberships.map(item => {
        const days = Math.max(0, Math.round((new Date(`${item.expiryDate}T00:00:00`) - new Date(new Date().toLocaleDateString("en-CA") + "T00:00:00")) / 86400000));
        return `<tr><td><span class="table-person"><span class="table-avatar">${escapeHtml(item.memberName.split(/\s+/).map(part => part[0]).slice(0, 2).join("").toUpperCase())}</span><span><strong>${escapeHtml(item.memberName)}</strong><small>ID #${item.memberId}</small></span></span></td><td>${escapeHtml(item.phone || "—")}</td><td>${escapeHtml(item.planName)}</td><td>${formatDate(item.expiryDate)}</td><td><strong class="days-remaining">${days === 0 ? "Today" : `${days} day${days === 1 ? "" : "s"}`}</strong></td><td>${statusPill("EXPIRING_SOON")}</td><td class="align-right"><button class="button button-small button-primary" data-renew="${item.id}">Renew <span>→</span></button></td></tr>`;
      }).join("") : `<tr><td colspan="7" class="empty-cell">No memberships expire in the next seven days.</td></tr>`;
    } catch (error) { table.innerHTML = `<tr><td colspan="7" class="empty-cell">${escapeHtml(error.message)}</td></tr>`; }
  }
  table.addEventListener("click", event => {
    const button = event.target.closest("button[data-renew]");
    if (!button) return;
    const item = expiringMemberships.find(record => record.id === Number(button.dataset.renew));
    if (item) window.GymFlex.openRenewDialog(item).catch(error => toast(error.message, "error"));
  });
  load();
  window.addEventListener("gymflex:changed", load);
})();
