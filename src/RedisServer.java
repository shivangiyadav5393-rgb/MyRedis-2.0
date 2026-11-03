import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class RedisServer {

    private static final int PORT = 6379;

    static final RedisDatabase database = new RedisDatabase();

    private static final ExecutorService clientPool =
            Executors.newCachedThreadPool();

    private static final ScheduledExecutorService expiryService =
            Executors.newSingleThreadScheduledExecutor();

    private static void handleClient(Socket client) {

        System.out.println("Client connected: "
                + client.getRemoteSocketAddress());

        RedisCommandHandler handler =
                new RedisCommandHandler(database);

        try (
            InputStream input = new BufferedInputStream(
                    client.getInputStream());

            OutputStream output = new BufferedOutputStream(
                    client.getOutputStream())
        ) {

            while (true) {

                java.util.List<String> command =
                        RespProtocol.readCommand(input);

                if (command == null) {
                    break;
                }

                boolean keepRunning =
                        handler.handle(command, output);

                if (!keepRunning) {
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("Client connection ended.");

        } finally {
            try {
                client.close();
            } catch (IOException e) {
                System.out.println("Could not close client.");
            }

            System.out.println("Client disconnected.");
        }
    }

    public static void main(String[] args) {

        PersistenceManager.load(database);

        expiryService.scheduleAtFixedRate(() -> {
            try {
                if (database.cleanupExpired()) {
                    PersistenceManager.save(database);
                }
            } catch (Exception e) {
                System.out.println("Expiration cleanup error.");
            }
        }, 1, 1, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Saving database...");
            PersistenceManager.save(database);
            clientPool.shutdown();
            expiryService.shutdown();
        }));

        try (ServerSocket server = new ServerSocket(PORT)) {

            System.out.println("==============================");
            System.out.println("       MyRedis Server 2.0");
            System.out.println("==============================");
            System.out.println("Port: " + PORT);
            System.out.println("Protocol: RESP2 subset");
            System.out.println("Persistence: Enabled");
            System.out.println("Waiting for clients...");

            while (true) {
                Socket client = server.accept();
                clientPool.execute(() -> handleClient(client));
            }

        } catch (BindException e) {
            System.out.println("Port 6379 is already in use.");

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());

        } finally {
            clientPool.shutdown();
            expiryService.shutdown();
        }
    }
}