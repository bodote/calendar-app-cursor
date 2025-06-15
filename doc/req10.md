# requirement 10
on the summary page the users should be able to add a name in the Participant column and check some of the checkboxed for the timeslots. there can be as much as name rows as the users wants to. as soon as the user "saves" on row with name and checkboxes , this rows is now fixed , can not be changed any more , but a new empty rows below is added. *

# acceptance criteria
* there should be a "save" Button below the table . When the user klicks this button the entered name and the the checkboxed checkt or uncheckt should be saved to the event poll as the first element of a list of names and date/timeslots. 
*  after "save" the user should now get an updated table with the just entered row as a fixed row, where the name nor the timeslots can be changed any more.
*  after "save" there should be an emtpy row added at the bottom
*  If another user (or even the same user) enters another name and set some checkboxes that should be also saved and so on

# hints
* add a new test method for this requirement
* make use of the existing GivenWoodleViewMvcAction, WhenWoodleViewMvcAction and ThenWoodleViewMvcAction methods to avoid code duplication
* you must not rely on HttpSession any more on the summary page, because all persistent data should be stored in and retrieved from the PollStorageService anyway.
  