package cn.how2j.trend.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {
	@GetMapping(value="/")
	public String listIndex(){
		return "view";
	}
}
