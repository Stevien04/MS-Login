package app.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/ui/login-academico")
    public String loginAcademico() {
        return "login-academico";
    }

    @GetMapping("/ui/login-administrativo")
    public String loginAdministrativo() {
        return "login-administrativo";
    }

    @GetMapping("/dashboard-academico")
    public String dashboardAcademico() {
        return "dashboard-academico";
    }

    @GetMapping("/dashboard-administrativo")
    public String dashboardAdministrativo() {
        return "dashboard-administrativo";
    }

    @GetMapping("/ui/usuarios")
    public String usuarios() {
        return "crud-usuario";
    }

    @GetMapping("/ui/estudiantes")
    public String estudiantes() {
        return "crud-estudiante";
    }

    @GetMapping("/ui/docentes")
    public String docentes() {
        return "crud-docente";
    }

    @GetMapping("/ui/administrativos")
    public String administrativos() {
        return "crud-administrativo";
    }
}