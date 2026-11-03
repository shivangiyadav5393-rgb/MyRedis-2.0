public class RedisEntry {

    private final String value;

    private volatile long expiryTime;

    public RedisEntry(String value) {

        this.value = value;
        this.expiryTime = -1;
    }

    public RedisEntry(String value, long expiryTime) {

        this.value = value;
        this.expiryTime = expiryTime;
    }

    public String getValue() {
        return value;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public boolean isExpired() {

        return expiryTime != -1 &&
                System.currentTimeMillis() >= expiryTime;
    }
}