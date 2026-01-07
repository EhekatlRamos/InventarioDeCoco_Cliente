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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogIn extends JFrame {
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
        setLayout(new GridLayout(3, 2, 10, 10));

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
        
        
        btnLogin.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                new Inventario().setVisible(true);
                dispose();
            }
        });
    }
}
