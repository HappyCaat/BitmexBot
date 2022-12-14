package org.ipenkin.framework;

import org.ipenkin.authentication.HMAC;
import org.ipenkin.authentication.Signature;
import org.ipenkin.framework.constants.URL.UtilURL;
import org.ipenkin.framework.constants.Verb;
import org.ipenkin.model.Model;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

public class NewWebSocket extends WebSocketClient {

    public NewWebSocket(URI serverURI) {
        super(serverURI);
    }

    String expires = String.valueOf(Instant.now().getEpochSecond() + 10);
    String signature = Signature.signatureToString(HMAC.calcHmacSha256(Model.getApiSecret().getBytes(StandardCharsets.UTF_8),
            (Verb.GET + UtilURL.REALTIME + expires).getBytes(StandardCharsets.UTF_8)));


    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send("{\"op\": \"authKeyExpires\", \"args\": [\"" + Model.getApiKey() + "\", " + expires + ", \"" + signature + "\"]}");
        send("{\"op\": \"subscribe\", \"args\": [\"order\"]}");
        System.out.println("opened connection");
        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void onMessage(String message) {
        System.out.println("received: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // The close codes are documented in class org.java_websocket.framing.CloseFrame
        System.out.println(
                "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: "
                        + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }
}