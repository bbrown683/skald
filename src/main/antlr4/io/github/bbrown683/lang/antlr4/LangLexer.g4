lexer grammar LangLexer;

PACKAGE: 'package';
IMPORT: 'import';
TYPE: 'type';
VARIABLE: 'var';
FUNCTION: 'fn';
MUTABLE: 'mut';
PUBLIC: 'pub';
NEW: 'new';
IF: 'if';
ELSE: 'else';
WHILE: 'while';
FOR: 'for';
IN: 'in';
OF: 'of';
MATCH: 'match';
STATIC: 'static';

PLUS: '+';
MINUS: '-';
STAR: '*';
EQUALS: '=';
COLON: ':';
UNDERSCORE: '_';
SEMICOLON: ';';
PERIOD: '.';
COMMA: ',';
ARROW: '->';
LEFT_PAREN: '(';
RIGHT_PAREN: ')';
LEFT_BRACKET: '[';
RIGHT_BRACKET: ']';
LEFT_BRACE: '{';
RIGHT_BRACE: '}';
PIPE: '|';

BYTE: 'byte';
UNSIGNED_BYTE: 'ubyte';
SHORT: 'short';
UNSIGNED_SHORT: 'ushort';
INT: 'int';
UNSIGNED_INT: 'uint';
LONG: 'long';
UNSIGNED_LONG: 'ulong';
FLOAT: 'float';
DOUBLE: 'double';
BOOLEAN: 'bool';
CHAR: 'char';

STRING_LITERAL: '"' ~["\r\n]* '"';
CHAR_LITERAL: '\'' ~['\r\n]* '\'';
INTEGER_LITERAL: '-'? [0-9]+;
FLOAT_LITERAL: [0-9]+ '.' [0-9]+;
TRUE: 'true';
FALSE: 'false';

IDENTIFIER: [a-zA-Z][a-zA-Z0-9_]*;

COMMENT: '//' ~[\r\n\f]* -> skip;
MULTILINE_COMMENT: '/*' .*? '*/' -> skip;
WS: [ \t\r\n]+ -> skip;