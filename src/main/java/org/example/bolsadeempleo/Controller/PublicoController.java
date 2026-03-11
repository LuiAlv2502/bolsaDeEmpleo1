package org.example.bolsadeempleo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicoController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}