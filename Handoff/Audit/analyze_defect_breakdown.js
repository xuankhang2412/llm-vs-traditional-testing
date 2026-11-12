const fs = require('fs');
const path = require('path');

const root = 'D:/SWT301_TEST';
const matrixPath = path.join(root, 'Paper_pack/03_defect_detection_matrix.csv');
const lines = fs.readFileSync(matrixPath, 'utf8').trim().split('\n');

const data = [];
for (let i = 1; i < lines.length; i++) {
    const parts = [];
    let inQ = false, cur = '';
    for (const c of lines[i]) {
        if (c === '"') inQ = !inQ;
        else if (c === ',' && !inQ) { parts.push(cur.trim()); cur = ''; }
        else cur += c;
    }
    parts.push(cur.trim());
    data.push({
        app: parts[0],
        defect_id: parts[1],
        defect_type: parts[2],
        domain: parts[3],
        desc: parts[4],
        TFD: parseInt(parts[5]),
        PICT_plain: parseInt(parts[6]),
        PICT_constrained: parts[7] === '' ? null : parseInt(parts[7]),
        LLM_C1: parseInt(parts[8]),
        LLM_C2: parseInt(parts[9]),
        LLM_C3: parseInt(parts[10]),
        LLM_C4: parseInt(parts[11]),
        LLM_C5: parseInt(parts[12]),
        LLM_C6: parseInt(parts[13]),
        tfd_failing_tests: parseInt(parts[14]),
        LLM_Union: (parseInt(parts[8]) || parseInt(parts[9]) || parseInt(parts[10]) || parseInt(parts[11]) || parseInt(parts[12]) || parseInt(parts[13])) ? 1 : 0
    });
}

console.log('Total defects:', data.length);

// Category and Domain Totals (All 60 defects)
const catTotals = { boundary: 0, arithmetic: 0, logic: 0, conditional: 0 };
const domTotals = { Package: 0, Payroll: 0 };
data.forEach(d => {
    catTotals[d.defect_type]++;
    domTotals[d.domain]++;
});
console.log('Total seeded per category (N=60):', catTotals);
console.log('Total seeded per domain (N=60):', domTotals);

// For Enhanced apps only (for PICT_constrained):
const enhancedData = data.filter(d => d.app.startsWith('Enhanced'));
const enhCatTotals = { boundary: 0, arithmetic: 0, logic: 0, conditional: 0 };
const enhDomTotals = { Package: 0, Payroll: 0 };
enhancedData.forEach(d => {
    enhCatTotals[d.defect_type]++;
    enhDomTotals[d.domain]++;
});
console.log('Total seeded Enhanced only (N=40):', enhCatTotals);
console.log('Total seeded Enhanced per domain (N=40):', enhDomTotals);

// Check detection for each LLM condition
console.log('\n--- POOLED DETECTION TOTALS ---');
const conditions = ['LLM_C1', 'LLM_C2', 'LLM_C3', 'LLM_C4', 'LLM_C5', 'LLM_C6'];
conditions.forEach(c => {
    const total = data.reduce((sum, d) => sum + d[c], 0);
    const byApp = {};
    data.forEach(d => {
        byApp[d.app] = (byApp[d.app] || 0) + d[c];
    });
    console.log(`${c}: Total = ${total}/60 | Breakdown by app:`, byApp);
});

// Best LLM selection:
// C1, C2, C3 are tied at 55/60.
// If Best LLM is a single condition: C1 is the lowest condition number / first alphabetically (tie-break rule: lowest condition index C1).
// Let's also check if Best_LLM is C1 across all apps or selected per app:
// Per app best:
// BasicPackage: all C1..C6 are 10/10. (Tie broken by C1)
// BasicPayroll: all C1..C6 are 10/10. (Tie broken by C1)
// EnhancedPackage: C1=18, C2=18, C3=18, C4=18, C5=18, C6=17. (Tie broken by C1)
// EnhancedPayroll: C1=17, C2=17, C3=17, C4=16, C5=16, C6=16. (Tie broken by C1)
// So whether selected per-app or globally, Best_LLM is IDENTICAL to LLM_C1 (55/60)!

data.forEach(d => {
    d.Best_LLM = d.LLM_C1; // Single best condition C1 (tie-broken by lowest condition index C1)
});

console.log('\n--- PER-CATEGORY DETECTION ---');
const methods = ['TFD', 'PICT_plain', 'PICT_constrained', 'LLM_C1', 'LLM_C2', 'LLM_C3', 'LLM_C4', 'LLM_C5', 'LLM_C6', 'Best_LLM', 'LLM_Union'];

for (const m of methods) {
    const subset = (m === 'PICT_constrained') ? enhancedData : data;
    const totals = (m === 'PICT_constrained') ? enhCatTotals : catTotals;
    const cats = { boundary: 0, arithmetic: 0, logic: 0, conditional: 0 };
    let totalDet = 0;
    subset.forEach(d => {
        if (d[m] === 1) {
            cats[d.defect_type]++;
            totalDet++;
        }
    });
    console.log(`\nMethod: ${m} (Denominator: ${subset.length})`);
    console.log(`  Boundary:    ${cats.boundary} / ${totals.boundary} (${(cats.boundary/totals.boundary*100).toFixed(2)}%)`);
    console.log(`  Arithmetic:  ${cats.arithmetic} / ${totals.arithmetic} (${(cats.arithmetic/totals.arithmetic*100).toFixed(2)}%)`);
    console.log(`  Logic:       ${cats.logic} / ${totals.logic} (${(cats.logic/totals.logic*100).toFixed(2)}%)`);
    console.log(`  Conditional: ${cats.conditional} / ${totals.conditional} (${(cats.conditional/totals.conditional*100).toFixed(2)}%)`);
    const sumCats = cats.boundary + cats.arithmetic + cats.logic + cats.conditional;
    console.log(`  SUM OF CATS: ${sumCats} | TOTAL DETECTED: ${totalDet} | Matches: ${sumCats === totalDet}`);
}

console.log('\n--- PER-DOMAIN DETECTION ---');
for (const m of methods) {
    const subset = (m === 'PICT_constrained') ? enhancedData : data;
    const totals = (m === 'PICT_constrained') ? enhDomTotals : domTotals;
    const doms = { Package: 0, Payroll: 0 };
    let totalDet = 0;
    subset.forEach(d => {
        if (d[m] === 1) {
            doms[d.domain]++;
            totalDet++;
        }
    });
    console.log(`Method: ${m} | Package: ${doms.Package}/${totals.Package} | Payroll: ${doms.Payroll}/${totals.Payroll} | Sum: ${doms.Package + doms.Payroll} | Total: ${totalDet}`);
}
