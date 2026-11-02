package Proyecto;
import java.util.LinkedHashMap;

public class TablaSimbolos {
    private LinkedHashMap<String, Simbolo> tabla;
    private int direccionActual;

    public TablaSimbolos() {
        this.tabla = new LinkedHashMap<>();
        this.direccionActual = 0;
    }

    public void declararVariable(String nombre, String tipo) {
        if (tabla.containsKey(nombre)) 
        {throw new RuntimeException("[Error Semántico] La variable '" + nombre + "' ya fue declarada previamente.");}
        
        int bytes = 0;
        if (tipo.equals("boolean")) bytes = 1;
        else bytes = 2; 

        Simbolo nuevoSimbolo = new Simbolo(nombre, tipo, direccionActual);
        tabla.put(nombre, nuevoSimbolo);
        direccionActual += bytes; 
    }

    public Simbolo obtenerVariable(String nombre) {
        if (!tabla.containsKey(nombre)) {
            throw new RuntimeException("[Error Semántico] La variable '" + nombre + "' no ha sido declarada.");
        }
        return tabla.get(nombre);
    }
    
    public boolean contieneVariable(String nombre) {
        return tabla.containsKey(nombre);
    }

    public String obtenerImpresionTabla() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- TABLA DE SÍMBOLOS ---\n");
        sb.append(String.format("%-10s %-10s %-15s\n", "NOMBRE", "TIPO", "DIRECCIÓN"));
        for (Simbolo s : tabla.values()) {
            sb.append(String.format("%-10s %-10s %-15d\n", s.getNombre(), s.getTipo(), s.getDireccion()));
        }
        return sb.toString();
    }
    
    public LinkedHashMap<String, Simbolo> getTabla(){
    	return tabla;
    }
}