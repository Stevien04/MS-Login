package app.dto;

public class SessionValidationRequest {
    private String token;

    public SessionValidationRequest() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String normalizedToken() {
        return token != null ? token.trim() : "";
    }
}