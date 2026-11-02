package Proyecto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;

public class Main extends JFrame implements ActionListener {
    private static FileReader fr = null;
    private static BufferedReader bf = null;
    private static String cadena;
    private JTextArea areaCodigo, areaTokens, areaCI;
    private JPanel panelEstado;
    private JLabel lblMsgEstado;
    private JButton btnScanner, btnParser, btnSemantico, btnCI, btnCopiarCI;
    private ScannerL lexer;
    
    private Main() {
        super("EndScript Compiler - Arturo Delgado Germán");
        if (!Abrir()) {
            System.out.println("No pudo abrir el archivo.");
            return;
        }
        if (!Leer()) {
            System.out.println("No pudo leer el archivo CSV.");
            return;
        }
        hazInterfaz();
        hazEscuchadores();
    }
    
    private void hazInterfaz() {
        setSize(1300, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelCentral = new JPanel(new GridLayout(1, 2, 15, 0));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel panelIzq = new JPanel(new GridLayout(2, 1, 0, 10));
        
        JPanel panelSupIzq = new JPanel(new BorderLayout(5, 5));
        panelSupIzq.add(new JLabel("Programa:"), BorderLayout.NORTH);
        areaCodigo = new JTextArea();
        areaCodigo.setFont(new Font("Cascadia Code", Font.PLAIN, 16));
        areaCodigo.setTabSize(4);
        areaCodigo.setText(cadena);
        areaCodigo.setEditable(true);
        panelSupIzq.add(new JScrollPane(areaCodigo), BorderLayout.CENTER);
        panelIzq.add(panelSupIzq);

        JPanel panelInfIzq = new JPanel(new BorderLayout(5, 5));
        // Codigo Intermedio
        JPanel panelBotonCI = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        
        btnCI = new JButton("CI");
        btnCI.setFont(new Font("Cascadia Code", Font.PLAIN, 14));
        panelBotonCI.add(btnCI);
        
        btnCopiarCI = new JButton("Copiar CI");
        btnCopiarCI.setFont(new Font("Cascadia Code", Font.PLAIN, 14));
        panelBotonCI.add(btnCopiarCI);
        
        panelInfIzq.add(panelBotonCI, BorderLayout.NORTH);

        areaCI = new JTextArea();
        areaCI.setFont(new Font("Cascadia Code", Font.PLAIN, 18)); 
        areaCI.setTabSize(4);
        areaCI.setEditable(false);
        panelInfIzq.add(new JScrollPane(areaCI), BorderLayout.CENTER);
        
        panelIzq.add(panelInfIzq);
        panelCentral.add(panelIzq);
        // Parte central
        JPanel panelDer = new JPanel(new GridLayout(2, 1, 0, 10));
        JPanel panelSupDer = new JPanel(new BorderLayout(5, 5));    
        JPanel panelBotones = new JPanel(new GridLayout(1, 3, 10, 0));
        btnScanner = new JButton("Scanner");
        btnParser = new JButton("Parser");
        btnSemantico = new JButton("Semántico");

        Font fuenteBotones = new Font("Cascadia Code", Font.PLAIN, 14);
        btnScanner.setFont(fuenteBotones);
        btnParser.setFont(fuenteBotones);
        btnSemantico.setFont(fuenteBotones);
        
        panelBotones.add(btnScanner);
        panelBotones.add(btnParser);
        panelBotones.add(btnSemantico);
        panelSupDer.add(panelBotones, BorderLayout.NORTH);

        areaTokens = new JTextArea();
        areaTokens.setFont(new Font("Cascadia Code", Font.PLAIN, 15));
        areaTokens.setTabSize(4);
        areaTokens.setEditable(false);
        areaTokens.setLineWrap(true);
        areaTokens.setWrapStyleWord(true);
        panelSupDer.add(new JScrollPane(areaTokens), BorderLayout.CENTER);
        
        panelDer.add(panelSupDer);

        JPanel panelInfDer = new JPanel();
        panelDer.add(panelInfDer);
        
        panelCentral.add(panelDer);
        // Parte Derecha
        JPanel contenedorEstado = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 32));
        contenedorEstado.setPreferredSize(new Dimension(220, 0));

        panelEstado = new JPanel(new BorderLayout());
        panelEstado.setPreferredSize(new Dimension(205, 55));
        panelEstado.setBackground(Color.LIGHT_GRAY);
        panelEstado.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        lblMsgEstado = new JLabel("", SwingConstants.CENTER);
        lblMsgEstado.setFont(new Font("Cascadia Code", Font.BOLD, 12));
        lblMsgEstado.setForeground(Color.BLACK);
        panelEstado.add(lblMsgEstado, BorderLayout.CENTER);

        contenedorEstado.add(panelEstado);

        add(panelCentral, BorderLayout.CENTER);
        add(contenedorEstado, BorderLayout.EAST);
        setVisible(true);
    }
    
    private void hazEscuchadores() {
        btnScanner.addActionListener(this);
        btnParser.addActionListener(this);
        btnSemantico.addActionListener(this);
        btnCI.addActionListener(this);
        btnCopiarCI.addActionListener(this);
    }
    
    private void botones(ActionEvent e) {
        if (e.getSource() == btnScanner)
            ejecutarScanner();
        if (e.getSource() == btnParser)  
            ejecutarParser();
        if (e.getSource() == btnSemantico)
            ejecutarSemantico();
        if (e.getSource() == btnCI)
            ejecutarCI();
        if(e.getSource() == btnCopiarCI)
        	copiarCI();
    }
    
    private void ejecutarScanner() {
        String codigo = areaCodigo.getText();
        areaTokens.setText("");

        if (codigo.trim().isEmpty()) {
            marcarEstado(Color.LIGHT_GRAY, "El código está vacío", Color.BLACK);
            return;
        }

        try {
            lexer = new ScannerL(codigo);
            int tok = lexer.getToken();

            while (tok != Token.EOF) {
                String categoria = Token.categoriaToken(tok);
                String linea = String.format("%-8s -> %s\n", categoria, lexer.getTxtActual());
                areaTokens.append(linea);
                tok = lexer.getToken();
            }
            marcarEstado(new Color(39, 174, 96), "Léxico Correcto", Color.WHITE);
        } catch (RuntimeException ex) {
            marcarEstado(new Color(192, 57, 43), "Léxico Incorrecto", Color.WHITE);
            areaTokens.append("\n" + ex.getMessage());
        }
    }
    
    private void ejecutarParser() {
        String codigo = areaCodigo.getText();

        if (codigo.trim().isEmpty()) {
            marcarEstado(Color.LIGHT_GRAY, "El código está vacío", Color.BLACK);
            return;
        }

        try {
            lexer = new ScannerL(codigo);
            Parser parser = new Parser(lexer);
            Nodo arbol = parser.parseP();

            marcarEstado(new Color(39, 174, 96), "Sintáctico Correcto", Color.WHITE);
            arbol.imprimir();
        } catch (RuntimeException ex) {
            marcarEstado(new Color(192, 57, 43), "Sintáctico Incorrecto", Color.WHITE);
            areaTokens.setText(ex.getMessage());
        }
    }
    
    private void ejecutarSemantico() {
        String codigo = areaCodigo.getText();

        if (codigo.trim().isEmpty()) {
            marcarEstado(Color.LIGHT_GRAY, "El código está vacío", Color.BLACK);
            return;
        }

        try {
            AnalizadorSemantico semantico = new AnalizadorSemantico();
            semantico.analizar(codigo);

            marcarEstado(new Color(39, 174, 96), "Semántico Correcto", Color.WHITE);            
            areaTokens.setText(semantico.getTabla().obtenerImpresionTabla() + "\n" + semantico.getArbolesTexto());
        } catch (RuntimeException ex) {
            marcarEstado(new Color(192, 57, 43), "Error Semántico", Color.WHITE);
            areaTokens.setText(ex.getMessage());
        }
    }

    private void ejecutarCI() {
        String codigo = areaCodigo.getText();
        areaCI.setText("");

        if (codigo.trim().isEmpty()) {
            marcarEstado(Color.LIGHT_GRAY, "El código está vacío", Color.BLACK);
            return;
        }

        try {
        	ScannerL lex = new ScannerL(codigo);
            Parser parser = new Parser(lex);
            Nodo arbol = parser.parseP();
            AnalizadorSemantico semantico = new AnalizadorSemantico();
            semantico.analizar(codigo);

            GeneradorIntermedio genCI = new GeneradorIntermedio(semantico.getTabla(), arbol);
            String codigoIntermedio = genCI.generarDirectivas();

            areaCI.setText(codigoIntermedio);
            marcarEstado(new Color(39, 174, 96), "CI Generado", Color.WHITE);
        } catch (RuntimeException ex) {
            marcarEstado(new Color(192, 57, 43), "Error en CI", Color.WHITE);
            areaCI.setText("[Error] No se pudo generar el código intermedio:\n" + ex.getMessage());
        }
    }
    
    private void copiarCI() {
        String codigo = areaCI.getText();
        if (!codigo.isEmpty()) {
            StringSelection stringSelection = new StringSelection(codigo);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            marcarEstado(new Color(41, 128, 185), "CI Copiado", Color.WHITE);
        } else {
            marcarEstado(new Color(192, 57, 43), "No hay CI para copiar", Color.WHITE);
        }
    }

    private void marcarEstado(Color colorFondo, String mensaje, Color colorTexto) {
        panelEstado.setBackground(colorFondo);
        lblMsgEstado.setText(mensaje);
        lblMsgEstado.setForeground(colorTexto);
    }
    
    public static void main(String[] args) {
        new Main();
    }

    public void actionPerformed(ActionEvent e) {
        botones(e);
    }
    
    public static boolean Abrir() {
        try {
            fr = new FileReader("pruebas.csv");
            bf = new BufferedReader(fr);
        } catch (Exception obj) {
            return false;
        }
        return true;
    }
    
    public static boolean Leer() {
        try {
            String linea;
            StringBuilder programa = new StringBuilder();

            while((linea = bf.readLine()) != null) {
                programa.append(linea).append("\n");
            }
            
            if (programa.length() > 0) {
                cadena = programa.toString();
            }
        } catch (Exception obj) {
            return false;
        }
        return true;
    }
}