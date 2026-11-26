package com.kroma.ecommerce.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Configuración de conexión a MongoDB.
 * Intenta conectar al servidor MongoDB externo configurado; si no está disponible,
 * inicia automáticamente un servidor MongoDB embebido compatible con el protocolo MongoDB.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.kroma.ecommerce.model.repository")
public class MongoConfig {

    @Value("${kroma.mongodb.uri:mongodb://127.0.0.1:27017/kroma_cosmetics}")
    private String mongoUri;

    @Value("${kroma.mongodb.embedded-fallback:true}")
    private boolean embeddedFallback;

    @Value("${kroma.mongodb.force-embedded:false}")
    private boolean forceEmbedded;

    private MongoServer embeddedServer;
    private String activeMode = "disconnected";

    @Bean
    public MongoClient mongoClient() {
        if (!forceEmbedded && isExternalMongoReachable(mongoUri)) {
            activeMode = "external-mongodb (" + mongoUri + ")";
            System.out.println("[MongoDB] Conectado a servidor externo: " + mongoUri);
            return MongoClients.create(mongoUri);
        }

        if (embeddedFallback || forceEmbedded) {
            embeddedServer = new MongoServer(new MemoryBackend());
            InetSocketAddress address = embeddedServer.bind();
            String embeddedUri = "mongodb://" + address.getHostString() + ":" + address.getPort() + "/kroma_cosmetics";
            activeMode = "embedded-mongo-java-server (" + embeddedUri + ")";
            System.out.println("[MongoDB] Servidor embebido iniciado en: " + embeddedUri);
            return MongoClients.create(embeddedUri);
        }

        throw new IllegalStateException("No fue posible conectarse a MongoDB en " + mongoUri);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, "kroma_cosmetics");
    }

    public String getActiveMode() {
        return activeMode;
    }

    private boolean isExternalMongoReachable(String uri) {
        try {
            // Si es Atlas (mongodb+srv://), intentamos directamente con MongoClients
            if (uri.startsWith("mongodb+srv://")) {
                return true;
            }
            String clean = uri.replace("mongodb://", "");
            String hostPort = clean.split("/")[0];
            if (hostPort.contains("@")) {
                hostPort = hostPort.substring(hostPort.indexOf('@') + 1);
            }
            String[] parts = hostPort.split(":");
            String host = parts[0];
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 27017;

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 800);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (embeddedServer != null) {
            embeddedServer.shutdownNow();
        }
    }
}
