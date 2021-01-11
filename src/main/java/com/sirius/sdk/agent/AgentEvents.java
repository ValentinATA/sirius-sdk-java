package com.sirius.sdk.agent;

import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.errors.sirius_exceptions.SiriusConnectionClosed;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * RPC service.
 * <p>
 * Reactive nature of Smart-Contract design
 */
public class AgentEvents extends BaseAgentConnection {

    String tunnel;

    public String getBalancingGroup() {
        return balancingGroup;
    }

    String balancingGroup;

    public AgentEvents(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout) {
        super(serverAddress, credentials, p2p, timeout);
    }

    @Override
    public String path() {
        return "events";
    }

    @Override
    public void setup(Message context) {
        super.setup(context);
        // Extract load balancing info
        JSONArray balancing = context.getJSONArrayFromJSON("~balancing", new JSONArray());
        for (int i = 0; i < balancing.length(); i++) {
            JSONObject balance = balancing.getJSONObject(i);
            if ("kafka".equals(balance.getString("id"))) {
                System.out.println("balance="+balance.toString());
                JSONObject jsonObject = balance.getJSONObject("data").getJSONObject("json");
                if(!jsonObject.isNull("group_id")){
                    balancingGroup = jsonObject.getString("group_id");
                }

            }
        }


    }

    public CompletableFuture<Message> pull() throws SiriusConnectionClosed, SiriusInvalidPayloadStructure {
        if (!connector.isOpen()) {
            throw new SiriusConnectionClosed("Open agent connection at first");
        }
        return connector.read().thenApply(data -> {
            try {
                System.out.println("!!!!!!!!!  " + new String(data, StandardCharsets.US_ASCII));
                JSONObject payload = new JSONObject(new String(data, StandardCharsets.US_ASCII));
                if (payload.has("protected")) {
                    String message = p2p.unpack(payload.toString());
                    return new Message(message);
                } else {
                    return new Message(payload.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
                //throw new SiriusInvalidPayloadStructure(e.getMessage());
            }
        });
    }
}
