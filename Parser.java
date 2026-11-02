package Proyecto;

public class Parser {
    private ScannerL scanner;   
    private int tokenActual;     
    private String txtactual;     

    public Parser(ScannerL scanner) {
        this.scanner = scanner;
        advance(); 
    }

    private void advance() {
        tokenActual = scanner.getToken();
        txtactual = scanner.getTxtActual();
    }

    private Nodo eat(int tokenEsperado) {
        if (tokenActual == tokenEsperado) {
            Nodo hoja = new Nodo(Token.nombreToken(tokenEsperado)
                    + (tokenEsperado == Token.ID || tokenEsperado == Token.NUM ? "(" + txtactual + ")" : ""));
            advance();
            return hoja;
        } else {
            String extra = (tokenActual == Token.ID || tokenActual == Token.NUM) ? " ('" + txtactual + "') ": "";
            throw new RuntimeException("[Error Sintáctico] Se esperaba '"
                        + Token.nombreToken(tokenEsperado) + "' pero se encontró '"
                        + Token.nombreToken(tokenActual) + "'" + extra);
        }
    }

    public Nodo parseP() {
        Nodo nodo = new Nodo("P");
        nodo.agregarHijo(eat(Token.INICIO));
        nodo.agregarHijo(parseD());   
        nodo.agregarHijo(parseL());  
        nodo.agregarHijo(eat(Token.FIN));
        return nodo;
    }

    private Nodo parseD() {
        Nodo nodo = new Nodo("D");
        if (tokenActual == Token.INT || tokenActual == Token.BOOLEAN) {
            nodo.agregarHijo(parseT());
            nodo.agregarHijo(eat(Token.ID));
            nodo.agregarHijo(eat(Token.SEMI));
            nodo.agregarHijo(parseD());
        } else {
            nodo.agregarHijo(new Nodo("e"));
        }
        return nodo;
    }
    
    private Nodo parseT() {
        Nodo nodo = new Nodo("T");
        if (tokenActual == Token.INT) {
            nodo.agregarHijo(eat(Token.INT));
        } else if (tokenActual == Token.BOOLEAN) {
            nodo.agregarHijo(eat(Token.BOOLEAN));
        } else {
            throw new RuntimeException("[Error Sintáctico] Se esperaba 'int' o 'boolean'");
        }
        return nodo;
    }

    private Nodo parseL() {
        Nodo nodo = new Nodo("L");
        if (tokenActual == Token.ID || tokenActual == Token.INPUT || 
            tokenActual == Token.PRINTLN || tokenActual == Token.WHILE || tokenActual == Token.IF) {
            nodo.agregarHijo(parseS());
            nodo.agregarHijo(eat(Token.SEMI));
            nodo.agregarHijo(parseL());
        } else {
            nodo.agregarHijo(new Nodo("e"));
        }
        return nodo;
    }

    private Nodo parseS() {
        Nodo nodo = new Nodo("S");
        switch (tokenActual) {
            case Token.ID:
                nodo.agregarHijo(eat(Token.ID));
                nodo.agregarHijo(eat(Token.ASSIGN));
                nodo.agregarHijo(parseE());
                break;
            case Token.INPUT:
                nodo.agregarHijo(eat(Token.INPUT));
                nodo.agregarHijo(eat(Token.ID));
                break;
            case Token.PRINTLN:
                nodo.agregarHijo(eat(Token.PRINTLN));
                nodo.agregarHijo(eat(Token.ID));
                break;
            case Token.WHILE:
                nodo.agregarHijo(eat(Token.WHILE));
                nodo.agregarHijo(parseE());
                nodo.agregarHijo(eat(Token.DO));
                nodo.agregarHijo(parseL());
                nodo.agregarHijo(eat(Token.ENDWHILE));
                break;
            case Token.IF:
                nodo.agregarHijo(eat(Token.IF));
                nodo.agregarHijo(parseE());
                nodo.agregarHijo(eat(Token.THEN));
                nodo.agregarHijo(parseL());
                nodo.agregarHijo(eat(Token.ELSE));
                nodo.agregarHijo(parseL());
                nodo.agregarHijo(eat(Token.ENDIF));
                break;
            default:
                throw new RuntimeException("[Error Sintáctico] Se esperaba una sentencia (id, input, println, while, if) pero"
                							+ " se encontró '" + Token.nombreToken(tokenActual) + "'");
        }
        return nodo;
    }

    private Nodo parseE() {
        Nodo nodo = new Nodo("E");
        if (tokenActual == Token.ID || tokenActual == Token.NUM || 
                tokenActual == Token.TRUE || tokenActual == Token.FALSE) {
	        if (tokenActual == Token.ID) {
	            nodo.agregarHijo(eat(Token.ID));
	        } else if (tokenActual == Token.NUM) {
	            nodo.agregarHijo(eat(Token.NUM));
	        } else {
	            nodo.agregarHijo(parseBOOL());
	        }
        } else if (tokenActual == Token.LPAREN) {
            nodo.agregarHijo(eat(Token.LPAREN));
            nodo.agregarHijo(parseE());
            nodo.agregarHijo(parseOP());
            nodo.agregarHijo(parseE());
            nodo.agregarHijo(eat(Token.RPAREN)); 
        } else {
            throw new RuntimeException("[Error Sintáctico] Se esperaba id, num, boolean o '(' en la expresión");
        }
        return nodo;
    }
    
    private Nodo parseBOOL() {
        Nodo nodo = new Nodo("BOOL");
        if (tokenActual == Token.TRUE) {
            nodo.agregarHijo(eat(Token.TRUE));
        } else {
            nodo.agregarHijo(eat(Token.FALSE));
        }
        return nodo;
    }
    
    private Nodo parseOP() {
        Nodo nodo = new Nodo("OP");
        switch (tokenActual) {
            case Token.SUMA:  nodo.agregarHijo(eat(Token.SUMA)); break;
            case Token.RESTA: nodo.agregarHijo(eat(Token.RESTA)); break;
            case Token.MULT:  nodo.agregarHijo(eat(Token.MULT)); break;
            case Token.DIV:   nodo.agregarHijo(eat(Token.DIV)); break;
            case Token.EQ:    nodo.agregarHijo(eat(Token.EQ)); break;
            default: throw new RuntimeException("[Error Sintáctico] Se esperaba un operador (@, #, &, |, ==)");
        }
        return nodo;
    }
    
    
}