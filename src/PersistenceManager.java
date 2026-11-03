import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PersistenceManager {

    private static final String FILE_PATH =
            "data/myredis.dat";

    public static synchronized void save(
            RedisDatabase database) {

        File file = new File(FILE_PATH);
        File parent = file.getParentFile();

        if (parent != null) {
            parent.mkdirs();
        }

        File tempFile = new File(FILE_PATH + ".tmp");

        try (
            DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(
                    new FileOutputStream(tempFile)))
        ) {

            Map<String, RedisEntry> data =
                    database.snapshot();

            output.writeInt(data.size());

            for (Map.Entry<String, RedisEntry> item
                    : data.entrySet()) {

                output.writeUTF(item.getKey());
                output.writeUTF(item.getValue().getValue());
                output.writeLong(
                        item.getValue().getExpiryTime());
            }

        } catch (IOException e) {
            System.out.println("Database save failed: "
                    + e.getMessage());
            return;
        }

        try {
            Files.move(
                tempFile.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            );

        } catch (IOException e) {
            try {
                Files.move(
                    tempFile.toPath(),
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException secondError) {
                System.out.println("File replacement failed.");
            }
        }
    }

    public static synchronized void load(
            RedisDatabase database) {

        File file = new File(FILE_PATH);

        if (!file.exists()) {
            System.out.println("No previous database found.");
            return;
        }

        Map<String, RedisEntry> savedData = new HashMap<>();

        try (
            DataInputStream input = new DataInputStream(
                new BufferedInputStream(
                    new FileInputStream(file)))
        ) {

            int count = input.readInt();

            if (count < 0 || count > 1000000) {
                throw new IOException("Invalid database file.");
            }

            for (int i = 0; i < count; i++) {

                String key = input.readUTF();
                String value = input.readUTF();
                long expiryTime = input.readLong();

                RedisEntry entry =
                        new RedisEntry(value, expiryTime);

                if (!entry.isExpired()) {
                    savedData.put(key, entry);
                }
            }

            database.restore(savedData);

            System.out.println("Restored "
                    + savedData.size() + " keys.");

        } catch (IOException e) {
            System.out.println("Database load failed: "
                    + e.getMessage());
        }
    }
}