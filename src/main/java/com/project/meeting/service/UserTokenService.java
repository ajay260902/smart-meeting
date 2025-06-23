package com.project.meeting.service;

import com.project.meeting.model.UserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class UserTokenService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void saveToken(UserToken token) {
        String checkSql = "SELECT COUNT(*) FROM smart_meeting.user_tokens WHERE email = ?";

        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, token.getEmail());

        if (count != null && count == 0) {
            String sql = """
        INSERT INTO smart_meeting.user_tokens
        (email, access_token, refresh_token, expires_in, issued_at, token_type, scope)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

            jdbcTemplate.update(sql,
                    token.getEmail(),
                    token.getAccessToken(),
                    token.getRefreshToken(),
                    token.getExpiresIn(),
                    token.getIssuedAt(),
                    token.getTokenType(),
                    token.getScope()
            );
        }
    }

    public UserToken getLatestTokenByEmail(String email) {
        String sql = """
            SELECT * FROM smart_meeting.user_tokens
            WHERE email = ?
            ORDER BY issued_at DESC
            LIMIT 1
        """;

        return jdbcTemplate.queryForObject(sql, new Object[]{email}, (rs, rowNum) -> {
            UserToken token = new UserToken();
            token.setEmail(rs.getString("email"));
            token.setAccessToken(rs.getString("access_token"));
            token.setRefreshToken(rs.getString("refresh_token"));
            token.setExpiresIn(rs.getInt("expires_in"));
            token.setIssuedAt(rs.getTimestamp("issued_at").toLocalDateTime());
            token.setTokenType(rs.getString("token_type"));
            token.setScope(rs.getString("scope"));
            return token;
        });
    }

    public boolean isAccessTokenExpired(UserToken token) {
        return token.getIssuedAt()
                .plusSeconds(token.getExpiresIn())
                .isBefore(LocalDateTime.now());
    }

    public void updateAccessTokenByEmail(String email, String newAccessToken, int expiresInSeconds) {
        String sql = "UPDATE smart_meeting.user_tokens SET access_token = ?, expires_in = ?, issued_at = ? WHERE email = ?";
        jdbcTemplate.update(
                sql,
                newAccessToken,
                expiresInSeconds,
                Timestamp.valueOf(LocalDateTime.now()),
                email
        );
    }
}
