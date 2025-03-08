import io.github.bbrown683.jasper.antlr.JasperVisitor;
import io.github.bbrown683.jasper.antlr4.LangLexer;
import io.github.bbrown683.jasper.antlr4.LangParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

public class LangTestCases extends ClassLoader {

    private InputStream loadFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        var inputStream = classLoader.getResourceAsStream(fileName);
        if(inputStream == null) {
            throw new RuntimeException("Could not find file");
        }
        return inputStream;
    }

    private byte[] getBytecode(String className, InputStream inputStream) throws IOException {
        CharStream charStream = CharStreams.fromStream(inputStream);
        var lexer = new LangLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new LangParser(tokenStream);

        JasperVisitor visitor = new JasperVisitor(className);
        return visitor.visitClassFile(parser.classFile());
    }

    @Test
    public void test() {
        String filename = "Test.lang";
        try(InputStream inputStream = loadFile(filename)) {
            String className = filename.split("\\.")[0];
            byte[] bytecode = getBytecode(className, inputStream);
        } catch(Exception e) {
            System.err.println("Failed to parse file due to error: " + e.getMessage());
        }
    }
}
