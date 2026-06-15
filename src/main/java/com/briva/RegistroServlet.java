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

public class RegistroServlet extends HttpServlet {

    private static final String DB_URL = "jdbc:postgresql://thomas.proxy.rlwy.net:18148/railway";    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "HhbimfZDyhOLItCzMjsxwBFbjSZumdcD";
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmar = request.getParameter("confirmar");

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (!password.equals(confirmar)) {
            mostrarMensaje(out, "Las contrasenas no coinciden.", false);
            return;
        }

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            String sqlCheck = "SELECT * FROM usuarios WHERE username=?";
            PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
            psCheck.setString(1, username);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                mostrarMensaje(out, "El usuario ya existe, elige otro.", false);
            } else {
                String sqlInsert = "INSERT INTO usuarios (username, password) VALUES (?, ?)";
                PreparedStatement psInsert = conn.prepareStatement(sqlInsert);
                psInsert.setString(1, username);
                psInsert.setString(2, password);
                psInsert.executeUpdate();
                mostrarMensaje(out, "Cuenta creada exitosamente! Ya puedes iniciar sesion.", true);
            }
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensaje(out, "Error al conectar con la base de datos.", false);
        }
    }

    private void mostrarMensaje(PrintWriter out, String mensaje, boolean exito) {
        String color = exito ? "green" : "red";
        String boton = exito
                ? "<button onclick=\"window.location.href='login.html'\">Ir al login</button>"
                : "<button onclick=\"history.back()\">Volver</button>";
        out.println("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>Briva</title>");
        out.println("<style>* { margin:0; padding:0; box-sizing:border-box; } body { font-family:'Segoe UI',sans-serif; background:#fff; } header { text-align:center; padding:30px; border-bottom:1px solid #ddd; letter-spacing:6px; font-size:2rem; font-weight:bold; } .main { display:flex; justify-content:center; align-items:center; padding:80px; } .container { text-align:center; } p { font-size:1rem; margin-bottom:25px; color:" + color + "; } button { padding:12px 28px; background:#2c2c2c; color:white; border:none; border-radius:4px; font-size:0.9rem; cursor:pointer; }</style></head><body>");
        out.println("<header>BRIVA</header>");
        out.println("<div class='main'><div class='container'>");
        out.println("<p>" + mensaje + "</p>");
        out.println(boton);
        out.println("</div></div></body></html>");
    }
}