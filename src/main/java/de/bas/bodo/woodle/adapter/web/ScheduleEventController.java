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
    public RedirectView handleStep3Submit(
            @RequestParam("expiryDate") String expiryDate,
            HttpSession session,
            RedirectAttributes redirectAttributes) throws Exception {

        // Get form data from session
        ScheduleEventStep1Form step1Form = (ScheduleEventStep1Form) session.getAttribute("step1FormData");
        ScheduleEventStep2Form step2Form = (ScheduleEventStep2Form) session.getAttribute("step2FormData");
        ScheduleEventStep3Form step3Form = new ScheduleEventStep3Form(expiryDate);

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

        // Add data to model for summary page
        redirectAttributes.addFlashAttribute("step1FormData", step1Form);
        redirectAttributes.addFlashAttribute("step2FormData", step2Form);
        redirectAttributes.addFlashAttribute("step3FormData", step3Form);
        redirectAttributes.addFlashAttribute("pollUrl", pollUrl);

        // Clear session
        session.invalidate();

        RedirectView redirectView = new RedirectView("/event-summary");
        redirectView.setHttp10Compatible(false);
        return redirectView;
    }

    @GetMapping("/event-summary")
    public Object showSummary(Model model) {
        if (!model.containsAttribute("step1FormData") ||
                !model.containsAttribute("step2FormData") ||
                !model.containsAttribute("step3FormData") ||
                !model.containsAttribute("pollUrl")) {
            RedirectView redirectView = new RedirectView("/");
            redirectView.setHttp10Compatible(false);
            return redirectView;
        }
        return "event-summary";
    }

    @GetMapping("/schedule-event-step3")
    public String showStep3Form(Model model) {
        model.addAttribute("step3FormData", new ScheduleEventStep3Form(null));
        return "schedule-event-step3";
    }
}