package org.example.bolsadeempleo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Redirige todas las rutas HTML al SPA (index.html en static/).
 * Las rutas /api/** son manejadas por los @RestController.
 */
@Controller
public class SpaController {

    @RequestMapping(value = {"/", "/login", "/empresa/**", "/oferente/**", "/admin/**"})
    public String spa() {
        return "forward:/index.html";
    }
}

