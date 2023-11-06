package com.example.stock.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
//한 요청당 한번 필터 실행
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    //토큰은 Http프로토콜의 헤더에 포함이 되는데 어떤 키를 기준으로 토큰을 주고 받을지에 대한 키 값
    public static final String TOKEN_HEADER = "Authorization";
    //인증타입을 나타내기 위해 사용, JWP를 사용하는 경우 토큰 앞에 Bearer을 붙힌다
    public static final String TOKEN_PREFIX = "Bearer";
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = this.resolveTokenFromRequest(request);

        if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
            //토큰 유효성 검증됨
            Authentication auth = this.tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            //어떤 사용자가 어떤 경로로 접근했는지 알려주는 로그
            log.info(String.format("[%s] -> %s", this.tokenProvider.getUserName(token), request.getRequestURI()));
        }

        //스프링의 필터에는 필터체인이라는 개념이 있다.
        filterChain.doFilter(request, response);
    }

    //request의 헤더로부터 토큰을 꺼내오는 메소드
    private String resolveTokenFromRequest(HttpServletRequest request) {
        //이 키에 해당하는 헤더의 Value가 나옴
        String token = request.getHeader(TOKEN_HEADER);

        //토큰이 비어있지 않고 토큰이 TOKEN_PREFIX로 시작을 하면
        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            //유효한토큰인지는 아직 모르지만 정상적인 형태의 토큰이다
            //TOKEN_PREFIX를 제외한 나머지 토큰 부분을 반환
            return token.substring(TOKEN_PREFIX.length());
        }
        return null; //정상적인 형태가 아니면 null 반환
    }
}
