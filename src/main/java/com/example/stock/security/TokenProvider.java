package com.example.stock.security;

import com.example.stock.Service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenProvider {
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; //1시간
    private static final String KEY_ROLES = "roles";

    private final MemberService memberService;

    @Value("{spring.jwt.secret}")
    private String secretKey;

    //토큰 생성(발급) 메소드
    public String generateToken(String username, List<String> roles) {
        //사용자의 권한 정보를 저장
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("KEY_ROLES", roles); //claims에 저장할때는 key value값으로 저장해야됨

        var now = new Date();//토큰이 생성된 시간
        var expiredDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);//토큰 만료 시간

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)   //토큰 생성시간
                .setExpiration(expiredDate) //토큰 만료시간
                .signWith(SignatureAlgorithm.HS512, this.secretKey) //사용할 암호화 알고리즘, 비밀키
                .compact();
    }

    //토큰 유효 체크 메소드(아래) 활용
    public String getUserName(String token) {
        return this.parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) return false;

        var claims = this.parseClaims(token);
        return !claims.getExpiration().before(new Date()); //토큰의 만료시간이 현재시간보다 이전인지 아닌지
    }

    //토큰이 유효한지 확인하는 메소드
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(this.secretKey)
                    .parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {  //만료된 토큰을 확인하려면 발생하는 오류
            return e.getClaims();
        }
    }

    //jwt토큰으로부터 인증 정보를 가져오는 메소드
    public Authentication getAuthentication(String jwt) {
        UserDetails userDetails = this.memberService.loadUserByUsername(this.getUserName(jwt));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}
