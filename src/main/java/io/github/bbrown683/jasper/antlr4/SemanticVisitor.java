package io.github.bbrown683.jasper.antlr4;

import io.github.bbrown683.jasper.symbol.Scope;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Map;
import java.util.Stack;

public class SemanticVisitor extends JasperParserBaseVisitor<Void> {
    private String className;
    private Map<ParserRuleContext,Stack<Scope>> contextScopes;

    public SemanticVisitor(String className, Map<ParserRuleContext, Stack<Scope>> contextScopes) {
        this.className = className;
        this.contextScopes = contextScopes;
    }

    Stack<Scope> getScope(ParserRuleContext ctx) {
        var scope = contextScopes.get(ctx);
        if (scope == null) {
            System.out.println("Scope not found");
        } else {
            System.out.println(scope);
        }
        return scope;
    }

    @Override
    public Void visitClassFile(JasperParser.ClassFileContext ctx) {
        var scope = getScope(ctx);
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitFunction(JasperParser.FunctionContext ctx) {
        var scope = getScope(ctx);
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitVariable(JasperParser.VariableContext ctx) {
        System.out.println(ctx.getText());
        var scope = getScope(ctx);
        visitChildren(ctx);
        return null;
    }
}
