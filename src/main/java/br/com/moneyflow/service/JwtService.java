package br.com.moneyflow.service;

import br.com.moneyflow.exception.authorization.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret:chave-secreta-para-jwt-token-com-minimo-256-bits-necessarios}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 horas em milissegundos
    private Long expiration;

    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Long.parseLong(claims.getSubject());
        } catch (SignatureException e) {
            throw new InvalidTokenException("Assinatura do token inválida");
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException("Token malformado");
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Token expirado");
        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException("Token não suportado");
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Token vazio ou nulo");
        } catch (Exception e) {
            throw new InvalidTokenException("Erro ao processar token");
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getExpirationInSeconds() {
        return expiration / 1000;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
