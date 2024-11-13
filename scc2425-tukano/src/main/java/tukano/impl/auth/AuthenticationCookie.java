package main.java.tukano.impl.auth;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import javax.ws.rs.core.Context;
import java.security.SecureRandom;
import java.util.Base64;


public class AuthenticationCookie {

    public static final String COOKIE_NAME = "blob_auth_token";
    public static final int MAX_AGE = 3600;     //in seconds
    public static NewCookie authCookie;
    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    public void createAuthTCookie(String token, @Context HttpServletResponse response) {
        // Create a new cookie with name "authToken" and the provided token as its value
        Cookie authCookie = new Cookie("authToken", token);
        authCookie.setPath("/");
        authCookie.setMaxAge(60 * 60 * 24); // 1 day (in seconds)
        authCookie.setHttpOnly(true);       // Makes the cookie inaccessible to JavaScript
        authCookie.setSecure(true);         // Send only over HTTPS (if applicable)

        // Add the cookie to the HTTP response
        response.addCookie(authCookie);
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