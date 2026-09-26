const $ = (selector) => document.querySelector(selector);
const esc = (value) => String(value ?? '').replace(/[&<>"']/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]));
const json = (value) => JSON.stringify(value, null, 2);
const shortId = (id) => id ? id.slice(0, 8) : '—';
const clock = (date) => date ? new Date(date).toLocaleTimeString() : '—';
const succeeded = (run) => run.status>=200 && run.status<300 && !['FAILED','FAILURE','ERROR'].includes(String(run.response?.status||'').toUpperCase());
const scenarios = [
  {id:'bank-a', title:'Bank A purchase', description:'Route a 111111 card through the full PSP → Shaparak → Bank A flow.', service:'psp', body:{sourceCardNumber:'1111111234567890',merchantNumber:'merchant-1001',amount:25000}, hint:'Requires merchant-1001 and matching Bank A source/destination accounts.'},
  {id:'bank-b', title:'Bank B purchase', description:'Route a 222222 card through the full PSP → Shaparak → Bank B flow.', service:'psp', body:{sourceCardNumber:'2222221234567890',merchantNumber:'merchant-1001',amount:25000}, hint:'Requires the merchant and Bank B sample accounts from BankB/README.md.'},
  {id:'bad-card', title:'Invalid card number', description:'Trigger PSP validation before the purchase reaches messaging.', service:'psp', body:{sourceCardNumber:'123',merchantNumber:'merchant-1001',amount:25000}, hint:'Expected: validation error. No purchase record should be created.'},
  {id:'low-amount', title:'Below minimum amount', description:'Exercise PSP’s minimum purchase amount of 1,000.', service:'psp', body:{sourceCardNumber:'1111111234567890',merchantNumber:'merchant-1001',amount:500}, hint:'Expected: validation error. The backend may return its own error format.'},
  {id:'merchant', title:'Unknown merchant', description:'Pass validation, then fail the merchant lookup in PSP.', service:'psp', body:{sourceCardNumber:'1111111234567890',merchantNumber:'merchant-does-not-exist',amount:25000}, hint:'Expected: merchant lookup failure; inspect the exact backend response.'},
  {id:'balance', title:'Insufficient balance', description:'Call Bank B directly with an amount above the sample source balance.', service:'bankB', body:{sourceCardNumber:'2222221234567890',targetAccountNumber:'333333333333',amount:999999999}, hint:'Direct bank test: bypasses PSP and RabbitMQ. Requires Bank B sample accounts.'}
];
const state = {scenario:scenarios[0], executions:[], logs:[], system:null, selected:null, logHiddenBefore:0, view:'overview'};

async function api(path, options) {
  const response = await fetch('/api' + path, {cache:'no-store', ...options});
  const body = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(body.error || `HTTP ${response.status}`);
  return body;
}
function toast(message, error=false) {
  const element = $('#toast'); element.textContent = message; element.className = 'toast show' + (error?' error':'');
  clearTimeout(toast.timer); toast.timer = setTimeout(() => element.className='toast', 3200);
}
function navigate(view) {
  state.view = view;
  document.querySelectorAll('.view').forEach(el => el.classList.toggle('active', el.id === 'view-'+view));
  document.querySelectorAll('.nav-link').forEach(el => el.classList.toggle('active', el.dataset.view === view));
  $('#breadcrumb-view').textContent = ({overview:'Overview',scenarios:'Scenarios',playground:'Playground',activity:'Transactions',logs:'Activity logs',system:'System'})[view];
  window.scrollTo({top:0,behavior:'smooth'});
}
function statusCard(name, status, detail) {
  const level = ['healthy','unavailable'].includes(status) ? status : 'unknown';
  return `<div class="status-card"><div class="status-card-top"><span>${esc(name)}</span><span class="status-pill ${level}">${level.toUpperCase()}</span></div><strong>${esc(detail)}</strong></div>`;
}
function renderSystem() {
  const services = state.system?.services || {};
  const cards = [
    ['PSP API',services.psp,'localhost:8084'],['Shaparak',services.shaparak,'localhost:8082'],
    ['Bank A',services.bankA,'localhost:8080'],['Bank B',services.bankB,'localhost:8081'],
    ['MongoDB',services.mongodb,'localhost:27017'],['MySQL / Bank A',services.mysqlBankA,'bank_a schema'],['MySQL / Bank B',services.mysqlBankB,'bank_b schema'],
    ['RabbitMQ',services.rabbitmq,'localhost:5672']
  ];
  $('#overview-status').innerHTML = cards.slice(0,4).map(x=>statusCard(...x)).join('');
  $('#system-status').innerHTML = cards.map(x=>statusCard(...x)).join('');
  const at = state.system?.checkedAt;
  $('#snapshot-time').textContent = at ? `Checked ${clock(at)}` : 'Could not check';
  $('#system-time').textContent = $('#snapshot-time').textContent;
  $('#last-check').textContent = at ? `Updated ${clock(at)}` : 'Runtime unavailable';
  const up = cards.filter(x=>x[1]==='healthy').length;
  $('#sidebar-status').textContent = `${up}/${cards.length} services reachable`;
  $('#sidebar-dot').className = 'live-dot ' + (up===cards.length ? 'good' : up===0 ? 'bad' : '');
}
function renderScenarios() {
  $('#scenario-grid').innerHTML = scenarios.map((item,i)=>`<button class="scenario-card ${state.scenario.id===item.id?'active':''}" data-scenario="${esc(item.id)}"><span class="scenario-number">${String(i+1).padStart(2,'0')} / ${esc(item.service==='psp'?'FULL FLOW':'DIRECT BANK')}</span><strong>${esc(item.title)}</strong><small>${esc(item.description)}</small></button>`).join('');
  document.querySelectorAll('[data-scenario]').forEach(button => button.addEventListener('click',()=>selectScenario(button.dataset.scenario)));
}
function selectScenario(id) {
  state.scenario = scenarios.find(x=>x.id===id) || scenarios[0];
  renderScenarios();
  $('#scenario-title').textContent = state.scenario.title;
  $('#scenario-endpoint').textContent = 'POST ' + (state.scenario.service==='psp'?'/payment/purchase':state.scenario.service==='bankA'?'/bank/bank1':'/bank/bank2');
  $('#scenario-hint').textContent = state.scenario.hint;
  $('#scenario-json').value = json(state.scenario.body);
}
function resultMarkup(run) {
  const status = Number(run.status);
  const label = status===0?'UNAVAILABLE':succeeded(run)?'SUCCESS':'FAILED';
  const level = succeeded(run)?'healthy':'unavailable';
  return `<div class="panel result-panel"><div class="result-heading"><h2>Execution result</h2><div class="result-meta"><span class="status-pill ${level}">${label}</span><span class="meta-chip">HTTP ${status||'—'}</span><span class="meta-chip">${esc(run.durationMs)} ms</span></div></div><div class="json-grid"><div class="json-block"><span>REQUEST · ${esc(run.method)} ${esc(new URL(run.url).pathname)}</span><pre>${esc(json(run.request))}</pre></div><div class="json-block"><span>RESPONSE</span><pre>${esc(typeof run.response==='string'?run.response:json(run.response))}</pre></div></div><div class="result-footer"><span>Trace ${esc(run.id)}</span><button class="text-button" data-inspect="${esc(run.id)}">Inspect persistence →</button></div></div>`;
}
async function run(service, editor, resultTarget, button) {
  let body;
  try { body = JSON.parse($(editor).value); if (!body || Array.isArray(body) || typeof body!=='object') throw Error('Request must be a JSON object'); }
  catch(error) { toast('Invalid JSON: '+error.message,true); $(editor).focus(); return; }
  button.disabled = true; const original = button.innerHTML; button.textContent = 'Running…';
  $(resultTarget).innerHTML = '<div class="panel result-panel"><div class="skeleton"></div></div>';
  try {
    const execution = await api('/execute',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({service,body})});
    $(resultTarget).innerHTML = resultMarkup(execution);
    $(resultTarget).querySelector('[data-inspect]').addEventListener('click',()=>openExecution(execution.id));
    await refreshActivity();
    toast(succeeded(execution)?'Request completed':'Request returned a failure',!succeeded(execution));
  } catch(error) { $(resultTarget).innerHTML=''; toast(error.message,true); }
  finally { button.disabled=false; button.innerHTML=original; }
}
function renderActivity() {
  const list=state.executions;
  $('#overview-recent').innerHTML = list.length?list.slice(0,4).map(item=>`<div class="recent-row" data-open="${esc(item.id)}"><div><strong>${esc(item.service==='psp'?'Purchase':item.service==='bankA'?'Bank A transfer':'Bank B transfer')}</strong><small>${clock(item.startedAt)} · ${shortId(item.id)}</small></div><span class="status-pill ${succeeded(item)?'healthy':'unavailable'}">${item.status||'ERR'}</span></div>`).join(''):'<div class="empty">No requests yet. Start with a scenario.</div>';
  $('#activity-list').innerHTML=list.length?list.map(item=>`<button class="activity-item ${state.selected===item.id?'active':''}" data-open="${esc(item.id)}"><div class="activity-item-top"><strong>${esc(item.service==='psp'?'Purchase':item.service==='bankA'?'Bank A transfer':'Bank B transfer')}</strong><span class="status-pill ${succeeded(item)?'healthy':'unavailable'}">${item.status||'ERR'}</span></div><small>${clock(item.startedAt)} · ${shortId(item.id)} · ${esc(item.durationMs)} ms</small></button>`).join(''):'<div class="empty">No executions yet.</div>';
  document.querySelectorAll('[data-open]').forEach(el=>el.addEventListener('click',()=>openExecution(el.dataset.open)));
}
function record(name, value) {
  const present = value!==null && value!==undefined && !(Array.isArray(value)&&value.length===0);
  return `<div class="record"><div class="record-header">${esc(name)}</div>${present?`<pre>${esc(json(value))}</pre>`:'<div class="missing">No matching record</div>'}</div>`;
}
function renderInspector(run) {
  const p=run.persistence||{}; const ok=succeeded(run);
  const psp=Boolean(p.pspRequest), routed=Boolean(p.routingRequest), bank=Array.isArray(p.bankA)&&p.bankA.length>0||Array.isArray(p.bankB)&&p.bankB.length>0;
  const stages=[['Console','Request dispatched',true],['PSP',psp?'Mongo record found':'No record',psp],['Shaparak',routed?'Mongo record found':'No record',routed],['Bank',bank?'SQL record found':'No record',bank]];
  $('#inspector').innerHTML=`<div class="inspector-heading"><div><div class="eyebrow">EXECUTION INSPECTOR</div><h2>${esc(run.service==='psp'?'Purchase':'Direct bank transfer')}</h2><small>${esc(run.id)} · ${clock(run.startedAt)}</small></div><span class="status-pill ${ok?'healthy':'unavailable'}">HTTP ${run.status||'—'}</span></div><div class="trace-path">${stages.map((step,i)=>`<div class="trace-stage ${step[2]?'hit':''}"><span>0${i+1}</span><strong>${step[0]}</strong><small>${step[1]}</small></div>`).join('')}</div><p class="fine-print">Stages indicate matching persisted records, not live message capture. Missing records can mean validation failed, processing stopped, or persistence was unavailable.</p><div class="inspector-section"><h3>HTTP exchange · ${esc(run.durationMs)} ms</h3><div class="json-grid">${record('Request',run.request)}${record('Response',run.response)}</div></div><div class="inspector-section"><h3>MongoDB · PSP and Shaparak</h3><div class="record-grid">${record('psp.payment_request',p.pspRequest)}${record('psp.payment_response',p.pspResponse)}${record('shaparak.routing_request',p.routingRequest)}${record('shaparak.routing_response',p.routingResponse)}</div></div><div class="inspector-section"><h3>MySQL · Bank transactions</h3><div class="record-grid">${record('bank_a.transaction_a',p.bankA)}${record('bank_b.transaction_b',p.bankB)}</div></div><div class="inspector-section"><h3>Message route</h3><div class="kv"><span>Exchange / key</span><code>payment.exchange / shaparak.purchase</code></div><div class="kv"><span>Queue / consumer</span><code>payment.purchase.queue / Shaparak</code></div><p class="fine-print">PSP sends a synchronous RPC. A Shaparak record suggests the consumer processed this trace; individual broker messages are not stored or displayed. Direct bank calls do not use RabbitMQ.</p></div>`;
}
async function openExecution(id) {
  state.selected=id; navigate('activity'); renderActivity();
  $('#inspector').innerHTML='<div class="empty">Loading matching database records…</div>';
  try {renderInspector(await api('/executions/'+encodeURIComponent(id)));}
  catch(error){$('#inspector').innerHTML=`<div class="empty">${esc(error.message)}</div>`;}
}
function renderLogs() {
  const term=$('#log-search').value.trim().toLowerCase(), level=$('#log-level').value, source=$('#log-source').value;
  const list=state.logs.filter((item,i)=>i<state.logs.length-state.logHiddenBefore).filter(item=>(level==='all'||item.level===level)&&(source==='all'||item.source===source)&&(!term||[item.message,item.traceId,item.source].some(x=>String(x||'').toLowerCase().includes(term))));
  $('#log-list').innerHTML=list.length?list.map(item=>`<div class="log-row"><span>${clock(item.timestamp)}</span><span class="level ${esc(item.level)}">${esc(item.level)}</span><span class="source">${esc(item.source)}</span><span class="message">${esc(item.message)}</span><span class="trace" title="${esc(item.traceId)}">${shortId(item.traceId)}</span></div>`).join(''):'<div class="empty">No matching console events.</div>';
  if ($('#log-autoscroll').checked) $('#log-list').scrollTop=0;
}
async function refreshSystem(){try{state.system=await api('/system');}catch{state.system=null;}renderSystem();}
async function refreshActivity(){try{state.executions=await api('/executions');state.logs=await api('/logs');}catch(error){toast('Activity refresh failed: '+error.message,true);}renderActivity();renderLogs();}
async function refreshAll(){await Promise.all([refreshSystem(),refreshActivity()]);}

document.querySelectorAll('[data-view],[data-go]').forEach(el=>el.addEventListener('click',()=>navigate(el.dataset.view||el.dataset.go)));
$('#refresh-all').addEventListener('click',refreshAll);
$('#refresh-system').addEventListener('click',refreshSystem);
$('#refresh-executions').addEventListener('click',refreshActivity);
$('#refresh-logs').addEventListener('click',refreshActivity);
$('#run-scenario').addEventListener('click',()=>run(state.scenario.service,'#scenario-json','#scenario-result',$('#run-scenario')));
$('#run-playground').addEventListener('click',()=>run($('#playground-service').value,'#playground-json','#playground-result',$('#run-playground')));
$('#playground-service').addEventListener('change',()=>{$('#playground-json').value=json($('#playground-service').value==='psp'?scenarios[0].body:{sourceCardNumber:$('#playground-service').value==='bankA'?'1111111234567890':'2222221234567890',targetAccountNumber:'333333333333',amount:25000});});
['#log-search','#log-level','#log-source'].forEach(selector=>$(selector).addEventListener('input',renderLogs));
$('#clear-log-view').addEventListener('click',()=>{state.logHiddenBefore=state.logs.length;renderLogs();toast('Log view cleared; stored events remain unchanged.');});
selectScenario(scenarios[0].id); $('#playground-service').dispatchEvent(new Event('change'));
refreshAll(); setInterval(refreshAll,15000);
