// src/main/java/com/admineasy/security/AuthTokenFilter.java
package com.admineasy.security;

import com.admineasy.model.User;
import com.admineasy.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {
        String headerAuth = request.getHeader("Authorization");

        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String jwt = headerAuth.substring(7);
            try {
                if (jwtUtils.validateJwtToken(jwt)) {
                    Long userId = jwtUtils.getUserIdFromJwt(jwt);
                    User user = userRepo.findById(userId).orElse(null);
                    if (user != null) {
                        // Crée un Authentication Spring avec principal = l’utilisateur
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user, null, null /* on gérera les rôles plus tard si on veut */);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (JwtException e) {
                // Token invalide, on peut logguer… et laisser passer sans auth
            }
        }
        filterChain.doFilter(request, response);
    }
}
