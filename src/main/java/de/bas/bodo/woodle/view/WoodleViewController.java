package de.bas.bodo.woodle.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WoodleViewController {

    @GetMapping("/index.html")
    public String index() {
        return "index";
    }

    @GetMapping("/schedule-event")
    public String scheduleEvent() {
        return "schedule-event";
    }

    @GetMapping("/")
    public String redirectToIndex() {
        return "redirect:/index.html";
    }
}