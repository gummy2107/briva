package com.briva;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class ProductoServlet extends HttpServlet {

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
        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";

        switch (accion) {
            case "listar" -> listar(request, response);
            case "formAgregar" -> mostrarFormAgregar(response);
            case "formEditar" -> mostrarFormEditar(request, response);
            case "eliminar" -> eliminar(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        if ("agregar".equals(accion)) {
            agregar(request, response);
        } else if ("editar".equals(accion)) {
            editar(request, response);
        }
    }

    private void listar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        printHeader(out, "Inventario");

        out.println("<div class='contenido'>");
        out.println("<div class='top'><h2>INVENTARIO</h2>");
        out.println("<a href='ProductoServlet?accion=formAgregar' class='btn-add'>+ Agregar producto</a></div>");

        out.println("<table>");
        out.println("<tr><th>ID</th><th>Nombre</th><th>Talla</th><th>Color</th><th>Precio</th><th>Stock</th><th>Acciones</th></tr>");

        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM productos ORDER BY id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + rs.getString("nombre") + "</td>");
                out.println("<td>" + rs.getString("talla") + "</td>");
                out.println("<td>" + rs.getString("color") + "</td>");
                out.println("<td>S/ " + rs.getDouble("precio") + "</td>");
                out.println("<td>" + rs.getInt("stock") + "</td>");
                out.println("<td>");
                out.println("<a href='ProductoServlet?accion=formEditar&id=" + id + "' class='btn-edit'>Editar</a> ");
                out.println("<a href='ProductoServlet?accion=eliminar&id=" + id + "' class='btn-delete' onclick='return confirm(\"¿Eliminar?\")'>Eliminar</a>");
                out.println("</td></tr>");
            }
            conn.close();
        } catch (Exception e) {
            out.println("<tr><td colspan='7'>Error: " + e.getMessage() + "</td></tr>");
        }

        out.println("</table></div>");
        printFooter(out);
    }

    private void mostrarFormAgregar(HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        printHeader(out, "Agregar Producto");
        out.println("<div class='contenido'>");
        out.println("<h2>AGREGAR PRODUCTO</h2>");
        out.println("<form action='ProductoServlet' method='post'>");
        out.println("<input type='hidden' name='accion' value='agregar'/>");
        printCampos(out, "", "", "", "", "");
        out.println("<button type='submit'>Guardar</button> ");
        out.println("<a href='ProductoServlet' class='btn-cancel'>Cancelar</a>");
        out.println("</form></div>");
        printFooter(out);
    }

    private void mostrarFormEditar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        printHeader(out, "Editar Producto");
        String id = request.getParameter("id");
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM productos WHERE id=?");
            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                out.println("<div class='contenido'>");
                out.println("<h2>EDITAR PRODUCTO</h2>");
                out.println("<form action='ProductoServlet' method='post'>");
                out.println("<input type='hidden' name='accion' value='editar'/>");
                out.println("<input type='hidden' name='id' value='" + id + "'/>");
                printCampos(out, rs.getString("nombre"), rs.getString("talla"),
                        rs.getString("color"), String.valueOf(rs.getDouble("precio")),
                        String.valueOf(rs.getInt("stock")));
                out.println("<button type='submit'>Guardar</button> ");
                out.println("<a href='ProductoServlet' class='btn-cancel'>Cancelar</a>");
                out.println("</form></div>");
            }
            conn.close();
        } catch (Exception e) {
            out.println("<p>Error: " + e.getMessage() + "</p>");
        }
        printFooter(out);
    }

    private void agregar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO productos (nombre, talla, color, precio, stock) VALUES (?,?,?,?,?)");
            ps.setString(1, request.getParameter("nombre"));
            ps.setString(2, request.getParameter("talla"));
            ps.setString(3, request.getParameter("color"));
            ps.setDouble(4, Double.parseDouble(request.getParameter("precio")));
            ps.setInt(5, Integer.parseInt(request.getParameter("stock")));
            ps.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect("ProductoServlet");
    }

    private void editar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE productos SET nombre=?, talla=?, color=?, precio=?, stock=? WHERE id=?");
            ps.setString(1, request.getParameter("nombre"));
            ps.setString(2, request.getParameter("talla"));
            ps.setString(3, request.getParameter("color"));
            ps.setDouble(4, Double.parseDouble(request.getParameter("precio")));
            ps.setInt(5, Integer.parseInt(request.getParameter("stock")));
            ps.setInt(6, Integer.parseInt(request.getParameter("id")));
            ps.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect("ProductoServlet");
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM productos WHERE id=?");
            ps.setInt(1, Integer.parseInt(request.getParameter("id")));
            ps.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        response.sendRedirect("ProductoServlet");
    }

    private void printCampos(PrintWriter out, String nombre, String talla, String color, String precio, String stock) {
        out.println("<label>Nombre:</label><input type='text' name='nombre' value='" + nombre + "' required/>");
        out.println("<label>Talla:</label><select name='talla'>");
        for (String t : new String[]{"XS","S","M","L","XL","XXL"}) {
            out.println("<option value='" + t + "'" + (t.equals(talla) ? " selected" : "") + ">" + t + "</option>");
        }
        out.println("</select>");
        out.println("<label>Color:</label><input type='text' name='color' value='" + color + "' required/>");
        out.println("<label>Precio (S/):</label><input type='number' name='precio' step='0.01' value='" + precio + "' required/>");
        out.println("<label>Stock:</label><input type='number' name='stock' value='" + stock + "' required/>");
    }

    private void printHeader(PrintWriter out, String titulo) {
        out.println("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>Briva - " + titulo + "</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f5f0eb; color:#2c2c2c; }");
        out.println("header { background:#2c2c2c; color:white; text-align:center; padding:20px; letter-spacing:6px; font-size:1.8rem; }");
        out.println("nav { background:#444; padding:10px 30px; display:flex; gap:20px; }");
        out.println("nav a { color:white; text-decoration:none; font-size:0.9rem; letter-spacing:1px; }");
        out.println("nav a:hover { text-decoration:underline; }");
        out.println(".contenido { padding:30px; }");
        out.println(".top { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; }");
        out.println("h2 { letter-spacing:2px; font-size:1.1rem; }");
        out.println("table { width:100%; border-collapse:collapse; background:white; border-radius:8px; overflow:hidden; box-shadow:0 2px 10px rgba(0,0,0,0.1); }");
        out.println("th { background:#2c2c2c; color:white; padding:12px; text-align:left; font-size:0.85rem; letter-spacing:1px; }");
        out.println("td { padding:12px; border-bottom:1px solid #eee; font-size:0.9rem; }");
        out.println("tr:hover { background:#faf7f4; }");
        out.println(".btn-add { background:#2c2c2c; color:white; padding:10px 20px; text-decoration:none; border-radius:4px; font-size:0.85rem; }");
        out.println(".btn-edit { background:#555; color:white; padding:5px 12px; text-decoration:none; border-radius:4px; font-size:0.8rem; }");
        out.println(".btn-delete { background:#c0392b; color:white; padding:5px 12px; text-decoration:none; border-radius:4px; font-size:0.8rem; }");
        out.println(".btn-cancel { background:#888; color:white; padding:10px 20px; text-decoration:none; border-radius:4px; font-size:0.85rem; }");
        out.println("form { background:white; padding:30px; border-radius:8px; max-width:500px; box-shadow:0 2px 10px rgba(0,0,0,0.1); }");
        out.println("label { display:block; font-size:0.85rem; color:#555; margin-bottom:5px; margin-top:15px; }");
        out.println("input, select { width:100%; padding:10px; border:1px solid #ccc; border-radius:4px; font-size:0.95rem; }");
        out.println("button { margin-top:20px; padding:10px 25px; background:#2c2c2c; color:white; border:none; border-radius:4px; cursor:pointer; font-size:0.9rem; }");
        out.println("</style></head><body>");
        out.println("<header>BRIVA</header>");
        out.println("<nav>");
        out.println("<a href='ProductoServlet'>Inventario</a>");
        out.println("<a href='login.html'>Cerrar sesion</a>");
        out.println("</nav>");
    }

    private void printFooter(PrintWriter out) {
        out.println("</body></html>");
    }
}