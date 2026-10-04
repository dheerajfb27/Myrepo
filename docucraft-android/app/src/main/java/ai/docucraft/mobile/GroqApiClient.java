package ai.docucraft.mobile;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class GroqApiClient {
    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "openai/gpt-oss-120b";

    public interface Callback {
        void onSuccess(String text);
        void onError(String message);
    }

    private GroqApiClient() {}

    public static void generate(String apiKey, String prompt, Callback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(ENDPOINT);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(90000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Bearer " + apiKey);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                JSONObject body = new JSONObject();
                body.put("model", MODEL);
                body.put("temperature", 0.35);
                body.put("max_completion_tokens", 4096);

                JSONArray messages = new JSONArray();
                JSONObject system = new JSONObject();
                system.put("role", "system");
                system.put("content",
                        "You are DocuCraft AI, an expert document and presentation assistant. " +
                        "Create professional, accurate, structured content. For resumes, optimize for ATS " +
                        "without inventing experience. For presentations, organize content into clear sections " +
                        "and slide-ready text.");
                messages.put(system);

                JSONObject user = new JSONObject();
                user.put("role", "user");
                user.put("content", prompt);
                messages.put(user);
                body.put("messages", messages);

                byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(payload);
                }

                int status = connection.getResponseCode();
                InputStream stream = status >= 200 && status < 300
                        ? connection.getInputStream() : connection.getErrorStream();
                String response = readAll(stream);

                if (status < 200 || status >= 300) {
                    String message = "Groq request failed (" + status + ")";
                    try {
                        JSONObject error = new JSONObject(response);
                        if (error.has("error")) {
                            JSONObject e = error.getJSONObject("error");
                            if (e.has("message")) message += ": " + e.getString("message");
                        }
                    } catch (Exception ignored) {}
                    callback.onError(message);
                    return;
                }

                JSONObject json = new JSONObject(response);
                JSONArray choices = json.optJSONArray("choices");
                if (choices == null || choices.length() == 0) {
                    callback.onError("Groq returned no response.");
                    return;
                }
                String text = choices.getJSONObject(0)
                        .getJSONObject("message")
                        .optString("content", "");
                if (text.trim().isEmpty()) {
                    callback.onError("Groq returned an empty response.");
                } else {
                    callback.onSuccess(text);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() == null ? "Unable to contact Groq." : e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private static String readAll(InputStream input) throws Exception {
        if (input == null) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }
}
