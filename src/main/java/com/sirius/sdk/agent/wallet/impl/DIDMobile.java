package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractDID;
import com.sirius.sdk.utils.Pair;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.crypto.CryptoJSONParameters;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidJSONParameters;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Time;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DIDMobile extends AbstractDID {
    Wallet wallet;
    int timeoutSec = 60;

    public DIDMobile(Wallet wallet) {
        this.wallet = wallet;
    }

    @Override
    public Pair<String, String> createAndStoreMyDid(String did, String seed, Boolean cid) {
        try {
            DidResults.CreateAndStoreMyDidResult res = Did.createAndStoreMyDid(
                    wallet,
                    new DidJSONParameters.CreateAndStoreMyDidJSONParameter(did, seed, null, cid).toJson()).
                    get(timeoutSec, TimeUnit.SECONDS);
            return new Pair<>(res.getDid(), res.getVerkey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void storeTheirDid(String did, String verkey) {
        JSONObject identityJson = new JSONObject().
                put("did", did);
        if (verkey != null)
            identityJson.put("verkey", verkey);
        try {
            Did.storeTheirDid(wallet, identityJson.toString()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setDidMetadata(String did, String metadata) {
        try {
            Did.setDidMetadata(wallet, did, metadata).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Object> listMyDidsWithMeta() {
        try {
            String listDidsWithMetaJson = Did.getListMyDidsWithMeta(wallet).get(timeoutSec, TimeUnit.SECONDS);
            JSONArray listDidsWithMeta = new JSONArray(listDidsWithMetaJson);
            return listDidsWithMeta.toList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getDidMetadata(String did) {
        try {
            return Did.getDidMetadata(wallet, did).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String keyForLocalDid(String did) {
        try {
            return Did.keyForLocalDid(wallet, did).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String keyForDid(String poolName, String did) {
        try {
            Pool pool = Pool.openPoolLedger(poolName, null).get(timeoutSec, TimeUnit.SECONDS);
            return Did.keyForDid(pool, wallet, did).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String createKey(String seed) {
        return null;
    }

    @Override
    public String replaceKeysStart(String did, String seed) {
        try {
            return Did.replaceKeysStart(wallet, did, new CryptoJSONParameters.CreateKeyJSONParameter(seed, null).toJson()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void replaceKeysApply(String did) {
        try {
            Did.replaceKeysApply(wallet, did).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setKeyMetadata(String verkey, String metadata) {

    }

    @Override
    public String getKeyMetadata(String verkey) {
        return null;
    }

    @Override
    public void setEndpointForDid(String did, String address, String transportKey) {
        try {
            Did.setEndpointForDid(wallet, did, address, transportKey).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Pair<String, String> getEndpointForDid(String pooName, String did) {
        try {
            Pool pool = Pool.openPoolLedger(pooName, null).get(timeoutSec, TimeUnit.SECONDS);
            DidResults.EndpointForDidResult res = Did.getEndpointForDid(wallet, pool, did).get(timeoutSec, TimeUnit.SECONDS);
            return new Pair<>(res.getAddress(), res.getTransportKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getMyDidMeta(String did) {
        try {
            return Did.getDidMetadata(wallet, did).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String abbreviateVerKey(String did, String fullVerkey) {
        return null;
    }

    @Override
    public String qualifyDid(String did, String method) {
        try {
            return Did.qualifyDid(wallet, did, method).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
