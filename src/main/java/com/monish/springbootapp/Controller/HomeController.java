package com.monish.springbootapp.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/poles")
    public String poles() {
        return "forward:/poles.html";
    }

    @GetMapping("/charging")
    public String charging() {
        return "forward:/charging.html";
    }

    @GetMapping("/billing")
    public String billing() {
        return "forward:/billing.html";
    }

    @GetMapping("/ledger")
    public String ledger() {
        return "forward:/ledger.html";
    }

    @GetMapping("/contacts")
    public String contacts() {
        return "forward:/contacts.html";
    }

    @GetMapping("/products")
    public String products() {
        return "forward:/products.html";
    }

    @GetMapping("/coa")
    public String coa() {
        return "forward:/coa.html";
    }

    @GetMapping("/budget")
    public String budget() {
        return "forward:/budget.html";
    }

    @GetMapping("/reports")
    public String reports() {
        return "forward:/reports.html";
    }

    @GetMapping("/workflow")
    public String workflow() {
        return "forward:/workflow.html";
    }
}