import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class RedisDatabase {

    private final ConcurrentHashMap<String, RedisEntry> database =
            new ConcurrentHashMap<>();

    public synchronized void set(String key, String value) {
        database.put(key, new RedisEntry(value));
    }

    public synchronized String get(String key) {

        removeIfExpired(key);

        RedisEntry entry = database.get(key);

        return entry == null ? null : entry.getValue();
    }

    public synchronized long delete(String... keys) {

        long count = 0;

        for (String key : keys) {

            removeIfExpired(key);

            if (database.remove(key) != null) {
                count++;
            }
        }

        return count;
    }

    public synchronized long exists(String... keys) {

        long count = 0;

        for (String key : keys) {

            removeIfExpired(key);

            if (database.containsKey(key)) {
                count++;
            }
        }

        return count;
    }

    public synchronized long expire(
            String key, long seconds) {

        removeIfExpired(key);

        RedisEntry entry = database.get(key);

        if (entry == null) {
            return 0;
        }

        if (seconds <= 0) {
            database.remove(key);
            return 1;
        }

        long expiryTime = System.currentTimeMillis()
                + seconds * 1000L;

        entry.setExpiryTime(expiryTime);

        return 1;
    }

    public synchronized long ttl(String key) {

        removeIfExpired(key);

        RedisEntry entry = database.get(key);

        if (entry == null) {
            return -2;
        }

        if (entry.getExpiryTime() == -1) {
            return -1;
        }

        long remaining = entry.getExpiryTime()
                - System.currentTimeMillis();

        return (remaining + 999) / 1000;
    }

    public synchronized List<String> keys(String pattern) {

        cleanupExpired();

        List<String> result = new ArrayList<>();

        StringBuilder regex = new StringBuilder("^");

        for (int i = 0; i < pattern.length(); i++) {

            char c = pattern.charAt(i);

            if (c == '*') {
                regex.append(".*");
            } else if (c == '?') {
                regex.append(".");
            } else {
                regex.append(Pattern.quote(
                        String.valueOf(c)));
            }
        }

        regex.append("$");

        Pattern compiled = Pattern.compile(regex.toString());

        for (String key : database.keySet()) {

            if (compiled.matcher(key).matches()) {
                result.add(key);
            }
        }

        Collections.sort(result);

        return result;
    }

    public synchronized long size() {
        cleanupExpired();
        return database.size();
    }

    public synchronized void flush() {
        database.clear();
    }

    private void removeIfExpired(String key) {

        RedisEntry entry = database.get(key);

        if (entry != null && entry.isExpired()) {
            database.remove(key, entry);
        }
    }

    public synchronized boolean cleanupExpired() {

        boolean changed = false;

        for (String key : database.keySet()) {

            RedisEntry entry = database.get(key);

            if (entry != null && entry.isExpired()) {

                if (database.remove(key, entry)) {
                    changed = true;
                }
            }
        }

        return changed;
    }

    public synchronized Map<String, RedisEntry> snapshot() {

        cleanupExpired();

        return new HashMap<>(database);
    }

    public synchronized void restore(
            Map<String, RedisEntry> savedData) {

        database.clear();

        for (Map.Entry<String, RedisEntry> item
                : savedData.entrySet()) {

            if (!item.getValue().isExpired()) {
                database.put(item.getKey(), item.getValue());
            }
        }
    }
}