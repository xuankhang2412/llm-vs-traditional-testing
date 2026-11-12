package audit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class TaskAValidator {

    record SuiteInfo(String app, String method, String condition, String suiteName, Path path, int expectedQuota, String expectedHeader) {}

    public static void main(String[] args) throws Exception {
        Path root = Paths.get("D:/SWT301_TEST");
        Path specDir = root.resolve("Spec");
        Path suitesDir = root.resolve("Suites");
        Path logsDir = root.resolve("Logs");
        Path auditDir = root.resolve("Audit");

        String headerBasicPkg = "weight,origin,destination,deliveryOption,signatureOption,deliveryLocationType,saturdayDelivery,holdAtLocation,fragile,dangerous,expected";
        String headerBasicPay = "hourlySalary,regularHours,overtimeHours,holidayHours,medicalPlan,dentalPlan,visionPlan,unitedWay,pension,longTermDisability,shortTermDisability,parking,expected";
        String headerEnhPkg = "weight,origin,destination,deliveryOption,packaging,signature,locationType,saturday,hold,fragile,dangerous,expected";
        String headerEnhPay = "hourly,regularHours,overtimeHours,holidayHours,state,medical,dental,vision,unitedWay,pension,ltd,std,parking,expected";

        List<SuiteInfo> suites = new ArrayList<>();

        // BasicPackage
        suites.add(new SuiteInfo("BasicPackage", "PICT", "plain", "PICT_plain", suitesDir.resolve("BasicPackage/PICT_plain/suite.csv"), 20, headerBasicPkg));
        suites.add(new SuiteInfo("BasicPackage", "TFD", "human", "TFD", suitesDir.resolve("BasicPackage/TFD/suite.csv"), 20, headerBasicPkg));
        for (int c = 1; c <= 6; c++) {
            suites.add(new SuiteInfo("BasicPackage", "LLM", "C" + c, "LLM_C" + c, suitesDir.resolve("BasicPackage/LLM/gemini_C" + c + "_r1.csv"), 20, headerBasicPkg));
        }

        // BasicPayroll
        suites.add(new SuiteInfo("BasicPayroll", "PICT", "plain", "PICT_plain", suitesDir.resolve("BasicPayroll/PICT_plain/suite.csv"), 20, headerBasicPay));
        suites.add(new SuiteInfo("BasicPayroll", "TFD", "human", "TFD", suitesDir.resolve("BasicPayroll/TFD/suite.csv"), 20, headerBasicPay));
        for (int c = 1; c <= 6; c++) {
            suites.add(new SuiteInfo("BasicPayroll", "LLM", "C" + c, "LLM_C" + c, suitesDir.resolve("BasicPayroll/LLM/gemini_C" + c + "_r1.csv"), 20, headerBasicPay));
        }

        // EnhancedPackage
        suites.add(new SuiteInfo("EnhancedPackage", "PICT", "plain", "PICT_plain", suitesDir.resolve("EnhancedPackage/PICT_plain/suite.csv"), 35, headerEnhPkg));
        suites.add(new SuiteInfo("EnhancedPackage", "PICT", "constrained", "PICT_constrained", suitesDir.resolve("EnhancedPackage/PICT_constrained/suite.csv"), 35, headerEnhPkg));
        suites.add(new SuiteInfo("EnhancedPackage", "TFD", "human", "TFD", suitesDir.resolve("EnhancedPackage/TFD/suite.csv"), 35, headerEnhPkg));
        for (int c = 1; c <= 6; c++) {
            suites.add(new SuiteInfo("EnhancedPackage", "LLM", "C" + c, "LLM_C" + c, suitesDir.resolve("EnhancedPackage/LLM/gemini_C" + c + "_r1.csv"), 35, headerEnhPkg));
        }

        // EnhancedPayroll
        suites.add(new SuiteInfo("EnhancedPayroll", "PICT", "plain", "PICT_plain", suitesDir.resolve("EnhancedPayroll/PICT_plain/suite.csv"), 35, headerEnhPay));
        suites.add(new SuiteInfo("EnhancedPayroll", "PICT", "constrained", "PICT_constrained", suitesDir.resolve("EnhancedPayroll/PICT_constrained/suite.csv"), 35, headerEnhPay));
        suites.add(new SuiteInfo("EnhancedPayroll", "TFD", "human", "TFD", suitesDir.resolve("EnhancedPayroll/TFD/suite.csv"), 35, headerEnhPay));
        for (int c = 1; c <= 6; c++) {
            suites.add(new SuiteInfo("EnhancedPayroll", "LLM", "C" + c, "LLM_C" + c, suitesDir.resolve("EnhancedPayroll/LLM/gemini_C" + c + "_r1.csv"), 35, headerEnhPay));
        }

        // Load generation log
        Path genLogPath = logsDir.resolve("generation_log.csv");
        Set<String> genLogKeys = new HashSet<>();
        if (Files.exists(genLogPath)) {
            List<String> logLines = Files.readAllLines(genLogPath, StandardCharsets.UTF_8);
            for (String l : logLines) {
                String[] parts = l.split(",");
                if (parts.length >= 4) {
                    // app_level, method, condition
                    genLogKeys.add(parts[1].trim() + "|" + parts[2].trim() + "|" + parts[3].trim());
                }
            }
        }

        // Load reference examples to check for copying
        Map<String, Set<String>> exampleRowsPerApp = new HashMap<>();
        exampleRowsPerApp.put("BasicPackage", loadExampleInputs(specDir, "BasicPackage", 10));
        exampleRowsPerApp.put("BasicPayroll", loadExampleInputs(specDir, "BasicPayroll", 12));
        exampleRowsPerApp.put("EnhancedPackage", loadExampleInputs(specDir, "EnhancedPackage", 11));
        exampleRowsPerApp.put("EnhancedPayroll", loadExampleInputs(specDir, "EnhancedPayroll", 13));

        StringBuilder reportMd = new StringBuilder();
        reportMd.append("# Task A - Test Suites Inventory and Validation Report\n\n");
        reportMd.append("Date: 2026-11-05\n\n");
        reportMd.append("Total suites evaluated: ").append(suites.size()).append("\n\n");

        StringBuilder csvSummary = new StringBuilder();
        csvSummary.append("app,method,condition,suite_name,file_exists,data_rows,expected_quota,header_match,header_actual,consistent_cols,invalid_inputs_count,error_format_valid,error_rows_count,duplicate_rows_count,copied_example_rows_count,in_generation_log\n");

        int filesChecked = 0;
        int filesMissing = 0;

        Pattern decimalPattern = Pattern.compile("^-?\\d+\\.\\d{2}$");

        for (SuiteInfo s : suites) {
            filesChecked++;
            boolean exists = Files.exists(s.path());
            if (!exists) {
                filesMissing++;
                reportMd.append("### ").append(s.app()).append(" - ").append(s.suiteName()).append("\n");
                reportMd.append("- **Path**: `").append(s.path()).append("`\n");
                reportMd.append("- **Status**: **MISSING FILE**\n\n");
                csvSummary.append(String.format("%s,%s,%s,%s,NO,0,%d,N/A,N/A,N/A,0,N/A,0,0,0,NO\n",
                        s.app(), s.method(), s.condition(), s.suiteName(), s.expectedQuota()));
                continue;
            }

            List<String> rawLines = Files.readAllLines(s.path(), StandardCharsets.UTF_8);
            List<String> lines = new ArrayList<>();
            for (String l : rawLines) {
                String t = l.trim();
                if (!t.isEmpty() && !t.startsWith("#") && !t.startsWith("```")) {
                    lines.add(t);
                }
            }

            String actualHeader = lines.isEmpty() ? "" : lines.get(0);
            boolean headerMatch = actualHeader.equals(s.expectedHeader());

            int dataRows = Math.max(0, lines.size() - 1);
            int expectedCols = s.expectedHeader().split(",").length;

            boolean consistentCols = true;
            int errorRowsCount = 0;
            int invalidFormatExpectedCount = 0;
            int duplicateRows = 0;
            int copiedExamples = 0;
            int invalidCodedOrDomainCount = 0;

            Set<String> seenInputVectors = new HashSet<>();
            List<String> copiedDetails = new ArrayList<>();
            List<String> formatIssues = new ArrayList<>();
            List<String> domainViolations = new ArrayList<>();

            for (int r = 1; r < lines.size(); r++) {
                String row = lines.get(r);
                String[] cols = row.split(",", -1);
                if (cols.length != expectedCols) {
                    // Check if it's an unnormalized file with extra leading test_id
                    consistentCols = false;
                }

                // Check expected column
                String expectedVal = cols[cols.length - 1].trim();
                if (expectedVal.equalsIgnoreCase("ERROR")) {
                    errorRowsCount++;
                } else if (!decimalPattern.matcher(expectedVal).matches()) {
                    invalidFormatExpectedCount++;
                    formatIssues.add("Row " + r + ": '" + expectedVal + "'");
                }

                // Normalize input vector key for duplicate & copy detection
                // If extra column (like test_id at col 0), skip col 0
                int startCol = (cols.length == expectedCols + 1) ? 1 : 0;
                int endCol = cols.length - 1;
                StringBuilder inputVec = new StringBuilder();
                for (int c = startCol; c < endCol; c++) {
                    if (c > startCol) inputVec.append(",");
                    inputVec.append(cols[c].trim());
                }
                String vecStr = inputVec.toString();

                if (!seenInputVectors.add(vecStr)) {
                    duplicateRows++;
                }

                Set<String> appExamples = exampleRowsPerApp.get(s.app());
                if (appExamples != null && appExamples.contains(vecStr)) {
                    copiedExamples++;
                    copiedDetails.add("Row " + r + " inputs: " + vecStr);
                }

                // Validate domain / coded values
                String domainCheck = validateDomain(s.app(), cols, startCol);
                if (domainCheck != null) {
                    invalidCodedOrDomainCount++;
                    domainViolations.add("Row " + r + ": " + domainCheck);
                }
            }

            boolean inGenLog = genLogKeys.contains(s.app() + "|" + s.method() + "|" + s.condition());

            reportMd.append("### ").append(s.app()).append(" - ").append(s.suiteName()).append("\n");
            reportMd.append("- **Path**: `").append(s.path()).append("`\n");
            reportMd.append("- **Data Rows**: ").append(dataRows).append(" (Quota: ").append(s.expectedQuota()).append(")\n");
            reportMd.append("- **Header Match**: ").append(headerMatch ? "EXACT MATCH" : "**MISMATCH**").append("\n");
            if (!headerMatch) {
                reportMd.append("  - *Expected*: `").append(s.expectedHeader()).append("`\n");
                reportMd.append("  - *Actual*:   `").append(actualHeader).append("`\n");
            }
            reportMd.append("- **Consistent Columns per Row**: ").append(consistentCols ? "Yes (" + expectedCols + " cols)" : "No (has differing column count)").append("\n");
            reportMd.append("- **Expected Column Format**: ").append(invalidFormatExpectedCount == 0 ? "100% Valid (ERROR or number with 2 decimals)" : "Issues (" + invalidFormatExpectedCount + " rows: " + formatIssues + ")").append("\n");
            reportMd.append("- **ERROR Rows**: ").append(errorRowsCount).append(" / ").append(dataRows).append("\n");
            reportMd.append("- **Domain / Coded Value Violations (Invalid inputs)**: ").append(invalidCodedOrDomainCount).append("\n");
            if (!domainViolations.isEmpty() && domainViolations.size() <= 5) {
                reportMd.append("  - Details: ").append(domainViolations).append("\n");
            } else if (domainViolations.size() > 5) {
                reportMd.append("  - Details: ").append(domainViolations.subList(0, 5)).append("... (and ").append(domainViolations.size() - 5).append(" more)\n");
            }
            reportMd.append("- **Duplicate Input Rows**: ").append(duplicateRows).append("\n");
            reportMd.append("- **Copied from Prompts/Spec Examples**: ").append(copiedExamples).append("\n");
            if (!copiedDetails.isEmpty()) {
                reportMd.append("  - Copied rows: ").append(copiedDetails).append("\n");
            }
            reportMd.append("- **Present in generation_log.csv**: ").append(inGenLog ? "YES" : "**MISSING**").append("\n\n");

            csvSummary.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",YES,%d,%d,%s,\"%s\",%s,%d,%s,%d,%d,%d,%s\n",
                    s.app(), s.method(), s.condition(), s.suiteName(), dataRows, s.expectedQuota(),
                    headerMatch ? "YES" : "NO", actualHeader.replace("\"", "\"\""),
                    consistentCols ? "YES" : "NO", invalidCodedOrDomainCount,
                    invalidFormatExpectedCount == 0 ? "YES" : "NO",
                    errorRowsCount, duplicateRows, copiedExamples, inGenLog ? "YES" : "NO"));
        }

        Files.writeString(auditDir.resolve("inventory_and_validation.md"), reportMd.toString(), StandardCharsets.UTF_8);
        Files.writeString(auditDir.resolve("inventory_and_validation.csv"), csvSummary.toString(), StandardCharsets.UTF_8);

        System.out.println("Inventory and validation finished successfully!");
        System.out.println("Wrote Audit/inventory_and_validation.md and Audit/inventory_and_validation.csv");
        System.out.println("Total suites checked: " + filesChecked + " (Missing files: " + filesMissing + ")");
    }

    private static String validateDomain(String app, String[] cols, int offset) {
        try {
            if (app.equals("BasicPackage")) {
                if (cols.length < offset + 10) return "Not enough columns";
                double w = Double.parseDouble(cols[offset].trim());
                int orig = Integer.parseInt(cols[offset + 1].trim());
                int dest = Integer.parseInt(cols[offset + 2].trim());
                int opt = Integer.parseInt(cols[offset + 3].trim());
                int sig = Integer.parseInt(cols[offset + 4].trim());
                int loc = Integer.parseInt(cols[offset + 5].trim());
                int sat = Integer.parseInt(cols[offset + 6].trim());
                int hold = Integer.parseInt(cols[offset + 7].trim());
                int frag = Integer.parseInt(cols[offset + 8].trim());
                int dang = Integer.parseInt(cols[offset + 9].trim());

                if (w <= 0.0 || w > 60.0) return "Weight out of (0, 60]: " + w;
                if (orig < 1 || orig > 4) return "Origin out of 1-4: " + orig;
                if (dest < 1 || dest > 4) return "Dest out of 1-4: " + dest;
                if (opt < 1 || opt > 7) return "DeliveryOption out of 1-7: " + opt;
                if (sig < 0 || sig > 2) return "Signature out of 0-2: " + sig;
                if (loc < 0 || loc > 1) return "LocationType out of 0-1: " + loc;
                if (sat < 0 || sat > 1) return "Saturday out of 0-1: " + sat;
                if (hold < 0 || hold > 1) return "Hold out of 0-1: " + hold;
                if (frag < 0 || frag > 1) return "Fragile out of 0-1: " + frag;
                if (dang < 0 || dang > 1) return "Dangerous out of 0-1: " + dang;
            } else if (app.equals("BasicPayroll")) {
                if (cols.length < offset + 12) return "Not enough columns";
                double wage = Double.parseDouble(cols[offset].trim());
                double regH = Double.parseDouble(cols[offset + 1].trim());
                double otH = Double.parseDouble(cols[offset + 2].trim());
                double holH = Double.parseDouble(cols[offset + 3].trim());
                int med = Integer.parseInt(cols[offset + 4].trim());
                int den = Integer.parseInt(cols[offset + 5].trim());
                int vis = Integer.parseInt(cols[offset + 6].trim());
                double uw = Double.parseDouble(cols[offset + 7].trim());
                double pen = Double.parseDouble(cols[offset + 8].trim());
                int ltd = Integer.parseInt(cols[offset + 9].trim());
                int std = Integer.parseInt(cols[offset + 10].trim());
                int park = Integer.parseInt(cols[offset + 11].trim());

                if (wage < 15.00 || wage > 50.00) return "Hourly salary out of [15, 50]: " + wage;
                if (regH < 0.0 || otH < 0.0 || holH < 0.0) return "Negative hours: " + regH + "/" + otH + "/" + holH;
                if (med < 0 || med > 3) return "Medical out of 0-3: " + med;
                if (den < 0 || den > 2) return "Dental out of 0-2: " + den;
                if (vis < 0 || vis > 2) return "Vision out of 0-2: " + vis;
                if (uw < 0.0) return "Negative UnitedWay: " + uw;
                if (pen < 0.0) return "Negative pension: " + pen;
                if (ltd < 0 || ltd > 1) return "LTD out of 0-1: " + ltd;
                if (std < 0 || std > 1) return "STD out of 0-1: " + std;
                if (park < 0 || park > 1) return "Parking out of 0-1: " + park;
            } else if (app.equals("EnhancedPackage")) {
                if (cols.length < offset + 11) return "Not enough columns";
                double w = Double.parseDouble(cols[offset].trim());
                int orig = Integer.parseInt(cols[offset + 1].trim());
                int dest = Integer.parseInt(cols[offset + 2].trim());
                int opt = Integer.parseInt(cols[offset + 3].trim());
                int pkg = Integer.parseInt(cols[offset + 4].trim());
                int sig = Integer.parseInt(cols[offset + 5].trim());
                int loc = Integer.parseInt(cols[offset + 6].trim());
                int sat = Integer.parseInt(cols[offset + 7].trim());
                int hold = Integer.parseInt(cols[offset + 8].trim());
                int frag = Integer.parseInt(cols[offset + 9].trim());
                int dang = Integer.parseInt(cols[offset + 10].trim());

                if (w <= 0.0 || w > 60.0) return "Weight out of (0, 60]: " + w;
                if (orig < 1 || orig > 4) return "Origin out of 1-4: " + orig;
                if (dest < 1 || dest > 4) return "Dest out of 1-4: " + dest;
                if (opt < 1 || opt > 7) return "DeliveryOption out of 1-7: " + opt;
                if (pkg < 1 || pkg > 8) return "Packaging out of 1-8: " + pkg;
                if (sig < 0 || sig > 2) return "Signature out of 0-2: " + sig;
                if (loc < 0 || loc > 1) return "LocationType out of 0-1: " + loc;
                if (sat < 0 || sat > 1) return "Saturday out of 0-1: " + sat;
                if (hold < 0 || hold > 1) return "Hold out of 0-1: " + hold;
                if (frag < 0 || frag > 1) return "Fragile out of 0-1: " + frag;
                if (dang < 0 || dang > 1) return "Dangerous out of 0-1: " + dang;
            } else if (app.equals("EnhancedPayroll")) {
                if (cols.length < offset + 13) return "Not enough columns";
                double hourly = Double.parseDouble(cols[offset].trim());
                double regH = Double.parseDouble(cols[offset + 1].trim());
                double otH = Double.parseDouble(cols[offset + 2].trim());
                double holH = Double.parseDouble(cols[offset + 3].trim());
                int state = Integer.parseInt(cols[offset + 4].trim());
                int med = Integer.parseInt(cols[offset + 5].trim());
                int den = Integer.parseInt(cols[offset + 6].trim());
                int vis = Integer.parseInt(cols[offset + 7].trim());
                double uw = Double.parseDouble(cols[offset + 8].trim());
                double pen = Double.parseDouble(cols[offset + 9].trim());
                int ltd = Integer.parseInt(cols[offset + 10].trim());
                int std = Integer.parseInt(cols[offset + 11].trim());
                int park = Integer.parseInt(cols[offset + 12].trim());

                if (hourly < 15.00 || hourly > 50.00) return "Hourly out of [15, 50]: " + hourly;
                if (regH < 0.0 || otH < 0.0 || holH < 0.0) return "Negative hours: " + regH + "/" + otH + "/" + holH;
                if (state < 1 || state > 3) return "State out of 1-3: " + state;
                if (med < 0 || med > 3) return "Medical out of 0-3: " + med;
                if (den < 0 || den > 2) return "Dental out of 0-2: " + den;
                if (vis < 0 || vis > 2) return "Vision out of 0-2: " + vis;
                if (uw < 0.0) return "Negative UnitedWay: " + uw;
                if (pen < 0.0) return "Negative pension: " + pen;
                if (ltd < 0 || ltd > 1) return "LTD out of 0-1: " + ltd;
                if (std < 0 || std > 1) return "STD out of 0-1: " + std;
                if (park < 0 || park > 1) return "Parking out of 0-1: " + park;
            }
        } catch (Exception e) {
            return "Parsing exception: " + e.getMessage();
        }
        return null;
    }

    private static Set<String> loadExampleInputs(Path specDir, String app, int inputColCount) throws Exception {
        Set<String> set = new HashSet<>();
        Path exDir = specDir.resolve("llm_prompts/examples");
        for (String type : List.of("one_shot.csv", "few_shot.csv")) {
            Path p = exDir.resolve(app + "_" + type);
            if (Files.exists(p)) {
                List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
                for (int i = 1; i < lines.size(); i++) {
                    String[] cols = lines.get(i).split(",");
                    if (cols.length >= inputColCount) {
                        StringBuilder sb = new StringBuilder();
                        for (int c = 0; c < inputColCount; c++) {
                            if (c > 0) sb.append(",");
                            sb.append(cols[c].trim());
                        }
                        set.add(sb.toString());
                    }
                }
            }
        }
        // Also add worked examples from spec if any
        if (app.equals("BasicPackage")) {
            set.add("1.05,1,2,3,0,0,0,0,0,0");
        } else if (app.equals("BasicPayroll")) {
            set.add("17.35,37.5,0.0,0.0,0,0,0,0.0,0.0,0,0,0");
        } else if (app.equals("EnhancedPackage")) {
            set.add("1.05,1,1,3,7,0,1,0,0,0,0");
        } else if (app.equals("EnhancedPayroll")) {
            set.add("24.35,37.5,0.0,0.0,1,0,0,0,0.0,0.0,0,0,0");
        }
        return set;
    }
}

