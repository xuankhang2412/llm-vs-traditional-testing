package com.shipping;

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
        double weight;
        int origin;
        int destination;
        int deliveryOption;
        int signatureOption;
        int deliveryLocationType;
        int saturdayDelivery;
        int holdAtLocation;
        int fragile;
        int dangerous;
        String expectedOutput; // numeric (e.g. 20.00) or ERROR
    }

    public static void main(String[] args) {
        String inputCsvPath = (args.length > 0) ? args[0] : "testcases/input.csv";
        String outputCsvPath = (args.length > 1) ? args[1] : "results/defect_matrix.csv";

        File inputFile = new File(inputCsvPath);
        if (!inputFile.exists()) {
            inputFile = new File("D:/SWT301_TEST/Project1/" + inputCsvPath);
        }
        if (!inputFile.exists()) {
            System.err.println("Input CSV file not found: " + inputCsvPath);
            System.exit(1);
        }

        File outputFile = new File(outputCsvPath);
        if (!outputFile.isAbsolute()) {
            File parent = inputFile.getParentFile();
            if (parent != null && parent.getParentFile() != null) {
                outputFile = new File(parent.getParentFile(), outputCsvPath);
            }
        }
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }

        List<TestCase> testCases = loadTestCases(inputFile);
        System.out.println("Loaded " + testCases.size() + " test cases from " + inputFile.getPath());

        PackageDefect[] defects = PackageDefect.values();
        // matrix[testCaseIndex][defectIndex] = "PASS" / "FAIL"
        String[][] matrix = new String[testCases.size()][defects.length];

        Map<PackageDefect, List<String>> failingCasesPerDefect = new LinkedHashMap<>();
        for (PackageDefect defect : defects) {
            failingCasesPerDefect.put(defect, new ArrayList<>());
        }

        for (int d = 0; d < defects.length; d++) {
            PackageDefect defect = defects[d];
            ShippingCalculator calculator = new ShippingCalculator(defect);

            for (int t = 0; t < testCases.size(); t++) {
                TestCase tc = testCases.get(t);
                boolean pass = executeTestCase(calculator, tc);
                matrix[t][d] = pass ? "PASS" : "FAIL";
                if (!pass) {
                    failingCasesPerDefect.get(defect).add(tc.testId);
                }
            }
        }

        // Print failing test case IDs per defect
        System.out.println("\n=======================================================");
        System.out.println("DEFECT EXECUTION RESULTS (Actual vs Expected)");
        System.out.println("=======================================================");
        for (PackageDefect defect : defects) {
            List<String> failed = failingCasesPerDefect.get(defect);
            if (failed.isEmpty()) {
                System.out.printf("Defect %-5s : All test cases PASSED (0 failures)%n", defect);
            } else {
                System.out.printf("Defect %-5s : FAILS on %d test case(s) -> %s%n",
                        defect, failed.size(), String.join(", ", failed));
            }
        }

        // Write defect_matrix.csv
        writeMatrixCsv(outputFile, testCases, defects, matrix);
        System.out.println("\nDefect matrix written to: " + outputFile.getPath());

        // Defect detection summary
        int detectedCount = 0;
        for (PackageDefect defect : defects) {
            if (defect == PackageDefect.NONE) continue;
            if (!failingCasesPerDefect.get(defect).isEmpty()) {
                detectedCount++;
            }
        }
        System.out.println("=======================================================");
        System.out.println("SUMMARY: Detected " + detectedCount + " / 10 seeded defects.");
        if (failingCasesPerDefect.get(PackageDefect.NONE).isEmpty()) {
            System.out.println("Defect NONE (clean baseline): 100% PASS on all test cases.");
        } else {
            System.err.println("WARNING: Defect NONE had failures! Baseline is not clean.");
        }
        System.out.println("=======================================================\n");
    }

    private static boolean executeTestCase(ShippingCalculator calculator, TestCase tc) {
        try {
            PackageShippingInput input = new PackageShippingInput(
                    tc.weight, tc.origin, tc.destination, tc.deliveryOption,
                    tc.signatureOption, tc.deliveryLocationType,
                    tc.saturdayDelivery, tc.holdAtLocation, tc.fragile, tc.dangerous);

            ShippingResult result = calculator.calculate(input);

            if ("ERROR".equalsIgnoreCase(tc.expectedOutput)) {
                // Expected an error, but calculation succeeded -> FAIL
                return false;
            }

            BigDecimal expected = new BigDecimal(tc.expectedOutput).setScale(2, RoundingMode.HALF_UP);
            BigDecimal actual = result.getTotalCost().setScale(2, RoundingMode.HALF_UP);
            return actual.compareTo(expected) == 0;
        } catch (IllegalArgumentException e) {
            // Error occurred
            return "ERROR".equalsIgnoreCase(tc.expectedOutput);
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
                    if (line.toLowerCase().startsWith("testid")) continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 12) continue;

                TestCase tc = new TestCase();
                tc.testId = parts[0].trim();
                tc.weight = Double.parseDouble(parts[1].trim());
                tc.origin = Integer.parseInt(parts[2].trim());
                tc.destination = Integer.parseInt(parts[3].trim());
                tc.deliveryOption = Integer.parseInt(parts[4].trim());
                tc.signatureOption = Integer.parseInt(parts[5].trim());
                tc.deliveryLocationType = Integer.parseInt(parts[6].trim());
                tc.saturdayDelivery = Integer.parseInt(parts[7].trim());
                tc.holdAtLocation = Integer.parseInt(parts[8].trim());
                tc.fragile = Integer.parseInt(parts[9].trim());
                tc.dangerous = Integer.parseInt(parts[10].trim());
                tc.expectedOutput = parts[11].trim();

                list.add(tc);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read testcases: " + e.getMessage(), e);
        }
        return list;
    }

    private static void writeMatrixCsv(File outputFile, List<TestCase> testCases, PackageDefect[] defects, String[][] matrix) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Header
            StringBuilder sb = new StringBuilder("testId");
            for (PackageDefect defect : defects) {
                sb.append(",").append(defect.name());
            }
            writer.println(sb.toString());

            // Rows
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
