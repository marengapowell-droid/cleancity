const path = require('path');
const express = require('express');
const cors = require('cors');
const { DatabaseSync } = require('node:sqlite');

const PORT = process.env.PORT || 3000;
const ROOT = path.join(__dirname, '..');

const dbPath = path.join(__dirname, 'cleancity.db');
const db = new DatabaseSync(dbPath);

db.exec(`
CREATE TABLE IF NOT EXISTS residents (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  phone TEXT,
  area TEXT,
  password TEXT NOT NULL,
  joined TEXT
);
CREATE TABLE IF NOT EXISTS collectors (
  id INTEGER PRIMARY KEY,
  name TEXT NOT NULL,
  email TEXT NOT NULL,
  phone TEXT,
  zone TEXT,
  password TEXT NOT NULL,
  joined TEXT
);
CREATE TABLE IF NOT EXISTS reports (
  id TEXT PRIMARY KEY,
  type TEXT,
  location TEXT,
  desc TEXT,
  status TEXT,
  date TEXT,
  user_name TEXT,
  user_email TEXT
);
CREATE TABLE IF NOT EXISTS pickups (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  type TEXT,
  location TEXT,
  date TEXT,
  time TEXT,
  status TEXT,
  user_email TEXT,
  user_name TEXT,
  area TEXT,
  collected_by INTEGER,
  collected_by_name TEXT,
  collected_at TEXT
);
`);

function rowResident(r) {
  return { id: r.id, name: r.name, email: r.email, phone: r.phone || '', area: r.area || '', joined: r.joined || '' };
}

function rowCollector(r) {
  return { id: r.id, name: r.name, email: r.email, phone: r.phone || '', zone: r.zone || '', joined: r.joined || '' };
}

function rowReport(r) {
  return {
    id: r.id,
    type: r.type,
    location: r.location,
    desc: r.desc || '',
    status: r.status,
    date: r.date,
    user: r.user_name,
    userEmail: r.user_email
  };
}

function rowPickup(r) {
  return {
    id: r.id,
    type: r.type,
    location: r.location,
    date: r.date,
    time: r.time,
    status: r.status,
    userEmail: r.user_email,
    userName: r.user_name,
    area: r.area || '',
    collectedBy: r.collected_by,
    collectedByName: r.collected_by_name || '',
    collectedAt: r.collected_at || ''
  };
}

const app = express();
app.use(cors());
app.use(express.json());

app.get('/api/data', (req, res) => {
  const residents = db.prepare('SELECT id, name, email, phone, area, joined FROM residents ORDER BY id').all();
  const collectors = db.prepare('SELECT id, name, email, phone, zone, joined FROM collectors ORDER BY id').all();
  const reports = db.prepare('SELECT * FROM reports ORDER BY rowid ASC').all().map(rowReport);
  const pickups = db.prepare('SELECT * FROM pickups ORDER BY id ASC').all().map(rowPickup);
  res.json({ residents, collectors, reports, pickups });
});

app.post('/api/residents', (req, res) => {
  const { name, email, phone, area, password } = req.body || {};
  if (!name || !email || !password) return res.status(400).json({ error: 'Missing fields' });
  const joined = new Date().toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
  try {
    const info = db
      .prepare('INSERT INTO residents (name, email, phone, area, password, joined) VALUES (?,?,?,?,?,?)')
      .run(name, email, phone || '', area || '', password, joined);
    const user = rowResident(
      db.prepare('SELECT id, name, email, phone, area, joined FROM residents WHERE id = ?').get(info.lastInsertRowid)
    );
    res.json({ user });
  } catch (e) {
    if (String(e.message).includes('UNIQUE')) return res.status(400).json({ error: 'Email already registered' });
    throw e;
  }
});

app.post('/api/residents/login', (req, res) => {
  const { email, password } = req.body || {};
  const r = db.prepare('SELECT id, name, email, phone, area, joined FROM residents WHERE email = ? AND password = ?').get(email, password);
  if (!r) return res.status(401).json({ error: 'Invalid email or password' });
  res.json({ user: rowResident(r) });
});

const MAX_COLLECTORS = 7;

app.post('/api/collectors', (req, res) => {
  const count = db.prepare('SELECT COUNT(*) AS c FROM collectors').get().c;
  if (count >= MAX_COLLECTORS) return res.status(400).json({ error: 'Collector roster is full (7 maximum).' });
  const { name, email, phone, zone, password } = req.body || {};
  if (!name || !email || !password) return res.status(400).json({ error: 'Missing fields' });
  const next = db.prepare('SELECT COALESCE(MAX(id), 200) + 1 AS nid FROM collectors').get().nid;
  const joined = new Date().toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
  db.prepare('INSERT INTO collectors (id, name, email, phone, zone, password, joined) VALUES (?,?,?,?,?,?,?)').run(
    next,
    name,
    email,
    phone || '',
    zone || '',
    password,
    joined
  );
  const user = rowCollector(db.prepare('SELECT id, name, email, phone, zone, joined FROM collectors WHERE id = ?').get(next));
  res.json({ user });
});

app.post('/api/collectors/login', (req, res) => {
  const id = Number(req.body?.id);
  const password = req.body?.password;
  const r = db.prepare('SELECT id, name, email, phone, zone, joined FROM collectors WHERE id = ? AND password = ?').get(id, password);
  if (!r) return res.status(401).json({ error: 'Invalid ID or password' });
  res.json({ user: rowCollector(r) });
});

app.post('/api/pickups', (req, res) => {
  const { type, location, date, time, userEmail, userName, area } = req.body || {};
  if (!type || !location || !date || !time || !userEmail) return res.status(400).json({ error: 'Missing fields' });
  const info = db
    .prepare(
      `INSERT INTO pickups (type, location, date, time, status, user_email, user_name, area)
       VALUES (?,?,?,?,?,?,?,?)`
    )
    .run(type, location, date, time, 'SCHEDULED', userEmail, userName || '', area || '');
  const row = db.prepare('SELECT * FROM pickups WHERE id = ?').get(info.lastInsertRowid);
  res.json({ pickup: rowPickup(row) });
});

app.post('/api/reports', (req, res) => {
  const { type, location, desc, user, userEmail } = req.body || {};
  if (!type || !location || !user || !userEmail) return res.status(400).json({ error: 'Missing fields' });
  const n = db.prepare('SELECT COUNT(*) AS c FROM reports').get().c;
  const id = 'RPT-' + (n + 101);
  const date = new Date().toLocaleDateString();
  db.prepare(
    'INSERT INTO reports (id, type, location, desc, status, date, user_name, user_email) VALUES (?,?,?,?,?,?,?,?)'
  ).run(id, type, location, desc || '', 'PENDING', date, user, userEmail);
  const row = db.prepare('SELECT * FROM reports WHERE id = ?').get(id);
  res.json({ report: rowReport(row) });
});

app.patch('/api/reports/:id/resolve', (req, res) => {
  const id = req.params.id;
  const row = db.prepare('SELECT * FROM reports WHERE id = ?').get(id);
  if (!row) return res.status(404).json({ error: 'Report not found' });
  if (row.status !== 'PENDING') return res.status(400).json({ error: 'Report is not pending' });
  db.prepare('UPDATE reports SET status = ? WHERE id = ?').run('RESOLVED', id);
  const updated = db.prepare('SELECT * FROM reports WHERE id = ?').get(id);
  res.json({ report: rowReport(updated) });
});

app.patch('/api/pickups/:id/confirm', (req, res) => {
  const id = Number(req.params.id);
  const { collectorId, collectorName } = req.body || {};
  const row = db.prepare('SELECT * FROM pickups WHERE id = ?').get(id);
  if (!row || row.status !== 'SCHEDULED') return res.status(400).json({ error: 'Invalid pickup' });
  const collectedAt = new Date().toLocaleString();
  db.prepare('UPDATE pickups SET status = ?, collected_by = ?, collected_by_name = ?, collected_at = ? WHERE id = ?').run(
    'COLLECTED',
    collectorId,
    collectorName || '',
    collectedAt,
    id
  );
  const updated = db.prepare('SELECT * FROM pickups WHERE id = ?').get(id);
  res.json({ pickup: rowPickup(updated) });
});

app.delete('/api/collectors', (req, res) => {
  db.prepare('DELETE FROM collectors').run();
  res.json({ ok: true });
});

app.use(express.static(ROOT));

app.listen(PORT, () => {
  console.log(`CleanCity API + SQLite at ${dbPath}`);
  console.log(`http://localhost:${PORT}/cleancity%20(1).html`);
});
