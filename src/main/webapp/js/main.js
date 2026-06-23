// main.js
// Client-side interactions and API communications

// Helper functions for safe date formatting
function formatDateTime(dateVal) {
    if (!dateVal) return '—';
    if (typeof dateVal === 'string') {
        return dateVal.substring(0, 16);
    }
    try {
        if (typeof dateVal === 'object' && dateVal.time) {
            const d = new Date(dateVal.time);
            return isNaN(d.getTime()) ? '—' : d.toISOString().replace('T', ' ').substring(0, 16);
        }
        const d = new Date(dateVal);
        return isNaN(d.getTime()) ? '—' : d.toISOString().replace('T', ' ').substring(0, 16);
    } catch (e) {
        console.error("Error parsing date:", dateVal, e);
        return '—';
    }
}

function formatDateOnly(dateVal) {
    if (!dateVal) return '—';
    if (typeof dateVal === 'string') {
        return dateVal.substring(0, 10);
    }
    try {
        if (typeof dateVal === 'object' && dateVal.time) {
            const d = new Date(dateVal.time);
            return isNaN(d.getTime()) ? '—' : d.toISOString().substring(0, 10);
        }
        const d = new Date(dateVal);
        return isNaN(d.getTime()) ? '—' : d.toISOString().substring(0, 10);
    } catch (e) {
        return '—';
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    // Ensure user is authenticated for app pages
    const path = window.location.pathname;
    const pathSegments = path.split('/');
    let pageName = pathSegments[pathSegments.length - 1];
    if (!pageName || pageName === 'smart-animal-health') {
        pageName = 'index.html';
    }

    const loginPages = ['index.html', 'user_login.html', 'admin_login.html'];
    const isLoginPage = loginPages.includes(pageName);
    
    const user = await checkAuth(!isLoginPage, isLoginPage);
    if (!user && !isLoginPage) return; // Redirected
    window.currentUser = user;

    // Enforce role-based page access for authenticated users
    if (user && !isLoginPage) {
        if (user.role === 'Admin') {
            const allowedPages = ['caretakers.html', 'assessments.html', 'audit_logs.html', 'medical_history.html', 'approvals.html', 'appointments.html', 'appointment.html'];
            const currentPageAllowed = allowedPages.includes(pageName);
            if (!currentPageAllowed) {
                window.location.href = 'caretakers.html';
                return;
            }
            updateApprovalsBadge();
        } else if (user.role === 'Doctor') {
            const allowedPages = ['doctor_dashboard.html', 'medical_history.html', 'animals.html', 'symptoms.html', 'vaccinations.html', 'appointments.html', 'appointment.html'];
            const currentPageAllowed = allowedPages.includes(pageName);
            if (!currentPageAllowed) {
                window.location.href = 'doctor_dashboard.html';
                return;
            }
        } else if (user.role === 'User') {
            const allowedPages = ['dashboard.html', 'animals.html', 'symptoms.html', 'vaccinations.html', 'medical_history.html', 'appointments.html', 'appointment.html'];
            const currentPageAllowed = allowedPages.includes(pageName);
            if (!currentPageAllowed) {
                window.location.href = 'dashboard.html';
                return;
            }
        }
    }

    // Reveal app container now that user is verified and authorized to view this page
    const appContainer = document.querySelector('.app-container');
    if (appContainer) {
        appContainer.style.opacity = '1';
    }

    // Page routers
    if (pageName === 'dashboard.html') {
        initDashboard();
    } else if (pageName === 'animals.html') {
        initAnimals();
    } else if (pageName === 'symptoms.html') {
        initSymptoms();
    } else if (pageName === 'vaccinations.html') {
        initVaccinations();
    } else if (pageName === 'caretakers.html') {
        initCaretakers();
    } else if (pageName === 'assessments.html') {
        initAssessments();
    } else if (pageName === 'audit_logs.html') {
        initAuditLogs();
    } else if (pageName === 'doctor_dashboard.html') {
        initDoctorDashboard();
    } else if (pageName === 'medical_history.html') {
        initMedicalHistory();
    } else if (pageName === 'approvals.html') {
        initApprovals();
    } else if (pageName === 'appointments.html' || pageName === 'appointment.html') {
        initAppointments();
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
                const dateStr = formatDateTime(item.assessmentDate);
                
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
    const searchInput = document.getElementById('search-animals');

    // Load animals on startup
    await loadAnimalsList();

    if (searchInput) {
        searchInput.addEventListener('input', () => {
            filterAnimals(searchInput.value);
        });
    }

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
        renderAnimalsList(allAnimals);
    } catch (err) {
        showToast("Error loading animal registry: " + err.message, "error");
    }
}

function renderAnimalsList(list) {
    const grid = document.getElementById('animals-grid');
    if (!grid) return;
    grid.innerHTML = '';

    if (list.length > 0) {
        list.forEach(animal => {
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
            <p>No animal profiles found matching the query.</p>
        </div>`;
    }
}

function filterAnimals(query) {
    const lowerQuery = query.toLowerCase().trim();
    const filtered = allAnimals.filter(animal => {
        return (
            animal.name.toLowerCase().includes(lowerQuery) ||
            animal.species.toLowerCase().includes(lowerQuery) ||
            (animal.breed && animal.breed.toLowerCase().includes(lowerQuery)) ||
            (animal.ownerName && animal.ownerName.toLowerCase().includes(lowerQuery)) ||
            animal.animalType.toLowerCase().includes(lowerQuery)
        );
    });
    renderAnimalsList(filtered);
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
    const filterDocSelect = document.getElementById('filter-doctor-select');
    const formDocSelect = document.getElementById('vaccination-doctor-select');
    const form = document.getElementById('vaccination-form');
    const isAdmin = document.getElementById('sidebar-user-role')?.textContent === 'Admin';

    // Populate dropdowns
    try {
        const res = await fetch('/api/animals');
        const animals = await res.json();
        
        if (isAdmin) {
            filterSelect.innerHTML = '<option value="all">All Animals (Clinic Overwatch)</option>';
        } else {
            filterSelect.innerHTML = '<option value="">-- Choose Animal to View Schedule --</option>';
        }
        formSelect.innerHTML = '<option value="">-- Choose Animal --</option>';
        
        animals.forEach(a => {
            filterSelect.innerHTML += `<option value="${a.animalId}">${a.name} (${a.species})</option>`;
            formSelect.innerHTML += `<option value="${a.animalId}">${a.name} (${a.species})</option>`;
        });
    } catch (err) {
        showToast("Error loading animal dropdowns: " + err.message, "error");
    }

    // Populate doctor dropdowns
    try {
        const docRes = await fetch('/api/appointments?action=getDoctors');
        if (docRes.ok) {
            const doctors = await docRes.json();
            if (formDocSelect) {
                formDocSelect.innerHTML = '<option value="">-- No Doctor Assigned --</option>';
                doctors.forEach(d => {
                    formDocSelect.innerHTML += `<option value="${d.userId}">Dr. ${d.fullName || d.username}</option>`;
                });
            }
            if (filterDocSelect) {
                filterDocSelect.innerHTML = '<option value="">Filter by Doctor (All)...</option>';
                doctors.forEach(d => {
                    filterDocSelect.innerHTML += `<option value="${d.userId}">Dr. ${d.fullName || d.username}</option>`;
                });
            }
        }
    } catch (err) {
        console.error("Error loading doctor dropdowns:", err);
    }

    if (isAdmin) {
        filterSelect.value = 'all';
        await loadVaccinations('all', '');
    }

    // Load table when filter changes
    filterSelect.addEventListener('change', () => {
        loadVaccinations(filterSelect.value, filterDocSelect ? filterDocSelect.value : '');
    });
    if (filterDocSelect) {
        filterDocSelect.addEventListener('change', () => {
            loadVaccinations(filterSelect.value, filterDocSelect.value);
        });
    }

    // Schedule form submit
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const animalId = formSelect.value;
            if (!animalId) {
                showToast("Please choose an animal.", "warning");
                return;
            }

            const doctorId = formDocSelect ? formDocSelect.value : '';
            const payload = {
                animalId: parseInt(animalId),
                vaccineName: document.getElementById('vac-name').value,
                scheduledDate: document.getElementById('vac-date').value,
                notes: document.getElementById('vac-notes').value,
                status: 'Pending'
            };
            if (doctorId) {
                payload.doctorId = parseInt(doctorId);
            }

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
                    // If filter matches scheduled animal or is all, reload
                    const curDoctorId = filterDocSelect ? filterDocSelect.value : '';
                    if (filterSelect.value === animalId || filterSelect.value === 'all') {
                        loadVaccinations(filterSelect.value, curDoctorId);
                    } else {
                        filterSelect.value = animalId;
                        if (filterDocSelect) filterDocSelect.value = '';
                        loadVaccinations(animalId, '');
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

async function loadVaccinations(animalId, doctorId = '') {
    const tbody = document.getElementById('vaccinations-body');
    if (!tbody) return;

    if ((!animalId || animalId === '') && (!doctorId || doctorId === '')) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;" class="text-secondary">Please select an animal or doctor above to inspect schedules.</td></tr>`;
        return;
    }

    try {
        let url = '/api/vaccinations?';
        const params = [];
        if (animalId && animalId !== 'all') {
            params.push(`animalId=${animalId}`);
        }
        if (doctorId) {
            params.push(`doctorId=${doctorId}`);
        }
        url += params.join('&');

        if (params.length === 0) {
            url = '/api/vaccinations';
        }

        const res = await fetch(url);
        const list = await res.json();
        tbody.innerHTML = '';

        const thead = tbody.closest('table').querySelector('thead tr');
        const showAnimalCols = (!animalId || animalId === 'all' || animalId === '');
        const totalCols = showAnimalCols ? 8 : 6;

        if (showAnimalCols) {
            thead.innerHTML = `
                <th>Animal</th>
                <th>Caretaker</th>
                <th>Vaccine</th>
                <th>Doctor</th>
                <th>Scheduled Date</th>
                <th>Administered</th>
                <th>Status</th>
                <th>Action</th>
            `;
        } else {
            thead.innerHTML = `
                <th>Vaccine</th>
                <th>Doctor</th>
                <th>Scheduled Date</th>
                <th>Administered</th>
                <th>Status</th>
                <th>Action</th>
            `;
        }

        if (list.length > 0) {
            list.forEach(v => {
                const tr = document.createElement('tr');
                const statusBadge = `<span class="badge ${v.status.toLowerCase()}">${v.status}</span>`;
                const adminDateStr = v.administeredDate ? v.administeredDate : '—';
                const docStr = v.doctorName ? `Dr. ${v.doctorName}` : '—';
                
                let actionBtn = '';
                if (v.status !== 'Completed') {
                    actionBtn = `
                        <button class="btn-primary" onclick="markVaccineComplete(${v.vaccinationId}, '${animalId || ''}', '${doctorId}')" style="padding:4px 8px; font-size:12px; font-weight:500; border-radius:6px; box-shadow:none;">
                            <i class="fas fa-check"></i> Complete
                        </button>
                    `;
                }
                
                let animalCells = '';
                if (showAnimalCols) {
                    animalCells = `
                        <td><strong>${v.animalName || '—'}</strong></td>
                        <td>${v.ownerName || '—'}</td>
                    `;
                }

                tr.innerHTML = `
                    ${animalCells}
                    <td><strong>${v.vaccineName}</strong></td>
                    <td>${docStr}</td>
                    <td>${v.scheduledDate}</td>
                    <td>${adminDateStr}</td>
                    <td>${statusBadge}</td>
                    <td>
                        <div style="display:flex; gap:6px; align-items:center;">
                            ${actionBtn}
                            <button class="btn-danger" onclick="deleteVaccine(${v.vaccinationId}, '${animalId || ''}', '${doctorId}')" style="padding:4px 8px; font-size:12px; font-weight:500; border-radius:6px;">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = `<tr><td colspan="${totalCols}" style="text-align:center;" class="text-secondary">No vaccinations logged.</td></tr>`;
        }
    } catch (err) {
        showToast("Error loading schedule: " + err.message, "error");
    }
}

async function markVaccineComplete(vacId, animalId, doctorId) {
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
            loadVaccinations(animalId, doctorId);
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast("Error completing vaccine: " + err.message, "error");
    }
}

async function deleteVaccine(vacId, animalId, doctorId) {
    if (!confirm("Remove this vaccination schedule?")) return;
    try {
        const res = await fetch(`/api/vaccinations?id=${vacId}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            loadVaccinations(animalId, doctorId);
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast("Connection error: " + err.message, "error");
    }
}

// ==========================================
// 5. USER DIRECTORY & ROLE MANAGEMENT
// ==========================================
let allCaretakers = [];

async function initCaretakers() {
    await loadCaretakersList();
}

async function loadCaretakersList() {
    const tbody = document.getElementById('caretakers-table-body');
    if (!tbody) return;

    try {
        const res = await fetch('/api/users');
        allCaretakers = await res.json();
        tbody.innerHTML = '';

        // Filter out unapproved doctors from the caretakers active directory
        const activeUsers = allCaretakers.filter(user => !(user.role === 'Doctor' && !user.isApproved));

        // Check for pending doctor approvals and show alert banner
        const pendingDoctors = allCaretakers.filter(user => user.role === 'Doctor' && !user.isApproved);
        const bannerEl = document.getElementById('pending-approvals-banner');
        if (bannerEl) {
            if (pendingDoctors.length > 0) {
                bannerEl.innerHTML = `
                    <div class="glass-panel alert-card warning" style="margin-top: 1rem; margin-bottom: 0.5rem; border: 1px solid rgba(244, 63, 94, 0.3); background: rgba(244, 63, 94, 0.05); display: flex; align-items: center; justify-content: space-between; padding: 1.2rem; gap: 1rem; border-radius: 12px;">
                        <div style="display: flex; align-items: center; gap: 12px;">
                            <div class="alert-icon-wrapper" style="background: rgba(244, 63, 94, 0.1); color: var(--accent-rose); width: 42px; height: 42px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 20px;">
                                <i class="fas fa-user-clock"></i>
                            </div>
                            <div style="text-align: left;">
                                <h3 style="font-size: 15px; font-weight: 600; color: #fff; margin: 0;">Pending Doctor Registrations</h3>
                                <p class="text-secondary" style="font-size: 13px; margin: 4px 0 0 0;">There are ${pendingDoctors.length} veterinarian registration request(s) awaiting your review.</p>
                            </div>
                        </div>
                        <a href="approvals.html" class="btn-primary" style="padding: 8px 16px; font-size: 13px; background: linear-gradient(135deg, var(--accent-rose) 0%, var(--accent-blue) 100%); box-shadow: 0 4px 10px rgba(244, 63, 94, 0.2); text-decoration: none; display: inline-flex; align-items: center; gap: 8px;">
                            <i class="fas fa-user-check"></i> Review Requests
                        </a>
                    </div>
                `;
            } else {
                bannerEl.innerHTML = '';
            }
        }

        // Render Active Users
        if (activeUsers.length > 0) {
            activeUsers.forEach(caretaker => {
                const tr = document.createElement('tr');
                
                let roleBadge = '';
                if (caretaker.role === 'Admin') {
                    roleBadge = '<span class="badge high">Admin</span>';
                } else if (caretaker.role === 'Doctor') {
                    roleBadge = '<span class="badge doctor">Doctor</span>';
                } else {
                    roleBadge = '<span class="badge low">User</span>';
                }
                
                const isSelf = window.currentUser && window.currentUser.userId === caretaker.userId;
                
                let roleSelect = '';
                if (isSelf) {
                    roleSelect = `<select disabled style="padding: 4px 8px; font-size: 12px; border-radius: 6px; background: rgba(255,255,255,0.05); border: 1px solid var(--panel-border); color: var(--text-muted); cursor: not-allowed; outline: none;">
                        <option value="Admin" selected>Admin (You)</option>
                    </select>`;
                } else {
                    roleSelect = `
                        <select onchange="updateUserRole(${caretaker.userId}, this.value)" style="padding: 4px 8px; font-size: 12px; border-radius: 6px; background: rgba(255,255,255,0.05); border: 1px solid var(--panel-border); color: #fff; cursor: pointer; outline: none;">
                            <option value="User" ${caretaker.role === 'User' ? 'selected' : ''}>User</option>
                            <option value="Doctor" ${caretaker.role === 'Doctor' ? 'selected' : ''}>Doctor</option>
                            <option value="Admin" ${caretaker.role === 'Admin' ? 'selected' : ''}>Admin</option>
                        </select>
                    `;
                }
                
                const deleteActionBtn = isSelf 
                    ? `<button class="btn-danger" disabled style="padding:4px 8px; font-size:12px; opacity:0.5; cursor:not-allowed;"><i class="fas fa-trash"></i> Delete</button>`
                    : `<button class="btn-danger" onclick="deleteUser(${caretaker.userId})" style="padding:4px 8px; font-size:12px;"><i class="fas fa-trash"></i> Delete</button>`;
                
                tr.innerHTML = `
                    <td><strong>${caretaker.username}</strong></td>
                    <td>${caretaker.fullName || '—'}</td>
                    <td>${caretaker.email || '—'}</td>
                    <td><small>${formatDateOnly(caretaker.createdDate)}</small></td>
                    <td>${roleBadge}</td>
                    <td>
                        <div style="display:flex; gap:6px; align-items:center;">
                            ${roleSelect}
                            ${deleteActionBtn}
                        </div>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;" class="text-secondary">No caretaker accounts found.</td></tr>`;
        }
    } catch (err) {
        showToast("Error loading users: " + err.message, "error");
    }
}

async function initApprovals() {
    const tbody = document.getElementById('approvals-table-body');
    const tableEl = document.getElementById('approvals-table');
    const emptyStateEl = document.getElementById('approvals-empty-state');
    if (!tbody) return;

    try {
        const res = await fetch('/api/users');
        const allUsers = await res.json();
        tbody.innerHTML = '';

        const pendingDoctors = allUsers.filter(user => user.role === 'Doctor' && !user.isApproved);

        if (pendingDoctors.length > 0) {
            if (tableEl) tableEl.style.display = 'table';
            if (emptyStateEl) emptyStateEl.style.display = 'none';

            pendingDoctors.forEach(doc => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>
                        <strong>${doc.fullName || doc.username}</strong><br>
                        <small class="text-secondary">@${doc.username}</small>
                    </td>
                    <td>${doc.qualification || '—'}</td>
                    <td>${doc.experience !== undefined ? doc.experience + ' Years' : '—'}</td>
                    <td>${doc.address || '—'}</td>
                    <td>
                        <small>${doc.email || '—'}</small><br>
                        <small class="text-secondary">${doc.phoneNo || '—'}</small>
                    </td>
                    <td><code>${doc.certificate || '—'}</code></td>
                    <td>
                        <button class="btn-primary" onclick="approveDoctor(${doc.userId})" style="padding: 4px 10px; font-size: 12px; background: linear-gradient(135deg, var(--accent-cyan) 0%, var(--accent-blue) 100%); box-shadow: 0 4px 10px rgba(6, 182, 212, 0.2);"><i class="fas fa-check"></i> Approve</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            if (tableEl) tableEl.style.display = 'none';
            if (emptyStateEl) emptyStateEl.style.display = 'flex';
        }
    } catch (err) {
        showToast("Error loading registration requests: " + err.message, "error");
    }
}

async function approveDoctor(userId) {
    try {
        const res = await fetch('/api/users', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId: userId, isApproved: true })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            const path = window.location.pathname;
            if (path.endsWith('approvals.html')) {
                await initApprovals();
            } else {
                await loadCaretakersList();
            }
            updateApprovalsBadge();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast("Error approving doctor: " + err.message, "error");
    }
}

async function updateApprovalsBadge() {
    try {
        const res = await fetch('/api/users');
        if (!res.ok) return;
        const allUsers = await res.json();
        const pendingDoctors = allUsers.filter(user => user.role === 'Doctor' && !user.isApproved);
        
        const approvalsLink = document.querySelector('a[href="approvals.html"]');
        if (approvalsLink) {
            let badge = approvalsLink.querySelector('.badge-count');
            if (pendingDoctors.length > 0) {
                if (!badge) {
                    badge = document.createElement('span');
                    badge.className = 'badge-count';
                    badge.style.cssText = 'margin-left: auto; background-color: var(--accent-rose); color: #fff; font-size: 11px; font-weight: 700; padding: 2px 6px; border-radius: 10px; box-shadow: 0 2px 5px rgba(244, 63, 94, 0.4);';
                    approvalsLink.appendChild(badge);
                }
                badge.textContent = pendingDoctors.length;
                badge.style.display = 'inline-block';
            } else if (badge) {
                badge.style.display = 'none';
            }
        }
    } catch (err) {
        console.error("Error updating approvals badge:", err);
    }
}

async function updateUserRole(userId, newRole) {
    try {
        const res = await fetch('/api/users', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ userId: userId, role: newRole })
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            await loadCaretakersList();
            checkAuth(false, false);
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast("Error updating user role: " + err.message, "error");
    }
}

async function deleteUser(userId) {
    if (!confirm("Are you sure you want to delete this caretaker account? This action is permanent and will delete all their registered animals and history!")) return;
    try {
        const res = await fetch(`/api/users?id=${userId}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            await loadCaretakersList();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast("Error deleting user: " + err.message, "error");
    }
}

// ==========================================
// 6. MASTER ASSESSMENT REGISTRY
// ==========================================
let allAssessments = [];

async function initAssessments() {
    const searchInput = document.getElementById('search-assessments');
    await loadAssessmentsList();

    if (searchInput) {
        searchInput.addEventListener('input', () => {
            filterAssessments(searchInput.value);
        });
    }
}

async function loadAssessmentsList() {
    const tbody = document.getElementById('assessments-table-body');
    if (!tbody) return;

    try {
        const res = await fetch('/api/assessments');
        allAssessments = await res.json();
        renderAssessmentsTable(allAssessments);
    } catch (err) {
        showToast("Error loading health assessments: " + err.message, "error");
    }
}

function renderAssessmentsTable(list) {
    const tbody = document.getElementById('assessments-table-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (list.length > 0) {
        list.forEach(item => {
            const tr = document.createElement('tr');
            const riskBadge = `<span class="badge ${item.riskLevel.toLowerCase()}">${item.riskLevel}</span>`;
            const dateStr = formatDateTime(item.assessmentDate);
            
            tr.innerHTML = `
                <td><strong>${item.animalName}</strong></td>
                <td>${item.ownerName || '—'}</td>
                <td>${riskBadge}</td>
                <td>${item.possibleCondition}</td>
                <td><small>${item.recommendedAction}</small></td>
                <td><small>${dateStr}</small></td>
            `;
            tbody.appendChild(tr);
        });
    } else {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;" class="text-secondary">No assessments found.</td></tr>`;
    }
}

function filterAssessments(query) {
    const lowerQuery = query.toLowerCase().trim();
    const filtered = allAssessments.filter(item => {
        return (
            item.animalName.toLowerCase().includes(lowerQuery) ||
            (item.ownerName && item.ownerName.toLowerCase().includes(lowerQuery)) ||
            item.possibleCondition.toLowerCase().includes(lowerQuery) ||
            item.riskLevel.toLowerCase().includes(lowerQuery)
        );
    });
    renderAssessmentsTable(filtered);
}

// ==========================================
// 7. AUDIT LOGS & SYSTEM INSIGHTS
// ==========================================
async function initAuditLogs() {
    const container = document.getElementById('timeline-container');
    if (!container) return;

    try {
        const res = await fetch('/api/audit-logs');
        const logs = await res.json();
        container.innerHTML = '';

        if (logs.length > 0) {
            logs.forEach(log => {
                const li = document.createElement('li');
                li.className = 'timeline-item';
                
                let markerClass = 'user-registered';
                let iconClass = 'fa-user-plus';
                
                if (log.activityType === 'Animal Registered') {
                    markerClass = 'animal-registered';
                    iconClass = 'fa-paw';
                } else if (log.activityType === 'Diagnosis Run') {
                    markerClass = 'diagnosis-run';
                    iconClass = 'fa-stethoscope';
                } else if (log.activityType === 'Vaccination Scheduled') {
                    markerClass = 'vaccination-scheduled';
                    iconClass = 'fa-syringe';
                }
                
                const timeStr = formatDateTime(log.timestamp);
                
                li.innerHTML = `
                    <div class="timeline-marker ${markerClass}">
                        <i class="fas ${iconClass}"></i>
                    </div>
                    <div class="timeline-content">
                        <span class="timeline-time">${timeStr}</span>
                        <div class="timeline-title">${log.activityType}</div>
                        <div class="timeline-details">${log.details}</div>
                    </div>
                `;
                container.appendChild(li);
            });
        } else {
            container.innerHTML = `<li style="text-align: center; list-style: none;" class="text-secondary">No system activity logged.</li>`;
        }
    } catch (err) {
        showToast("Error loading activity logs: " + err.message, "error");
    }
}

// ==========================================
// 8. DOCTOR OVERWATCH DASHBOARD CONTROLLER
// ==========================================
let pendingAssessments = [];
let pendingVaccinationsQueue = [];

async function initDoctorDashboard() {
    await loadDoctorDashboard();
}

async function loadDoctorDashboard() {
    try {
        // Load stats
        const resStats = await fetch('/api/dashboard');
        if (!resStats.ok) throw new Error("Failed to fetch dashboard data");
        const stats = await resStats.json();

        document.getElementById('stat-active-high-risk').textContent = stats.activeHighRiskCases || 0;
        document.getElementById('stat-pending-consultations').textContent = stats.pendingConsultations || 0;
        document.getElementById('stat-upcoming-vaccinations').textContent = stats.pendingVaccinations || 0;

        // Load Pending Consultations Table
        const resPending = await fetch('/api/assessments?pending=true');
        pendingAssessments = await resPending.json();

        const pendingTbody = document.getElementById('doctor-pending-table-body');
        pendingTbody.innerHTML = '';

        if (pendingAssessments.length > 0) {
            pendingAssessments.forEach(item => {
                const tr = document.createElement('tr');
                const riskBadge = `<span class="badge ${item.riskLevel.toLowerCase()}">${item.riskLevel}</span>`;
                const dateStr = formatDateTime(item.assessmentDate);
                
                tr.innerHTML = `
                    <td><strong>${item.animalName}</strong></td>
                    <td>${item.ownerName || '—'}</td>
                    <td>${riskBadge}</td>
                    <td>${item.possibleCondition}</td>
                    <td><small>${dateStr}</small></td>
                    <td>
                        <button class="btn-primary" onclick="openClinicalOverride(${item.assessmentId})" style="padding: 6px 12px; font-size:13px;">
                            <i class="fas fa-file-signature"></i> Review
                        </button>
                    </td>
                `;
                pendingTbody.appendChild(tr);
            });
        } else {
            pendingTbody.innerHTML = `<tr><td colspan="6" style="text-align:center;" class="text-secondary">No pending clinical reviews. All systems stable.</td></tr>`;
        }

        // Load Vaccination Queue
        const resVac = await fetch('/api/vaccinations');
        const allVac = await resVac.json();
        pendingVaccinationsQueue = allVac.filter(v => v.status !== 'Completed');

        const vacTbody = document.getElementById('doctor-vaccines-table-body');
        vacTbody.innerHTML = '';

        if (pendingVaccinationsQueue.length > 0) {
            pendingVaccinationsQueue.forEach(item => {
                const tr = document.createElement('tr');
                const statusBadge = `<span class="badge ${item.status.toLowerCase()}">${item.status}</span>`;
                
                tr.innerHTML = `
                    <td><strong>${item.animalName}</strong></td>
                    <td>${item.ownerName || '—'}</td>
                    <td><strong>${item.vaccineName}</strong></td>
                    <td>${item.scheduledDate}</td>
                    <td>${statusBadge}</td>
                    <td>
                        <button class="btn-primary" onclick="openVaccineAdmin(${item.vaccinationId})" style="padding: 6px 12px; font-size:13px; background: linear-gradient(135deg, var(--accent-emerald) 0%, var(--accent-blue) 100%); border:none; box-shadow:none;">
                            <i class="fas fa-syringe"></i> Administer
                        </button>
                    </td>
                `;
                vacTbody.appendChild(tr);
            });
        } else {
            vacTbody.innerHTML = `<tr><td colspan="6" style="text-align:center;" class="text-secondary">No pending vaccine administrations in queue.</td></tr>`;
        }

        // Load Pending Appointments
        const resApps = await fetch('/api/appointments');
        const allApps = await resApps.json();
        const pendingApps = allApps.filter(a => a.status === 'Pending');

        const appsTbody = document.getElementById('doctor-appointments-awaiting-body');
        if (appsTbody) {
            appsTbody.innerHTML = '';
            if (pendingApps.length > 0) {
                pendingApps.forEach(item => {
                    const tr = document.createElement('tr');
                    const dateStr = formatDateTime(item.appointmentDate);
                    tr.innerHTML = `
                        <td>
                            <strong>${item.animalName}</strong><br>
                            <small class="text-secondary">${item.animalSpecies} | ${item.animalBreed || 'Unknown'}</small>
                        </td>
                        <td>
                            <strong>${item.ownerName || 'Unknown'}</strong><br>
                            <small class="text-secondary"><i class="fas fa-phone"></i> ${item.ownerContact || '—'}</small>
                        </td>
                        <td><small>${dateStr}</small></td>
                        <td>${item.reason || 'Regular Checkup'}</td>
                        <td>
                            <div style="display: flex; gap: 6px;">
                                <button class="btn-primary" onclick="updateAppointmentStatus(${item.appointmentId}, 'Scheduled')" style="padding: 4px 8px; font-size: 11px; border-radius: 6px; background: linear-gradient(135deg, var(--accent-emerald) 0%, #059669 100%);">
                                    <i class="fas fa-check"></i> Accept
                                </button>
                                <button class="btn-danger" onclick="updateAppointmentStatus(${item.appointmentId}, 'Cancelled')" style="padding: 4px 8px; font-size: 11px; border-radius: 6px;">
                                    <i class="fas fa-times"></i> Decline
                                </button>
                            </div>
                        </td>
                    `;
                    appsTbody.appendChild(tr);
                });
            } else {
                appsTbody.innerHTML = `<tr><td colspan="5" style="text-align:center;" class="text-secondary">No pending appointments awaiting acceptance.</td></tr>`;
            }
        }

    } catch (err) {
        showToast("Error loading vet dashboard: " + err.message, "error");
    }
}

// Override Modal Triggers
function openClinicalOverride(assessmentId) {
    const item = pendingAssessments.find(a => a.assessmentId === assessmentId);
    if (!item) return;

    document.getElementById('override-assessment-id').value = item.assessmentId;
    document.getElementById('override-animal-name').value = item.animalName;
    document.getElementById('override-owner-name').value = item.ownerName || '—';
    document.getElementById('override-auto-condition').value = item.possibleCondition;
    
    const riskContainer = document.getElementById('override-auto-risk');
    riskContainer.innerHTML = `<span class="badge ${item.riskLevel.toLowerCase()}">${item.riskLevel}</span>`;
    
    document.getElementById('override-diagnosis').value = item.doctorDiagnosis || '';
    document.getElementById('override-notes').value = item.treatmentNotes || '';
    document.getElementById('override-prescription').value = item.prescription || '';

    document.getElementById('override-modal').classList.add('active');
}

function closeOverrideModal() {
    document.getElementById('override-modal').classList.remove('active');
}

async function saveClinicalOverride(event) {
    event.preventDefault();
    const payload = {
        assessmentId: parseInt(document.getElementById('override-assessment-id').value),
        doctorDiagnosis: document.getElementById('override-diagnosis').value,
        treatmentNotes: document.getElementById('override-notes').value,
        prescription: document.getElementById('override-prescription').value
    };

    try {
        const res = await fetch('/api/assessments', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            closeOverrideModal();
            // Check if we are on doctor dashboard or medical history and refresh
            if (window.location.pathname.endsWith('doctor_dashboard.html')) {
                await loadDoctorDashboard();
            } else if (window.location.pathname.endsWith('medical_history.html')) {
                const select = document.getElementById('history-animal-select');
                if (select) loadAnimalMedicalHistory(select.value);
            }
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast("Error updating assessment: " + err.message, "error");
    }
}

// Vaccine Modal Triggers
function openVaccineAdmin(vaccinationId) {
    const item = pendingVaccinationsQueue.find(v => v.vaccinationId === vaccinationId);
    if (!item) return;

    document.getElementById('vaccinate-id').value = item.vaccinationId;
    document.getElementById('vaccinate-animal-name').value = item.animalName;
    document.getElementById('vaccinate-name').value = item.vaccineName;
    document.getElementById('vaccinate-date').value = new Date().toISOString().split('T')[0];
    document.getElementById('vaccinate-notes').value = item.notes || '';

    document.getElementById('vaccinate-modal').classList.add('active');
}

function closeVaccinateModal() {
    document.getElementById('vaccinate-modal').classList.remove('active');
}

async function saveVaccineAdministration(event) {
    event.preventDefault();
    const payload = {
        vaccinationId: parseInt(document.getElementById('vaccinate-id').value),
        administeredDate: document.getElementById('vaccinate-date').value,
        notes: document.getElementById('vaccinate-notes').value
    };

    try {
        const res = await fetch('/api/vaccinations', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (data.success) {
            showToast(data.message, 'success');
            closeVaccinateModal();
            if (window.location.pathname.endsWith('doctor_dashboard.html')) {
                await loadDoctorDashboard();
            }
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast("Error administering vaccination: " + err.message, "error");
    }
}

// ==========================================
// 9. MEDICAL HISTORY TIMELINE CONTROLLER
// ==========================================
let historyAssessments = [];

async function initMedicalHistory() {
    const select = document.getElementById('history-animal-select');
    if (!select) return;

    try {
        const res = await fetch('/api/animals');
        const animals = await res.json();
        select.innerHTML = '<option value="">-- Choose an Animal --</option>';
        animals.forEach(a => {
            select.innerHTML += `<option value="${a.animalId}">${a.name} (${a.species})</option>`;
        });
    } catch (err) {
        showToast("Error loading animals: " + err.message, "error");
    }
}

async function loadAnimalMedicalHistory(animalId) {
    const container = document.getElementById('history-details-container');
    if (!container) return;

    if (!animalId || animalId === '') {
        container.style.display = 'none';
        return;
    }

    try {
        // 1. Fetch Profile
        const resProfile = await fetch(`/api/animals?id=${animalId}`);
        if (!resProfile.ok) throw new Error("Failed to fetch animal details");
        const animal = await resProfile.json();

        document.getElementById('info-name').textContent = animal.name;
        document.getElementById('info-species').textContent = animal.species;
        document.getElementById('info-breed').textContent = animal.breed || 'Unknown';
        document.getElementById('info-age').textContent = animal.age != null ? animal.age + ' years' : 'N/A';
        document.getElementById('info-weight').textContent = animal.weight != null ? animal.weight + ' kg' : 'N/A';
        document.getElementById('info-type').textContent = animal.animalType;
        document.getElementById('info-owner').textContent = animal.ownerName || 'Unknown';
        document.getElementById('info-contact').textContent = animal.contactNumber || 'N/A';

        // 2. Fetch Assessments Timeline
        const resAssess = await fetch(`/api/assessments?animalId=${animalId}`);
        const assessments = await resAssess.json();
        historyAssessments = assessments;

        const timeline = document.getElementById('clinical-timeline');
        timeline.innerHTML = '';

        if (assessments.length > 0) {
            assessments.forEach(item => {
                const li = document.createElement('li');
                li.className = 'timeline-item';

                // Determine risk style
                let riskClass = 'low-risk';
                if (item.riskLevel === 'High') riskClass = 'high-risk';
                if (item.riskLevel === 'Medium') riskClass = 'med-risk';

                let markerIcon = '<i class="fas fa-stethoscope"></i>';
                
                const dateStr = formatDateTime(item.assessmentDate);
                
                let overrideSection = '';
                if (item.doctorDiagnosis) {
                    overrideSection = `
                        <div style="margin-top:12px; padding-top:10px; border-top:1px dashed var(--panel-border);">
                            <div style="color: var(--accent-blue); font-weight:600; margin-bottom:4px; display:flex; align-items:center; gap:6px;">
                                <i class="fas fa-user-doctor"></i> Final Diagnosis: ${item.doctorDiagnosis}
                            </div>
                            <div style="font-size:13px; color: var(--text-secondary); margin-bottom:4px;">
                                <strong>Treatment Notes:</strong> ${item.treatmentNotes || '—'}
                            </div>
                            <div style="font-size:13px; color: var(--text-secondary);">
                                <strong>Prescription:</strong> ${item.prescription || '—'}
                            </div>
                            <div style="font-size:11px; color: var(--text-muted); text-align:right; margin-top:4px;">
                                Signed by: Dr. ${item.doctorName || 'Veterinarian'}
                            </div>
                        </div>
                    `;
                } else {
                    const isDoctorOrAdmin = document.getElementById('sidebar-user-role')?.textContent === 'Doctor' || 
                                           document.getElementById('sidebar-user-role')?.textContent === 'Admin';
                    if (isDoctorOrAdmin) {
                        overrideSection = `
                            <div style="margin-top:10px; text-align:right;">
                                <button class="btn-secondary" onclick="openClinicalOverrideFromHistory(${item.assessmentId})" style="padding:4px 8px; font-size:12px; border-radius:6px;">
                                    <i class="fas fa-file-signature"></i> Add Clinical Review
                                </button>
                            </div>
                        `;
                    }
                }

                li.innerHTML = `
                    <div class="timeline-marker ${riskClass}">
                        ${markerIcon}
                    </div>
                    <div class="timeline-content">
                        <span class="timeline-time">${dateStr}</span>
                        <div class="timeline-title">
                            <span>System Assessment: ${item.possibleCondition}</span>
                            <span class="badge ${item.riskLevel.toLowerCase()}">${item.riskLevel} Risk</span>
                        </div>
                        <div class="timeline-details">
                            <strong>System Guideline:</strong> ${item.recommendedAction}
                            ${overrideSection}
                        </div>
                    </div>
                `;
                timeline.appendChild(li);
            });
        } else {
            timeline.innerHTML = '<li style="text-align: center; list-style: none;" class="text-secondary">No diagnostics run.</li>';
        }

        // 3. Fetch Vaccinations
        const resVac = await fetch(`/api/vaccinations?animalId=${animalId}`);
        const vaccines = await resVac.json();

        const vacTbody = document.getElementById('history-vaccines-body');
        vacTbody.innerHTML = '';

        if (vaccines.length > 0) {
            vaccines.forEach(v => {
                const tr = document.createElement('tr');
                const statusBadge = `<span class="badge ${v.status.toLowerCase()}">${v.status}</span>`;
                const adminDate = v.administeredDate ? v.administeredDate : '—';
                
                tr.innerHTML = `
                    <td><strong>${v.vaccineName}</strong></td>
                    <td>${v.scheduledDate}</td>
                    <td>${adminDate}</td>
                    <td>${statusBadge}</td>
                    <td><small>${v.notes || '—'}</small></td>
                `;
                vacTbody.appendChild(tr);
            });
        } else {
            vacTbody.innerHTML = '<tr><td colspan="5" style="text-align: center;" class="text-secondary">No vaccinations on record.</td></tr>';
        }

        container.style.display = 'block';

    } catch (err) {
        showToast("Error loading clinical history: " + err.message, "error");
    }
}

// Dynamic clinical override call from history page
function openClinicalOverrideFromHistory(assessmentId) {
    const item = historyAssessments.find(a => a.assessmentId === assessmentId);
    if (item) {
        pendingAssessments = [item];
        openClinicalOverride(assessmentId);
    } else {
        showToast("Assessment details not found.", "error");
    }
}

// ==========================================
// 6. APPOINTMENTS SYSTEM CONTROLLER
// ==========================================
async function initAppointments() {
    const role = window.currentUser ? window.currentUser.role : 'User';
    
    // Hide all role sections first
    document.querySelectorAll('.role-section').forEach(s => s.style.display = 'none');
    
    if (role === 'Admin') {
        const adminSec = document.getElementById('admin-appointments-section');
        if (adminSec) adminSec.style.display = 'block';
        document.getElementById('appointments-title').textContent = 'Clinic Appointments Directory';
        document.getElementById('appointments-subtitle').textContent = 'Global overview and administrative control of all scheduled clinical consultations.';
        await loadAdminDirectory();
    } else if (role === 'Doctor') {
        const doctorSec = document.getElementById('doctor-appointments-section');
        if (doctorSec) doctorSec.style.display = 'block';
        document.getElementById('appointments-title').textContent = 'Doctor Consultation Queue';
        document.getElementById('appointments-subtitle').textContent = 'Review your assigned scheduled appointments, check patient history, and update visit statuses.';
        await loadDoctorQueue();
    } else {
        const userSec = document.getElementById('user-appointments-section');
        if (userSec) userSec.style.display = 'block';
        document.getElementById('appointments-title').textContent = 'Appointments Scheduler';
        document.getElementById('appointments-subtitle').textContent = 'Book a new session with our certified veterinarians and review your upcoming visits.';
        
        await loadUserAnimalsDropdown();
        await loadDoctorCardsGrid();
        await loadUserAppointmentsList();
        
        const form = document.getElementById('appointment-form');
        if (form) {
            form.addEventListener('submit', handleBookAppointment);
        }
    }
}

// Helper: load user animals into select dropdown
async function loadUserAnimalsDropdown() {
    const select = document.getElementById('appointment-animal-select');
    if (!select) return;
    try {
        const res = await fetch('/api/animals');
        if (!res.ok) throw new Error("Failed to fetch animals");
        const animals = await res.json();
        
        select.innerHTML = '<option value="">-- Choose Your Pet --</option>';
        animals.forEach(a => {
            select.innerHTML += `<option value="${a.animalId}">${a.name} (${a.species} - ${a.breed || 'Unknown'})</option>`;
        });
    } catch (err) {
        showToast("Error loading pets dropdown: " + err.message, "error");
    }
}

// Helper: load approved doctor cards
async function loadDoctorCardsGrid() {
    const grid = document.getElementById('doctor-cards-grid');
    if (!grid) return;
    try {
        const res = await fetch('/api/appointments?action=getDoctors');
        if (!res.ok) throw new Error("Failed to fetch approved doctors");
        const doctors = await res.json();
        
        grid.innerHTML = '';
        if (doctors.length > 0) {
            doctors.forEach(doc => {
                const card = document.createElement('div');
                card.className = 'doctor-select-card';
                card.onclick = () => selectDoctor(doc.userId, card);
                
                card.innerHTML = `
                    <div class="doctor-card-avatar">
                        <i class="fas fa-user-md"></i>
                    </div>
                    <div class="doctor-card-info">
                        <h4>Dr. ${doc.fullName || doc.username}</h4>
                        <p class="doctor-qualification"><i class="fas fa-graduation-cap"></i> ${doc.qualification || 'General Practice'}</p>
                        <p class="doctor-experience"><i class="fas fa-briefcase"></i> ${doc.experience || 0} Years Experience</p>
                        <p class="doctor-address"><i class="fas fa-location-dot"></i> ${doc.address || 'Clinic Branch'}</p>
                        <p class="doctor-contact"><i class="fas fa-phone"></i> ${doc.phoneNo || '—'}</p>
                    </div>
                `;
                grid.appendChild(card);
            });
        } else {
            grid.innerHTML = `<div style="padding: 1.5rem; text-align:center;" class="text-secondary">No approved veterinarians currently available.</div>`;
        }
    } catch (err) {
        showToast("Error loading doctors: " + err.message, "error");
    }
}

// Global selector function for doctor card click
function selectDoctor(doctorId, element) {
    const input = document.getElementById('selected-doctor-id');
    if (input) input.value = doctorId;
    
    document.querySelectorAll('.doctor-select-card').forEach(c => c.classList.remove('selected'));
    element.classList.add('selected');
}
window.selectDoctor = selectDoctor;

// Helper: load user's appointments list
async function loadUserAppointmentsList() {
    const tbody = document.getElementById('user-appointments-body');
    if (!tbody) return;
    try {
        const res = await fetch('/api/appointments');
        if (!res.ok) throw new Error("Failed to fetch appointments");
        const appointments = await res.json();
        
        tbody.innerHTML = '';
        if (appointments.length > 0) {
            appointments.forEach(app => {
                const tr = document.createElement('tr');
                const statusClass = (app.status || 'Scheduled').toLowerCase();
                const statusBadge = `<span class="badge ${statusClass}">${app.status || 'Scheduled'}</span>`;
                const dateStr = formatDateTime(app.appointmentDate);
                
                let actionBtn = '—';
                if (app.status === 'Scheduled' || app.status === 'Pending') {
                    actionBtn = `
                        <button class="btn-danger" onclick="updateAppointmentStatus(${app.appointmentId}, 'Cancelled')" style="padding: 4px 8px; font-size: 11px; border-radius: 6px;">
                            <i class="fas fa-circle-xmark"></i> Cancel
                        </button>
                    `;
                }
                
                tr.innerHTML = `
                    <td><strong>${app.animalName}</strong></td>
                    <td>Dr. ${app.doctorName || 'Veterinarian'}</td>
                    <td><small>${dateStr}</small></td>
                    <td>${app.reason || 'Regular Checkup'}</td>
                    <td>${statusBadge}</td>
                    <td>${actionBtn}</td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;" class="text-secondary">No scheduled appointments logged.</td></tr>`;
        }
    } catch (err) {
        showToast("Error loading appointments: " + err.message, "error");
    }
}

// Helper: handle user booking form submit
async function handleBookAppointment(e) {
    e.preventDefault();
    const animalId = document.getElementById('appointment-animal-select').value;
    const doctorId = document.getElementById('selected-doctor-id').value;
    const appDate = document.getElementById('appointment-date').value;
    const reason = document.getElementById('appointment-reason').value;
    
    if (!animalId) {
        showToast("Please select one of your registered animals.", "warning");
        return;
    }
    if (!doctorId) {
        showToast("Please choose one of the approved veterinarians.", "warning");
        return;
    }
    if (!appDate) {
        showToast("Please specify the preferred appointment date.", "warning");
        return;
    }
    
    const payload = {
        animalId: parseInt(animalId),
        doctorId: parseInt(doctorId),
        appointmentDate: appDate,
        reason: reason
    };
    
    try {
        const res = await fetch('/api/appointments', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        
        if (data.success) {
            showToast(data.message, 'success');
            document.getElementById('appointment-form').reset();
            const docIdInput = document.getElementById('selected-doctor-id');
            if (docIdInput) docIdInput.value = '';
            document.querySelectorAll('.doctor-select-card').forEach(c => c.classList.remove('selected'));
            await loadUserAppointmentsList();
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast("Booking failed: " + err.message, "error");
    }
}

// Helper: load doctor queue list
async function loadDoctorQueue() {
    const tbody = document.getElementById('doctor-appointments-body');
    if (!tbody) return;
    try {
        const res = await fetch('/api/appointments');
        if (!res.ok) throw new Error("Failed to load consultation queue");
        const appointments = await res.json();
        
        tbody.innerHTML = '';
        if (appointments.length > 0) {
            appointments.forEach(app => {
                const tr = document.createElement('tr');
                const statusClass = (app.status || 'Scheduled').toLowerCase();
                const statusBadge = `<span class="badge ${statusClass}">${app.status || 'Scheduled'}</span>`;
                const dateStr = formatDateTime(app.appointmentDate);
                
                let actions = '—';
                if (app.status === 'Pending') {
                    actions = `
                        <div style="display: flex; gap: 6px;">
                            <button class="btn-primary" onclick="updateAppointmentStatus(${app.appointmentId}, 'Scheduled')" style="padding: 4px 8px; font-size: 11px; border-radius: 6px; background: linear-gradient(135deg, var(--accent-emerald) 0%, #059669 100%);">
                                <i class="fas fa-check"></i> Accept
                            </button>
                            <button class="btn-danger" onclick="updateAppointmentStatus(${app.appointmentId}, 'Cancelled')" style="padding: 4px 8px; font-size: 11px; border-radius: 6px;">
                                <i class="fas fa-times"></i> Decline
                            </button>
                        </div>
                    `;
                } else if (app.status === 'Scheduled') {
                    actions = `
                        <div style="display: flex; gap: 6px;">
                            <button class="btn-primary" onclick="updateAppointmentStatus(${app.appointmentId}, 'Completed')" style="padding: 4px 8px; font-size: 11px; border-radius: 6px; background: linear-gradient(135deg, var(--accent-emerald) 0%, #059669 100%);">
                                <i class="fas fa-circle-check"></i> Complete
                            </button>
                            <button class="btn-danger" onclick="updateAppointmentStatus(${app.appointmentId}, 'Cancelled')" style="padding: 4px 8px; font-size: 11px; border-radius: 6px;">
                                <i class="fas fa-circle-xmark"></i> Cancel
                            </button>
                        </div>
                    `;
                }
                
                tr.innerHTML = `
                    <td>
                        <strong>${app.animalName}</strong><br>
                        <small class="text-secondary">${app.animalSpecies} | ${app.animalBreed || 'Unknown'}</small><br>
                        <small class="text-muted">Age: ${app.animalAge || '—'} yrs | Wt: ${app.animalWeight || '—'} kg</small>
                    </td>
                    <td>
                        <strong>${app.ownerName || 'Unknown'}</strong><br>
                        <small class="text-secondary"><i class="fas fa-phone"></i> ${app.ownerContact || '—'}</small>
                    </td>
                    <td><small>${dateStr}</small></td>
                    <td>${app.reason || 'Regular Checkup'}</td>
                    <td>${statusBadge}</td>
                    <td>${actions}</td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;" class="text-secondary">No assigned appointments in queue.</td></tr>`;
        }
    } catch (err) {
        showToast("Error loading consultation queue: " + err.message, "error");
    }
}

// Helper: load admin global appointments directory
async function loadAdminDirectory() {
    const tbody = document.getElementById('admin-appointments-body');
    if (!tbody) return;
    try {
        const res = await fetch('/api/appointments');
        if (!res.ok) throw new Error("Failed to load appointments registry");
        const appointments = await res.json();
        
        tbody.innerHTML = '';
        if (appointments.length > 0) {
            appointments.forEach(app => {
                const tr = document.createElement('tr');
                const statusClass = (app.status || 'Scheduled').toLowerCase();
                const statusBadge = `<span class="badge ${statusClass}">${app.status || 'Scheduled'}</span>`;
                const dateStr = formatDateTime(app.appointmentDate);
                
                let actions = '—';
                if (app.status === 'Pending') {
                    actions = `
                        <div style="display: flex; gap: 6px;">
                            <button class="btn-primary" onclick="updateAppointmentStatus(${app.appointmentId}, 'Scheduled')" style="padding: 4px 8px; font-size: 11px; border-radius: 6px; background: linear-gradient(135deg, var(--accent-emerald) 0%, #059669 100%);" title="Accept">
                                <i class="fas fa-check"></i>
                            </button>
                            <button class="btn-danger" onclick="updateAppointmentStatus(${app.appointmentId}, 'Cancelled')" style="padding: 4px 8px; font-size: 11px; border-radius: 6px;" title="Decline">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                    `;
                } else if (app.status === 'Scheduled') {
                    actions = `
                        <div style="display: flex; gap: 6px;">
                            <button class="btn-primary" onclick="updateAppointmentStatus(${app.appointmentId}, 'Completed')" style="padding: 4px 8px; font-size: 11px; border-radius: 6px; background: linear-gradient(135deg, var(--accent-emerald) 0%, #059669 100%);" title="Complete">
                                <i class="fas fa-check-double"></i>
                            </button>
                            <button class="btn-danger" onclick="updateAppointmentStatus(${app.appointmentId}, 'Cancelled')" style="padding: 4px 8px; font-size: 11px; border-radius: 6px;" title="Cancel">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    `;
                }
                
                tr.innerHTML = `
                    <td>
                        <strong>${app.animalName}</strong><br>
                        <small class="text-secondary">${app.animalSpecies} | ${app.animalBreed || 'Unknown'}</small>
                    </td>
                    <td>
                        <strong>${app.ownerName || 'Unknown'}</strong><br>
                        <small class="text-secondary"><i class="fas fa-phone"></i> ${app.ownerContact || '—'}</small>
                    </td>
                    <td>Dr. ${app.doctorName || 'Veterinarian'}</td>
                    <td><small>${dateStr}</small></td>
                    <td>${app.reason || 'Regular Checkup'}</td>
                    <td>${statusBadge}</td>
                    <td>${actions}</td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;" class="text-secondary">No appointments recorded.</td></tr>`;
        }
    } catch (err) {
        showToast("Error loading appointments registry: " + err.message, "error");
    }
}

// Global action dispatcher for completing/cancelling appointments
async function updateAppointmentStatus(appointmentId, status) {
    if (status === 'Cancelled' && !confirm("Are you sure you want to cancel this appointment?")) {
        return;
    }
    
    const payload = {
        appointmentId: appointmentId,
        status: status
    };
    
    try {
        const res = await fetch('/api/appointments', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const data = await res.json();
        
        if (data.success) {
            showToast(data.message, 'success');
            // Refresh based on active page
            if (window.location.pathname.endsWith('doctor_dashboard.html')) {
                await loadDoctorDashboard();
            } else {
                const role = window.currentUser ? window.currentUser.role : 'User';
                if (role === 'Admin') {
                    await loadAdminDirectory();
                } else if (role === 'Doctor') {
                    await loadDoctorQueue();
                } else {
                    await loadUserAppointmentsList();
                }
            }
        } else {
            showToast(data.message, 'error');
        }
    } catch (err) {
        showToast("Failed to update status: " + err.message, "error");
    }
}
window.updateAppointmentStatus = updateAppointmentStatus;


