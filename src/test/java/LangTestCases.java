import io.github.bbrown683.lang.LangVisitorASM;
import io.github.bbrown683.lang.antlr4.LangLexer;
import io.github.bbrown683.lang.antlr4.LangParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LangTestCases extends ClassLoader {
    Map<String, byte[]> classes = new HashMap<>();

    private InputStream loadFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        var inputStream = classLoader.getResourceAsStream(fileName);
        if(inputStream == null) {
            throw new RuntimeException("Could not find file");
        }
        return inputStream;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] b = classes.get(name);
        if (b == null) {
            throw new ClassNotFoundException(name);
        }
        return defineClass(name, b, 0, b.length);
    }

    private byte[] getBytecode(String className, InputStream inputStream) throws IOException {
        CharStream charStream = CharStreams.fromStream(inputStream);
        var lexer = new LangLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new LangParser(tokenStream);

        LangVisitorASM visitor = new LangVisitorASM(className, true);
        return visitor.visitClassFile(parser.classFile());
    }

    @Test
    public void helloWorldTest() {
        String filename = "HelloWorld.lang";
        try(InputStream inputStream = loadFile(filename)) {
            String className = filename.split("\\.")[0];
            String classPath = "language.sample." + className;

            byte[] bytecode = getBytecode(className, inputStream);
            classes.put(classPath, bytecode);

            Class<?> exampleClass = findClass(classPath);
            var mainMethod = exampleClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) new String[0]);
        } catch(Exception e) {
            System.err.println("Failed to parse file due to error: " + e.getMessage());
        }
    }

    @Test
    public void testVariable() {
        String filename = "Variable.lang";
        try(InputStream inputStream = loadFile(filename)) {
            String className = filename.split("\\.")[0];
            String classPath = "language.sample." + className;

            byte[] bytecode = getBytecode(className, inputStream);
            classes.put(classPath, bytecode);

//            Class<?> exampleClass = findClass(classPath);
//            var mainMethod = exampleClass.getMethod("main", String[].class);
//            mainMethod.invoke(null, (Object) new String[0]);
        } catch(Exception e) {
            System.err.println("Failed to parse file due to error: " + e.getMessage());
        } }
}
