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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogIn extends JFrame {
    private JTextField txtIP;
    private JTextField txtPuerto;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnIrARegistro;

    public LogIn() {
        // Configuración de la ventana
        setTitle("Inicio de Sesión - Sistema de Inventario");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2, 10, 10)); //5 filas para el puerto y la ip
        
        
        add(new JLabel("  Dirección IP:"));
        txtIP = new JTextField();
        add(txtIP);

        add(new JLabel("  Puerto Servidor:"));
        txtPuerto = new JTextField();
        add(txtPuerto);

        add(new JLabel("  Usuario:"));
        txtUsuario = new JTextField();
        add(txtUsuario);

        add(new JLabel("  Contraseña:"));
        txtPassword = new JPasswordField();
        add(txtPassword);

        btnLogin = new JButton("Entrar");
        btnIrARegistro = new JButton("Registrarse");

        add(btnLogin);
        add(btnIrARegistro);

        btnIrARegistro.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SignIn().setVisible(true);
                dispose();
            }
        });
        
        
        btnLogin.addActionListener(e -> {
        String ip = txtIP.getText();
        int puerto = Integer.parseInt(txtPuerto.getText());
        String user = txtUsuario.getText();
        String pass = new String(txtPassword.getPassword());

        ClienteSocket cliente = new ClienteSocket();
        if(txtIP.getText().isEmpty() || txtPuerto.getText().isEmpty()){
        JOptionPane.showMessageDialog(this, "Por favor, configure la IP y el Puerto primero.");
        return;
        }
        if (cliente.conectar(ip, puerto)) { // Intenta la conexión
        if (cliente.enviarLogin(user, pass)) {
            new Inventario().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.");
        }
    } else {
        JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor.");
    }
    });
    }
}
