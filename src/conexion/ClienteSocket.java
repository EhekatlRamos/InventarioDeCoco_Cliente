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
            // Formato según v2.pdf [cite: 30]
            salida.writeUTF("LOGIN:" + usuario + "|" + password);
            return entrada.readBoolean();
        } catch (IOException e) { return false; }
    }
    
    public List<String[]> solicitarInventario() {
        List<String[]> productos = new ArrayList<>();
        try {
            // Comando para productos activos [cite: 35]
            salida.writeUTF("LISTAR_PRODUCTOS:");
            String inicio = entrada.readUTF();
            if (!inicio.startsWith("LISTA_INICIO:")) return productos;
            
            while (true) {
                String linea = entrada.readUTF();
                if (linea.equals("LISTA_FIN:")) break;
                if (linea.startsWith("PRODUCTO:")) {
                    productos.add(linea.substring(9).split("\\|"));
                }
            }
        } catch (IOException e) { System.out.println("Error: " + e.getMessage()); }
        return productos;
    }

    public String obtenerProducto(int id) {
        try {
            salida.writeUTF("OBTENER_PRODUCTO:" + id); // [cite: 54]
            String resp = entrada.readUTF();
            if (resp.startsWith("PRODUCTO:")) {
                String[] p = resp.substring(9).split("\\|");
                return p[1] + " (Stock: " + p[3] + "/" + p[4] + ")"; // Nombre (Cant/Umbral)
            }
        } catch (IOException e) { }
        return "ID: " + id;
    }

    public boolean insertarProducto(String nom, String desc, int cant, int umb, double pre) {
        try {
            // [cite: 71]
            salida.writeUTF("INSERTAR_PRODUCTO:" + nom + "|" + desc + "|" + cant + "|" + umb + "|" + pre);
            return entrada.readUTF().startsWith("OK:");
        } catch (IOException e) { return false; }
    }

    public boolean actualizarCantidad(int id, int cant) {
        try {
            // Formato ejemplo v2.pdf [cite: 230]
            salida.writeUTF("ACTUALIZAR_CANTIDAD:" + id + "/" + cant);
            return entrada.readUTF().startsWith("OK:");
        } catch (IOException e) { return false; }
    }

    public boolean actualizarUmbral(int id, int umb) {
        try {
            // [cite: 61]
            salida.writeUTF("ACTUALIZAR_UMBRAL:" + id + " " + umb);
            return entrada.readUTF().startsWith("OK:");
        } catch (IOException e) { return false; }
    }

    public boolean desactivarProducto(int id) {
        try {
            // Soft delete [cite: 77, 81]
            salida.writeUTF("DESACTIVAR_PRODUCTO:" + id);
            return entrada.readUTF().startsWith("OK:");
        } catch (IOException e) { return false; }
    }
}
