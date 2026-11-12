package audit;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class CreateBackupZip {
    public static void main(String[] args) throws Exception {
        Path root = Paths.get("D:/SWT301_TEST");
        Path zipFile = Paths.get("D:/SWT301_TEST/Backup/Stage2c_spec_v1.1.1.zip");

        if (Files.exists(zipFile)) {
            Files.delete(zipFile);
        }

        int count = 0;
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(zipFile)))) {
            zos.setLevel(Deflater.BEST_COMPRESSION);

            try (var stream = Files.walk(root)) {
                for (Path p : (Iterable<Path>) stream::iterator) {
                    if (Files.isRegularFile(p)) {
                        String rel = root.relativize(p).toString().replace('\\', '/');

                        // Exclude target/, Backup/, Audit/tmp/
                        if (rel.startsWith("Backup/") || rel.contains("/target/") || rel.startsWith("Audit/tmp/")) {
                            continue;
                        }

                        ZipEntry entry = new ZipEntry(rel);
                        entry.setTime(Files.getLastModifiedTime(p).toMillis());
                        zos.putNextEntry(entry);
                        Files.copy(p, zos);
                        zos.closeEntry();
                        count++;
                    }
                }
            }
        }

        System.out.println("Successfully created: " + zipFile);
        System.out.println("Total files zipped: " + count);
        System.out.println("Zip file size: " + Files.size(zipFile) + " bytes");
    }
}

