(() => {
  const { api, escapeHtml, formatDateTime } = window.GymFlex;
  const today = new Date();
  const label = document.getElementById("today-label");
  if (label) label.textContent = today.toLocaleDateString(undefined, { weekday: "long", day: "2-digit", month: "long" }).toUpperCase();

  async function loadDashboard() {
    try {
      const stats = await api("/api/dashboard/stats");
      document.getElementById("total-members").textContent = stats.totalMembers;
      document.getElementById("active-members").textContent = stats.activeMembers;
      document.getElementById("expired-members").textContent = stats.expiredMembers;
      document.getElementById("expiring-members").textContent = stats.expiringSoon;
      document.getElementById("today-checkins").textContent = stats.todaysCheckIns;
      const list = document.getElementById("recent-checkins");
      list.innerHTML = stats.recentCheckIns.length ? stats.recentCheckIns.map((item, index) => `<div class="activity-row"><span class="activity-index">${String(index + 1).padStart(2, "0")}</span><span class="activity-avatar">${escapeHtml(item.memberName.split(/\s+/).map(part => part[0]).slice(0, 2).join("").toUpperCase())}</span><span class="activity-name">${escapeHtml(item.memberName)}<small>GYMFLEX MEMBER</small></span><time>${formatDateTime(item.checkInDateTime)}</time></div>`).join("") : `<div class="empty-state">No check-ins recorded yet today.</div>`;
    } catch (error) {
      window.GymFlex.toast(error.message, "error");
      document.getElementById("recent-checkins").innerHTML = `<div class="empty-state">Dashboard data could not be loaded. Check the server and database connection.</div>`;
    }
  }
  loadDashboard();
  window.addEventListener("gymflex:changed", loadDashboard);
})();
