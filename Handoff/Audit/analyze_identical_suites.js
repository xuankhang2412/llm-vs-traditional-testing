const fs = require('fs');
const path = require('path');

const apps = ['BasicPackage', 'BasicPayroll', 'EnhancedPackage', 'EnhancedPayroll'];
const genLog = fs.readFileSync('D:/SWT301_TEST/Logs/generation_log.csv', 'utf8').trim().split('\n').slice(1);
const genTimestamps = {};
for (const l of genLog) {
    if (!l.trim()) continue;
    const p = l.split(',');
    genTimestamps[p[1].trim() + '_' + p[3].trim()] = p[0].trim();
}

console.log('IDENTICAL SUITES AUDIT ACROSS ALL 4 APPLICATIONS (AS SETS):\n');

for (const app of apps) {
    console.log('====================================================================');
    console.log('APPLICATION: ' + app);
    console.log('====================================================================');

    const suites = {};
    for (let c = 1; c <= 6; c++) {
        const p = 'D:/SWT301_TEST/Suites/' + app + '/LLM/gemini_C' + c + '_r1.csv';
        const lines = fs.readFileSync(p, 'utf8').trim().split('\n').slice(1);
        const rows = lines.map(l => {
            const parts = l.split(',');
            const input = parts.slice(0, parts.length - 1).map(x => x.trim()).join(',');
            const expected = parts[parts.length - 1].trim();
            return { input, expected, full: input + ',' + expected };
        });
        const stat = fs.statSync(p);
        suites['C' + c] = {
            rows,
            keySet: Array.from(new Set(rows.map(r => r.input))).sort().join('|||'),
            fullSet: Array.from(new Set(rows.map(r => r.full))).sort().join('|||'),
            mtime: stat.mtime.toISOString(),
            genTime: genTimestamps[app + '_C' + c] || 'N/A'
        };
    }

    // Group identical inputs as sets
    const inputGroups = [];
    const visited = new Set();
    for (let i = 1; i <= 6; i++) {
        const cI = 'C' + i;
        if (visited.has(cI)) continue;
        const grp = [cI];
        visited.add(cI);
        for (let j = i + 1; j <= 6; j++) {
            const cJ = 'C' + j;
            if (visited.has(cJ)) continue;
            if (suites[cI].keySet === suites[cJ].keySet) {
                grp.push(cJ);
                visited.add(cJ);
            }
        }
        inputGroups.push(grp);
    }

    console.log('Distinct Suite Groups (as sets): ' + inputGroups.length + ' / 6');
    for (let g = 0; g < inputGroups.length; g++) {
        const grp = inputGroups[g];
        const repr = grp[0];
        let expIdentical = true;
        for (const c of grp) {
            if (suites[repr].fullSet !== suites[c].fullSet) {
                expIdentical = false;
                break;
            }
        }
        console.log(`\n  Group ${g + 1} [${grp.join(', ')}] (${grp.length} condition${grp.length > 1 ? 's' : ''}):`);
        console.log(`    - Input rows identical as sets: YES (${suites[repr].rows.length} rows)`);
        console.log(`    - Expected column identical across group: ${expIdentical ? 'YES' : 'NO'}`);
        for (const c of grp) {
            console.log(`      * ${c}: gen_log_timestamp = "${suites[c].genTime}" | file_mtime = "${suites[c].mtime}"`);
        }
    }

    const unionInputs = new Set();
    for (let c = 1; c <= 6; c++) {
        for (const r of suites['C' + c].rows) unionInputs.add(r.input);
    }
    console.log('\n  Total distinct input rows in LLM Union (C1..C6): ' + unionInputs.size);
    console.log('--------------------------------------------------------------------\n');
}
