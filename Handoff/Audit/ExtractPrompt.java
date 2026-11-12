import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ExtractPrompt {
    public static void main(String[] args) throws Exception {
        Pattern p = Pattern.compile("\\{[^}]+\\}");
        try (var s = Files.list(Paths.get("D:/SWT301_TEST/Spec/llm_prompts"))) {
            for (Path path : (Iterable<Path>) s::iterator) {
                if (Files.isRegularFile(path) && path.toString().endsWith(".txt")) {
                    String content = Files.readString(path);
                    Matcher m = p.matcher(content);
                    System.out.print(path.getFileName() + ": ");
                    while (m.find()) {
                        System.out.print(m.group() + " ");
                    }
                    System.out.println();
                }
            }
        }
    }
}
