package de.bas.bodo.woodle.adapter.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class WoodleViewTest {

    private static final String INDEX_PATH = "/index.html";
    private static final String EXPECTED_CONTENT = "hello world";
    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /index.html returns homepage with Woodle header and proper HTML structure")
    void indexHtmlShouldContainWoodleHeaderAndHtmlStructure() throws Exception {
        mockMvc.perform(get(INDEX_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<h1>Woodle</h1>")))
                .andExpect(content().string(containsString("<html>")))
                .andExpect(content().string(containsString("</html>")))
                .andExpect(header().string("Content-Type", CONTENT_TYPE));
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
        mockMvc.perform(get(INDEX_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Schedule Event")))
                .andExpect(content().string(containsString("Woodle")))
                .andExpect(header().string("Content-Type", CONTENT_TYPE));
    }

    @Test
    @DisplayName("GET /schedule-event shows form with name field")
    void scheduleEventShouldShowForm() throws Exception {
        mockMvc.perform(get("/schedule-event"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your name")))
                .andExpect(header().string("Content-Type", CONTENT_TYPE));
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
        mockMvc.perform(get(INDEX_PATH))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<img src=\"/woodle-logo.jpeg\"")));
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
        mockMvc.perform(get("/schedule-event"))
                .andExpect(status().isOk())
                // Your name
                .andExpect(content().string(containsString("<label for=\"name\">Your name")))
                .andExpect(content().string(containsString("<input type=\"text\" id=\"name\" name=\"name\"")))
                // Your email address
                .andExpect(content().string(containsString("<label for=\"email\">Your email address")))
                .andExpect(content().string(containsString("<input type=\"email\" id=\"email\" name=\"email\"")))
                // Poll title
                .andExpect(content().string(containsString("<label for=\"title\">Poll title")))
                .andExpect(content().string(containsString("<input type=\"text\" id=\"title\" name=\"title\"")))
                // Description
                .andExpect(content().string(containsString("<label for=\"description\">Description")))
                .andExpect(content().string(containsString("<textarea id=\"description\" name=\"description\"")))
                // Optional parameters (just check for the section for now)
                .andExpect(content().string(containsString("Optional parameters")))
                // Go to step 2 button
                .andExpect(content().string(containsString(">Go to step 2<")));
    }
}