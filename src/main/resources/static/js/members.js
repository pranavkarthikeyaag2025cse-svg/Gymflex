(() => {
  const { api, escapeHtml, formatDate, statusPill, money, toast } = window.GymFlex;
  const registrationForm = document.getElementById("registration-form");
  const directory = document.getElementById("members-table");
  const planOptions = document.getElementById("plan-options");
  let plans = [];
  let members = [];
  let memberships = [];

  async function loadPlans() {
    plans = await api("/api/plans");
    if (!planOptions) return;
    planOptions.innerHTML = plans.map((plan, index) => `<label class="plan-option ${index === 0 ? "selected" : ""}"><input type="radio" name="plan-choice" value="${plan.id}" ${index === 0 ? "checked" : ""}><span class="plan-option-top"><strong>${escapeHtml(plan.planName)}</strong><span>${plan.durationInMonths} MO</span></span><span class="plan-option-price">${money(plan.price)}<small> / plan</small></span><span class="plan-option-description">${escapeHtml(plan.description || "Gym access membership")}</span></label>`).join("");
    document.getElementById("selected-plan").value = plans[0]?.id || "";
    planOptions.querySelectorAll("input").forEach(input => input.addEventListener("change", () => {
      document.getElementById("selected-plan").value = input.value;
      planOptions.querySelectorAll(".plan-option").forEach(card => card.classList.toggle("selected", card.contains(input)));
    }));
  }

  if (registrationForm) {
    const startDate = document.getElementById("start-date");
    startDate.value = new Date().toLocaleDateString("en-CA");
    loadPlans().catch(error => { toast(error.message, "error"); planOptions.innerHTML = `<div class="empty-state">Could not load membership plans.</div>`; });
    registrationForm.addEventListener("submit", async event => {
      event.preventDefault();
      if (!registrationForm.reportValidity()) return;
      const data = Object.fromEntries(new FormData(registrationForm));
      data.planId = Number(data.planId);
      try {
        await api("/api/memberships/register", { method: "POST", body: JSON.stringify(data) });
        toast("Member and membership created.");
        window.location.assign("/members.html");
      } catch (error) { toast(error.message, "error"); }
    });
  }

  if (!directory) return;
  const search = document.getElementById("member-search");
  const memberDialog = document.getElementById("member-dialog");
  const viewDialog = document.getElementById("view-dialog");
  const membershipFields = document.getElementById("edit-membership-fields");
  const membershipHint = document.getElementById("edit-membership-hint");
  const membershipInputs = [document.getElementById("edit-plan-id"), document.getElementById("edit-start-date"), document.getElementById("edit-expiry-date")];
  document.getElementById("edit-start-date").addEventListener("change", event => {
    document.getElementById("edit-expiry-date").min = event.target.value;
  });

  function renderDirectory() {
    const term = search.value.trim().toLowerCase();
    const filtered = members.filter(member => `${member.name} ${member.phone} ${member.email} ${member.id}`.toLowerCase().includes(term));
    document.getElementById("member-count").textContent = `${filtered.length} member${filtered.length === 1 ? "" : "s"}`;
    directory.innerHTML = filtered.length ? filtered.map(member => {
      const membership = memberships.find(item => item.memberId === member.id);
      return `<tr><td><span class="table-person"><span class="table-avatar">${escapeHtml(member.name.split(/\s+/).map(part => part[0]).slice(0, 2).join("").toUpperCase())}</span><span><strong>${escapeHtml(member.name)}</strong><small>ID #${member.id}</small></span></span></td><td><span class="contact-stack">${escapeHtml(member.phone)}<small>${escapeHtml(member.email)}</small></span></td><td>${escapeHtml(member.planName || "—")}</td><td>${formatDate(member.startDate)}</td><td>${formatDate(member.expiryDate)}</td><td>${statusPill(member.status)}</td><td class="align-right"><div class="row-actions"><button class="small-action" data-action="view" data-id="${member.id}" title="View member">View</button><button class="small-action" data-action="edit" data-id="${member.id}" title="Edit member">Edit</button><button class="small-action action-accent" data-action="renew" data-id="${member.id}" ${membership ? "" : "disabled"}>Renew</button><button class="small-action" data-action="checkin" data-id="${member.id}">Check in</button><button class="small-action action-danger" data-action="delete" data-id="${member.id}" aria-label="Delete ${escapeHtml(member.name)}">×</button></div></td></tr>`;
    }).join("") : `<tr><td colspan="7" class="empty-cell">No members match this search.</td></tr>`;
  }

  async function loadMembers() {
    try {
      [members, memberships, plans] = await Promise.all([api("/api/members"), api("/api/memberships"), api("/api/plans")]);
      renderDirectory();
    } catch (error) { directory.innerHTML = `<tr><td colspan="7" class="empty-cell">${escapeHtml(error.message)}</td></tr>`; }
  }

  search.addEventListener("input", renderDirectory);
  directory.addEventListener("click", async event => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const member = members.find(item => item.id === Number(button.dataset.id));
    if (!member) return;
    const action = button.dataset.action;
    if (action === "view") {
      document.getElementById("member-details").innerHTML = `<div><span>Name</span><strong>${escapeHtml(member.name)}</strong></div><div><span>Member ID</span><strong>#${member.id}</strong></div><div><span>Email</span><strong>${escapeHtml(member.email)}</strong></div><div><span>Phone</span><strong>${escapeHtml(member.phone)}</strong></div><div><span>Plan</span><strong>${escapeHtml(member.planName || "No membership")}</strong></div><div><span>Expiry date</span><strong>${formatDate(member.expiryDate)}</strong></div>`;
      viewDialog.showModal();
    } else if (action === "edit") {
      const membership = memberships.find(item => item.memberId === member.id);
      document.getElementById("edit-member-id").value = member.id;
      document.getElementById("edit-name").value = member.name;
      document.getElementById("edit-email").value = member.email;
      document.getElementById("edit-phone").value = member.phone;
      document.getElementById("edit-address").value = member.address;
      membershipFields.classList.toggle("is-hidden", !membership);
      membershipHint.classList.toggle("is-hidden", Boolean(membership));
      membershipInputs.forEach(input => { input.disabled = !membership; });
      if (membership) {
        document.getElementById("edit-plan-id").innerHTML = plans.map(plan => `<option value="${plan.id}">${escapeHtml(plan.planName)}</option>`).join("");
        document.getElementById("edit-plan-id").value = membership.planId;
        document.getElementById("edit-start-date").value = membership.startDate;
        document.getElementById("edit-expiry-date").value = membership.expiryDate;
        document.getElementById("edit-expiry-date").min = membership.startDate;
      }
      memberDialog.showModal();
    } else if (action === "delete") {
      if (!window.confirm(`Delete ${member.name} and their membership and attendance history?`)) return;
      try { await api(`/api/members/${member.id}`, { method: "DELETE" }); toast("Member deleted."); await loadMembers(); }
      catch (error) { toast(error.message, "error"); }
    } else if (action === "renew") {
      const membership = memberships.find(item => item.memberId === member.id);
      if (membership) window.GymFlex.openRenewDialog(membership).catch(error => toast(error.message, "error"));
    } else if (action === "checkin") {
      try { await api("/api/checkins", { method: "POST", body: JSON.stringify({ memberId: member.id }) }); toast("Check-in successful."); }
      catch (error) { toast(error.message, "error"); }
    }
  });

  document.getElementById("member-edit-form")?.addEventListener("submit", async event => {
    event.preventDefault();
    const id = document.getElementById("edit-member-id").value;
    const profile = { name: document.getElementById("edit-name").value, email: document.getElementById("edit-email").value, phone: document.getElementById("edit-phone").value, address: document.getElementById("edit-address").value };
    const membership = { planId: Number(document.getElementById("edit-plan-id").value), startDate: document.getElementById("edit-start-date").value, expiryDate: document.getElementById("edit-expiry-date").value };
    const hasMembership = !membershipFields.classList.contains("is-hidden");
    const url = hasMembership ? `/api/members/${id}/details` : `/api/members/${id}`;
    const body = hasMembership ? { member: profile, membership } : profile;
    try { await api(url, { method: "PUT", body: JSON.stringify(body) }); memberDialog.close(); toast("Member and membership updated."); await loadMembers(); }
    catch (error) { toast(error.message, "error"); }
  });
  loadMembers();
  window.addEventListener("gymflex:changed", loadMembers);
})();
