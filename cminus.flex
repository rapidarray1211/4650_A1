/*
  Created By: Jubair Ali
  File Name: cminus.flex
  To Build: jflex cminus.flex

  and then after the parser is created
    javac Lexer.java
*/
   
/* --------------------------Usercode Section------------------------ */
   
import java_cup.runtime.*;
      
%%
   
/* -----------------Options and Declarations Section----------------- */
   
/* 
   The name of the class JFlex will create will be Lexer.
   Will write the code to the file Lexer.java. 
*/
%class Lexer

%eofval{
  return null;
%eofval};

/*
  The current line number can be accessed with the variable yyline
  and the current column number with the variable yycolumn.
*/
%line
%column
    
/* 
   Will switch to a CUP compatibility mode to interface with a CUP
   generated parser.
*/

%cup
/*Output to String for testing*/
// %type String

   
/*
  Declarations
   
  Code between %{ and %}, both of which must be at the beginning of a
  line, will be copied letter to letter into the lexer class source.
  Here you declare member variables and functions that are used inside
  scanner actions.  
*/
%{   
    /* To create a new java_cup.runtime.Symbol with information about
       the current token, the token will have no value in this
       case. */
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    
    /* Also creates a new java_cup.runtime.Symbol with information
       about the current token, but this object has a value. */
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}
   

/*
  Macro Declarations
  
  These declarations are regular expressions that will be used latter
  in the Lexical Rules Section.  
*/
   
/* A line terminator is a \r (carriage return), \n (line feed), or
   \r\n. */
LineTerminator = \r|\n|\r\n
   
/* White space is a line terminator, space, tab, or form feed. */
WhiteSpace     = {LineTerminator} | [ \t\f]
   
/* Definitions for ID, NUM, COMMENT, and TRUTH. ID is any alphanumeric word starting with
   a character. NUM is any integer, TRUTH is either true or false. COMMENT refers 
   anything between /* and *\/ */
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
{WhiteSpace}+       {/* skip whitespace}*/}
.                   {return symbol(sym.ERR);}

/*THese outputs string for testing purposes*/
// {COMMENT}           {/*skip comments*/}
// "bool"              {return "<BOOL>";}
// "if"                {return "<IF>";}
// "else"              {return "<ELSE>";}
// "int"               {return "<INT>";}
// "return"            {return "<RETURN>";}
// "void"              {return "<VOID>";}
// "while"             {return "<WHILE";}
// "+"                 {return "<PLUS>";}
// "-"                 {return "<MINUS";}
// "*"                 {return "<ASTERISK>";}
// "/"                 {return "<DIVIDE>";}
// "<"                 {return "<LESSTHAN>";}
// ">"                 {return "<GREATERTHAN>";}
// "<="                {return "<LESSTHANE>";}
// ">="                {return "<GREATERTHANE>";}
// "=="                {return "<EQUALS";}
// "!="                {return "<NOTEQUAL>";}
// "~"                 {return "<TILDE>";}
// "||"                {return "<OR>";}
// "&&"                {return "<AND>";}
// "="                 {return "<ASsign>";}
// ";"                 {return "<semicolon>";}
// ","                 {return "<comma>";}
// "("                 {return "<LPAREN>";}
// ")"                 {return "<RPAREN>";}
// "["                 {return "<LSquaare>";}
// "]"                 {return "<RSQUARE>";}
// "{"                 {return "<LCURL>";}
// "}"                 {return "<RCURLY>";}
// {TRUTH}             {return "<TRUTH>";}
// {ID}                {return "<ID";}
// {NUM}               {return "<NUM>";}
// {WhiteSpace}+       {/* skip whitespace}*/}
// .                   {return "<ERR>";}
   
