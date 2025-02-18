import com.sirius.sdk.agent.CloudAgent;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Ping;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Pong;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.model.Entity;
import com.sirius.sdk.agent.coprotocols.AbstractCloudCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.PairwiseCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.TheirEndpointCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.ThreadBasedCoProtocolTransport;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.AbstractP2PCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolThreadedP2P;
import com.sirius.sdk.hub.coprotocols.CoProtocolThreadedTheirs;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestCopropocols {

    static final String[] TEST_MSG_TYPES = {
            "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/test_protocol/1.0/request-1",
            "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/test_protocol/1.0/response-1",
            "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/test_protocol/1.0/request-2",
            "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/test_protocol/1.0/response-2"
    };

    ConfTest confTest;
    ServerTestSuite testSuite;
    List<Message> msgLog;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
        testSuite = confTest.getSuiteSingleton();
        msgLog = new ArrayList<>();
    }

    void routine1(AbstractCloudCoProtocolTransport protocol) {
        try {
            Message firstReq = new Message(new JSONObject().
                    put("@type", TEST_MSG_TYPES[0]).
                    put("content", "Request1"));
            msgLog.add(firstReq);
            Pair<Boolean, Message> okResp1 = protocol.sendAndWait(firstReq);
            Assert.assertTrue(okResp1.first);
            msgLog.add(okResp1.second);
            Message secondReq = new Message(new JSONObject().
                    put("@type", TEST_MSG_TYPES[2]).
                    put("content", "Request2"));
            Pair<Boolean, Message> okResp2 = protocol.sendAndWait(secondReq);
            Assert.assertTrue(okResp2.first);
            msgLog.add(okResp2.second);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    void routine1OnHub(AbstractP2PCoProtocol protocol) {
        try {
            Message firstReq = new Message(new JSONObject().
                    put("@type", TEST_MSG_TYPES[0]).
                    put("content", "Request1"));
            msgLog.add(firstReq);
            Pair<Boolean, Message> okResp1 = protocol.sendAndWait(firstReq);
            Assert.assertTrue(okResp1.first);
            msgLog.add(okResp1.second);
            Message secondReq = new Message(new JSONObject().
                    put("@type", TEST_MSG_TYPES[2]).
                    put("content", "Request2"));
            Pair<Boolean, Message> okResp2 = protocol.sendAndWait(secondReq);
            Assert.assertTrue(okResp2.first);
            msgLog.add(okResp2.second);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    void routine2(AbstractCloudCoProtocolTransport protocol) {
        try {
            Thread.sleep(1000);
            Message firstResp = new Message(new JSONObject().
                    put("@type", TEST_MSG_TYPES[1]).
                    put("content", "Response1"));
            Pair<Boolean, Message> okResp1 = protocol.sendAndWait(firstResp);
            Assert.assertTrue(okResp1.first);
            msgLog.add(okResp1.second);
            Message endMsg = new Message(new JSONObject().
                    put("@type", TEST_MSG_TYPES[3]).
                    put("content", "End"));
            protocol.send(endMsg);

        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage(), false);
        }
    }

    void routine2OnHub(AbstractP2PCoProtocol protocol) {
        try {
            Thread.sleep(1000);
            Message firstResp = new Message(new JSONObject().
                    put("@type", TEST_MSG_TYPES[1]).
                    put("content", "Response1"));
            Pair<Boolean, Message> okResp1 = protocol.sendAndWait(firstResp);
            Assert.assertTrue(okResp1.first);
            msgLog.add(okResp1.second);
            Message endMsg = new Message(new JSONObject().
                    put("@type", TEST_MSG_TYPES[3]).
                    put("content", "End"));
            protocol.send(endMsg);

        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage(), false);
        }
    }

    void checkMsgLog() {
        Assert.assertEquals(msgLog.size(), TEST_MSG_TYPES.length);
        for(int i = 0; i < TEST_MSG_TYPES.length; i++) {
            Assert.assertEquals(TEST_MSG_TYPES[i], msgLog.get(i).getType());
        }

        Assert.assertEquals("Request1", msgLog.get(0).getStringFromJSON("content"));
        Assert.assertEquals("Response1", msgLog.get(1).getStringFromJSON("content"));
        Assert.assertEquals("Request2", msgLog.get(2).getStringFromJSON("content"));
        Assert.assertEquals("End", msgLog.get(3).getStringFromJSON("content"));
    }

    @Test
    public void testTheirEndpointProtocol() {
        AgentParams agent1params = testSuite.getAgentParams("agent1");
        AgentParams agent2params = testSuite.getAgentParams("agent2");

        Entity entity1 = agent1params.getEntitiesList().get(0);
        Entity entity2 = agent2params.getEntitiesList().get(0);

        CloudAgent agent1 = confTest.getAgent("agent1");
        CloudAgent agent2 = confTest.getAgent("agent2");

        agent1.open();
        agent2.open();

        String agent1Endpoint = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(agent1);
        String agent2Endpoint = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(agent2);

        TheirEndpoint their1 = new TheirEndpoint(agent2Endpoint, entity2.getVerkey());
        TheirEndpointCoProtocolTransport agent1Protocol = (TheirEndpointCoProtocolTransport) agent1.spawn(entity1.getVerkey(), their1);
        TheirEndpoint their2 = new TheirEndpoint(agent1Endpoint, entity1.getVerkey());
        TheirEndpointCoProtocolTransport agent2Protocol = (TheirEndpointCoProtocolTransport) agent2.spawn(entity2.getVerkey(), their2);

        agent1Protocol.start(Collections.singletonList("test_protocol"));
        agent2Protocol.start(Collections.singletonList("test_protocol"));

        msgLog.clear();
        CompletableFuture<Void> cf1 = CompletableFuture.runAsync(() -> routine1(agent1Protocol));
        CompletableFuture<Void> cf2 = CompletableFuture.runAsync(() -> routine2(agent2Protocol));

        cf1.join();
        cf2.join();
        checkMsgLog();

        agent1Protocol.stop();
        agent2Protocol.stop();

        agent1.close();
        agent2.close();
    }

    @Test
    public void testPairwiseProtocol() {
        CloudAgent agent1 = confTest.getAgent("agent1");
        CloudAgent agent2 = confTest.getAgent("agent2");

        agent1.open();
        agent2.open();

        String agent1Endpoint = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(agent1);
        String agent2Endpoint = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(agent2);

        Pair<String, String> didVerkey1 = agent1.getWallet().getDid().createAndStoreMyDid();
        Pair<String, String> didVerkey2 = agent2.getWallet().getDid().createAndStoreMyDid();
        agent1.getWallet().getDid().storeTheirDid(didVerkey2.first, didVerkey2.second);
        agent1.getWallet().getPairwise().createPairwise(didVerkey2.first, didVerkey1.first);
        agent2.getWallet().getDid().storeTheirDid(didVerkey1.first, didVerkey1.second);
        agent2.getWallet().getPairwise().createPairwise(didVerkey1.first, didVerkey2.first);

        Pairwise pairwise1 = new Pairwise(
                new Pairwise.Me(didVerkey1.first, didVerkey1.second),
                new Pairwise.Their(didVerkey2.first, "Label-2", agent2Endpoint, didVerkey2.second));
        Pairwise pairwise2 = new Pairwise(
                new Pairwise.Me(didVerkey2.first, didVerkey2.second),
                new Pairwise.Their(didVerkey1.first, "Label-1", agent1Endpoint, didVerkey1.second));

        PairwiseCoProtocolTransport agent1Protocol = agent1.spawn(pairwise1);
        PairwiseCoProtocolTransport agent2Protocol = agent2.spawn(pairwise2);

        agent1Protocol.start(Collections.singletonList("test_protocol"));
        agent2Protocol.start(Collections.singletonList("test_protocol"));

        msgLog.clear();
        CompletableFuture<Void> cf1 = CompletableFuture.runAsync(() -> routine1(agent1Protocol));
        CompletableFuture<Void> cf2 = CompletableFuture.runAsync(() -> routine2(agent2Protocol));

        cf1.join();
        cf2.join();
        checkMsgLog();

        agent1Protocol.stop();
        agent2Protocol.stop();

        agent1.close();
        agent2.close();
    }

    @Test
    public void testThreadBasedProtocol() {
        CloudAgent agent1 = confTest.getAgent("agent1");
        CloudAgent agent2 = confTest.getAgent("agent2");

        agent1.open();
        agent2.open();

        String agent1Endpoint = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(agent1);
        String agent2Endpoint = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(agent2);

        Pair<String, String> didVerkey1 = agent1.getWallet().getDid().createAndStoreMyDid();
        Pair<String, String> didVerkey2 = agent2.getWallet().getDid().createAndStoreMyDid();
        agent1.getWallet().getDid().storeTheirDid(didVerkey2.first, didVerkey2.second);
        agent1.getWallet().getPairwise().createPairwise(didVerkey2.first, didVerkey1.first);
        agent2.getWallet().getDid().storeTheirDid(didVerkey1.first, didVerkey1.second);
        agent2.getWallet().getPairwise().createPairwise(didVerkey1.first, didVerkey2.first);

        Pairwise pairwise1 = new Pairwise(
                new Pairwise.Me(didVerkey1.first, didVerkey1.second),
                new Pairwise.Their(didVerkey2.first, "Label-2", agent2Endpoint, didVerkey2.second));
        Pairwise pairwise2 = new Pairwise(
                new Pairwise.Me(didVerkey2.first, didVerkey2.second),
                new Pairwise.Their(didVerkey1.first, "Label-1", agent1Endpoint, didVerkey1.second));

        String threadUi = UUID.randomUUID().toString();
        ThreadBasedCoProtocolTransport agent1Protocol = agent1.spawn(threadUi, pairwise1);
        ThreadBasedCoProtocolTransport agent2Protocol = agent2.spawn(threadUi, pairwise2);

        agent1Protocol.start(Collections.singletonList("test_protocol"));
        agent2Protocol.start(Collections.singletonList("test_protocol"));

        msgLog.clear();
        CompletableFuture<Void> cf1 = CompletableFuture.runAsync(() -> routine1(agent1Protocol));
        CompletableFuture<Void> cf2 = CompletableFuture.runAsync(() -> routine2(agent2Protocol));

        cf1.join();
        cf2.join();
        checkMsgLog();

        agent1Protocol.stop();
        agent2Protocol.stop();

        agent1.close();
        agent2.close();
    }

    @Test
    public void testThreadbasedProtocolOnHub() {
        CloudAgent agent1 = confTest.getAgent("agent1");
        CloudAgent agent2 = confTest.getAgent("agent2");

        AgentParams agent1params = testSuite.getAgentParams("agent1");
        AgentParams agent2params = testSuite.getAgentParams("agent2");

        agent1.open();
        agent2.open();

        Pairwise pairwise1 = null;
        Pairwise pairwise2 = null;

        try {
            // Get endpoints
            String agent1Endpoint = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(agent1);
            String agent2Endpoint = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(agent2);

            // Init pairwise list #1
            Pair<String, String> didVerkey1 = agent1.getWallet().getDid().createAndStoreMyDid();
            Pair<String, String> didVerkey2 = agent2.getWallet().getDid().createAndStoreMyDid();
            agent1.getWallet().getDid().storeTheirDid(didVerkey2.first, didVerkey2.second);
            agent1.getWallet().getPairwise().createPairwise(didVerkey2.first, didVerkey1.first);
            agent2.getWallet().getDid().storeTheirDid(didVerkey1.first, didVerkey1.second);
            agent2.getWallet().getPairwise().createPairwise(didVerkey1.first, didVerkey2.first);

            // Init pairwise list #2
            pairwise1 = new Pairwise(
                    new Pairwise.Me(didVerkey1.first, didVerkey1.second),
                    new Pairwise.Their(didVerkey2.first, "Label-2", agent2Endpoint, didVerkey2.second));
            pairwise2 = new Pairwise(
                    new Pairwise.Me(didVerkey2.first, didVerkey2.second),
                    new Pairwise.Their(didVerkey1.first, "Label-1", agent1Endpoint, didVerkey1.second));
        } finally {
            agent1.close();
            agent2.close();
        }

        String threadUi = UUID.randomUUID().toString();
        Pairwise finalPairwise = pairwise1;
        CompletableFuture<Void> cf1 = CompletableFuture.runAsync(() -> {
            try (Context context = CloudContext.builder().
                    setServerUri(agent1params.getServerAddress()).
                    setP2p(agent1params.getConnection()).
                    setCredentials(agent1params.getCredentials().getBytes(StandardCharsets.UTF_8)).
                    build()) {
                CoProtocolThreadedP2P co1 = new CoProtocolThreadedP2P(context, threadUi, finalPairwise);
                routine1OnHub(co1);
            }
        });

        Pairwise finalPairwise2 = pairwise2;
        CompletableFuture<Void> cf2 = CompletableFuture.runAsync(() -> {
            try (Context context = CloudContext.builder().
                    setServerUri(agent2params.getServerAddress()).
                    setP2p(agent2params.getConnection()).
                    setCredentials(agent2params.getCredentials().getBytes(StandardCharsets.UTF_8)).
                    build()) {
                CoProtocolThreadedP2P co2 = new CoProtocolThreadedP2P(context, threadUi, finalPairwise2);
                routine2OnHub(co2);
            }
        });

        msgLog.clear();

        cf1.join();
        cf2.join();
        checkMsgLog();
    }

    @Test
    public void testCoprotocolThreadedTheirsSend() throws InterruptedException, ExecutionException, TimeoutException {
        CloudAgent agent1 = confTest.getAgent("agent1");
        CloudAgent agent2 = confTest.getAgent("agent2");
        CloudAgent agent3 = confTest.getAgent("agent3");

        AgentParams agent1params = testSuite.getAgentParams("agent1");
        AgentParams agent2params = testSuite.getAgentParams("agent2");
        AgentParams agent3params = testSuite.getAgentParams("agent3");

        agent1.open();
        agent2.open();
        agent3.open();

        Pairwise pw1 = confTest.getPairwise(agent1, agent2);
        Pairwise pw2 = confTest.getPairwise(agent1, agent3);

        String threadId = "thread-id-" + UUID.randomUUID();
        List<Message> rcvMessages = Collections.synchronizedList(new ArrayList<>());

        CompletableFuture<Void> sender = CompletableFuture.supplyAsync(() -> {
            try (Context context = CloudContext.builder().
                    setServerUri(agent1params.getServerAddress()).
                    setCredentials(agent1params.getCredentials().getBytes(StandardCharsets.UTF_8))
                    .setP2p(agent1params.getConnection()).build()) {
                Ping msg = Ping.builder().
                        setComment("Test Ping").
                        build();
                CoProtocolThreadedTheirs co = new CoProtocolThreadedTheirs(context, threadId, Arrays.asList(pw1, pw2), null, 60);
                co.send(msg);

            }
            return null;
        }, r -> new Thread(r).start());

        CompletableFuture<Void> reader1 = CompletableFuture.supplyAsync(() -> {
            try (Context context = CloudContext.builder().
                    setServerUri(agent2params.getServerAddress()).
                    setCredentials(agent2params.getCredentials().getBytes(StandardCharsets.UTF_8))
                    .setP2p(agent2params.getConnection()).build()) {
                rcvMessages.add(context.subscribe().getOne().get(30, TimeUnit.SECONDS));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }, r -> new Thread(r).start());

        CompletableFuture<Void> reader2 = CompletableFuture.supplyAsync(() -> {
            try (Context context = CloudContext.builder().
                    setServerUri(agent3params.getServerAddress()).
                    setCredentials(agent3params.getCredentials().getBytes(StandardCharsets.UTF_8))
                    .setP2p(agent3params.getConnection()).build()) {
                rcvMessages.add(context.subscribe().getOne().get(30, TimeUnit.SECONDS));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }, r -> new Thread(r).start());

        sender.get(30, TimeUnit.SECONDS);
        reader1.get(30, TimeUnit.SECONDS);
        reader2.get(30, TimeUnit.SECONDS);

        Assert.assertEquals(2, rcvMessages.size());
    }

    @Test
    public void testCoprotocolThreadedTheirsSwitch() throws InterruptedException, ExecutionException, TimeoutException {
        CloudAgent agent1 = confTest.getAgent("agent1");
        CloudAgent agent2 = confTest.getAgent("agent2");
        CloudAgent agent3 = confTest.getAgent("agent3");

        AgentParams agent1params = testSuite.getAgentParams("agent1");
        AgentParams agent2params = testSuite.getAgentParams("agent2");
        AgentParams agent3params = testSuite.getAgentParams("agent3");

        agent1.open();
        agent2.open();
        agent3.open();

        Pairwise pw1 = confTest.getPairwise(agent1, agent2);
        Pairwise pw2 = confTest.getPairwise(agent1, agent3);

        String threadId = "thread-id-" + UUID.randomUUID();
        List<CoProtocolThreadedTheirs.SendAndWaitResult> statuses = new ArrayList<>();

        CompletableFuture<Void> actor = CompletableFuture.supplyAsync(() -> {
            try (Context context = CloudContext.builder().
                    setServerUri(agent1params.getServerAddress()).
                    setCredentials(agent1params.getCredentials().getBytes(StandardCharsets.UTF_8))
                    .setP2p(agent1params.getConnection()).build()) {
                Ping msg = Ping.builder().
                        setComment("Test Ping").
                        build();
                CoProtocolThreadedTheirs co = new CoProtocolThreadedTheirs(context, threadId, Arrays.asList(pw1, pw2), null, 60);
                statuses.addAll(co.sendAndWait(msg));

            }
            return null;
        }, r -> new Thread(r).start());

        CompletableFuture<Void> responder1 = CompletableFuture.supplyAsync(() -> {
            try (Context context = CloudContext.builder().
                    setServerUri(agent2params.getServerAddress()).
                    setCredentials(agent2params.getCredentials().getBytes(StandardCharsets.UTF_8))
                    .setP2p(agent2params.getConnection()).build()) {
                Event event = context.subscribe().getOne().get(30, TimeUnit.SECONDS);
                String threadId_ = event.message().getJSONOBJECTFromJSON("~thread").optString("thid");
                Pong pong = Pong.builder().
                        setPingId(threadId_).
                        setComment("PONG").
                        build();
                context.sendTo(pong, event.getPairwise());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }, r -> new Thread(r).start());

        CompletableFuture<Void> responder2 = CompletableFuture.supplyAsync(() -> {
            try (Context context = CloudContext.builder().
                    setServerUri(agent3params.getServerAddress()).
                    setCredentials(agent3params.getCredentials().getBytes(StandardCharsets.UTF_8))
                    .setP2p(agent3params.getConnection()).build()) {
                Event event = context.subscribe().getOne().get(30, TimeUnit.SECONDS);
                String threadId_ = event.message().getJSONOBJECTFromJSON("~thread").optString("thid");
                Pong pong = Pong.builder().
                        setPingId(threadId_).
                        setComment("PONG").
                        build();
                context.sendTo(pong, event.getPairwise());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }, r -> new Thread(r).start());

        actor.get(30, TimeUnit.SECONDS);
        responder1.get(30, TimeUnit.SECONDS);
        responder2.get(30, TimeUnit.SECONDS);

        Assert.assertFalse(statuses.isEmpty());
        for (CoProtocolThreadedTheirs.SendAndWaitResult s : statuses) {
            Assert.assertTrue(s.success);
            Assert.assertEquals("PONG", s.message.getMessageObj().optString("comment"));
        }
    }

}
