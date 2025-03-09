parser grammar JasperParser;

options { tokenVocab=JasperLexer; }

classFile
    : packagePath? importPath*
    (variable | function | object | enum | union)* EOF
    ;

packagePath : PACKAGE path SEMICOLON;
importPath : IMPORT path SEMICOLON;

variable
    : PUBLIC? STATIC? MUTABLE? VARIABLE IDENTIFIER (COLON typeName array*)? (EQUALS (literals | reference | expression))? SEMICOLON
    ;

array
    : LEFT_BRACKET INTEGER_LITERAL? RIGHT_BRACKET
    ;

function
    : PUBLIC? STATIC? FUNCTION IDENTIFIER
    LEFT_PAREN (functionParameter (COMMA functionParameter)*)? RIGHT_PAREN
    (ARROW typeName)? LEFT_BRACE expression* RIGHT_BRACE
    ;

functionParameter
    : MUTABLE? IDENTIFIER COLON typeName array*
    ;

object
    : TYPE IDENTIFIER LEFT_BRACE (variable | function)* RIGHT_BRACE
    ;

enum
    : TYPE IDENTIFIER LEFT_BRACE enumMember (COMMA enumMember)* RIGHT_BRACE
    ;

enumMember
    : IDENTIFIER
    ;

union
    : TYPE IDENTIFIER LEFT_BRACE unionMember (COMMA unionMember)* RIGHT_BRACE
    ;

unionMember
    : IDENTIFIER OF typeName
    ;

ifStatement
    : IF expression LEFT_BRACE expression* RIGHT_BRACE
    elseIfStatement*
    elseStatement?
    ;

elseIfStatement
    : ELSE IF expression LEFT_BRACE expression* RIGHT_BRACE
    ;

elseStatement
    : ELSE LEFT_BRACE expression* RIGHT_BRACE
    ;

whileLoop
    : WHILE expression LEFT_BRACE expression* RIGHT_BRACE
    ;

forLoop
    : FOR IDENTIFIER IN typeName LEFT_BRACE expression* RIGHT_BRACE
    ;

matchStatement
    : MATCH IDENTIFIER LEFT_BRACE matchCase* matchDefaultCase? RIGHT_BRACE
    ;

matchCase
    : IDENTIFIER ARROW expression
    ;

matchDefaultCase
    : UNDERSCORE ARROW expression
    ;

functionCall
    : reference LEFT_PAREN (functionCallArgument (COMMA functionCallArgument)*)? RIGHT_PAREN SEMICOLON
    ;

functionCallArgument
    : literals | reference | expression
    ;

expression
    : reference | ifStatement | whileLoop | forLoop | matchStatement | functionCall | variable
    ;

typeName
    : baseTypes | reference
    ;

reference
    : IDENTIFIER | path
    ;

path : (IDENTIFIER PERIOD)+ (IDENTIFIER | STAR);

baseTypes: BYTE | UNSIGNED_BYTE | SHORT | UNSIGNED_SHORT | INT | UNSIGNED_INT | LONG | UNSIGNED_LONG | FLOAT | DOUBLE | BOOLEAN | STRING | CHAR;
literals: STRING_LITERAL | CHAR_LITERAL | INTEGER_LITERAL | FLOAT_LITERAL | TRUE | FALSE;