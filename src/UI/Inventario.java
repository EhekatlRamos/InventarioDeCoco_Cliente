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
import java.awt.*;

public class Inventario extends JFrame {
    private JTable tablaInventario;
    private DefaultTableModel modelo;
    private JButton btnNuevo;
    private JButton btnEliminar;
    private JButton btnGuardar;
    private JButton btnImagen;

    public Inventario() {
        setTitle("Gestión de Inventario - Parámetros Completos");
        setSize(900, 500); // Un poco más ancho para la nueva columna
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Panel Superior
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevo = new JButton("Nuevo +");
        panelSuperior.add(new JLabel("Inventario de Productos    "));
        panelSuperior.add(btnNuevo);
        add(panelSuperior, BorderLayout.NORTH);

        // 2. Configuración de la Tabla con el campo "Suscrito"
        String[] columnas = {"ID", "Nombre", "Descripción", "Stock Actual", "Mín. Umbral", "Precio", "Suscrito"};
        
        // Sobrescribimos getColumnClass para que la columna "Suscrito" muestre un Checkbox
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // La columna 6 es "Suscrito" (empezando desde 0)
                if (columnIndex == 6) {
                    return Boolean.class; 
                }
                return super.getColumnClass(columnIndex);
            }
        };

        // Datos de ejemplo (Agregamos true/false al final)
        modelo.addRow(new Object[]{"1", "Coco Rayado", "Bolsa 500g", "50", "10", "15.50", true});
        modelo.addRow(new Object[]{"2", "Aceite de Coco", "Frasco de vidrio", "20", "5", "45.00", false});

        tablaInventario = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tablaInventario);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Panel Inferior
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnEliminar = new JButton("Eliminar Seleccionado");
        btnGuardar = new JButton("Guardar Cambios");
        
        // Botón con imagen (Configuración simplificada)
        try {
            ImageIcon icono = new ImageIcon(getClass().getResource("/UI/icons/info.png"));
            Image img = icono.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            btnImagen = new JButton("Detalles", new ImageIcon(img));
        } catch (Exception e) {
            btnImagen = new JButton("Detalles");
        }

        panelInferior.add(btnEliminar);
        panelInferior.add(btnGuardar);
        panelInferior.add(btnImagen);
        add(panelInferior, BorderLayout.SOUTH);

        // --- LÓGICA ---

        // Nuevo ítem con el booleano por defecto en false
        btnNuevo.addActionListener(e -> {
            modelo.addRow(new Object[]{"0", "Nuevo item", "...", "0", "0", "0.00", false});
        });

        btnEliminar.addActionListener(e -> {
            int fila = tablaInventario.getSelectedRow();
            if (fila >= 0) modelo.removeRow(fila);
            else JOptionPane.showMessageDialog(null, "Selecciona una fila.");
        });

        btnGuardar.addActionListener(e -> {
            JOptionPane.showMessageDialog(null, "Cambios guardados.");
        });
    }
}
