instead of  using 
```java
mockMvc.perform(post("/schedule-event-step3")
                .param("expiryDate", expiryDate)
                .session(session))
                .andExpect(status().isOk())
                .andReturn();

```
or similar and working with JGiven 
```
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ScenarioStage;
import com.tngtech.jgiven.annotation.ScenarioState;
import com.tngtech.jgiven.junit5.ScenarioTest;
```

 we need to split the `mockMvc` calls in 2 pieces: 

the first goes into the "When" `Stage`: 
```java
@ScenarioState
ResultActions resultAction;

public WhenWoodleViewMvcAction user_clicks_schedule_event_button2() throws Exception {
        // Go to the homepage and click the schedule event button (GET /schedule-event)
        resultAction = mockMvc.perform(get("/schedule-event").session(session));         
        return self();
    }
```


and the 2nd that goes into the "Then" `Stage` 

```java
@ScenarioState
ResultActions resultAction;
 public ThenWoodleViewMvcOutcome user_should_be_redirected_to_index_html() throws Exception {
        resultAction.andExpect(...);   
        return self();
    } 
   
```

