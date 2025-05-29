package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
public final class Jtescheduleeventstep3Generated {
	public static final String JTE_NAME = "schedule-event-step3.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,57,57,57,57,57,57,57,57,57,57,64,64,64,0,0,0,0};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, de.bas.bodo.woodle.view.ScheduleEventStep3Form step3FormData) {
		jteOutput.writeContent("\n<!DOCTYPE html>\n<html>\n<head>\n    <title>Schedule Event - Step 3</title>\n    <style>\n        body {\n            font-family: Arial, sans-serif;\n            margin: 0;\n            padding: 20px;\n            text-align: center;\n        }\n        h1 {\n            color: #333;\n        }\n        .logo {\n            max-width: 200px;\n            margin-bottom: 20px;\n        }\n        form {\n            display: inline-block;\n            text-align: left;\n            max-width: 400px;\n            width: 100%;\n        }\n        label {\n            display: block;\n            margin-bottom: 5px;\n            font-weight: bold;\n        }\n        input[type=\"date\"] {\n            padding: 8px;\n            width: 100%;\n            margin-bottom: 10px;\n        }\n        button {\n            padding: 10px 20px;\n            background-color: #7a8c5c;\n            color: white;\n            border: none;\n            border-radius: 5px;\n            cursor: pointer;\n            margin-right: 10px;\n        }\n        .input-group {\n            position: relative;\n            margin-bottom: 15px;\n        }\n    </style>\n</head>\n<body>\n    <img src=\"/woodle-logo.jpeg\" alt=\"Woodle Logo\" class=\"logo\">\n    <h1>Schedule Event - Step 3</h1>\n    <form action=\"/schedule-event-step3\" method=\"post\">\n        <div class=\"input-group\">\n            <label for=\"expiryDate\">Expiry Date</label>\n            <input type=\"date\" id=\"expiryDate\" name=\"expiryDate\" required");
		var __jte_html_attribute_0 = step3FormData.expiryDate() != null ? step3FormData.expiryDate() : "";
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_0);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(">\n        </div>\n\n        <button type=\"button\" onclick=\"window.history.back()\">Back</button>\n        <button type=\"submit\">Create the poll</button>\n    </form>\n</body>\n</html> ");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		de.bas.bodo.woodle.view.ScheduleEventStep3Form step3FormData = (de.bas.bodo.woodle.view.ScheduleEventStep3Form)params.get("step3FormData");
		render(jteOutput, jteHtmlInterceptor, step3FormData);
	}
}
