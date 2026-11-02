
public class Token {
    public static final int INICIO = 1, FIN = 2, INT = 3, BOOLEAN = 4;
    public static final int INPUT = 5, PRINTLN = 6, WHILE = 7, DO = 8;
    public static final int ENDWHILE = 9, IF = 10, THEN = 11, ELSE = 12;
    public static final int ENDIF = 13, TRUE = 14, FALSE = 15;
    
    public static final int ID = 16, NUM = 17;
    
    public static final int ASSIGN = 18, EQ = 19, SEMI = 20;
    public static final int LPAREN = 21, RPAREN = 22;
    public static final int SUMA = 23, RESTA = 24, MULT = 25, DIV = 26;
    
    public static final int EOF = 27;

    public static String nombreToken(int tok) {
        switch (tok) {
            case INICIO: return "INICIO"; case FIN: return "FIN";
            case INT: return "int"; case BOOLEAN: return "boolean";
            case INPUT: return "input"; case PRINTLN: return "println";
            case WHILE: return "while"; case DO: return "do";
            case ENDWHILE: return "endwhile"; case IF: return "if";
            case THEN: return "then"; case ELSE: return "else";
            case ENDIF: return "endif"; case TRUE: return "true";
            case FALSE: return "false"; case ID: return "id";
            case NUM: return "num"; case ASSIGN: return "'='";
            case EQ: return "'=='"; case SEMI: return "';'";
            case LPAREN: return "'('"; case RPAREN: return "')'";
            case SUMA: return "'@'"; case RESTA: return "'#'";
            case MULT: return "'&'"; case DIV: return "'|'";
            case EOF: return "<eof>"; default: return "?(" + tok + ")";
        }
    }
    
    public static String categoriaToken(int tok) {
        if (tok >= Token.INICIO && tok <= Token.FALSE) {
            return "PR";
        }
        switch (tok) {
            case ID:
                return "ID";
            case NUM:
                return "NUM";
            case SUMA: case RESTA: case MULT: case DIV:
                return "Op. Arit";
            case ASSIGN: case EQ:
                return "Op. Rel";
            case SEMI: case LPAREN: case RPAREN:
                return "Punt";
            case EOF:
                return "EOF";
            default:
                return "Desconocido";
        }
    }
}