package de.bas.bodo.woodle.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.Data;
import de.bas.bodo.woodle.view.ScheduleEventStep1Form;
import de.bas.bodo.woodle.view.ScheduleEventStep2Form;
import de.bas.bodo.woodle.view.ScheduleEventStep3Form;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@SessionAttributes({ "step1FormData", "step2FormData" })
public class WoodleViewController {

    @GetMapping("/index.html")
    public String index(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "index";
    }

    @GetMapping("/schedule-event")
    public String scheduleEvent(Model model) {
        if (!model.containsAttribute("step1FormData")) {
            model.addAttribute("step1FormData", new ScheduleEventStep1Form("", "", "", ""));
        }
        return "schedule-event";
    }

    @PostMapping("/schedule-event")
    public ResponseEntity<String> scheduleEventStep2(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            Model model) {
        ScheduleEventStep1Form formData = new ScheduleEventStep1Form(name, email, title, description);
        model.addAttribute("step1FormData", formData);
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .header("Location", "/schedule-event-step2")
                .build();
    }

    @GetMapping("/")
    public String redirectToIndex() {
        return "redirect:/index.html";
    }

    @GetMapping("/schedule-event-step2")
    public String getScheduleEventStep2(Model model) {
        if (!model.containsAttribute("step1FormData")) {
            return "redirect:/schedule-event";
        }
        if (!model.containsAttribute("step2FormData")) {
            model.addAttribute("step2FormData", new ScheduleEventStep2Form("", "", ""));
        }
        return "schedule-event-step2";
    }

    @PostMapping("/schedule-event-step2")
    public ResponseEntity<String> scheduleEventStep3(
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime,
            Model model) {
        ScheduleEventStep2Form step2FormData = new ScheduleEventStep2Form(date, startTime, endTime);
        model.addAttribute("step2FormData", step2FormData);
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .header("Location", "/schedule-event-step3")
                .build();
    }
}