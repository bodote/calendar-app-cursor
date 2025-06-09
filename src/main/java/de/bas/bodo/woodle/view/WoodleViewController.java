package de.bas.bodo.woodle.view;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@SessionAttributes({ "step1FormData", "step2FormData" })
public class WoodleViewController {

    private static final Logger log = LoggerFactory.getLogger(WoodleViewController.class);

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

    @PostMapping("/schedule-event-step2/add-time-slot")
    public ResponseEntity<String> addTimeSlot(
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime,
            Model model) {
        log.debug("addTimeSlot called with date='{}', startTime='{}', endTime='{}'", date, startTime, endTime);
        ScheduleEventStep2Form currentForm = (ScheduleEventStep2Form) model.getAttribute("step2FormData");
        if (currentForm == null) {
            log.debug("No current form, creating new with submitted values.");
            currentForm = new ScheduleEventStep2Form(date, startTime, endTime);
            log.debug("Initial timeSlots: {}", currentForm.timeSlots());
        } else {
            List<ScheduleEventStep2Form.TimeSlot> timeSlots = new ArrayList<>(currentForm.timeSlots());
            log.debug("Before adding: size={}, content={}", timeSlots.size(), timeSlots);
            boolean submittedSlotIsEmpty = date.isEmpty() && startTime.isEmpty() && endTime.isEmpty();
            log.debug("Submitted slot is empty: {}", submittedSlotIsEmpty);

            // Check if the submitted data already exists in the list
            boolean slotExists = timeSlots.stream()
                    .anyMatch(slot -> date.equals(slot.date()) &&
                            startTime.equals(slot.startTime()) &&
                            endTime.equals(slot.endTime()));
            log.debug("Submitted slot already exists in list: {}", slotExists);

            if (!submittedSlotIsEmpty && !slotExists) {
                if (timeSlots.isEmpty()) {
                    log.debug("List is empty, adding submitted slot.");
                    timeSlots.add(new ScheduleEventStep2Form.TimeSlot(date, startTime, endTime));
                } else {
                    ScheduleEventStep2Form.TimeSlot last = timeSlots.get(timeSlots.size() - 1);
                    boolean lastIsEmpty = last.date().isEmpty() && last.startTime().isEmpty()
                            && last.endTime().isEmpty();
                    log.debug("Last slot: {} (empty={})", last, lastIsEmpty);

                    if (lastIsEmpty) {
                        log.debug("Last slot is empty, updating it with submitted values.");
                        timeSlots.set(timeSlots.size() - 1,
                                new ScheduleEventStep2Form.TimeSlot(date, startTime, endTime));
                    } else {
                        log.debug("Last slot is not empty, adding new slot.");
                        timeSlots.add(new ScheduleEventStep2Form.TimeSlot(date, startTime, endTime));
                    }
                }
            } else {
                log.debug("Not adding slot: submitted slot is empty or already exists in list.");
            }

            // Always ensure only one empty slot at the end
            ScheduleEventStep2Form.TimeSlot last = timeSlots.isEmpty() ? null : timeSlots.get(timeSlots.size() - 1);
            boolean lastIsEmpty = last != null && last.date().isEmpty() && last.startTime().isEmpty()
                    && last.endTime().isEmpty();
            if (!lastIsEmpty) {
                log.debug("Ensuring one empty slot at the end.");
                timeSlots.add(new ScheduleEventStep2Form.TimeSlot("", "", ""));
            } else {
                log.debug("Last slot is already empty, not adding another.");
            }
            log.debug("Final timeSlots before updating model: size={}, content={}", timeSlots.size(), timeSlots);
            currentForm = new ScheduleEventStep2Form(timeSlots);
        }
        model.addAttribute("step2FormData", currentForm);
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .header("Location", "/schedule-event-step2")
                .build();
    }
}