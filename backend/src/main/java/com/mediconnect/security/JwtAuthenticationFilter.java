package com.mediconnect.security;
import com.mediconnect.entity.Role;
import com.mediconnect.entity.User;
import com.mediconnect.repository.UserRepository;
import jakarta.servlet.FilterChain;import jakarta.servlet.ServletException;import jakarta.servlet.http.HttpServletRequest;import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;import org.springframework.security.core.authority.SimpleGrantedAuthority;import org.springframework.security.core.context.SecurityContextHolder;import org.springframework.stereotype.Component;import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;import java.util.List;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt; private final UserRepository users;
    public JwtAuthenticationFilter(JwtService jwt,UserRepository users){this.jwt=jwt;this.users=users;}
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
        String header=req.getHeader("Authorization");
        if(header!=null&&header.startsWith("Bearer ")&&SecurityContextHolder.getContext().getAuthentication()==null){
            try{String token=header.substring(7);Long id=jwt.extractUserId(token);User u=users.findById(id).orElse(null);
                boolean platform=u!=null&&u.getRole()==Role.PLATFORM_SUPER_ADMIN;
                boolean tenantActive=platform||u!=null&&u.getHospital()!=null&&u.getHospital().isActive();
                if(u!=null&&u.isActive()&&u.isApproved()&&tenantActive&&jwt.isValid(token))SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u,null,List.of(new SimpleGrantedAuthority("ROLE_"+u.getRole().name()))));
            }catch(Exception ignored){}
        }
        chain.doFilter(req,res);
    }
}
