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
import org.mockito.ArgumentCaptor;
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
import de.bas.bodo.woodle.view.TimeSlot;
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

    // Additional common test data - made public static for inner class access
    public static final String JOHN_DOE_NAME = "John Doe";
    public static final String JOHN_DOE_EMAIL = "john@example.com";
    public static final String TEAM_MEETING_TITLE = "Team Meeting";
    public static final String WEEKLY_SYNC_DESCRIPTION = "Weekly sync";

    public static final String JANE_SMITH_NAME = "Jane Smith";
    public static final String JANE_SMITH_EMAIL = "jane@example.com";
    public static final String PROJECT_PLANNING_TITLE = "Project Planning";
    public static final String QUARTERLY_PLANNING_DESCRIPTION = "Quarterly planning session";

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
                .and().summary_page_should_show_all_entered_data(Map.of(
                        0, new TimeSlot("2024-03-20", "10:00", "11:00"),
                        1, new TimeSlot("2024-03-21", "14:00", "15:00")));
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
                        0, new TimeSlot("2024-03-20", "10:00", "11:00")))
                .and().user_clicks_plus_button();
        then().user_should_see_step2_form()
                .and().step2_form_should_have_plus_button()
                .and().step2_form_should_show_previous_data(Map.of(
                        0, new TimeSlot("2024-03-20", "10:00", "11:00")))
                .and().step2_form_should_have_additional_time_slot_fields()
                .and().step2_form_should_have_empty_slot(1);
        when().user_sets_input_fields_on_schedule_event_step2_and_clicks_next(Map.of(
                0, new TimeSlot("2024-03-21", "14:00", "15:00"),
                1, new TimeSlot("2024-03-20", "10:00", "11:00")))
                .and().user_clicks_plus_button();
        then().user_should_see_step2_form()
                .and().step2_form_should_have_plus_button()
                .and().step2_form_should_show_previous_data(Map.of(
                        0, new TimeSlot("2024-03-21", "14:00", "15:00"),
                        1, new TimeSlot("2024-03-20", "10:00", "11:00")))
                .and().step2_form_should_have_three_time_slots()
                .and().step2_form_should_have_empty_slot(2);
    }

    @Test
    @DisplayName("Should ignore empty time slots when submitting from step 2")
    void should_ignore_empty_time_slots_when_submitting() throws Exception {
        given().mock_mvc_is_configured(mockMvc)
                .and().user_is_on_homepage()
                .and().user_has_test_data(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION);
        when().user_clicks_schedule_event_button()
                .and().user_sets_input_fields_on_schedule_event_page_and_clicks_next()
                .and().user_sets_input_fields_on_schedule_event_step2_and_clicks_next(Map.of(
                        0, new TimeSlot("2024-03-20", "10:00", "11:00"),
                        1, new TimeSlot("", "", "")))
                .and().user_clicks_plus_button();
        then().user_should_see_step2_form()
                .and().only_one_time_slot_should_be_in_session();
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
                        0, new TimeSlot("2024-03-20", "10:00", "11:00")))
                .and().user_clicks_plus_button();
        then().user_should_see_step2_form()
                .and().step2_form_should_have_plus_button()
                .and().step2_form_should_show_previous_data()
                .and().step2_form_should_have_additional_time_slot_fields()
                .and().step2_form_should_preserve_all_time_slots();
    }

    @Test
    @DisplayName("Should display all entered time slots on the summary page")
    void should_display_all_entered_time_slots_on_summary_page() throws Exception {
        given().mock_mvc_is_configured(mockMvc)
                .and().s3_client_is_configured(s3Client)
                .and().user_is_on_homepage()
                .and().user_has_test_data("John Doe", "john@example.com", "Team Meeting", "Weekly sync");
        when().user_clicks_schedule_event_button()
                .and().user_sets_input_fields_on_schedule_event_page_and_clicks_next()
                .and().user_sets_input_fields_on_schedule_event_step2_and_clicks_next(Map.of(
                        0, new TimeSlot("2024-03-20", "10:00", "11:00"),
                        1, new TimeSlot("2024-03-21", "14:00", "15:00"),
                        2, new TimeSlot("2024-03-22", "16:00", "17:00")))
                .and().user_sets_input_fields_on_schedule_event_step3_and_clicks_next();
        then().user_should_see_summary_page()
                .and().summary_page_should_show_all_entered_data(Map.of(
                        0, new TimeSlot("2024-03-20", "10:00", "11:00"),
                        1, new TimeSlot("2024-03-21", "14:00", "15:00"),
                        2, new TimeSlot("2024-03-22", "16:00", "17:00")));
    }

    @Test
    @DisplayName("Should save and reload multiple time slots correctly")
    void should_save_and_reload_multiple_time_slots() throws Exception {
        given().mock_mvc_is_configured(mockMvc)
                .and().s3_client_is_configured(s3Client)
                .and().user_is_on_homepage()
                .and().user_has_test_data("John Doe", "john@example.com", "Team Meeting", "Weekly sync");
        when().user_clicks_schedule_event_button()
                .and().user_sets_input_fields_on_schedule_event_page_and_clicks_next()
                .and().user_sets_input_fields_on_schedule_event_step2_and_clicks_next(Map.of(
                        0, new TimeSlot("2024-03-20", "10:00", "11:00"),
                        1, new TimeSlot("2024-03-21", "14:00", "15:00")))
                .and().user_sets_input_fields_on_schedule_event_step3_and_clicks_next()
                .and().user_captures_event_url_and_reloads_in_new_session();
        then().user_should_see_summary_page()
                .and().summary_page_should_show_all_entered_data(Map.of(
                        0, new TimeSlot("2024-03-20", "10:00", "11:00"),
                        1, new TimeSlot("2024-03-21", "14:00", "15:00")));
    }

    @Test
    @DisplayName("Should display proposed events in a table with grouped dates and ordered time slots")
    void should_display_proposed_events_in_table_with_grouped_dates() throws Exception {
        String testUuid = "123e4567-e89b-12d3-a456-426614174001";
        given().mock_mvc_is_configured(mockMvc)
                .and().s3_client_is_configured(s3Client)
                .and().s3_client_returns_three_test_events_for_uuid(testUuid);
        when().user_accesses_event_url_without_session(testUuid);
        then().user_should_see_summary_page()
                .and().summary_page_should_have_events_table()
                .and().events_table_should_have_column_headers()
                .and().events_table_should_group_dates_together()
                .and().events_table_should_order_dates_and_times_correctly()
                .and().events_table_should_have_empty_name_input_field();
    }

    @Test
    @DisplayName("Should save participant name and time slot selections when save button is clicked")
    void should_save_participant_selections_when_save_button_clicked() throws Exception {
        String testUuid = "123e4567-e89b-12d3-a456-426614174002";
        given().mock_mvc_is_configured(mockMvc)
                .and().s3_client_is_configured(s3Client)
                .and().s3_client_returns_three_test_events_for_uuid(testUuid);
        when().user_accesses_event_url_without_session(testUuid)
                .and().user_saves_participant_with_selections("Alice", testUuid, Map.of(
                        0, true, // 2024-03-15 09:00-10:00
                        1, false, // 2024-03-15 14:00-15:00
                        2, true // 2024-03-18 11:00-12:00
                ));
        then().participant_selection_should_be_saved_to_poll()
                .and().events_table_should_show_fixed_participant_row("Alice", Map.of(
                        0, true,
                        1, false,
                        2, true))
                .and().events_table_should_have_new_empty_participant_row()
                .and().save_button_should_be_present();
    }

    @Test
    @DisplayName("Should allow multiple participants to save their selections and create multiple fixed rows")
    void should_allow_multiple_participants_to_save_selections() throws Exception {
        String testUuid = "123e4567-e89b-12d3-a456-426614174003";
        given().mock_mvc_is_configured(mockMvc)
                .and().s3_client_is_configured(s3Client)
                .and().s3_client_returns_three_test_events_for_uuid(testUuid);
        when().user_accesses_event_url_without_session(testUuid)
                .and().user_saves_participant_with_selections("Alice", testUuid, Map.of(
                        0, true, // 2024-03-15 09:00-10:00
                        1, false, // 2024-03-15 14:00-15:00
                        2, true // 2024-03-18 11:00-12:00
                ))
                .and().user_saves_participant_with_selections("Bob", testUuid, Map.of(
                        0, false, // 2024-03-15 09:00-10:00
                        1, true, // 2024-03-15 14:00-15:00
                        2, false // 2024-03-18 11:00-12:00
                ));
        then().participant_selection_should_be_saved_to_poll()
                .and().events_table_should_show_multiple_fixed_participant_rows(Map.of(
                        "Alice", Map.of(0, true, 1, false, 2, true),
                        "Bob", Map.of(0, false, 1, true, 2, false)))
                .and().events_table_should_have_new_empty_participant_row()
                .and().save_button_should_be_present();
    }

    @Test
    @DisplayName("Should allow adding 3 time slots step by step using plus button")
    void should_allow_adding_three_time_slots_step_by_step() throws Exception {
        given().mock_mvc_is_configured(mockMvc)
                .and().user_is_on_homepage()
                .and().user_has_test_data(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION);
        when().user_clicks_schedule_event_button()
                .and().user_sets_input_fields_on_schedule_event_page_and_clicks_next()
                // Start with 1 time slot
                .and().user_sets_input_fields_on_schedule_event_step2_and_clicks_next(Map.of(
                        0, new TimeSlot("2024-03-20", "10:00", "11:00")))
                .and().user_clicks_plus_button();
        then().user_should_see_step2_form()
                .and().step2_form_should_have_plus_button()
                .and().step2_form_should_show_previous_data(Map.of(
                        0, new TimeSlot("2024-03-20", "10:00", "11:00")))
                .and().step2_form_should_have_additional_time_slot_fields()
                .and().step2_form_should_have_empty_slot(1);
        // Add second time slot
        when().user_sets_input_fields_on_schedule_event_step2_and_clicks_next(Map.of(
                0, new TimeSlot("2024-03-20", "10:00", "11:00"),
                1, new TimeSlot("2024-03-21", "14:00", "15:00")))
                .and().user_clicks_plus_button();
        then().user_should_see_step2_form()
                .and().step2_form_should_have_plus_button()
                .and().step2_form_should_show_previous_data(Map.of(
                        0, new TimeSlot("2024-03-20", "10:00", "11:00"),
                        1, new TimeSlot("2024-03-21", "14:00", "15:00")))
                .and().step2_form_should_have_three_time_slots()
                .and().step2_form_should_have_empty_slot(2);
        // Add third time slot
        when().user_sets_input_fields_on_schedule_event_step2_and_clicks_next(Map.of(
                0, new TimeSlot("2024-03-20", "10:00", "11:00"),
                1, new TimeSlot("2024-03-21", "14:00", "15:00"),
                2, new TimeSlot("2024-03-22", "16:00", "17:00")))
                .and().user_clicks_plus_button();
        then().user_should_see_step2_form()
                .and().step2_form_should_have_plus_button()
                .and().step2_form_should_show_previous_data(Map.of(
                        0, new TimeSlot("2024-03-20", "10:00", "11:00"),
                        1, new TimeSlot("2024-03-21", "14:00", "15:00"),
                        2, new TimeSlot("2024-03-22", "16:00", "17:00")))
                .and().step2_form_should_have_four_time_slots()
                .and().step2_form_should_have_empty_slot(3);
    }

    @Test
    @DisplayName("Should add a new empty row after saving a participant - verifying AC3 properly")
    void should_add_new_empty_row_after_saving_participant() throws Exception {
        String testUuid = "123e4567-e89b-12d3-a456-426614174004";

        given().mock_mvc_is_configured(mockMvc)
                .and().s3_client_is_configured(s3Client)
                .and().s3_client_returns_three_test_events_for_uuid(testUuid);

        when().user_accesses_event_url_without_session(testUuid)
                .and().user_saves_participant_with_selections("Alice", testUuid, Map.of(
                        0, true, // 2024-03-15 09:00-10:00
                        1, false // 2024-03-15 14:00-15:00
                ));

        then().events_table_should_have_exactly_two_participant_rows()
                .and().events_table_should_show_fixed_participant_row("Alice", Map.of(0, true, 1, false))
                .and().events_table_should_have_new_empty_participant_row();
    }

    @Test
    @DisplayName("Form action URL should be correct - should point to /event/{uuid}/participants/save")
    void form_action_should_be_correct() throws Exception {
        String testUuid = "test-uuid-123";

        given().mock_mvc_is_configured(mockMvc)
                .and().s3_client_is_configured(s3Client)
                .and().s3_client_returns_three_test_events_for_uuid(testUuid);

        when().user_accesses_event_url_without_session(testUuid);

        then().form_action_should_point_to_correct_participants_save_url(testUuid);
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
        return s3_client_returns_test_event_for_uuid_with_time_slots(uuid,
                WoodleViewMvcTest.JOHN_DOE_NAME,
                WoodleViewMvcTest.JOHN_DOE_EMAIL,
                WoodleViewMvcTest.TEAM_MEETING_TITLE,
                WoodleViewMvcTest.WEEKLY_SYNC_DESCRIPTION,
                List.of(
                        new PollData.EventTimeSlot(
                                LocalDate.parse("2024-03-20"),
                                LocalTime.parse("10:00"),
                                LocalTime.parse("11:00")),
                        new PollData.EventTimeSlot(
                                LocalDate.parse("2024-03-21"),
                                LocalTime.parse("14:00"),
                                LocalTime.parse("15:00"))),
                LocalDate.parse("2024-06-20"));
    }

    public GivenWoodleViewMvcState s3_client_returns_three_test_events_for_uuid(String uuid) throws Exception {
        return s3_client_returns_test_event_for_uuid_with_time_slots(uuid,
                WoodleViewMvcTest.JANE_SMITH_NAME,
                WoodleViewMvcTest.JANE_SMITH_EMAIL,
                WoodleViewMvcTest.PROJECT_PLANNING_TITLE,
                WoodleViewMvcTest.QUARTERLY_PLANNING_DESCRIPTION,
                List.of(
                        // Two events on the same date (2024-03-15) with different times
                        new PollData.EventTimeSlot(
                                LocalDate.parse("2024-03-15"),
                                LocalTime.parse("09:00"),
                                LocalTime.parse("10:00")),
                        new PollData.EventTimeSlot(
                                LocalDate.parse("2024-03-15"),
                                LocalTime.parse("14:00"),
                                LocalTime.parse("15:00")),
                        // One event on a different date (2024-03-18)
                        new PollData.EventTimeSlot(
                                LocalDate.parse("2024-03-18"),
                                LocalTime.parse("11:00"),
                                LocalTime.parse("12:00"))),
                LocalDate.parse("2024-06-15"));
    }

    public GivenWoodleViewMvcState s3_client_returns_test_event_for_uuid_with_time_slots(
            String uuid,
            String name,
            String email,
            String title,
            String description,
            List<PollData.EventTimeSlot> timeSlots,
            LocalDate expiryDate) throws Exception {
        PollData testEvent = new PollData(name, email, title, description, timeSlots, expiryDate, List.of());
        return s3_client_returns_poll_data_for_uuid(uuid, testEvent);
    }

    public GivenWoodleViewMvcState s3_client_returns_poll_data_for_uuid(String uuid, PollData pollData)
            throws Exception {
        String jsonData = objectMapper.writeValueAsString(pollData);
        GetObjectResponse response = GetObjectResponse.builder().build();

        // Use thenAnswer to return a fresh stream on each call
        org.mockito.Mockito.when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenAnswer(invocation -> new ResponseInputStream<>(
                        response,
                        new ByteArrayInputStream(jsonData.getBytes())));
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
    private S3Client s3Client;
    @ScenarioState
    private List<String> navigationHistory = new ArrayList<>();
    @ScenarioState
    private String currentPage;
    @ScenarioState
    private String eventUrl;

    @ScenarioState
    private ArgumentCaptor<RequestBody> s3RequestBodyCaptor;

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
            Map<Integer, TimeSlot> timeSlots) throws Exception {
        log.info("Setting input fields on schedule event step2 and submitting with {} time slots", timeSlots.size());
        MockHttpServletRequestBuilder requestBuilder = post("/schedule-event-step2").session(session);

        for (Map.Entry<Integer, TimeSlot> entry : timeSlots.entrySet()) {
            int index = entry.getKey();
            TimeSlot slot = entry.getValue();
            requestBuilder
                    .param("date" + index, slot.date())
                    .param("startTime" + index, slot.startTime())
                    .param("endTime" + index, slot.endTime());
        }

        resultAction = mockMvc.perform(requestBuilder);
        return self();
    }

    public WhenWoodleViewMvcAction user_sets_input_fields_on_schedule_event_step2_and_clicks_next() throws Exception {
        Map<Integer, TimeSlot> defaultTimeSlots = Map.of(
                0, new TimeSlot("2024-03-20", "10:00", "11:00"));
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

    public WhenWoodleViewMvcAction user_captures_event_url_and_reloads_in_new_session() throws Exception {
        // 1. Capture the event URL from the page
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);
        String url = doc.select("div[data-test-section='poll-url'] div.poll-url").text();
        assertThat(url).isNotBlank();
        this.eventUrl = url;

        // 2. Capture what was sent to S3
        s3RequestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(any(PutObjectRequest.class), s3RequestBodyCaptor.capture());

        // 3. Set up the S3 mock to return the captured data
        RequestBody capturedBody = s3RequestBodyCaptor.getValue();
        GetObjectResponse getResponse = GetObjectResponse.builder().build();
        ResponseInputStream<GetObjectResponse> responseStream = new ResponseInputStream<>(
                getResponse,
                new ByteArrayInputStream(capturedBody.contentStreamProvider().newStream().readAllBytes()));
        doReturn(responseStream).when(s3Client).getObject(any(GetObjectRequest.class));

        // 4. Access the URL in a new session
        resultAction = mockMvc.perform(get(this.eventUrl));
        return self();
    }

    public WhenWoodleViewMvcAction user_saves_participant_with_selections(String participantName, String eventUuid,
            Map<Integer, Boolean> timeSlots) throws Exception {
        log.info("Saving participant {} with time slot selections: {} for event {}", participantName, timeSlots,
                eventUuid);

        // Create the request to save participant with name and selected time slots
        MockHttpServletRequestBuilder requestBuilder = post("/event/" + eventUuid + "/participants/save")
                .param("participantName", participantName);

        // Add selected time slots as parameters
        for (Map.Entry<Integer, Boolean> entry : timeSlots.entrySet()) {
            int slotIndex = entry.getKey();
            boolean selected = entry.getValue();
            if (selected) {
                requestBuilder.param("selectedSlots", String.valueOf(slotIndex));
            }
        }

        resultAction = mockMvc.perform(requestBuilder);

        // Verify that the save action redirects to the correct event page
        resultAction.andExpect(status().isFound())
                .andExpect(redirectedUrl("/event/" + eventUuid));

        // Capture what was saved to S3 to update our mock
        ArgumentCaptor<RequestBody> requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(any(PutObjectRequest.class), requestBodyCaptor.capture());

        // Update the S3 mock to return the newly saved data for subsequent GET requests
        RequestBody capturedBody = requestBodyCaptor.getValue();
        GetObjectResponse getResponse = GetObjectResponse.builder().build();
        doReturn(new ResponseInputStream<>(
                getResponse,
                new ByteArrayInputStream(capturedBody.contentStreamProvider().newStream().readAllBytes())))
                .when(s3Client).getObject(any(GetObjectRequest.class));

        // Follow the redirect to get the updated page content (like a real browser
        // would)
        log.info("Following redirect to /event/{} after saving participant", eventUuid);
        resultAction = mockMvc.perform(get("/event/" + eventUuid));
        resultAction.andExpect(status().isOk());

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

    // Helper method to reduce HTML parsing duplication
    private Document getDocumentFromResult() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        return Jsoup.parse(content);
    }

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
        Document doc = getDocumentFromResult();
        // Check for indexed date input (at least one time slot should exist)
        Elements dateInputs = doc.select("input[type=date][id=date0][name=date0]");
        Elements startTimeInputs = doc.select("input[type=time][id=startTime0][name=startTime0]");
        Elements endTimeInputs = doc.select("input[type=time][id=endTime0][name=endTime0]");
        Elements backButtons = doc.select("button[type=button]:contains(Back)");
        Elements nextButtons = doc.select("button[type=submit]");
        assertThat(dateInputs.size()).isEqualTo(1);
        assertThat(startTimeInputs.size()).isEqualTo(1);
        assertThat(endTimeInputs.size()).isEqualTo(1);
        assertThat(backButtons.text()).containsIgnoringCase("back");
        assertThat(nextButtons.text()).containsIgnoringCase("next");
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
        Document doc = getDocumentFromResult();
        String expiryValue = doc.select("input[type=date][id=expiryDate][name=expiryDate]").attr("value");
        assertThat(expiryValue).isEqualTo(expectedDate);
        return self();
    }

    public ThenWoodleViewMvcOutcome user_should_see_step2_form_with_previous_data() throws Exception {
        return step2_form_should_show_previous_data(
                Map.of(0, new TimeSlot("2024-03-20", "10:00", "11:00")));
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
        return summary_page_should_show_all_entered_data(Map.of(
                0, new TimeSlot("2024-03-20", "10:00", "11:00")));
    }

    public ThenWoodleViewMvcOutcome summary_page_should_show_all_entered_data(
            Map<Integer, TimeSlot> expectedTimeSlots) throws Exception {
        Document doc = getDocumentFromResult();

        // Check that poll information is still displayed (title and description in
        // poll-details section)
        assertThat(doc.select(
                "div[data-test-section='poll-details'] div[data-test-item='title'] span:contains(" + title + ")")
                .size())
                .isEqualTo(1);
        assertThat(doc.select("div[data-test-section='poll-details'] div[data-test-item='description'] span:contains("
                + description + ")").size())
                .isEqualTo(1);

        // Check that organizer information is still displayed (name and email in
        // organizer-details section)
        assertThat(doc.select(
                "div[data-test-section='organizer-details'] div[data-test-item='name'] span:contains(" + name + ")")
                .size())
                .isEqualTo(1);
        assertThat(doc.select(
                "div[data-test-section='organizer-details'] div[data-test-item='email'] span:contains(" + email + ")")
                .size())
                .isEqualTo(1);

        // Check that expiry date is still displayed somewhere on the page
        assertThat(doc.select("span:contains(2024-06-20)").size()).isGreaterThan(0);

        // Check that time slot data is now in the events table, not in the old
        // event-details section
        Elements eventsTable = doc.select("table[data-test='events-table']");
        assertThat(eventsTable.size()).isEqualTo(1);

        // Verify that time slots are in the table headers
        for (TimeSlot slot : expectedTimeSlots.values()) {
            Elements timeHeaders = doc
                    .select("table[data-test='events-table'] thead tr th[data-test-date='" + slot.date() + "']");
            assertThat(timeHeaders.size()).isGreaterThan(0);
        }

        // Verify that the old event-details section no longer exists
        Elements oldEventDetails = doc.select("div[data-test-section='event-details']");
        assertThat(oldEventDetails.size()).isEqualTo(0);

        return self();
    }

    public ThenWoodleViewMvcOutcome summary_page_should_show_event_url() throws Exception {
        Document doc = getDocumentFromResult();
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
        Document doc = getDocumentFromResult();

        // Check for plus button
        Elements plusButtons = doc.select("button[type=button].add-time-slot");
        assertThat(plusButtons.size()).isEqualTo(1);

        // Check for plus image with alt text '+'
        Elements plusImages = plusButtons.select("img[src*='Plus-Symbol-Transparent-small.png'][alt='+']");
        assertThat(plusImages.size()).isEqualTo(1);

        return self();
    }

    public ThenWoodleViewMvcOutcome step2_form_should_show_previous_data(
            Map<Integer, TimeSlot> expectedTimeSlots)
            throws Exception {
        log.info("Verifying step2 form with previous data");
        Document doc = getDocumentFromResult();

        for (Map.Entry<Integer, TimeSlot> entry : expectedTimeSlots.entrySet()) {
            int index = entry.getKey();
            TimeSlot expectedSlot = entry.getValue();
            Element dateInput = doc.select("input[data-test='date-" + index + "']").first();
            Element startTimeInput = doc.select("input[data-test='startTime-" + index + "']").first();
            Element endTimeInput = doc.select("input[data-test='endTime-" + index + "']").first();

            assertThat(dateInput.attr("value")).isEqualTo(expectedSlot.date());
            assertThat(startTimeInput.attr("value")).isEqualTo(expectedSlot.startTime());
            assertThat(endTimeInput.attr("value")).isEqualTo(expectedSlot.endTime());
        }

        return self();
    }

    public ThenWoodleViewMvcOutcome step2_form_should_preserve_all_time_slots() throws Exception {
        Document doc = getDocumentFromResult();

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

    public ThenWoodleViewMvcOutcome step2_form_should_have_four_time_slots() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Check that we have four sets of date/time input fields
        Elements dateInputs = doc.select("input[data-test^='date-']");
        Elements startTimeInputs = doc.select("input[data-test^='startTime-']");
        Elements endTimeInputs = doc.select("input[data-test^='endTime-']");

        assertThat(dateInputs.size()).isEqualTo(4);
        assertThat(startTimeInputs.size()).isEqualTo(4);
        assertThat(endTimeInputs.size()).isEqualTo(4);

        return self();
    }

    public ThenWoodleViewMvcOutcome step2_form_should_show_previous_data() throws Exception {
        return step2_form_should_show_previous_data(
                Map.of(0, new TimeSlot("2024-03-20", "10:00", "11:00")));
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

    public ThenWoodleViewMvcOutcome only_one_time_slot_should_be_in_session() throws Exception {
        MvcResult result = resultAction.andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();
        de.bas.bodo.woodle.view.ScheduleEventStep2Form step2Form = (de.bas.bodo.woodle.view.ScheduleEventStep2Form) session
                .getAttribute("step2FormData");
        assertThat(step2Form.timeSlots()).hasSize(2);
        assertThat(step2Form.timeSlots().get(0).date()).isEqualTo("2024-03-20");
        assertThat(step2Form.timeSlots().get(1).date()).isEmpty();
        return self();
    }

    public ThenWoodleViewMvcOutcome summary_page_should_have_events_table() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Check that a table with data-test="events-table" exists
        Elements eventsTable = doc.select("table[data-test='events-table']");
        assertThat(eventsTable.size()).isEqualTo(1);

        return self();
    }

    public ThenWoodleViewMvcOutcome events_table_should_have_column_headers() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Check that we have 2 header rows
        Elements headerRows = doc.select("table[data-test='events-table'] thead tr");
        assertThat(headerRows.size()).isEqualTo(2);

        // First header row: dates with proper colspan
        Element firstHeaderRow = headerRows.get(0);
        Elements firstRowHeaders = firstHeaderRow.select("th");

        // First column should be for participant names
        Element participantHeader = firstRowHeaders.get(0);
        assertThat(participantHeader.attr("data-test")).isEqualTo("participant-header");
        assertThat(participantHeader.attr("rowspan")).isEqualTo("2"); // Should span both header rows

        // Date headers with proper colspan
        Element date1Header = firstRowHeaders.get(1);
        assertThat(date1Header.attr("data-test-date")).isEqualTo("2024-03-15");
        assertThat(date1Header.attr("colspan")).isEqualTo("2"); // Should span 2 columns (2 time slots)

        Element date2Header = firstRowHeaders.get(2);
        assertThat(date2Header.attr("data-test-date")).isEqualTo("2024-03-18");
        assertThat(date2Header.attr("colspan")).isEqualTo("1"); // Should span 1 column (1 time slot)

        // Second header row: time slots
        Element secondHeaderRow = headerRows.get(1);
        Elements secondRowHeaders = secondHeaderRow.select("th");
        assertThat(secondRowHeaders.size()).isEqualTo(3); // 3 time slot columns (no participant column as it's rowspan)

        // Time slot headers in chronological order
        Element timeSlot1Header = secondRowHeaders.get(0);
        assertThat(timeSlot1Header.attr("data-test-date")).isEqualTo("2024-03-15");
        assertThat(timeSlot1Header.attr("data-test-start-time")).isEqualTo("09:00:00");
        assertThat(timeSlot1Header.attr("data-test-end-time")).isEqualTo("10:00:00");

        Element timeSlot2Header = secondRowHeaders.get(1);
        assertThat(timeSlot2Header.attr("data-test-date")).isEqualTo("2024-03-15");
        assertThat(timeSlot2Header.attr("data-test-start-time")).isEqualTo("14:00:00");
        assertThat(timeSlot2Header.attr("data-test-end-time")).isEqualTo("15:00:00");

        Element timeSlot3Header = secondRowHeaders.get(2);
        assertThat(timeSlot3Header.attr("data-test-date")).isEqualTo("2024-03-18");
        assertThat(timeSlot3Header.attr("data-test-start-time")).isEqualTo("11:00:00");
        assertThat(timeSlot3Header.attr("data-test-end-time")).isEqualTo("12:00:00");

        return self();
    }

    public ThenWoodleViewMvcOutcome events_table_should_group_dates_together() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Check that dates are grouped - same dates should appear next to each other
        // Only look at the first header row for date grouping
        Elements dateHeaders = doc.select("table[data-test='events-table'] thead tr:first-child th[data-test-date]");

        // We should have date headers for both 2024-03-15 and 2024-03-18 in the first
        // row
        // The dates should be grouped properly (all 2024-03-15 together, then
        // 2024-03-18)
        assertThat(dateHeaders.size()).isEqualTo(2); // Should have exactly 2 date headers in first row

        Element firstDateHeader = dateHeaders.get(0);
        assertThat(firstDateHeader.attr("data-test-date")).isEqualTo("2024-03-15");
        assertThat(firstDateHeader.attr("colspan")).isEqualTo("2"); // Should span 2 time slots

        Element secondDateHeader = dateHeaders.get(1);
        assertThat(secondDateHeader.attr("data-test-date")).isEqualTo("2024-03-18");
        assertThat(secondDateHeader.attr("colspan")).isEqualTo("1"); // Should span 1 time slot

        return self();
    }

    public ThenWoodleViewMvcOutcome events_table_should_order_dates_and_times_correctly() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Check that time slot headers are ordered chronologically
        Elements timeHeaders = doc.select("table[data-test='events-table'] thead tr th[data-test-time]");

        // Expected order: 2024-03-15 09:00-10:00, 2024-03-15 14:00-15:00, 2024-03-18
        // 11:00-12:00
        assertThat(timeHeaders.size()).isEqualTo(3);

        // First time slot should be 2024-03-15 09:00:00-10:00:00
        Element firstTimeHeader = timeHeaders.get(0);
        assertThat(firstTimeHeader.attr("data-test-date")).isEqualTo("2024-03-15");
        assertThat(firstTimeHeader.attr("data-test-start-time")).isEqualTo("09:00:00");
        assertThat(firstTimeHeader.attr("data-test-end-time")).isEqualTo("10:00:00");

        // Second time slot should be 2024-03-15 14:00:00-15:00:00
        Element secondTimeHeader = timeHeaders.get(1);
        assertThat(secondTimeHeader.attr("data-test-date")).isEqualTo("2024-03-15");
        assertThat(secondTimeHeader.attr("data-test-start-time")).isEqualTo("14:00:00");
        assertThat(secondTimeHeader.attr("data-test-end-time")).isEqualTo("15:00:00");

        // Third time slot should be 2024-03-18 11:00:00-12:00:00
        Element thirdTimeHeader = timeHeaders.get(2);
        assertThat(thirdTimeHeader.attr("data-test-date")).isEqualTo("2024-03-18");
        assertThat(thirdTimeHeader.attr("data-test-start-time")).isEqualTo("11:00:00");
        assertThat(thirdTimeHeader.attr("data-test-end-time")).isEqualTo("12:00:00");

        return self();
    }

    public ThenWoodleViewMvcOutcome events_table_should_have_empty_name_input_field() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Check for empty input field for participant name in the first column of the
        // data row
        Elements nameInputs = doc
                .select("table[data-test='events-table'] tbody tr td input[data-test='participant-name']");
        assertThat(nameInputs.size()).isEqualTo(1);

        Element nameInput = nameInputs.first();
        assertThat(nameInput.attr("value")).isEmpty();
        assertThat(nameInput.attr("type")).isEqualTo("text");

        return self();
    }

    public ThenWoodleViewMvcOutcome participant_selection_should_be_saved_to_poll() throws Exception {
        log.info("Verifying that participant selection was saved to poll");
        // Verify that the S3 putObject was called to save the updated poll data
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        return self();
    }

    public ThenWoodleViewMvcOutcome events_table_should_show_fixed_participant_row(String participantName,
            Map<Integer, Boolean> timeSlots) throws Exception {
        log.info("Verifying fixed participant row for: {}", participantName);
        Document doc = getDocumentFromResult();

        // Check that the participant name is displayed as read-only text (not an input
        // field)
        Elements participantNameCells = doc
                .select("table[data-test='events-table'] tbody tr td[data-test-participant='" + participantName + "']");
        assertThat(participantNameCells.size()).isEqualTo(1);
        assertThat(participantNameCells.text()).isEqualTo(participantName);

        // Check that the time slot selections are displayed as read-only checkboxes
        // (disabled)
        for (Map.Entry<Integer, Boolean> entry : timeSlots.entrySet()) {
            int slotIndex = entry.getKey();
            boolean selected = entry.getValue();

            Elements checkboxes = doc.select("table[data-test='events-table'] tbody tr td:nth-child(" + (slotIndex + 2)
                    + ") input[type='checkbox'][disabled]");
            assertThat(checkboxes.size()).isEqualTo(1);

            Element checkbox = checkboxes.first();
            if (selected) {
                assertThat(checkbox.hasAttr("checked")).isTrue();
            } else {
                assertThat(checkbox.hasAttr("checked")).isFalse();
            }
        }

        return self();
    }

    public ThenWoodleViewMvcOutcome events_table_should_have_new_empty_participant_row() throws Exception {
        log.info("Verifying new empty participant row exists");
        Document doc = getDocumentFromResult();

        // Check that there's an input field for a new participant name
        Elements newParticipantInputs = doc.select(
                "table[data-test='events-table'] tbody tr:last-child td input[data-test='participant-name'][type='text']");
        assertThat(newParticipantInputs.size()).isEqualTo(1);
        assertThat(newParticipantInputs.first().attr("value")).isEmpty();

        // Check that there are unchecked checkboxes for the new participant row
        Elements newParticipantCheckboxes = doc.select(
                "table[data-test='events-table'] tbody tr:last-child td input[type='checkbox']:not([disabled])");
        assertThat(newParticipantCheckboxes.size()).isGreaterThan(0);

        for (Element checkbox : newParticipantCheckboxes) {
            assertThat(checkbox.hasAttr("checked")).isFalse();
        }

        return self();
    }

    public ThenWoodleViewMvcOutcome save_button_should_be_present() throws Exception {
        log.info("Verifying save button is present");
        Document doc = getDocumentFromResult();

        Elements saveButtons = doc.select("button[data-test='save-participant-button']");
        assertThat(saveButtons.size()).isEqualTo(1);
        assertThat(saveButtons.text()).containsIgnoringCase("save");

        return self();
    }

    public ThenWoodleViewMvcOutcome events_table_should_show_multiple_fixed_participant_rows(
            Map<String, Map<Integer, Boolean>> participantsAndTimeSlots) throws Exception {
        log.info("Verifying multiple fixed participant rows for: {}", participantsAndTimeSlots.keySet());
        Document doc = getDocumentFromResult();

        // Check that all participants are displayed as fixed rows
        for (Map.Entry<String, Map<Integer, Boolean>> participantEntry : participantsAndTimeSlots.entrySet()) {
            String participantName = participantEntry.getKey();
            Map<Integer, Boolean> timeSlots = participantEntry.getValue();

            // Check that the participant name is displayed as read-only text (not an input
            // field)
            Elements participantNameCells = doc
                    .select("table[data-test='events-table'] tbody tr td[data-test-participant='" + participantName
                            + "']");
            assertThat(participantNameCells.size()).isEqualTo(1);
            assertThat(participantNameCells.text()).isEqualTo(participantName);

            // Check that the time slot selections are displayed as read-only checkboxes
            // (disabled)
            for (Map.Entry<Integer, Boolean> timeSlotEntry : timeSlots.entrySet()) {
                int slotIndex = timeSlotEntry.getKey();
                boolean selected = timeSlotEntry.getValue();

                // Find the row containing this participant
                Element participantRow = participantNameCells.first().parent();
                Elements checkboxes = participantRow
                        .select("td:nth-child(" + (slotIndex + 2) + ") input[type='checkbox'][disabled]");
                assertThat(checkboxes.size()).isEqualTo(1);

                Element checkbox = checkboxes.first();
                if (selected) {
                    assertThat(checkbox.hasAttr("checked")).isTrue();
                } else {
                    assertThat(checkbox.hasAttr("checked")).isFalse();
                }
            }
        }

        return self();
    }

    public ThenWoodleViewMvcOutcome events_table_should_have_exactly_one_participant_row() throws Exception {
        MvcResult result = resultAction.andReturn();
        String content = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(content);

        // Check that there's exactly one participant row
        Elements participantRows = doc.select("table[data-test='events-table'] tbody tr");
        assertThat(participantRows.size()).isEqualTo(1);

        return self();
    }

    public ThenWoodleViewMvcOutcome events_table_should_have_exactly_two_participant_rows() throws Exception {
        log.info("Verifying that events table has exactly two participant rows");
        Document doc = getDocumentFromResult();
        Elements participantRows = doc.select("table[data-test='events-table'] tbody tr");
        assertThat(participantRows.size()).isEqualTo(2);
        return self();
    }

    public ThenWoodleViewMvcOutcome form_action_should_point_to_correct_participants_save_url(String testUuid)
            throws Exception {
        log.info("Verifying that form action URL is correct for UUID: {}", testUuid);
        Document doc = getDocumentFromResult();

        // Find the form that contains the participant save functionality
        Elements form = doc.select("form[method='post']");
        assertThat(form.size()).isEqualTo(1);

        String formAction = form.attr("action");
        log.info("Form action found: {}", formAction);

        // The form action should be /event/{uuid}/participants/save
        String expectedAction = "/event/" + testUuid + "/participants/save";
        assertThat(formAction).isEqualTo(expectedAction);

        return self();
    }
}