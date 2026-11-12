package com.payroll.enhanced;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class DefectRunner {

    public static class TestCase {
        String testId;
        double hourly;
        double regularHours;
        double overtimeHours;
        double holidayHours;
        int state;
        int medical;
        int dental;
        int vision;
        double unitedWay;
        double pension;
        int ltd;
        int std;
        int parking;
        String expectedNet; // numeric (e.g. 592.92) or ERROR
    }

    public static void main(String[] args) {
        String inputCsvPath = (args.length > 0) ? args[0] : "testcases/sample_payroll_tests.csv";
        String outputCsvPath = (args.length > 1) ? args[1] : "results/defect_matrix.csv";

        File inputFile = new File(inputCsvPath);
        if (!inputFile.exists()) {
            inputFile = new File("D:/SWT301_TEST/Project4/" + inputCsvPath);
        }
        if (!inputFile.exists()) {
            System.err.println("Input CSV file not found: " + inputCsvPath);
            return;
        }

        File outputFile = new File(outputCsvPath);
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }

        List<TestCase> testCases = loadTestCases(inputFile);
        System.out.println("Loaded " + testCases.size() + " test cases from " + inputFile.getPath());

        Defect[] defects = Defect.values();
        String[][] matrix = new String[testCases.size()][defects.length];

        Map<Defect, List<String>> failingCasesPerDefect = new LinkedHashMap<>();
        for (Defect defect : defects) {
            failingCasesPerDefect.put(defect, new ArrayList<>());
        }

        for (int d = 0; d < defects.length; d++) {
            Defect defect = defects[d];
            PayrollCalculator calculator = new PayrollCalculator(defect);

            for (int t = 0; t < testCases.size(); t++) {
                TestCase tc = testCases.get(t);
                boolean pass = executeTestCase(calculator, tc);
                matrix[t][d] = pass ? "PASS" : "FAIL";
                if (!pass) {
                    failingCasesPerDefect.get(defect).add(tc.testId);
                }
            }
        }

        System.out.println("\n=======================================================");
        System.out.println("DEFECT EXECUTION RESULTS (Actual vs Expected)");
        System.out.println("=======================================================");
        for (Defect defect : defects) {
            List<String> failed = failingCasesPerDefect.get(defect);
            if (failed.isEmpty()) {
                System.out.printf("Defect %-5s : All test cases PASSED (0 failures)%n", defect);
            } else {
                System.out.printf("Defect %-5s : FAILS on %d test case(s) -> %s%n",
                        defect, failed.size(), String.join(", ", failed));
            }
        }

        writeMatrixCsv(outputFile, testCases, defects, matrix);
        System.out.println("\nDefect matrix written to: " + outputFile.getPath());

        int detectedCount = 0;
        for (Defect defect : defects) {
            if (defect == Defect.NONE) continue;
            if (!failingCasesPerDefect.get(defect).isEmpty()) {
                detectedCount++;
            }
        }
        System.out.println("=======================================================");
        System.out.println("SUMMARY: Detected " + detectedCount + " / 20 seeded defects.");
        if (failingCasesPerDefect.get(Defect.NONE).isEmpty()) {
            System.out.println("Defect NONE (clean baseline): 100% PASS on all test cases.");
        } else {
            System.err.println("WARNING: Defect NONE had failures! Baseline is not clean.");
        }
        System.out.println("=======================================================\n");
    }

    private static boolean executeTestCase(PayrollCalculator calculator, TestCase tc) {
        try {
            PayrollInput input = new PayrollInput(
                    tc.hourly, tc.regularHours, tc.overtimeHours, tc.holidayHours,
                    tc.state, tc.medical, tc.dental, tc.vision,
                    tc.unitedWay, tc.pension, tc.ltd, tc.std, tc.parking);

            PayrollResult result = calculator.calculate(input);

            if ("ERROR".equalsIgnoreCase(tc.expectedNet)) {
                return false;
            }

            BigDecimal expected = new BigDecimal(tc.expectedNet).setScale(2, RoundingMode.HALF_UP);
            BigDecimal actual = result.netPay().setScale(2, RoundingMode.HALF_UP);
            return actual.compareTo(expected) == 0;
        } catch (IllegalArgumentException e) {
            return "ERROR".equalsIgnoreCase(tc.expectedNet);
        }
    }

    private static List<TestCase> loadTestCases(File file) {
        List<TestCase> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("testid") || line.toLowerCase().startsWith("hourly")) continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 14) continue;

                TestCase tc = new TestCase();
                int idx = 0;
                if (parts.length >= 15) {
                    tc.testId = parts[idx++].trim();
                } else {
                    tc.testId = "TC" + (list.size() + 1);
                }
                tc.hourly = Double.parseDouble(parts[idx++].trim());
                tc.regularHours = Double.parseDouble(parts[idx++].trim());
                tc.overtimeHours = Double.parseDouble(parts[idx++].trim());
                tc.holidayHours = Double.parseDouble(parts[idx++].trim());
                tc.state = Integer.parseInt(parts[idx++].trim());
                tc.medical = Integer.parseInt(parts[idx++].trim());
                tc.dental = Integer.parseInt(parts[idx++].trim());
                tc.vision = Integer.parseInt(parts[idx++].trim());
                tc.unitedWay = Double.parseDouble(parts[idx++].trim());
                tc.pension = Double.parseDouble(parts[idx++].trim());
                tc.ltd = Integer.parseInt(parts[idx++].trim());
                tc.std = Integer.parseInt(parts[idx++].trim());
                tc.parking = Integer.parseInt(parts[idx++].trim());
                tc.expectedNet = parts[idx].trim();

                list.add(tc);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read testcases: " + e.getMessage(), e);
        }
        return list;
    }

    private static void writeMatrixCsv(File outputFile, List<TestCase> testCases, Defect[] defects, String[][] matrix) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            StringBuilder sb = new StringBuilder("testId");
            for (Defect defect : defects) {
                sb.append(",").append(defect.name());
            }
            writer.println(sb.toString());

            for (int t = 0; t < testCases.size(); t++) {
                sb = new StringBuilder(testCases.get(t).testId);
                for (int d = 0; d < defects.length; d++) {
                    sb.append(",").append(matrix[t][d]);
                }
                writer.println(sb.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write matrix CSV: " + e.getMessage(), e);
        }
    }
}

