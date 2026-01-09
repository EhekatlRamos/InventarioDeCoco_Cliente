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
import java.util.ArrayList;
import java.util.List;

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
    
    public List<String[]> solicitarInventario() {
        List<String[]> productos = new ArrayList<>();
        try {
            salida.writeUTF("GET_LISTADO");
            String inicio = entrada.readUTF();
            if (!inicio.equals("LISTA_INICIO:")) return productos;
            while (true) {
                String linea = entrada.readUTF();
                if (linea.equals("LISTA_FIN:")) break;
                if (linea.startsWith("PRODUCTO:")) {
                    String datosCrudos = linea.substring(9); 
                    String[] datos = datosCrudos.split("\\|");
                    productos.add(datos);
                }
            }
        } catch (IOException e) {
            System.out.println("Error al recibir inventario: " + e.getMessage());
        }
        return productos;
    }
    
}
