package Proyecto;

public class AnalizadorSemantico {
    private TablaSimbolos tabla;
    private StringBuilder arbolSemantico;

    public AnalizadorSemantico() {
        this.tabla = new TablaSimbolos();
        this.arbolSemantico = new StringBuilder();
    }

    public void analizar(String codigo) {
        arbolSemantico.setLength(0);
        String lineas[] = codigo.split("\n");

        for (String lineaOriginal : lineas) {
            String linea = lineaOriginal.trim();
           
            if (linea.isEmpty() || linea.startsWith("INICIO") || linea.startsWith("FIN") 
                    || linea.equals("else") || linea.equals("endif;")) {
                continue;
            }

            if (linea.startsWith("input ") || linea.startsWith("println ")) {
                String variable = linea.replace(";", "").split("\\s+")[1].trim();
                tabla.obtenerVariable(variable); 
                continue;
            }
            
            if (linea.startsWith("if ") && linea.endsWith("then")) {
                String condicion = linea.substring(2, linea.length() - 4).replace("(", "").replace(")", "").trim();
                String tipoCondicion = evaluarTipoOperacion(condicion);               
                if (!"boolean".equals(tipoCondicion)) {
                    throw new RuntimeException("[Error Semántico] Type Mismatch: La condición del 'if' debe ser de tipo 'boolean'.");
                }
                continue;
            }

            if (linea.startsWith("int ") || linea.startsWith("boolean ")) {
                String partes[] = linea.replace(";", "").split("\\s+");
                if (partes.length == 2) {
                    String tipo = partes[0];
                    String nombreVar = partes[1];
                    tabla.declararVariable(nombreVar, tipo);
                }
            } else if (linea.contains("=")) {
                String partes[] = linea.replace(";", "").split("=", 2);
                if (partes.length == 2) {
                    String variable = partes[0].trim();
                    String valorAsignado = partes[1].replace("(", "").replace(")", "").trim();
                    
                    Simbolo sim = tabla.obtenerVariable(variable);             
                    String tipoValor = "";
                  
                    if (esAsignacion(valorAsignado)) {
                        tipoValor = obtenerTipo(valorAsignado);
                    } else {
                        tipoValor = evaluarTipoOperacion(valorAsignado);
                        
                        String arbolOp = generarArbolOperacion(valorAsignado);
                        if (!arbolOp.isEmpty()) 
                            arbolSemantico.append("Árbol de Expresión:\n").append(arbolOp).append("\n");
                    }
                    
                    if (tipoValor != null && !sim.getTipo().equals(tipoValor)) {
                        throw new RuntimeException("[Error Semántico] Type Mismatch: No se puede asignar un valor de tipo '" 
                            + tipoValor + "' a la variable '" + variable + "' que es de tipo '" + sim.getTipo() + "'.");
                    }
                }
            }
        }
    }
    
    private boolean esAsignacion(String valor) {
        return valor.equals("true") || valor.equals("false") || valor.matches("\\d+") || tabla.contieneVariable(valor);
    }

    private String obtenerTipo(String valor) {
        if (valor.equals("true") || valor.equals("false")) {
            return "boolean";
        }
        if (valor.matches("\\d+")) {
            return "int";
        }
        if (tabla.contieneVariable(valor)) {
            return tabla.obtenerVariable(valor).getTipo();
        }
        return null;
    }
    
    private String evaluarTipoOperacion(String expresion) {
        String operadores[] = {"==", "@", "#", "&", "|"}; 
        
        for (String operador : operadores) {
            String operadorLiteral = operador.equals("|") ? "\\|" : operador;
            
            if (expresion.contains(operador)) {
                String[] lados = expresion.split(operadorLiteral);
                if (lados.length == 2) {
                    String tipoIzq = obtenerTipo(lados[0].trim());
                    String tipoDer = obtenerTipo(lados[1].trim());

                    if (tipoIzq == null || tipoDer == null) {
                        throw new RuntimeException("[Error Semántico] Operandos no válidos en la expresión: '" + expresion + "'.");
                    }

                    if (operador.equals("==")) {
                        if (!tipoIzq.equals(tipoDer)) {
                            throw new RuntimeException("[Error Semántico] Type Mismatch: No se puede comparar '" + tipoIzq + "' con '" + tipoDer + "' usando '=='.");
                        }
                        return "boolean";
                    } else if (operador.equals("&") || operador.equals("|") || operador.equals("@") || operador.equals("#")) {
                        if (!tipoIzq.equals("int") || !tipoDer.equals("int")) {
                            throw new RuntimeException("[Error Semántico] Type Mismatch: El operador matemático '" + operador + "' requiere tipos (int, int).");
                        }
                        return "int";
                    }
                }
            }
        }
   
        return obtenerTipo(expresion); //no es operacion binaria
    }
    
    private String generarArbolOperacion(String expresion) {
        String operadores[] = {"==", "@", "#", "&", "|"}; 
        for (String operador : operadores) {
            String opLiteral = operador.equals("|") ? "\\|" : operador;
            if (expresion.contains(operador)) {
                String lados[] = expresion.split(opLiteral);
                String izq = lados[0].trim();
                String der = lados[1].trim();
                
                String tipoRes = operador.equals("==") ? "boolean" : "int";
                
                return operador + " (" + tipoRes + ")\n" +
                       "├── " + izq + " (" + obtenerTipo(izq) + ")\n" +
                       "└── " + der + " (" + obtenerTipo(der) + ")";
            }
        }
        return "";
    }

    public String getArbolesTexto() {
        return arbolSemantico.toString();
    }
    
    public TablaSimbolos getTabla() {
        return tabla;
    }
}