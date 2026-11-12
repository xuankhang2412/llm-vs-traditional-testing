package audit;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TaskCExecutor {

    record RunJob(String app, String mainClass, String suiteName, Path suiteFile, Path outDir) {}

    public static void main(String[] args) throws Exception {
        Path root = Paths.get("D:/SWT301_TEST");
        Path suitesDir = root.resolve("Suites");
        Path resultsDir = root.resolve("Results");

        List<RunJob> jobs = new ArrayList<>();

        // BasicPackage
        String bpRunner = "com.shipping.SuiteRunner";
        jobs.add(new RunJob("BasicPackage", bpRunner, "TFD", suitesDir.resolve("BasicPackage/TFD/suite.csv"), resultsDir.resolve("BasicPackage/TFD")));
        jobs.add(new RunJob("BasicPackage", bpRunner, "PICT_plain", suitesDir.resolve("BasicPackage/PICT_plain/suite.csv"), resultsDir.resolve("BasicPackage/PICT_plain")));
        for (int c = 1; c <= 6; c++) {
            jobs.add(new RunJob("BasicPackage", bpRunner, "LLM_C" + c, suitesDir.resolve("BasicPackage/LLM/gemini_C" + c + "_r1.csv"), resultsDir.resolve("BasicPackage/LLM_C" + c)));
        }

        // BasicPayroll
        String bpayRunner = "com.payroll.SuiteRunner";
        jobs.add(new RunJob("BasicPayroll", bpayRunner, "TFD", suitesDir.resolve("BasicPayroll/TFD/suite.csv"), resultsDir.resolve("BasicPayroll/TFD")));
        jobs.add(new RunJob("BasicPayroll", bpayRunner, "PICT_plain", suitesDir.resolve("BasicPayroll/PICT_plain/suite.csv"), resultsDir.resolve("BasicPayroll/PICT_plain")));
        for (int c = 1; c <= 6; c++) {
            jobs.add(new RunJob("BasicPayroll", bpayRunner, "LLM_C" + c, suitesDir.resolve("BasicPayroll/LLM/gemini_C" + c + "_r1.csv"), resultsDir.resolve("BasicPayroll/LLM_C" + c)));
        }

        // EnhancedPackage
        String epkgRunner = "com.shipping.enhanced.SuiteRunner";
        jobs.add(new RunJob("EnhancedPackage", epkgRunner, "TFD", suitesDir.resolve("EnhancedPackage/TFD/suite.csv"), resultsDir.resolve("EnhancedPackage/TFD")));
        jobs.add(new RunJob("EnhancedPackage", epkgRunner, "PICT_plain", suitesDir.resolve("EnhancedPackage/PICT_plain/suite.csv"), resultsDir.resolve("EnhancedPackage/PICT_plain")));
        jobs.add(new RunJob("EnhancedPackage", epkgRunner, "PICT_constrained", suitesDir.resolve("EnhancedPackage/PICT_constrained/suite.csv"), resultsDir.resolve("EnhancedPackage/PICT_constrained")));
        for (int c = 1; c <= 6; c++) {
            jobs.add(new RunJob("EnhancedPackage", epkgRunner, "LLM_C" + c, suitesDir.resolve("EnhancedPackage/LLM/gemini_C" + c + "_r1.csv"), resultsDir.resolve("EnhancedPackage/LLM_C" + c)));
        }

        // EnhancedPayroll
        String epayRunner = "com.payroll.enhanced.SuiteRunner";
        jobs.add(new RunJob("EnhancedPayroll", epayRunner, "TFD", suitesDir.resolve("EnhancedPayroll/TFD/suite.csv"), resultsDir.resolve("EnhancedPayroll/TFD")));
        jobs.add(new RunJob("EnhancedPayroll", epayRunner, "PICT_plain", suitesDir.resolve("EnhancedPayroll/PICT_plain/suite.csv"), resultsDir.resolve("EnhancedPayroll/PICT_plain")));
        jobs.add(new RunJob("EnhancedPayroll", epayRunner, "PICT_constrained", suitesDir.resolve("EnhancedPayroll/PICT_constrained/suite.csv"), resultsDir.resolve("EnhancedPayroll/PICT_constrained")));
        for (int c = 1; c <= 6; c++) {
            jobs.add(new RunJob("EnhancedPayroll", epayRunner, "LLM_C" + c, suitesDir.resolve("EnhancedPayroll/LLM/gemini_C" + c + "_r1.csv"), resultsDir.resolve("EnhancedPayroll/LLM_C" + c)));
        }

        System.out.println("Starting execution of " + jobs.size() + " test suites...");
        int successCount = 0;
        int failureCount = 0;
        List<String> failedSuites = new ArrayList<>();

        for (RunJob job : jobs) {
            Files.createDirectories(job.outDir());
            try {
                if (job.mainClass().equals("com.shipping.SuiteRunner")) {
                    com.shipping.SuiteRunner.main(new String[]{job.suiteFile().toString(), job.outDir().toString()});
                } else if (job.mainClass().equals("com.payroll.SuiteRunner")) {
                    com.payroll.SuiteRunner.main(new String[]{job.suiteFile().toString(), job.outDir().toString()});
                } else if (job.mainClass().equals("com.shipping.enhanced.SuiteRunner")) {
                    com.shipping.enhanced.SuiteRunner.main(new String[]{job.suiteFile().toString(), job.outDir().toString()});
                } else if (job.mainClass().equals("com.payroll.enhanced.SuiteRunner")) {
                    com.payroll.enhanced.SuiteRunner.main(new String[]{job.suiteFile().toString(), job.outDir().toString()});
                }

                // Verify output matrix_long.csv exists and has rows
                Path matrix = job.outDir().resolve("matrix_long.csv");
                if (Files.exists(matrix) && Files.size(matrix) > 0) {
                    successCount++;
                    System.out.printf("  [SUCCESS] %s / %s -> %s (%d bytes)%n",
                            job.app(), job.suiteName(), matrix, Files.size(matrix));
                } else {
                    failureCount++;
                    failedSuites.add(job.app() + "/" + job.suiteName() + " (matrix_long.csv empty or missing)");
                }
            } catch (Throwable t) {
                failureCount++;
                failedSuites.add(job.app() + "/" + job.suiteName() + ": " + t.getMessage());
                System.err.printf("  [FAILED] %s / %s: %s%n", job.app(), job.suiteName(), t.getMessage());
            }
        }

        System.out.println("\nExecution complete!");
        System.out.println("Successful runs: " + successCount + " / " + jobs.size());
        if (failureCount > 0) {
            System.out.println("Failed runs (" + failureCount + "): " + failedSuites);
        } else {
            System.out.println("All 34 suites ran with 0 errors!");
        }
    }
}

