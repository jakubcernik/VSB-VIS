package com.example.watchmanagement.interceptor;

import com.example.watchmanagement.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        String uri = request.getRequestURI();

        // Povolena URL bez autentifikace
        if (uri.startsWith("/home") || uri.startsWith("/login") || uri.startsWith("/register") ||
                uri.startsWith("/css/") || uri.startsWith("/js/") ||
                uri.startsWith("/images/")) {
            return true;
        }

        // Kontrola přihlášení
        if (loggedInUser != null) {
            return true;
        }

        // Pokud není přihlášen, přesměruj na přihlášení
        response.sendRedirect("/login");
        return false;
    }
}
