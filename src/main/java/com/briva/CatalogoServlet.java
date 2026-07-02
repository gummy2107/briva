package com.briva;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class CatalogoServlet extends HttpServlet {

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

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String username = (String) session.getAttribute("username");

        out.println("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>Briva - Catalogo</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f5f0eb; color:#2c2c2c; }");
        out.println("header { background:#2c2c2c; color:white; text-align:center; padding:20px; letter-spacing:6px; font-size:1.8rem; }");
        out.println("nav { background:#444; padding:10px 30px; display:flex; justify-content:space-between; align-items:center; }");
        out.println("nav a { color:white; text-decoration:none; font-size:0.9rem; letter-spacing:1px; }");
        out.println("nav a:hover { text-decoration:underline; }");
        out.println(".nav-right { display:flex; gap:20px; align-items:center; }");
        out.println(".nav-right span { color:#ddd; font-size:0.85rem; }");
        out.println(".contenido { padding:30px; }");
        out.println("h2 { letter-spacing:2px; font-size:1.1rem; margin-bottom:20px; }");
        out.println(".grid { display:grid; grid-template-columns:repeat(auto-fill, minmax(280px,1fr)); gap:20px; }");
        out.println(".card { background:white; border-radius:8px; padding:20px; box-shadow:0 2px 10px rgba(0,0,0,0.1); }");
        out.println(".card h3 { font-size:1rem; margin-bottom:8px; }");
        out.println(".card p { font-size:0.85rem; color:#777; margin-bottom:4px; }");
        out.println(".card .precio { font-size:1.1rem; font-weight:bold; color:#2c2c2c; margin:10px 0; }");
        out.println(".card form { display:flex; flex-direction:column; gap:8px; }");
        out.println(".card button { padding:8px; background:#2c2c2c; color:white; border:none; border-radius:4px; cursor:pointer; font-size:0.85rem; }");
        out.println(".card button:hover { background:#444; }");
        out.println(".carrito-btn { background:#2c2c2c; color:white; padding:8px 16px; text-decoration:none; border-radius:4px; font-size:0.85rem; }");
        out.println("</style></head><body>");
        out.println("<header>BRIVA</header>");
        out.println("<nav>");
        out.println("<span style='color:white;letter-spacing:2px;font-size:0.85rem;'>CATALOGO</span>");
        out.println("<div class='nav-right'>");
        out.println("<span>Hola, " + username + "</span>");
        out.println("<a href='CarritoServlet' class='carrito-btn'>🛒 Mi carrito</a>");
        out.println("<a href='login.html' style='color:#ddd;'>Cerrar sesion</a>");
        out.println("</div></nav>");
        out.println("<div class='contenido'><h2>NUESTROS PRODUCTOS</h2>");
        out.println("<div class='grid'>");

        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM productos ORDER BY id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int prodId = rs.getInt("id");
                out.println("<div class='card'>");
                out.println("<h3>" + rs.getString("nombre") + "</h3>");
                out.println("<p>Color: " + rs.getString("color") + "</p>");
                out.println("<div class='precio'>S/ " + rs.getDouble("precio") + "</div>");

                PreparedStatement psTallas = conn.prepareStatement(
                        "SELECT talla, stock FROM producto_tallas WHERE producto_id=? AND stock > 0 ORDER BY talla");
                psTallas.setInt(1, prodId);
                ResultSet rsTallas = psTallas.executeQuery();

                out.println("<form action='CarritoServlet' method='post'>");
                out.println("<input type='hidden' name='productoId' value='" + prodId + "'/>");
                out.println("<div style='display:flex;flex-direction:column;gap:8px;'>");
                out.println("<label style='font-size:0.8rem;color:#555;'>Talla:</label>");
                out.println("<select name='talla' style='width:100%;padding:8px;border:1px solid #ccc;border-radius:4px;'>");

                boolean hayStock = false;
                while (rsTallas.next()) {
                    hayStock = true;
                    out.println("<option value='" + rsTallas.getString("talla") + "'>" +
                            rsTallas.getString("talla") + " (Stock: " + rsTallas.getInt("stock") + ")</option>");
                }
                out.println("</select>");

                if (hayStock) {
                    out.println("<div style='display:flex;gap:8px;margin-top:5px;'>");
                    out.println("<input type='number' name='cantidad' value='1' min='1' style='width:60px;padding:6px;border:1px solid #ccc;border-radius:4px;'/>");
                    out.println("<button type='submit'>Agregar al carrito</button>");
                    out.println("</div>");
                } else {
                    out.println("<p style='color:red;font-size:0.85rem;'>Sin stock disponible</p>");
                }
                out.println("</div></form></div>");
            }
            conn.close();
        } catch (Exception e) {
            out.println("<p>Error: " + e.getMessage() + "</p>");
        }

        out.println("</div></div></body></html>");
    }
}