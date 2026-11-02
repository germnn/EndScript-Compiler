
public class ScannerL {
    private String entrada, txtActual;   
    private int linea, columna, pos;  

    public ScannerL(String entrada) {
        this.entrada = entrada;
        this.linea = 1;
        this.columna = 1;
        this.pos = 0;
        this.txtActual = "";
    }
    
    public void reiniciar() {
        this.pos = 0;
        this.linea = 1;
        this.columna = 1;
        this.txtActual = "";
    }

    public int getToken() {
        while (pos < entrada.length() && Character.isWhitespace(entrada.charAt(pos))) {
            avanzar();
        }

        if (pos >= entrada.length()) {
            txtActual = "<eof>";
            return Token.EOF;
        }

        char c = entrada.charAt(pos);
        
        if (Character.isLetter(c)) return reconocerIDoPR();
        if (Character.isDigit(c))  return reconocerNum();
        
        switch (c) {
        	case '=':
                avanzar();
                if (pos < entrada.length() && entrada.charAt(pos) == '=') {
                    avanzar();
                    txtActual = "==";
                    return Token.EQ;
                }
                txtActual = "=";
                return Token.ASSIGN;
            case '@': avanzar(); txtActual = "@"; return Token.SUMA;
            case '#': avanzar(); txtActual = "#"; return Token.RESTA;
            case '&': avanzar(); txtActual = "&"; return Token.MULT;
            case '|': avanzar(); txtActual = "|"; return Token.DIV;
            case '(': avanzar(); txtActual = "("; return Token.LPAREN;
            case ')': avanzar(); txtActual = ")"; return Token.RPAREN;
            case ';': avanzar(); txtActual = ";"; return Token.SEMI;
            default:
                throw new RuntimeException("[Error Léxico] Carácter inválido: '" + c + "' en linea "+linea+", columna "+columna);
        }
    }

    private int reconocerIDoPR() {
        int inicio=pos;
        while (pos < entrada.length() && Character.isLetterOrDigit(entrada.charAt(pos))) 
        {avanzar();}
        txtActual = entrada.substring(inicio, pos);
        switch (txtActual) {
            case "INICIO":   return Token.INICIO;
            case "FIN":      return Token.FIN;
            case "int":      return Token.INT;
            case "boolean":  return Token.BOOLEAN;
            case "input":    return Token.INPUT;
            case "println":  return Token.PRINTLN;
            case "while":    return Token.WHILE;
            case "do":       return Token.DO;
            case "endwhile": return Token.ENDWHILE;
            case "if":       return Token.IF;
            case "then":     return Token.THEN;
            case "else":     return Token.ELSE;
            case "endif":    return Token.ENDIF;
            case "true":     return Token.TRUE;
            case "false":    return Token.FALSE;
            default:         return Token.ID;
        }
    }

    private int reconocerNum() {
        int inicio = pos;
        while (pos < entrada.length() && Character.isDigit(entrada.charAt(pos))) {
            avanzar();
        }
        txtActual = entrada.substring(inicio, pos);
        return Token.NUM;
    }
    
    public String getTxtActual() {
        return txtActual;
    }
    
    private void avanzar() {
        if(entrada.charAt(pos) == '\n') {
            linea++;
            columna = 1;
        } else {
            columna++;
        }
        pos++;
    }
}
