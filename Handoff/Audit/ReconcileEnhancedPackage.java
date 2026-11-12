package audit;

import com.shipping.enhanced.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ReconcileEnhancedPackage {

    public static void main(String[] args) throws Exception {
        Path epSuites = Paths.get("D:/SWT301_TEST/Suites/EnhancedPackage");
        ShippingCalculator calc = new ShippingCalculator(Defect.NONE);

        String[] suites = {
                "TFD",
                "PICT_plain",
                "PICT_constrained",
                "LLM/gemini_C1_r1",
                "LLM/gemini_C2_r1",
                "LLM/gemini_C3_r1",
                "LLM/gemini_C4_r1",
                "LLM/gemini_C5_r1",
                "LLM/gemini_C6_r1"
        };

        System.out.println("===============================================================================");
        System.out.println("RECONCILIATION FOR ENHANCED PACKAGE TEST SUITES");
        System.out.println("===============================================================================");

        for (String sName : suites) {
            Path csvFile = epSuites.resolve(sName + (sName.endsWith(".csv") ? "" : (sName.startsWith("LLM") ? ".csv" : "/suite.csv")));
            List<String> lines = Files.readAllLines(csvFile);

            int totalTests = lines.size() - 1;
            int noneThrewCount = 0;
            int expErrorCount = 0;
            int expNumericCount = 0;
            int expEmptyCount = 0;
            int mismatchCount = 0;

            List<String> details = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",", -1);
                for (int j = 0; j < p.length; j++) p[j] = p[j].trim();

                double w = Double.parseDouble(p[0]);
                int orig = Integer.parseInt(p[1]);
                int dest = Integer.parseInt(p[2]);
                int deliv = Integer.parseInt(p[3]);
                int pkg = Integer.parseInt(p[4]);
                int sig = Integer.parseInt(p[5]);
                int loc = Integer.parseInt(p[6]);
                int sat = Integer.parseInt(p[7]);
                int hold = Integer.parseInt(p[8]);
                int frag = Integer.parseInt(p[9]);
                int dang = Integer.parseInt(p[10]);
                String exp = p[11];

                String testId = String.format("TC%02d", i);
                boolean threw = false;
                String errorMsg = "";
                try {
                    ShippingInput in = new ShippingInput(w, orig, dest, deliv, pkg, sig, loc, sat, hold, frag, dang);
                    calc.calculate(in);
                } catch (IllegalArgumentException ex) {
                    threw = true;
                    errorMsg = ex.getMessage();
                    noneThrewCount++;
                }

                if (exp.equalsIgnoreCase("ERROR")) expErrorCount++;
                else if (exp.isEmpty()) expEmptyCount++;
                else expNumericCount++;

                if (!exp.isEmpty()) {
                    boolean expError = exp.equalsIgnoreCase("ERROR");
                    if (threw != expError) {
                        mismatchCount++;
                        details.add(String.format("    MISMATCH %s: exp='%s', noneThrew=%b, msg='%s', row='%s'",
                                testId, exp, threw, errorMsg, line));
                    }
                }
            }

            System.out.printf("Suite: %-18s | Total: %2d | NONE Threw (Invalid): %2d | Valid: %2d | exp=ERROR: %2d | exp=Num: %2d | exp=Empty: %2d | Mismatches: %d%n",
                    sName, totalTests, noneThrewCount, (totalTests - noneThrewCount),
                    expErrorCount, expNumericCount, expEmptyCount, mismatchCount);

            for (String d : details) {
                System.out.println(d);
            }
        }

        System.out.println("===============================================================================");
    }
}

