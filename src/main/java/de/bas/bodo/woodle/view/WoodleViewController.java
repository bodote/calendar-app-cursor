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
import de.bas.bodo.woodle.view.ScheduleEventForm;
import de.bas.bodo.woodle.view.ScheduleEventStep2Form;
import de.bas.bodo.woodle.view.ScheduleEventStep3Form;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@SessionAttributes({ "formData", "step2FormData" })
public class WoodleViewController {

    @GetMapping("/index.html")
    public String index() {
        return "index";
    }

    @GetMapping("/schedule-event")
    public String scheduleEvent(Model model) {
        if (!model.containsAttribute("formData")) {
            model.addAttribute("formData", new ScheduleEventForm("", "", "", ""));
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
        ScheduleEventForm formData = new ScheduleEventForm(name, email, title, description);
        model.addAttribute("formData", formData);
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
        if (!model.containsAttribute("formData")) {
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

    @GetMapping("/schedule-event-step3")
    public String getScheduleEventStep3(Model model) {
        if (!model.containsAttribute("formData") || !model.containsAttribute("step2FormData")) {
            return "redirect:/schedule-event";
        }

        // Calculate default expiry date (3 months from the event date)
        ScheduleEventStep2Form step2FormData = (ScheduleEventStep2Form) model.getAttribute("step2FormData");
        LocalDate eventDate = LocalDate.parse(step2FormData.date());
        LocalDate expiryDate = eventDate.plusMonths(3);
        String formattedExpiryDate = expiryDate.format(DateTimeFormatter.ISO_DATE);

        if (!model.containsAttribute("step3FormData")) {
            model.addAttribute("step3FormData", new ScheduleEventStep3Form(formattedExpiryDate));
        }
        return "schedule-event-step3";
    }
}