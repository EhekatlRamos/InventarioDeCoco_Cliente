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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Inventario extends JFrame {
    private JTable tablaInventario;
    private DefaultTableModel modelo;
    private JButton btnNuevo, btnEliminar, btnGuardar, btnRefrescar;
    private DefaultListModel<String> modeloNotificaciones;
    private JList<String> listaNotificaciones;
    
    private Set<Integer> filasConAlerta; 
    private List<Integer> idsParaDesactivar; 
    private ClienteSocket cliente;

    public Inventario(ClienteSocket cliente) {
        this.cliente = cliente;
        this.filasConAlerta = new HashSet<>(); 
        this.idsParaDesactivar = new ArrayList<>();
        
        setTitle("Gestión de Inventario - Sistema de Alertas");
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        cargarDatosDesdeServidor();
    }

    private void initComponents() {
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevo = new JButton("Nuevo Producto +");
        btnRefrescar = new JButton("Refrescar");
        panelSuperior.add(new JLabel("Inventario de Productos    "));
        panelSuperior.add(btnRefrescar);
        panelSuperior.add(btnNuevo);
        add(panelSuperior, BorderLayout.NORTH);

        // RF07: Listado en tabla 
        String[] columnas = {"ID", "Nombre", "Descripción", "Stock", "Umbral", "Precio", "Vigencia"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 0 || col == 3 || col == 4) return Integer.class;
                if (col == 5) return Double.class;
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int col) { return col != 0; }
        };

        tablaInventario = new JTable(modelo);
        configurarResaltadoTabla(); 
        add(new JScrollPane(tablaInventario), BorderLayout.CENTER);

        // RF09: Panel de notificaciones 
        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setBorder(BorderFactory.createTitledBorder("Alertas del Sistema"));
        panelDerecho.setPreferredSize(new Dimension(300, 0));
        modeloNotificaciones = new DefaultListModel<>();
        listaNotificaciones = new JList<>(modeloNotificaciones);
        panelDerecho.add(new JScrollPane(listaNotificaciones), BorderLayout.CENTER);
        add(panelDerecho, BorderLayout.EAST);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnEliminar = new JButton("Eliminar Seleccionado");
        btnGuardar = new JButton("GUARDAR CAMBIOS");
        btnGuardar.setBackground(new Color(0, 100, 0));
        btnGuardar.setForeground(Color.WHITE);

        panelInferior.add(btnEliminar);
        panelInferior.add(btnGuardar);
        add(panelInferior, BorderLayout.SOUTH);

        // Listeners CRUD Local
        btnNuevo.addActionListener(e -> modelo.addRow(new Object[]{0, "Nuevo", "...", 0, 0, 0.0, "1"}));

        btnEliminar.addActionListener(e -> {
            int fila = tablaInventario.getSelectedRow();
            if (fila >= 0) {
                int id = (int) modelo.getValueAt(fila, 0);
                if (id != 0) idsParaDesactivar.add(id); // Registrar para desactivar en server [cite: 77]
                modelo.removeRow(fila);
            }
        });

        btnGuardar.addActionListener(e -> {
            btnGuardar.setEnabled(false);
            new Thread(this::sincronizarConServidor).start();
        });

        btnRefrescar.addActionListener(e -> cargarDatosDesdeServidor());
    }

    private void sincronizarConServidor() {
        int vExitos = 0;
        int vErrores = 0;

        // Procesar cambios en tabla
        for (int i = 0; i < modelo.getRowCount(); i++) {
            int id = (int) modelo.getValueAt(i, 0);
            String nom = (String) modelo.getValueAt(i, 1);
            String des = (String) modelo.getValueAt(i, 2);
            int stock = (int) modelo.getValueAt(i, 3);
            int umb = (int) modelo.getValueAt(i, 4);
            double pre = (double) modelo.getValueAt(i, 5);

            if (id == 0) {
                if (cliente.insertarProducto(nom, des, stock, umb, pre)) vExitos++;
                else vErrores++;
            } else {
                if (cliente.actualizarCantidad(id, stock) && cliente.actualizarUmbral(id, umb)) vExitos++;
                else vErrores++;
            }
        }

        // Procesar bajas (Soft Delete) [cite: 81]
        for (int id : idsParaDesactivar) {
            if (cliente.desactivarProducto(id)) vExitos++;
            else vErrores++;
        }
        idsParaDesactivar.clear();

        // Solución al error de variables finales/efectivamente finales
        final int fExitos = vExitos;
        final int fErrores = vErrores;

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Sincronización terminada.\nÉxitos: " + fExitos + "\nErrores: " + fErrores);
            btnGuardar.setEnabled(true);
            cargarDatosDesdeServidor();
        });
    }

    private void cargarDatosDesdeServidor() {
        List<String[]> productos = cliente.solicitarInventario();
        SwingUtilities.invokeLater(() -> {
            modelo.setRowCount(0);
            filasConAlerta.clear();
            for (String[] p : productos) {
                modelo.addRow(new Object[]{
                    Integer.parseInt(p[0]), p[1], p[2], 
                    Integer.parseInt(p[3]), Integer.parseInt(p[4]), 
                    Double.parseDouble(p[5]), p[6]
                });
            }
        });
    }

    public void agregarAlerta(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.startsWith("ALERTA:")) { // Formato simplificado [cite: 126]
                try {
                    int id = Integer.parseInt(msg.substring(7).trim());
                    String detalles = cliente.obtenerProducto(id); // RF09: Detalle sin bloqueo 
                    modeloNotificaciones.insertElementAt("⚠️ " + detalles, 0);
                    marcarFilaConAlerta(id); // RF10: Resaltar producto 
                    Toolkit.getDefaultToolkit().beep();
                } catch (Exception e) { }
            }
        });
    }

    private void marcarFilaConAlerta(int id) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            Object cell = modelo.getValueAt(i, 0);
            if (cell != null && cell.toString().equals(String.valueOf(id))) {
                filasConAlerta.add(i);
                break;
            }
        }
        tablaInventario.repaint();
    }

    private void configurarResaltadoTabla() {
        tablaInventario.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (filasConAlerta.contains(r)) {
                    comp.setBackground(new Color(255, 200, 200)); // RF10: Resaltado visual 
                    comp.setForeground(Color.RED);
                } else {
                    comp.setBackground(s ? t.getSelectionBackground() : Color.WHITE);
                    comp.setForeground(s ? t.getSelectionForeground() : Color.BLACK);
                }
                return comp;
            }
        });
    }
}