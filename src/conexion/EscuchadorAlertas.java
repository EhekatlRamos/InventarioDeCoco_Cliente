/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexion;

/**
 *
 * @author kira
 */
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.SwingUtilities;
import java.util.function.Consumer;

public class EscuchadorAlertas extends Thread {
    private Socket socket;
    private DataInputStream entrada;
    private Consumer<String> onAlertaRecibida;
    private boolean ejecutando = true;

    public EscuchadorAlertas(String ip, int puerto, Consumer<String> callback) {
        this.onAlertaRecibida = callback;
        try {
            this.socket = new Socket(ip, puerto);
            this.entrada = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Error al conectar socket de alertas: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (ejecutando && socket != null && !socket.isClosed()) {
                String alerta = entrada.readUTF();
                SwingUtilities.invokeLater(() -> onAlertaRecibida.accept(alerta));
            }
        } catch (IOException e) {
            System.out.println("Conexión de alertas cerrada.");
        }
    }

    public void detener() {
        ejecutando = false;
        try { if(socket != null) socket.close(); } catch (IOException e) {}
    }
}
