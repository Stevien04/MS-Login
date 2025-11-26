package app.dto;

public class LoginRequest {
    private String codigoOEmail;
    private String password;
    private String tipoLogin;

    public LoginRequest() {}

    public String getCodigoOEmail() {
        return codigoOEmail;
    }

    public void setCodigoOEmail(String codigoOEmail) {
        this.codigoOEmail = codigoOEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTipoLogin() {
        return tipoLogin;
    }

    public void setTipoLogin(String tipoLogin) {
        this.tipoLogin = tipoLogin;
    }

    public String identifier() {
        return codigoOEmail != null ? codigoOEmail.trim() : "";
    }

    public String normalizedTipoLogin() {
        return tipoLogin != null ? tipoLogin.trim().toLowerCase() : "";
    }
}
