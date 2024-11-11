package main.java.tukano.impl.auth;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.security.SecureRandom;
import java.util.Base64;


public class AuthenticationCookie {

    public static final String COOKIE_NAME = "blob_auth_token";
    public static final int MAX_AGE = 3600;     //in seconds
    public static NewCookie authCookie;
    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    public static void createAuthCookie() {
        String authToken = generateAuthToken();
        authCookie = new NewCookie(COOKIE_NAME, authToken, "/", null, NewCookie.DEFAULT_VERSION, "auth token", MAX_AGE, false);
    }

    public static String getToken() {
        return authCookie != null ? authCookie.getValue() : null;
    }

    public static String generateAuthToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

}