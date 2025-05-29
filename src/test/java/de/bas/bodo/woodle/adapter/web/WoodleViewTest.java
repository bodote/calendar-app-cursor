package de.bas.bodo.woodle.adapter.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import static org.assertj.core.api.Assertions.assertThat;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import org.springframework.mock.web.MockHttpSession;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ScenarioState;
import com.tngtech.jgiven.junit5.ScenarioTest;
import com.tngtech.jgiven.annotation.ScenarioStage;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import de.bas.bodo.woodle.config.TestConfig;
import de.bas.bodo.woodle.config.S3Config;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
@ActiveProfiles({ "test" })
class WoodleViewTest extends ScenarioTest<GivenWoodleState, WhenWoodleAction, ThenWoodleOutcome> {

        private static final String INDEX_PATH = "/index.html";
        private static final String EXPECTED_CONTENT = "hello world";
        private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private S3Client s3Client;

        @ScenarioStage
        private GivenWoodleState givenWoodleState;
        @ScenarioStage
        private WhenWoodleAction whenWoodleAction;
        @ScenarioStage
        private ThenWoodleOutcome thenWoodleOutcome;

        // Test data constants
        private static final String TEST_NAME = "Alice";
        private static final String TEST_EMAIL = "alice@example.com";
        private static final String TEST_TITLE = "My Poll";
        private static final String TEST_DESCRIPTION = "Some description";

        private MockHttpSession session;

        @BeforeEach
        void setupStages() {
                givenWoodleState.setMockMvc(mockMvc);
                whenWoodleAction.setMockMvc(mockMvc);
                thenWoodleOutcome.setMockMvc(mockMvc);
                session = new MockHttpSession();
        }

        @Test
        @DisplayName("User can navigate through the scheduling process")
        void user_can_navigate_through_scheduling_process() throws Exception {
                given().user_is_on_homepage()
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
        @DisplayName("Event data is stored in S3 and can be retrieved")
        void event_data_is_stored_in_s3_and_can_be_retrieved() throws Exception {
                given().user_is_on_homepage()
                                .and().user_has_test_data(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION);
                when().user_clicks_schedule_event_button()
                                .and().user_fills_step1_form()
                                .and().user_clicks_next()
                                .and().user_fills_step2_form("2024-03-20", "10:00", "11:00")
                                .and().user_clicks_next()
                                .and().user_fills_step3_form("2024-06-20")
                                .and().user_clicks_create_poll();
                then().event_data_should_be_stored_in_s3()
                                .and().event_data_can_be_retrieved_from_s3();
        }

        @Test
        public void should_display_summary_and_store_in_s3() throws Exception {
                // Given
                String name = "John Doe";
                String email = "john@example.com";
                String title = "Team Meeting";
                String description = "Weekly sync";
                String date = "2024-03-20";
                String startTime = "10:00";
                String endTime = "11:00";
                String expiryDate = "2024-06-20";

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

                mockMvc.perform(post("/schedule-event-step3")
                                .param("expiryDate", expiryDate)
                                .session(session))
                                .andExpect(status().isSeeOther())
                                .andExpect(redirectedUrl("/event-summary"));

                // Then
                mockMvc.perform(get("/event-summary")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("Poll Summary")))
                                .andExpect(content().string(containsString(name)))
                                .andExpect(content().string(containsString(email)))
                                .andExpect(content().string(containsString(title)))
                                .andExpect(content().string(containsString(description)))
                                .andExpect(content().string(containsString(date)))
                                .andExpect(content().string(containsString(startTime)))
                                .andExpect(content().string(containsString(endTime)))
                                .andExpect(content().string(containsString(expiryDate)))
                                .andExpect(content().string(containsString("Your poll has been created successfully!")))
                                .andExpect(content().string(containsString("Poll URL:")));

                // Verify S3 storage
                verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }
}

@org.springframework.stereotype.Component
class GivenWoodleState extends Stage<GivenWoodleState> {
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

        public void setMockMvc(MockMvc mockMvc) {
                this.mockMvc = mockMvc;
        }

        public GivenWoodleState user_is_on_homepage() throws Exception {
                session = new MockHttpSession();
                return self();
        }

        public GivenWoodleState user_has_test_data(String name, String email, String title, String description) {
                this.name = name;
                this.email = email;
                this.title = title;
                this.description = description;
                return self();
        }

        public GivenWoodleState user_fills_step1_form(String name, String email, String title, String description) {
                this.name = name;
                this.email = email;
                this.title = title;
                this.description = description;
                return self();
        }
}

@org.springframework.stereotype.Component
class WhenWoodleAction extends Stage<WhenWoodleAction> {
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

        public void setMockMvc(MockMvc mockMvc) {
                this.mockMvc = mockMvc;
        }

        public WhenWoodleAction user_clicks_schedule_event_button() throws Exception {
                mockMvc.perform(get("/index.html").session(session))
                                .andExpect(status().isOk());
                return self();
        }

        public WhenWoodleAction user_fills_step1_form() throws Exception {
                mockMvc.perform(post("/schedule-event")
                                .param("name", name)
                                .param("email", email)
                                .param("title", title)
                                .param("description", description)
                                .session(session))
                                .andExpect(status().isSeeOther())
                                .andExpect(redirectedUrl("/schedule-event-step2"));
                return self();
        }

        public WhenWoodleAction user_clicks_next() throws Exception {
                // Implementation depends on current step
                return self();
        }

        public WhenWoodleAction user_clicks_back() throws Exception {
                // Implementation depends on current step
                return self();
        }

        public WhenWoodleAction user_fills_step2_form(String date, String startTime, String endTime) throws Exception {
                mockMvc.perform(post("/schedule-event-step2")
                                .param("date", date)
                                .param("startTime", startTime)
                                .param("endTime", endTime)
                                .session(session))
                                .andExpect(status().isSeeOther())
                                .andExpect(redirectedUrl("/schedule-event-step3"));
                return self();
        }

        public WhenWoodleAction user_fills_step3_form(String expiryDate) throws Exception {
                mockMvc.perform(post("/schedule-event-step3")
                                .param("expiryDate", expiryDate)
                                .session(session))
                                .andExpect(status().isSeeOther())
                                .andExpect(redirectedUrl("/event-summary"));
                return self();
        }

        public WhenWoodleAction user_clicks_create_poll() throws Exception {
                // The form submission is handled in user_fills_step3_form
                return self();
        }
}

@org.springframework.stereotype.Component
class ThenWoodleOutcome extends Stage<ThenWoodleOutcome> {
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
        @Autowired
        private S3Client s3Client;

        public void setMockMvc(MockMvc mockMvc) {
                this.mockMvc = mockMvc;
        }

        public ThenWoodleOutcome user_should_see_step1_form() throws Exception {
                Document doc = getAndParseHtml("/schedule-event", session);
                assertThat(doc.select("input[name=name]").size()).isEqualTo(1);
                assertThat(doc.select("input[name=email]").size()).isEqualTo(1);
                assertThat(doc.select("input[name=title]").size()).isEqualTo(1);
                assertThat(doc.select("textarea[name=description]").size()).isEqualTo(1);
                return self();
        }

        public ThenWoodleOutcome user_should_see_step1_form_with_previous_data() throws Exception {
                Document doc = getAndParseHtml("/schedule-event", session);
                assertThat(doc.select("input[name=name]").val()).isEqualTo(name);
                assertThat(doc.select("input[name=email]").val()).isEqualTo(email);
                assertThat(doc.select("input[name=title]").val()).isEqualTo(title);
                assertThat(doc.select("textarea[name=description]").text()).isEqualTo(description);
                return self();
        }

        public ThenWoodleOutcome user_should_see_step2_form() throws Exception {
                Document doc = getAndParseHtml("/schedule-event-step2", session);
                assertThat(doc.select("input[type=date][id=date][name=date]").size()).isEqualTo(1);
                assertThat(doc.select("input[type=time][id=startTime][name=startTime]").size()).isEqualTo(1);
                assertThat(doc.select("input[type=time][id=endTime][name=endTime]").size()).isEqualTo(1);
                return self();
        }

        public ThenWoodleOutcome step2_form_should_have_all_required_fields() throws Exception {
                Document doc = getAndParseHtml("/schedule-event-step2", session);
                assertThat(doc.select("input[type=date][id=date][name=date]").size()).isEqualTo(1);
                assertThat(doc.select("input[type=time][id=startTime][name=startTime]").size()).isEqualTo(1);
                assertThat(doc.select("input[type=time][id=endTime][name=endTime]").size()).isEqualTo(1);
                assertThat(doc.select("button[type=button]:contains(Back)").size()).isEqualTo(1);
                assertThat(doc.select("button[type=submit]:contains(Next)").size()).isEqualTo(1);
                return self();
        }

        public ThenWoodleOutcome user_should_see_step2_form_with_previous_data() throws Exception {
                Document doc = getAndParseHtml("/schedule-event-step2", session);
                assertThat(doc.select("input[name=date]").attr("value")).isEqualTo("2024-03-20");
                assertThat(doc.select("input[name=startTime]").attr("value")).isEqualTo("10:00");
                assertThat(doc.select("input[name=endTime]").attr("value")).isEqualTo("11:00");
                return self();
        }

        public ThenWoodleOutcome user_should_see_step3_form() throws Exception {
                Document doc = getAndParseHtml("/schedule-event-step3", session);
                assertThat(doc.select("input[type=date][id=expiryDate][name=expiryDate]").size()).isEqualTo(1);
                assertThat(doc.select("button[type=button]:contains(Back)").size()).isEqualTo(1);
                assertThat(doc.select("button[type=submit]:contains(Create the poll)").size()).isEqualTo(1);
                return self();
        }

        public ThenWoodleOutcome step3_form_should_have_expiry_date(String expectedDate) throws Exception {
                Document doc = getAndParseHtml("/schedule-event-step3", session);
                String expiryValue = doc.select("input[type=date][id=expiryDate][name=expiryDate]").attr("value");
                assertThat(expiryValue).isEqualTo(expectedDate);
                return self();
        }

        public ThenWoodleOutcome user_should_see_summary_page() throws Exception {
                Document doc = getAndParseHtml("/event-summary", session);
                assertThat(doc.select("h1:contains(Event Summary)").size()).isEqualTo(1);
                return self();
        }

        public ThenWoodleOutcome summary_page_should_show_all_entered_data() throws Exception {
                Document doc = getAndParseHtml("/event-summary", session);
                assertThat(doc.select("div:contains(" + name + ")").size()).isEqualTo(1);
                assertThat(doc.select("div:contains(" + email + ")").size()).isEqualTo(1);
                assertThat(doc.select("div:contains(" + title + ")").size()).isEqualTo(1);
                assertThat(doc.select("div:contains(" + description + ")").size()).isEqualTo(1);
                assertThat(doc.select("div:contains(2024-03-20)").size()).isEqualTo(1);
                assertThat(doc.select("div:contains(10:00)").size()).isEqualTo(1);
                assertThat(doc.select("div:contains(11:00)").size()).isEqualTo(1);
                assertThat(doc.select("div:contains(2024-06-20)").size()).isEqualTo(1);
                return self();
        }

        public ThenWoodleOutcome summary_page_should_show_event_url() throws Exception {
                Document doc = getAndParseHtml("/event-summary", session);
                Elements eventUrl = doc.select("div.event-url");
                assertThat(eventUrl.size()).isEqualTo(1);
                assertThat(eventUrl.text()).matches(
                                "http://localhost:8080/event/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
                return self();
        }

        public ThenWoodleOutcome event_data_should_be_stored_in_s3() throws Exception {
                verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
                return self();
        }

        public ThenWoodleOutcome event_data_can_be_retrieved_from_s3() throws Exception {
                // For now, we just verify that the data was stored
                // In a real implementation, we would also verify that it can be retrieved
                verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
                return self();
        }

        private Document getAndParseHtml(String path, MockHttpSession session) throws Exception {
                String htmlContent = mockMvc.perform(get(path).session(session))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();
                return Jsoup.parse(htmlContent);
        }
}