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
        this(new ClienteSocket()); 
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

        // --- PANEL SUPERIOR ---
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevo = new JButton("Nuevo +");
        panelSuperior.add(new JLabel("Inventario de Productos    "));
        panelSuperior.add(btnNuevo);
        add(panelSuperior, BorderLayout.NORTH);

        // --- CONFIGURACIÓN DE TABLA (8 Columnas) ---
        String[] columnas = {"ID", "Nombre", "Descripción", "Stock Actual", "Mín. Umbral", "Precio", "Suscrito", "Vigencia"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 0 || col == 3 || col == 4 || col == 7) return Integer.class;
                if (col == 5) return Double.class;
                if (col == 6) return Boolean.class;
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return col != 0; // El ID es gestionado por el servidor
            }
        };

        tablaInventario = new JTable(modelo);
        
        // Sorter y Filtro para Vigencia (Oculta filas con Vigencia 0) 
        sorter = new TableRowSorter<>(modelo);
        tablaInventario.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, 1, 7));

        // Ocultar columna Vigencia (índice 7) de la vista
        tablaInventario.getColumnModel().getColumn(7).setMinWidth(0);
        tablaInventario.getColumnModel().getColumn(7).setMaxWidth(0);
        tablaInventario.getColumnModel().getColumn(7).setPreferredWidth(0);

        configurarResaltadoTabla(); 
        add(new JScrollPane(tablaInventario), BorderLayout.CENTER);

        // --- PANEL DE NOTIFICACIONES ---
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setBorder(BorderFactory.createTitledBorder("Alertas del Sistema"));
        panelDerecho.setPreferredSize(new Dimension(250, 0));
        
        modeloNotificaciones = new DefaultListModel<>();
        listaNotificaciones = new JList<>(modeloNotificaciones);
        listaNotificaciones.setForeground(Color.RED);
        panelDerecho.add(new JScrollPane(listaNotificaciones), BorderLayout.CENTER);
        add(panelDerecho, BorderLayout.EAST);

        // --- PANEL INFERIOR (BOTONES) ---
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnEliminar = new JButton("Desactivar Seleccionado");
        btnGuardar = new JButton("Sincronizar con Servidor");
        btnSimularAlerta = new JButton("Simular Alerta Local");

        panelInferior.add(btnEliminar);
        panelInferior.add(btnGuardar);
        panelInferior.add(btnSimularAlerta);
        add(panelInferior, BorderLayout.SOUTH);

        // --- LÓGICA DE EVENTOS ---

        btnSimularAlerta.addActionListener(e -> recibirAlertaDesdeSocket(2, "Stock crítico."));

        // RF11: Agregar fila localmente [cite: 7]
        btnNuevo.addActionListener(e -> {
            modelo.addRow(new Object[]{0, "Nuevo Producto", "", 0, 0, 0.0, true, 1});
        });

        // RF11: Borrado lógico (Vigencia = 0) 
        btnEliminar.addActionListener(e -> {
            int selectedRow = tablaInventario.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = tablaInventario.convertRowIndexToModel(selectedRow);
                modelo.setValueAt(0, modelRow, 7); // Cambiar vigencia a 0 (el filtro la ocultará)
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un producto.");
            }
        });

        // RF08 y RF11: Sincronización masiva al servidor 
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
                    else JOptionPane.showMessageDialog(this, "Error al guardar cambios.");
                });
            }).start();
        });

        cargarDatosDesdeServidor();
    }

    private void cargarDatosDesdeServidor() {
        new Thread(() -> {
            java.util.List<String[]> listaProductos = cliente.solicitarInventario();
            SwingUtilities.invokeLater(() -> {
                if (listaProductos != null) {
                    modelo.setRowCount(0); 
                    for (String[] datos : listaProductos) {
                        Object[] fila = new Object[8]; 
                        fila[0] = Integer.parseInt(datos[0]); // ID
                        fila[1] = datos[1];                   // Nombre
                        fila[2] = datos[2];                   // Descripción
                        fila[3] = Integer.parseInt(datos[3]); // Stock
                        fila[4] = Integer.parseInt(datos[4]); // Umbral
                        fila[5] = Double.parseDouble(datos[5]); // Precio
                        fila[6] = true;                       // Suscrito
                        fila[7] = Integer.parseInt(datos[6]); // Vigencia (desde servidor) 
                        modelo.addRow(fila);
                    }
                }
            });
        }).start();
    }

    public void agregarAlerta(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            if (mensaje.startsWith("ALERTA:")) {
                // 1. Extraer ID del mensaje "ALERTA:X" 
                int id = Integer.parseInt(mensaje.substring(7));

                // 2. Pedir detalles en un hilo para no bloquear la UI
                new Thread(() -> {
                    String[] d = cliente.obtenerDetallesProducto(id);
                    if (d != null) {
                        SwingUtilities.invokeLater(() -> {
                            // 3. Mostrar mensaje detallado al usuario [cite: 147]
                            String info = d[1] + " tiene stock bajo (" + d[3] + "/" + d[4] + ")";
                            modeloNotificaciones.insertElementAt("⚠️ " + info, 0);
                            marcarFilaConAlerta(id);
                            cargarDatosDesdeServidor(); // Refrescar tabla [cite: 250]
                        });
                    }
                }).start();
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