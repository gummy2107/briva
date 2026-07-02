package com.briva;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public class ConfirmacionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        String pedidoId = request.getParameter("pedidoId");
        String total = request.getParameter("total");
        String username = (String) session.getAttribute("username");

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><title>Briva - Confirmacion</title>");
        out.println("<style>");
        out.println("* { margin:0; padding:0; box-sizing:border-box; }");
        out.println("body { font-family:'Segoe UI',sans-serif; background:#f5f0eb; color:#2c2c2c; }");
        out.println("header { background:#2c2c2c; color:white; text-align:center; padding:20px; letter-spacing:6px; font-size:1.8rem; }");
        out.println(".contenido { display:flex; justify-content:center; align-items:center; min-height:80vh; }");
        out.println(".card { background:white; padding:50px 60px; border-radius:12px; box-shadow:0 4px 20px rgba(0,0,0,0.1); text-align:center; max-width:500px; }");
        out.println(".check { font-size:4rem; margin-bottom:20px; }");
        out.println("h2 { letter-spacing:3px; font-size:1.3rem; margin-bottom:10px; }");
        out.println("p { color:#555; font-size:0.95rem; margin-bottom:8px; }");
        out.println(".pedido-num { font-size:1.1rem; font-weight:bold; color:#2c2c2c; margin:15px 0; }");
        out.println(".total { font-size:1.3rem; font-weight:bold; margin:10px 0 25px 0; }");
        out.println(".btns { display:flex; gap:15px; justify-content:center; margin-top:10px; }");
        out.println(".btn { padding:12px 25px; border-radius:4px; text-decoration:none; font-size:0.9rem; letter-spacing:1px; }");
        out.println(".btn-dark { background:#2c2c2c; color:white; }");
        out.println(".btn-light { background:#f5f0eb; color:#2c2c2c; border:1px solid #ddd; }");
        out.println("</style></head><body>");
        out.println("<header>BRIVA</header>");
        out.println("<div class='contenido'><div class='card'>");
        out.println("<div class='check'>✅</div>");
        out.println("<h2>PAGO EXITOSO</h2>");
        out.println("<p>Gracias por tu compra, <strong>" + username + "</strong></p>");
        out.println("<div class='pedido-num'>Pedido #" + pedidoId + "</div>");
        out.println("<div class='total'>Total pagado: S/ " + total + "</div>");
        out.println("<p style='font-size:0.85rem;color:#888;'>Tu pedido ha sido registrado exitosamente.</p>");
        out.println("<div class='btns'>");
        out.println("<a href='CatalogoServlet' class='btn btn-dark'>Seguir comprando</a>");
        out.println("<a href='login.html' class='btn btn-light'>Cerrar sesion</a>");
        out.println("</div></div></div></body></html>");
    }
}