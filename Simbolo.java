
public class Simbolo {
    private String nombre;
    private String tipo;
    private int direccion;

    public Simbolo(String nombre, String tipo, int direccion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.direccion = direccion;
    }

	public String getNombre() {return nombre;}
	public String getTipo() {return tipo;}
	public int getDireccion() {return direccion;}  
}