import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RedisClient {

    private static void writeCommand(
            OutputStream output, List<String> parts)
            throws IOException {

        output.write(("*" + parts.size() + "\r\n")
                .getBytes(StandardCharsets.UTF_8));

        for (String part : parts) {

            byte[] bytes = part.getBytes(StandardCharsets.UTF_8);

            output.write(("$" + bytes.length + "\r\n")
                    .getBytes(StandardCharsets.UTF_8));

            output.write(bytes);
            output.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }

        output.flush();
    }

    private static String readLine(InputStream input)
            throws IOException {

        ByteArrayOutputStream bytes =
                new ByteArrayOutputStream();

        int current;

        while ((current = input.read()) != -1) {

            if (current == '\r') {
                if (input.read() != '\n') {
                    throw new IOException("Invalid response.");
                }

                return new String(
                        bytes.toByteArray(),
                        StandardCharsets.UTF_8);
            }

            bytes.write(current);
        }

        throw new EOFException("Server disconnected.");
    }

    private static Object readResponse(InputStream input)
            throws IOException {

        int type = input.read();

        if (type == -1) {
            throw new EOFException("Server disconnected.");
        }

        String line = readLine(input);

        switch (type) {

            case '+':
            case '-':
                return line;

            case ':':
                return Long.parseLong(line);

            case '$': {

                int length = Integer.parseInt(line);

                if (length == -1) {
                    return null;
                }

                if (length < 0 || length > 1024 * 1024) {
                    throw new IOException("Invalid response length.");
                }

                byte[] bytes = new byte[length];

                int position = 0;

                while (position < length) {
                    int count = input.read(
                            bytes, position, length - position);

                    if (count == -1) {
                        throw new EOFException();
                    }

                    position += count;
                }

                if (input.read() != '\r' ||
                        input.read() != '\n') {
                    throw new IOException("Invalid response ending.");
                }

                return new String(bytes, StandardCharsets.UTF_8);
            }

            case '*': {

                int count = Integer.parseInt(line);

                if (count == -1) {
                    return null;
                }

                if (count < 0 || count > 100000) {
                    throw new IOException("Invalid array size.");
                }

                List<Object> values = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    values.add(readResponse(input));
                }

                return values;
            }

            default:
                throw new IOException("Unknown response type.");
        }
    }

    private static List<String> parseCommand(String line) {

        String trimmed = line.trim();

        if (trimmed.isEmpty()) {
            return new ArrayList<>();
        }

        String[] parts;

        if (trimmed.regionMatches(true, 0, "SET ", 0, 4)) {
            parts = trimmed.split("\\s+", 3);
        } else {
            parts = trimmed.split("\\s+");
        }

        return new ArrayList<>(Arrays.asList(parts));
    }

    private static void printResponse(Object response) {

        if (response == null) {
            System.out.println("(nil)");

        } else if (response instanceof List<?>) {

            List<?> values = (List<?>) response;

            if (values.isEmpty()) {
                System.out.println("(empty list)");
            }

            for (Object value : values) {
                System.out.println(value);
            }

        } else {
            System.out.println(response);
        }
    }

    public static void main(String[] args) {

        try (
            Socket socket = new Socket("localhost", 6379);

            InputStream input = new BufferedInputStream(
                    socket.getInputStream());

            OutputStream output = new BufferedOutputStream(
                    socket.getOutputStream());

            Scanner scanner = new Scanner(System.in)
        ) {

            System.out.println("==============================");
            System.out.println("       MyRedis Client 2.0");
            System.out.println("==============================");
            System.out.println("Connected successfully!");
            System.out.println();
            System.out.println("Available commands:");
            System.out.println("SET key value");
            System.out.println("GET key");
            System.out.println("DEL key [key ...]");
            System.out.println("EXISTS key [key ...]");
            System.out.println("EXPIRE key seconds");
            System.out.println("TTL key");
            System.out.println("KEYS pattern");
            System.out.println("DBSIZE");
            System.out.println("FLUSHDB");
            System.out.println("SAVE");
            System.out.println("PING");
            System.out.println("QUIT");
            System.out.println();

            while (true) {

                System.out.print("MyRedis> ");

                String line = scanner.nextLine();

                List<String> command = parseCommand(line);

                if (command.isEmpty()) {
                    continue;
                }

                writeCommand(output, command);

                Object response = readResponse(input);

                printResponse(response);

                if (command.get(0).equalsIgnoreCase("QUIT")) {
                    break;
                }
            }

        } catch (ConnectException e) {
            System.out.println(
                    "Cannot connect. Start RedisServer first.");

        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}