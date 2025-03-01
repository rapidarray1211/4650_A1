/*
  Created By: Jubair Ali
  File Name: cminus.flex
  To Build: jflex cminus.flex

  and then after the parser is created
    javac Lexer.java
*/
   
   
import java_cup.runtime.*;
      
%%

%class Lexer

%eofval{
  return null;
%eofval};

%line
%column
    

%cup

   
%{   

    private Symbol symbol(int type) {
        System.out.println("Scanned Token: " + sym.terminalNames[type]);
        return new Symbol(type, yyline, yycolumn);
    }
    
    private Symbol symbol(int type, Object value) {
        System.out.println("Scanned Token: " + sym.terminalNames[type] + " | Val: " + value);
        return new Symbol(type, yyline, yycolumn, value);
    }
%}
   



LineTerminator = \r|\n|\r\n
   
/* White space is a line terminator, space, tab, or form feed. */
WhiteSpace     = {LineTerminator} | [ \t\f]
   
/* Definitions for ID, NUM, COMMENT, and TRUTH. ID is any alphanumeric word starting with
   a character. NUM is any integer, TRUTH is either true or false. COMMENT refers 
   anything between /* and */ */

ID = [_a-zA-Z][_a-zA-Z0-9]*
NUM = [0-9]+
TRUTH = (false)|(true)
COMMENT = "/*"([^\*]|(\*+[^\/]))*"*/"
   
%%
/* ------------------------Lexical Rules Section---------------------- */
   
/*
   This section contains regular expressions and actions, i.e. Java
   code, that will be executed when the scanner matches the associated
   regular expression. */
{COMMENT}           {/*skip comments*/}
"bool"              {return symbol(sym.BOOL);}
"if"                {return symbol(sym.IF);}
"else"              {return symbol(sym.ELSE);}
"int"               {return symbol(sym.INT);}
"return"            {return symbol(sym.RETURN);}
"void"              {return symbol(sym.VOID);}
"while"             {return symbol(sym.WHILE);}
"+"                 {return symbol(sym.PLUS);}
"-"                 {return symbol(sym.MINUS);}
"*"                 {return symbol(sym.TIMES);}
"/"                 {return symbol(sym.DIVIDE);}
"<"                 {return symbol(sym.LT);}
">"                 {return symbol(sym.GT);}
"<="                {return symbol(sym.LTE);}
">="                {return symbol(sym.GTE);}
"=="                {return symbol(sym.EQ);}
"!="                {return symbol(sym.NEQ);}
"~"                 {return symbol(sym.TILDE);}
"||"                {return symbol(sym.OR);}
"&&"                {return symbol(sym.AND);}
"="                 {return symbol(sym.ASSIGN);}
";"                 {return symbol(sym.SCOLON);}
","                 {return symbol(sym.COMMA);}
"("                 {return symbol(sym.LPAREN);}
")"                 {return symbol(sym.RPAREN);}
"["                 {return symbol(sym.LSQUARE);}
"]"                 {return symbol(sym.RSQUARE);}
"{"                 {return symbol(sym.LCURLY);}
"}"                 {return symbol(sym.RCURLY);}
{TRUTH}             {return symbol(sym.TRUTH,yytext());}
{ID}                {return symbol(sym.ID,yytext());}
{NUM}               {return symbol(sym.NUM,yytext());}
{WhiteSpace}+       {}
.                   {return symbol(sym.ERR);}
