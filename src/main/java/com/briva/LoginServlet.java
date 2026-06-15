package com.briva;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:sqlserver://localhost;databaseName=briva;encrypt=false;trustServerCertificate=true";    private static final String DB_USER = "sa";
    private static final String DB_PASS = "BERE2107";
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            String sql = "SELECT * FROM usuarios WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                response.sendRedirect("bienvenida.html");
            } else {
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>Briva</title>");
                out.println("<style>* { margin:0; padding:0; box-sizing:border-box; } body { font-family:'Segoe UI',sans-serif; background:#fff; color:#2c2c2c; } header { text-align:center; padding:30px; border-bottom:1px solid #ddd; letter-spacing:6px; font-size:2rem; font-weight:bold; } .main { display:flex; justify-content:center; align-items:flex-start; gap:60px; padding:60px 100px; } .left,.right { flex:1; max-width:400px; } h2 { font-size:1.1rem; letter-spacing:2px; margin-bottom:25px; text-transform:uppercase; } label { font-size:0.85rem; display:block; margin-bottom:6px; color:#555; } input { width:100%; padding:10px 12px; border:1px solid #ccc; border-radius:4px; font-size:0.95rem; margin-bottom:18px; outline:none; } button { padding:12px 28px; background:#2c2c2c; color:white; border:none; border-radius:4px; font-size:0.9rem; cursor:pointer; } .divider { width:1px; background:#ddd; align-self:stretch; } .right { background:#f5f5f5; padding:30px; border-radius:6px; } .right ul { margin:15px 0 25px 20px; font-size:0.9rem; color:#555; line-height:2; } .error { color:red; font-size:0.85rem; margin-bottom:15px; }</style></head><body>");
                out.println("<header>BRIVA</header>");
                out.println("<div class='main'><div class='left'><h2>Iniciar Sesion</h2>");
                out.println("<p class='error'>Usuario o contrasena incorrectos.</p>");
                out.println("<form action='LoginServlet' method='post'>");
                out.println("<label>Usuario:</label><input type='text' name='username' placeholder='Tu usuario' required/>");
                out.println("<label>Contrasena:</label><input type='password' name='password' placeholder='Tu contrasena' required/>");
                out.println("<button type='submit'>Iniciar sesion</button></form></div>");
                out.println("<div class='divider'></div>");
                out.println("<div class='right'><h2>Nuevo cliente?</h2><p style='font-size:0.9rem;color:#555;'>Crea una cuenta con nosotros y podras:</p>");
                out.println("<ul><li>Pagar mas rapido</li><li>Guardar direcciones de envio</li><li>Ver historial de pedidos</li><li>Rastrear pedidos</li></ul>");
                out.println("<button onclick=\"window.location.href='registro.html'\">Crear cuenta</button></div></div>");
                out.println("</body></html>");
            }
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}