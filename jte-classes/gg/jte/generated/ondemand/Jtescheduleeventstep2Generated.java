package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
public final class Jtescheduleeventstep2Generated {
	public static final String JTE_NAME = "schedule-event-step2.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,57,57,57,57,57,57,57,57,57,57,62,62,62,62,62,62,62,62,62,67,67,67,67,67,67,67,67,67,74,74,74,0,0,0,0};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, de.bas.bodo.woodle.view.ScheduleEventStep2Form step2FormData) {
		jteOutput.writeContent("\n<!DOCTYPE html>\n<html>\n<head>\n    <title>Schedule Event - Step 2</title>\n    <style>\n        body {\n            font-family: Arial, sans-serif;\n            margin: 0;\n            padding: 20px;\n            text-align: center;\n        }\n        h1 {\n            color: #333;\n        }\n        .logo {\n            max-width: 200px;\n            margin-bottom: 20px;\n        }\n        form {\n            display: inline-block;\n            text-align: left;\n            max-width: 400px;\n            width: 100%;\n        }\n        label {\n            display: block;\n            margin-bottom: 5px;\n            font-weight: bold;\n        }\n        input[type=\"date\"], input[type=\"time\"] {\n            padding: 8px;\n            width: 100%;\n            margin-bottom: 10px;\n        }\n        button {\n            padding: 10px 20px;\n            background-color: #7a8c5c;\n            color: white;\n            border: none;\n            border-radius: 5px;\n            cursor: pointer;\n            margin-right: 10px;\n        }\n        .input-group {\n            position: relative;\n            margin-bottom: 15px;\n        }\n    </style>\n</head>\n<body>\n    <img src=\"/woodle-logo.jpeg\" alt=\"Woodle Logo\" class=\"logo\">\n    <h1>Schedule Event - Step 2</h1>\n    <form action=\"/schedule-event-step2\" method=\"post\">\n        <div class=\"input-group\">\n            <label for=\"date\">Choose a date</label>\n            <input type=\"date\" id=\"date\" name=\"date\" required");
		var __jte_html_attribute_0 = step2FormData.date() != null ? step2FormData.date() : "";
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_0);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(">\n        </div>\n\n        <div class=\"input-group\">\n            <label for=\"startTime\">Start time</label>\n            <input type=\"time\" id=\"startTime\" name=\"startTime\" required");
		var __jte_html_attribute_1 = step2FormData.startTime() != null ? step2FormData.startTime() : "";
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_1);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(">\n        </div>\n\n        <div class=\"input-group\">\n            <label for=\"endTime\">End time</label>\n            <input type=\"time\" id=\"endTime\" name=\"endTime\" required");
		var __jte_html_attribute_2 = step2FormData.endTime() != null ? step2FormData.endTime() : "";
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_2)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_2);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(">\n        </div>\n\n        <button type=\"button\" onclick=\"window.history.back()\">Back</button>\n        <button type=\"submit\">Next</button>\n    </form>\n</body>\n</html> ");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		de.bas.bodo.woodle.view.ScheduleEventStep2Form step2FormData = (de.bas.bodo.woodle.view.ScheduleEventStep2Form)params.get("step2FormData");
		render(jteOutput, jteHtmlInterceptor, step2FormData);
	}
}
