// auth.js
// Handles user authentication, session validations, and logins

const AUTH_API = '/api/auth';

// Helper: Show toast notifications
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let iconClass = 'fa-check-circle';
    if (type === 'error') iconClass = 'fa-exclamation-circle';
    if (type === 'warning') iconClass = 'fa-exclamation-triangle';
    
    toast.innerHTML = `<i class="fas ${iconClass}"></i> <span>${message}</span>`;
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// Verify user authentication
async function checkAuth(redirectOnFail = true, redirectOnSuccess = false) {
    try {
        const res = await fetch(AUTH_API);
        if (!res.ok) throw new Error("Auth endpoint unreachable");
        const data = await res.json();
        
        if (data.authenticated) {
            // Update User Profile details on sidebar if elements exist
            const nameEl = document.getElementById('sidebar-user-name');
            const roleEl = document.getElementById('sidebar-user-role');
            const avatarEl = document.getElementById('sidebar-user-avatar');
            
            if (nameEl) nameEl.textContent = data.user.fullName || data.user.username;
            if (roleEl) roleEl.textContent = data.user.role;
            if (avatarEl) {
                const initial = (data.user.fullName || data.user.username).charAt(0).toUpperCase();
                avatarEl.textContent = initial;
            }

            if (data.user.role === 'Admin') {
                document.querySelectorAll('.admin-only').forEach(el => el.style.display = 'block');
                document.querySelectorAll('.doctor-only').forEach(el => el.style.display = 'none');
                document.querySelectorAll('.nav-link').forEach(el => {
                    const href = el.getAttribute('href');
                    if (href === 'dashboard.html' || href === 'animals.html' || href === 'symptoms.html' || href === 'vaccinations.html') {
                        const li = el.closest('li');
                        if (li) li.style.display = 'none';
                    }
                });
            } else if (data.user.role === 'Doctor') {
                document.querySelectorAll('.doctor-only').forEach(el => el.style.display = 'block');
                document.querySelectorAll('.admin-only').forEach(el => el.style.display = 'none');
                document.querySelectorAll('.nav-link').forEach(el => {
                    const href = el.getAttribute('href');
                    if (href === 'dashboard.html') {
                        const li = el.closest('li');
                        if (li) li.style.display = 'none';
                    }
                });
            } else {
                document.querySelectorAll('.admin-only').forEach(el => el.style.display = 'none');
                document.querySelectorAll('.doctor-only').forEach(el => el.style.display = 'none');
            }

            // Reveal the navigation menu after elements have been filtered to avoid visual flickering
            const navMenu = document.querySelector('.nav-menu');
            if (navMenu) {
                navMenu.style.opacity = '1';
            }

            if (redirectOnSuccess) {
                if (data.user.role === 'Doctor') {
                    window.location.href = 'doctor_dashboard.html';
                } else if (data.user.role === 'Admin') {
                    window.location.href = 'caretakers.html';
                } else {
                    window.location.href = 'dashboard.html';
                }
            }
            return data.user;
        } else {
            if (redirectOnFail) {
                window.location.href = 'index.html';
            }
            return null;
        }
    } catch (err) {
        console.error('Session verification error:', err);
        if (redirectOnFail) {
            window.location.href = 'index.html';
        }
        return null;
    }
}

// Log out session
async function logout() {
    try {
        const response = await fetch(`${AUTH_API}?action=logout`, {
            method: 'POST'
        });
        const result = await response.json();
        if (result.success) {
            window.location.href = 'index.html';
        } else {
            showToast(result.message || 'Logout failed', 'error');
        }
    } catch (err) {
        console.error('Logout error:', err);
        window.location.href = 'index.html';
    }
}
