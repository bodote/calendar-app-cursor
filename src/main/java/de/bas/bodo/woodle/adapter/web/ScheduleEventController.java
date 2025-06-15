package de.bas.bodo.woodle.adapter.web;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.bas.bodo.woodle.domain.model.PollData;
import de.bas.bodo.woodle.domain.service.PollStorageService;
import de.bas.bodo.woodle.view.ScheduleEventStep1Form;
import de.bas.bodo.woodle.view.ScheduleEventStep2Form;
import de.bas.bodo.woodle.view.ScheduleEventStep3Form;
import de.bas.bodo.woodle.view.TimeSlot;
import jakarta.servlet.http.HttpSession;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Controller
// a test
public class ScheduleEventController {

    private final PollStorageService pollStorageService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;
    private final ObjectMapper objectMapper;
    private final String bucketName;
    private final String baseUrl;
    private final S3Client s3Client;

    public ScheduleEventController(PollStorageService pollStorageService, ObjectMapper objectMapper,
            @Value("${app.s3.bucket-name}") String bucketName,
            @Value("${app.base-url}") String baseUrl,
            S3Client s3Client) {
        this.pollStorageService = pollStorageService;
        this.objectMapper = objectMapper;
        this.bucketName = bucketName;
        this.baseUrl = baseUrl;
        this.s3Client = s3Client;
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
        List<PollData.EventTimeSlot> eventTimeSlots = step2Form.timeSlots().stream()
                .filter(slot -> !slot.date().isEmpty() && !slot.startTime().isEmpty() && !slot.endTime().isEmpty())
                .map(slot -> new PollData.EventTimeSlot(
                        LocalDate.parse(slot.date(), DATE_FORMATTER),
                        LocalTime.parse(slot.startTime(), TIME_FORMATTER),
                        LocalTime.parse(slot.endTime(), TIME_FORMATTER)))
                .toList();
        LocalDate expiryDateParsed = LocalDate.parse(expiryDate, DATE_FORMATTER);

        // Create poll data
        PollData pollData = new PollData(
                step1Form.name(),
                step1Form.email(),
                step1Form.title(),
                step1Form.description(),
                eventTimeSlots,
                expiryDateParsed,
                List.of()); // Initialize with empty participants list

        // Store poll data and get URL
        String pollUrl = pollStorageService.storePoll(pollData);
        session.setAttribute("pollUrl", pollUrl);

        // Add data to model for summary page
        model.addAttribute("step1FormData", step1Form);
        model.addAttribute("step2FormData", step2Form);
        model.addAttribute("step3FormData", step3Form);
        model.addAttribute("pollUrl", pollUrl);
        model.addAttribute("participants", List.of()); // Initialize with empty participants list for new polls

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
        model.addAttribute("participants", List.of()); // Initialize with empty participants list for new polls

        return "event-summary";
    }

    @GetMapping("/schedule-event-step3")
    public String showStep3Form(Model model, HttpSession session) {
        ScheduleEventStep2Form step2Form = (ScheduleEventStep2Form) session.getAttribute("step2FormData");
        if (step2Form != null && !step2Form.timeSlots().isEmpty()) {
            TimeSlot firstSlot = step2Form.timeSlots().get(0);
            if (firstSlot.date() != null && !firstSlot.date().isEmpty()) {
                LocalDate eventDate = LocalDate.parse(firstSlot.date(), DATE_FORMATTER);
                LocalDate defaultExpiryDate = eventDate.plusMonths(3);
                model.addAttribute("step3FormData",
                        new ScheduleEventStep3Form(defaultExpiryDate.format(DATE_FORMATTER)));
            } else {
                model.addAttribute("step3FormData", new ScheduleEventStep3Form(null));
            }
        } else {
            model.addAttribute("step3FormData", new ScheduleEventStep3Form(null));
        }
        return "schedule-event-step3";
    }

    @GetMapping("/event/{uuid}")
    public String showEventByUuid(@PathVariable String uuid, Model model) throws Exception {
        // Get poll data from S3
        String key = "polls/" + uuid + ".json";
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> responseStream = s3Client.getObject(getObjectRequest);
        String jsonData = new String(responseStream.readAllBytes());
        PollData pollData = objectMapper.readValue(jsonData, PollData.class);

        // Convert PollData to form data
        ScheduleEventStep1Form step1Form = new ScheduleEventStep1Form(
                pollData.name(),
                pollData.email(),
                pollData.title(),
                pollData.description());

        List<TimeSlot> timeSlots = pollData.timeSlots().stream()
                .map(slot -> new TimeSlot(
                        slot.date().format(DATE_FORMATTER),
                        slot.startTime().format(TIME_FORMATTER),
                        slot.endTime().format(TIME_FORMATTER)))
                .toList();

        ScheduleEventStep2Form step2Form = new ScheduleEventStep2Form(timeSlots);

        ScheduleEventStep3Form step3Form = new ScheduleEventStep3Form(
                pollData.expiryDate().format(DATE_FORMATTER));

        // Add data to model
        model.addAttribute("step1FormData", step1Form);
        model.addAttribute("step2FormData", step2Form);
        model.addAttribute("step3FormData", step3Form);
        model.addAttribute("pollUrl", baseUrl + "/event/" + uuid);
        model.addAttribute("participants", pollData.participants());

        return "event-summary";
    }

    @PostMapping("/event/{uuid}/participants/save")
    public String saveParticipant(
            @PathVariable String uuid,
            @RequestParam String participantName,
            @RequestParam(value = "selectedSlots", required = false) String[] selectedSlots,
            Model model) throws Exception {

        // Load existing poll data
        String key = "polls/" + uuid + ".json";
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> responseStream = s3Client.getObject(getObjectRequest);
        String jsonData = new String(responseStream.readAllBytes());
        PollData existingPollData = objectMapper.readValue(jsonData, PollData.class);

        // Convert selected slots to integers
        List<Integer> selectedSlotIndexes = new ArrayList<>();
        if (selectedSlots != null) {
            selectedSlotIndexes = Arrays.stream(selectedSlots)
                    .map(Integer::parseInt)
                    .toList();
        }

        // Create new participant
        PollData.Participant newParticipant = new PollData.Participant(participantName, selectedSlotIndexes);

        // Add participant to existing list
        List<PollData.Participant> updatedParticipants = new ArrayList<>(existingPollData.participants());
        updatedParticipants.add(newParticipant);

        // Create updated poll data
        PollData updatedPollData = new PollData(
                existingPollData.name(),
                existingPollData.email(),
                existingPollData.title(),
                existingPollData.description(),
                existingPollData.timeSlots(),
                existingPollData.expiryDate(),
                updatedParticipants);

        // Save updated poll data back to S3
        pollStorageService.storePollWithUuid(uuid, updatedPollData);

        // Convert updated PollData to form data for display
        ScheduleEventStep1Form step1Form = new ScheduleEventStep1Form(
                updatedPollData.name(),
                updatedPollData.email(),
                updatedPollData.title(),
                updatedPollData.description());

        List<TimeSlot> timeSlots = updatedPollData.timeSlots().stream()
                .map(slot -> new TimeSlot(
                        slot.date().format(DATE_FORMATTER),
                        slot.startTime().format(TIME_FORMATTER),
                        slot.endTime().format(TIME_FORMATTER)))
                .toList();

        ScheduleEventStep2Form step2Form = new ScheduleEventStep2Form(timeSlots);

        ScheduleEventStep3Form step3Form = new ScheduleEventStep3Form(
                updatedPollData.expiryDate().format(DATE_FORMATTER));

        // Redirect to the event page to show the updated data
        // This follows the Post-Redirect-Get pattern to prevent duplicate submissions
        return "redirect:/event/" + uuid;
    }
}