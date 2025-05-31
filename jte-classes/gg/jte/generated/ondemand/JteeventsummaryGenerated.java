package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
public final class JteeventsummaryGenerated {
	public static final String JTE_NAME = "event-summary.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,85,85,85,85,89,89,89,97,97,97,101,101,101,101,101,101,105,105,105,113,113,113,117,117,117,124,124,124,131,131,131,0,1,2,3,3,3,3};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, de.bas.bodo.woodle.view.ScheduleEventStep1Form step1FormData, de.bas.bodo.woodle.view.ScheduleEventStep2Form step2FormData, de.bas.bodo.woodle.view.ScheduleEventStep3Form step3FormData, String pollUrl) {
		jteOutput.writeContent("\n<!DOCTYPE html>\n<html>\n<head>\n    <title>Poll Summary</title>\n    <style>\n        body {\n            font-family: Arial, sans-serif;\n            margin: 0;\n            padding: 20px;\n            text-align: center;\n        }\n        h1 {\n            color: #333;\n        }\n        .logo {\n            max-width: 200px;\n            margin-bottom: 20px;\n        }\n        .summary-container {\n            display: inline-block;\n            text-align: left;\n            max-width: 600px;\n            width: 100%;\n            margin: 20px auto;\n            padding: 20px;\n            border: 1px solid #ddd;\n            border-radius: 5px;\n        }\n        .summary-section {\n            margin-bottom: 20px;\n        }\n        .summary-section h2 {\n            color: #7a8c5c;\n            margin-bottom: 10px;\n        }\n        .summary-item {\n            margin-bottom: 10px;\n        }\n        .summary-label {\n            font-weight: bold;\n            color: #666;\n        }\n        .success-message {\n            color: #28a745;\n            font-size: 1.2em;\n            margin: 20px 0;\n        }\n        .poll-url {\n            background-color: #f8f9fa;\n            padding: 10px;\n            border-radius: 5px;\n            margin: 10px 0;\n            word-break: break-all;\n        }\n        .button {\n            padding: 10px 20px;\n            background-color: #7a8c5c;\n            color: white;\n            border: none;\n            border-radius: 5px;\n            cursor: pointer;\n            text-decoration: none;\n            display: inline-block;\n            margin-top: 20px;\n        }\n    </style>\n</head>\n<body>\n    <img src=\"/woodle-logo.jpeg\" alt=\"Woodle Logo\" class=\"logo\">\n    <h1>Poll Summary</h1>\n\n    <div class=\"summary-container\">\n        <div class=\"success-message\">\n            Your poll has been created successfully!\n        </div>\n\n        <div class=\"summary-section\" data-test-section=\"poll-details\">\n            <h2>Poll Details</h2>\n            <div class=\"summary-item\" data-test-item=\"title\">\n                <span class=\"summary-label\">Title:</span>\n                <span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(step1FormData.title());
		jteOutput.writeContent("</span>\n            </div>\n            <div class=\"summary-item\" data-test-item=\"description\">\n                <span class=\"summary-label\">Description:</span>\n                <span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(step1FormData.description());
		jteOutput.writeContent("</span>\n            </div>\n        </div>\n\n        <div class=\"summary-section\" data-test-section=\"event-details\">\n            <h2>Event Details</h2>\n            <div class=\"summary-item\" data-test-item=\"date\">\n                <span class=\"summary-label\">Date:</span>\n                <span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(step2FormData.date());
		jteOutput.writeContent("</span>\n            </div>\n            <div class=\"summary-item\" data-test-item=\"time\">\n                <span class=\"summary-label\">Time:</span>\n                <span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(step2FormData.startTime());
		jteOutput.writeContent(" - ");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(step2FormData.endTime());
		jteOutput.writeContent("</span>\n            </div>\n            <div class=\"summary-item\" data-test-item=\"expiry-date\">\n                <span class=\"summary-label\">Expiry Date:</span>\n                <span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(step3FormData.expiryDate());
		jteOutput.writeContent("</span>\n            </div>\n        </div>\n\n        <div class=\"summary-section\" data-test-section=\"organizer-details\">\n            <h2>Organizer Details</h2>\n            <div class=\"summary-item\" data-test-item=\"name\">\n                <span class=\"summary-label\">Name:</span>\n                <span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(step1FormData.name());
		jteOutput.writeContent("</span>\n            </div>\n            <div class=\"summary-item\" data-test-item=\"email\">\n                <span class=\"summary-label\">Email:</span>\n                <span>");
		jteOutput.setContext("span", null);
		jteOutput.writeUserContent(step1FormData.email());
		jteOutput.writeContent("</span>\n            </div>\n        </div>\n\n        <div class=\"summary-section\" data-test-section=\"poll-url\">\n            <h2>Poll URL</h2>\n            <div class=\"poll-url\">\n                ");
		jteOutput.setContext("div", null);
		jteOutput.writeUserContent(pollUrl);
		jteOutput.writeContent("\n            </div>\n        </div>\n\n        <a href=\"/\" class=\"button\">Back to Home</a>\n    </div>\n</body>\n</html> ");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		de.bas.bodo.woodle.view.ScheduleEventStep1Form step1FormData = (de.bas.bodo.woodle.view.ScheduleEventStep1Form)params.get("step1FormData");
		de.bas.bodo.woodle.view.ScheduleEventStep2Form step2FormData = (de.bas.bodo.woodle.view.ScheduleEventStep2Form)params.get("step2FormData");
		de.bas.bodo.woodle.view.ScheduleEventStep3Form step3FormData = (de.bas.bodo.woodle.view.ScheduleEventStep3Form)params.get("step3FormData");
		String pollUrl = (String)params.get("pollUrl");
		render(jteOutput, jteHtmlInterceptor, step1FormData, step2FormData, step3FormData, pollUrl);
	}
}
