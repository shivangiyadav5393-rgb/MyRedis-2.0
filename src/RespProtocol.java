import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RespProtocol {

    public static List<String> readCommand(
            InputStream input) throws IOException {

        String firstLine = readLine(input);

        if (firstLine == null) {
            return null;
        }

        if (!firstLine.startsWith("*")) {
            throw new IOException("Expected RESP array.");
        }

        int count;

        try {
            count = Integer.parseInt(firstLine.substring(1));
        } catch (NumberFormatException e) {
            throw new IOException("Invalid array length.");
        }

        if (count < 1 || count > 1000) {
            throw new IOException("Invalid command length.");
        }

        List<String> command = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            String lengthLine = readLine(input);

            if (lengthLine == null ||
                    !lengthLine.startsWith("$")) {
                throw new IOException("Expected bulk string.");
            }

            int length;

            try {
                length = Integer.parseInt(
                        lengthLine.substring(1));
            } catch (NumberFormatException e) {
                throw new IOException("Invalid bulk length.");
            }

            if (length < 0 || length > 1024 * 1024) {
                throw new IOException("Invalid bulk length.");
            }

            byte[] bytes = new byte[length];

            int position = 0;

            while (position < length) {

                int read = input.read(
                        bytes, position, length - position);

                if (read == -1) {
                    throw new EOFException();
                }

                position += read;
            }

            if (input.read() != '\r' ||
                    input.read() != '\n') {
                throw new IOException("Invalid bulk ending.");
            }

            command.add(new String(
                    bytes, StandardCharsets.UTF_8));
        }

        return command;
    }

    private static String readLine(InputStream input)
            throws IOException {

        ByteArrayOutputStream bytes =
                new ByteArrayOutputStream();

        int current;

        while ((current = input.read()) != -1) {

            if (current == '\r') {

                if (input.read() != '\n') {
                    throw new IOException("Invalid line ending.");
                }

                return new String(
                        bytes.toByteArray(),
                        StandardCharsets.UTF_8);
            }

            bytes.write(current);

            if (bytes.size() > 1024) {
                throw new IOException("Line too long.");
            }
        }

        if (bytes.size() == 0) {
            return null;
        }

        throw new EOFException();
    }

    public static void writeSimple(
            OutputStream output, String value)
            throws IOException {

        writeText(output, "+" + value + "\r\n");
    }

    public static void writeError(
            OutputStream output, String value)
            throws IOException {

        writeText(output, "-" + value + "\r\n");
    }

    public static void writeInteger(
            OutputStream output, long value)
            throws IOException {

        writeText(output, ":" + value + "\r\n");
    }

    public static void writeBulk(
            OutputStream output, String value)
            throws IOException {

        if (value == null) {
            writeText(output, "$-1\r\n");
            return;
        }

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

        writeText(output, "$" + bytes.length + "\r\n");

        output.write(bytes);

        writeText(output, "\r\n");

        output.flush();
    }

    public static void writeArray(
            OutputStream output, List<String> values)
            throws IOException {

        writeText(output, "*" + values.size() + "\r\n");

        for (String value : values) {

            byte[] bytes = value.getBytes(
                    StandardCharsets.UTF_8);

            writeText(output, "$" + bytes.length + "\r\n");

            output.write(bytes);

            writeText(output, "\r\n");
        }

        output.flush();
    }

    private static void writeText(
            OutputStream output, String text)
            throws IOException {

        output.write(text.getBytes(StandardCharsets.UTF_8));
        output.flush();
    }
}