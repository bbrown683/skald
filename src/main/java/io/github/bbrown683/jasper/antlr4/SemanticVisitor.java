package io.github.bbrown683.jasper.antlr4;

import io.github.bbrown683.jasper.symbol.Symbol;
import io.github.bbrown683.jasper.symbol.SymbolTable;
import org.antlr.v4.runtime.ParserRuleContext;

public class SemanticVisitor extends JasperParserBaseVisitor<Void> {
    private String className;
    private SymbolTable symbolTable;

    public SemanticVisitor(String className, SymbolTable symbolTable) {
        this.className = className;
        this.symbolTable = symbolTable;
    }

    Symbol getSymbol(ParserRuleContext ctx) {
        var symbol = symbolTable.getSymbol(ctx);
        if (symbol == null) {
            System.out.println("Symbol not found for " + ctx.getText());
        } else {
            System.out.println(symbol.getName());
        }
        return symbol;
    }

    @Override
    public Void visitClassFile(JasperParser.ClassFileContext ctx) {
        var symbol = getSymbol(ctx);
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitFunction(JasperParser.FunctionContext ctx) {
        var symbol = getSymbol(ctx);
        visitChildren(ctx);
        return null;
    }

    @Override
    public Void visitVariable(JasperParser.VariableContext ctx) {
        var symbol = getSymbol(ctx);
        var visibleSymbols = symbol.getVisibleSymbols();
        visitChildren(ctx);
        return null;
    }
}
