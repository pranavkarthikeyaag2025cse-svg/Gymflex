(() => {
  const { api, escapeHtml, money, toast } = window.GymFlex;
  const grid = document.getElementById("plans-grid");
  const form = document.getElementById("plan-form");
  const editor = document.getElementById("plan-editor");
  let plans = [];

  function render() {
    grid.innerHTML = plans.length ? plans.map((plan, index) => `<article class="plan-card ${index === 1 ? "plan-card-featured" : ""}"><div class="plan-card-top"><span class="plan-number">PLAN / ${String(index + 1).padStart(2, "0")}</span><span class="plan-duration">${plan.durationInMonths} MONTH${plan.durationInMonths === 1 ? "" : "S"}</span></div><h2>${escapeHtml(plan.planName)}</h2><p>${escapeHtml(plan.description || "Gym access membership")}</p><div class="plan-price">${money(plan.price)}<small> / term</small></div><div class="plan-card-actions"><button class="small-action" data-action="edit" data-id="${plan.id}">Edit plan</button><button class="small-action action-danger" data-action="delete" data-id="${plan.id}">Delete</button></div></article>`).join("") : `<div class="empty-state">No plans yet. Create your first membership plan.</div>`;
  }
  async function load() {
    try { plans = await api("/api/plans"); render(); }
    catch (error) { grid.innerHTML = `<div class="empty-state">${escapeHtml(error.message)}</div>`; }
  }
  function resetForm() {
    form.reset();
    form.elements.id.value = "";
    document.getElementById("plan-form-title").textContent = "Create a plan";
    document.getElementById("plan-form-eyebrow").textContent = "ADD TO THE CATALOG";
    document.getElementById("plan-form-message").textContent = "";
  }
  document.getElementById("new-plan-button").addEventListener("click", () => { resetForm(); editor.scrollIntoView({ behavior: "smooth", block: "center" }); form.elements.planName.focus(); });
  document.getElementById("cancel-plan-edit").addEventListener("click", resetForm);
  grid.addEventListener("click", async event => {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const plan = plans.find(item => item.id === Number(button.dataset.id));
    if (!plan) return;
    if (button.dataset.action === "edit") {
      form.elements.id.value = plan.id;
      form.elements.planName.value = plan.planName;
      form.elements.durationInMonths.value = plan.durationInMonths;
      form.elements.price.value = plan.price;
      form.elements.description.value = plan.description || "";
      document.getElementById("plan-form-title").textContent = `Edit ${plan.planName}`;
      document.getElementById("plan-form-eyebrow").textContent = `PLAN / ${String(plan.id).padStart(2, "0")}`;
      editor.scrollIntoView({ behavior: "smooth", block: "center" });
      form.elements.planName.focus();
    } else if (button.dataset.action === "delete" && window.confirm(`Delete the ${plan.planName} plan? Plans already used by a member cannot be deleted.`)) {
      try { await api(`/api/plans/${plan.id}`, { method: "DELETE" }); toast("Plan deleted."); await load(); }
      catch (error) { toast(error.message, "error"); }
    }
  });
  form.addEventListener("submit", async event => {
    event.preventDefault();
    const id = form.elements.id.value;
    const body = { planName: form.elements.planName.value.trim(), durationInMonths: Number(form.elements.durationInMonths.value), price: Number(form.elements.price.value), description: form.elements.description.value.trim() || null };
    try {
      await api(id ? `/api/plans/${id}` : "/api/plans", { method: id ? "PUT" : "POST", body: JSON.stringify(body) });
      toast(id ? "Plan updated." : "Plan created."); resetForm(); await load();
    } catch (error) { toast(error.message, "error"); }
  });
  load();
})();
