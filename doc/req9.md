# requirement 9
 the user should find all previously entered  proposed event date/time in a table to make it more human readable especially if there are lots of proposed events 

# acceptance criteria 

 * there is a column for each date , also each timespan, but all time span of the same date are grouped together so that the user can clearly see what data a timespan is refering to 
 * there is only one rows as long as no other user has entered his name and hast not yet make his choice about the events.
 * this only row start with an empty input field (where later a user can enter is name) 
 * the table should look like this: ![proposed events table](StimmabgabeTabelle.png)
 * the test should test that the event details are in a table, that the table has column headers an that the dates and timespans of all entered events are ordered from left to right
 * once the table works the still existing  Event Details on the summary page that is NOT in the table should be removed. 
  
# hints 
* this new test should NOT test all the previous steps , instead just make up a list of 3 events, 2 events on the same date but 2 different time values and one other date with a time , inject these into the `@MockBean
    private S3Client s3Client;` then call the summary page directly and check for the table 
* remember to facilitate easy access to html elements for tests use the `data-test-*` tag. Therefore the tests should access html elements, such as html-table elements primarily with those `data-test-*` tags