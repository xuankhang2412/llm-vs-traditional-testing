package audit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

public class TaskBNormalizer {

    public static void main(String[] args) throws Exception {
        Path root = Paths.get("D:/SWT301_TEST");
        Path suitesDir = root.resolve("Suites");
        Path rawLlmDir = suitesDir.resolve("_raw_llm");
        Path auditDir = root.resolve("Audit");

        String headerBasicPkg = "weight,origin,destination,deliveryOption,signatureOption,deliveryLocationType,saturdayDelivery,holdAtLocation,fragile,dangerous,expected";
        String headerBasicPay = "hourlySalary,regularHours,overtimeHours,holidayHours,medicalPlan,dentalPlan,visionPlan,unitedWay,pension,longTermDisability,shortTermDisability,parking,expected";
        String headerEnhPay = "hourly,regularHours,overtimeHours,holidayHours,state,medical,dental,vision,unitedWay,pension,ltd,std,parking,expected";

        List<String[]> logRows = new ArrayList<>();
        logRows.add(new String[]{"file", "change_type", "detail", "sha256_before", "sha256_after", "values_identical"});

        // 1. BasicPackage gemini_C1_r1.csv
        normalizeFile(
                suitesDir.resolve("BasicPackage/LLM/gemini_C1_r1.csv"),
                rawLlmDir.resolve("BasicPackage/gemini_C1_r1.csv"),
                headerBasicPkg,
                0, // test_id at col 0 to remove
                new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, // input cols
                11, // expected_output col
                "header_and_col_removal",
                "Removed leading test_id column, renamed expected_output to expected, standardized header",
                logRows
        );

        // 2. BasicPayroll gemini_C1_r1.csv
        normalizeFile(
                suitesDir.resolve("BasicPayroll/LLM/gemini_C1_r1.csv"),
                rawLlmDir.resolve("BasicPayroll/gemini_C1_r1.csv"),
                headerBasicPay,
                0, // testCaseId at col 0 to remove
                new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, // input cols
                14, // expectedNet col (skip expectedGross at 13)
                "header_and_col_removal",
                "Removed leading testCaseId column, removed auxiliary expectedGross column, renamed expectedNet to expected, standardized header",
                logRows
        );

        // 3. EnhancedPayroll gemini_C1_r1.csv
        normalizeFile(
                suitesDir.resolve("EnhancedPayroll/LLM/gemini_C1_r1.csv"),
                rawLlmDir.resolve("EnhancedPayroll/gemini_C1_r1.csv"),
                headerEnhPay,
                0, // Test_ID at col 0 to remove
                new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13}, // input cols
                15, // Expected_NetPay col (skip Expected_GrossPay at 14)
                "header_and_col_removal",
                "Removed leading Test_ID column, removed auxiliary Expected_GrossPay column, renamed Expected_NetPay to expected, standardized header",
                logRows
        );

        // Write Audit/llm_normalization_log.csv
        StringBuilder sb = new StringBuilder();
        for (String[] r : logRows) {
            sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    r[0], r[1], r[2], r[3], r[4], r[5]));
        }
        Files.writeString(auditDir.resolve("llm_normalization_log.csv"), sb.toString(), StandardCharsets.UTF_8);

        System.out.println("Normalization completed successfully!");
        System.out.println("Wrote Audit/llm_normalization_log.csv");
    }

    private static void normalizeFile(
            Path targetFile, Path rawBackupFile, String targetHeader,
            int colToRemove, int[] inputCols, int expectedColIdx,
            String changeType, String detail, List<String[]> logRows) throws Exception {

        if (!Files.exists(targetFile)) {
            throw new FileNotFoundException("Target file not found: " + targetFile);
        }

        byte[] originalBytes = Files.readAllBytes(targetFile);
        String shaBefore = sha256(originalBytes);

        // 1. Copy original byte for byte to raw backup (never overwrite existing)
        if (!Files.exists(rawBackupFile)) {
            Files.createDirectories(rawBackupFile.getParent());
            Files.write(rawBackupFile, originalBytes, StandardOpenOption.CREATE_NEW);
            System.out.println("Saved raw backup: " + rawBackupFile);
        } else {
            System.out.println("Raw backup already exists (not overwriting): " + rawBackupFile);
        }

        // 2. Read and parse original data
        List<String> lines = Files.readAllLines(targetFile, StandardCharsets.UTF_8);
        List<String> dataRows = new ArrayList<>();
        for (String l : lines) {
            String t = l.trim();
            if (!t.isEmpty() && !t.startsWith("#") && !t.startsWith("```")) {
                dataRows.add(t);
            }
        }

        List<List<String>> originalParsedInputs = new ArrayList<>();
        List<String> originalExpected = new ArrayList<>();

        for (int r = 1; r < dataRows.size(); r++) {
            String[] cols = dataRows.get(r).split(",", -1);
            List<String> inRow = new ArrayList<>();
            for (int idx : inputCols) {
                inRow.add(cols[idx].trim());
            }
            originalParsedInputs.add(inRow);
            originalExpected.add(cols[expectedColIdx].trim());
        }

        // 3. Build normalized file
        StringBuilder normSb = new StringBuilder();
        normSb.append(targetHeader).append("\n");

        for (int r = 0; r < originalParsedInputs.size(); r++) {
            List<String> inRow = originalParsedInputs.get(r);
            for (int i = 0; i < inRow.size(); i++) {
                if (i > 0) normSb.append(",");
                normSb.append(inRow.get(i));
            }
            normSb.append(",").append(originalExpected.get(r)).append("\n");
        }

        byte[] normBytes = normSb.toString().getBytes(StandardCharsets.UTF_8);
        Files.write(targetFile, normBytes, StandardOpenOption.TRUNCATE_EXISTING);
        String shaAfter = sha256(normBytes);

        // 4. Verification assertion: read back normalized file and assert exact value equality
        List<String> verifyLines = Files.readAllLines(targetFile, StandardCharsets.UTF_8);
        if (!verifyLines.get(0).trim().equals(targetHeader)) {
            throw new AssertionError("Header mismatch after normalization in " + targetFile);
        }
        if (verifyLines.size() - 1 != originalParsedInputs.size()) {
            throw new AssertionError("Row count mismatch after normalization in " + targetFile);
        }

        boolean allIdentical = true;
        for (int r = 1; r < verifyLines.size(); r++) {
            String[] vCols = verifyLines.get(r).trim().split(",", -1);
            List<String> origIn = originalParsedInputs.get(r - 1);
            for (int i = 0; i < origIn.size(); i++) {
                if (!vCols[i].trim().equals(origIn.get(i))) {
                    allIdentical = false;
                    throw new AssertionError(String.format("Value mismatch in %s at row %d col %d: '%s' vs '%s'",
                            targetFile, r, i, vCols[i], origIn.get(i)));
                }
            }
            if (!vCols[vCols.length - 1].trim().equals(originalExpected.get(r - 1))) {
                allIdentical = false;
                throw new AssertionError(String.format("Expected mismatch in %s at row %d: '%s' vs '%s'",
                        targetFile, r, vCols[vCols.length - 1], originalExpected.get(r - 1)));
            }
        }

        String relPath = targetFile.toString().replace("D:\\SWT301_TEST\\", "");
        logRows.add(new String[]{
                relPath,
                changeType,
                detail,
                shaBefore,
                shaAfter,
                allIdentical ? "yes" : "no"
        });
        System.out.println("Normalized and verified " + relPath + " (Values identical: " + (allIdentical ? "YES" : "NO") + ")");
    }

    private static String sha256(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}

