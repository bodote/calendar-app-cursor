package de.bas.bodo.woodle.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ScenarioStage;
import com.tngtech.jgiven.annotation.ScenarioState;
import com.tngtech.jgiven.junit5.ScenarioTest;

import de.bas.bodo.woodle.config.WebMvcTestConfig;
import de.bas.bodo.woodle.domain.service.PollStorageService;
import de.bas.bodo.woodle.view.WoodleViewController;
import gg.jte.springframework.boot.autoconfigure.JteAutoConfiguration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@WebMvcTest({ WoodleViewController.class, ScheduleEventController.class })
@ImportAutoConfiguration(JteAutoConfiguration.class)
@Import({ WebMvcTestConfig.class, PollStorageService.class })
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
        givenWoodleViewMvcState.user_is_on_homepage();
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
                .and().user_fills_step1_form()
                .and().user_clicks_next();
        then().user_should_see_step2_form()
                .and().step2_form_should_have_all_required_fields();
        when().user_clicks_back();
        then().user_should_see_step1_form_with_previous_data();
        when().user_fills_step2_form("2024-03-20", "10:00", "11:00")
                .and().user_clicks_next();
        then().user_should_see_step3_form()
                .and().step3_form_should_have_expiry_date("2024-06-20");
        when().user_clicks_back();
        then().user_should_see_step2_form_with_previous_data();
        when().user_fills_step3_form("2024-06-20")
                .and().user_clicks_create_poll();
        then().user_should_see_summary_page()
                .and().summary_page_should_show_all_entered_data()
                .and().summary_page_should_show_event_url();
    }

    @Test
    @DisplayName("Should display summary and store in S3")
    void should_display_summary_and_store_in_s3() throws Exception {
        // Given
        String name = "John Doe";
        String email = "john@example.com";
        String title = "Team Meeting";
        String description = "Weekly sync";
        String date = "2024-03-20";
        String startTime = "10:00";
        String endTime = "11:00";
        String expiryDate = "2024-06-20";

        given().mock_mvc_is_configured(mockMvc);

        // When
        mockMvc.perform(post("/schedule-event")
                .param("name", name)
                .param("email", email)
                .param("title", title)
                .param("description", description)
                .session(session))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/schedule-event-step2"));

        mockMvc.perform(post("/schedule-event-step2")
                .param("date", date)
                .param("startTime", startTime)
                .param("endTime", endTime)
                .session(session))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/schedule-event-step3"));

        MvcResult result = mockMvc.perform(post("/schedule-event-step3")
                .param("expiryDate", expiryDate)
                .session(session))
                .andExpect(status().isOk())
                .andReturn();

        // Log the response content for debugging
        log.info("Response content: {}", result.getResponse().getContentAsString());

        // Verify the response contains expected content
        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent)
                .contains(name)
                .contains(email)
                .contains(title)
                .contains(description)
                .contains(date)
                .contains(startTime)
                .contains(endTime)
                .contains(expiryDate);

        // Then: accessing /event-summary with a new session should redirect to /
        mockMvc.perform(get("/event-summary"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));

        // Verify S3 storage
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
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

    public GivenWoodleViewMvcState mock_mvc_is_configured(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
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

    public WhenWoodleViewMvcAction user_fills_step1_form() throws Exception {
        log.info("Performing POST /schedule-event with form data");
        resultAction = mockMvc.perform(post("/schedule-event")
                .param("name", name)
                .param("email", email)
                .param("title", title)
                .param("description", description)
                .session(session));
        return self();
    }

    public WhenWoodleViewMvcAction user_clicks_next() throws Exception {
        // The next action depends on the current step
        // This will be handled by the specific form submission methods
        return self();
    }

    public WhenWoodleViewMvcAction user_clicks_back() throws Exception {
        // Get the current page content
        MvcResult currentResult = resultAction.andReturn();
        String content = currentResult.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Find the back button and verify it has the correct onclick handler
        Elements backButtons = doc.select("button[type=button]");
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

    public WhenWoodleViewMvcAction user_fills_step2_form(String date, String startTime, String endTime)
            throws Exception {
        log.info("Performing POST /schedule-event-step2 with form data");
        resultAction = mockMvc.perform(post("/schedule-event-step2")
                .param("date", date)
                .param("startTime", startTime)
                .param("endTime", endTime)
                .session(session));
        return self();
    }

    public WhenWoodleViewMvcAction user_fills_step3_form(String expiryDate) throws Exception {
        log.info("Performing POST /schedule-event-step3 with form data");
        resultAction = mockMvc.perform(post("/schedule-event-step3")
                .param("expiryDate", expiryDate)
                .session(session));
        return self();
    }

    public WhenWoodleViewMvcAction user_clicks_create_poll() throws Exception {
        // The form submission is handled in user_fills_step3_form
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
        // Check for date input
        org.jsoup.select.Elements dateInputs = doc.select("input[type=date][id=date][name=date]");
        org.jsoup.select.Elements startTimeInputs = doc.select("input[type=time][id=startTime][name=startTime]");
        org.jsoup.select.Elements endTimeInputs = doc.select("input[type=time][id=endTime][name=endTime]");
        org.jsoup.select.Elements backButtons = doc.select("button[type=button]");
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
        log.info("Verifying step2 form with previous data");
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);
        assertThat(doc.select("input[name=date]").attr("value")).isEqualTo("2024-03-20");
        assertThat(doc.select("input[name=startTime]").attr("value")).isEqualTo("10:00");
        assertThat(doc.select("input[name=endTime]").attr("value")).isEqualTo("11:00");
        addToHistory("/schedule-event-step2");
        return self();
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
        assertThat(content).contains("http://localhost:8080/poll/");
        return self();
    }
}