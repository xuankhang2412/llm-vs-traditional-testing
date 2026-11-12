const fs = require('fs');
const path = require('path');
const root = 'D:/SWT301_TEST';

function nCr(n, r) {
    if (r < 0 || r > n) return 0;
    if (r === 0 || r === n) return 1;
    if (r > n / 2) r = n - r;
    let res = 1;
    for (let i = 1; i <= r; i++) {
        res = res * (n - i + 1) / i;
    }
    return res;
}

function exactMcNemarP(b, c) {
    const m = b + c;
    if (m === 0) return 1.0;
    const k = Math.min(b, c);
    let sum = 0;
    for (let i = 0; i <= k; i++) {
        sum += nCr(m, i) * Math.pow(0.5, m);
    }
    return Math.min(1.0, 2.0 * sum);
}

const apps = ['BasicPackage', 'BasicPayroll', 'EnhancedPackage', 'EnhancedPayroll'];
const defectDetection = {};
const defectsByApp = {};
const defectsByDomain = { Package: [], Payroll: [] };

for (const app of apps) {
    const domain = app.includes('Package') ? 'Package' : 'Payroll';
    const suites = ['TFD', 'PICT_plain', 'LLM_C1', 'LLM_C2', 'LLM_C3', 'LLM_C4', 'LLM_C5', 'LLM_C6'];
    defectsByApp[app] = [];
    const matLines = fs.readFileSync(path.join(root, 'Results', app, 'TFD', 'matrix_long.csv'), 'utf8').trim().split('\n').slice(1);
    const defectIds = Array.from(new Set(matLines.map(l => l.split(',')[2].trim())));

    for (const dId of defectIds) {
        const key = app + '_' + dId;
        defectDetection[key] = { app, domain, defect_id: dId };
        defectsByApp[app].push(key);
        defectsByDomain[domain].push(key);
    }

    for (const s of suites) {
        const lines = fs.readFileSync(path.join(root, 'Results', app, s, 'matrix_long.csv'), 'utf8').trim().split('\n').slice(1);
        const hits = new Set();
        for (const l of lines) {
            const p = l.split(',');
            if (parseInt(p[3].trim()) === 1) hits.add(p[2].trim());
        }
        for (const dId of defectIds) {
            const key = app + '_' + dId;
            defectDetection[key][s] = hits.has(dId);
        }
    }
}

const allKeys = Object.keys(defectDetection);
console.log('Total pooled defects:', allKeys.length);

function getDiscordant(keys, methA, methB) {
    let b = 0, c = 0;
    for (const k of keys) {
        const a = defectDetection[k][methA];
        const d = defectDetection[k][methB];
        if (a && !d) b++;
        if (!a && d) c++;
    }
    return { b, c, rawP: exactMcNemarP(b, c) };
}

// (a) Pooled C1..C6 vs PICT_plain and vs TFD (12 tests)
const pooled12 = [];
for (let i = 1; i <= 6; i++) {
    const cName = 'LLM_C' + i;
    // vs PICT_plain
    const discPict = getDiscordant(allKeys, cName, 'PICT_plain');
    pooled12.push({ scope: 'Pooled_All_Apps', group: 'All_60_Defects', methodA: cName, methodB: 'PICT_plain', ...discPict });
    // vs TFD
    const discTfd = getDiscordant(allKeys, 'TFD', cName);
    pooled12.push({ scope: 'Pooled_All_Apps', group: 'All_60_Defects', methodA: 'TFD', methodB: cName, ...discTfd });
}

// Holm correction on the 12 tests
pooled12.sort((x, y) => x.rawP - y.rawP);
const M = pooled12.length;
for (let i = 0; i < M; i++) {
    let adj = pooled12[i].rawP * (M - i);
    if (i > 0 && adj < pooled12[i - 1].holmP) adj = pooled12[i - 1].holmP;
    pooled12[i].holmP = Math.min(1.0, adj);
}

console.log('\n--- (a) Pooled 12 Tests with Holm correction ---');
for (const t of pooled12) {
    console.log(`${t.methodA} vs ${t.methodB} | b=${t.b}, c=${t.c} | raw_p=${t.rawP.toFixed(6)} | holm_p=${t.holmP.toFixed(6)} | sig=${t.holmP < 0.05 ? 'YES' : 'NO'}`);
}

// (b) Same 12 comparisons per domain (Package, Payroll) - descriptive only
console.log('\n--- (b) Domain Comparisons (Descriptive) ---');
for (const dom of ['Package', 'Payroll']) {
    const domKeys = defectsByDomain[dom];
    console.log(`Domain: ${dom} (N=${domKeys.length})`);
    for (let i = 1; i <= 6; i++) {
        const cName = 'LLM_C' + i;
        const dPict = getDiscordant(domKeys, cName, 'PICT_plain');
        console.log(`  ${cName} vs PICT_plain | b=${dPict.b}, c=${dPict.c} | raw_p=${dPict.rawP.toFixed(6)}`);
        const dTfd = getDiscordant(domKeys, 'TFD', cName);
        console.log(`  TFD vs ${cName} | b=${dTfd.b}, c=${dTfd.c} | raw_p=${dTfd.rawP.toFixed(6)}`);
    }
}

// (c) For each app: 2x2 discordant counts b and c of TFD vs PICT_plain
console.log('\n--- (c) TFD vs PICT_plain per app ---');
for (const app of apps) {
    const appKeys = defectsByApp[app];
    const disc = getDiscordant(appKeys, 'TFD', 'PICT_plain');
    console.log(`  ${app} (N=${appKeys.length}) | b=${disc.b}, c=${disc.c} | raw_p=${disc.rawP.toFixed(6)}`);
}

