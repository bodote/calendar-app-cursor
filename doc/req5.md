# requirement 5
as a user i want to be able to go to the next form page to enter more data for my planed activity: a date and at least 2 input fields for time
![Schedule Event Form 2](woodle-screenshot-step2.png) 

## acceptance criteria
1. if the user is still on the first form ![Schedule Event Form 1](schedule-event.png) and clicks on the "Next" button, the 2nd step form appears
2. the layout of the step 2 form should look similar to ![Schedule Event Form 2](woodle-screenshot-step2.png) 
3. the user can input a date 
4. the user can input a time in all the time fields
5. if the user clicks the "back" button, the previous from ![Schedule Event Form 1](schedule-event.png) appears again including the data he already has entered
6.  
## hints:
* instead of testing for just some strings, better test for existence of a calendar icon called "calendar-icon.png"  and test if it is reachable for the server
* same for all input fields: test if the input fields exist 
* use input type "date" so that the browser shows an small calendar icon , same for input type "time" 
* you need to test this scenario : user inputs data on the first form page (like  his email address) then clicks the "go to step 2" button, then goes back and wants to see the data he already has entered before

