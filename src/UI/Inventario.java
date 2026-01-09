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
import javax.swing.table.TableRowSorter;

public class Inventario extends JFrame {
    private JTable tablaInventario;
    private DefaultTableModel modelo;
    private JButton btnNuevo, btnEliminar, btnGuardar, btnSimularAlerta;
    private DefaultListModel<String> modeloNotificaciones; 
    private JList<String> listaNotificaciones;
    private Set<Integer> filasConAlerta;
    private ClienteSocket cliente;
    private TableRowSorter<DefaultTableModel> sorter;

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

        String[] columnas = {"ID", "Nombre", "Descripción", "Stock Actual", "Mín. Umbral", "Precio", "Suscrito", "Vigencia"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 0 || col == 3 || col == 4 || col == 7) return Integer.class;
                if (col ==  5) return Double.class;
                if (col == 6) return Boolean.class;
                return String.class;
            }
        };
        tablaInventario = new JTable(modelo);
        sorter = new TableRowSorter<>(modelo);
        tablaInventario.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, 1, 7));

        tablaInventario.getColumnModel().getColumn(7).setMinWidth(0);
        tablaInventario.getColumnModel().getColumn(7).setMaxWidth(0);
        tablaInventario.getColumnModel().getColumn(7).setPreferredWidth(0);
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
        btnNuevo.addActionListener(e -> {
            modelo.addRow(new Object[]{0, "Nuevo Producto", "", 0, 0, 0.0, true, 1});
        });
        btnEliminar.addActionListener(e -> {
            int selectedRow = tablaInventario.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = tablaInventario.convertRowIndexToModel(selectedRow);
                modelo.setValueAt(0, modelRow, 7); // Vigencia = 0
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un producto para eliminar.");
            }
        });

        btnGuardar.addActionListener(e -> {
            java.util.List<Object[]> datosCompletos = new java.util.ArrayList<>();
            for (int i = 0; i < modelo.getRowCount(); i++) {
                Object[] fila = new Object[8];
                for (int j = 0; j < 8; j++) {
                    fila[j] = modelo.getValueAt(i, j);
                }
                datosCompletos.add(fila);
            }

            new Thread(() -> {
                boolean exito = cliente.guardarCambios(datosCompletos);
                SwingUtilities.invokeLater(() -> {
                    if(exito) JOptionPane.showMessageDialog(this, "Sincronización exitosa.");
                    else JOptionPane.showMessageDialog(this, "Error al guardar.");
                });
            }).start();
        });
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
            modeloNotificaciones.insertElementAt("📢 Actualización: " + mensaje, 0);
            if (mensaje.contains("|")) {
                try {
                    String[] partes = mensaje.split("\\|");
                    int id = Integer.parseInt(partes[0].trim());
                    marcarFilaConAlerta(id);
                } catch (Exception e) { }
            }
            cargarDatosDesdeServidor(); 
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