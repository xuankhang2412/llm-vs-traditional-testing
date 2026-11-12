package results;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.util.*;

public class MetricsCalculator {

    public static class DefectMeta {
        String id;
        String type; // boundary, arithmetic, logic, conditional
        String description;

        DefectMeta(String id, String type, String description) {
            this.id = id;
            this.type = type;
            this.description = description;
        }
    }

    public static class SuiteResult {
        String app;
        String suiteName; // TFD, PICT_plain, PICT_constrained, LLM_C1..C6
        String method;    // TFD, PICT, LLM
        String condition; // C1..C6, plain, constrained, TFD
        Path suiteCsv;
        Path matrixCsv;

        int totalTests;
        int totalDefects;
        Set<String> detectedDefects = new LinkedHashSet<>();
        Map<String, Integer> failingTestsPerDefect = new LinkedHashMap<>();

        int invalidTestsCount = 0;
        int testsWithExpectedCount = 0;
        int validTestsCount = 0;
        int errorTestsCount = 0;

        int expectedAccValidMatches = 0;
        int expectedAccErrorMatches = 0;
        int expectedAccTotalMatches = 0;

        int hitExpBaselineOkDetectedCount = 0;
        Set<String> hitExpBaselineOkDefects = new LinkedHashSet<>();

        List<List<String>> inputRows = new ArrayList<>();
    }

    public static void main(String[] args) throws Exception {
        Path root = Paths.get("D:/SWT301_TEST");
        Path resultsDir = root.resolve("Results");
        Path scriptsDir = resultsDir.resolve("scripts");
        Files.createDirectories(scriptsDir);

        Map<String, Map<String, DefectMeta>> defectCatalogues = initDefectCatalogues();

        String[] apps = {"BasicPackage", "BasicPayroll", "EnhancedPackage", "EnhancedPayroll"};
        List<SuiteResult> allSuites = new ArrayList<>();

        for (String app : apps) {
            Path appResults = resultsDir.resolve(app);
            File[] suiteDirs = appResults.toFile().listFiles(File::isDirectory);
            if (suiteDirs == null) continue;

            // Sort suiteDirs: TFD, PICT_plain, PICT_constrained, LLM_C1..C6
            Arrays.sort(suiteDirs, Comparator.comparing(File::getName));

            for (File sDir : suiteDirs) {
                if (sDir.getName().equals("scripts")) continue;
                SuiteResult sr = processSuite(root, app, sDir.getName(), defectCatalogues.get(app));
                allSuites.add(sr);
            }
        }

        System.out.println("Loaded " + allSuites.size() + " suite results.");

        // 1. Primary Metrics CSV
        writePrimaryMetrics(resultsDir.resolve("primary_metrics.csv"), allSuites);

        // 2. Secondary Metrics CSV
        writeSecondaryMetrics(resultsDir.resolve("secondary_metrics.csv"), allSuites);

        // 3. Defect Type and Domain Breakdown CSV
        writeDefectBreakdown(resultsDir.resolve("defect_type_and_domain_breakdown.csv"), allSuites, defectCatalogues);

        // 4. Hybrid Analysis CSV
        writeHybridAnalysis(resultsDir.resolve("hybrid_analysis.csv"), allSuites, defectCatalogues);

        // 5. LLM Factors CSV
        writeLlmFactors(resultsDir.resolve("llm_factors.csv"), allSuites);

        // 6. Diversity and Overlap CSV
        writeDiversityAndOverlap(resultsDir.resolve("diversity_and_overlap.csv"), allSuites);

        // 7. Statistical Tests CSV
        writeStatisticalTests(resultsDir.resolve("statistical_tests.csv"), allSuites, defectCatalogues);

        // 8. Cost Comparison CSV
        writeCostComparison(resultsDir.resolve("cost_comparison.csv"), root, allSuites);

        System.out.println("All 8 CSV metrics files generated successfully!");
    }

    private static SuiteResult processSuite(Path root, String app, String suiteName, Map<String, DefectMeta> catalog) throws Exception {
        SuiteResult sr = new SuiteResult();
        sr.app = app;
        sr.suiteName = suiteName;
        sr.matrixCsv = root.resolve("Results").resolve(app).resolve(suiteName).resolve("matrix_long.csv");

        if (suiteName.equals("TFD")) {
            sr.method = "TFD";
            sr.condition = "TFD";
            sr.suiteCsv = root.resolve("Suites").resolve(app).resolve("TFD/suite.csv");
        } else if (suiteName.startsWith("PICT")) {
            sr.method = "PICT";
            sr.condition = suiteName.replace("PICT_", "");
            sr.suiteCsv = root.resolve("Suites").resolve(app).resolve(suiteName + "/suite.csv");
        } else if (suiteName.startsWith("LLM_")) {
            sr.method = "LLM";
            sr.condition = suiteName.replace("LLM_", "");
            sr.suiteCsv = root.resolve("Suites").resolve(app).resolve("LLM/gemini_" + sr.condition + "_r1.csv");
        }

        sr.totalDefects = catalog.size();
        for (String dId : catalog.keySet()) {
            sr.failingTestsPerDefect.put(dId, 0);
        }

        // Read matrix_long.csv
        // Header: suite,test_id,defect,HIT_REF,HIT_EXP,BASELINE_OK
        List<String> matrixLines = Files.readAllLines(sr.matrixCsv);
        Set<String> testIds = new LinkedHashSet<>();
        Map<String, Set<String>> baselineOkTests = new HashMap<>(); // testId -> "1"/"0"
        Map<String, Map<String, Integer>> hitExpByTestAndDefect = new HashMap<>();

        for (int i = 1; i < matrixLines.size(); i++) {
            String line = matrixLines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",", -1);
            String testId = parts[1];
            String defect = parts[2];
            int hitRef = Integer.parseInt(parts[3]);
            String hitExpStr = parts.length > 4 ? parts[4] : "";
            String baselineOkStr = parts.length > 5 ? parts[5] : "";

            testIds.add(testId);

            if (hitRef == 1) {
                sr.detectedDefects.add(defect);
                sr.failingTestsPerDefect.put(defect, sr.failingTestsPerDefect.getOrDefault(defect, 0) + 1);
            }

            if ("1".equals(baselineOkStr) && "1".equals(hitExpStr)) {
                sr.hitExpBaselineOkDefects.add(defect);
            }
        }
        sr.totalTests = testIds.size();
        sr.hitExpBaselineOkDetectedCount = sr.hitExpBaselineOkDefects.size();

        // Now inspect suite CSV and run NONE via reference calculators
        List<String> suiteLines = Files.readAllLines(sr.suiteCsv);
        if (suiteLines.isEmpty()) return sr;

        String header = suiteLines.get(0);
        String[] cols = header.split(",");
        int expectedColIdx = cols.length - 1; // expected is always the last column

        for (int i = 1; i < suiteLines.size(); i++) {
            String line = suiteLines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] values = line.split(",", -1);
            for (int v = 0; v < values.length; v++) values[v] = values[v].trim();

            List<String> inputOnly = new ArrayList<>();
            for (int c = 0; c < expectedColIdx; c++) {
                inputOnly.add(values[c]);
            }
            sr.inputRows.add(inputOnly);

            String expectedVal = values.length > expectedColIdx ? values[expectedColIdx] : "";
            String noneOutput = runNoneReference(app, values);

            boolean isInvalid = "ERROR".equals(noneOutput);
            if (isInvalid) {
                sr.invalidTestsCount++;
                sr.errorTestsCount++;
            } else {
                sr.validTestsCount++;
            }

            if (!expectedVal.isEmpty()) {
                sr.testsWithExpectedCount++;
                boolean match = matchesExpected(noneOutput, expectedVal);
                if (match) {
                    sr.expectedAccTotalMatches++;
                    if (isInvalid) {
                        sr.expectedAccErrorMatches++;
                    } else {
                        sr.expectedAccValidMatches++;
                    }
                }
            }
        }

        return sr;
    }

    private static String runNoneReference(String app, String[] values) {
        try {
            if ("BasicPackage".equals(app)) {
                double w = Double.parseDouble(values[0]);
                int orig = Integer.parseInt(values[1]);
                int dest = Integer.parseInt(values[2]);
                int deliv = Integer.parseInt(values[3]);
                int sig = Integer.parseInt(values[4]);
                int loc = Integer.parseInt(values[5]);
                int sat = Integer.parseInt(values[6]);
                int hold = Integer.parseInt(values[7]);
                int frag = Integer.parseInt(values[8]);
                int dang = Integer.parseInt(values[9]);
                com.shipping.ShippingCalculator calc = new com.shipping.ShippingCalculator(com.shipping.PackageDefect.NONE);
                com.shipping.ShippingResult res = calc.calculate(new com.shipping.PackageShippingInput(w, orig, dest, deliv, sig, loc, sat, hold, frag, dang));
                return res.getTotalCost().setScale(2, RoundingMode.HALF_UP).toPlainString();
            } else if ("BasicPayroll".equals(app)) {
                double h = Double.parseDouble(values[0]);
                double reg = Double.parseDouble(values[1]);
                double ot = Double.parseDouble(values[2]);
                double hol = Double.parseDouble(values[3]);
                int med = Integer.parseInt(values[4]);
                int den = Integer.parseInt(values[5]);
                int vis = Integer.parseInt(values[6]);
                double uw = Double.parseDouble(values[7]);
                double pen = Double.parseDouble(values[8]);
                int ltd = Integer.parseInt(values[9]);
                int std = Integer.parseInt(values[10]);
                int park = Integer.parseInt(values[11]);
                com.payroll.PayrollCalculator calc = new com.payroll.PayrollCalculator(com.payroll.PayrollDefect.NONE);
                com.payroll.PayrollResult res = calc.calculate(new com.payroll.PayrollInput(h, reg, ot, hol, med, den, vis, uw, pen, ltd, std, park));
                return res.getNetSalary().setScale(2, RoundingMode.HALF_UP).toPlainString();
            } else if ("EnhancedPackage".equals(app)) {
                double w = Double.parseDouble(values[0]);
                int orig = Integer.parseInt(values[1]);
                int dest = Integer.parseInt(values[2]);
                int deliv = Integer.parseInt(values[3]);
                int pkg = Integer.parseInt(values[4]);
                int sig = Integer.parseInt(values[5]);
                int loc = Integer.parseInt(values[6]);
                int sat = Integer.parseInt(values[7]);
                int hold = Integer.parseInt(values[8]);
                int frag = Integer.parseInt(values[9]);
                int dang = Integer.parseInt(values[10]);
                com.shipping.enhanced.ShippingCalculator calc = new com.shipping.enhanced.ShippingCalculator(com.shipping.enhanced.Defect.NONE);
                com.shipping.enhanced.ShippingResult res = calc.calculate(new com.shipping.enhanced.ShippingInput(w, orig, dest, deliv, pkg, sig, loc, sat, hold, frag, dang));
                return res.totalCost().setScale(2, RoundingMode.HALF_UP).toPlainString();
            } else if ("EnhancedPayroll".equals(app)) {
                double h = Double.parseDouble(values[0]);
                double reg = Double.parseDouble(values[1]);
                double ot = Double.parseDouble(values[2]);
                double hol = Double.parseDouble(values[3]);
                int state = Integer.parseInt(values[4]);
                int med = Integer.parseInt(values[5]);
                int den = Integer.parseInt(values[6]);
                int vis = Integer.parseInt(values[7]);
                double uw = Double.parseDouble(values[8]);
                double pen = Double.parseDouble(values[9]);
                int ltd = Integer.parseInt(values[10]);
                int std = Integer.parseInt(values[11]);
                int park = Integer.parseInt(values[12]);
                com.payroll.enhanced.PayrollCalculator calc = new com.payroll.enhanced.PayrollCalculator(com.payroll.enhanced.Defect.NONE);
                com.payroll.enhanced.PayrollResult res = calc.calculate(new com.payroll.enhanced.PayrollInput(h, reg, ot, hol, state, med, den, vis, uw, pen, ltd, std, park));
                return res.netPay().setScale(2, RoundingMode.HALF_UP).toPlainString();
            }
        } catch (Throwable t) {
            return "ERROR";
        }
        return "ERROR";
    }

    private static boolean matchesExpected(String actual, String expected) {
        if ("ERROR".equalsIgnoreCase(expected)) {
            return "ERROR".equals(actual);
        }
        if ("ERROR".equals(actual)) {
            return false;
        }
        try {
            BigDecimal a = new BigDecimal(actual).setScale(2, RoundingMode.HALF_UP);
            BigDecimal e = new BigDecimal(expected).setScale(2, RoundingMode.HALF_UP);
            return a.compareTo(e) == 0;
        } catch (Exception ex) {
            return actual.trim().equalsIgnoreCase(expected.trim());
        }
    }

    private static void writePrimaryMetrics(Path out, List<SuiteResult> suites) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out))) {
            pw.println("app,suite,method,condition,total_defects,detected_defects,detection_rate,total_tests,defects_per_test");
            for (SuiteResult sr : suites) {
                double rate = (double) sr.detectedDefects.size() / sr.totalDefects;
                double dpt = (double) sr.detectedDefects.size() / sr.totalTests;
                pw.printf("%s,%s,%s,%s,%d,%d,%.4f,%d,%.4f%n",
                        sr.app, sr.suiteName, sr.method, sr.condition,
                        sr.totalDefects, sr.detectedDefects.size(), rate,
                        sr.totalTests, dpt);
            }
        }
    }

    private static void writeSecondaryMetrics(Path out, List<SuiteResult> suites) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out))) {
            pw.println("app,suite,method,condition,total_tests,invalid_tests,invalid_test_rate,tests_with_expected," +
                    "expected_acc_overall,expected_acc_valid,expected_acc_error,sum_failing_tests," +
                    "hit_exp_baseline_ok_detected,hit_exp_baseline_ok_rate");
            for (SuiteResult sr : suites) {
                double invRate = (double) sr.invalidTestsCount / sr.totalTests;
                String accOverall = sr.testsWithExpectedCount > 0 ? String.format(Locale.US, "%.4f", (double) sr.expectedAccTotalMatches / sr.testsWithExpectedCount) : "N/A";
                String accValid = sr.validTestsCount > 0 && sr.testsWithExpectedCount > 0 ? String.format(Locale.US, "%.4f", (double) sr.expectedAccValidMatches / sr.validTestsCount) : "N/A";
                String accError = sr.errorTestsCount > 0 && sr.testsWithExpectedCount > 0 ? String.format(Locale.US, "%.4f", (double) sr.expectedAccErrorMatches / sr.errorTestsCount) : "N/A";

                int sumFailing = 0;
                for (int count : sr.failingTestsPerDefect.values()) {
                    sumFailing += count;
                }

                double sensRate = (double) sr.hitExpBaselineOkDetectedCount / sr.totalDefects;

                pw.printf(Locale.US, "%s,%s,%s,%s,%d,%d,%.4f,%d,%s,%s,%s,%d,%d,%.4f%n",
                        sr.app, sr.suiteName, sr.method, sr.condition,
                        sr.totalTests, sr.invalidTestsCount, invRate, sr.testsWithExpectedCount,
                        accOverall, accValid, accError, sumFailing,
                        sr.hitExpBaselineOkDetectedCount, sensRate);
            }
        }
    }

    private static void writeDefectBreakdown(Path out, List<SuiteResult> suites, Map<String, Map<String, DefectMeta>> catalogs) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out))) {
            pw.println("domain,app,suite,method,category,category_total_defects,category_detected_defects,category_detection_rate");

            for (SuiteResult sr : suites) {
                String domain = sr.app.contains("Package") ? "Package" : "Payroll";
                Map<String, DefectMeta> cat = catalogs.get(sr.app);

                Map<String, Integer> catTotal = new LinkedHashMap<>();
                Map<String, Integer> catDetected = new LinkedHashMap<>();
                catTotal.put("boundary", 0);
                catTotal.put("arithmetic", 0);
                catTotal.put("logic", 0);
                catTotal.put("conditional", 0);

                catDetected.put("boundary", 0);
                catDetected.put("arithmetic", 0);
                catDetected.put("logic", 0);
                catDetected.put("conditional", 0);

                for (Map.Entry<String, DefectMeta> entry : cat.entrySet()) {
                    String dId = entry.getKey();
                    String type = entry.getValue().type;
                    catTotal.put(type, catTotal.getOrDefault(type, 0) + 1);
                    if (sr.detectedDefects.contains(dId)) {
                        catDetected.put(type, catDetected.getOrDefault(type, 0) + 1);
                    }
                }

                for (String type : catTotal.keySet()) {
                    int tot = catTotal.get(type);
                    if (tot == 0) continue;
                    int det = catDetected.get(type);
                    double rate = (double) det / tot;
                    pw.printf(Locale.US, "%s,%s,%s,%s,%s,%d,%d,%.4f%n",
                            domain, sr.app, sr.suiteName, sr.method, type, tot, det, rate);
                }
            }
        }
    }

    private static void writeHybridAnalysis(Path out, List<SuiteResult> suites, Map<String, Map<String, DefectMeta>> catalogs) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out))) {
            pw.println("app,total_defects,combination,methods_included,total_tests_in_combination,detected_count,detection_rate,best_single_count,gain_abs,gain_pct");

            Map<String, List<SuiteResult>> byApp = new LinkedHashMap<>();
            for (SuiteResult sr : suites) {
                byApp.computeIfAbsent(sr.app, k -> new ArrayList<>()).add(sr);
            }

            for (String app : byApp.keySet()) {
                List<SuiteResult> appSuites = byApp.get(app);
                int totDef = catalogs.get(app).size();

                SuiteResult tfd = null;
                SuiteResult pictPlain = null;
                SuiteResult bestLlm = null;
                Set<String> llmUnion = new LinkedHashSet<>();
                int llmTotalTests = 0;

                for (SuiteResult sr : appSuites) {
                    if ("TFD".equals(sr.suiteName)) tfd = sr;
                    if ("PICT_plain".equals(sr.suiteName)) pictPlain = sr;
                    if (sr.suiteName.startsWith("LLM_")) {
                        llmUnion.addAll(sr.detectedDefects);
                        llmTotalTests += sr.totalTests;
                        if (bestLlm == null || sr.detectedDefects.size() > bestLlm.detectedDefects.size()) {
                            bestLlm = sr;
                        }
                    }
                }

                int tfdTests = tfd.totalTests;
                int pictTests = pictPlain.totalTests;
                int bestLlmTests = bestLlm.totalTests;

                evalCombo(pw, app, totDef, "TFD + Best LLM", "TFD," + bestLlm.suiteName,
                        tfdTests + bestLlmTests,
                        union(tfd.detectedDefects, bestLlm.detectedDefects),
                        Math.max(tfd.detectedDefects.size(), bestLlm.detectedDefects.size()));

                evalCombo(pw, app, totDef, "TFD + PICT_plain", "TFD,PICT_plain",
                        tfdTests + pictTests,
                        union(tfd.detectedDefects, pictPlain.detectedDefects),
                        Math.max(tfd.detectedDefects.size(), pictPlain.detectedDefects.size()));

                evalCombo(pw, app, totDef, "Best LLM + PICT_plain", bestLlm.suiteName + ",PICT_plain",
                        bestLlmTests + pictTests,
                        union(bestLlm.detectedDefects, pictPlain.detectedDefects),
                        Math.max(bestLlm.detectedDefects.size(), pictPlain.detectedDefects.size()));

                evalCombo(pw, app, totDef, "TFD + Best LLM + PICT_plain", "TFD," + bestLlm.suiteName + ",PICT_plain",
                        tfdTests + bestLlmTests + pictTests,
                        union(tfd.detectedDefects, bestLlm.detectedDefects, pictPlain.detectedDefects),
                        Math.max(tfd.detectedDefects.size(), Math.max(bestLlm.detectedDefects.size(), pictPlain.detectedDefects.size())));

                evalCombo(pw, app, totDef, "TFD + LLM_Union", "TFD,LLM_Union",
                        tfdTests + llmTotalTests,
                        union(tfd.detectedDefects, llmUnion),
                        Math.max(tfd.detectedDefects.size(), llmUnion.size()));

                evalCombo(pw, app, totDef, "LLM_Union + PICT_plain", "LLM_Union,PICT_plain",
                        llmTotalTests + pictTests,
                        union(llmUnion, pictPlain.detectedDefects),
                        Math.max(llmUnion.size(), pictPlain.detectedDefects.size()));

                evalCombo(pw, app, totDef, "TFD + LLM_Union + PICT_plain", "TFD,LLM_Union,PICT_plain",
                        tfdTests + llmTotalTests + pictTests,
                        union(tfd.detectedDefects, llmUnion, pictPlain.detectedDefects),
                        Math.max(tfd.detectedDefects.size(), Math.max(llmUnion.size(), pictPlain.detectedDefects.size())));
            }
        }
    }

    private static void evalCombo(PrintWriter pw, String app, int totDef, String comboName, String methods, int totalTestsInCombo, Set<String> comboSet, int bestSingle) {
        int count = comboSet.size();
        double rate = (double) count / totDef;
        int gainAbs = count - bestSingle;
        double gainPct = bestSingle > 0 ? ((double) gainAbs / bestSingle) * 100.0 : 0.0;
        pw.printf(Locale.US, "%s,%d,\"%s\",\"%s\",%d,%d,%.4f,%d,%d,%.2f%%%n",
                app, totDef, comboName, methods, totalTestsInCombo, count, rate, bestSingle, gainAbs, gainPct);
    }

    private static Set<String> union(Set<String>... sets) {
        Set<String> res = new LinkedHashSet<>();
        for (Set<String> s : sets) {
            if (s != null) res.addAll(s);
        }
        return res;
    }

    private static void writeLlmFactors(Path out, List<SuiteResult> suites) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out))) {
            pw.println("scope,factor,level,suite_count,pooled_defects_detected,pooled_defect_opportunities,pooled_detection_rate,mean_of_per_suite_rates,mean_defects_per_test,label");

            List<SuiteResult> llmSuites = new ArrayList<>();
            for (SuiteResult sr : suites) {
                if (sr.suiteName.startsWith("LLM_")) llmSuites.add(sr);
            }

            // Per condition across 4 apps
            Map<String, List<SuiteResult>> byCond = new LinkedHashMap<>();
            for (SuiteResult sr : llmSuites) {
                byCond.computeIfAbsent(sr.condition, k -> new ArrayList<>()).add(sr);
            }

            for (Map.Entry<String, List<SuiteResult>> entry : byCond.entrySet()) {
                String cond = entry.getKey();
                List<SuiteResult> list = entry.getValue();
                int pooledDet = 0;
                int pooledOpp = 0;
                List<Double> rates = new ArrayList<>();
                List<Double> dpts = new ArrayList<>();
                for (SuiteResult sr : list) {
                    pooledDet += sr.detectedDefects.size();
                    pooledOpp += sr.totalDefects;
                    rates.add((double) sr.detectedDefects.size() / sr.totalDefects);
                    dpts.add((double) sr.detectedDefects.size() / sr.totalTests);
                }
                double pooledRate = (double) pooledDet / pooledOpp;
                double meanRate = mean(rates);
                double meanDpt = mean(dpts);
                pw.printf(Locale.US, "All_Apps,Condition,%s,%d,%d,%d,%.4f,%.4f,%.4f,descriptive%n",
                        cond, list.size(), pooledDet, pooledOpp, pooledRate, meanRate, meanDpt);
            }

            // Detail level: Low (C1-C3) vs High (C4-C6)
            List<SuiteResult> lowSuites = new ArrayList<>();
            List<SuiteResult> highSuites = new ArrayList<>();
            for (SuiteResult sr : llmSuites) {
                int cNum = Integer.parseInt(sr.condition.substring(1));
                if (cNum <= 3) lowSuites.add(sr);
                else highSuites.add(sr);
            }

            writeFactorRow(pw, "Detail_Level", "Low (C1-C3)", lowSuites);
            writeFactorRow(pw, "Detail_Level", "High (C4-C6)", highSuites);

            // Shots: 0-shot (C1,C4) vs 1-shot (C2,C5) vs 3-shot (C3,C6)
            List<SuiteResult> s0Suites = new ArrayList<>();
            List<SuiteResult> s1Suites = new ArrayList<>();
            List<SuiteResult> s3Suites = new ArrayList<>();
            for (SuiteResult sr : llmSuites) {
                if (sr.condition.equals("C1") || sr.condition.equals("C4")) s0Suites.add(sr);
                else if (sr.condition.equals("C2") || sr.condition.equals("C5")) s1Suites.add(sr);
                else if (sr.condition.equals("C3") || sr.condition.equals("C6")) s3Suites.add(sr);
            }

            writeFactorRow(pw, "Shots", "0-shot (C1,C4)", s0Suites);
            writeFactorRow(pw, "Shots", "1-shot (C2,C5)", s1Suites);
            writeFactorRow(pw, "Shots", "3-shot (C3,C6)", s3Suites);
        }
    }

    private static void writeFactorRow(PrintWriter pw, String factor, String level, List<SuiteResult> list) {
        int pooledDet = 0;
        int pooledOpp = 0;
        List<Double> rates = new ArrayList<>();
        List<Double> dpts = new ArrayList<>();
        for (SuiteResult sr : list) {
            pooledDet += sr.detectedDefects.size();
            pooledOpp += sr.totalDefects;
            rates.add((double) sr.detectedDefects.size() / sr.totalDefects);
            dpts.add((double) sr.detectedDefects.size() / sr.totalTests);
        }
        double pooledRate = (double) pooledDet / pooledOpp;
        double meanRate = mean(rates);
        double meanDpt = mean(dpts);
        pw.printf(Locale.US, "All_Apps,%s,\"%s\",%d,%d,%d,%.4f,%.4f,%.4f,descriptive%n",
                factor, level, list.size(), pooledDet, pooledOpp, pooledRate, meanRate, meanDpt);
    }

    private static double mean(List<Double> vals) {
        if (vals.isEmpty()) return 0;
        double s = 0;
        for (double v : vals) s += v;
        return s / vals.size();
    }

    private static void writeDiversityAndOverlap(Path out, List<SuiteResult> suites) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out))) {
            pw.println("app,comparison_type,suite_a,suite_b,size_a,size_b,overlap_count,jaccard_similarity");

            Map<String, List<SuiteResult>> byApp = new LinkedHashMap<>();
            for (SuiteResult sr : suites) {
                byApp.computeIfAbsent(sr.app, k -> new ArrayList<>()).add(sr);
            }

            for (String app : byApp.keySet()) {
                List<SuiteResult> appSuites = byApp.get(app);
                Map<String, SuiteResult> map = new LinkedHashMap<>();
                for (SuiteResult sr : appSuites) map.put(sr.suiteName, sr);

                // 1. Unique inputs per LLM suite
                for (int c = 1; c <= 6; c++) {
                    SuiteResult sr = map.get("LLM_C" + c);
                    Set<String> uniqueInputs = new HashSet<>();
                    for (List<String> row : sr.inputRows) uniqueInputs.add(String.join(",", row));
                    pw.printf(Locale.US, "%s,Unique_Inputs,%s,%s,%d,%d,%d,%.4f%n",
                            app, sr.suiteName, sr.suiteName, sr.totalTests, sr.totalTests,
                            uniqueInputs.size(), (double) uniqueInputs.size() / sr.totalTests);
                }

                // 2. Pairwise overlap among 6 LLM conditions
                for (int i = 1; i <= 6; i++) {
                    for (int j = i + 1; j <= 6; j++) {
                        SuiteResult a = map.get("LLM_C" + i);
                        SuiteResult b = map.get("LLM_C" + j);
                        writeOverlapPair(pw, app, "LLM_vs_LLM", a, b);
                    }
                }

                // 3. Overlap LLM vs TFD
                SuiteResult tfd = map.get("TFD");
                for (int c = 1; c <= 6; c++) {
                    SuiteResult llm = map.get("LLM_C" + c);
                    writeOverlapPair(pw, app, "LLM_vs_TFD", llm, tfd);
                }

                // 4. Overlap LLM vs PICT_plain
                SuiteResult pict = map.get("PICT_plain");
                for (int c = 1; c <= 6; c++) {
                    SuiteResult llm = map.get("LLM_C" + c);
                    writeOverlapPair(pw, app, "LLM_vs_PICT_plain", llm, pict);
                }

                // 5. Overlap TFD vs PICT_plain
                writeOverlapPair(pw, app, "TFD_vs_PICT_plain", tfd, pict);

                // 6. If PICT_constrained exists:
                if (map.containsKey("PICT_constrained")) {
                    SuiteResult pictC = map.get("PICT_constrained");
                    writeOverlapPair(pw, app, "TFD_vs_PICT_constrained", tfd, pictC);
                    writeOverlapPair(pw, app, "PICT_plain_vs_PICT_constrained", pict, pictC);
                }
            }
        }
    }

    private static void writeOverlapPair(PrintWriter pw, String app, String type, SuiteResult a, SuiteResult b) {
        Set<String> setA = new HashSet<>();
        for (List<String> r : a.inputRows) setA.add(String.join(",", r));
        Set<String> setB = new HashSet<>();
        for (List<String> r : b.inputRows) setB.add(String.join(",", r));

        int overlap = 0;
        for (String row : setA) {
            if (setB.contains(row)) overlap++;
        }
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);
        double jaccard = union.isEmpty() ? 0 : (double) overlap / union.size();

        pw.printf(Locale.US, "%s,%s,%s,%s,%d,%d,%d,%.4f%n",
                app, type, a.suiteName, b.suiteName, a.inputRows.size(), b.inputRows.size(), overlap, jaccard);
    }

    private static void writeStatisticalTests(Path out, List<SuiteResult> suites, Map<String, Map<String, DefectMeta>> catalogs) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out))) {
            pw.println("scope,test_type,method_a,method_b,n_defects,b_discordant,c_discordant,p_value_raw,p_value_holm,significance_alpha_0_05,odds_ratio,risk_difference,cohens_g,power_warning");

            // Build pooled defect binary table across all 4 apps (n = 60 defects)
            // Defects order: BasicPackage D1..D10, BasicPayroll D1..D10, EnhancedPackage D1..D20, EnhancedPayroll D1..D20
            List<String> allDefectKeys = new ArrayList<>();
            Map<String, Boolean> tfdDetected = new LinkedHashMap<>();
            Map<String, Boolean> pictPlainDetected = new LinkedHashMap<>();
            Map<String, Boolean> bestLlmDetected = new LinkedHashMap<>();
            Map<String, Boolean> llmUnionDetected = new LinkedHashMap<>();

            Map<String, List<SuiteResult>> byApp = new LinkedHashMap<>();
            for (SuiteResult sr : suites) {
                byApp.computeIfAbsent(sr.app, k -> new ArrayList<>()).add(sr);
            }

            for (String app : byApp.keySet()) {
                List<SuiteResult> appSuites = byApp.get(app);
                SuiteResult tfd = null;
                SuiteResult pictPlain = null;
                SuiteResult bestLlm = null;
                Set<String> llmUnion = new HashSet<>();

                for (SuiteResult sr : appSuites) {
                    if ("TFD".equals(sr.suiteName)) tfd = sr;
                    if ("PICT_plain".equals(sr.suiteName)) pictPlain = sr;
                    if (sr.suiteName.startsWith("LLM_")) {
                        llmUnion.addAll(sr.detectedDefects);
                        if (bestLlm == null || sr.detectedDefects.size() > bestLlm.detectedDefects.size()) {
                            bestLlm = sr;
                        }
                    }
                }

                for (String dId : catalogs.get(app).keySet()) {
                    String globalKey = app + "_" + dId;
                    allDefectKeys.add(globalKey);
                    tfdDetected.put(globalKey, tfd.detectedDefects.contains(dId));
                    pictPlainDetected.put(globalKey, pictPlain.detectedDefects.contains(dId));
                    bestLlmDetected.put(globalKey, bestLlm.detectedDefects.contains(dId));
                    llmUnionDetected.put(globalKey, llmUnion.contains(dId));
                }
            }

            int n = allDefectKeys.size(); // 60

            // 1. Cochran's Q test: TFD vs PICT_plain vs Best LLM
            double qStat1 = computeCochransQ(allDefectKeys, tfdDetected, pictPlainDetected, bestLlmDetected);
            double qPval1 = chiSquarePValue(2, qStat1);
            pw.printf(Locale.US, "Pooled_All_Apps,Cochran_Q,TFD_vs_PICT_vs_BestLLM,df=2,%d,-,-,%.6f,%.6f,%s,Q=%.4f,-,-,WARNING: n=60 provides low statistical power (effect size must be large)%n",
                    n, qPval1, qPval1, qPval1 < 0.05 ? "YES" : "NO", qStat1);

            // 2. Cochran's Q test: TFD vs PICT_plain vs LLM Union
            double qStat2 = computeCochransQ(allDefectKeys, tfdDetected, pictPlainDetected, llmUnionDetected);
            double qPval2 = chiSquarePValue(2, qStat2);
            pw.printf(Locale.US, "Pooled_All_Apps,Cochran_Q,TFD_vs_PICT_vs_LLMUnion,df=2,%d,-,-,%.6f,%.6f,%s,Q=%.4f,-,-,WARNING: n=60 provides low statistical power%n",
                    n, qPval2, qPval2, qPval2 < 0.05 ? "YES" : "NO", qStat2);

            // Pairwise exact McNemar tests (5 pairs)
            List<McPair> pairs = new ArrayList<>();
            pairs.add(makePair("TFD", "PICT_plain", tfdDetected, pictPlainDetected, n, allDefectKeys));
            pairs.add(makePair("TFD", "Best_LLM", tfdDetected, bestLlmDetected, n, allDefectKeys));
            pairs.add(makePair("Best_LLM", "PICT_plain", bestLlmDetected, pictPlainDetected, n, allDefectKeys));
            pairs.add(makePair("TFD", "LLM_Union", tfdDetected, llmUnionDetected, n, allDefectKeys));
            pairs.add(makePair("LLM_Union", "PICT_plain", llmUnionDetected, pictPlainDetected, n, allDefectKeys));

            // Holm-Bonferroni correction
            pairs.sort(Comparator.comparingDouble(p -> p.pRaw));
            int M = pairs.size();
            for (int i = 0; i < M; i++) {
                double adjusted = pairs.get(i).pRaw * (M - i);
                if (i > 0 && adjusted < pairs.get(i - 1).pHolm) {
                    adjusted = pairs.get(i - 1).pHolm;
                }
                pairs.get(i).pHolm = Math.min(1.0, adjusted);
            }

            for (McPair p : pairs) {
                String orStr = Double.isInfinite(p.or) ? "INF" : String.format(Locale.US, "%.4f", p.or);
                pw.printf(Locale.US, "Pooled_All_Apps,Exact_McNemar,%s,%s,%d,%d,%d,%.6f,%.6f,%s,%s,%.4f,%.4f,WARNING: n=60 low power; do not interpret non-significance as equivalence%n",
                        p.nameA, p.nameB, n, p.b, p.c, p.pRaw, p.pHolm,
                        p.pHolm < 0.05 ? "YES" : "NO", orStr, p.rd, p.g);
            }
        }
    }

    private static double computeCochransQ(List<String> keys, Map<String, Boolean>... methods) {
        int k = methods.length;
        int n = keys.size();
        int[] colSums = new int[k];
        int[] rowSums = new int[n];
        int totalSum = 0;

        for (int i = 0; i < n; i++) {
            String key = keys.get(i);
            int rSum = 0;
            for (int j = 0; j < k; j++) {
                int val = methods[j].getOrDefault(key, false) ? 1 : 0;
                colSums[j] += val;
                rSum += val;
            }
            rowSums[i] = rSum;
            totalSum += rSum;
        }

        long sumColSq = 0;
        for (int c : colSums) sumColSq += (long) c * c;

        long sumRowSq = 0;
        for (int r : rowSums) sumRowSq += (long) r * r;

        double numerator = (k - 1) * (k * (double) sumColSq - (double) totalSum * totalSum);
        double denominator = k * (double) totalSum - (double) sumRowSq;
        if (denominator == 0) return 0.0;
        return numerator / denominator;
    }

    private static double chiSquarePValue(int df, double x) {
        if (x <= 0) return 1.0;
        // For df = 2, ChiSquare CDF is 1 - exp(-x/2)
        if (df == 2) {
            return Math.exp(-x / 2.0);
        }
        // General regularized upper gamma function approximation
        return gammaP(df / 2.0, x / 2.0);
    }

    private static double gammaP(double a, double x) {
        // Incomplete gamma Q(a, x) = 1 - P(a, x)
        // Using continued fraction approximation
        double gln = logGamma(a);
        double b = x + 1.0 - a;
        double c = 1.0 / 1.0e-30;
        double d = 1.0 / b;
        double h = d;
        for (int i = 1; i <= 100; i++) {
            double an = -i * (i - a);
            b += 2.0;
            d = an * d + b;
            if (Math.abs(d) < 1.0e-30) d = 1.0e-30;
            c = b + an / c;
            if (Math.abs(c) < 1.0e-30) c = 1.0e-30;
            d = 1.0 / d;
            double del = d * c;
            h *= del;
            if (Math.abs(del - 1.0) < 1.0e-7) break;
        }
        return Math.exp(-x + a * Math.log(x) - gln) * h;
    }

    private static double logGamma(double x) {
        double[] cof = {76.18009172947146, -86.50532032941677, 24.01409824083091,
                -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5};
        double y = x;
        double tmp = x + 5.5;
        tmp -= (x + 0.5) * Math.log(tmp);
        double ser = 1.000000000190015;
        for (int j = 0; j <= 5; j++) ser += cof[j] / ++y;
        return -tmp + Math.log(2.5066282746310005 * ser / x);
    }

    public static class McPair {
        String nameA;
        String nameB;
        int b;
        int c;
        double pRaw;
        double pHolm;
        double or;
        double rd;
        double g;
    }

    private static McPair makePair(String nameA, String nameB,
                                   Map<String, Boolean> mapA, Map<String, Boolean> mapB,
                                   int n, List<String> keys) {
        McPair p = new McPair();
        p.nameA = nameA;
        p.nameB = nameB;

        int b = 0;
        int c = 0;
        for (String k : keys) {
            boolean detA = mapA.getOrDefault(k, false);
            boolean detB = mapB.getOrDefault(k, false);
            if (detA && !detB) b++;
            if (!detA && detB) c++;
        }
        p.b = b;
        p.c = c;
        int m = b + c;

        if (m == 0) {
            p.pRaw = 1.0;
        } else {
            int k = Math.min(b, c);
            double sum = 0;
            for (int i = 0; i <= k; i++) {
                sum += nCr(m, i) * Math.pow(0.5, m);
            }
            p.pRaw = Math.min(1.0, 2.0 * sum);
        }

        p.or = (c == 0) ? (b == 0 ? 1.0 : Double.POSITIVE_INFINITY) : (double) b / c;
        p.rd = (double) (b - c) / n;
        p.g = m == 0 ? 0 : Math.abs((double) b / m - 0.5);

        return p;
    }

    private static double nCr(int n, int r) {
        if (r < 0 || r > n) return 0;
        if (r == 0 || r == n) return 1;
        if (r > n / 2) r = n - r;
        double res = 1.0;
        for (int i = 1; i <= r; i++) {
            res = res * (n - i + 1) / i;
        }
        return res;
    }

    private static void writeCostComparison(Path out, Path root, List<SuiteResult> suites) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out))) {
            pw.println("app,method,condition,time_minutes,time_type,tests_generated,detected_defects,defects_per_minute,notes");

            // Read generation_log.csv for LLM
            Path genLog = root.resolve("Logs/generation_log.csv");
            List<String> genLines = Files.readAllLines(genLog);
            Map<String, Double> llmMinutes = new HashMap<>(); // "app_condition" -> minutes
            for (int i = 1; i < genLines.size(); i++) {
                String line = genLines.get(i).trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",", -1);
                String app = p[1].trim();
                String cond = p[3].trim();
                String minStr = p[8].trim();
                if (!minStr.isEmpty()) {
                    llmMinutes.put(app + "_" + cond, Double.parseDouble(minStr));
                }
            }

            // TFD self-reported minutes from notes:
            Map<String, Double> tfdMinutes = new HashMap<>();
            tfdMinutes.put("BasicPackage", 45.0);
            tfdMinutes.put("BasicPayroll", 55.0);
            tfdMinutes.put("EnhancedPackage", 85.0);
            tfdMinutes.put("EnhancedPayroll", 95.0);

            for (SuiteResult sr : suites) {
                if (sr.method.equals("TFD")) {
                    double min = tfdMinutes.get(sr.app);
                    double dpm = min > 0 ? (double) sr.detectedDefects.size() / min : 0;
                    pw.printf(Locale.US, "%s,TFD,TFD,%.2f,self-reported,%d,%d,%.4f,\"Self-reported by external tester in TFD_tester_notes.txt\"%n",
                            sr.app, min, sr.totalTests, sr.detectedDefects.size(), dpm);
                } else if (sr.method.equals("PICT")) {
                    pw.printf(Locale.US, "%s,PICT,%s,0.01,automated_execution,%d,%d,%.4f,\"Pairwise combinatorial model generation <1s\"%n",
                            sr.app, sr.condition, sr.totalTests, sr.detectedDefects.size(), (double) sr.detectedDefects.size() / 0.01);
                } else if (sr.method.equals("LLM")) {
                    double min = llmMinutes.getOrDefault(sr.app + "_" + sr.condition, 0.0);
                    double dpm = min > 0 ? (double) sr.detectedDefects.size() / min : 0;
                    pw.printf(Locale.US, "%s,LLM,%s,%.2f,measured,%d,%d,%.4f,\"Measured wall-clock API generation time\"%n",
                            sr.app, sr.condition, min, sr.totalTests, sr.detectedDefects.size(), dpm);
                }
            }
        }
    }

    private static Map<String, Map<String, DefectMeta>> initDefectCatalogues() {
        Map<String, Map<String, DefectMeta>> cat = new LinkedHashMap<>();

        // BasicPackage
        Map<String, DefectMeta> bp = new LinkedHashMap<>();
        bp.put("D1", new DefectMeta("D1", "boundary", "Max weight limit reduced from 60 to 50 lbs"));
        bp.put("D2", new DefectMeta("D2", "boundary", "Validation of weight <= 0 removed"));
        bp.put("D3", new DefectMeta("D3", "arithmetic", "Same-zone rate increased from $2.00 to $3.00 per lb"));
        bp.put("D4", new DefectMeta("D4", "logic", "Zone pair (2,3)/(3,2) looked up with invalid key (2,5)"));
        bp.put("D5", new DefectMeta("D5", "logic", "Special handling of pair (1,4)/(4,1) removed"));
        bp.put("D6", new DefectMeta("D6", "arithmetic", "Priority Overnight multiplier increased from 1.2 to 2.2"));
        bp.put("D7", new DefectMeta("D7", "logic", "Multipliers of Same Day (2.1) and Standard Overnight (1.0) swapped"));
        bp.put("D8", new DefectMeta("D8", "arithmetic", "Residential surcharge increased from $10.00 to $20.00"));
        bp.put("D9", new DefectMeta("D9", "logic", "Delivery location type interpreted inversely (Business <-> Residence)"));
        bp.put("D10", new DefectMeta("D10", "logic", "Fragile condition inverted"));
        cat.put("BasicPackage", bp);

        // BasicPayroll
        Map<String, DefectMeta> bpay = new LinkedHashMap<>();
        bpay.put("D1", new DefectMeta("D1", "boundary", "Maximum hourly salary ($50) validation removed"));
        bpay.put("D2", new DefectMeta("D2", "arithmetic", "Social Security rate changed from 0.062 to 0.05"));
        bpay.put("D3", new DefectMeta("D3", "arithmetic", "Overtime multiplier changed from 1.5 to 1.25"));
        bpay.put("D4", new DefectMeta("D4", "boundary", "22% bracket ends at $35 instead of $40"));
        bpay.put("D5", new DefectMeta("D5", "logic", "Vision insurance deduction never applied"));
        bpay.put("D6", new DefectMeta("D6", "logic", "Medical Plan 2 and Plan 3 amounts swapped"));
        bpay.put("D7", new DefectMeta("D7", "logic", "Dental deduction uses medical plan amounts"));
        bpay.put("D8", new DefectMeta("D8", "logic", "Medical deduction uses LTD amount ($14)"));
        bpay.put("D9", new DefectMeta("D9", "logic", "United Way contribution ignored"));
        bpay.put("D10", new DefectMeta("D10", "logic", "Overtime hours ignored"));
        cat.put("BasicPayroll", bpay);

        // EnhancedPackage
        Map<String, DefectMeta> epkg = new LinkedHashMap<>();
        epkg.put("D1", new DefectMeta("D1", "arithmetic", "Zone 3-4 rate 2.30 -> 2.50"));
        epkg.put("D2", new DefectMeta("D2", "arithmetic", "Same-zone rate 2.00 -> 2.20"));
        epkg.put("D3", new DefectMeta("D3", "conditional", "N4 requires dangerous=1 instead of locationType=1"));
        epkg.put("D4", new DefectMeta("D4", "boundary", "Weight limit 60 -> 65"));
        epkg.put("D5", new DefectMeta("D5", "arithmetic", "Direct Signature fee 10.00 -> 12.00"));
        epkg.put("D6", new DefectMeta("D6", "conditional", "N6 exception for packaging 7-8 removed"));
        epkg.put("D7", new DefectMeta("D7", "conditional", "N7 allowed delivery options 4-7 instead of 5-7"));
        epkg.put("D8", new DefectMeta("D8", "logic", "Saturday Delivery fee charged when sat=0"));
        epkg.put("D9", new DefectMeta("D9", "conditional", "N2 discount applied to zone 1->3 instead of 1->4"));
        epkg.put("D10", new DefectMeta("D10", "conditional", "N10 removed"));
        epkg.put("D11", new DefectMeta("D11", "boundary", "N5 threshold weight>50 becomes weight>55"));
        epkg.put("D12", new DefectMeta("D12", "boundary", "Envelope limit w<=2 becomes w<2"));
        epkg.put("D13", new DefectMeta("D13", "arithmetic", "Packaging code 7 multiplier 1.15 -> 1.25"));
        epkg.put("D14", new DefectMeta("D14", "boundary", "Medium Box limit w<=25 becomes w<25"));
        epkg.put("D15", new DefectMeta("D15", "conditional", "N9 applies to destination zone 3 instead of 4"));
        epkg.put("D16", new DefectMeta("D16", "arithmetic", "N1 discount 10% -> 5%"));
        epkg.put("D17", new DefectMeta("D17", "conditional", "N3 exception removed"));
        epkg.put("D18", new DefectMeta("D18", "conditional", "N8 allows deliveryOption 5"));
        epkg.put("D19", new DefectMeta("D19", "logic", "Rate for pair (4,2) falls back to 2.00"));
        epkg.put("D20", new DefectMeta("D20", "logic", "Packaging 8 multiplier 1.30 applied to base rate only"));
        cat.put("EnhancedPackage", epkg);

        // EnhancedPayroll
        Map<String, DefectMeta> epay = new LinkedHashMap<>();
        epay.put("D1", new DefectMeta("D1", "arithmetic", "Overtime multiplier 1.5 -> 1.7"));
        epay.put("D2", new DefectMeta("D2", "conditional", "N8 credit requires holiday>0"));
        epay.put("D3", new DefectMeta("D3", "arithmetic", "Social Security 6.2% -> 6.5%"));
        epay.put("D4", new DefectMeta("D4", "logic", "Kentucky state tax 5% -> 4%"));
        epay.put("D5", new DefectMeta("D5", "boundary", "Max hourly limit $50 removed"));
        epay.put("D6", new DefectMeta("D6", "conditional", "N9 parking subsidy requires reg>=35 instead of >=30"));
        epay.put("D7", new DefectMeta("D7", "arithmetic", "Federal 22% bracket rate 22% -> 24%"));
        epay.put("D8", new DefectMeta("D8", "boundary", "N3 bonus requires holiday>5 instead of >=5"));
        epay.put("D9", new DefectMeta("D9", "conditional", "N5 high-earner rule threshold applied to grossPay"));
        epay.put("D10", new DefectMeta("D10", "logic", "N10 credit is subtracted instead of added"));
        epay.put("D11", new DefectMeta("D11", "boundary", "N2 triggers at overtime>=5 instead of >5"));
        epay.put("D12", new DefectMeta("D12", "boundary", "N1 overtime limit 10 -> 12"));
        epay.put("D13", new DefectMeta("D13", "arithmetic", "Medicare 1.45% -> 1.5%"));
        epay.put("D14", new DefectMeta("D14", "arithmetic", "Tennessee upper rate 8% -> 6%"));
        epay.put("D15", new DefectMeta("D15", "conditional", "N4 removed"));
        epay.put("D16", new DefectMeta("D16", "conditional", "N7 removed"));
        epay.put("D17", new DefectMeta("D17", "boundary", "Mississippi middle bracket starts at 25.00 instead of 25.01"));
        epay.put("D18", new DefectMeta("D18", "logic", "Pension cap computed on base pay instead of gross"));
        epay.put("D19", new DefectMeta("D19", "conditional", "N10 requires LTD OR STD instead of both"));
        epay.put("D20", new DefectMeta("D20", "logic", "N6 subsidy allows medical=1 even with dental=0"));
        cat.put("EnhancedPayroll", epay);

        return cat;
    }
}
