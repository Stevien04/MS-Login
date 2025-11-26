package app.dto;

public class LogoutRequest {
    private Integer usuarioId;
    private String token;

    public LogoutRequest() {
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
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