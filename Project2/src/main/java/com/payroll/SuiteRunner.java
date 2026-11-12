package com.payroll;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.util.*;

public class SuiteRunner {

    public static class TestCase {
        String testId;
        double hourlySalary;
        double regularHours;
        double overtimeHours;
        double holidayHours;
        int medicalPlan;
        int dentalPlan;
        int visionPlan;
        double unitedWay;
        double pension;
        int longTermDisability;
        int shortTermDisability;
        int parking;
        String expected; // null or trimmed string
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -cp target/classes com.payroll.SuiteRunner <suite.csv> <outDir>");
            System.exit(1);
        }

        File suiteFile = new File(args[0]);
        if (!suiteFile.exists()) {
            System.err.println("Suite file not found: " + suiteFile.getAbsolutePath());
            System.exit(1);
        }

        File outDir = new File(args[1]);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        String suiteName = suiteFile.getName().replaceFirst("[.][^.]+$", "");
        if ("suite".equalsIgnoreCase(suiteName) && suiteFile.getParentFile() != null) {
            suiteName = suiteFile.getParentFile().getName();
        }

        List<TestCase> testCases = loadTestCases(suiteFile);

        PayrollDefect[] allDefects = PayrollDefect.values();
        List<PayrollDefect> seededDefects = new ArrayList<>();
        for (PayrollDefect d : allDefects) {
            if (d != PayrollDefect.NONE) {
                seededDefects.add(d);
            }
        }

        int invalidTestsCount = 0;
        int baselineFailuresCount = 0;
        int testsWithExpectedCount = 0;
        Set<PayrollDefect> detectedDefects = new LinkedHashSet<>();

        File matrixFile = new File(outDir, "matrix_long.csv");

        try (PrintWriter pw = new PrintWriter(new FileWriter(matrixFile))) {
            pw.println("suite,test_id,defect,HIT_REF,HIT_EXP,BASELINE_OK");

            for (TestCase tc : testCases) {
                // Run NONE
                String noneOutput = runTest(PayrollDefect.NONE, tc);
                boolean isInvalid = "ERROR".equals(noneOutput);
                if (isInvalid) {
                    invalidTestsCount++;
                }

                String baselineOkStr = "";
                if (tc.expected != null && !tc.expected.isEmpty()) {
                    testsWithExpectedCount++;
                    boolean baselineOk = matchesExpected(noneOutput, tc.expected);
                    baselineOkStr = baselineOk ? "1" : "0";
                    if (!baselineOk) {
                        baselineFailuresCount++;
                    }
                }

                // Run each defect Dk
                for (PayrollDefect defect : seededDefects) {
                    String dkOutput = runTest(defect, tc);

                    // HIT_REF = output/error of Dk differs from NONE at 2 decimals
                    boolean hitRef = differs(dkOutput, noneOutput);
                    int hitRefVal = hitRef ? 1 : 0;
                    if (hitRef) {
                        detectedDefects.add(defect);
                    }

                    // HIT_EXP = Dk differs from the creator's "expected" column (only when expected is not empty)
                    String hitExpStr = "";
                    if (tc.expected != null && !tc.expected.isEmpty()) {
                        boolean hitExp = !matchesExpected(dkOutput, tc.expected);
                        hitExpStr = hitExp ? "1" : "0";
                    }

                    pw.printf("%s,%s,%s,%d,%s,%s%n",
                            suiteName, tc.testId, defect.name(), hitRefVal, hitExpStr, baselineOkStr);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write matrix_long.csv: " + e.getMessage(), e);
        }

        // Summary printed to console
        System.out.println("=======================================================");
        System.out.println("SUITE EXECUTION SUMMARY: " + suiteName);
        System.out.println("=======================================================");
        System.out.println("Total test cases: " + testCases.size());
        System.out.println("Number of invalid tests: " + invalidTestsCount);
        if (testsWithExpectedCount > 0) {
            System.out.println("Number of tests that fail on the baseline: " + baselineFailuresCount + " / " + testsWithExpectedCount);
        } else {
            System.out.println("Number of tests that fail on the baseline: 0 (unlabelled suite, expected column empty)");
        }
        System.out.printf("Defects detected / total: %d / %d%n", detectedDefects.size(), seededDefects.size());
        System.out.println("Matrix written to: " + matrixFile.getAbsolutePath());
        System.out.println("=======================================================");
    }

    private static String runTest(PayrollDefect defect, TestCase tc) {
        try {
            PayrollCalculator calc = new PayrollCalculator(defect);
            PayrollInput input = new PayrollInput(
                    tc.hourlySalary, tc.regularHours, tc.overtimeHours, tc.holidayHours,
                    tc.medicalPlan, tc.dentalPlan, tc.visionPlan,
                    tc.unitedWay, tc.pension,
                    tc.longTermDisability, tc.shortTermDisability, tc.parking);
            PayrollResult res = calc.calculate(input);
            return res.getNetSalary().setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (IllegalArgumentException e) {
            return "ERROR";
        }
    }

    private static boolean differs(String out1, String out2) {
        if ("ERROR".equals(out1) && "ERROR".equals(out2)) {
            return false;
        }
        if ("ERROR".equals(out1) || "ERROR".equals(out2)) {
            return true;
        }
        BigDecimal bd1 = new BigDecimal(out1).setScale(2, RoundingMode.HALF_UP);
        BigDecimal bd2 = new BigDecimal(out2).setScale(2, RoundingMode.HALF_UP);
        return bd1.compareTo(bd2) != 0;
    }

    private static boolean matchesExpected(String actual, String expected) {
        if ("ERROR".equalsIgnoreCase(expected)) {
            return "ERROR".equals(actual);
        }
        if ("ERROR".equals(actual)) {
            return false;
        }
        try {
            BigDecimal actBd = new BigDecimal(actual).setScale(2, RoundingMode.HALF_UP);
            BigDecimal expBd = new BigDecimal(expected).setScale(2, RoundingMode.HALF_UP);
            return actBd.compareTo(expBd) == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static List<TestCase> loadTestCases(File file) {
        List<TestCase> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            boolean hasTestIdCol = false;
            int rowNum = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (firstLine) {
                    firstLine = false;
                    String lower = line.toLowerCase();
                    if (lower.startsWith("testid") || lower.startsWith("test_id") || lower.startsWith("tc")) {
                        hasTestIdCol = true;
                        continue;
                    } else if (lower.startsWith("hourly")) {
                        hasTestIdCol = false;
                        continue;
                    }
                }

                rowNum++;
                String[] parts = line.split(",", -1);
                TestCase tc = new TestCase();

                int offset = 0;
                if (hasTestIdCol) {
                    tc.testId = parts[0].trim();
                    offset = 1;
                } else {
                    tc.testId = String.format("TC%02d", rowNum);
                    offset = 0;
                }

                tc.hourlySalary = Double.parseDouble(parts[offset].trim());
                tc.regularHours = Double.parseDouble(parts[offset + 1].trim());
                tc.overtimeHours = Double.parseDouble(parts[offset + 2].trim());
                tc.holidayHours = Double.parseDouble(parts[offset + 3].trim());
                tc.medicalPlan = Integer.parseInt(parts[offset + 4].trim());
                tc.dentalPlan = Integer.parseInt(parts[offset + 5].trim());
                tc.visionPlan = Integer.parseInt(parts[offset + 6].trim());
                tc.unitedWay = Double.parseDouble(parts[offset + 7].trim());
                tc.pension = Double.parseDouble(parts[offset + 8].trim());
                tc.longTermDisability = Integer.parseInt(parts[offset + 9].trim());
                tc.shortTermDisability = Integer.parseInt(parts[offset + 10].trim());
                tc.parking = Integer.parseInt(parts[offset + 11].trim());

                if (parts.length > offset + 12) {
                    String exp = parts[offset + 12].trim();
                    tc.expected = exp.isEmpty() ? null : exp;
                } else {
                    tc.expected = null;
                }

                list.add(tc);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read suite CSV: " + e.getMessage(), e);
        }
        return list;
    }
}

