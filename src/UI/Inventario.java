/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UI;

/**
 *
 * @author kira
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Inventario extends JFrame {
    private JTable tablaInventario;
    private DefaultTableModel modelo;
    private DefaultListModel<String> modeloAlertas;
    private JList<String> listaAlertas;
    private Set<Integer> filasConAlerta = new HashSet<>();

    public Inventario() {
        setTitle("Gestión de Inventario - Sistema de Alertas");
        setSize(1100, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.setPreferredSize(new Dimension(250, 0));
        panelDerecho.setBorder(BorderFactory.createTitledBorder("Panel de Notificaciones"));

        modeloAlertas = new DefaultListModel<>();
        listaAlertas = new JList<>(modeloAlertas);
        panelDerecho.add(new JScrollPane(listaAlertas), BorderLayout.CENTER);
        add(panelDerecho, BorderLayout.EAST);

        String[] columnas = {"ID", "Nombre", "Descripción", "Stock Actual", "Mín. Umbral", "Precio", "Suscrito"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int posicion) {
                if(posicion == 6){
                    return Boolean.class;
                }else if(posicion == 1 || posicion == 2){
                    return String.class;
                }else{
                    return Integer.class;
                }
            }
        };
        modelo.addRow(new Object[]{1, "Coco Rayado", "Bolsa 500g", 50, 10, 15.50, true});
        modelo.addRow(new Object[]{2, "Aceite de Coco", "Frasco de vidrio", 3, 5, 45.00, true});
        tablaInventario = new JTable(modelo);
        
        configurarResaltadoTabla();

        add(new JScrollPane(tablaInventario), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnSimularAlerta = new JButton("Simular Alerta Servidor");
        panelInferior.add(btnSimularAlerta);
        add(panelInferior, BorderLayout.SOUTH);

        btnSimularAlerta.addActionListener(e -> {
            recibirAlertaDesdeSocket(2, "¡Alerta! Stock crítico en Aceite de Coco.");
        });
    }

    private void configurarResaltadoTabla() {
        tablaInventario.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Si la fila está en nuestro set de alertas, la pintamos de rojo suave
                if (filasConAlerta.contains(row)) {
                    c.setBackground(new Color(255, 200, 200)); // Rojo claro
                    c.setForeground(Color.RED);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                
                if (isSelected) c.setBackground(table.getSelectionBackground());
                
                return c;
            }
        });
    }

    /**
     * RF09 y RF10: Método que debe ser llamado cuando el Socket recibe un mensaje.
     */
    public void recibirAlertaDesdeSocket(int idProducto, String mensaje) {
        SwingUtilities.invokeLater(() -> {
            modeloAlertas.insertElementAt("ID " + idProducto + ": " + mensaje, 0);
            for (int i = 0; i < modelo.getRowCount(); i++) {
                if (modelo.getValueAt(i, 0).equals(idProducto)) {
                    filasConAlerta.add(i);
                    break;
                }
            }
            tablaInventario.repaint(); // Refrescar colores
            
            // Opcional: Sonido o Popup
            Toolkit.getDefaultToolkit().beep();
        });
    }
}
