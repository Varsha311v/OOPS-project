const $ = id => document.getElementById(id);
let editId = null, students = [], subjects = [];
const tabs = {students:'Students', subjects:'Subjects', mark:'Mark Attendance', report:'Student Report', low:'Below 75%'};
async function api(url, method='GET', body) {
  const r = await fetch('/api' + url, {method, headers:{'Content-Type':'application/json'}, body: body ? JSON.stringify(body) : undefined});
  if (!r.ok) { const e = await r.json().catch(()=>({})); $('msg').textContent = e.error || 'Request failed'; throw new Error('fail'); }
  $('msg').textContent = ''; return r.status === 204 ? null : r.json();
}
const esc = s => String(s ?? '').replace(/[&<>"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]));
function show(t) {
  document.querySelectorAll('section').forEach(s => s.classList.toggle('on', s.id === t));
  document.querySelectorAll('#tabs button').forEach(b => b.classList.toggle('on', b.dataset.t === t));
}
$('tabs').innerHTML = Object.entries(tabs).map(([k,v]) => `<button data-t="${k}" onclick="show('${k}')">${v}</button>`).join('');

async function loadStudents() {
  const q = $('sSearch').value;
  students = await api('/students' + (q ? '?q=' + encodeURIComponent(q) : ''));
  $('sBody').innerHTML = students.map(s => `<tr><td>${esc(s.rollNo)}</td><td>${esc(s.name)}</td><td>${esc(s.email)}</td><td>${esc(s.course)}</td>
    <td><button class="a" onclick="editStudent(${s.id})">Edit</button><button class="d" onclick="delStudent(${s.id})">Delete</button></td></tr>`).join('');
  if (!q) $('rStu').innerHTML = students.map(s => `<option value="${s.id}">${esc(s.rollNo)} - ${esc(s.name)}</option>`).join('');
}
async function saveStudent() {
  const s = {rollNo:$('sRoll').value.trim(), name:$('sName').value.trim(), email:$('sEmail').value.trim(), course:$('sCourse').value.trim()};
  if (!s.rollNo || !s.name) { $('msg').textContent = 'Roll no and name are required'; return; }
  await (editId ? api('/students/' + editId, 'PUT', s) : api('/students', 'POST', s));
  resetStudent(); loadStudents();
}
function editStudent(id) {
  const s = students.find(x => x.id === id); editId = id;
  $('sRoll').value = s.rollNo; $('sName').value = s.name; $('sEmail').value = s.email || ''; $('sCourse').value = s.course || '';
  $('sSave').textContent = 'Update student';
}
function resetStudent() { editId = null; ['sRoll','sName','sEmail','sCourse'].forEach(i => $(i).value = ''); $('sSave').textContent = 'Add student'; }
async function delStudent(id) { if (confirm('Delete student and all their attendance?')) { await api('/students/' + id, 'DELETE'); loadStudents(); } }

async function loadSubjects() {
  subjects = await api('/subjects');
  $('subBody').innerHTML = subjects.map(s => `<tr><td>${esc(s.code)}</td><td>${esc(s.name)}</td><td><button class="d" onclick="delSubject(${s.id})">Delete</button></td></tr>`).join('');
  const opts = subjects.map(s => `<option value="${s.id}">${esc(s.code)} - ${esc(s.name)}</option>`).join('');
  $('mSub').innerHTML = opts; $('lSub').innerHTML = '<option value="">All subjects (overall)</option>' + opts;
}
async function addSubject() {
  const code = $('subCode').value.trim(), name = $('subName').value.trim();
  if (!code || !name) return;
  await api('/subjects', 'POST', {code, name}); $('subCode').value = $('subName').value = ''; loadSubjects();
}
async function delSubject(id) { if (confirm('Delete subject and its attendance?')) { await api('/subjects/' + id, 'DELETE'); loadSubjects(); } }

async function loadMark() {
  const sub = $('mSub').value, date = $('mDate').value;
  if (!sub || !date) { $('msg').textContent = 'Choose subject and date'; return; }
  const all = await api('/students'), existing = await api(`/attendance?subjectId=${sub}&date=${date}`);
  const map = Object.fromEntries(existing.map(a => [a.studentId, a.status]));
  $('mBody').innerHTML = all.map(s => `<tr data-id="${s.id}"><td>${esc(s.rollNo)}</td><td>${esc(s.name)}</td>
    <td><input type="radio" name="r${s.id}" value="PRESENT" ${map[s.id] !== 'ABSENT' ? 'checked' : ''}></td>
    <td><input type="radio" name="r${s.id}" value="ABSENT" ${map[s.id] === 'ABSENT' ? 'checked' : ''}></td></tr>`).join('');
}
async function saveMark() {
  const sub = +$('mSub').value, date = $('mDate').value;
  const recs = [...$('mBody').querySelectorAll('tr')].map(tr => ({studentId:+tr.dataset.id, subjectId:sub, date,
    status: tr.querySelector('input:checked').value}));
  if (!recs.length) return;
  const r = await api('/attendance', 'POST', recs); alert('Saved ' + r.saved + ' records');
}
const pct = p => `<span class="${p < 75 ? 'low' : 'good'}">${p.toFixed(2)}%</span>`;
async function studentReport() {
  const id = $('rStu').value; if (!id) return;
  const rows = await api('/reports/student/' + id);
  $('rBody').innerHTML = rows.map(r => `<tr><td>${esc(r.subject)}</td><td>${r.total}</td><td>${r.present}</td><td>${pct(r.percentage)}</td></tr>`).join('') || '<tr><td colspan=4>No attendance yet</td></tr>';
}
async function loadLow() {
  const sub = $('lSub').value;
  const rows = await api(`/reports/low?threshold=${$('lTh').value}` + (sub ? '&subjectId=' + sub : ''));
  $('lBody').innerHTML = rows.map(r => `<tr><td>${esc(r.rollNo)}</td><td>${esc(r.name)}</td><td>${esc(r.subject)}</td><td>${r.total}</td><td>${r.present}</td><td>${pct(r.percentage)}</td></tr>`).join('') || '<tr><td colspan=6>No students below threshold</td></tr>';
}
$('mDate').value = new Date().toISOString().slice(0, 10);
show('students'); loadStudents(); loadSubjects();
