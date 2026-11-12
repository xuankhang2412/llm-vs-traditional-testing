package audit;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CheckAllAppsConsistency {
    public static void main(String[] args) throws Exception {
        Path root = Paths.get("D:/SWT301_TEST");
        Path secCsv = root.resolve("Results/secondary_metrics.csv");
        List<String> secLines = Files.readAllLines(secCsv);

        Map<String, Integer> csvInvalid = new LinkedHashMap<>();
        Map<String, Integer> csvTotal = new LinkedHashMap<>();
        Map<String, Integer> csvExpectedMatches = new LinkedHashMap<>();

        for (int i = 1; i < secLines.size(); i++) {
            String[] p = secLines.get(i).split(",", -1);
            String app = p[0];
            String suite = p[1];
            int tot = Integer.parseInt(p[4]);
            int inv = Integer.parseInt(p[5]);
            csvTotal.put(app + "/" + suite, tot);
            csvInvalid.put(app + "/" + suite, inv);
        }

        System.out.println("==========================================================================================");
        System.out.println("CONSISTENCY CHECK ACROSS ALL 4 APPS (secondary_metrics.csv vs Live Execution)");
        System.out.println("==========================================================================================");

        String[] apps = {"BasicPackage", "BasicPayroll", "EnhancedPackage", "EnhancedPayroll"};
        int totalSuitesChecked = 0;
        int discrepancies = 0;

        for (String app : apps) {
            Path appSuites = root.resolve("Suites").resolve(app);
            File[] files = appSuites.toFile().listFiles();
            List<File> suiteDirs = new ArrayList<>();
            // collect TFD, PICT_plain, PICT_constrained, and LLM files
            for (File f : files) {
                if (f.getName().equals("TFD") || f.getName().startsWith("PICT")) suiteDirs.add(f);
                if (f.getName().equals("LLM")) {
                    File[] llmFiles = f.listFiles((d, n) -> n.endsWith(".csv"));
                    if (llmFiles != null) {
                        Arrays.sort(llmFiles, Comparator.comparing(File::getName));
                        for (File lf : llmFiles) suiteDirs.add(lf);
                    }
                }
            }

            for (File sf : suiteDirs) {
                totalSuitesChecked++;
                String suiteName;
                Path csvPath;
                if (sf.isDirectory()) {
                    suiteName = sf.getName();
                    csvPath = sf.toPath().resolve("suite.csv");
                } else {
                    String fn = sf.getName().replace(".csv", "");
                    // e.g. gemini_C1_r1 -> LLM_C1
                    String cond = fn.substring(fn.indexOf("_C") + 1, fn.indexOf("_r"));
                    suiteName = "LLM_" + cond;
                    csvPath = sf.toPath();
                }

                String key = app + "/" + suiteName;
                List<String> lines = Files.readAllLines(csvPath);
                int tests = lines.size() - 1;
                int liveInvalid = 0;
                int liveExpectedErrors = 0;
                int liveExpectedMatches = 0;
                int testsWithExp = 0;

                for (int i = 1; i < lines.size(); i++) {
                    String[] p = lines.get(i).split(",", -1);
                    for (int j = 0; j < p.length; j++) p[j] = p[j].trim();
                    String exp = p[p.length - 1];

                    String noneOut = runNone(app, p);
                    boolean isInv = "ERROR".equals(noneOut);
                    if (isInv) liveInvalid++;

                    if (!exp.isEmpty()) {
                        testsWithExp++;
                        if ("ERROR".equalsIgnoreCase(exp)) liveExpectedErrors++;
                        if (matches(noneOut, exp)) liveExpectedMatches++;
                    }
                }

                int recordedInv = csvInvalid.getOrDefault(key, -1);
                boolean match = (recordedInv == liveInvalid);
                if (!match) discrepancies++;

                System.out.printf("App: %-16s | Suite: %-16s | Tests: %2d | Live Inv: %2d | CSV Inv: %2d | Match: %-5b | ExpErrors: %2d | ExpMatches: %2d/%2d%n",
                        app, suiteName, tests, liveInvalid, recordedInv, match, liveExpectedErrors, liveExpectedMatches, testsWithExp);
            }
        }
        System.out.println("==========================================================================================");
        System.out.printf("TOTAL SUITES CHECKED: %d | TOTAL DISCREPANCIES: %d%n", totalSuitesChecked, discrepancies);
        System.out.println("==========================================================================================");
    }

    private static String runNone(String app, String[] values) {
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
                return res.getTotalCost().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
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
                return res.getNetSalary().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
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
                return res.totalCost().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
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
                return res.netPay().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
            }
        } catch (Throwable t) {
            return "ERROR";
        }
        return "ERROR";
    }

    private static boolean matches(String actual, String expected) {
        if ("ERROR".equalsIgnoreCase(expected)) return "ERROR".equals(actual);
        if ("ERROR".equals(actual)) return false;
        try {
            java.math.BigDecimal a = new java.math.BigDecimal(actual).setScale(2, java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal e = new java.math.BigDecimal(expected).setScale(2, java.math.RoundingMode.HALF_UP);
            return a.compareTo(e) == 0;
        } catch (Exception ex) {
            return actual.trim().equalsIgnoreCase(expected.trim());
        }
    }
}

