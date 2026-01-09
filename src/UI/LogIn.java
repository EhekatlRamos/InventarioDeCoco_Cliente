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

public class LogIn extends JFrame {
    private JTextField txtIP;
    private JTextField txtPuerto;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnIrARegistro;

    public LogIn() {
        setTitle("Inicio de Sesión - Sistema de Inventario");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2, 10, 10));

        add(new JLabel("  Dirección IP:"));
        txtIP = new JTextField("localhost");
        add(txtIP);

        add(new JLabel("  Puerto Servidor:"));
        txtPuerto = new JTextField("5000");
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

        btnLogin.addActionListener(e -> {
            String ip = txtIP.getText();
            String puertoStr = txtPuerto.getText();
            String user = txtUsuario.getText();
            String pass = new String(txtPassword.getPassword());

            if (ip.isEmpty() || puertoStr.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.");
                return;
            }

            int puerto = Integer.parseInt(puertoStr);

            new Thread(() -> {
                ClienteSocket cliente = new ClienteSocket();
                
                if (cliente.conectar(ip, puerto)) { // Intenta la conexión
                    if (cliente.enviarLogin(user, pass)) { // Intenta el login
                        SwingUtilities.invokeLater(() -> {
                            Inventario inv = new Inventario(cliente); 
                            inv.setVisible(true);

                            new conexion.EscuchadorAlertas(ip, puerto + 1, mensaje -> {
                                inv.agregarAlerta(mensaje);
                            }).start();
                            dispose();
                        });
                        
                    } else {
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos."));
                    }
                } else {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor."));
                }
            }).start();
        });
        
        btnIrARegistro.addActionListener(e -> {
            new SignIn().setVisible(true);
            dispose();
        });
    }
}
