package audit;

import com.shipping.enhanced.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CheckEnhancedPackage {
    public static void main(String[] args) throws Exception {
        Path csvPath = Paths.get("D:/SWT301_TEST/Suites/EnhancedPackage/TFD/suite.csv");
        List<String> lines = Files.readAllLines(csvPath);
        ShippingCalculator calc = new ShippingCalculator(Defect.NONE);
        int invalidCount = 0;
        int mismatchCount = 0;

        System.out.println("Checking EnhancedPackage TFD suite:");
        for (int i = 1; i < lines.size(); i++) {
            String[] p = lines.get(i).split(",", -1);
            double w = Double.parseDouble(p[0].trim());
            int orig = Integer.parseInt(p[1].trim());
            int dest = Integer.parseInt(p[2].trim());
            int deliv = Integer.parseInt(p[3].trim());
            int pkg = Integer.parseInt(p[4].trim());
            int sig = Integer.parseInt(p[5].trim());
            int loc = Integer.parseInt(p[6].trim());
            int sat = Integer.parseInt(p[7].trim());
            int hold = Integer.parseInt(p[8].trim());
            int frag = Integer.parseInt(p[9].trim());
            int dang = Integer.parseInt(p[10].trim());
            String exp = p[11].trim();

            String actual;
            boolean threw = false;
            try {
                ShippingInput in = new ShippingInput(w, orig, dest, deliv, pkg, sig, loc, sat, hold, frag, dang);
                actual = calc.calculate(in).totalCost().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
            } catch (IllegalArgumentException e) {
                threw = true;
                actual = "ERROR";
                invalidCount++;
            }

            boolean expIsError = "ERROR".equalsIgnoreCase(exp);
            if (threw != expIsError) {
                mismatchCount++;
                System.out.printf("Row %02d (TC%02d): threw=%b, exp=%s, line=%s%n", i, i, threw, exp, lines.get(i));
            }
        }
        System.out.println("Total rows: " + (lines.size() - 1));
        System.out.println("Total invalid (NONE threw): " + invalidCount);
        System.out.println("Total mismatches between threw and expected==ERROR: " + mismatchCount);
    }
}

