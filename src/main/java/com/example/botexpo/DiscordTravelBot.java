package com.example.botexpo;

import net.dv8tion.jda.api.JDA;

import net.dv8tion.jda.api.JDABuilder;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

import net.dv8tion.jda.api.requests.GatewayIntent;

import okhttp3.*;

import org.json.JSONArray;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;



import java.io.IOException;



@Component

public class DiscordTravelBot extends ListenerAdapter {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    private static final OkHttpClient client = new OkHttpClient();



    @Value("${groq.api.key:}")

    private String groqApiKey;



    @Value("${groq.model:mixtral-8x7b-32768}")

    private String groqModel;



    private final String botPrefix = "!viaje";



    @Override

    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) return;



        String message = event.getMessage().getContentRaw();



        if (message.startsWith(botPrefix)) {

            String question = message.substring(botPrefix.length()).trim();



            if (question.isEmpty()) {

                event.getChannel().sendMessage("Por favor, hazme una pregunta sobre viajes. Ejemplo: `" + botPrefix + " ¿Cuál es el mejor lugar para viajar en verano?`").queue();

                return;

            }



            event.getChannel().sendTyping().queue();



            try {

                String response = getGroqResponse(question);

                if (response.length() > 2000) {

                    sendLongMessage(event, response);

                } else {

                    event.getChannel().sendMessage(response).queue();

                }

            } catch (IOException e) {

                event.getChannel().sendMessage("❌ Error al conectar con Groq API: " + e.getMessage()).queue();

                System.err.println("Error en Groq API: " + e.getMessage());

            }

        }

    }



    private String getGroqResponse(String userQuestion) throws IOException {

        JSONObject requestBody = new JSONObject();

        requestBody.put("model", groqModel);

        requestBody.put("temperature", 0.7);

        requestBody.put("max_tokens", 1024);



        JSONArray messages = new JSONArray();



        JSONObject systemMessage = new JSONObject();

        systemMessage.put("role", "system");

        systemMessage.put("content", "Eres un asistente experto en viajes. Proporciona información útil, consejos prácticos y recomendaciones sobre destinos turísticos, transporte, alojamiento y experiencias de viaje. Sé amable y conciso.");

        messages.put(systemMessage);



        JSONObject userMessage = new JSONObject();

        userMessage.put("role", "user");

        userMessage.put("content", userQuestion);

        messages.put(userMessage);



        requestBody.put("messages", messages);



        RequestBody body = RequestBody.create(

                requestBody.toString(),

                MediaType.get("application/json; charset=utf-8")

        );



        Request request = new Request.Builder()

                .url(GROQ_URL)

                .header("Authorization", "Bearer " + groqApiKey)

                .post(body)

                .build();



        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {

                String errorBody = response.body() != null ? response.body().string() : "Sin detalles";

                throw new IOException("API error: " + response.code() + " - " + errorBody);

            }



            String responseBody = response.body().string();

            JSONObject jsonResponse = new JSONObject(responseBody);



            return jsonResponse.getJSONArray("choices")

                    .getJSONObject(0)

                    .getJSONObject("message")

                    .getString("content");

        }

    }



    private void sendLongMessage(MessageReceivedEvent event, String message) {

        int chunkSize = 1900;

        for (int i = 0; i < message.length(); i += chunkSize) {

            int endIndex = Math.min(i + chunkSize, message.length());

            String chunk = message.substring(i, endIndex);

            event.getChannel().sendMessage(chunk).queue();

        }

    }

}
