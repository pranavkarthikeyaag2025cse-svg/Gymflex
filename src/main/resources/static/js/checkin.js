(() => {
  const { api, escapeHtml, formatDate, formatTime, statusPill, toast } = window.GymFlex;
  const search = document.getElementById("checkin-search");
  const results = document.getElementById("search-results");
  const pass = document.getElementById("member-pass");
  let members = [];
  let selected = null;
  const now = new Date();
  document.getElementById("checkin-date").textContent = now.toLocaleDateString(undefined, { weekday: "short", day: "2-digit", month: "short", year: "numeric" }).toUpperCase();

  async function loadMembers() {
    try { members = await api("/api/members"); }
    catch (error) { toast(error.message, "error"); }
  }
  function searchMembers() {
    const term = search.value.trim().toLowerCase();
    if (!term) { results.innerHTML = `<div class="muted">Start typing to find a member.</div>`; return; }
    const matches = members.filter(member => `${member.id} ${member.name} ${member.phone}`.toLowerCase().includes(term)).slice(0, 8);
    results.innerHTML = matches.length ? matches.map(member => `<button class="search-result" data-id="${member.id}"><span class="table-avatar">${escapeHtml(member.name.split(/\s+/).map(part => part[0]).slice(0, 2).join("").toUpperCase())}</span><span><strong>${escapeHtml(member.name)}</strong><small>${escapeHtml(member.phone)} · ID #${member.id}</small></span><span class="result-arrow">→</span></button>`).join("") : `<div class="empty-state">No matching member found.</div>`;
  }
  async function selectMember(member) {
    selected = member;
    const state = member.status || "NO_MEMBERSHIP";
    const allowed = state !== "EXPIRED" && state !== "NO_MEMBERSHIP";
    pass.innerHTML = `<div class="pass-top"><span class="eyebrow">MEMBER PASS / #${member.id}</span>${statusPill(state)}</div><div class="pass-identity"><span class="pass-avatar">${escapeHtml(member.name.split(/\s+/).map(part => part[0]).slice(0, 2).join("").toUpperCase())}</span><div><h2>${escapeHtml(member.name)}</h2><p>${escapeHtml(member.phone)} <span>·</span> ${escapeHtml(member.email)}</p></div></div><div class="pass-facts"><div><span>MEMBERSHIP PLAN</span><strong>${escapeHtml(member.planName || "No membership")}</strong></div><div><span>EXPIRY DATE</span><strong>${formatDate(member.expiryDate)}</strong></div></div><button class="button ${allowed ? "button-primary" : "button-disabled"} checkin-submit" id="submit-checkin" ${allowed ? "" : "disabled"}>${allowed ? "Check in member" : state === "EXPIRED" ? "Membership expired" : "No active membership"}<span>→</span></button>`;
    const button = document.getElementById("submit-checkin");
    if (allowed) button.addEventListener("click", async () => {
      button.disabled = true;
      try { await api("/api/checkins", { method: "POST", body: JSON.stringify({ memberId: member.id }) }); toast("Check-in successful."); await loadToday(); }
      catch (error) { toast(error.message, "error"); button.disabled = false; }
    });
  }
  async function loadToday() {
    const today = new Date().toLocaleDateString("en-CA");
    const body = document.getElementById("today-table");
    try {
      const records = await api(`/api/checkins?date=${today}`);
      body.innerHTML = records.length ? records.map(record => `<tr><td><strong>${escapeHtml(record.memberName)}</strong></td><td>${formatDate(record.checkInDateTime.slice(0, 10))}</td><td>${formatTime(record.checkInDateTime)}</td><td>${statusPill("ACTIVE")}</td></tr>`).join("") : `<tr><td colspan="4" class="empty-cell">No arrivals logged today.</td></tr>`;
    } catch (error) { body.innerHTML = `<tr><td colspan="4" class="empty-cell">${escapeHtml(error.message)}</td></tr>`; }
  }
  search.addEventListener("input", searchMembers);
  results.addEventListener("click", event => {
    const result = event.target.closest("button[data-id]");
    if (result) selectMember(members.find(member => member.id === Number(result.dataset.id)));
  });
  loadMembers();
  loadToday();
})();
