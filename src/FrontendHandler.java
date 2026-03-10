import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class FrontendHandler implements HttpHandler {

    // Full HTML embedded as a constant — no filesystem dependency, works on Railway/Docker/local
    private static final String HTML = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Library Management System</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: 'Segoe UI', system-ui, sans-serif;
      background: #0f172a;
      color: #e2e8f0;
      min-height: 100vh;
    }
    header {
      background: linear-gradient(135deg, #1e3a5f, #0f4c75);
      padding: 20px 32px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      box-shadow: 0 4px 20px rgba(0,0,0,0.4);
    }
    header h1 { font-size: 1.6rem; font-weight: 700; color: #22d3ee; letter-spacing: 0.5px; }
    header h1 span { color: #e2e8f0; font-weight: 400; font-size: 1rem; margin-left: 8px; }
    #healthBadge {
      display: inline-flex; align-items: center; gap: 6px;
      padding: 6px 14px; border-radius: 999px; font-size: 0.82rem; font-weight: 600;
      background: #1e293b; border: 1px solid #334155; transition: all 0.3s;
    }
    #healthBadge.healthy  { border-color: #10b981; color: #10b981; }
    #healthBadge.unhealthy{ border-color: #ef4444; color: #ef4444; }
    #healthBadge .dot {
      width: 8px; height: 8px; border-radius: 50%;
      background: currentColor; animation: pulse 2s infinite;
    }
    @keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.4} }
    main { max-width: 1200px; margin: 0 auto; padding: 28px 24px; }
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px; margin-bottom: 28px;
    }
    .stat-card {
      background: #1e293b; border: 1px solid #334155; border-radius: 12px;
      padding: 20px; text-align: center; transition: transform 0.2s, border-color 0.2s;
    }
    .stat-card:hover { transform: translateY(-2px); border-color: #22d3ee; }
    .stat-card .value { font-size: 2.4rem; font-weight: 700; color: #22d3ee; line-height: 1; }
    .stat-card .label { font-size: 0.8rem; color: #94a3b8; margin-top: 6px; text-transform: uppercase; letter-spacing: 0.5px; }
    .tabs { display: flex; gap: 4px; margin-bottom: 20px; }
    .tab-btn {
      padding: 10px 24px; border: none; border-radius: 8px 8px 0 0; cursor: pointer;
      font-size: 0.9rem; font-weight: 600; background: #1e293b; color: #94a3b8;
      border-bottom: 2px solid transparent; transition: all 0.2s;
    }
    .tab-btn.active { color: #22d3ee; border-bottom-color: #22d3ee; background: #243449; }
    .tab-btn:hover:not(.active) { color: #e2e8f0; }
    .tab-panel { display: none; }
    .tab-panel.active { display: block; }
    .panel { background: #1e293b; border: 1px solid #334155; border-radius: 0 12px 12px 12px; padding: 20px; }
    .toolbar { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 16px; align-items: center; }
    .toolbar input, .toolbar select {
      background: #0f172a; border: 1px solid #475569; color: #e2e8f0;
      border-radius: 8px; padding: 8px 12px; font-size: 0.88rem; outline: none; transition: border-color 0.2s;
    }
    .toolbar input:focus, .toolbar select:focus { border-color: #22d3ee; }
    .toolbar input { width: 220px; }
    .toolbar select { min-width: 140px; }
    .btn {
      padding: 8px 16px; border: none; border-radius: 8px; cursor: pointer;
      font-size: 0.85rem; font-weight: 600; transition: opacity 0.2s, transform 0.1s;
    }
    .btn:active { transform: scale(0.97); }
    .btn:hover { opacity: 0.88; }
    .btn-primary   { background: #22d3ee; color: #0f172a; }
    .btn-success   { background: #10b981; color: #fff; }
    .btn-danger    { background: #ef4444; color: #fff; }
    .btn-warning   { background: #f59e0b; color: #0f172a; }
    .btn-secondary { background: #334155; color: #e2e8f0; }
    .btn-sm { padding: 5px 10px; font-size: 0.78rem; }
    .ml-auto { margin-left: auto; }
    .table-wrap { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
    th {
      background: #0f172a; color: #94a3b8; text-transform: uppercase;
      font-size: 0.72rem; letter-spacing: 0.5px; padding: 10px 12px;
      text-align: left; white-space: nowrap;
    }
    td { padding: 11px 12px; border-top: 1px solid #1e293b; }
    tr:hover td { background: #243449; }
    .actions { display: flex; gap: 6px; }
    .badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 0.75rem; font-weight: 600; }
    .badge-available { background: rgba(16,185,129,0.15); color: #10b981; border: 1px solid #10b981; }
    .badge-checked   { background: rgba(239,68,68,0.15);  color: #ef4444;  border: 1px solid #ef4444; }
    .empty-state { text-align: center; padding: 48px 20px; color: #475569; }
    .empty-state .icon { font-size: 3rem; margin-bottom: 12px; }
    .modal-overlay {
      display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.7);
      z-index: 1000; align-items: center; justify-content: center;
    }
    .modal-overlay.open { display: flex; }
    .modal {
      background: #1e293b; border: 1px solid #334155; border-radius: 16px;
      padding: 28px; width: 420px; max-width: 90vw; box-shadow: 0 20px 60px rgba(0,0,0,0.5);
    }
    .modal h3 { color: #22d3ee; font-size: 1.1rem; margin-bottom: 20px; }
    .form-group { margin-bottom: 16px; }
    .form-group label { display: block; font-size: 0.82rem; color: #94a3b8; margin-bottom: 6px; font-weight: 500; }
    .form-group input {
      width: 100%; background: #0f172a; border: 1px solid #475569;
      color: #e2e8f0; border-radius: 8px; padding: 10px 12px; font-size: 0.9rem;
      outline: none; transition: border-color 0.2s;
    }
    .form-group input:focus { border-color: #22d3ee; }
    .modal-actions { display: flex; gap: 10px; justify-content: flex-end; margin-top: 20px; }
    #toastContainer { position: fixed; bottom: 24px; right: 24px; display: flex; flex-direction: column; gap: 10px; z-index: 2000; }
    .toast {
      padding: 12px 18px; border-radius: 10px; font-size: 0.88rem; font-weight: 500;
      max-width: 320px; box-shadow: 0 8px 24px rgba(0,0,0,0.4);
      animation: slideIn 0.3s ease; border-left: 4px solid;
    }
    .toast.success { background: #0d2b22; color: #10b981; border-color: #10b981; }
    .toast.error   { background: #2b0d0d; color: #ef4444; border-color: #ef4444; }
    .toast.info    { background: #0d1f2b; color: #22d3ee; border-color: #22d3ee; }
    @keyframes slideIn { from { transform: translateX(120%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
  </style>
</head>
<body>

<header>
  <h1>&#x1F4DA; Library Management System <span>v2.0</span></h1>
  <div id="healthBadge"><span class="dot"></span> Checking&hellip;</div>
</header>

<main>
  <div class="stats-grid">
    <div class="stat-card">
      <div class="value" id="statTotal">&mdash;</div>
      <div class="label">Total Books</div>
    </div>
    <div class="stat-card">
      <div class="value" id="statAvailable" style="color:#10b981">&mdash;</div>
      <div class="label">Available</div>
    </div>
    <div class="stat-card">
      <div class="value" id="statCheckedOut" style="color:#ef4444">&mdash;</div>
      <div class="label">Checked Out</div>
    </div>
    <div class="stat-card">
      <div class="value" id="statBorrows" style="color:#f59e0b">&mdash;</div>
      <div class="label">Total Borrows</div>
    </div>
  </div>

  <div class="tabs">
    <button class="tab-btn active" onclick="switchTab('books', this)">&#x1F4D6; Books</button>
    <button class="tab-btn" onclick="switchTab('history', this)">&#x1F551; Borrow History</button>
  </div>

  <div id="tab-books" class="tab-panel active">
    <div class="panel">
      <div class="toolbar">
        <input id="searchInput" type="text" placeholder="Search by title or author&hellip;" oninput="filterBooks()" />
        <select id="genreFilter" onchange="filterBooks()">
          <option value="">All Genres</option>
        </select>
        <select id="availFilter" onchange="filterBooks()">
          <option value="">All Status</option>
          <option value="available">Available</option>
          <option value="checked">Checked Out</option>
        </select>
        <div class="ml-auto" style="display:flex;gap:8px">
          <button class="btn btn-primary" onclick="openAddModal()">&#xff0b; Add Book</button>
          <button class="btn btn-secondary" onclick="loadAll()">&#x21bb; Refresh</button>
        </div>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr><th>ID</th><th>Title</th><th>Author</th><th>Genre</th><th>Status</th><th>Actions</th></tr>
          </thead>
          <tbody id="booksBody"></tbody>
        </table>
        <div id="booksEmpty" class="empty-state" style="display:none">
          <div class="icon">&#x1F4ED;</div>
          <div>No books found</div>
        </div>
      </div>
    </div>
  </div>

  <div id="tab-history" class="tab-panel">
    <div class="panel">
      <div class="table-wrap">
        <table>
          <thead>
            <tr><th>#</th><th>Book ID</th><th>Title</th><th>Borrower</th><th>Borrowed At</th><th>Returned At</th></tr>
          </thead>
          <tbody id="historyBody"></tbody>
        </table>
        <div id="historyEmpty" class="empty-state" style="display:none">
          <div class="icon">&#x1F551;</div>
          <div>No borrow history yet</div>
        </div>
      </div>
    </div>
  </div>
</main>

<div class="modal-overlay" id="addModal">
  <div class="modal">
    <h3>&#xff0b; Add New Book</h3>
    <div class="form-group">
      <label>Title *</label>
      <input id="newTitle" type="text" placeholder="e.g. Clean Code" />
    </div>
    <div class="form-group">
      <label>Author *</label>
      <input id="newAuthor" type="text" placeholder="e.g. Robert C. Martin" />
    </div>
    <div class="form-group">
      <label>Genre *</label>
      <input id="newGenre" type="text" placeholder="e.g. Technology" />
    </div>
    <div class="modal-actions">
      <button class="btn btn-secondary" onclick="closeModal('addModal')">Cancel</button>
      <button class="btn btn-primary" onclick="addBook()">Add Book</button>
    </div>
  </div>
</div>

<div class="modal-overlay" id="checkoutModal">
  <div class="modal">
    <h3>&#x1F4E4; Checkout Book</h3>
    <p style="color:#94a3b8;font-size:0.88rem;margin-bottom:16px" id="checkoutBookTitle"></p>
    <div class="form-group">
      <label>Borrower Name *</label>
      <input id="borrowerName" type="text" placeholder="e.g. John Doe" />
    </div>
    <div class="modal-actions">
      <button class="btn btn-secondary" onclick="closeModal('checkoutModal')">Cancel</button>
      <button class="btn btn-success" onclick="confirmCheckout()">Checkout</button>
    </div>
  </div>
</div>

<div id="toastContainer"></div>

<script>
  var allBooks = [];
  var checkoutBookId = null;
  var BASE = window.location.origin;

  document.addEventListener('DOMContentLoaded', loadAll);

  function loadAll() {
    return Promise.all([checkHealth(), loadStats(), loadBooks(), loadHistory()]);
  }

  function checkHealth() {
    return fetch(BASE + '/health')
      .then(function(res) { return res.json().then(function(data) { return { ok: res.ok, data: data }; }); })
      .then(function(r) {
        var badge = document.getElementById('healthBadge');
        var ok = r.ok && r.data.status === 'healthy';
        badge.className = ok ? 'healthy' : 'unhealthy';
        badge.innerHTML = '<span class="dot"></span> ' + (ok ? 'API Healthy' : 'API Unhealthy');
      })
      .catch(function() {
        var badge = document.getElementById('healthBadge');
        badge.className = 'unhealthy';
        badge.innerHTML = '<span class="dot"></span> Unreachable';
      });
  }

  function loadStats() {
    return fetch(BASE + '/books/stats')
      .then(function(res) { return res.json(); })
      .then(function(data) {
        var s = data.data || {};
        document.getElementById('statTotal').textContent      = s.totalBooks      != null ? s.totalBooks      : '—';
        document.getElementById('statAvailable').textContent  = s.availableBooks  != null ? s.availableBooks  : '—';
        document.getElementById('statCheckedOut').textContent = s.checkedOutBooks != null ? s.checkedOutBooks : '—';
        document.getElementById('statBorrows').textContent    = s.totalBorrows    != null ? s.totalBorrows    : '—';
      })
      .catch(function() {});
  }

  function loadBooks() {
    return fetch(BASE + '/books?size=100')
      .then(function(res) { return res.json(); })
      .then(function(data) {
        allBooks = data.data || [];
        populateGenreFilter();
        renderBooks(allBooks);
      })
      .catch(function(e) { showToast('Failed to load books: ' + e.message, 'error'); });
  }

  function loadHistory() {
    return fetch(BASE + '/books/history')
      .then(function(res) { return res.json(); })
      .then(function(data) {
        var records = data.data || [];
        var tbody = document.getElementById('historyBody');
        var empty = document.getElementById('historyEmpty');
        if (!records.length) {
          tbody.innerHTML = '';
          empty.style.display = 'block';
          return;
        }
        empty.style.display = 'none';
        tbody.innerHTML = records.map(function(r, i) {
          return '<tr>' +
            '<td style="color:#94a3b8">' + (i + 1) + '</td>' +
            '<td>' + r.bookId + '</td>' +
            '<td>' + esc(r.bookTitle || '') + '</td>' +
            '<td>' + esc(r.borrowerName || '') + '</td>' +
            '<td style="color:#94a3b8">' + esc(r.borrowedAt || '') + '</td>' +
            '<td style="color:#94a3b8">' + (r.returnedAt ? esc(r.returnedAt) : '<span style="color:#f59e0b">Active</span>') + '</td>' +
            '</tr>';
        }).join('');
      })
      .catch(function() {});
  }

  function populateGenreFilter() {
    var genres = [];
    allBooks.forEach(function(b) { if (b.genre && genres.indexOf(b.genre) === -1) genres.push(b.genre); });
    genres.sort();
    var sel = document.getElementById('genreFilter');
    var current = sel.value;
    sel.innerHTML = '<option value="">All Genres</option>';
    genres.forEach(function(g) {
      var opt = document.createElement('option');
      opt.value = g; opt.textContent = g;
      if (g === current) opt.selected = true;
      sel.appendChild(opt);
    });
  }

  function filterBooks() {
    var q     = document.getElementById('searchInput').value.toLowerCase();
    var genre = document.getElementById('genreFilter').value;
    var avail = document.getElementById('availFilter').value;
    var filtered = allBooks.filter(function(b) {
      var matchSearch = !q || b.title.toLowerCase().indexOf(q) !== -1 || b.author.toLowerCase().indexOf(q) !== -1;
      var matchGenre  = !genre || b.genre === genre;
      var matchAvail  = !avail
        || (avail === 'available' && b.available)
        || (avail === 'checked'   && !b.available);
      return matchSearch && matchGenre && matchAvail;
    });
    renderBooks(filtered);
  }

  function renderBooks(books) {
    var tbody = document.getElementById('booksBody');
    var empty = document.getElementById('booksEmpty');
    if (!books.length) {
      tbody.innerHTML = '';
      empty.style.display = 'block';
      return;
    }
    empty.style.display = 'none';
    tbody.innerHTML = books.map(function(b) {
      var statusBadge = b.available
        ? '<span class="badge badge-available">&#x2713; Available</span>'
        : '<span class="badge badge-checked">&#x2717; Checked Out</span>';
      var actionBtn = b.available
        ? '<button class="btn btn-sm btn-success" onclick="openCheckoutModal(' + b.id + ')">Checkout</button>'
        : '<button class="btn btn-sm btn-warning" onclick="returnBook(' + b.id + ')">Return</button>';
      return '<tr>' +
        '<td style="color:#94a3b8">' + b.id + '</td>' +
        '<td style="font-weight:600">' + esc(b.title) + '</td>' +
        '<td>' + esc(b.author) + '</td>' +
        '<td><span style="color:#94a3b8">' + esc(b.genre) + '</span></td>' +
        '<td>' + statusBadge + '</td>' +
        '<td><div class="actions">' + actionBtn +
          '<button class="btn btn-sm btn-danger" onclick="deleteBook(' + b.id + ')">Delete</button>' +
        '</div></td>' +
        '</tr>';
    }).join('');
  }

  function openAddModal() {
    document.getElementById('newTitle').value = '';
    document.getElementById('newAuthor').value = '';
    document.getElementById('newGenre').value = '';
    openModal('addModal');
  }

  function addBook() {
    var title  = document.getElementById('newTitle').value.trim();
    var author = document.getElementById('newAuthor').value.trim();
    var genre  = document.getElementById('newGenre').value.trim();
    if (!title || !author || !genre) { showToast('All fields are required.', 'error'); return; }
    fetch(BASE + '/books', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title: title, author: author, genre: genre })
    })
    .then(function(res) { return res.json().then(function(data) { return { ok: res.ok, data: data }; }); })
    .then(function(r) {
      if (r.ok) {
        closeModal('addModal');
        showToast('Book added successfully!', 'success');
        loadAll();
      } else {
        showToast(r.data.message || 'Failed to add book.', 'error');
      }
    })
    .catch(function(e) { showToast('Error: ' + e.message, 'error'); });
  }

  function deleteBook(id) {
    if (!confirm('Delete this book?')) return;
    fetch(BASE + '/books/' + id, { method: 'DELETE' })
    .then(function(res) { return res.json().then(function(data) { return { ok: res.ok, data: data }; }); })
    .then(function(r) {
      if (r.ok) {
        showToast('Book deleted.', 'success');
        loadAll();
      } else {
        showToast(r.data.message || 'Failed to delete.', 'error');
      }
    })
    .catch(function(e) { showToast('Error: ' + e.message, 'error'); });
  }

  function openCheckoutModal(id) {
    checkoutBookId = id;
    var book = allBooks.find(function(b) { return b.id === id; });
    document.getElementById('checkoutBookTitle').textContent = book ? '"' + book.title + '"' : '';
    document.getElementById('borrowerName').value = '';
    openModal('checkoutModal');
  }

  function confirmCheckout() {
    var name = document.getElementById('borrowerName').value.trim();
    if (!name) { showToast('Borrower name is required.', 'error'); return; }
    fetch(BASE + '/books/' + checkoutBookId + '/borrow', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ borrowerName: name })
    })
    .then(function(res) { return res.json().then(function(data) { return { ok: res.ok, data: data }; }); })
    .then(function(r) {
      if (r.ok) {
        closeModal('checkoutModal');
        showToast('Book checked out to ' + name + '!', 'success');
        loadAll();
      } else {
        showToast(r.data.message || 'Failed to checkout.', 'error');
      }
    })
    .catch(function(e) { showToast('Error: ' + e.message, 'error'); });
  }

  function returnBook(id) {
    fetch(BASE + '/books/' + id + '/return', { method: 'POST' })
    .then(function(res) { return res.json().then(function(data) { return { ok: res.ok, data: data }; }); })
    .then(function(r) {
      if (r.ok) {
        showToast('Book returned successfully!', 'success');
        loadAll();
      } else {
        showToast(r.data.message || 'Failed to return.', 'error');
      }
    })
    .catch(function(e) { showToast('Error: ' + e.message, 'error'); });
  }

  function switchTab(name, btn) {
    document.querySelectorAll('.tab-panel').forEach(function(p) { p.classList.remove('active'); });
    document.querySelectorAll('.tab-btn').forEach(function(b) { b.classList.remove('active'); });
    document.getElementById('tab-' + name).classList.add('active');
    btn.classList.add('active');
    if (name === 'history') loadHistory();
  }

  function openModal(id) { document.getElementById(id).classList.add('open'); }
  function closeModal(id) { document.getElementById(id).classList.remove('open'); }

  function showToast(msg, type) {
    var c = document.getElementById('toastContainer');
    var t = document.createElement('div');
    t.className = 'toast ' + (type || 'info');
    t.textContent = msg;
    c.appendChild(t);
    setTimeout(function() { t.style.animation = 'none'; t.style.opacity = '0'; t.style.transition = 'opacity 0.4s'; setTimeout(function() { c.removeChild(t); }, 400); }, 3500);
  }

  function esc(s) {
    return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#x27;');
  }
</script>
</body>
</html>
""";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        byte[] bytes = HTML.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
        Logger.log(exchange.getRequestMethod(), exchange.getRequestURI().getPath(), 200);
    }
}