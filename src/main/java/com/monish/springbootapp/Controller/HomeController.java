package com.monish.springbootapp.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/grid")
    public String grid() {
        return "forward:/grid.html";
    }

    @GetMapping("/finance")
    public String finance() {
        return "forward:/finance.html";
    }

    @GetMapping("/master")
    public String master() {
        return "forward:/master.html";
    }

    // Forward legacy routes to their respective nested hub panels
    @GetMapping("/poles")
    public String poles() {
        return "redirect:/grid.html#poles";
    }

    @GetMapping("/charging")
    public String charging() {
        return "redirect:/grid.html#charging";
    }

    @GetMapping("/workflow")
    public String workflow() {
        return "redirect:/grid.html#workflow";
    }

    @GetMapping("/billing")
    public String billing() {
        return "redirect:/finance.html#billing";
    }

    @GetMapping("/ledger")
    public String ledger() {
        return "redirect:/finance.html#ledger";
    }

    @GetMapping("/reports")
    public String reports() {
        return "redirect:/finance.html#reports";
    }

    @GetMapping("/contacts")
    public String contacts() {
        return "redirect:/master.html#contacts";
    }

    @GetMapping("/products")
    public String products() {
        return "redirect:/master.html#products";
    }

    @GetMapping("/coa")
    public String coa() {
        return "redirect:/master.html#coa";
    }

    @GetMapping("/budget")
    public String budget() {
        return "redirect:/master.html#budget";
    }
}