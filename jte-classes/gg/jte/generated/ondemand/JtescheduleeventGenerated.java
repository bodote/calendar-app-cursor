package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
public final class JtescheduleeventGenerated {
	public static final String JTE_NAME = "schedule-event.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,0,72,72,72,72,72,72,72,72,72,72,75,75,75,75,75,75,75,75,75,78,78,78,78,78,78,78,78,78,81,81,81,86,93,93,93,0,0,0,0};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, de.bas.bodo.woodle.view.ScheduleEventForm formData) {
		jteOutput.writeContent("\n<!DOCTYPE html>\n<html>\n<head>\n    <title>Schedule Event</title>\n    <style>\n        body {\n            font-family: Arial, sans-serif;\n            margin: 0;\n            padding: 20px;\n            text-align: center;\n        }\n        h1 {\n            color: #333;\n        }\n        .logo {\n            max-width: 200px;\n            margin-bottom: 20px;\n        }\n        form {\n            display: inline-block;\n            text-align: left;\n        }\n        label {\n            display: block;\n            margin-bottom: 5px;\n            font-weight: bold;\n        }\n        input[type=\"text\"], input[type=\"email\"] {\n            padding: 8px;\n            width: 100%;\n            margin-bottom: 10px;\n        }\n        textarea {\n            width: 100%;\n            height: 120px;\n            margin-bottom: 10px;\n        }\n        button {\n            padding: 10px 20px;\n            background-color: #7a8c5c;\n            color: white;\n            border: none;\n            border-radius: 5px;\n            cursor: pointer;\n        }\n        .optional {\n            margin-top: 20px;\n        }\n        .optional-toggle {\n            color: #007bff;\n            cursor: pointer;\n            text-decoration: underline;\n        }\n    </style>\n    <script>\n        function toggleOptional() {\n            var section = document.getElementById('optional-section');\n            if (section.style.display === 'none') {\n                section.style.display = 'block';\n            } else {\n                section.style.display = 'none';\n            }\n        }\n    </script>\n</head>\n<body>\n    <img src=\"/woodle-logo.jpeg\" alt=\"Woodle Logo\" class=\"logo\">\n    <h1>Schedule Event</h1>\n    <form action=\"/schedule-event\" method=\"post\">\n        <label for=\"name\">Your name *</label>\n        <input type=\"text\" id=\"name\" name=\"name\" required");
		var __jte_html_attribute_0 = formData.name() != null ? formData.name() : "";
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_0)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_0);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(">\n\n        <label for=\"email\">Your email address *</label>\n        <input type=\"email\" id=\"email\" name=\"email\" required");
		var __jte_html_attribute_1 = formData.email() != null ? formData.email() : "";
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_1)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_1);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(">\n\n        <label for=\"title\">Poll title *</label>\n        <input type=\"text\" id=\"title\" name=\"title\" required");
		var __jte_html_attribute_2 = formData.title() != null ? formData.title() : "";
		if (gg.jte.runtime.TemplateUtils.isAttributeRendered(__jte_html_attribute_2)) {
			jteOutput.writeContent(" value=\"");
			jteOutput.setContext("input", "value");
			jteOutput.writeUserContent(__jte_html_attribute_2);
			jteOutput.setContext("input", null);
			jteOutput.writeContent("\"");
		}
		jteOutput.writeContent(">\n\n        <label for=\"description\">Description</label>\n        <textarea id=\"description\" name=\"description\">");
		jteOutput.setContext("textarea", null);
		jteOutput.writeUserContent(formData.description() != null ? formData.description() : "");
		jteOutput.writeContent("</textarea>\n\n        <div class=\"optional\">\n            <span class=\"optional-toggle\" onclick=\"toggleOptional()\">Optional parameters &#9660;</span>\n            <div id=\"optional-section\" style=\"display:none;\">\n                ");
		jteOutput.writeContent("\n            </div>\n        </div>\n\n        <button type=\"submit\">Next</button>\n    </form>\n</body>\n</html> ");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		de.bas.bodo.woodle.view.ScheduleEventForm formData = (de.bas.bodo.woodle.view.ScheduleEventForm)params.get("formData");
		render(jteOutput, jteHtmlInterceptor, formData);
	}
}
