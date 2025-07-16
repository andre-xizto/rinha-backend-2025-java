package dev.buskopan.rinha_backend_2025;

import jakarta.annotation.PostConstruct;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PaymentService {

    @Value("${URL_DB}")
    private String urlDb;
    @Value("${USER_DB}")
    private String dbUser;
    @Value("${NAME_DB}")
    private String dbName;
    @Value("${PASSWORD_DB}")
    private String dbPassword;

    private String urlConnection;

    private final OkHttpClient client;

    public PaymentService(OkHttpClient client) {
        this.client = client;
    }

    @PostConstruct
    private void init() {
        final String port = "5432";
        this.urlConnection = String.format(
                "jdbc:postgresql://%s:%s/%s",
                urlDb, port, dbName
        );
    }

    public void process(PaymentRequest req) {
        FormBody body = new FormBody.Builder()
                .add("correlationId", req.correlationId().toString())
                .add("amount", String.valueOf(req.amount()))
                .add("requestedAt", LocalDateTime.now().toString())
                .build();

        Request requestDefault = new Request.Builder()
                .url("http://payment-processor-default:8080/payments")
                .post(body)
                .build();

        try (Response response = client.newCall(requestDefault).execute()) {
            String respBody = response.body().string();
            System.out.println("Default response: " + respBody);
            System.out.println("Default HTTP code: " + response.code());

            if (response.isSuccessful()) {
                insertDB(req, "default");
                return;
            }
        } catch (IOException e) {
            System.err.println("Erro no default processor: " + e.getMessage());
        }

        Request requestFallback = new Request.Builder()
                .url("http://payment-processor-fallback:8080/payments")
                .post(body)
                .build();

        try (Response responseFallback = client.newCall(requestFallback).execute()) {
            String fallbackBody = responseFallback.body().string();
            System.out.println("Fallback response: " + fallbackBody);
            System.out.println("Fallback HTTP code: " + responseFallback.code());

            if (responseFallback.isSuccessful()) {
                insertDB(req, "fallback");
            } else {
                System.err.println("Fallback também falhou com código: "
                        + responseFallback.code());
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro no fallback processor", e);
        }
    }


    private void insertDB(PaymentRequest req, String processorType) {
        LocalDateTime now = LocalDateTime.now();
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO payments ")
                .append("(correlationId, amount, requested_at, processor) VALUES (")
                .append("'").append(req.correlationId()).append("'").append(",")
                .append(req.amount()).append(",")
                .append("'").append(now).append("'").append(",")
                .append("'").append(processorType).append("'")
                .append(")");

        try (Connection con = DriverManager.getConnection(urlConnection, dbUser, dbPassword)) {
            try (Statement stmt = con.createStatement()) {
                int executed = stmt.executeUpdate(sb.toString());
                System.out.println("ITEMS INSERTED: " + executed);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
