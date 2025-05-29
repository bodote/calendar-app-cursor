package de.bas.bodo.woodle.adapter.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

@SpringBootTest
@AutoConfigureMockMvc
class WoodleViewTest {

        private static final String INDEX_PATH = "/index.html";
        private static final String EXPECTED_CONTENT = "hello world";
        private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

        @Autowired
        private MockMvc mockMvc;

        // Test data constants
        private static final String TEST_NAME = "Alice";
        private static final String TEST_EMAIL = "alice@example.com";
        private static final String TEST_TITLE = "My Poll";
        private static final String TEST_DESCRIPTION = "Some description";

        // Helper method to create a session and post to step 1
        private MockHttpSession postToStep1(String name, String email, String title, String description)
                        throws Exception {
                MockHttpSession session = new MockHttpSession();
                mockMvc.perform(post("/schedule-event")
                                .param("name", name)
                                .param("email", email)
                                .param("title", title)
                                .param("description", description)
                                .session(session))
                                .andExpect(status().isSeeOther())
                                .andExpect(redirectedUrl("/schedule-event-step2"));
                return session;
        }

        // Helper method to verify date and time fields
        private void verifyDateAndTimeFields(Document doc) {
                assertThat(doc.select("input[type=date][id=date][name=date]").size()).isEqualTo(1);
                assertThat(doc.select("label[for=date]:contains(Choose a date)").size()).isEqualTo(1);
                assertThat(doc.select("input[type=time][id=startTime][name=startTime]").size()).isEqualTo(1);
                assertThat(doc.select("label[for=startTime]:contains(Start time)").size()).isEqualTo(1);
                assertThat(doc.select("input[type=time][id=endTime][name=endTime]").size()).isEqualTo(1);
                assertThat(doc.select("label[for=endTime]:contains(End time)").size()).isEqualTo(1);
        }

        // Helper method to verify required attributes
        private void verifyRequiredAttributes(Document doc) {
                assertThat(doc.select("input[type=date][id=date][name=date][required]").size()).isEqualTo(1);
                assertThat(doc.select("input[type=time][id=startTime][name=startTime][required]").size()).isEqualTo(1);
                assertThat(doc.select("input[type=time][id=endTime][name=endTime][required]").size()).isEqualTo(1);
        }

        // Helper method to verify step 1 form data
        private void verifyStep1FormData(Document doc) {
                assertThat(doc.select("input[name=name]").val()).isEqualTo(TEST_NAME);
                assertThat(doc.select("input[name=email]").val()).isEqualTo(TEST_EMAIL);
                assertThat(doc.select("input[name=title]").val()).isEqualTo(TEST_TITLE);
                assertThat(doc.select("textarea[name=description]").text()).isEqualTo(TEST_DESCRIPTION);
        }

        // Helper method to verify step 2 form fields
        private void verifyStep2FormFields(Document doc) {
                assertThat(doc.select("label[for=date]:contains(Choose a date)").size()).isEqualTo(1);
                assertThat(doc.select("input[type=date][id=date][name=date]").size()).isEqualTo(1);
                assertThat(doc.select("label[for=startTime]:contains(Start time)").size()).isEqualTo(1);
                assertThat(doc.select("input[type=time][id=startTime][name=startTime]").size()).isEqualTo(1);
                assertThat(doc.select("label[for=endTime]:contains(End time)").size()).isEqualTo(1);
                assertThat(doc.select("input[type=time][id=endTime][name=endTime]").size()).isEqualTo(1);
                assertThat(doc.select("button[type=button]:contains(Back)").size()).isEqualTo(1);
                assertThat(doc.select("button[type=submit]:contains(Next)").size()).isEqualTo(1);
        }

        // Helper method to get HTML content and parse it
        private Document getAndParseHtml(String path, MockHttpSession session) throws Exception {
                String htmlContent = mockMvc.perform(get(path).session(session))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();
                return Jsoup.parse(htmlContent);
        }

        // Original tests refactored to use helper methods
        @Test
        @DisplayName("GET /index.html returns homepage with Woodle header and proper HTML structure")
        void indexHtmlShouldContainWoodleHeaderAndHtmlStructure() throws Exception {
                Document doc = getAndParseHtml(INDEX_PATH, new MockHttpSession());
                assertThat(doc.select("h1:contains(Woodle)").size()).isEqualTo(1);
                assertThat(doc.select("html").size()).isEqualTo(1);
                assertThat(doc.select("body").size()).isEqualTo(1);
        }

        @Test
        @DisplayName("GET /notfound returns 404")
        void notFoundShouldReturn404() throws Exception {
                mockMvc.perform(get("/notfound"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /index.html shows Schedule Event button")
        void indexHtmlShouldShowScheduleEventButton() throws Exception {
                String htmlContent = mockMvc.perform(get(INDEX_PATH))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();
                Document doc = Jsoup.parse(htmlContent);
                Elements button = doc.select("a.button:contains(Schedule Event)");
                assertThat(button.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("GET /schedule-event shows form with name field")
        void scheduleEventShouldShowForm() throws Exception {
                String htmlContent = mockMvc.perform(get("/schedule-event"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();
                Document doc = Jsoup.parse(htmlContent);
                Elements input = doc.select("input[name=name]");
                assertThat(input.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("GET / redirects to /index.html")
        void rootShouldRedirectToIndexHtml() throws Exception {
                mockMvc.perform(get("/"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(header().string("Location", "/index.html"));
        }

        @Test
        @DisplayName("GET /index.html contains the Woodle logo")
        void indexHtmlShouldContainWoodleLogo() throws Exception {
                String htmlContent = mockMvc.perform(get(INDEX_PATH))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();
                Document doc = Jsoup.parse(htmlContent);
                Elements logo = doc.select("img[src='/woodle-logo.jpeg']");
                assertThat(logo.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("GET /woodle-logo.jpeg returns the logo image")
        void woodleLogoShouldBeAccessible() throws Exception {
                mockMvc.perform(get("/woodle-logo.jpeg"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /schedule-event shows all required input fields with correct labels and associations")
        void scheduleEventShouldShowAllRequiredFieldsWithLabels() throws Exception {
                String htmlContent = mockMvc.perform(get("/schedule-event"))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();
                Document doc = Jsoup.parse(htmlContent);
                assertThat(doc.select("label[for=name]:contains(Your name)").size()).isEqualTo(1);
                assertThat(doc.select("input[type=text][id=name][name=name]").size()).isEqualTo(1);
                assertThat(doc.select("label[for=email]:contains(Your email address)").size()).isEqualTo(1);
                assertThat(doc.select("input[type=email][id=email][name=email]").size()).isEqualTo(1);
                assertThat(doc.select("label[for=title]:contains(Poll title)").size()).isEqualTo(1);
                assertThat(doc.select("input[type=text][id=title][name=title]").size()).isEqualTo(1);
                assertThat(doc.select("label[for=description]:contains(Description)").size()).isEqualTo(1);
                assertThat(doc.select("textarea[id=description][name=description]").size()).isEqualTo(1);
                assertThat(doc.select("span.optional-toggle:contains(Optional parameters)").size()).isEqualTo(1);
                assertThat(doc.select("button[type=submit]:contains(Next)").size()).isEqualTo(1);
        }

        @Test
        @DisplayName("POST /schedule-event redirects to /schedule-event-step2 and shows step 2 form with input fields")
        void postScheduleEventRedirectsToStep2AndShowsFields() throws Exception {
                MockHttpSession session = postToStep1(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION);
                Document doc = getAndParseHtml("/schedule-event-step2", session);
                verifyDateAndTimeFields(doc);
                assertThat(doc.select("button[type=button]:contains(Back)").size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Step 2 form should have all required fields and labels")
        void step2FormShouldHaveAllFieldsAndLabels() throws Exception {
                MockHttpSession session = postToStep1(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION);
                Document doc = getAndParseHtml("/schedule-event-step2", session);
                verifyStep2FormFields(doc);
        }

        @Test
        @DisplayName("Step 2 form allows user to input date and time values")
        void step2FormAllowsDateAndTimeInput() throws Exception {
                MockHttpSession session = postToStep1(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION);
                Document doc = getAndParseHtml("/schedule-event-step2", session);
                assertThat(doc.select("input[type=date][id=date][name=date]").attr("value")).isNotNull();
                assertThat(doc.select("input[type=time][id=startTime][name=startTime]").attr("value")).isNotNull();
                assertThat(doc.select("input[type=time][id=endTime][name=endTime]").attr("value")).isNotNull();
        }

        @Test
        @DisplayName("Back button on step 2 returns to step 1 with previous data")
        void backButtonReturnsToStep1WithPreviousData() throws Exception {
                MockHttpSession session = postToStep1(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION);
                Document doc = getAndParseHtml("/schedule-event", session);
                verifyStep1FormData(doc);
        }

        @Test
        @DisplayName("Form data should persist when going back from step 2 to step 1")
        void formDataShouldPersistWhenNavigatingBack() throws Exception {
                MockHttpSession session = postToStep1(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION);
                Document doc = getAndParseHtml("/schedule-event", session);
                verifyStep1FormData(doc);
        }

        @Test
        @DisplayName("Clicking 'Go to step 2' button should show step 2 form with date and time fields")
        void clickingGoToStep2ShowsStep2Form() throws Exception {
                MockHttpSession session = postToStep1("Test User", "test@example.com", "Test Event",
                                "Test Description");
                Document doc = getAndParseHtml("/schedule-event-step2", session);
                verifyDateAndTimeFields(doc);
                assertThat(doc.select("button[type=button]:contains(Back)").size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Step 2 form should require date and time fields")
        void step2FormShouldRequireDateAndTimeFields() throws Exception {
                MockHttpSession session = postToStep1("Test User", "test@example.com", "Test Event",
                                "Test Description");
                Document doc = getAndParseHtml("/schedule-event-step2", session);
                verifyRequiredAttributes(doc);
        }

        // Helper to go to step 3
        private void goToStep3(MockMvc mockMvc, MockHttpSession session) throws Exception {
                mockMvc.perform(post("/schedule-event-step2")
                                .param("date", "2024-03-20")
                                .param("startTime", "10:00")
                                .param("endTime", "11:00")
                                .session(session))
                                .andExpect(status().isSeeOther())
                                .andExpect(redirectedUrl("/schedule-event-step3"));
        }

        @Test
        @DisplayName("Step 3: navigation, layout, expiry date, and back button/data persistence")
        void step3NavigationLayoutExpiryAndBackButton() throws Exception {
                // Go to step 2
                MockHttpSession session = postToStep1(TEST_NAME, TEST_EMAIL, TEST_TITLE, TEST_DESCRIPTION);
                // Go to step 3
                goToStep3(mockMvc, session);

                // Check step 3 form
                String htmlContent = mockMvc.perform(get("/schedule-event-step3").session(session))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();
                Document doc = Jsoup.parse(htmlContent);

                // Verify all required elements
                assertThat(doc.select("label[for=expiryDate]:contains(Expiry Date)").size()).isEqualTo(1);
                assertThat(doc.select("input[type=date][id=expiryDate][name=expiryDate][required]").size())
                                .isEqualTo(1);
                assertThat(doc.select("button[type=button]:contains(Back)").size()).isEqualTo(1);
                assertThat(doc.select("button[type=submit]:contains(Create the poll)").size()).isEqualTo(1);

                // Check for expiry date field with default value (3 months from input date)
                String expiryValue = doc.select("input[type=date][id=expiryDate][name=expiryDate]").attr("value");
                assertThat(expiryValue).isEqualTo("2024-06-20");

                // Simulate clicking back (GET step 2) and check data persistence
                String step2Html = mockMvc.perform(get("/schedule-event-step2").session(session))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();
                Document doc2 = Jsoup.parse(step2Html);
                assertThat(doc2.select("input[name=date]").attr("value")).isEqualTo("2024-03-20");
                assertThat(doc2.select("input[name=startTime]").attr("value")).isEqualTo("10:00");
                assertThat(doc2.select("input[name=endTime]").attr("value")).isEqualTo("11:00");
        }
}