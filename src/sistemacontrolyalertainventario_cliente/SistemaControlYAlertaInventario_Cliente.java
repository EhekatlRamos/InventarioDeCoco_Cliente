/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package sistemacontrolyalertainventario_cliente;

import UI.LogIn;

/**
 *
 * @author kira
 */
public class SistemaControlYAlertaInventario_Cliente {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            // CAMBIO: Iniciar con LogIn, no con Inventario
            new UI.LogIn().setVisible(true); 
        });
    }
}
