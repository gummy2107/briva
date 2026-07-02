package com.briva;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

public class PagoServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:postgresql://thomas.proxy.rlwy.net:18148/railway";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "HhbimfZDyhOLItCzMjsxwBFbjSZumdcD";

    private Connection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        Map<Integer, int[]> carrito = (Map<Integer, int[]>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            response.sendRedirect("CatalogoServlet");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        double total = 0;
        try {
            Connection conn = getConnection();
            for (Map.Entry<Integer, int[]> entry : carrito.entrySet()) {
                PreparedStatement ps = conn.prepareStatement("SELECT precio FROM productos WHERE id=?");
                ps.setInt(1, entry.getKey());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) total += rs.getDouble("precio") * entry.getValue()[0];
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        out.println("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>Briva - Pago</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f5f0eb; color:#2c2c2c; }");
        out.println("header { background:#2c2c2c; color:white; text-align:center; padding:20px; letter-spacing:6px; font-size:1.8rem; }");
        out.println("nav { background:#444; padding:10px 30px; }");
        out.println("nav a { color:white; text-decoration:none; font-size:0.9rem; }");
        out.println(".contenido { padding:30px; max-width:600px; margin:0 auto; }");
        out.println("h2 { letter-spacing:2px; font-size:1.1rem; margin-bottom:20px; }");
        out.println(".card { background:white; padding:30px; border-radius:8px; box-shadow:0 2px 10px rgba(0,0,0,0.1); margin-bottom:20px; }");
        out.println("label { display:block; font-size:0.85rem; color:#555; margin-bottom:5px; margin-top:15px; }");
        out.println("input { width:100%; padding:10px; border:1px solid #ccc; border-radius:4px; font-size:0.95rem; }");
        out.println(".total { font-size:1.3rem; font-weight:bold; margin:20px 0; color:#2c2c2c; }");
        out.println(".metodos { display:flex; gap:10px; margin-top:10px; }");
        out.println(".metodo { flex:1; border:2px solid #ddd; border-radius:8px; padding:15px; text-align:center; cursor:pointer; font-size:0.85rem; }");
        out.println(".metodo.seleccionado { border-color:#2c2c2c; background:#f5f0eb; }");
        out.println("button { width:100%; margin-top:20px; padding:15px; background:#2c2c2c; color:white; border:none; border-radius:4px; font-size:1rem; cursor:pointer; letter-spacing:2px; }");
        out.println("button:hover { background:#444; }");
        out.println("</style></head><body>");
        out.println("<header>BRIVA</header>");
        out.println("<nav><a href='CarritoServlet'>← Volver al carrito</a></nav>");
        out.println("<div class='contenido'><h2>PAGO SIMULADO</h2>");
        out.println("<div class='card'>");
        out.println("<p class='total'>Total a pagar: S/ " + String.format("%.2f", total) + "</p>");
        out.println("<form action='PagoServlet' method='post'>");

        out.println("<h3 style='letter-spacing:2px;margin-bottom:15px;font-size:0.95rem;'>DATOS DE TARJETA</h3>");
        out.println("<label>Nombre en la tarjeta:</label>");
        out.println("<input type='text' name='nombre' placeholder='Como aparece en tu tarjeta' required/>");
        out.println("<label>Numero de tarjeta:</label>");
        out.println("<input type='text' name='tarjeta' placeholder='1234 5678 9012 3456' maxlength='19' required/>");
        out.println("<div style='display:flex;gap:15px;'>");
        out.println("<div style='flex:1;'><label>Fecha de vencimiento:</label>");
        out.println("<input type='text' name='vencimiento' placeholder='MM/AA' maxlength='5' required/></div>");
        out.println("<div style='flex:1;'><label>CVV:</label>");
        out.println("<input type='text' name='cvv' placeholder='123' maxlength='3' required/></div>");
        out.println("</div>");

        out.println("<h3 style='letter-spacing:2px;margin:20px 0 15px;font-size:0.95rem;'>DIRECCION DE ENVIO</h3>");
        out.println("<label>Nombre completo:</label>");
        out.println("<input type='text' name='nombreEnvio' placeholder='Tu nombre completo' required/>");
        out.println("<label>Direccion:</label>");
        out.println("<input type='text' name='direccion' placeholder='Calle, numero, distrito' required/>");
        out.println("<label>Ciudad:</label>");
        out.println("<input type='text' name='ciudad' placeholder='Lima' required/>");
        out.println("<label>Telefono:</label>");
        out.println("<input type='text' name='telefono' placeholder='+51 999 999 999' required/>");

        out.println("<input type='hidden' name='metodo' value='Tarjeta'/>");
        out.println("<button type='submit'>CONFIRMAR PAGO</button>");
        out.println("</form></div></div></div></body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        Map<Integer, int[]> carrito = (Map<Integer, int[]>) session.getAttribute("carrito");
        int usuarioId = (int) session.getAttribute("usuarioId");

        try {
            Connection conn = getConnection();

            double total = 0;
            for (Map.Entry<Integer, int[]> entry : carrito.entrySet()) {
                PreparedStatement ps = conn.prepareStatement("SELECT precio FROM productos WHERE id=?");
                ps.setInt(1, entry.getKey());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) total += rs.getDouble("precio") * entry.getValue()[0];
            }

            PreparedStatement psPedido = conn.prepareStatement(
                    "INSERT INTO pedidos (usuario_id, total) VALUES (?,?) RETURNING id");
            psPedido.setInt(1, usuarioId);
            psPedido.setDouble(2, total);
            ResultSet rsPedido = psPedido.executeQuery();
            rsPedido.next();
            int pedidoId = rsPedido.getInt("id");

            for (Map.Entry<Integer, int[]> entry : carrito.entrySet()) {
                PreparedStatement psP = conn.prepareStatement("SELECT precio FROM productos WHERE id=?");
                psP.setInt(1, entry.getKey());
                ResultSet rsP = psP.executeQuery();
                if (rsP.next()) {
                    PreparedStatement psD = conn.prepareStatement(
                            "INSERT INTO pedido_detalle (pedido_id, producto_id, cantidad, precio_unitario) VALUES (?,?,?,?)");
                    psD.setInt(1, pedidoId);
                    psD.setInt(2, entry.getKey());
                    psD.setInt(3, entry.getValue()[0]);
                    psD.setDouble(4, rsP.getDouble("precio"));
                    psD.executeUpdate();

                    PreparedStatement psStock = conn.prepareStatement(
                            "UPDATE productos SET stock = stock - ? WHERE id=?");
                    psStock.setInt(1, entry.getValue()[0]);
                    psStock.setInt(2, entry.getKey());
                    psStock.executeUpdate();
                }
            }

            conn.close();
            session.removeAttribute("carrito");
            response.sendRedirect("ConfirmacionServlet?pedidoId=" + pedidoId + "&total=" + String.format("%.2f", total));

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("CarritoServlet");
        }
    }
}