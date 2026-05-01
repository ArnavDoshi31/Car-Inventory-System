let currentTab = 'cars';
const apiBase = '/api';
let editingId = null;
let chartInstance = null;

const tabConfig = {
    cars: { title: 'Cars Inventory', subtitle: 'Manage your fleet and track availability.' },
    customers: { title: 'Customers', subtitle: 'View and manage client relationships.' },
    employees: { title: 'Employees', subtitle: 'Manage staff roles and details.' },
    sales: { title: 'Sales & Analytics', subtitle: 'Track revenue, profits, and performance.' }
};

const schemas = {
    cars: [
        { name: 'id', label: 'Car ID', type: 'text', icon: 'fa-hashtag' },
        { name: 'make', label: 'Make', type: 'text', icon: 'fa-car' },
        { name: 'model', label: 'Model', type: 'text', icon: 'fa-car-side' },
        { name: 'year', label: 'Year', type: 'text', icon: 'fa-calendar' },
        { name: 'price', label: 'Price', type: 'text', icon: 'fa-tag' },
        { name: 'quantity', label: 'Quantity', type: 'text', icon: 'fa-cubes' },
        { name: 'image', label: 'Car Picture', type: 'file' }
    ],
    customers: [
        { name: 'id', label: 'Customer ID', type: 'text', icon: 'fa-hashtag' },
        { name: 'name', label: 'Full Name', type: 'text', icon: 'fa-user' },
        { name: 'contact', label: 'Contact Number', type: 'text', icon: 'fa-phone' },
        { name: 'address', label: 'Address', type: 'text', icon: 'fa-map-marker-alt' }
    ],
    employees: [
        { name: 'id', label: 'Employee ID', type: 'text', icon: 'fa-hashtag' },
        { name: 'name', label: 'Full Name', type: 'text', icon: 'fa-user-tie' },
        { name: 'role', label: 'Role / Position', type: 'text', icon: 'fa-briefcase' },
        { name: 'image', label: 'Profile Picture', type: 'file' }
    ],
    sales: [
        { name: 'id', label: 'Sale ID', type: 'text', icon: 'fa-hashtag' },
        { name: 'carId', label: 'Car ID', type: 'text', icon: 'fa-car' },
        { name: 'customerId', label: 'Customer ID', type: 'text', icon: 'fa-user' },
        { name: 'employeeId', label: 'Employee ID', type: 'text', icon: 'fa-user-tie' },
        { name: 'date', label: 'Date (YYYY-MM)', type: 'text', icon: 'fa-calendar-alt' },
        { name: 'amount', label: 'Sale Amount', type: 'text', icon: 'fa-dollar-sign' }
    ]
};

document.addEventListener('DOMContentLoaded', () => {
    showTab(currentTab);
    
    document.getElementById('add-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const form = e.target;
        const formData = new FormData(form);
        const data = Object.fromEntries(formData.entries());
        
        if (currentTab === 'sales' && !editingId) {
            try {
                const carRes = await fetch(`${apiBase}/cars`);
                const cars = await carRes.json();
                const car = cars.find(c => c.id === data.carId);
                if (!car) {
                    alert("Error: Car ID not found in inventory!");
                    return;
                }
                if ((parseInt(car.quantity) || 0) <= 0) {
                    alert("Error: Cannot add sale, this car is Sold Out (0 left).");
                    return;
                }
            } catch (err) {
                console.error("Error validating inventory", err);
            }
        }


        const fileInput = form.querySelector('input[type="file"]');
        if (fileInput && fileInput.files.length > 0) {
            const file = fileInput.files[0];
            data.base64Image = await toBase64(file);
        }
        if (data.removeImage === 'true') {
            data.imageUrl = '';
        }
        delete data.image; 
        delete data.removeImage;

        const method = editingId ? 'PUT' : 'POST';
        const url = editingId ? `${apiBase}/${currentTab}/${editingId}` : `${apiBase}/${currentTab}`;
        
        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        if (res.ok) {
            closeModal();
            loadData(currentTab);
        } else {
            const errData = await res.json();
            alert("Failed: " + (errData.error || "Server error"));
        }
    });
});

function toBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
    });
}

function showTab(tab) {
    currentTab = tab;
    document.querySelectorAll('nav li').forEach(li => li.classList.remove('active'));
    document.querySelector(`nav li[onclick*="${tab}"]`).classList.add('active');
    
    document.getElementById('page-title').innerText = tabConfig[tab].title;
    document.getElementById('page-subtitle').innerText = tabConfig[tab].subtitle;
    
    // Hide/Show Analytics Container
    document.getElementById('analytics-container').style.display = tab === 'sales' ? 'block' : 'none';
    
    // Trigger re-animation
    const dataContainer = document.getElementById('data-container');
    dataContainer.style.animation = 'none';
    dataContainer.offsetHeight; /* trigger reflow */
    dataContainer.style.animation = 'fadeInUp 0.5s ease 0.3s both';
    
    loadData(tab);
}

async function loadData(tab) {
    const container = document.getElementById('data-container');
    container.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 50px;"><i class="fa-solid fa-spinner fa-spin fa-3x"></i><p style="margin-top:20px;">Loading data...</p></div>';
    
    try {
        const [res, carsRes, custRes] = await Promise.all([
            fetch(`${apiBase}/${tab}`),
            tab === 'sales' ? fetch(`${apiBase}/cars`) : Promise.resolve(null),
            tab === 'sales' ? fetch(`${apiBase}/customers`) : Promise.resolve(null)
        ]);
        
        const data = await res.json();
        const carsData = carsRes ? await carsRes.json() : [];
        const customersData = custRes ? await custRes.json() : [];
        
        renderSummaryWidgets(tab, data, carsData);
        
        if (tab === 'sales') {
            renderSalesChart(data, carsData, customersData);
        }

        if (data.length === 0) {
            container.innerHTML = `
                <div style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 50px; background: var(--glass-card); border-radius: 16px; border: 1px dashed var(--border);">
                    <i class="fa-solid fa-folder-open fa-3x" style="opacity: 0.5; margin-bottom: 15px;"></i>
                    <p>No records found. Click "Add New" to get started.</p>
                </div>`;
            return;
        }
        
        container.innerHTML = data.map(item => buildCardHTML(item, tab, carsData)).join('');
    } catch (e) {
        container.innerHTML = `<div style="grid-column: 1/-1; color: var(--danger); text-align: center; padding: 20px;">Error loading data: ${e.message}</div>`;
    }
}

function buildCardHTML(item, tab, carsData = []) {
    let iconClass = 'fa-car';
    if (tab === 'customers') iconClass = 'fa-user';
    if (tab === 'employees') iconClass = 'fa-user-tie';
    if (tab === 'sales') iconClass = 'fa-file-invoice-dollar';

    const hasImage = item.imageUrl && item.imageUrl !== 'null' && item.imageUrl !== '';
    const imageHTML = hasImage 
        ? `<img src="${item.imageUrl}" class="card-image" alt="Image">` 
        : `<div class="card-icon-placeholder"><i class="fa-solid ${iconClass}"></i></div>`;

    let badgeHTML = '';
    let priceHTML = '';
    let titleHTML = '';

    if (tab === 'cars') {
        const qty = parseInt(item.quantity) || 0;
        const isAvail = qty > 0;
        badgeHTML = `<span class="badge ${isAvail ? 'badge-green' : 'badge-red'}">${isAvail ? qty + ' Available' : 'Sold Out (0)'}</span>`;
        priceHTML = `<div class="card-price"><i class="fa-solid fa-tag"></i> $${parseFloat(item.price).toLocaleString()}</div>`;
        titleHTML = `${item.make} ${item.model}`;
    } else if (tab === 'sales') {
        const relatedCar = carsData.find(c => c.id === item.carId);
        const profit = relatedCar ? parseFloat(item.amount) - parseFloat(relatedCar.price) : 0;
        badgeHTML = `<span class="badge badge-green">Profit: $${profit.toLocaleString()}</span>`;
        priceHTML = `<div class="card-price"><i class="fa-solid fa-dollar-sign"></i> ${parseFloat(item.amount).toLocaleString()}</div>`;
        titleHTML = `Sale #${item.id}`;
    } else {
        titleHTML = item.name;
    }

    const detailsFields = schemas[tab].filter(f => f.name !== 'image' && f.name !== 'id' && f.name !== 'make' && f.name !== 'model' && f.name !== 'price' && f.name !== 'available' && f.name !== 'name' && f.name !== 'amount');

    const detailsHTML = detailsFields.map(f => {
        if (!item[f.name]) return '';
        return `<div class="card-info-row"><i class="fa-solid ${f.icon || 'fa-info-circle'}"></i> <span><strong>${f.label.split(' ')[0]}:</strong> ${item[f.name]}</span></div>`;
    }).join('');

    return `
        <div class="card">
            <div class="card-content" style="${tab === 'sales' || tab === 'customers' ? 'flex-direction: column; align-items: flex-start;' : ''}">
                ${(tab === 'cars' || tab === 'employees') ? imageHTML : ''}
                <div class="card-details">
                    ${badgeHTML}
                    <h3>${titleHTML}</h3>
                    <div class="card-info-row"><i class="fa-solid fa-hashtag"></i> <span><strong>ID:</strong> ${item.id}</span></div>
                    ${detailsHTML}
                    ${priceHTML}
                </div>
            </div>
            <div class="card-actions">
                <button class="btn-action btn-edit" onclick='editItem(${JSON.stringify(item).replace(/'/g, "\\'")})'><i class="fa-solid fa-pen"></i> Edit</button>
                <button class="btn-action btn-delete" onclick="deleteItem('${item.id}')"><i class="fa-solid fa-trash"></i> Delete</button>
            </div>
        </div>
    `;
}

function renderSummaryWidgets(tab, data, carsData) {
    const widgetsContainer = document.getElementById('summary-widgets');
    let widgetsHTML = '';

    if (tab === 'cars') {
        const total = data.length;
        const available = data.filter(c => (parseInt(c.quantity) || 0) > 0).length;
        const totalValue = data.reduce((sum, c) => sum + parseFloat(c.price || 0), 0);
        
        widgetsHTML = `
            ${buildWidget('fa-car', 'Total Cars', total)}
            ${buildWidget('fa-check-circle', 'Available', available, 'var(--success)')}
            ${buildWidget('fa-money-bill-wave', 'Total Value', '$' + totalValue.toLocaleString(), 'var(--secondary)')}
        `;
    } else if (tab === 'sales') {
        const totalSales = data.length;
        const totalRevenue = data.reduce((sum, s) => sum + parseFloat(s.amount || 0), 0);
        let totalProfit = 0;
        data.forEach(s => {
            const car = carsData.find(c => c.id === s.carId);
            if (car) totalProfit += (parseFloat(s.amount) - parseFloat(car.price));
        });

        widgetsHTML = `
            ${buildWidget('fa-file-invoice-dollar', 'Total Sales', totalSales)}
            ${buildWidget('fa-chart-line', 'Total Revenue', '$' + totalRevenue.toLocaleString(), 'var(--secondary)')}
            ${buildWidget('fa-arrow-trend-up', 'Total Profit', '$' + totalProfit.toLocaleString(), 'var(--success)')}
        `;
    } else if (tab === 'employees') {
        widgetsHTML = `${buildWidget('fa-user-tie', 'Active Employees', data.length)}`;
    } else if (tab === 'customers') {
        widgetsHTML = `${buildWidget('fa-users', 'Total Customers', data.length)}`;
    }

    widgetsContainer.innerHTML = widgetsHTML;
}

function buildWidget(icon, title, value, color = 'var(--primary)') {
    return `
        <div class="widget">
            <div class="widget-icon" style="color: ${color}; background: ${color}33;"><i class="fa-solid ${icon}"></i></div>
            <div class="widget-info">
                <h4>${title}</h4>
                <p>${value}</p>
            </div>
        </div>
    `;
}

function renderSalesChart(salesData, carsData, customersData) {
    const ctx = document.getElementById('salesChart').getContext('2d');
    
    const sortedSales = [...salesData].sort((a, b) => new Date(a.date || 0) - new Date(b.date || 0));

    const labels = [];
    const tooltipLabels = [];
    const revenueArr = [];
    const profitArr = [];

    let cumulativeRevenue = 0;
    let cumulativeProfit = 0;

    sortedSales.forEach((sale, index) => {
        const car = carsData.find(c => c.id === sale.carId);
        const customer = customersData.find(c => c.id === sale.customerId);
        
        const carName = car ? `${car.make} ${car.model}` : `Car ${sale.carId}`;
        const custName = customer ? customer.name : `Customer ${sale.customerId}`;
        
        const labelStr = sale.date ? sale.date : `Sale ${index + 1}`;
        labels.push(labelStr);
        tooltipLabels.push(`Sale #${sale.id}: ${carName} to ${custName}`);
        
        const rev = parseFloat(sale.amount || 0);
        cumulativeRevenue += rev;
        revenueArr.push(cumulativeRevenue);
        
        let currentSaleProfit = 0;
        if (car) {
            currentSaleProfit = rev - parseFloat(car.price || 0);
        }
        cumulativeProfit += currentSaleProfit;
        profitArr.push(cumulativeProfit);
    });

    if (chartInstance) chartInstance.destroy();

    Chart.defaults.color = '#94a3b8';
    Chart.defaults.font.family = "'Outfit', sans-serif";

    chartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Total Cumulative Revenue ($)',
                    data: revenueArr,
                    borderColor: '#ec4899',
                    backgroundColor: 'rgba(236, 72, 153, 0.1)',
                    borderWidth: 3,
                    tension: 0.5,
                    fill: true
                },
                {
                    label: 'Total Cumulative Profit ($)',
                    data: profitArr,
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    borderWidth: 3,
                    tension: 0.5,
                    fill: true
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'top' },
                tooltip: { 
                    mode: 'index', 
                    intersect: false,
                    callbacks: {
                        title: function(context) {
                            return tooltipLabels[context[0].dataIndex] + '\\nOn: ' + context[0].label;
                        }
                    }
                }
            },
            scales: {
                y: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.05)' } },
                x: { grid: { display: false } }
            }
        }
    });
}

function showModal(item = null) {
    editingId = item ? item.id : null;
    const fields = schemas[currentTab];
    const formFields = document.getElementById('form-fields');
    
    formFields.innerHTML = fields.map(f => {
        const value = item && item[f.name] ? item[f.name] : '';
        if (f.type === 'file') {
            return `
                <div class="form-group">
                    <label><i class="fa-solid fa-image"></i> ${f.label}</label>
                    <input type="file" name="${f.name}" accept="image/*">
                    ${item && item.imageUrl && item.imageUrl !== 'null' && item.imageUrl !== '' ? `<label class="checkbox-label"><input type="checkbox" name="removeImage" value="true"> Remove current picture</label>` : ''}
                </div>
            `;
        } else {
            return `
                <div class="form-group">
                    <label><i class="fa-solid ${f.icon || 'fa-info-circle'}"></i> ${f.label}</label>
                    <input type="text" name="${f.name}" value="${value}" required>
                </div>
            `;
        }
    }).join('');
    
    if (item && item.imageUrl) {
        formFields.innerHTML += `<input type="hidden" name="imageUrl" value="${item.imageUrl}">`;
    }
    
    document.getElementById('modal-title').innerText = item ? `Edit ${tabConfig[currentTab].title}` : `Add New ${tabConfig[currentTab].title}`;
    document.getElementById('modal-overlay').classList.add('active');
}

function editItem(item) {
    showModal(item);
}

async function deleteItem(id) {
    try {
        await fetch(`${apiBase}/${currentTab}/${id}`, { method: 'DELETE' });
        loadData(currentTab);
    } catch(e) {
        alert('Error deleting: ' + e.message);
    }
}

function closeModal() {
    editingId = null;
    document.getElementById('modal-overlay').classList.remove('active');
    document.getElementById('add-form').reset();
}
