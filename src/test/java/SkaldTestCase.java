import io.github.bbrown683.skald.antlr4.*;
import io.github.bbrown683.skald.symbol.external.ExternalSymbolTable;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.BCELifier;
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

        var symbolTable = symbolVisitor.getLocalSymbolTable();
        var referenceTable = symbolVisitor.getExternalSymbolTable();

        //var semanticVisitor = new SemanticVisitor(className, symbolTable);
        //semanticVisitor.visitClassFile(classFileContext);

        var compilerVisitor = new CompilerVisitor(className, symbolTable, referenceTable);
        compilerVisitor.visit(classFileContext);
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
        ExternalSymbolTable externalSymbolTable = new ExternalSymbolTable();
        externalSymbolTable.addImport("java.io.*");
        externalSymbolTable.addImport("java.lang.reflect.Modifier");
        externalSymbolTable.addImport("java.util.List");
        externalSymbolTable.addImport("java.util.Scanner");
    }
}
