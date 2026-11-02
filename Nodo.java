package Proyecto;
import java.util.ArrayList;
import java.util.List;

public class Nodo {
    public String etiqueta;
    public List<Nodo> hijos;

    public Nodo(String etiqueta) {
        this.etiqueta = etiqueta;
        this.hijos = new ArrayList<>();
    }

    public void agregarHijo(Nodo hijo) {
        hijos.add(hijo);
    }

    public void imprimir() {
        imprimirRecursivo("", true);
    }

    private void imprimirRecursivo(String prefijo, boolean esUltimo) {
        if (prefijo.isEmpty()) {
            System.out.println(etiqueta);
        } else {
            System.out.println(prefijo + (esUltimo ? "+-- " : "|-- ") + etiqueta);
        }
        String nuevoPrefijo = prefijo + (esUltimo ? "    " : "|   ");
        for (int i = 0; i < hijos.size(); i++) {
            hijos.get(i).imprimirRecursivo(nuevoPrefijo, i == hijos.size()-1);
        }
    }
}
