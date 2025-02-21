package com.lyc.utils;

import com.lyc.dto.UserDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * @ClassName JwtUtil
 * @Description TODO
 * @Author LuKey_C
 * @Date 2025/2/21 10:14
 * @Version 1.0
 */

public class JwtUtil {

    private static final long tokenExpiration = 60 * 60 * 1000L;
    private static final SecretKey tokenSingKey = Keys.hmacShaKeyFor("J6juKnBkhBfJZrRcJFc5wHajHGmnr5ZV".getBytes());

    public static String createToken(Map<String, Object> map){

        String token = Jwts.builder().
                setSubject("USER_INFO").
                setExpiration(new Date(System.currentTimeMillis() + tokenExpiration)).
                setClaims(map).
                signWith(tokenSingKey).
                compact();
        return token;
    }

    public static Claims parseToken(String token) throws Exception {
        if (token==null){
            throw new RuntimeException("未登陆");
        }
        try{
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(tokenSingKey).build();
            return jwtParser.parseClaimsJws(token).getBody();
        }catch (ExpiredJwtException e){
            throw new RuntimeException("token过期");
        }catch (JwtException e){
            throw new RuntimeException("token非法");
        }
    }
}
