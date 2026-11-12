package com.shipping.enhanced;

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
        int packaging;
        int signature;
        int locationType;
        int saturday;
        int hold;
        int fragile;
        int dangerous;
        String expectedOutput; // numeric (e.g. 131.40) or ERROR
    }

    public static void main(String[] args) {
        String inputCsvPath = (args.length > 0) ? args[0] : "testcases/sample_shipping_tests.csv";
        String outputCsvPath = (args.length > 1) ? args[1] : "results/defect_matrix.csv";

        File inputFile = new File(inputCsvPath);
        if (!inputFile.exists()) {
            inputFile = new File("D:/SWT301_TEST/Project3/" + inputCsvPath);
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

    private static boolean executeTestCase(ShippingCalculator calculator, TestCase tc) {
        try {
            ShippingInput input = new ShippingInput(
                    tc.weight, tc.origin, tc.destination, tc.deliveryOption,
                    tc.packaging, tc.signature, tc.locationType,
                    tc.saturday, tc.hold, tc.fragile, tc.dangerous);

            ShippingResult result = calculator.calculate(input);

            if ("ERROR".equalsIgnoreCase(tc.expectedOutput)) {
                return false;
            }

            BigDecimal expected = new BigDecimal(tc.expectedOutput).setScale(2, RoundingMode.HALF_UP);
            BigDecimal actual = result.totalCost().setScale(2, RoundingMode.HALF_UP);
            return actual.compareTo(expected) == 0;
        } catch (IllegalArgumentException e) {
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
                    if (line.toLowerCase().startsWith("testid") || line.toLowerCase().startsWith("weight")) continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 12) continue;

                TestCase tc = new TestCase();
                int idx = 0;
                if (parts.length >= 13) {
                    tc.testId = parts[idx++].trim();
                } else {
                    tc.testId = "TC" + (list.size() + 1);
                }
                tc.weight = Double.parseDouble(parts[idx++].trim());
                tc.origin = Integer.parseInt(parts[idx++].trim());
                tc.destination = Integer.parseInt(parts[idx++].trim());
                tc.deliveryOption = Integer.parseInt(parts[idx++].trim());
                tc.packaging = Integer.parseInt(parts[idx++].trim());
                tc.signature = Integer.parseInt(parts[idx++].trim());
                tc.locationType = Integer.parseInt(parts[idx++].trim());
                tc.saturday = Integer.parseInt(parts[idx++].trim());
                tc.hold = Integer.parseInt(parts[idx++].trim());
                tc.fragile = Integer.parseInt(parts[idx++].trim());
                tc.dangerous = Integer.parseInt(parts[idx++].trim());
                tc.expectedOutput = parts[idx].trim();

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
