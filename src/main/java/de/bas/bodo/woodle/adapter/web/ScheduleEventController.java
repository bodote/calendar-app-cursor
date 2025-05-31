package de.bas.bodo.woodle.adapter.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import de.bas.bodo.woodle.domain.service.PollStorageService;
import de.bas.bodo.woodle.domain.model.PollData;
import de.bas.bodo.woodle.view.ScheduleEventStep1Form;
import de.bas.bodo.woodle.view.ScheduleEventStep2Form;
import de.bas.bodo.woodle.view.ScheduleEventStep3Form;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ScheduleEventController {

    private final PollStorageService pollStorageService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    public ScheduleEventController(PollStorageService pollStorageService) {
        this.pollStorageService = pollStorageService;
    }

    @PostMapping("/schedule-event-step3")
    public String handleStep3Submit(
            @RequestParam("expiryDate") String expiryDate,
            HttpSession session,
            Model model) throws Exception {

        // Get form data from session
        ScheduleEventStep1Form step1Form = (ScheduleEventStep1Form) session.getAttribute("step1FormData");
        ScheduleEventStep2Form step2Form = (ScheduleEventStep2Form) session.getAttribute("step2FormData");
        ScheduleEventStep3Form step3Form = new ScheduleEventStep3Form(expiryDate);

        // Store step3 form data in session
        session.setAttribute("step3FormData", step3Form);

        // Convert string values to LocalDate and LocalTime
        LocalDate eventDate = LocalDate.parse(step2Form.date(), DATE_FORMATTER);
        LocalTime startTime = LocalTime.parse(step2Form.startTime(), TIME_FORMATTER);
        LocalTime endTime = LocalTime.parse(step2Form.endTime(), TIME_FORMATTER);
        LocalDate expiryDateParsed = LocalDate.parse(expiryDate, DATE_FORMATTER);

        // Create poll data
        PollData pollData = new PollData(
                step1Form.name(),
                step1Form.email(),
                step1Form.title(),
                step1Form.description(),
                eventDate,
                startTime,
                endTime,
                expiryDateParsed);

        // Store poll data and get URL
        String pollUrl = pollStorageService.storePoll(pollData);
        session.setAttribute("pollUrl", pollUrl);

        // Add data to model for summary page
        model.addAttribute("step1FormData", step1Form);
        model.addAttribute("step2FormData", step2Form);
        model.addAttribute("step3FormData", step3Form);
        model.addAttribute("pollUrl", pollUrl);

        // Keep session active to allow going back and modifying data
        return "event-summary";
    }

    @GetMapping("/event-summary")
    public String showSummary(Model model, HttpSession session) {
        ScheduleEventStep1Form step1Form = (ScheduleEventStep1Form) session.getAttribute("step1FormData");
        ScheduleEventStep2Form step2Form = (ScheduleEventStep2Form) session.getAttribute("step2FormData");
        ScheduleEventStep3Form step3Form = (ScheduleEventStep3Form) session.getAttribute("step3FormData");
        String pollUrl = (String) session.getAttribute("pollUrl");

        if (step1Form == null || step2Form == null || step3Form == null || pollUrl == null) {
            return "redirect:/";
        }

        model.addAttribute("step1FormData", step1Form);
        model.addAttribute("step2FormData", step2Form);
        model.addAttribute("step3FormData", step3Form);
        model.addAttribute("pollUrl", pollUrl);

        return "event-summary";
    }

    @GetMapping("/schedule-event-step3")
    public String showStep3Form(Model model, HttpSession session) {
        ScheduleEventStep2Form step2Form = (ScheduleEventStep2Form) session.getAttribute("step2FormData");
        if (step2Form != null) {
            LocalDate eventDate = LocalDate.parse(step2Form.date(), DATE_FORMATTER);
            LocalDate defaultExpiryDate = eventDate.plusMonths(3);
            model.addAttribute("step3FormData", new ScheduleEventStep3Form(defaultExpiryDate.format(DATE_FORMATTER)));
        } else {
            model.addAttribute("step3FormData", new ScheduleEventStep3Form(null));
        }
        return "schedule-event-step3";
    }
}