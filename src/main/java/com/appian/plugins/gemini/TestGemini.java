package com.appian.plugins.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestGemini {

    public static void main(String[] args) {

//        System.out.println("===== GEMINI TEST STARTED =====");

        try {
            GeminiService service = new GeminiService();

            String apiKey = "AIzaSyB1JYsH9H43-fb48MXxdIkyD3TZ_bsGpoo"; // 🔥 never hardcode in real apps
            String model = "gemini-3.1-flash-lite-preview";
            Double temperature = 0.7;
            String prompt = "Expian about java";
            String systemPrompt = "You are a helpful assistant";

//            System.out.println("🔹 Calling Gemini API...");
//            System.out.println("Model: " + model);
//            System.out.println("Prompt: " + prompt);

            String response = service.callGeminiAPI(
                    apiKey,
                    model,
                    temperature,
                    prompt,
                    systemPrompt
            );

            System.out.println("\n===== GEMINI RESPONSE =====");

            // ✅ Pretty JSON print
//            ObjectMapper mapper = new ObjectMapper();
//            Object json = mapper.readValue(response, Object.class);
//            System.out.println(
//                    mapper.writerWithDefaultPrettyPrinter()
//                          .writeValueAsString(json)
//            );
            System.out.println(response);
            System.out.println("===== TEST COMPLETED SUCCESSFULLY =====");

        } catch (Exception e) {

            System.out.println("Error Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}