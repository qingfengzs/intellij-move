package org.move.lang;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.move.lang.MvElementTypes.*;

%%

%{
  public _MvLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _MvLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

WHITESPACE=[ \n\t\r\f]
LINE_COMMENT=("//".*\n)|("//".*\R)
BLOCK_COMMENT="/"\*(.|[ \t\n\x0B\f\r])*\*"/"
ADDRESS_LITERAL=0x[0-9a-fA-F]{1,40}
BOOL_LITERAL=(true)|(false)
INTEGER_LITERAL=0|[1-9][0-9]*((u8)|(u64)|(u128))?
HEX_STRING_LITERAL=x\"([A-F0-9a-f]+)\"
BYTE_STRING_LITERAL=b\"(.*)\"
IDENTIFIER=[_a-zA-Z][_a-zA-Z0-9]*

%%
<YYINITIAL> {
  {WHITE_SPACE}              { return WHITE_SPACE; }

  "{"                        { return L_BRACE; }
  "}"                        { return R_BRACE; }
  "["                        { return L_BRACK; }
  "]"                        { return R_BRACK; }
  "("                        { return L_PAREN; }
  ")"                        { return R_PAREN; }
  "::"                       { return COLON_COLON; }
  ":"                        { return COLON; }
  "<"                        { return LT; }
  ">"                        { return GT; }
  "&"                        { return AND; }
  "|"                        { return OR; }
  "=="                       { return EQ_EQ; }
  "="                        { return EQ; }
//  "<="                       { return LT_EQ; }
//  "<<"                       { return LT_LT; }
//  ">="                       { return GT_EQ; }
//  ">>"                       { return GT_GT; }
//  "||"                       { return OR_OR; }
//  "&&"                       { return AND_AND; }
  "script"                   { return SCRIPT; }
  "address"                  { return ADDRESS; }
  "module"                   { return MODULE; }
  "const"                    { return CONST; }
  "public"                   { return PUBLIC; }
  "fun"                      { return FUN; }
  "acquires"                 { return ACQUIRES; }
  "resource"                 { return RESOURCE; }
  "struct"                   { return STRUCT; }
  "use"                      { return USE; }
  "as"                       { return AS; }
  "mut"                      { return MUT; }
  "copyable"                 { return COPYABLE; }
  "copy"                     { return COPY; }
  "move"                     { return MOVE; }
  "return"                   { return RETURN; }
  "abort"                    { return ABORT; }
  "break"                    { return BREAK; }
  "continue"                 { return CONTINUE; }
  "if"                       { return IF; }
  "else"                     { return ELSE; }
  "loop"                     { return LOOP; }
  "while"                    { return WHILE; }
  "let"                      { return LET; }

  {WHITESPACE}               { return WHITESPACE; }
  {LINE_COMMENT}             { return LINE_COMMENT; }
  {BLOCK_COMMENT}            { return BLOCK_COMMENT; }
  {ADDRESS_LITERAL}          { return ADDRESS_LITERAL; }
  {BOOL_LITERAL}             { return BOOL_LITERAL; }
  {INTEGER_LITERAL}          { return INTEGER_LITERAL; }
  {HEX_STRING_LITERAL}       { return HEX_STRING_LITERAL; }
  {BYTE_STRING_LITERAL}      { return BYTE_STRING_LITERAL; }
  {IDENTIFIER}               { return IDENTIFIER; }

}

[^] { return BAD_CHARACTER; }
