/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

/**
 *
 * @author kira
 */
import conexion.ClienteSocket;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Inventario extends JFrame {
    private JTable tablaInventario;
    private DefaultTableModel modelo;
    private JButton btnNuevo, btnEliminar, btnGuardar, btnSimularAlerta;
    private DefaultListModel<String> modeloNotificaciones; 
    private JList<String> listaNotificaciones;
    private Set<Integer> filasConAlerta;
    private ClienteSocket cliente;

    public Inventario() {
        this(new ClienteSocket()); // Llama al constructor real con un socket vacío
        System.out.println("Aviso: Iniciando en modo de prueba sin conexión.");
    }
    
    public Inventario(ClienteSocket cliente) {
        this.cliente = cliente;
        setTitle("Gestión de Inventario - Sistema de Alertas");
        setSize(1000, 500); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        filasConAlerta = new HashSet<>();

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevo = new JButton("Nuevo +");
        panelSuperior.add(new JLabel("Inventario de Productos    "));
        panelSuperior.add(btnNuevo);
        add(panelSuperior, BorderLayout.NORTH);

        String[] columnas = {"ID", "Nombre", "Descripción", "Stock Actual", "Mín. Umbral", "Precio", "Suscrito"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int posicion) {
                switch (posicion) {
                    case 0: case 3: case 4: return Integer.class;
                    case 5: return Double.class;
                    case 6: return Boolean.class;
                    default: return String.class;
                }
            }
        };

        modelo.addRow(new Object[]{1, "Coco Rayado", "Bolsa 500g", 50, 10, 15.50, true});
        modelo.addRow(new Object[]{2, "Aceite de Coco", "Frasco de vidrio", 3, 5, 45.00, true});

        tablaInventario = new JTable(modelo);
        configurarResaltadoTabla(); 
        add(new JScrollPane(tablaInventario), BorderLayout.CENTER);

        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setBorder(BorderFactory.createTitledBorder("Alertas del Sistema"));
        panelDerecho.setPreferredSize(new Dimension(250, 0));
        
        modeloNotificaciones = new DefaultListModel<>();
        listaNotificaciones = new JList<>(modeloNotificaciones);
        listaNotificaciones.setForeground(Color.RED);
        panelDerecho.add(new JScrollPane(listaNotificaciones), BorderLayout.CENTER);
        add(panelDerecho, BorderLayout.EAST);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnEliminar = new JButton("Eliminar Seleccionado");
        btnGuardar = new JButton("Guardar Cambios");
        btnSimularAlerta = new JButton("Simular Alerta Local");

        panelInferior.add(btnEliminar);
        panelInferior.add(btnGuardar);
        panelInferior.add(btnSimularAlerta);
        add(panelInferior, BorderLayout.SOUTH);

        btnSimularAlerta.addActionListener(e -> {
            recibirAlertaDesdeSocket(2, "Stock crítico.");
        });
        cargarDatosDesdeServidor();
    }
    private void cargarDatosDesdeServidor() {
        // Llamar al nuevo método que devuelve la lista procesada
        java.util.List<String[]> listaProductos = cliente.solicitarInventario();

        if (!listaProductos.isEmpty()) {
            modelo.setRowCount(0); // Limpiar la tabla antes de llenar

            for (String[] datos : listaProductos) {
                // Tu tabla tiene 7 columnas, pero el servidor manda 6.
                // Mapeo: ID, Nombre, Descripción, Cantidad, Umbral, Precio + [Suscrito]
                Object[] filaParaTabla = new Object[7];
                filaParaTabla[0] = Integer.parseInt(datos[0]); // ID
                filaParaTabla[1] = datos[1];                   // Nombre
                filaParaTabla[2] = datos[2];                   // Descripción
                filaParaTabla[3] = Integer.parseInt(datos[3]); // Stock Actual
                filaParaTabla[4] = Integer.parseInt(datos[4]); // Mín. Umbral
                filaParaTabla[5] = Double.parseDouble(datos[5]); // Precio
                filaParaTabla[6] = true;                       // Suscrito (Default local)

                modelo.addRow(filaParaTabla);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No se recibieron productos del servidor.");
        }
    }

    public void agregarAlerta(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            modeloNotificaciones.insertElementAt("⚠️ " + mensaje, 0);
            Toolkit.getDefaultToolkit().beep();

            if (mensaje.contains("|")) {
                try {
                    String[] partes = mensaje.split("\\|");
                    int id = Integer.parseInt(partes[0].trim());
                    String texto = partes[1].trim();
                    marcarFilaConAlerta(id);
                } catch (Exception e) {
                }
            }
        });
    }

    private void marcarFilaConAlerta(int idProducto) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (modelo.getValueAt(i, 0).equals(idProducto)) {
                filasConAlerta.add(i);
                break;
            }
        }
        tablaInventario.repaint();
    }

    public void recibirAlertaDesdeSocket(int idProducto, String mensaje) {
        agregarAlerta(idProducto + " | " + mensaje);
    }

    private void configurarResaltadoTabla() {
        tablaInventario.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (filasConAlerta.contains(row)) {
                    c.setBackground(new Color(255, 200, 200)); 
                    c.setForeground(Color.RED);
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                    c.setForeground(isSelected ? table.getSelectionForeground() : Color.BLACK);
                }
                return c;
            }
        });
    }
}