(() => {
  const { api, escapeHtml, formatDate, formatTime, toast } = window.GymFlex;
  const memberSelect = document.getElementById("attendance-member");
  const dateFilter = document.getElementById("attendance-date");
  const table = document.getElementById("attendance-table");
  async function loadMembers() {
    try {
      const members = await api("/api/members");
      memberSelect.innerHTML = `<option value="">Choose a member</option>${members.map(member => `<option value="${member.id}">${escapeHtml(member.name)} · #${member.id}</option>`).join("")}`;
    } catch (error) { toast(error.message, "error"); }
  }
  async function loadAll() {
    try {
      const query = dateFilter.value ? `?date=${dateFilter.value}` : "";
      const records = await api(`/api/checkins${query}`);
      document.getElementById("attendance-count").textContent = `${records.length} check-in record${records.length === 1 ? "" : "s"}`;
      table.innerHTML = records.length ? records.map(record => `<tr><td><strong>${escapeHtml(record.memberName)}</strong></td><td>#${record.memberId}</td><td>${formatDate(record.checkInDateTime.slice(0, 10))}</td><td>${formatTime(record.checkInDateTime)}</td></tr>`).join("") : `<tr><td colspan="4" class="empty-cell">No attendance records for this date.</td></tr>`;
    } catch (error) { table.innerHTML = `<tr><td colspan="4" class="empty-cell">${escapeHtml(error.message)}</td></tr>`; }
  }
  async function loadMonthly() {
    const id = memberSelect.value;
    const display = document.getElementById("monthly-summary");
    if (!id) { display.innerHTML = `<span class="summary-number">—</span><span class="muted">Select a member to view this month's attendance</span>`; return; }
    try {
      const summary = await api(`/api/checkins/monthly/${id}`);
      display.innerHTML = `<span class="summary-number">${summary.totalAttendance}</span><span><strong>Attendance this month</strong><small>${escapeHtml(summary.memberName)} · ${summary.month}</small></span>`;
    } catch (error) { toast(error.message, "error"); }
  }
  memberSelect.addEventListener("change", loadMonthly);
  dateFilter.addEventListener("change", loadAll);
  loadMembers();
  loadAll();
})();
