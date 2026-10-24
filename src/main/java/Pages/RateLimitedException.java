package Pages;

/**
 * Thrown when the portal refuses a request because of its per-IP rate limits
 * (by default 20 submissions per hour and 30 status checks per 10 minutes).
 * The test listener reports such tests as SKIPPED rather than FAILED.
 */
public class RateLimitedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RateLimitedException(String serverMessage) {
        super("Server rate limit reached - " + serverMessage);
    }

    public static boolean isRateLimitMessage(String text) {
        return text != null && text.startsWith("Too many");
    }
}
