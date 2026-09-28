(() => {
  const api = async (url, options = {}) => {
    const response = await fetch(url, {
      ...options,
      headers: { ...(options.body ? { "Content-Type": "application/json" } : {}), ...options.headers }
    });
    if (response.status === 204) return null;
    const data = await response.json().catch(() => null);
    if (!response.ok) throw new Error(data?.message || `Request failed (${response.status})`);
    return data;
  };

  const escapeHtml = (value = "") => String(value).replace(/[&<>"']/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[char]);
  const formatDate = value => value ? new Date(`${value}T00:00:00`).toLocaleDateString(undefined, { day: "2-digit", month: "short", year: "numeric" }) : "—";
  const formatDateTime = value => value ? new Date(value).toLocaleString(undefined, { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" }) : "—";
  const formatTime = value => value ? new Date(value).toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit" }) : "—";
  const money = value => new Intl.NumberFormat(undefined, { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(Number(value || 0));
  const statusPill = status => `<span class="status-pill status-${escapeHtml((status || "none").toLowerCase())}">${escapeHtml((status || "NO MEMBERSHIP").replaceAll("_", " "))}</span>`;

  function toast(message, type = "success") {
    const region = document.getElementById("toast-region");
    if (!region) return;
    const item = document.createElement("div");
    item.className = `toast toast-${type}`;
    item.textContent = message;
    region.append(item);
    window.setTimeout(() => item.remove(), 4200);
  }

  function addMonth(dateValue, months) {
    const [year, month, day] = dateValue.split("-").map(Number);
    const first = new Date(Date.UTC(year, month - 1 + months, 1));
    const lastDay = new Date(Date.UTC(first.getUTCFullYear(), first.getUTCMonth() + 1, 0)).getUTCDate();
    first.setUTCDate(Math.min(day, lastDay));
    return `${first.getUTCFullYear()}-${String(first.getUTCMonth() + 1).padStart(2, "0")}-${String(first.getUTCDate()).padStart(2, "0")}`;
  }

  async function openRenewDialog(membership) {
    const plans = await api("/api/plans");
    const existing = document.getElementById("renew-dialog");
    existing?.remove();
    const dialog = document.createElement("dialog");
    dialog.className = "app-dialog renew-dialog";
    dialog.id = "renew-dialog";
    dialog.innerHTML = `<form id="renew-form"><div class="dialog-heading"><div><p class="eyebrow">MEMBERSHIP RENEWAL</p><h2>Extend ${escapeHtml(membership.memberName)}</h2></div><button class="icon-button" type="button" data-close-dialog aria-label="Close">×</button></div><div class="renew-date"><span>Current expiry</span><strong>${formatDate(membership.expiryDate)}</strong></div><label>Renewal plan<select name="planId" required>${plans.map(plan => `<option value="${plan.id}" data-months="${plan.durationInMonths}">${escapeHtml(plan.planName)} · ${plan.durationInMonths} mo · ${money(plan.price)}</option>`).join("")}</select></label><div class="renew-preview"><span>New expiry date</span><strong id="renew-preview-date">—</strong></div><div class="dialog-actions"><button type="button" class="button button-quiet" data-close-dialog>Cancel</button><button class="button button-primary" type="submit">Confirm renewal <span>→</span></button></div></form>`;
    document.body.append(dialog);
    const form = dialog.querySelector("#renew-form");
    const updatePreview = () => {
      const selected = form.elements.planId.selectedOptions[0];
      dialog.querySelector("#renew-preview-date").textContent = formatDate(addMonth(membership.expiryDate, Number(selected.dataset.months)));
    };
    form.elements.planId.addEventListener("change", updatePreview);
    updatePreview();
    dialog.querySelectorAll("[data-close-dialog]").forEach(button => button.addEventListener("click", () => dialog.close()));
    form.addEventListener("submit", async event => {
      event.preventDefault();
      const button = form.querySelector('[type="submit"]');
      button.disabled = true;
      try {
        await api(`/api/memberships/${membership.id}/renew`, { method: "POST", body: JSON.stringify({ planId: Number(form.elements.planId.value) }) });
        dialog.close();
        toast("Membership renewed successfully.");
        window.dispatchEvent(new CustomEvent("gymflex:changed"));
      } catch (error) {
        toast(error.message, "error");
        button.disabled = false;
      }
    });
    dialog.addEventListener("close", () => dialog.remove(), { once: true });
    dialog.showModal();
  }

  document.querySelectorAll("[data-nav]").forEach(link => {
    if (link.dataset.nav === document.body.dataset.page) link.classList.add("active");
  });
  document.getElementById("menu-toggle")?.addEventListener("click", () => document.getElementById("sidebar")?.classList.toggle("sidebar-open"));
  document.querySelectorAll("[data-close-dialog]").forEach(button => button.addEventListener("click", () => button.closest("dialog")?.close()));

  window.GymFlex = { api, escapeHtml, formatDate, formatDateTime, formatTime, money, statusPill, toast, openRenewDialog };
})();
