/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexion;

/**
 *
 * @author andre
 */
import java.io.*;
import java.net.Socket;

public class ClienteSocket {
    private Socket socket;
    private DataOutputStream salida;
    private DataInputStream entrada;

    public boolean conectar(String ip, int puerto) {
        try {
            socket = new Socket(ip, puerto);
            salida = new DataOutputStream(socket.getOutputStream());
            entrada = new DataInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            System.out.println("Error al conectar: " + e.getMessage());
            return false;
        }
    }

    public boolean enviarLogin(String usuario, String password) {
        try {
            salida.writeUTF("LOGIN:" + usuario + "|" + password);
            return entrada.readBoolean();
        } catch (IOException e) {
            return false;
        }
    }
}
