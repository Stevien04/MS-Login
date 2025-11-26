package app.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

public class LoginResponse {
    public boolean success;
    public String message;
    public String token;
    public PerfilResponse perfil;

    @JsonIgnore
    public HttpStatus status = HttpStatus.OK;

    public LoginResponse() {
    }

    public static LoginResponse success(String message, String token, PerfilResponse perfil) {
        LoginResponse response = new LoginResponse();
        response.success = true;
        response.message = message;
        response.token = token;
        response.perfil = perfil;
        response.status = HttpStatus.OK;
        return response;
    }

    public static LoginResponse failure(String message, HttpStatus status) {
        LoginResponse response = new LoginResponse();
        response.success = false;
        response.message = message;
        response.token = null;
        response.perfil = null;
        response.status = status != null ? status : HttpStatus.BAD_REQUEST;
        return response;
    }
}