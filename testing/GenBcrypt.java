import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenBcrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("test123");
        System.out.println("BCRYPT_HASH=" + hash);
        // Verify
        System.out.println("VERIFY=" + encoder.matches("test123", hash));
    }
}
