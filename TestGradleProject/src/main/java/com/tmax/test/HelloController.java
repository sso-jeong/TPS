package com.tmax.test;

import org.springframework.ui.Model;

//@Controller
public class HelloController {
	
	//@GetMapping("hello")
	public String hello(Model model) {
		model.addAttribute("data", "hello!!");
		return "hello";
	}
}
