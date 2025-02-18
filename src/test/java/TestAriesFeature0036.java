import com.sirius.sdk.agent.CloudAgent;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.AttribTranslation;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.OfferCredentialMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.ProposedAttrib;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines.Holder;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines.Issuer;
import com.sirius.sdk.agent.ledger.CredentialDefinition;
import com.sirius.sdk.agent.ledger.Schema;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.errors.indy_exceptions.DuplicateMasterSecretNameException;
import com.sirius.sdk.errors.indy_exceptions.WalletItemNotFoundException;
import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.Context;
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

public class TestAriesFeature0036 {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testSane() throws InterruptedException, ExecutionException, TimeoutException {

        CloudAgent issuer = confTest.getAgent("agent1");
        CloudAgent holder = confTest.getAgent("agent2");
        issuer.open();
        holder.open();
        Pairwise i2h = confTest.getPairwise(issuer, holder);
        Pairwise h2i = confTest.getPairwise(holder, issuer);

        String issuerDid = i2h.getMe().getDid();
        String issuerVerkey = i2h.getMe().getVerkey();
        String schemaName = "schema_" + UUID.randomUUID().toString();
        Pair<String, AnonCredSchema> schemaPair = issuer.getWallet().getAnoncreds().issuerCreateSchema(issuerDid,
                schemaName, "1.0", "attr1", "attr2", "attr3", "attr4");
        String schemaId = schemaPair.first;
        AnonCredSchema anoncredSchema = schemaPair.second;
        Ledger ledger = issuer.getLedgers().get("default");
        Pair<Boolean, Schema> okSchema = ledger.registerSchema(anoncredSchema, issuerDid);
        Assert.assertTrue(okSchema.first);
        Schema schema = okSchema.second;

        Pair<Boolean, CredentialDefinition> okCredDef = ledger.registerCredDef(new CredentialDefinition("TAG", schema), issuerDid);
        Assert.assertTrue(okCredDef.first);
        CredentialDefinition credDef = okCredDef.second;

        try {
            holder.getWallet().getAnoncreds().proverCreateMasterSecret(ConfTest.proverMasterSecretName);
        } catch (DuplicateMasterSecretNameException ignored) {}

        issuer.close();
        holder.close();

        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams issuerParams = testSuite.getAgentParams("agent1");
        AgentParams holderParams = testSuite.getAgentParams("agent2");
        String holderSecretId = ConfTest.proverMasterSecretName;

        String credId = "cred-id-" + UUID.randomUUID().toString();

        JSONObject values = (new JSONObject()).
                put("attr1", "Value-1").
                put("attr2", 567).
                put("attr3", 5.7).
                put("attr4", "base64");
        List<ProposedAttrib> preview = Arrays.asList(
                new ProposedAttrib("attr1", "Value-1", "text/plain"),
                new ProposedAttrib("attr4", "base64", "image/png")
                );

        CompletableFuture<Boolean> issuerFuture = CompletableFuture.supplyAsync(
                () -> {
                    try (Context context = CloudContext.builder().
                            setServerUri(issuerParams.getServerAddress()).
                            setCredentials(issuerParams.getCredentials().getBytes(StandardCharsets.UTF_8)).
                            setP2p(issuerParams.getConnection()).
                            build()) {
                        Issuer issuerMachine = new Issuer(context, i2h, 60);
                        Thread.sleep(10);
                        return issuerMachine.issue(new Issuer.IssueParams().
                                        setValues(values).
                                        setSchema(schema).
                                        setCredDef(credDef).
                                        setComment("Hello Iam issuer").
                                        setLocale("en").
                                        setPreview(preview).
                                        setCredId(credId));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return false;
                });

        CompletableFuture<Pair<Boolean, String>> holderFuture = CompletableFuture.supplyAsync(
                () -> {
                    try (Context context = CloudContext.builder().
                            setServerUri(holderParams.getServerAddress()).
                            setCredentials(holderParams.getCredentials().getBytes(StandardCharsets.UTF_8)).
                            setP2p(holderParams.getConnection()).
                            build()) {
                        Event event = null;
                        try {
                            event = context.subscribe().getOne().get(30, TimeUnit.SECONDS);
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            e.printStackTrace();
                            return new Pair<Boolean, String>(false, "");
                        }
                        Message offer = event.message();
                        Assert.assertTrue(offer instanceof OfferCredentialMessage);
                        Holder holderMachine = new Holder(context, h2i, holderSecretId, "en");
                        Pair<Boolean, String> okCredId = holderMachine.accept((OfferCredentialMessage) offer, "Hello, Iam holder");
                        if (okCredId.first) {
                            String cred = context.getAnonCreds().proverGetCredential(okCredId.second);
                            System.out.println(cred);

                            JSONObject mimeTypes = Holder.getMimeTypes(context, okCredId.second);
                            Assert.assertEquals(2, mimeTypes.length());
                            Assert.assertEquals("text/plain", mimeTypes.optString("attr1"));
                            Assert.assertEquals("image/png", mimeTypes.optString("attr4"));
                        }
                        return okCredId;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail();
                    }
                    return null;
                }
        );

        boolean issueRes = issuerFuture.get(30, TimeUnit.SECONDS);
        boolean holderRes = holderFuture.get(30, TimeUnit.SECONDS).first;

        Assert.assertTrue(issueRes);
        Assert.assertTrue(holderRes);
    }
}
