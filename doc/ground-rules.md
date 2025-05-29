

# general rules
* instead of using default values for tool usage , just ask me first what parameters i would like to use.
e.g. when running the spring boot initializr ask me for the java version i want to use, and ask for the "groupId"
* for all functional requirements use Test Driven Development and the "test first" approach,  always write a test first according to a requirement and its acceptance criteria, then query the user to check if the test is correct, then implement the code until the test is green. 
* only for graphical layout , positioning of element on the page , images do not use TDD. Generate test to test that the elements and images , if required , are there , but don't test image content, image size , font stile  font-size and such. 
* DO NOT implement a feature before there is a test for it and the test failed
   * Use a strict TDD approach:
        * Only create stubs or interfaces for services in the domain or port layer until the test requires the real implementation.
        * Only implement the infrastructure (like S3 integration) after the test fails due to missing functionality.
* use a hexagonal architecture, the code should be organized in a way that the domain logic is separated from the infrastructure code.
* if there is a problem, enable more debugging output so that we can see, where there problem is 
* for example: use any debug options to exactly see how spring boot does resolve the requested path if the spring boot does respond with a http status 404 not found
* When writing Spring Boot tests for an HTML page and testing specific elements like icons, forms, and input fields, use MockMvc with HTML Parsers Jsoup: `org.jsoup:jsoup:1.20.1` and use it like this 
```java 
// Parse the HTML content with Jsoup
Document doc = Jsoup.parse(htmlContent);
// Test for a specific icon (e.g., <i class="fas fa-user"></i>)
Elements input = doc.select("input[name=username]");
assertThat(input.size()).isEqual(1)
```
* when using JTE with forms , use java record to store the form data , don't use classes with public fields.
* when adding new tests check always if there is any code duplication with previous existing tests and find way to avoid that code duplication





