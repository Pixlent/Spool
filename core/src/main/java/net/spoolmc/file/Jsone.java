package net.spoolmc.file;

import com.google.gson.Gson;
import net.spoolmc.logger.Logger;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for preprocessing JSON content by replacing variables in the text.
 */
public class Jsone {
    private final Map<String, String> variables = new HashMap<>();
    private final String variablePrefix;
    private final Gson gson = new Gson();
    private final Logger logger = new Logger("Jsone");

    /**
     * Constructs a JsonPreProcessor with the ability to add or get variables
     *
     * @param variablePrefix The prefix used before a variable (for example "$")
     */
    public Jsone(String variablePrefix) {
        this.variablePrefix = variablePrefix;
    }

    /**
     * Adds a variable to the list of variables
     *
     * @param namespace The namespace used in the variable
     * @param key The key used after the namespace to identify the variable
     * @param value The value which the variable correlates to
     */
    public void addVariable(String namespace, String key, String value) {
        variables.put(variablePrefix + namespace + ":" + key, value);
    }

    /**
     * Gets a variable from the list of variables
     *
     * @param namespace The namespace used in the variable
     * @param key The key used after the namespace to identify the variable
     * @return The variable returned by the method
     */
    public String getVariable(String namespace, String key) {
        String variable = variables.get(variablePrefix + namespace + ":" + key);
        if (variable == null) {
            logger.error(variablePrefix + namespace + ":" + key + " is not a variable");
            return null;
        }
        return variable;
    }

    /**
     * Processes JSON content by replacing variables with their values.
     *
     * @param json The string from which the object is to be deserialized
     * @param typeOfT The specific generalized type of src
     * @return An object of type T from the string. Returns null if json is null or if json is empty.
     */
    public <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(processJsonContent(json), typeOfT);
    }

    /**
     * Processes JSON content by replacing variables with their values.
     *
     * @param content The JSON content with variables to replace.
     * @return The JSON content with variables replaced by their values.
     */
    public String processJsonContent(String content) {
        return processString(content);
    }

    private String processString(String input) {
        Pattern pattern = Pattern.compile("\\\\?@([^:]+):([^@]+)"); // Match @namespace:variable_name
        Matcher matcher = pattern.matcher(input);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String variableKey = matcher.group(0);
            String variableValue = variables.get(variableKey);
            if (variableValue != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(variableValue));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
