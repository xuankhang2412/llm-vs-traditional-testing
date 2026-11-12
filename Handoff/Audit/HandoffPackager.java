package audit;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.*;

public class HandoffPackager {

    public static void main(String[] args) throws Exception {
        Path root = Paths.get("D:/SWT301_TEST");
        Path handoffDir = root.resolve("Handoff");

        System.out.println("Cleaning and creating Handoff directory: " + handoffDir);
        deleteRecursively(handoffDir);
        Files.createDirectories(handoffDir);

        // 1. Reference PDF
        Path refDir = handoffDir.resolve("Reference");
        Files.createDirectories(refDir);
        Files.copy(root.resolve("Reference/Benchmarking-Software-Test-Cases-Produced-by-Gener.pdf"),
                refDir.resolve("Benchmarking-Software-Test-Cases-Produced-by-Gener.pdf"), StandardCopyOption.REPLACE_EXISTING);

        // 2. Spec (including llm_prompts)
        copyDirectoryExcluding(root.resolve("Spec"), handoffDir.resolve("Spec"), Collections.emptySet());

        // 3. Project READMEs & Defect Catalogues
        Path readmeDir = handoffDir.resolve("Projects_READMEs");
        Files.createDirectories(readmeDir);
        for (int i = 1; i <= 4; i++) {
            Path src = root.resolve("Project" + i + "/README.md");
            if (Files.exists(src)) {
                Files.copy(src, readmeDir.resolve("Project" + i + "_README.md"), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // 4. All Suites (raw LLM copies from Suites/_raw_llm and normalized ones, TFD, PICT)
        copyDirectoryExcluding(root.resolve("Suites"), handoffDir.resolve("Suites"), Collections.emptySet());

        // 5. LLM_ready prompts
        copyDirectoryExcluding(root.resolve("LLM_ready"), handoffDir.resolve("LLM_ready"), Collections.emptySet());

        // 6. Logs
        copyDirectoryExcluding(root.resolve("Logs"), handoffDir.resolve("Logs"), Collections.emptySet());

        // 7. Audit (excluding .class files and classes folder)
        Set<String> auditExcludes = new HashSet<>(Arrays.asList("classes", "classes\\", ".class"));
        copyDirectoryExcluding(root.resolve("Audit"), handoffDir.resolve("Audit"), auditExcludes);

        // 8. Results (excluding .class files and classes folder)
        Set<String> resultsExcludes = new HashSet<>(Arrays.asList("classes", "classes\\", ".class"));
        copyDirectoryExcluding(root.resolve("Results"), handoffDir.resolve("Results"), resultsExcludes);

        // 9. Write KNOWN_ISSUES.md
        writeKnownIssues(handoffDir.resolve("KNOWN_ISSUES.md"));

        // 10. Write MANIFEST.md
        writeManifest(handoffDir, handoffDir.resolve("MANIFEST.md"));

        System.out.println("Handoff directory prepared! Now creating Handoff.zip...");

        // 11. Zip Handoff.zip
        Path zipPath = root.resolve("Handoff.zip");
        Files.deleteIfExists(zipPath);
        zipDirectory(handoffDir, zipPath);

        long zipSize = Files.size(zipPath);
        System.out.printf("Handoff.zip created successfully! File size: %d bytes (%.2f MB)%n",
                zipSize, (double) zipSize / (1024 * 1024));
    }

    private static void writeKnownIssues(Path out) throws IOException {
        String content = """
# KNOWN ISSUES & BENCHMARK NOTES FOR PAPER AUTHORS

This document records all methodological nuances, known format anomalies, test generation constraints, and suite characteristics observed during the black-box benchmarking of **BasicPackage**, **BasicPayroll**, **EnhancedPackage**, and **EnhancedPayroll**.

---

## 1. Format Normalization of LLM-Generated Suites
* **Context:** Low-detail prompts (especially Condition $C_1$) in Gemini 3.1 Pro generated CSV outputs with minor formatting variations:
  * `BasicPackage/LLM/gemini_C1_r1.csv`: Included a leading `test_id` column and titled the expected column `expected_output`.
  * `BasicPayroll/LLM/gemini_C1_r1.csv`: Included a leading `test_id` column, intermediate `grossPay` calculation columns, and titled the net pay column `expectedNet`.
  * `EnhancedPayroll/LLM/gemini_C1_r1.csv`: Included a leading `test_id` column, intermediate `grossPay` calculation columns, and titled the net pay column `Expected_NetPay`.
* **Remediation:** In accordance with Task B, exact byte-for-byte unnormalized originals were backed up in `Suites/_raw_llm/<App>/<file>`. The working suites in `Suites/<App>/LLM/` were normalized by stripping extraneous non-input columns and standardizing the header to match `Spec/csv_format.md`.
* **Integrity Assertion:** An automated SHA-256 and field-by-field value equality validator verified that **no input or expected value was altered, reordered, added, or deleted**. Detailed before/after hashes are recorded in `Audit/llm_normalization_log.csv`.

---

## 2. Test Suite Row Counts and Quota Discrepancies
* **Benchmark Quotas:** Specified target quotas were **20 rows** for Basic applications and **35 rows** for Enhanced applications.
* **TFD & LLM Suites:** All 4 Human TFD suites and all 24 Gemini LLM suites met their exact quotas (20 rows Basic, 35 rows Enhanced).
* **PICT Combinatorial Suites:**
  * `BasicPackage/PICT_plain`: 20 rows (meets quota).
  * `EnhancedPackage/PICT_plain`: 35 rows (meets quota).
  * `EnhancedPackage/PICT_constrained`: 35 rows (meets quota).
  * `BasicPayroll/PICT_plain`: **16 rows** (below 20-row quota).
  * `EnhancedPayroll/PICT_plain`: **27 rows** (below 35-row quota).
  * `EnhancedPayroll/PICT_constrained`: **32 rows** (below 35-row quota).
* **Reasoning:** Microsoft PICT optimizes for complete 2-way parameter combinatorial coverage. For `BasicPayroll` and `EnhancedPayroll`, all pairwise parameter combinations were mathematically saturated at 16, 27, and 32 test cases respectively. Adding arbitrary filler rows would artificially depress `defects_per_test` efficiency.

---

## 3. Specification Clarifications & Rounding Policies (v1.1)
* **Rounding Tie Policy:** Specifications clarify that half-up rounding applies to the final computed dollar amount at the second decimal place, rounding ties away from zero (e.g., $12.345 \\to 12.35$ and $-161.795 \\to -161.80$). Intermediate precision is preserved across all preceding calculation steps.
* **Negative Net Pay:** In Payroll systems, excessive voluntary deductions (e.g., parking, United Way, benefit plans with low or zero hours worked) yield valid negative net pay. The system does not clamp net pay to zero.

---

## 4. TFD Suite Provenance & Self-Reported Status
* **Authoring Context:** The TFD (Test-First Development) test suites were authored by an external human software tester working strictly from the functional specifications (`Spec/`) without access to seeded defects, mutant source code, or internal implementation classes.
* **Caveat for Publication:** The tester states that no AI tool was used; this statement and the effort times are self-reported and could not be independently verified.

---

## 5. Specification Example Ingestion in LLM Suites
* **Example Reuse:** Some few-shot Gemini LLM suites (notably $C_2$, $C_3$, $C_5$, $C_6$) ingested the worked specification examples provided in the prompt templates as part of their generated test cases (e.g. `BasicPackage` worked example $w=1.05, orig=1, dest=2, deliv=3$).
* **Verification:** Detailed inventory in `Audit/inventory_and_validation.md` catalogs each instance of example reuse.

---

## 6. Identical LLM Suites Across Conditions
* **Non-Independence:** Several prompt conditions produced identical suites (as sets and byte-identical outputs), meaning the 24 runs are not 24 independent samples:
  * `BasicPackage`: $C_2, C_3, C_4, C_5, C_6$ are 100% identical (2 distinct suites total).
  * `BasicPayroll`: All 6 conditions distinct (6 distinct suites total).
  * `EnhancedPackage`: $C_1, C_2, C_4$ are 100% identical (4 distinct suites total).
  * `EnhancedPayroll`: $C_4, C_5$ are 100% identical (5 distinct suites total).
""";
        Files.writeString(out, content);
    }

    private static void writeManifest(Path handoffDir, Path out) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# HANDOFF PACKAGE MANIFEST\n\n");
        sb.append("**Benchmark Package:** SWT301 Black-Box Test Generation Benchmark\n");
        sb.append("**Timestamp:** 2026-11-05\n\n");
        sb.append("This manifest details the directory structure, file inventory, and descriptions for all assets included in the research handoff package.\n\n");
        sb.append("## Directory Overview\n\n");
        sb.append("| Directory / Path | Description |\n");
        sb.append("| :--- | :--- |\n");
        sb.append("| `Reference/` | Authoritative literature reference PDF (`Benchmarking-Software-Test-Cases-Produced-by-Gener.pdf`) |\n");
        sb.append("| `Spec/` | Clean specifications (v1.1.1), `csv_format.md`, and prompt templates in `llm_prompts/` |\n");
        sb.append("| `Projects_READMEs/` | Functional specifications and seeded defect catalogues for Projects 1 through 4 |\n");
        sb.append("| `Suites/` | All 34 normalized test suites across 4 apps (TFD, PICT_plain, PICT_constrained, LLM C1-C6) |\n");
        sb.append("| `Suites/_raw_llm/` | Byte-for-byte unedited original copies of LLM suites that underwent format normalization |\n");
        sb.append("| `LLM_ready/` | 24 pre-assembled ready-to-paste prompt text files for all apps and conditions |\n");
        sb.append("| `Logs/` | `generation_log.csv` (LLM measured minutes) and `TFD_tester_notes.txt` (TFD self-reported notes) |\n");
        sb.append("| `Audit/` | Comprehensive inventory, validation logs, normalization logs, and audit reports |\n");
        sb.append("| `Results/` | Matrix long CSVs for all 34 suites, 8 summary CSV metrics, 5 PNG charts, scripts, and `summary.md` |\n");
        sb.append("| `KNOWN_ISSUES.md` | Authoritative notes on normalizations, quotas, rounding rules, and TFD provenance |\n");
        sb.append("| `MANIFEST.md` | Complete file inventory and metadata catalog (this file) |\n\n");

        sb.append("## Detailed File Inventory\n\n");
        sb.append("```\n");

        List<Path> allFiles = new ArrayList<>();
        Files.walkFileTree(handoffDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!file.getFileName().toString().equals("MANIFEST.md")) {
                    allFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        allFiles.sort(Comparator.comparing(Path::toString));
        for (Path f : allFiles) {
            String rel = handoffDir.relativize(f).toString().replace('\\', '/');
            long size = Files.size(f);
            sb.append(String.format("%-65s [%8d bytes]%n", rel, size));
        }
        sb.append("```\n");

        Files.writeString(out, sb.toString());
    }

    private static void copyDirectoryExcluding(Path src, Path dest, Set<String> excludes) throws IOException {
        if (!Files.exists(src)) return;
        Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String name = dir.getFileName().toString();
                if (excludes.contains(name) || name.equals("target") || name.equals(".git") || name.equals("classes")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                Path rel = src.relativize(dir);
                Path targetDir = dest.resolve(rel);
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String name = file.getFileName().toString();
                if (name.endsWith(".class") || excludes.contains(name)) {
                    return FileVisitResult.CONTINUE;
                }
                Path rel = src.relativize(file);
                Path targetFile = dest.resolve(rel);
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                file.toFile().setWritable(true);
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                dir.toFile().setWritable(true);
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(zipFile)))) {
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path rel = sourceDir.relativize(file);
                    ZipEntry entry = new ZipEntry(rel.toString().replace('\\', '/'));
                    zos.putNextEntry(entry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}

