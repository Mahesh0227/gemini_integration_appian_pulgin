package com.appian.plugins.gemini;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.appiancorp.suiteapi.process.framework.Order;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
@PaletteInfo(
	    paletteCategory = "Integration Services",
	    palette = "AI / Gemini"
	)
@Order({ "apiKey", "model", "temperature","prompt" ,"systemPrompt"})
public class GeminiService {

    public String callGeminiAPI(String apiKey,
                                 String model,
                                 Double temperature,
                                 String prompt,
                                 String systemPrompt) throws Exception {

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;
  

    		
        CloseableHttpClient client = HttpClients.createDefault();

        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");

        String requestBody = buildRequest(prompt, systemPrompt, temperature);

        request.setEntity(new StringEntity(requestBody, "UTF-8"));

        CloseableHttpResponse response = client.execute(request);

        String result = EntityUtils.toString(response.getEntity());

//        System.out.println("RAW RESPONSE: " + result); // 🔥 DEBUG

        client.close();

        return buildFinalOutput(result, prompt);
    }
private String buildFinalOutput(String jsonResponse, String prompt) throws Exception {

    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(jsonResponse);

    // Handle error
    if (root.has("error")) {
        return "{ \"error\": \"" + root.path("error").path("message").asText() + "\" }";
    }

    // Extract response text
    String responseText = extractText(jsonResponse);

    // Extract tokens
    JsonNode usage = root.path("usageMetadata");

    int inputTokens = usage.path("promptTokenCount").asInt();
    int outputTokens = usage.path("candidatesTokenCount").asInt();

    // ✅ Create JSON object instead of string
    JsonNode finalJson = mapper.createObjectNode()
            .put("prompt", prompt)
            .set("model_data",
                    mapper.createObjectNode()
                            .put("response", responseText)
                            .put("input_tokens", inputTokens)
                            .put("output_tokens", outputTokens)
            );

    // ✅ Pretty print here (moved from main)
    return mapper.writerWithDefaultPrettyPrinter()
                 .writeValueAsString(finalJson);
}
    
    
    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
    private String buildRequest(String prompt, String systemPrompt, Double temperature) {

        String finalPrompt = (systemPrompt != null && !systemPrompt.isEmpty())
                ? systemPrompt + "\n" + prompt
                : prompt;

        return "{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"parts\": [\n" +
                "        {\"text\": \"" + finalPrompt.replace("\"", "\\\"") + "\"}\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"generationConfig\": {\n" +
                "    \"temperature\": " + temperature + "\n" +
                "  }\n" +
                "}";
    }

    private String extractText(String jsonResponse) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonResponse);

        // 🔥 Handle API errors
        if (root.has("error")) {
            return "API ERROR: " + root.path("error").path("message").asText();
        }

        JsonNode candidates = root.path("candidates");

        if (candidates.isMissingNode() || candidates.size() == 0) {
            return "No candidates in response: " + jsonResponse;
        }

        JsonNode parts = candidates.get(0)
                .path("content")
                .path("parts");

        if (parts.isMissingNode() || parts.size() == 0) {
            return "Invalid response structure: " + jsonResponse;
        }

        return parts.get(0).path("text").asText();
    }
}