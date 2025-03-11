import io.github.bbrown683.jasper.antlr4.*;
import io.github.bbrown683.jasper.symbol.v1.GlobalSymbolTable;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
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

    private void visit(String className, InputStream inputStream) throws IOException {
        CharStream charStream = CharStreams.fromStream(inputStream);
        var lexer = new JasperLexer(charStream);
        var parser = new JasperParser(new CommonTokenStream(lexer));
        var classFileContext = parser.classFile();

        var symbolListener = new SymbolListener2(className);

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(symbolListener, classFileContext);

        var symbolTable = symbolListener.getSymbolTable();

        //var contextScope = symbolListener.getContextScopes();

        //var semanticVisitor = new SemanticVisitor(className, contextScope);
        //semanticVisitor.visitClassFile(classFileContext);

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

    }
}
