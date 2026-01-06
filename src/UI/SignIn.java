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

public class SignIn extends JFrame {
    private JTextField txtNombre;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JButton btnRegistrar;
    private JButton btnVolver;

    public SignIn() {
        setTitle("Registro de Usuario");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2, 10, 10));

        // Componentes
        add(new JLabel("  Nombre Completo:"));
        txtNombre = new JTextField();
        add(txtNombre);

        add(new JLabel("  Usuario:"));
        txtUsuario = new JTextField();
        add(txtUsuario);

        add(new JLabel("  Contraseña:"));
        txtPassword = new JPasswordField();
        add(txtPassword);

        add(new JLabel("  Confirmar Contraseña:"));
        txtConfirmPassword = new JPasswordField();
        add(txtConfirmPassword);

        btnRegistrar = new JButton("Crear Cuenta");
        btnVolver = new JButton("Volver al Login");

        add(btnRegistrar);
        add(btnVolver);

        // Evento para volver al Login
        btnVolver.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LogIn().setVisible(true);
                dispose();
            }
        });
    }
}
