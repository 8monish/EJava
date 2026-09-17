package com.monish.springbootapp.Controller;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;




@RestController
@RequestMapping("/")
public class HomeController {
    @GetMapping("")
    public String get() {
        return "Hello";
    }

    @PostMapping("create")
    public String create(@RequestBody String payload) {
        return payload + "Successfully created";
    }

    @PutMapping("put")
    public String put(@RequestBody String entity) {
        return entity+"Successfull put";
    }

    @DeleteMapping("del/{id}")
    public String del(@PathVariable String id) {
      return "Deleted" + " " + id;
    }

    @GetMapping("getmap")
    public String getmap(@PathVariable String id) {
        return id + "-_-";
    }
    
}