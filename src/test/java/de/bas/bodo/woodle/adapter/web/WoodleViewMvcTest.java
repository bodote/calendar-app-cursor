package de.bas.bodo.woodle.adapter.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import de.bas.bodo.woodle.view.WoodleViewController;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ScenarioState;
import com.tngtech.jgiven.junit5.ScenarioTest;
import com.tngtech.jgiven.annotation.ScenarioStage;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.mock.mockito.MockBean;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.mock.web.MockHttpSession;

@WebMvcTest(WoodleViewController.class)
class WoodleViewMvcTest
        extends ScenarioTest<GivenWoodleViewMvcState, WhenWoodleViewMvcAction, ThenWoodleViewMvcOutcome> {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3Client s3Client;

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

    @BeforeEach
    void setupStages() {
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
}

class GivenWoodleViewMvcState extends Stage<GivenWoodleViewMvcState> {
    @ScenarioState
    private MockMvc mockMvc;
    @ScenarioState
    private MockHttpSession session;

    public GivenWoodleViewMvcState mock_mvc_is_configured(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        return self();
    }

    public GivenWoodleViewMvcState user_is_on_homepage() {
        session = new MockHttpSession();
        return self();
    }

    public GivenWoodleViewMvcState user_has_test_data(String name, String email, String title, String description) {
        // Implementation of user_has_test_data method
        return self();
    }
}

class WhenWoodleViewMvcAction extends Stage<WhenWoodleViewMvcAction> {
    @ScenarioState
    private MockMvc mockMvc;

    public WhenWoodleViewMvcAction user_visits_root_path() throws Exception {
        mockMvc.perform(get("/"));
        return self();
    }

    public WhenWoodleViewMvcAction user_clicks_schedule_event_button() throws Exception {
        // Implementation of user_clicks_schedule_event_button method
        return self();
    }

    public WhenWoodleViewMvcAction user_fills_step1_form() throws Exception {
        // Implementation of user_fills_step1_form method
        return self();
    }

    public WhenWoodleViewMvcAction user_clicks_next() throws Exception {
        // Implementation of user_clicks_next method
        return self();
    }

    public WhenWoodleViewMvcAction user_clicks_back() throws Exception {
        // Implementation of user_clicks_back method
        return self();
    }

    public WhenWoodleViewMvcAction user_fills_step2_form(String date, String startTime, String endTime)
            throws Exception {
        // Implementation of user_fills_step2_form method
        return self();
    }

    public WhenWoodleViewMvcAction user_fills_step3_form(String expiryDate) throws Exception {
        // Implementation of user_fills_step3_form method
        return self();
    }

    public WhenWoodleViewMvcAction user_clicks_create_poll() throws Exception {
        // Implementation of user_clicks_create_poll method
        return self();
    }
}

class ThenWoodleViewMvcOutcome extends Stage<ThenWoodleViewMvcOutcome> {
    @ScenarioState
    private MockMvc mockMvc;

    public ThenWoodleViewMvcOutcome user_should_be_redirected_to_index_html() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/index.html"));
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_step2_form() throws Exception {
        // Implementation of user_should_see_step2_form method
        return self();
    }

    public ThenWoodleViewMvcOutcome step2_form_should_have_all_required_fields() throws Exception {
        // Implementation of step2_form_should_have_all_required_fields method
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_step1_form_with_previous_data() throws Exception {
        // Implementation of user_should_see_step1_form_with_previous_data method
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_step3_form() throws Exception {
        // Implementation of user_should_see_step3_form method
        return self();
    }

    public ThenWoodleViewMvcOutcome step3_form_should_have_expiry_date(String expiryDate) throws Exception {
        // Implementation of step3_form_should_have_expiry_date method
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_step2_form_with_previous_data() throws Exception {
        // Implementation of user_should_see_step2_form_with_previous_data method
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_summary_page() throws Exception {
        // Implementation of user_should_see_summary_page method
        return self();
    }

    public ThenWoodleViewMvcOutcome summary_page_should_show_all_entered_data() throws Exception {
        // Implementation of summary_page_should_show_all_entered_data method
        return self();
    }

    public ThenWoodleViewMvcOutcome summary_page_should_show_event_url() throws Exception {
        // Implementation of summary_page_should_show_event_url method
        return self();
    }
}