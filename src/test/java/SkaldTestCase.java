import io.github.bbrown683.skald.antlr4.*;
import io.github.bbrown683.skald.reference.ReferenceTable;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

public class SkaldTestCase {
    private InputStream loadFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        var inputStream = classLoader.getResourceAsStream(fileName);
        if(inputStream == null) {
            throw new RuntimeException("Could not find file");
        }
        return inputStream;
    }

    private void visit(String className, InputStream inputStream) throws IOException {
        CharStream charStream = CharStreams.fromStream(inputStream);
        var lexer = new SkaldLexer(charStream);
        var parser = new SkaldParser(new CommonTokenStream(lexer));
        var classFileContext = parser.classFile();

        var symbolVisitor = new SymbolVisitor(className);
        symbolVisitor.visit(classFileContext);

        var symbolTable = symbolVisitor.getSymbolTable();

        var semanticVisitor = new SemanticVisitor(className, symbolTable);
        semanticVisitor.visitClassFile(classFileContext);

        //var visitor = new CompilerVisitor(className);
        return;
    }

    @Test
    public void test() {
        String filename = "Test.lang";
        try(InputStream inputStream = loadFile(filename)) {
            String className = filename.split("\\.")[0];
            visit(className, inputStream);

        } catch(Exception e) {
            System.err.println("Failed to parse file due to error: " + e.getMessage());
        }
    }

    @Test
    public void test2() {
        ReferenceTable referenceTable = new ReferenceTable();
        referenceTable.addImport("java.io.*");
        referenceTable.addImport("java.lang.reflect.Modifier");
        referenceTable.addImport("java.util.List");
        referenceTable.addImport("java.util.Scanner");

        var reference = referenceTable.getReference("Integer");
    }
}
