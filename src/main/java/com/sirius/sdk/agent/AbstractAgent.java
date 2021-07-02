package com.sirius.sdk.agent;

import com.sirius.sdk.agent.connections.AgentEvents;
import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.microledgers.AbstractMicroledgerList;
import com.sirius.sdk.agent.microledgers.MicroledgerList;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.WalletPairwiseList;
import com.sirius.sdk.agent.wallet.DynamicWallet;
import com.sirius.sdk.errors.sirius_exceptions.SiriusRPCError;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;
import com.sirius.sdk.utils.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractAgent extends TransportLayer {

    List<Endpoint> endpoints;
    Map<String, Ledger> ledgers = new HashMap<>();
    WalletPairwiseList pairwiseList;
    DynamicWallet wallet;
    MicroledgerList microledgers;
    AbstractImmutableCollection storage;
    AgentEvents events;

    public abstract void open();

    public abstract boolean isOpen();

    public abstract String getName();

    /**
     * Implementation of basicmessage feature
     * See details:
     * - https://github.com/hyperledger/aries-rfcs/tree/master/features/0095-basic-message
     *
     * @param message      Message
     *                     See details:
     *                     - https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0020-message-types
     * @param their_vk     Verkey of recipient
     * @param endpoint     Endpoint address of recipient
     * @param my_vk        VerKey of Sender (AuthCrypt mode)
     *                     See details:
     *                     - https://github.com/hyperledger/aries-rfcs/tree/master/features/0019-encryption-envelope#authcrypt-mode-vs-anoncrypt-mode
     * @param routing_keys Routing key of recipient
     * @return
     */
    public abstract Pair<Boolean, Message> sendMessage(Message message, List<String> their_vk,
                                              String endpoint, String my_vk, List<String> routing_keys);

    public void sendTo(Message message, Pairwise to) {
        sendMessage(message, Collections.singletonList(to.getTheir().getVerkey()), to.getTheir().getEndpoint(), to.getMe().getVerkey(), to.getTheir().getRoutingKeys());
    }

    public abstract void close();

    public abstract boolean checkIsOpen();

    public abstract Listener subscribe();

    public abstract String generateQrCode(String value);

    public AgentEvents getEvents() {
        checkIsOpen();
        return events;
    }

    public DynamicWallet getWallet() {
        checkIsOpen();
        return wallet;
    }

    public List<Endpoint> getEndpoints() {
        checkIsOpen();
        return endpoints;
    }

    public Map<String, Ledger> getLedgers() {
        checkIsOpen();
        return ledgers;
    }


    public AbstractMicroledgerList getMicroledgers() {
        checkIsOpen();
        return microledgers;
    }

    public WalletPairwiseList getPairwiseList() {
        checkIsOpen();
        return pairwiseList;
    }

}
