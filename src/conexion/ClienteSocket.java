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
            salida.writeUTF("LISTAR_PRODUCTOS:"); // O "LISTAR_TODOS:" según necesidad 
            String inicio = entrada.readUTF();
            if (!inicio.startsWith("LISTA_INICIO:")) return productos;
            while (true) {
                String linea = entrada.readUTF();
                if (linea.equals("LISTA_FIN:")) break;
                if (linea.startsWith("PRODUCTO:")) {
                    productos.add(linea.substring(9).split("\\|"));
                }
            }
        } catch (IOException e) { }
        return productos;
    }

    public synchronized boolean guardarCambios(java.util.List<Object[]> listaFilas) {
        try {
            // Protocolo de envío masivo 
            salida.writeUTF("LISTA_INICIO:"); 

            for (Object[] fila : listaFilas) {
                // Formato: id|nombre|descripcion|cantidad|umbral|precio|vigencia
                String msg = String.format("PRODUCTO:%s|%s|%s|%s|%s|%s|%s",
                        fila[0], fila[1], fila[2], fila[3], fila[4], fila[5], fila[7]);
                salida.writeUTF(msg);
            }
            
            salida.writeUTF("LISTA_FIN:");
            return entrada.readBoolean(); // El servidor confirma la transacción
        } catch (IOException e) {
            return false;
        }
    }
    
    public String[] obtenerDetallesProducto(int id) {
        try {
            salida.writeUTF("OBTENER_PRODUCTO:" + id); // Solicita detalles según protocolo 
            String resp = entrada.readUTF();
            if (resp.startsWith("PRODUCTO:")) {
                return resp.substring(9).split("\\|"); // Extrae los datos [cite: 145]
            }
        } catch (IOException e) {}
        return null;
    }
}
