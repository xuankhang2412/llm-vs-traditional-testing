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
        Best_LLM: parseInt(parts[8]), // C1 (tie broken by lowest condition index C1)
        LLM_Union: (parseInt(parts[8]) || parseInt(parts[9]) || parseInt(parts[10]) || parseInt(parts[11]) || parseInt(parts[12]) || parseInt(parts[13])) ? 1 : 0
    });
}

const enhancedData = data.filter(d => d.app.startsWith('Enhanced'));

const categories = ['boundary', 'arithmetic', 'logic', 'conditional'];
const suites = ['TFD', 'PICT_plain', 'PICT_constrained', 'LLM_C1', 'LLM_C2', 'LLM_C3', 'LLM_C4', 'LLM_C5', 'LLM_C6', 'Best_LLM', 'LLM_Union'];

// Check assertions against primary_metrics totals
const expectedTotals = {
    'TFD': 60,
    'PICT_plain': 45,
    'PICT_constrained': 27,
    'LLM_C1': 55,
    'LLM_C2': 55,
    'LLM_C3': 55,
    'LLM_C4': 54,
    'LLM_C5': 54,
    'LLM_C6': 53,
    'Best_LLM': 55,
    'LLM_Union': 59
};

let csvContent = 'suite,category,detected_defects,total_defects,detection_rate\n';

for (const suite of suites) {
    const dataset = (suite === 'PICT_constrained') ? enhancedData : data;
    let suiteCatSum = 0;
    let suiteDomSum = 0;

    // Categories
    for (const cat of categories) {
        const catRows = dataset.filter(d => d.defect_type === cat);
        const tot = catRows.length;
        const det = catRows.filter(d => d[suite] === 1).length;
        const rate = (tot > 0) ? (det / tot).toFixed(4) : '0.0000';
        csvContent += `${suite},${cat},${det},${tot},${rate}\n`;
        suiteCatSum += det;
    }

    // Domains
    const pkgDet = dataset.filter(d => d.domain === 'Package' && d[suite] === 1).length;
    const payDet = dataset.filter(d => d.domain === 'Payroll' && d[suite] === 1).length;
    suiteDomSum = pkgDet + payDet;

    const exp = expectedTotals[suite];
    if (suiteCatSum !== exp) {
        console.error(`ASSERTION FAILED for ${suite}: Category sum ${suiteCatSum} != expected ${exp}`);
        process.exit(1);
    }
    if (suiteDomSum !== exp) {
        console.error(`ASSERTION FAILED for ${suite}: Domain sum ${suiteDomSum} != expected ${exp}`);
        process.exit(1);
    }
}

const outPath = path.join(root, 'Paper_pack/defect_type_by_suite.csv');
fs.writeFileSync(outPath, csvContent, 'utf8');
console.log(`Successfully wrote ${outPath} (${csvContent.trim().split('\n').length - 1} rows). All assertions passed!`);

