package com.toopher;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;


/**
 * Created by graceyim on 1/26/15.
 */
public class TestPairing {
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1";

    private String id;
    private JSONObject user;
    private String userId;
    private String userName;
    private JSONObject jsonResponse;

    @Before
    public void setUp() {
        this.id = UUID.randomUUID().toString();
        this.user = new JSONObject();
        this.user.put("id", UUID.randomUUID().toString());
        this.user.put("name", "userName");
        this.userId = user.getString("id");
        this.userName = user.getString("name");
        this.jsonResponse = new JSONObject();
        this.jsonResponse.put("id", id);
        this.jsonResponse.put("enabled", true);
        this.jsonResponse.put("pending", false);
        this.jsonResponse.put("user", user);
    }

    @Test
    public void testRefreshFromServer() throws InterruptedException, RequestError {
        Pairing pairing = new Pairing(jsonResponse);

        JSONObject newJsonResponse = jsonResponse;
        JSONObject newUser = newJsonResponse.getJSONObject("user");
        newUser.remove("name");
        newUser.put("name", "userNameChanged");
        newJsonResponse.remove("enabled");
        newJsonResponse.put("enabled", false);

        HttpClientMock httpClient = new HttpClientMock(200, newJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        assertEquals(pairing.id, id);
        assertEquals(pairing.user.name, userName);
        assertTrue(pairing.enabled);
        assertFalse(pairing.pending);

        pairing.refreshFromServer(toopherAPI);

        assertEquals(pairing.id, id);
        assertEquals(pairing.user.name, "userNameChanged");
        assertFalse(pairing.enabled);
        assertFalse(pairing.pending);
    }

    @Test
    public void testGetResetLink() throws InterruptedException, RequestError {
        Pairing pairing = new Pairing(jsonResponse);

        JSONObject urlJsonResponse = new JSONObject();
        urlJsonResponse.put("url", "http://api.toopher.test/v1/pairings/{0}/reset?reset_authorization=abcde".format(id));

        HttpClientMock httpClient = new HttpClientMock(200, urlJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        String resetLink = pairing.getResetLink(toopherAPI);

        assertEquals(resetLink, "http://api.toopher.test/v1/pairings/{0}/reset?reset_authorization=abcde".format(id));
    }

    @Test
    public void testGetResetLinkWithExtras() throws InterruptedException, RequestError {
        Pairing pairing = new Pairing(jsonResponse);

        JSONObject urlJsonResponse = new JSONObject();
        urlJsonResponse.put("url", "http://api.toopher.test/v1/pairings/{0}/reset?reset_authorization=abcde".format(id));

        HttpClientMock httpClient = new HttpClientMock(200, urlJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("security_question", "is this a test?");
        extras.put("security_answer", "yes!");
        String resetLink = pairing.getResetLink(toopherAPI, extras);

        assertEquals(resetLink, "http://api.toopher.test/v1/pairings/{0}/reset?reset_authorization=abcde".format(id));
    }
}
