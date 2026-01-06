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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Inventario extends JFrame {
    private JTable tablaInventario;
    private DefaultTableModel modelo;
    private JButton btnNuevo;
    private JButton btnEliminar;
    private JButton btnGuardar;

    public Inventario() {
        // Configuración de la ventana
        setTitle("Gestión de Inventario");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. Encabezado y Botón "Nuevo" (según tu boceto)
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevo = new JButton("Nuevo +");
        panelSuperior.add(new JLabel("Inventario de Productos    "));
        panelSuperior.add(btnNuevo);
        add(panelSuperior, BorderLayout.NORTH);

        // 2. Configuración de la Tabla (CRUD Local)
        // Columnas correspondientes a tu dibujo: Nombre, Cantidad, Límite
        String[] columnas = {"Nombre", "Cantidad", "Límite"};
        modelo = new DefaultTableModel(columnas, 0); // 0 indica que inicia vacía
        
        // Datos de ejemplo "falsificados"
        modelo.addRow(new Object[]{"Coco Rayado", "50", "10"});
        modelo.addRow(new Object[]{"Aceite de Coco", "20", "5"});

        tablaInventario = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tablaInventario);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Panel Inferior: Eliminar y Guardar
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnEliminar = new JButton("Eliminar Seleccionado");
        btnGuardar = new JButton("Guardar Cambios (Local)");
        
        panelInferior.add(btnEliminar);
        panelInferior.add(btnGuardar);
        add(panelInferior, BorderLayout.SOUTH);

        // --- LÓGICA DE LOS BOTONES ---

        // Agregar fila vacía (Simulación de "Nuevo")
        btnNuevo.addActionListener(e -> {
            modelo.addRow(new Object[]{"Nuevo item", "0", "0"});
        });

        // Eliminar fila seleccionada
        btnEliminar.addActionListener(e -> {
            int filaSeleccionada = tablaInventario.getSelectedRow();
            if (filaSeleccionada >= 0) {
                modelo.removeRow(filaSeleccionada);
            } else {
                JOptionPane.showMessageDialog(null, "Por favor, selecciona una fila para eliminar.");
            }
        });

        // Guardar cambios (Simulación de persistencia)
        btnGuardar.addActionListener(e -> {
            // Aquí en el futuro irá la conexión a la DB
            JOptionPane.showMessageDialog(null, "¡Cambios guardados localmente en el modelo!");
        });
    }
}
