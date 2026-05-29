// main.js
// Client-side interactions and API communications

document.addEventListener('DOMContentLoaded', async () => {
    // Ensure user is authenticated for app pages
    const isLoginPage = window.location.pathname.endsWith('index.html') || window.location.pathname.endsWith('/') || window.location.pathname === '';
    
    const user = await checkAuth(!isLoginPage, isLoginPage);
    if (!user && !isLoginPage) return; // Redirected

    // Page routers
    if (window.location.pathname.endsWith('dashboard.html')) {
        initDashboard();
    } else if (window.location.pathname.endsWith('animals.html')) {
        initAnimals();
    } else if (window.location.pathname.endsWith('symptoms.html')) {
        initSymptoms();
    } else if (window.location.pathname.endsWith('vaccinations.html')) {
        initVaccinations();
    }
});

// ==========================================
// 1. DASHBOARD CONTROLLER
// ==========================================
async function initDashboard() {
    try {
        const res = await fetch('/api/dashboard');
        if (!res.ok) throw new Error("Failed to fetch dashboard data");
        const data = await res.json();

        // Populate metrics
        document.getElementById('stat-total-animals').textContent = data.totalAnimals || 0;
        document.getElementById('stat-pending-vaccinations').textContent = data.pendingVaccinations || 0;
        document.getElementById('stat-risk-alerts').textContent = data.highRiskAlerts || 0;

        // Populate Recent Assessments Table
        const raTable = document.getElementById('recent-assessments-body');
        raTable.innerHTML = '';
        if (data.recentAssessments && data.recentAssessments.length > 0) {
            data.recentAssessments.forEach(item => {
                const tr = document.createElement('tr');
                const riskBadge = `<span class="badge ${item.riskLevel.toLowerCase()}">${item.riskLevel}</span>`;
                // Format Date
                const dateStr = item.assessmentDate.substring(0, 16);
                
                tr.innerHTML = `
                    <td><strong>${item.animalName}</strong></td>
                    <td>${riskBadge}</td>
                    <td>${item.possibleCondition}</td>
                    <td><small>${dateStr}</small></td>
                `;
                raTable.appendChild(tr);
            });
        } else {
            raTable.innerHTML = `<tr><td colspan="4" style="text-align:center;" class="text-secondary">No recent assessments logged.</td></tr>`;
        }

        // Populate Upcoming Vaccinations Table
        const uvTable = document.getElementById('upcoming-vaccinations-body');
        uvTable.innerHTML = '';
        if (data.upcomingVaccinations && data.upcomingVaccinations.length > 0) {
            data.upcomingVaccinations.forEach(item => {
                const tr = document.createElement('tr');
                const statusBadge = `<span class="badge ${item.status.toLowerCase()}">${item.status}</span>`;
                tr.innerHTML = `
                    <td><strong>${item.animalName}</strong></td>
                    <td>${item.vaccineName}</td>
                    <td>${item.scheduledDate}</td>
                    <td>${statusBadge}</td>
                `;
                uvTable.appendChild(tr);
            });
        } else {
            uvTable.innerHTML = `<tr><td colspan="4" style="text-align:center;" class="text-secondary">No pending vaccinations.</td></tr>`;
        }

    } catch (err) {
        showToast("Error loading dashboard metrics: " + err.message, "error");
    }
}

// ==========================================
// 2. ANIMAL REGISTRY CONTROLLER
// ==========================================
let allAnimals = [];

async function initAnimals() {
    const addBtn = document.getElementById('btn-add-animal');
    const modal = document.getElementById('animal-modal');
    const closeBtn = document.getElementById('modal-close-btn');
    const cancelBtn = document.getElementById('btn-cancel-modal');
    const form = document.getElementById('animal-form');

    // Load animals on startup
    await loadAnimalsList();

    if (addBtn && modal) {
        addBtn.addEventListener('click', () => {
            document.getElementById('modal-title-text').textContent = 'Register New Animal';
            form.reset();
            document.getElementById('animal-id-input').value = 0;
            modal.classList.add('active');
        });
    }

    const closeModal = () => modal.classList.remove('active');
    if (closeBtn) closeBtn.addEventListener('click', closeModal);
    if (cancelBtn) cancelBtn.addEventListener('click', closeModal);

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const id = parseInt(document.getElementById('animal-id-input').value) || 0;
            
            const payload = {
                animalId: id,
                name: document.getElementById('animal-name').value,
                species: document.getElementById('animal-species').value,
                breed: document.getElementById('animal-breed').value,
                age: parseInt(document.getElementById('animal-age').value) || null,
                weight: parseFloat(document.getElementById('animal-weight').value) || null,
                animalType: document.getElementById('animal-type').value,
                ownerName: document.getElementById('animal-owner').value,
                contactNumber: document.getElementById('animal-contact').value
            };

            try {
                const res = await fetch('/api/animals', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                const data = await res.json();

                if (data.success) {
                    showToast(data.message, 'success');
                    closeModal();
                    await loadAnimalsList();
                } else {
                    showToast(data.message, 'error');
                }
            } catch (err) {
                showToast('Connection error saving profile: ' + err.message, 'error');
            }
        });
    }
}

async function loadAnimalsList() {
    const grid = document.getElementById('animals-grid');
    if (!grid) return;

    try {
        const res = await fetch('/api/animals');
        allAnimals = await res.json();
        grid.innerHTML = '';

        if (allAnimals.length > 0) {
            allAnimals.forEach(animal => {
                const card = document.createElement('div');
                card.className = 'glass-panel animal-card';
                
                const typeIcon = animal.animalType === 'pet' ? 'fa-paw' : (animal.animalType === 'farm' ? 'fa-tractor' : 'fa-handshake-angle');
                
                card.innerHTML = `
                    <div class="animal-card-header">
                        <h3 class="animal-card-title">${animal.name}</h3>
                        <span class="badge" style="background: rgba(255,255,255,0.05); color:#fff; border: 1px solid var(--panel-border);">
                            <i class="fas ${typeIcon}"></i> ${animal.animalType}
                        </span>
                    </div>
                    <div class="animal-meta">
                        <div><strong>Species:</strong> ${animal.species} (${animal.breed || 'Unknown'})</div>
                        <div><strong>Age:</strong> ${animal.age != null ? animal.age + ' years' : 'N/A'}</div>
                        <div><strong>Weight:</strong> ${animal.weight != null ? animal.weight + ' kg' : 'N/A'}</div>
                        <div><strong>Owner:</strong> ${animal.ownerName || 'Unknown'}</div>
                        <div><strong>Contact:</strong> ${animal.contactNumber || 'N/A'}</div>
                    </div>
                    <div class="animal-card-actions">
                        <button class="btn-secondary" onclick="editAnimal(${animal.animalId})" style="padding: 6px 12px; font-size:13px;">
                            <i class="fas fa-edit"></i> Edit
                        </button>
                        <button class="btn-danger" onclick="deleteAnimal(${animal.animalId})" style="padding: 6px 12px; font-size:13px;">
                            <i class="fas fa-trash-can"></i> Delete
                        </button>
                    </div>
                `;
                grid.appendChild(card);
            });
        } else {
            grid.innerHTML = `<div style="grid-column: 1/-1; text-align:center; padding: 3rem;" class="glass-panel text-secondary">
                <i class="fas fa-paw" style="font-size: 40px; margin-bottom: 1rem; color: var(--text-muted);"></i>
                <p>No animals registered yet. Click "Register Animal" to start profiling.</p>
            </div>`;
        }
    } catch (err) {
        showToast("Error loading animal registry: " + err.message, "error");
    }
}

function editAnimal(id) {
    const animal = allAnimals.find(a => a.animalId === id);
    if (!animal) return;

    document.getElementById('animal-id-input').value = animal.animalId;
    document.getElementById('animal-name').value = animal.name;
    document.getElementById('animal-species').value = animal.species;
    document.getElementById('animal-breed').value = animal.breed || '';
    document.getElementById('animal-age').value = animal.age != null ? animal.age : '';
    document.getElementById('animal-weight').value = animal.weight != null ? animal.weight : '';
    document.getElementById('animal-type').value = animal.animalType;
    document.getElementById('animal-owner').value = animal.ownerName || '';
    document.getElementById('animal-contact').value = animal.contactNumber || '';

    document.getElementById('modal-title-text').textContent = 'Modify Animal Profile';
    document.getElementById('animal-modal').classList.add('active');
}

async function deleteAnimal(id) {
    if (!confirm("Are you absolutely sure you want to delete this animal profile? This will also remove all associated symptom logs and vaccinations!")) return;
    try {
        const res = await fetch(`/api/animals?id=${id}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            await loadAnimalsList();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast("Connection error deleting profile: " + err.message, 'error');
    }
}

// ==========================================
// 3. SYMPTOM DIAGNOSIS CONTROLLER
// ==========================================
async function initSymptoms() {
    const select = document.getElementById('symptom-animal-select');
    const form = document.getElementById('symptom-form');
    const resultPanel = document.getElementById('assessment-result-panel');

    // Load animal options
    try {
        const res = await fetch('/api/animals');
        const animals = await res.json();
        select.innerHTML = '<option value="">-- Choose Animal --</option>';
        animals.forEach(a => {
            select.innerHTML += `<option value="${a.animalId}">${a.name} (${a.species})</option>`;
        });
    } catch (err) {
        showToast("Error loading animals dropdown: " + err.message, "error");
    }

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const animalId = select.value;
            if (!animalId) {
                showToast("Please choose an animal from the list.", "warning");
                return;
            }

            const payload = {
                animalId: parseInt(animalId),
                reducedAppetite: document.getElementById('sym-appetite').checked,
                fever: document.getElementById('sym-fever').checked,
                vomiting: document.getElementById('sym-vomiting').checked,
                lowActivity: document.getElementById('sym-activity').checked,
                limping: document.getElementById('sym-limping').checked,
                otherSymptoms: document.getElementById('sym-other').value
            };

            try {
                const res = await fetch('/api/symptoms', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                const data = await res.json();

                if (data.success) {
                    showToast(data.message, 'success');
                    
                    // Render Health Assessment output
                    const r = data.assessment;
                    const cardClass = r.riskLevel.toLowerCase();
                    
                    resultPanel.innerHTML = `
                        <div class="assessment-card ${cardClass}" style="margin-top:0;">
                            <h3 style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
                                <span>Health Assessment Result</span>
                                <span class="badge ${cardClass}">${r.riskLevel} Risk</span>
                            </h3>
                            <div style="margin-bottom:10px;">
                                <strong>Probable Diagnosis:</strong> ${r.possibleCondition}
                            </div>
                            <div style="padding:12px; background:rgba(0,0,0,0.2); border-radius:8px; line-height:1.5;">
                                <strong>Instructions &amp; Guidance:</strong><br>
                                <span style="font-size:14px;">${r.recommendedAction}</span>
                            </div>
                        </div>
                    `;
                    resultPanel.style.display = 'block';
                    form.reset();
                } else {
                    showToast(data.message, 'error');
                }
            } catch (err) {
                showToast("Diagnosis API error: " + err.message, "error");
            }
        });
    }
}

// ==========================================
// 4. VACCINATIONS TRACKER CONTROLLER
// ==========================================
async function initVaccinations() {
    const filterSelect = document.getElementById('filter-animal-select');
    const formSelect = document.getElementById('vaccination-animal-select');
    const form = document.getElementById('vaccination-form');

    // Populate dropdowns
    try {
        const res = await fetch('/api/animals');
        const animals = await res.json();
        
        filterSelect.innerHTML = '<option value="">-- Choose Animal to View Schedule --</option>';
        formSelect.innerHTML = '<option value="">-- Choose Animal --</option>';
        
        animals.forEach(a => {
            filterSelect.innerHTML += `<option value="${a.animalId}">${a.name} (${a.species})</option>`;
            formSelect.innerHTML += `<option value="${a.animalId}">${a.name} (${a.species})</option>`;
        });
    } catch (err) {
        showToast("Error loading animal dropdowns: " + err.message, "error");
    }

    // Load table when filter changes
    filterSelect.addEventListener('change', () => {
        loadVaccinations(filterSelect.value);
    });

    // Schedule form submit
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const animalId = formSelect.value;
            if (!animalId) {
                showToast("Please choose an animal.", "warning");
                return;
            }

            const payload = {
                animalId: parseInt(animalId),
                vaccineName: document.getElementById('vac-name').value,
                scheduledDate: document.getElementById('vac-date').value,
                notes: document.getElementById('vac-notes').value,
                status: 'Pending'
            };

            try {
                const res = await fetch('/api/vaccinations', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                const data = await res.json();

                if (data.success) {
                    showToast(data.message, 'success');
                    form.reset();
                    // If filter matches scheduled animal, reload
                    if (filterSelect.value === animalId) {
                        loadVaccinations(animalId);
                    } else {
                        filterSelect.value = animalId;
                        loadVaccinations(animalId);
                    }
                } else {
                    showToast(data.message, 'error');
                }
            } catch (err) {
                showToast("Schedule failed: " + err.message, "error");
            }
        });
    }
}

async function loadVaccinations(animalId) {
    const tbody = document.getElementById('vaccinations-body');
    if (!tbody) return;

    if (!animalId) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;" class="text-secondary">Please select an animal above to inspect schedules.</td></tr>`;
        return;
    }

    try {
        const res = await fetch(`/api/vaccinations?animalId=${animalId}`);
        const list = await res.json();
        tbody.innerHTML = '';

        if (list.length > 0) {
            list.forEach(v => {
                const tr = document.createElement('tr');
                const statusBadge = `<span class="badge ${v.status.toLowerCase()}">${v.status}</span>`;
                const adminDateStr = v.administeredDate ? v.administeredDate : '—';
                
                let actionBtn = '';
                if (v.status !== 'Completed') {
                    actionBtn = `
                        <button class="btn-primary" onclick="markVaccineComplete(${v.vaccinationId}, ${v.animalId})" style="padding:4px 8px; font-size:12px; font-weight:500; border-radius:6px; box-shadow:none;">
                            <i class="fas fa-check"></i> Complete
                        </button>
                    `;
                }
                
                tr.innerHTML = `
                    <td><strong>${v.vaccineName}</strong></td>
                    <td>${v.scheduledDate}</td>
                    <td>${adminDateStr}</td>
                    <td>${statusBadge}</td>
                    <td>
                        <div style="display:flex; gap:6px; align-items:center;">
                            ${actionBtn}
                            <button class="btn-danger" onclick="deleteVaccine(${v.vaccinationId}, ${v.animalId})" style="padding:4px 8px; font-size:12px; font-weight:500; border-radius:6px;">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;" class="text-secondary">No vaccinations logged for this animal.</td></tr>`;
        }
    } catch (err) {
        showToast("Error loading schedule: " + err.message, "error");
    }
}

async function markVaccineComplete(vacId, animalId) {
    const today = new Date().toISOString().split('T')[0];
    try {
        const res = await fetch('/api/vaccinations', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                vaccinationId: vacId,
                administeredDate: today,
                completeOnly: true
            })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            loadVaccinations(animalId);
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast("Error completing vaccine: " + err.message, "error");
    }
}

async function deleteVaccine(vacId, animalId) {
    if (!confirm("Remove this vaccination schedule?")) return;
    try {
        const res = await fetch(`/api/vaccinations?id=${vacId}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            loadVaccinations(animalId);
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast("Connection error: " + err.message, "error");
    }
}
