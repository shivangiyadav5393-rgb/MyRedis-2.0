import java.io.*;
import java.util.*;

public class RedisCommandHandler {

    private final RedisDatabase database;

    public RedisCommandHandler(RedisDatabase database) {
        this.database = database;
    }

    public boolean handle(
            List<String> parts,
            OutputStream output) throws IOException {

        if (parts == null || parts.isEmpty()) {
            RespProtocol.writeError(output, "Empty command");
            return true;
        }

        String command =
                parts.get(0).toUpperCase(Locale.ROOT);

        try {

            switch (command) {

                case "PING":

                    if (parts.size() > 2) {
                        RespProtocol.writeError(output,
                                "Wrong number of arguments");
                    } else if (parts.size() == 2) {
                        RespProtocol.writeBulk(output, parts.get(1));
                    } else {
                        RespProtocol.writeSimple(output, "PONG");
                    }
                    break;

                case "SET":

                    if (parts.size() != 3) {
                        RespProtocol.writeError(output,
                                "Usage: SET key value");
                        break;
                    }

                    database.set(parts.get(1), parts.get(2));
                    PersistenceManager.save(database);
                    RespProtocol.writeSimple(output, "OK");
                    break;

                case "GET":

                    if (parts.size() != 2) {
                        RespProtocol.writeError(output,
                                "Usage: GET key");
                        break;
                    }

                    RespProtocol.writeBulk(
                            output, database.get(parts.get(1)));
                    break;

                case "DEL":

                    if (parts.size() < 2) {
                        RespProtocol.writeError(output,
                                "Usage: DEL key [key ...]");
                        break;
                    }

                    long deleted = database.delete(
                            parts.subList(1, parts.size())
                                    .toArray(new String[0]));

                    if (deleted > 0) {
                        PersistenceManager.save(database);
                    }

                    RespProtocol.writeInteger(output, deleted);
                    break;

                case "EXISTS":

                    if (parts.size() < 2) {
                        RespProtocol.writeError(output,
                                "Usage: EXISTS key [key ...]");
                        break;
                    }

                    long found = database.exists(
                            parts.subList(1, parts.size())
                                    .toArray(new String[0]));

                    RespProtocol.writeInteger(output, found);
                    break;

                case "EXPIRE":

                    if (parts.size() != 3) {
                        RespProtocol.writeError(output,
                                "Usage: EXPIRE key seconds");
                        break;
                    }

                    long seconds = Long.parseLong(parts.get(2));

                    long result = database.expire(
                            parts.get(1), seconds);

                    if (result == 1) {
                        PersistenceManager.save(database);
                    }

                    RespProtocol.writeInteger(output, result);
                    break;

                case "TTL":

                    if (parts.size() != 2) {
                        RespProtocol.writeError(output,
                                "Usage: TTL key");
                        break;
                    }

                    RespProtocol.writeInteger(
                            output, database.ttl(parts.get(1)));
                    break;

                case "KEYS":

                    if (parts.size() != 2) {
                        RespProtocol.writeError(output,
                                "Usage: KEYS pattern");
                        break;
                    }

                    RespProtocol.writeArray(
                            output, database.keys(parts.get(1)));
                    break;

                case "DBSIZE":

                    if (parts.size() != 1) {
                        RespProtocol.writeError(output,
                                "Usage: DBSIZE");
                        break;
                    }

                    RespProtocol.writeInteger(
                            output, database.size());
                    break;

                case "FLUSHDB":

                    if (parts.size() != 1) {
                        RespProtocol.writeError(output,
                                "Usage: FLUSHDB");
                        break;
                    }

                    database.flush();
                    PersistenceManager.save(database);
                    RespProtocol.writeSimple(output, "OK");
                    break;

                case "SAVE":

                    if (parts.size() != 1) {
                        RespProtocol.writeError(output,
                                "Usage: SAVE");
                        break;
                    }

                    PersistenceManager.save(database);
                    RespProtocol.writeSimple(output, "OK");
                    break;

                case "QUIT":

                    RespProtocol.writeSimple(output, "BYE");
                    return false;

                default:

                    RespProtocol.writeError(
                            output, "Unknown command: " + command);
            }

        } catch (NumberFormatException e) {

            RespProtocol.writeError(output,
                    "Invalid number format");

        } catch (Exception e) {

            RespProtocol.writeError(output,
                    "Command failed: " + e.getMessage());
        }

        return true;
    }
}