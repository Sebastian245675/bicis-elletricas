package com.openbravo.pos.sales;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CreateBranch {
    public static void main(String[] args) {
        String user = "sdsmsmds";
        String pass = "123456";
        String url = "https://apisandbox.facturama.mx/BranchOffice";
        
        String payload = "{"
                + "\"Name\": \"Matriz\","
                + "\"Description\": \"Sucursal Principal\","
                + "\"Address\": {"
                + "  \"Street\": \"Calle Ficticia\","
                + "  \"ExteriorNumber\": \"100\","
                + "  \"Neighborhood\": \"Centro\","
                + "  \"ZipCode\": \"26015\","
                + "  \"Municipality\": \"Piedras Negras\","
                + "  \"State\": \"Coahuila\","
                + "  \"Country\": \"MEXICO\""
                + "}"
                + "}";
                
        System.out.println("Registering branch office in Facturama Sandbox: " + url + "...");
        try {
            String auth = user + ":" + pass;
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();
                    
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            System.out.println("Status Code: " + code);
            System.out.println("Response Body:");
            System.out.println(response.body());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
