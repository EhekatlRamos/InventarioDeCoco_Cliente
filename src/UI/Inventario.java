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
    private JButton btnNuevo, btnEliminar, btnGuardar;
    private conexion.ClienteSocket socket; // El socket que recibimos del Login

    public Inventario(conexion.ClienteSocket socket) {
        this.socket = socket;
        
        setTitle("Gestión de Inventario - RF07");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        //Panel Superior
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnNuevo = new JButton("Nuevo +");
        panelSuperior.add(new JLabel("Inventario de Productos    "));
        panelSuperior.add(btnNuevo);
        add(panelSuperior, BorderLayout.NORTH);

        //listado
        String[] columnas = {"ID", "Nombre", "Cantidad Actual", "Umbral Mínimo", "Precio"};
        modelo = new DefaultTableModel(columnas, 0); 
        tablaInventario = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tablaInventario);
        add(scrollPane, BorderLayout.CENTER);

        //Panel Inferior
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnEliminar = new JButton("Eliminar Seleccionado");
        btnGuardar = new JButton("Guardar Cambios");
        panelInferior.add(btnEliminar);
        panelInferior.add(btnGuardar);
        add(panelInferior, BorderLayout.SOUTH);

        // --- LÓGICA ---
        cargarDatosDesdeServidor();

        // Eliminar fila
        btnEliminar.addActionListener(e -> {
            int fila = tablaInventario.getSelectedRow();
            if (fila >= 0) modelo.removeRow(fila);
            else JOptionPane.showMessageDialog(null, "Selecciona una fila.");
        });
    }

    private void cargarDatosDesdeServidor() {
        // método en ClienteSocket
        String respuesta = socket.solicitarInventario();
        
        if (respuesta != null && !respuesta.isEmpty()) {
            modelo.setRowCount(0); // Limpiamos la tabla
            String[] productos = respuesta.split(";");
            for (String p : productos) {
                String[] datos = p.split(",");
                modelo.addRow(datos); // Llenamos la tabla con datos reales
            }
        } else {
            JOptionPane.showMessageDialog(this, "Aviso: No hay productos o el servidor no respondió.");
        }
    }
}