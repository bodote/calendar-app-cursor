package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
public final class JteindexGenerated {
	public static final String JTE_NAME = "index.jte";
	public static final int[] JTE_LINE_INFO = {33,33,33,33,33,33,33,33,33,33,33,33};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor) {
		jteOutput.writeContent("<!DOCTYPE html>\n<html>\n<head>\n    <title>Woodle</title>\n    <style>\n        body {\n            font-family: Arial, sans-serif;\n            margin: 0;\n            padding: 20px;\n            text-align: center;\n        }\n        h1 {\n            color: #333;\n        }\n        .logo {\n            max-width: 200px;\n            margin-bottom: 20px;\n        }\n        .button {\n            display: inline-block;\n            padding: 10px 20px;\n            background-color: #007bff;\n            color: white;\n            text-decoration: none;\n            border-radius: 5px;\n        }\n    </style>\n</head>\n<body>\n    <img src=\"/woodle-logo.jpeg\" alt=\"Woodle Logo\" class=\"logo\">\n    <h1>Woodle</h1>\n    <a href=\"/schedule-event\" class=\"button\">Schedule Event</a>\n</body>\n</html> ");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		render(jteOutput, jteHtmlInterceptor);
	}
}
