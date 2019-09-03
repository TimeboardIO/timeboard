package kronops.ui;

import java.util.HashMap;
import java.util.Map;

public class TemplateResolver {

    private static final Map<String, String> templates = new HashMap<>();

    static {
        templates.put("/", "home.html");
        templates.put("/projects", "projects.html");
    }

    private TemplateResolver() {
    }

    public static String getTemplateName(String path) {
        return templates.get(path);
    }


}
