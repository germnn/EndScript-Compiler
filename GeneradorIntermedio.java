package Proyecto;

import java.util.Map;

public class GeneradorIntermedio {
    private TablaSimbolos tabla;
    private Nodo arbol;
    private StringBuilder sb;
    private int contadorEtiquetas;

    public GeneradorIntermedio(TablaSimbolos tabla, Nodo arbol) {
        this.tabla = tabla;
        this.arbol = arbol;
        this.sb = new StringBuilder();
        this.contadorEtiquetas = 1;
    }

    public String generarDirectivas() {
        sb.append(formato("", "TITLE", "Progra1"));
        sb.append(formato("", ".MODEL", "Small"));
        sb.append(formato("", "INCLUDE", "MACROS.ASM"));
        sb.append(formato("", ".486", ""));
        sb.append(formato("", ".STACK", ""));
        
        sb.append(formato("", ".DATA", ""));
        for (Map.Entry<String, Simbolo> entry : tabla.getTabla().entrySet()) {
            String nombre = entry.getKey();
            String tipo = entry.getValue().getTipo();
            String directiva = tipo.equalsIgnoreCase("int") ? "DW" : "DB";
            sb.append(formato(nombre, directiva, "?"));
        }

        sb.append(formato("", ".CODE", ""));
        sb.append(formato("MAIN", "PROC", "FAR"));
        sb.append(formato("", ".STARTUP", ""));
        sb.append("\n");
        sb.append(formato("", ";", "--- INICIO DE INSTRUCCIONES ---"));
        traducirEstructura(arbol);
        sb.append(formato("", ";", "--- FIN DE INSTRUCCIONES ---"));
        sb.append("\n");
        sb.append(formato("", ".EXIT", ""));
        sb.append(formato("MAIN", "ENDP", ""));
        sb.append(formato("", "END", ""));

        return sb.toString();
    }

    private void traducirEstructura(Nodo nodo) {
        if (nodo == null) return;

        if (nodo.etiqueta.equals("S") && !nodo.hijos.isEmpty()) {
            String tipoSentencia = nodo.hijos.get(0).etiqueta;

            if (tipoSentencia.contains("=") || (nodo.hijos.size() >= 2 && nodo.hijos.get(1).etiqueta.contains("="))) {
                traducirAsignacion(nodo); return;
            } 
            if (tipoSentencia.contains("println")) {traducirPrintln(nodo); return; }
            if (tipoSentencia.contains("input")) {traducirInput(nodo); return;}
            if (tipoSentencia.contains("if")) {traducirIf(nodo); return;}
            if (tipoSentencia.contains("while")) {traducirWhile(nodo); return;}
        }

        for (Nodo hijo : nodo.hijos) { //sigue bajando por P, D, L
            traducirEstructura(hijo);
        }
    }

    private void traducirAsignacion(Nodo nodoS) {
        Nodo idNodo = nodoS.hijos.get(0);   
        Nodo expresionNodo = nodoS.hijos.get(2);  

        String idDestino = extraerValor(idNodo.etiqueta);

        if (expresionNodo.hijos.size() == 1) { // asignacion
            String valorExpresion = obtenerValorExpresion(expresionNodo);
            if (valorExpresion != null) {
                if (valorExpresion.equalsIgnoreCase("true")) valorExpresion = "1";
                if (valorExpresion.equalsIgnoreCase("false")) valorExpresion = "0";

                sb.append(formato("", "MOV", idDestino + ", " + valorExpresion));
            }
        } else if (expresionNodo.hijos.size() >= 5) { //operacion Binaria
            Nodo expr1 = expresionNodo.hijos.get(1);
            Nodo opNodo = expresionNodo.hijos.get(2);
            Nodo expr2 = expresionNodo.hijos.get(3);

            String val1 = obtenerValorExpresion(expr1);
            String operador = obtenerSimboloOperador(opNodo).replace("'", ""); 
            String val2 = obtenerValorExpresion(expr2);
            
            cargarEnAX(val1);
            cargarEnBX(val2);
            
            String operadorEnsamblador = "";
            switch (operador) {
                case "@": operadorEnsamblador = "ADD"; break; 
                case "#": operadorEnsamblador = "SUB"; break; 
                case "&": operadorEnsamblador = "MUL"; break; 
                case "|": operadorEnsamblador = "DIV"; break; 
                default:  operadorEnsamblador = "MOV"; break;
            }

            if (operadorEnsamblador.equals("MUL") || operadorEnsamblador.equals("DIV")) {
                sb.append(formato("", operadorEnsamblador, "BX"));
            } else {
                sb.append(formato("", operadorEnsamblador, "AX, BX"));
            }
            
            Simbolo simDest = tabla.getTabla().get(idDestino);
            if (simDest != null && simDest.getTipo().equalsIgnoreCase("boolean")) {
                sb.append(formato("", "MOV", idDestino + ", AL"));
            } else {
                sb.append(formato("", "MOV", idDestino + ", AX"));
            }
        }
    }

    private void traducirPrintln(Nodo nodoS) {
        Nodo idNodo = nodoS.hijos.get(1);
        String variable = extraerValor(idNodo.etiqueta);
        sb.append(formato("", ";", "--- PRINTLN " + variable + " ---"));
        cargarEnAX(variable);
        sb.append(formato("", "PRINT_NUM", "AX")); 
    }

    private void traducirInput(Nodo nodoS) {
        Nodo idNodo = nodoS.hijos.get(1);
        String variable = extraerValor(idNodo.etiqueta);
        sb.append(formato("", ";", "Input " + variable));
        sb.append(formato("", "MOV", "AH, 1"));
        sb.append(formato("", "INT", "21H"));
        sb.append(formato("", "MOV", variable + ", AL"));
    }

    private void traducirIf(Nodo nodoS) {
        Nodo expNodo = nodoS.hijos.get(1);
        Nodo bloqueThen = nodoS.hijos.get(3);
        Nodo bloqueElse = nodoS.hijos.get(5);

        String etiquetaElse = generarEtiqueta("ELSE");
        String etiquetaFinIf = generarEtiqueta("FIN_IF");

        sb.append(formato("", ";", "--- IF ---"));
        evaluarCondicionLogica(expNodo, etiquetaElse);

        traducirEstructura(bloqueThen);
        sb.append(formato("", "JMP", etiquetaFinIf));

        sb.append(formato(etiquetaElse + ":", "", ""));
        traducirEstructura(bloqueElse);

        sb.append(formato(etiquetaFinIf + ":", "", ""));
    }

    private void traducirWhile(Nodo nodoS) {
        Nodo expNodo = nodoS.hijos.get(1);
        Nodo bloqueDo = nodoS.hijos.get(3);

        String etiquetaInicio = generarEtiqueta("WHILE");
        String etiquetaFin = generarEtiqueta("FIN_WHL");

        sb.append(formato("", ";", "--- WHILE ---"));
        sb.append(formato(etiquetaInicio + ":", "", ""));

        evaluarCondicionLogica(expNodo, etiquetaFin);

        traducirEstructura(bloqueDo);
        sb.append(formato("", "JMP", etiquetaInicio)); 

        sb.append(formato(etiquetaFin + ":", "", ""));
    }

    private void evaluarCondicionLogica(Nodo expNodo, String etiquetaSalidaFalsa) {
        if (expNodo.hijos.size() == 1) {
            String valor = obtenerValorExpresion(expNodo);
            if (valor.equalsIgnoreCase("true")) valor = "1";
            if (valor.equalsIgnoreCase("false")) valor = "0";

            cargarEnAX(valor); 
            sb.append(formato("", "CMP", "AX, 1"));
            
            String etiquetaVerdadero = generarEtiqueta("VERDADERO");
            sb.append(formato("", "JE", etiquetaVerdadero));
            sb.append(formato("", "JMP", etiquetaSalidaFalsa));
            sb.append(formato(etiquetaVerdadero + ":", "", ""));
        } else if (expNodo.hijos.size() >= 5) {
            Nodo expr1 = expNodo.hijos.get(1);
            Nodo opNodo = expNodo.hijos.get(2);
            Nodo expr2 = expNodo.hijos.get(3);

            String val1 = obtenerValorExpresion(expr1);
            String val2 = obtenerValorExpresion(expr2);
            String operador = obtenerSimboloOperador(opNodo).replace("'", "");

            cargarEnAX(val1);
            cargarEnBX(val2);
            sb.append(formato("", "CMP", "AX, BX"));

            if (operador.equals("==")) {
                String etiquetaVerdadero = generarEtiqueta("VERDADERO");
                sb.append(formato("", "JE", etiquetaVerdadero));
                sb.append(formato("", "JMP", etiquetaSalidaFalsa));
                sb.append(formato(etiquetaVerdadero + ":", "", ""));
            }
        }
    }

    private void cargarEnAX(String valor) {
        Simbolo sim = tabla.getTabla().get(valor);
        if (sim != null && sim.getTipo().equalsIgnoreCase("boolean")) {
            sb.append(formato("", "MOV", "AL, " + valor));
            sb.append(formato("", "MOV", "AH, 0"));
        } else {
        	sb.append(formato("", "MOV", "AX, " + valor));
        }
    }

    private void cargarEnBX(String valor) {
        Simbolo sim = tabla.getTabla().get(valor);
        if (sim != null && sim.getTipo().equalsIgnoreCase("boolean")) {
            sb.append(formato("", "MOV", "BL, " + valor));
            sb.append(formato("", "MOV", "BH, 0"));
        } else {
            sb.append(formato("", "MOV", "BX, " + valor));
        }
    }

    private String generarEtiqueta(String prefijo) {return prefijo + (contadorEtiquetas++); }

    private String obtenerValorExpresion(Nodo nodoE) {
        if (!nodoE.hijos.isEmpty()) {
            Nodo hijo = nodoE.hijos.get(0);
            if (hijo.etiqueta.contains("BOOL") && !hijo.hijos.isEmpty()) {
                return extraerValor(hijo.hijos.get(0).etiqueta);
            }
            return extraerValor(hijo.etiqueta);
        }
        return extraerValor(nodoE.etiqueta);
    }

    private String obtenerSimboloOperador(Nodo nodoOp) {
        if (!nodoOp.hijos.isEmpty()) {
            return extraerValor(nodoOp.hijos.get(0).etiqueta);
        }
        return "";
    }

    private String extraerValor(String etiqueta) {
        if (etiqueta.contains("(") && etiqueta.contains(")")) {
            int inicio = etiqueta.indexOf('(') + 1;
            int fin = etiqueta.indexOf(')');
            return etiqueta.substring(inicio, fin);
        }
        return etiqueta;
    }

    private String formato(String col1, String col2, String col3) {
        return String.format("%-15s %-15s %s\n", col1, col2, col3);
    }
}