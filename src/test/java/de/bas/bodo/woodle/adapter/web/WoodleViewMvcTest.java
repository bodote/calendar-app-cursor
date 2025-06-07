package de.bas.bodo.woodle.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    public WhenWoodleViewMvcAction user_visits_root_path() throws Exception {
        resultAction = mockMvc.perform(get("/"));
        return self();
    }

    public WhenWoodleViewMvcAction user_clicks_schedule_event_button() throws Exception {
        resultAction = mockMvc.perform(get("/schedule-event").session(session));
        return self();
    }

    public WhenWoodleViewMvcAction user_fills_step1_form() throws Exception {
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
        resultAction = mockMvc.perform(get("/schedule-event").session(session));
        return self();
    }

    public WhenWoodleViewMvcAction user_fills_step2_form(String date, String startTime, String endTime)
            throws Exception {
        resultAction = mockMvc.perform(post("/schedule-event-step2")
                .param("date", date)
                .param("startTime", startTime)
                .param("endTime", endTime)
                .session(session));
        return self();
    }

    public WhenWoodleViewMvcAction user_fills_step3_form(String expiryDate) throws Exception {
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

    public ThenWoodleViewMvcOutcome user_should_be_redirected_to_index_html() throws Exception {
        resultAction.andExpect(status().isFound())
                .andExpect(redirectedUrl("/index.html"));
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_step2_form() throws Exception {
        resultAction.andExpect(status().isOk());
        return self();
    }

    public ThenWoodleViewMvcOutcome step2_form_should_have_all_required_fields() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("input[type=date][id=date][name=date]")
                .contains("input[type=time][id=startTime][name=startTime]")
                .contains("input[type=time][id=endTime][name=endTime]");
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_step1_form_with_previous_data() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("value=\"" + name + "\"")
                .contains("value=\"" + email + "\"")
                .contains("value=\"" + title + "\"")
                .contains(description);
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_step3_form() throws Exception {
        resultAction.andExpect(status().isOk());
        return self();
    }

    public ThenWoodleViewMvcOutcome step3_form_should_have_expiry_date(String expectedDate) throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("input[type=date][id=expiryDate][name=expiryDate]")
                .contains("value=\"" + expectedDate + "\"");
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_step2_form_with_previous_data() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        assertThat(content).contains("value=\"2024-03-20\"")
                .contains("value=\"10:00\"")
                .contains("value=\"11:00\"");
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_summary_page() throws Exception {
        resultAction.andExpect(status().isOk());
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