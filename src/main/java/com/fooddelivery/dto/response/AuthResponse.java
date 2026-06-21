package com.fooddelivery.dto.response;

import com.fooddelivery.model.enums.UserRole;

public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String name;
    private String email;
    private UserRole role;

    public AuthResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token; private String tokenType; private Long userId;
        private String name; private String email; private UserRole role;

        public Builder token(String token) { this.token = token; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(UserRole role) { this.role = role; return this; }

        public AuthResponse build() {
            AuthResponse r = new AuthResponse();
            r.token = token; r.tokenType = tokenType; r.userId = userId;
            r.name = name; r.email = email; r.role = role;
            return r;
        }
    }

    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
}
