package com.openbravo.pos.sales;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TestBranches {
    public static void main(String[] args) {
        String user = "sdsmsmds";
        String pass = "123456";
        // We will try /api/BranchOffice and /BranchOffice to see which one works.
        String[] urls = {
            "https://apisandbox.facturama.mx/api/BranchOffice",
            "https://apisandbox.facturama.mx/BranchOffice"
        };
        
        for (String url : urls) {
            System.out.println("Connecting to Facturama Sandbox: " + url + "...");
            try {
                String auth = user + ":" + pass;
                String authHeader = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", authHeader)
                        .GET()
                        .build();
                        
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int code = response.statusCode();
                System.out.println("Status Code: " + code);
                System.out.println("Response Body:");
                System.out.println(response.body());
                System.out.println("----------------------------------------------");
            } catch (Exception e) {
                System.err.println("Error connecting: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
