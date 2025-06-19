# requirement 11
the user should be able to delete one line in the proposed event times table on the summary page. 
# acceptance criteria
* there is a "delete" button at the end of each line of the event times table on the summary page in the last column
* if the user clicks on that icon the row should be deleted from the poll 
* the delete button should be using ![this icon ](trashcan.png) 
* the delete column should have 2 empty header lines
# hints
* make a new test in `WoodleViewMvcTest` but reuse as much existing `GivenWoodleViewMvcStage` , `WhenWoodleViewMvcAction` `ThenWoodleViewMvcOutcome` - methods as possible for the test in `WoodleViewMvcTest`
