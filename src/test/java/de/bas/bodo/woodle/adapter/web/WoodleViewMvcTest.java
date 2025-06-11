package de.bas.bodo.woodle.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ScenarioStage;
import com.tngtech.jgiven.annotation.ScenarioState;
import com.tngtech.jgiven.junit5.ScenarioTest;

import de.bas.bodo.woodle.domain.model.PollData;
import de.bas.bodo.woodle.domain.service.PollStorageService;
import de.bas.bodo.woodle.view.WoodleViewController;
import gg.jte.springframework.boot.autoconfigure.JteAutoConfiguration;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@WebMvcTest({ WoodleViewController.class, ScheduleEventController.class })
@ImportAutoConfiguration(JteAutoConfiguration.class)
@Import({ PollStorageService.class })
@ActiveProfiles({ "test" })
@TestPropertySource(properties = {
        "gg.jte.development-mode=true",
        "app.s3.bucket-name=de.bas.bodo",
        "app.base-url=http://localhost:8080"
})
class WoodleViewMvcTest
        extends ScenarioTest<GivenWoodleViewMvcState, WhenWoodleViewMvcAction, ThenWoodleViewMvcOutcome> {

    private static final Logger log = LoggerFactory.getLogger(WoodleViewMvcTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3Client s3Client;

    @Autowired
    private ObjectMapper objectMapper;

    // Test data constants
    private static final String TEST_NAME = "Alice";
    private static final String TEST_EMAIL = "alice@example.com";
    private static final String TEST_TITLE = "My Poll";
    private static final String TEST_DESCRIPTION = "Some description";

    public static class TimeSlot {
        private final String date;
        private final String startTime;
        private final String endTime;

        public TimeSlot(String date, String startTime, String endTime) {
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String date() {
            return date;
        }

        public String startTime() {
            return startTime;
        }

        public String endTime() {
            return endTime;
        }
    }

    @ScenarioStage
    private GivenWoodleViewMvcState givenWoodleViewMvcState;
    @ScenarioStage
    private WhenWoodleViewMvcAction whenWoodleViewMvcAction;
    @ScenarioStage
    private ThenWoodleViewMvcOutcome thenWoodleViewMvcOutcome;

    private MockHttpSession session;

    @BeforeEach
    void setupStages() {
        session = new MockHttpSession();
        givenWoodleViewMvcState.mock_mvc_is_configured(mockMvc)
                .and().object_mapper_is_configured(objectMapper);
    }

    @Test
    @DisplayName("Root path should redirect to index html")
    void root_path_should_redirect_to_index_html() throws Exception {
        given().mock_mvc_is_configured(mockMvc);
        when().user_visits_root_path();
        then().user_should_be_redirected_to_index_html();
    }

    @Test
    @DisplayName("User can navigate through the scheduling process")
    void user_can_navigate_through_scheduling_process() throws Exception {
        given().mock_mvc_is_configured(mockMvc)
                .and().user_is_on_homepage()
                .and().user_has_test_data(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION);
        when().user_clicks_schedule_event_button()
                .and().user_sets_input_fields_on_schedule_event_page_and_clicks_next();
        then().user_should_see_step2_form()
                .and().step2_form_should_have_all_required_fields();
        when().user_clicks_back();
        then().user_should_see_step1_form_with_previous_data();
        when().user_sets_input_fields_on_schedule_event_step2_and_clicks_next();
        then().user_should_see_step3_form()
                .and().step3_form_should_have_expiry_date("2024-06-20");
        when().user_clicks_back();
        then().user_should_see_step2_form_with_previous_data();
        when().user_sets_input_fields_on_schedule_event_step3_and_clicks_next();
        then().user_should_see_summary_page()
                .and().summary_page_should_show_all_entered_data()
                .and().summary_page_should_show_event_url();
    }

    @Test
    @DisplayName("Should display summary and store in S3")
    void should_display_summary_and_store_in_s3() throws Exception {
        given().mock_mvc_is_configured(mockMvc)
                .and().s3_client_is_configured(s3Client)
                .and().user_is_on_homepage()
                .and().user_has_test_data("John Doe", "john@example.com", "Team Meeting", "Weekly sync");
        when().user_clicks_schedule_event_button()
                .and().user_sets_input_fields_on_schedule_event_page_and_clicks_next()
                .and().user_sets_input_fields_on_schedule_event_step2_and_clicks_next()
                .and().user_sets_input_fields_on_schedule_event_step3_and_clicks_next();
        then().user_should_see_summary_page()
                .and().summary_page_should_show_all_entered_data()
                .and().summary_page_should_show_event_url()
                .and().poll_should_be_stored_in_s3();
    }

    @Test
    @DisplayName("Should access summary page later without session")
    void should_access_summary_page_later_without_session() throws Exception {
        String testUuid = "123e4567-e89b-12d3-a456-426614174000";
        given().mock_mvc_is_configured(mockMvc)
                .and().s3_client_is_configured(s3Client)
                .and().user_has_test_data("John Doe", "john@example.com", "Team Meeting", "Weekly sync")
                .and().s3_client_returns_test_event_for_uuid(testUuid);
        when().user_accesses_event_url_without_session(testUuid);
        then().user_should_see_summary_page()
                .and().summary_page_should_show_all_entered_data();
    }

    @Test
    @DisplayName("Should show plus button on schedule-event-step2 page and handle adding new time slots")
    void should_show_plus_button_on_schedule_event_step2_page() throws Exception {
        given().mock_mvc_is_configured(mockMvc)
                .and().user_is_on_homepage()
                .and().user_has_test_data(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION);
        when().user_clicks_schedule_event_button()
                .and().user_sets_input_fields_on_schedule_event_page_and_clicks_next()
                .and().user_sets_input_fields_on_schedule_event_step2_and_clicks_next(Map.of(
                        0, new de.bas.bodo.woodle.adapter.web.TimeSlot("2024-03-20", "10:00", "11:00")))
                .and().user_clicks_plus_button();
        then().user_should_see_step2_form()
                .and().step2_form_should_have_plus_button()
                .and()
                .step2_form_should_show_previous_data(
                        List.of(new de.bas.bodo.woodle.adapter.web.TimeSlot("2024-03-20", "10:00", "11:00")))
                .and().step2_form_should_have_additional_time_slot_fields()
                .and().step2_form_should_have_empty_slot(1);
        when().user_sets_input_fields_on_schedule_event_step2_and_clicks_next(Map.of(
                0, new de.bas.bodo.woodle.adapter.web.TimeSlot("2024-03-21", "14:00", "15:00"),
                1, new de.bas.bodo.woodle.adapter.web.TimeSlot("2024-03-20", "10:00", "11:00")))
                .and().user_clicks_plus_button();
        then().user_should_see_step2_form()
                .and().step2_form_should_have_plus_button()
                .and().step2_form_should_show_previous_data(List.of(
                        new de.bas.bodo.woodle.adapter.web.TimeSlot("2024-03-21", "14:00", "15:00"),
                        new de.bas.bodo.woodle.adapter.web.TimeSlot("2024-03-20", "10:00", "11:00")))
                .and().step2_form_should_have_three_time_slots()
                .and().step2_form_should_show_previous_data(List.of(
                        new de.bas.bodo.woodle.adapter.web.TimeSlot("2024-03-21", "14:00", "15:00"),
                        new de.bas.bodo.woodle.adapter.web.TimeSlot("2024-03-20", "10:00", "11:00")))
                .and().step2_form_should_have_empty_slot(2);
    }

    @Test
    @DisplayName("Should allow adding multiple time slots and preserve their data")
    void should_allow_adding_multiple_time_slots() throws Exception {
        given().mock_mvc_is_configured(mockMvc)
                .and().user_is_on_homepage()
                .and().user_has_test_data(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION);
        when().user_clicks_schedule_event_button()
                .and().user_sets_input_fields_on_schedule_event_page_and_clicks_next()
                .and().user_sets_input_fields_on_schedule_event_step2_and_clicks_next(Map.of(
                        0, new de.bas.bodo.woodle.adapter.web.TimeSlot("2024-03-20", "10:00", "11:00")))
                .and().user_clicks_plus_button();
        then().user_should_see_step2_form()
                .and().step2_form_should_have_plus_button()
                .and().step2_form_should_show_previous_data()
                .and().step2_form_should_have_additional_time_slot_fields()
                .and().step2_form_should_preserve_all_time_slots();
    }
}

class GivenWoodleViewMvcState extends Stage<GivenWoodleViewMvcState> {
    @ScenarioState
    private MockMvc mockMvc;
    @ScenarioState
    private MockHttpSession session;
    @ScenarioState
    private String name;
    @ScenarioState
    private String email;
    @ScenarioState
    private String title;
    @ScenarioState
    private String description;
    @ScenarioState
    private S3Client s3Client;
    @ScenarioState
    private ObjectMapper objectMapper;

    public GivenWoodleViewMvcState mock_mvc_is_configured(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        return self();
    }

    public GivenWoodleViewMvcState object_mapper_is_configured(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return self();
    }

    public GivenWoodleViewMvcState s3_client_is_configured(S3Client s3Client) {
        this.s3Client = s3Client;
        return self();
    }

    public GivenWoodleViewMvcState user_is_on_homepage() {
        session = new MockHttpSession();
        return self();
    }

    public GivenWoodleViewMvcState user_has_test_data(String name, String email, String title, String description) {
        this.name = name;
        this.email = email;
        this.title = title;
        this.description = description;
        return self();
    }

    public GivenWoodleViewMvcState s3_client_returns_test_event_for_uuid(String uuid) throws Exception {
        PollData testEvent = new PollData(
                "John Doe",
                "john@example.com",
                "Team Meeting",
                "Weekly sync",
                LocalDate.parse("2024-03-20"),
                LocalTime.parse("10:00"),
                LocalTime.parse("11:00"),
                LocalDate.parse("2024-06-20"));

        String jsonData = objectMapper.writeValueAsString(testEvent);
        GetObjectResponse response = GetObjectResponse.builder().build();
        ResponseInputStream<GetObjectResponse> responseStream = new ResponseInputStream<>(
                response,
                new ByteArrayInputStream(jsonData.getBytes()));
        doReturn(responseStream).when(s3Client).getObject(any(GetObjectRequest.class));
        return self();
    }
}

class WhenWoodleViewMvcAction extends Stage<WhenWoodleViewMvcAction> {
    private static final Logger log = LoggerFactory.getLogger(WhenWoodleViewMvcAction.class);

    @ScenarioState
    private MockMvc mockMvc;
    @ScenarioState
    private MockHttpSession session;
    @ScenarioState
    private ResultActions resultAction;
    @ScenarioState
    private String name;
    @ScenarioState
    private String email;
    @ScenarioState
    private String title;
    @ScenarioState
    private String description;
    @ScenarioState
    private List<String> navigationHistory = new ArrayList<>();
    @ScenarioState
    private String currentPage;
    @ScenarioState
    private String eventUrl;

    private void addToHistory(String page) {
        log.info("Adding to history - Current page: {}, New page: {}, History size: {}",
                currentPage, page, navigationHistory.size());

        // Validate navigation path
        if (currentPage != null) {
            boolean isValidNavigation = switch (currentPage) {
                case "/schedule-event" -> page.equals("/schedule-event-step2");
                case "/schedule-event-step2" -> page.equals("/schedule-event-step3") || page.equals("/schedule-event");
                case "/schedule-event-step3" -> page.equals("/schedule-event-step2");
                default -> false;
            };

            if (!isValidNavigation) {
                log.warn("Invalid navigation from {} to {}", currentPage, page);
                return;
            }

            if (!currentPage.equals(page)) {
                navigationHistory.add(currentPage);
                log.info("Added {} to history. New history: {}", currentPage, navigationHistory);
            }
        }
        currentPage = page;
    }

    public WhenWoodleViewMvcAction user_visits_root_path() throws Exception {
        log.info("Performing GET /");
        resultAction = mockMvc.perform(get("/"));
        return self();
    }

    public WhenWoodleViewMvcAction user_clicks_schedule_event_button() throws Exception {
        log.info("Performing GET /schedule-event");
        resultAction = mockMvc.perform(get("/schedule-event").session(session));
        return self();
    }

    public WhenWoodleViewMvcAction user_sets_input_fields_on_schedule_event_page_and_clicks_next() throws Exception {
        log.info("Setting input fields on schedule event page and submitting");
        resultAction = mockMvc.perform(post("/schedule-event")
                .param("name", name)
                .param("email", email)
                .param("title", title)
                .param("description", description)
                .session(session));
        return self();
    }

    public WhenWoodleViewMvcAction user_clicks_back() throws Exception {
        // Get the current page content
        MvcResult currentResult = resultAction.andReturn();
        String content = currentResult.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Find the back button specifically by looking for the button that contains
        // "Back" text
        Elements backButtons = doc.select("button[type=button]:contains(Back)");
        assertThat(backButtons).isNotEmpty();
        assertThat(backButtons.attr("onclick")).isEqualTo("window.history.back()");

        // Simulate browser's back behavior by going to the previous page in our history
        if (!navigationHistory.isEmpty()) {
            String previousPage = navigationHistory.remove(navigationHistory.size() - 1);
            log.info("Navigating back to: {}", previousPage);
            resultAction = mockMvc.perform(get(previousPage).session(session));
        } else {
            // If no history, go to the first step
            log.info("No history, going to first step: /schedule-event");
            resultAction = mockMvc.perform(get("/schedule-event").session(session));
        }
        return self();
    }

    public WhenWoodleViewMvcAction user_sets_input_fields_on_schedule_event_step2_and_clicks_next(
            Map<Integer, de.bas.bodo.woodle.adapter.web.TimeSlot> timeSlots) throws Exception {
        log.info("Setting input fields on schedule event step2 and submitting with {} time slots", timeSlots.size());
        MockHttpServletRequestBuilder requestBuilder = post("/schedule-event-step2").session(session);

        for (Map.Entry<Integer, de.bas.bodo.woodle.adapter.web.TimeSlot> entry : timeSlots.entrySet()) {
            int index = entry.getKey();
            de.bas.bodo.woodle.adapter.web.TimeSlot slot = entry.getValue();
            requestBuilder
                    .param("date" + index, slot.date())
                    .param("startTime" + index, slot.startTime())
                    .param("endTime" + index, slot.endTime());
        }

        resultAction = mockMvc.perform(requestBuilder);
        return self();
    }

    public WhenWoodleViewMvcAction user_sets_input_fields_on_schedule_event_step2_and_clicks_next() throws Exception {
        Map<Integer, de.bas.bodo.woodle.adapter.web.TimeSlot> defaultTimeSlots = Map.of(
                0, new de.bas.bodo.woodle.adapter.web.TimeSlot("2024-03-20", "10:00", "11:00"));
        return user_sets_input_fields_on_schedule_event_step2_and_clicks_next(defaultTimeSlots);
    }

    public WhenWoodleViewMvcAction user_sets_time_slot_fields(String date, String startTime, String endTime)
            throws Exception {
        log.info("Setting time slot fields: date={}, startTime={}, endTime={}", date, startTime, endTime);
        resultAction = mockMvc.perform(post("/schedule-event-step2")
                .param("date0", date)
                .param("startTime0", startTime)
                .param("endTime0", endTime)
                .session(session));
        return self();
    }

    public WhenWoodleViewMvcAction user_sets_input_fields_on_schedule_event_step3_and_clicks_next() throws Exception {
        log.info("Setting input fields on schedule event step3 and submitting");
        resultAction = mockMvc.perform(post("/schedule-event-step3")
                .param("expiryDate", "2024-06-20")
                .session(session));
        return self();
    }

    public WhenWoodleViewMvcAction user_accesses_event_url_without_session(String uuid) throws Exception {
        resultAction = mockMvc.perform(get("/event/" + uuid));
        return self();
    }

    public WhenWoodleViewMvcAction user_clicks_plus_button() throws Exception {
        log.info("Clicking plus button to add new time slot");
        resultAction = mockMvc.perform(post("/schedule-event-step2/add-time-slot")
                .param("date", "2024-03-20")
                .param("startTime", "10:00")
                .param("endTime", "11:00")
                .session(session));
        return self();
    }
}

class ThenWoodleViewMvcOutcome extends Stage<ThenWoodleViewMvcOutcome> {
    private static final Logger log = LoggerFactory.getLogger(ThenWoodleViewMvcOutcome.class);

    @ScenarioState
    private MockMvc mockMvc;
    @ScenarioState
    private MockHttpSession session;
    @ScenarioState
    private ResultActions resultAction;
    @ScenarioState
    private String name;
    @ScenarioState
    private String email;
    @ScenarioState
    private String title;
    @ScenarioState
    private String description;
    @ScenarioState
    private List<String> navigationHistory;
    @ScenarioState
    private String currentPage;
    @ScenarioState
    private S3Client s3Client;

    private void addToHistory(String page) {
        log.info("Adding to history - Current page: {}, New page: {}, History size: {}",
                currentPage, page, navigationHistory.size());

        // Validate navigation path
        if (currentPage != null) {
            boolean isValidNavigation = switch (currentPage) {
                case "/schedule-event" -> page.equals("/schedule-event-step2");
                case "/schedule-event-step2" -> page.equals("/schedule-event-step3") || page.equals("/schedule-event");
                case "/schedule-event-step3" -> page.equals("/schedule-event-step2");
                default -> false;
            };

            if (!isValidNavigation) {
                log.warn("Invalid navigation from {} to {}", currentPage, page);
                return;
            }

            if (!currentPage.equals(page)) {
                navigationHistory.add(currentPage);
                log.info("Added {} to history. New history: {}", currentPage, navigationHistory);
            }
        }
        currentPage = page;
    }

    public ThenWoodleViewMvcOutcome user_should_be_redirected_to_index_html() throws Exception {
        log.info("Verifying redirect to index.html");
        resultAction.andExpect(status().isFound())
                .andExpect(redirectedUrl("/index.html"));
        addToHistory("/index.html");
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_step2_form() throws Exception {
        log.info("Verifying redirect to step2 form");
        resultAction.andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/schedule-event-step2"));
        // Follow the redirect
        log.info("Following redirect to /schedule-event-step2");
        resultAction = mockMvc.perform(get("/schedule-event-step2").session(session));
        resultAction.andExpect(status().isOk());
        addToHistory("/schedule-event-step2");
        return self();
    }

    public ThenWoodleViewMvcOutcome step2_form_should_have_all_required_fields() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(content);
        // Check for indexed date input (at least one time slot should exist)
        org.jsoup.select.Elements dateInputs = doc.select("input[type=date][id=date0][name=date0]");
        org.jsoup.select.Elements startTimeInputs = doc.select("input[type=time][id=startTime0][name=startTime0]");
        org.jsoup.select.Elements endTimeInputs = doc.select("input[type=time][id=endTime0][name=endTime0]");
        org.jsoup.select.Elements backButtons = doc.select("button[type=button]:contains(Back)");
        org.jsoup.select.Elements nextButtons = doc.select("button[type=submit]");
        org.assertj.core.api.Assertions.assertThat(dateInputs.size()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(startTimeInputs.size()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(endTimeInputs.size()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(backButtons.text()).containsIgnoringCase("back");
        org.assertj.core.api.Assertions.assertThat(nextButtons.text()).containsIgnoringCase("next");
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_step1_form_with_previous_data() throws Exception {
        log.info("Verifying step1 form with previous data");
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("value=\"" + name + "\"")
                .contains("value=\"" + email + "\"")
                .contains("value=\"" + title + "\"")
                .contains(description);
        addToHistory("/schedule-event");
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_step3_form() throws Exception {
        log.info("Verifying redirect to step3 form");
        resultAction.andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/schedule-event-step3"));
        // Follow the redirect
        log.info("Following redirect to /schedule-event-step3");
        resultAction = mockMvc.perform(get("/schedule-event-step3").session(session));
        resultAction.andExpect(status().isOk());
        addToHistory("/schedule-event-step3");
        return self();
    }

    public ThenWoodleViewMvcOutcome step3_form_should_have_expiry_date(String expectedDate) throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);
        String expiryValue = doc.select("input[type=date][id=expiryDate][name=expiryDate]").attr("value");
        assertThat(expiryValue).isEqualTo(expectedDate);
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_step2_form_with_previous_data() throws Exception {
        return step2_form_should_show_previous_data(List.of(new TimeSlot("2024-03-20", "10:00", "11:00")));
    }

    public ThenWoodleViewMvcOutcome user_should_see_summary_page() throws Exception {
        log.info("Verifying summary page");
        resultAction.andExpect(status().isOk());
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);
        assertThat(doc.select("h1:contains(Poll Summary)").size()).isEqualTo(1);
        addToHistory("/event-summary");
        return self();
    }

    public ThenWoodleViewMvcOutcome summary_page_should_show_all_entered_data() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        assertThat(content)
                .contains(name)
                .contains(email)
                .contains(title)
                .contains(description)
                .contains("2024-03-20")
                .contains("10:00")
                .contains("11:00")
                .contains("2024-06-20");
        return self();
    }

    public ThenWoodleViewMvcOutcome summary_page_should_show_event_url() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);
        Elements eventUrl = doc.select("div[data-test-section='poll-url'] div.poll-url");
        assertThat(eventUrl.size()).isEqualTo(1);
        assertThat(eventUrl.text()).matches(
                "http://localhost:8080/event/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        return self();
    }

    public ThenWoodleViewMvcOutcome poll_should_be_stored_in_s3() throws Exception {
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        return self();
    }

    public ThenWoodleViewMvcOutcome step2_form_should_have_plus_button() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Check for plus button
        Elements plusButtons = doc.select("button[type=button].add-time-slot");
        assertThat(plusButtons.size()).isEqualTo(1);

        // Check for plus image with alt text '+'
        Elements plusImages = plusButtons.select("img[src*='Plus-Symbol-Transparent-small.png'][alt='+']");
        assertThat(plusImages.size()).isEqualTo(1);

        return self();
    }

    public ThenWoodleViewMvcOutcome step2_form_should_show_previous_data(
            List<de.bas.bodo.woodle.adapter.web.TimeSlot> expectedTimeSlots)
            throws Exception {
        log.info("Verifying step2 form with previous data");
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        for (int i = 0; i < expectedTimeSlots.size(); i++) {
            de.bas.bodo.woodle.adapter.web.TimeSlot expectedSlot = expectedTimeSlots.get(i);
            Element dateInput = doc.select("input[data-test='date-" + i + "']").first();
            Element startTimeInput = doc.select("input[data-test='startTime-" + i + "']").first();
            Element endTimeInput = doc.select("input[data-test='endTime-" + i + "']").first();

            assertThat(dateInput.attr("value")).isEqualTo(expectedSlot.date());
            assertThat(startTimeInput.attr("value")).isEqualTo(expectedSlot.startTime());
            assertThat(endTimeInput.attr("value")).isEqualTo(expectedSlot.endTime());
        }

        return self();
    }

    public ThenWoodleViewMvcOutcome step2_form_should_preserve_all_time_slots() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Check that we have two sets of date/time input fields
        Elements dateInputs = doc.select("input[data-test^='date-']");
        Elements startTimeInputs = doc.select("input[data-test^='startTime-']");
        Elements endTimeInputs = doc.select("input[data-test^='endTime-']");

        assertThat(dateInputs.size()).isEqualTo(2);
        assertThat(startTimeInputs.size()).isEqualTo(2);
        assertThat(endTimeInputs.size()).isEqualTo(2);

        // Check that first slot has the existing data
        assertThat(dateInputs.get(0).attr("value")).isEqualTo("2024-03-20");
        assertThat(startTimeInputs.get(0).attr("value")).isEqualTo("10:00");
        assertThat(endTimeInputs.get(0).attr("value")).isEqualTo("11:00");

        // Check that second slot is empty (for new input)
        assertThat(dateInputs.get(1).attr("value")).isEmpty();
        assertThat(startTimeInputs.get(1).attr("value")).isEmpty();
        assertThat(endTimeInputs.get(1).attr("value")).isEmpty();

        return self();
    }

    public ThenWoodleViewMvcOutcome step2_form_should_have_three_time_slots() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Check that we have three sets of date/time input fields
        Elements dateInputs = doc.select("input[data-test^='date-']");
        Elements startTimeInputs = doc.select("input[data-test^='startTime-']");
        Elements endTimeInputs = doc.select("input[data-test^='endTime-']");

        assertThat(dateInputs.size()).isEqualTo(3);
        assertThat(startTimeInputs.size()).isEqualTo(3);
        assertThat(endTimeInputs.size()).isEqualTo(3);

        return self();
    }

    public ThenWoodleViewMvcOutcome step2_form_should_show_previous_data() throws Exception {
        return step2_form_should_show_previous_data(
                List.of(new de.bas.bodo.woodle.adapter.web.TimeSlot("2024-03-20", "10:00", "11:00")));
    }

    public ThenWoodleViewMvcOutcome step2_form_should_have_additional_time_slot_fields() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Check that we have two sets of date/time input fields
        Elements dateInputs = doc.select("input[data-test^='date-']");
        Elements startTimeInputs = doc.select("input[data-test^='startTime-']");
        Elements endTimeInputs = doc.select("input[data-test^='endTime-']");

        assertThat(dateInputs.size()).isEqualTo(2);
        assertThat(startTimeInputs.size()).isEqualTo(2);
        assertThat(endTimeInputs.size()).isEqualTo(2);

        // Check that the second set of fields is empty (for new input)
        Element secondDateInput = dateInputs.get(1);
        Element secondStartTimeInput = startTimeInputs.get(1);
        Element secondEndTimeInput = endTimeInputs.get(1);

        assertThat(secondDateInput.attr("value")).isEmpty();
        assertThat(secondStartTimeInput.attr("value")).isEmpty();
        assertThat(secondEndTimeInput.attr("value")).isEmpty();

        return self();
    }

    public ThenWoodleViewMvcOutcome step2_form_should_have_empty_slot(int slotIndex) throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Check specified time slot is empty
        Element dateInput = doc.select("input[data-test='date-" + slotIndex + "']").first();
        Element startTimeInput = doc.select("input[data-test='startTime-" + slotIndex + "']").first();
        Element endTimeInput = doc.select("input[data-test='endTime-" + slotIndex + "']").first();

        assertThat(dateInput.attr("value")).isEmpty();
        assertThat(startTimeInput.attr("value")).isEmpty();
        assertThat(endTimeInput.attr("value")).isEmpty();

        return self();
    }
}