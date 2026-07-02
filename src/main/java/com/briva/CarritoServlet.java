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

public class CarritoServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:postgresql://thomas.proxy.rlwy.net:18148/railway";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "HhbimfZDyhOLItCzMjsxwBFbjSZumdcD";

    private Connection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        if (session.getAttribute("usuarioId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        // Carrito: Map<"productoId_talla", [cantidad, productoId]>
        Map<String, int[]> carrito = (Map<String, int[]>) session.getAttribute("carrito");
        if (carrito == null) carrito = new LinkedHashMap<>();

        String productoId = request.getParameter("productoId");
        String talla = request.getParameter("talla");
        int cantidad = Integer.parseInt(request.getParameter("cantidad"));
        String key = productoId + "_" + talla;

        if (carrito.containsKey(key)) {
            carrito.get(key)[0] += cantidad;
        } else {
            carrito.put(key, new int[]{cantidad, Integer.parseInt(productoId)});
        }

        session.setAttribute("carrito", carrito);
        response.sendRedirect("CarritoServlet");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        String accion = request.getParameter("accion");
        if ("eliminar".equals(accion)) {
            Map<String, int[]> carrito = (Map<String, int[]>) session.getAttribute("carrito");
            if (carrito != null) {
                carrito.remove(request.getParameter("key"));
                session.setAttribute("carrito", carrito);
            }
            response.sendRedirect("CarritoServlet");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, int[]> carrito = (Map<String, int[]>) session.getAttribute("carrito");

        out.println("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>Briva - Carrito</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f5f0eb; color:#2c2c2c; }");
        out.println("header { background:#2c2c2c; color:white; text-align:center; padding:20px; letter-spacing:6px; font-size:1.8rem; }");
        out.println("nav { background:#444; padding:10px 30px; display:flex; justify-content:space-between; }");
        out.println("nav a { color:white; text-decoration:none; font-size:0.9rem; }");
        out.println(".contenido { padding:30px; max-width:800px; margin:0 auto; }");
        out.println("h2 { letter-spacing:2px; font-size:1.1rem; margin-bottom:20px; }");
        out.println("table { width:100%; border-collapse:collapse; background:white; border-radius:8px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.1); margin-bottom:20px; }");
        out.println("th { background:#2c2c2c; color:white; padding:12px; text-align:left; font-size:0.85rem; }");
        out.println("td { padding:12px; border-bottom:1px solid #eee; font-size:0.9rem; }");
        out.println(".total { background:white; padding:20px; border-radius:8px; text-align:right; box-shadow:0 2px 10px rgba(0,0,0,0.1); margin-bottom:20px; }");
        out.println(".total h3 { font-size:1.2rem; }");
        out.println(".btn-pagar { display:block; width:100%; padding:15px; background:#2c2c2c; color:white; border:none; border-radius:4px; font-size:1rem; cursor:pointer; letter-spacing:2px; text-align:center; text-decoration:none; }");
        out.println(".btn-pagar:hover { background:#444; }");
        out.println(".btn-delete { background:#c0392b; color:white; padding:5px 10px; text-decoration:none; border-radius:4px; font-size:0.8rem; }");
        out.println("</style></head><body>");
        out.println("<header>BRIVA</header>");
        out.println("<nav>");
        out.println("<a href='CatalogoServlet'>← Seguir comprando</a>");
        out.println("<a href='login.html'>Cerrar sesion</a>");
        out.println("</nav>");
        out.println("<div class='contenido'><h2>MI CARRITO</h2>");

        if (carrito == null || carrito.isEmpty()) {
            out.println("<p style='background:white;padding:20px;border-radius:8px;'>Tu carrito esta vacio. <a href='CatalogoServlet'>Ver productos</a></p>");
        } else {
            out.println("<table><tr><th>Producto</th><th>Color</th><th>Talla</th><th>Precio</th><th>Cantidad</th><th>Subtotal</th><th></th></tr>");
            double total = 0;
            try {
                Connection conn = getConnection();
                for (Map.Entry<String, int[]> entry : carrito.entrySet()) {
                    String key = entry.getKey();
                    String[] parts = key.split("_");
                    int prodId = Integer.parseInt(parts[0]);
                    String talla = parts[1];
                    int cantidad = entry.getValue()[0];

                    PreparedStatement ps = conn.prepareStatement("SELECT * FROM productos WHERE id=?");
                    ps.setInt(1, prodId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        double precio = rs.getDouble("precio");
                        double subtotal = precio * cantidad;
                        total += subtotal;
                        out.println("<tr>");
                        out.println("<td>" + rs.getString("nombre") + "</td>");
                        out.println("<td>" + rs.getString("color") + "</td>");
                        out.println("<td>" + talla + "</td>");
                        out.println("<td>S/ " + precio + "</td>");
                        out.println("<td>" + cantidad + "</td>");
                        out.println("<td>S/ " + String.format("%.2f", subtotal) + "</td>");
                        out.println("<td><a href='CarritoServlet?accion=eliminar&key=" + key + "' class='btn-delete'>Quitar</a></td>");
                        out.println("</tr>");
                    }
                }
                conn.close();
            } catch (Exception e) {
                out.println("<tr><td colspan='7'>Error: " + e.getMessage() + "</td></tr>");
            }
            out.println("</table>");
            out.println("<div class='total'><h3>Total: S/ " + String.format("%.2f", total) + "</h3></div>");
            out.println("<a href='PagoServlet' class='btn-pagar'>PROCEDER AL PAGO</a>");
        }

        out.println("</div></body></html>");
    }
}