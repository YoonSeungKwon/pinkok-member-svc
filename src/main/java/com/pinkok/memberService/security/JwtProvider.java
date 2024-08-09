package com.pinkok.memberService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.pinkok.memberService.entity.Members;
import com.pinkok.memberService.repository.MemberRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final MemberRepository memberRepository;

    @Value("${JWT_SECRET}")
    private String SECRET;

    @Value("${JWT_ACC}")
    private String ACC;

    @Value("${JWT_REF}")
    private String REF;
    private long getAccExp(){
        return 5 * 24 * 2 * Long.parseLong(ACC) * 60 * 1000l;
    }
    private long getRefExp(){
        return Long.parseLong(REF) * 60 * 60 * 1000l;
    }
    private SecretKey getSecretKey(){
      return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private String findIdByToken(String token){
        return memberRepository.findMembersByRefresh(token).getEmail();
    }

    public String createAccessToken(String id){

        Claims claims = Jwts.claims()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + getAccExp()));

        return  Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HMAC")
                .setClaims(claims)
                .claim("username", id)
                .signWith(getSecretKey())
                .compact();
    }

    public String createRefreshToken(){

        Claims claims = Jwts.claims()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + getRefExp()));

        return  Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HMAC")
                .setClaims(claims)
                .signWith(getSecretKey())
                .compact();
    }

    public String createNewToken(String ref_token){
        return createAccessToken(findIdByToken(ref_token));
    }


    public Authentication getAuthentication(String token){
        Members members = memberRepository.findMembersByEmail(getId(token));
        return new UsernamePasswordAuthenticationToken(members, null, members.getAuthority());
    }

    public String getId(String token){
        return (String)Jwts.parserBuilder().setSigningKey(getSecretKey()).build().parseClaimsJws(token)
                .getBody().get("username");
    }

    public String resolveAccessToken(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if(StringUtils.hasText(token) && token.startsWith("Bearer"))
            return token.substring(7);
        else return null;
    }

    public String resolveRefreshToken(HttpServletRequest request){
        String token = request.getHeader("X-Refresh-Token");
        if(StringUtils.hasText(token) && token.startsWith("Bearer"))
            return token.substring(7);
        else return null;
    }

    public boolean validateToken(String token){
        try{
            Claims claims = Jwts.parserBuilder().setSigningKey(getSecretKey()).build()
                    .parseClaimsJws(token).getBody();
            return !claims.getExpiration().before(new Date());
        }catch(Exception e){
            return false;
        }
    }

}
