package com.sirius.sdk.encryption;

import com.google.gson.JsonArray;
import com.goterl.lazycode.lazysodium.LazySodium;
import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.interfaces.*;
import com.goterl.lazycode.lazysodium.utils.Key;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import com.sirius.sdk.Main;
import com.sirius.sdk.errors.sirius_exceptions.SiriusCryptoError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusFieldValueError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidType;
import com.sirius.sdk.naclJava.CryptoAead;
import com.sirius.sdk.naclJava.LibSodium;

import java.lang.String;

import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Ed25519 {
    Custom custom = new Custom();

    public byte[] ensureIsBytes(String b58_or_bytes) {
        return custom.b58ToBytes(b58_or_bytes);
    }

    /**
     * Assemble the recipients block of a packed message.
     *
     * @param to_verkeys:  Verkeys of recipients
     * @param from_verkey: Sender Verkey needed to authcrypt package
     * @param from_sigkey: Sender Sigkey needed to authcrypt package
     * @return A tuple of (json result, key)
     */
    public Pair<String, Key> prepare_pack_recipient_keys(
            List<byte[]> to_verkeys,
            byte[] from_verkey,
            byte[] from_sigkey, KeyPair keyPair
    ) throws SiriusCryptoError, SodiumException {
        if ((from_verkey != null && from_sigkey == null) || (from_verkey == null && from_sigkey != null)) {
            throw new SiriusCryptoError("Both verkey and sigkey needed to authenticated encrypt message");
        }

        Key cek = LibSodium.getInstance().getLazySecretStream().cryptoSecretStreamKeygen();

        JSONArray recips = new JSONArray();
        String enc_cek = null;
        String enc_sender = null;
        byte[] nonce = null;
        for (byte[] target_vk : to_verkeys) {
            KeyPair keyPairToConvert = new KeyPair(Key.fromBytes(target_vk), Key.fromBytes(from_sigkey));
            KeyPair convertedKeyPair = LibSodium.getInstance().getLazySodium().convertKeyPairEd25519ToCurve25519(keyPairToConvert);
            Key target_pk = convertedKeyPair.getPublicKey();
            Key sk = convertedKeyPair.getSecretKey();
            if (from_verkey != null) {
                String sender_vk = custom.bytesToB58(from_verkey);
               // LibSodium.getInstance().getNativeBox().cryptoBoxSeal()

                enc_sender = new CryptoAead().cryptoBoxSealEasy(sender_vk, target_pk);
                nonce = LibSodium.getInstance().getLazySodium().randomBytesBuf(Box.NONCEBYTES);
            //    LibSodium.getInstance().getLazySecretBox().cryptoSecretBoxEasy()
              //  LibSodium.getInstance().getNativeBox().
               // enc_cek =    new CryptoAead().crypto_box(cek.getAsBytes(), nonce, target_pk.getAsBytes(), sk.getAsBytes());
                enc_cek =    new CryptoAead().cryptoBoxEasy(new String(cek.getAsBytes(),StandardCharsets.US_ASCII), nonce, convertedKeyPair);
            //  enc_cek = LibSodium.getInstance().getLazySodium().cryptoBoxEasy(new String(cek.getAsBytes(),StandardCharsets.US_ASCII), nonce, convertedKeyPair);

              //  enc_cek = LibSodium.getInstance().getLazySodium().cryptoSecretBoxEasy(new String(cek.getAsBytes(),StandardCharsets.US_ASCII), nonce, convertedKeyPair);
            } else {
                enc_sender = null;
                nonce = null;
                enc_cek = LibSodium.getInstance().getLazySodium().cryptoBoxSealEasy((new String(cek.getAsBytes(),StandardCharsets.US_ASCII)), target_pk);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("encrypted_key", custom.bytesToB64(enc_cek.getBytes(StandardCharsets.US_ASCII), true));
            JSONObject headerObject = new JSONObject();
            headerObject.put("kid", custom.bytesToB58(target_vk));
            if (enc_sender == null) {
                headerObject.put("sender", enc_sender);
            } else {
                headerObject.put("sender", custom.bytesToB64(enc_sender.getBytes(StandardCharsets.US_ASCII), true));
            }
            if (nonce == null) {
                headerObject.put("iv", nonce);
            } else {
                headerObject.put("iv", custom.bytesToB64(nonce, true));
            }
            jsonObject.put("header", headerObject);
            recips.put(jsonObject);
        }

        JSONObject data = new JSONObject();
        data.put("enc", "xchacha20poly1305_ietf");
        data.put("typ", "JWM/1.0");
        if (from_verkey != null) {
            data.put("alg", "Authcrypt");
        } else {
            data.put("alg", "Anoncrypt");
        }
        data.put("recipients", recips);
        return new Pair<>(data.toString(), cek);


    }


    /**
     * Locate pack recipient key.
     * Decode the encryption key and sender verification key from a
     * corresponding recipient block, if any is defined.
     *
     * @param recipients Recipients to locate
     * @return bytes, str, str A tuple of (cek, sender_vk, recip_vk_b58)
     * @throws SiriusFieldValueError: If no corresponding recipient key found
     */

    public DecryptModel locate_pack_recipient_key(List<JSONObject> recipients, KeyPair keyPair) throws SiriusFieldValueError, SodiumException {
        List<String> not_found = new ArrayList<>();
        for (JSONObject recip : recipients) {
            if (recip == null || !recip.has("header") || !recip.has("encrypted_key")) {
                throw new SiriusFieldValueError("Invalid recipient header");
            }
          //  JSONObject recipObj = new JSONObject(recip);
            JSONObject headerObj = recip.getJSONObject("header");
            String recip_vk_b58 = headerObj.getString("kid");

            if (!custom.bytesToB58(keyPair.getPublicKey().getAsBytes()).equals(recip_vk_b58)) {
                not_found.add(recip_vk_b58);
                continue;
            }
            KeyPair convertedKeyPair = LibSodium.getInstance().getLazySodium().convertKeyPairEd25519ToCurve25519(keyPair);

            byte[] encrypted_key = custom.b64ToBytes(recip.getString("encrypted_key"), true);
            String iv = headerObj.optString("iv");
            String sender = headerObj.optString("sender");
            byte[] nonce = null;
            byte[] enc_sender = null;
            if (iv != null && sender != null) {
                nonce = custom.b64ToBytes(iv, true);
                enc_sender = custom.b64ToBytes(sender, true);
            } else {
                nonce = null;
                enc_sender = null;
            }
            String sender_vk = null;
            String cek = null;
            if(nonce!=null && enc_sender!=null){
               //String enc_sender = LazySodium.toBin(enc_sender);
                String enc_senderHex = new String(enc_sender,StandardCharsets.US_ASCII);
              //  byte[] encSenderBytes =  LazySodium.toBin(enc_senderHex);
               // String enc_sender2 = new String(encSenderBytes,StandardCharsets.US_ASCII);
                sender_vk = new CryptoAead().cryptoBoxSealOpenEasy(enc_senderHex, convertedKeyPair);
             //     byte[] sender_vkBytes =  LazySodium.toBin(sender_vk);
               // String sender_vkstring = new String(sender_vkBytes,StandardCharsets.US_ASCII);
                byte[] senderBytes = custom.b58ToBytes(sender_vk);
                Key senderKey = Key.fromBytes(senderBytes);
                KeyPair senderKeyPair = new KeyPair(senderKey,senderKey);
                KeyPair senderConvertedKeyPair = LibSodium.getInstance().getLazySodium().convertKeyPairEd25519ToCurve25519(senderKeyPair);

                Key sender_pk = senderConvertedKeyPair.getPublicKey();
                KeyPair openKeyPair = new KeyPair(sender_pk,convertedKeyPair.getSecretKey());
                cek =   new CryptoAead().cryptoBoxOpenEasy(new String(encrypted_key,StandardCharsets.US_ASCII),nonce,openKeyPair);
              //  String cekHex = LibSodium.getInstance().getLazySodium().cryptoBoxOpenEasy(new String(encrypted_key,StandardCharsets.US_ASCII),nonce,openKeyPair);
                //  byte[] cekStringBytes =  LazySodium.toBin(cekHex);
                //  cek  = new String(cekStringBytes,StandardCharsets.US_ASCII);
            }else{
                sender_vk = null;
                cek =  LibSodium.getInstance().getLazySodium().cryptoBoxSealOpenEasy(new String(encrypted_key,StandardCharsets.US_ASCII), convertedKeyPair);
            }

            return new DecryptModel(cek, sender_vk, recip_vk_b58);
        }
        throw new SiriusFieldValueError(String.format("No corresponding recipient key found in %s", not_found));

    }

    /**
     * Encrypt the payload of a packed message.
     *
     * @param message  Message to encrypt
     * @param add_data additional data
     * @param key      Key used for encryption
     * @return A tuple of (ciphertext, nonce, tag)
     */
    public EncryptModel encryptPlaintext(
            String message, String add_data, Key key
    ) {

        byte[] nonce = LibSodium.getInstance().getLazySodium().randomBytesBuf(AEAD.CHACHA20POLY1305_IETF_NPUBBYTES);
        String outputHex = LibSodium.getInstance().getLazyAaed().encrypt(message, add_data, nonce, key, AEAD.Method.CHACHA20_POLY1305_IETF);
       byte[] outputBytes = LazySodium.toBin(outputHex);
        String output = new String(outputBytes,StandardCharsets.US_ASCII);
        int mlen = message.length();
        String ciphertext = output.substring(0,mlen);
        String tag = output.substring(mlen);
        return new EncryptModel(ciphertext.getBytes(StandardCharsets.US_ASCII), nonce, tag.getBytes(StandardCharsets.US_ASCII));

    }

    /**
     * Decrypt the payload of a packed message.
     *
     * @param ciphertext
     * @param recips_bin
     * @param nonce
     * @param key
     * @return The decrypted string
     */
    public String decryptPlaintext(String ciphertext, byte[] recips_bin, byte[] nonce, byte[] key) {
        Key keys = Key.fromBytes(key);
        //String output = new CryptoAead().decrypt(ciphertext, recips_bin, nonce, keys, AEAD.Method.CHACHA20_POLY1305_IETF);
        String output = LibSodium.getInstance().getLazyAaed().decrypt(ciphertext, new String(recips_bin,StandardCharsets.US_ASCII), nonce, keys, AEAD.Method.CHACHA20_POLY1305_IETF);
        return output;
    }


    /**
     * Assemble a packed message for a set of recipients, optionally including
     * the sender.
     *
     * @param message    The message to pack
     * @param toVerkeys  (Sequence of bytes or base58 string) The verkeys to pack the message for
     * @param fromVerkey (bytes or base58 string) The sender verkey
     * @param fromSigkey (bytes or base58 string) The sender sigkey
     * @return The encoded message
     */

   /* def pack_message(
            message: str,
            to_verkeys: Sequence[Union[bytes, str]],
            from_verkey: Union[bytes, str] = None,
            from_sigkey: Union[bytes, str] = None
    ) -> bytes:
    */
    //toVerkeys - LIST?
    public String packMessage(String message,
                              List<String> toVerkeys,
                              String fromVerkey,
                              String fromSigkey
    ) throws SiriusCryptoError, SodiumException {

        List<byte[]> toVerKeysBytes = new ArrayList<>();
        for (String vk : toVerkeys) {
            byte[] to_verkeys = ensureIsBytes(vk);
            toVerKeysBytes.add(to_verkeys);
        }

        byte[] from_verkey = ensureIsBytes(fromVerkey);
        byte[] from_sigkey = ensureIsBytes(fromSigkey);
        KeyPair keyPair = new KeyPair(Key.fromBytes(from_verkey), Key.fromBytes(from_sigkey));
        Pair<String, Key> stringKeyPair = prepare_pack_recipient_keys(toVerKeysBytes, from_verkey, from_sigkey, keyPair);
        String recips_json = stringKeyPair.getKey();
        String recips_b64 = custom.bytesToB64(recips_json.getBytes(StandardCharsets.US_ASCII), true);

        EncryptModel model = encryptPlaintext(message, recips_b64, stringKeyPair.getValue());
        System.out.println("packMEss nonce before="+new String(model.nonce,StandardCharsets.US_ASCII));
        System.out.println("packMEss chiper before="+new String(model.ciphertext,StandardCharsets.US_ASCII));
        System.out.println("packMEss tag before="+new String(model.tag,StandardCharsets.US_ASCII));
        String nonce64 = custom.bytesToB64(model.nonce, true);
        String ciphertext64 = custom.bytesToB64(model.ciphertext, true);
        String tag64 = custom.bytesToB64(model.tag, true);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("protected", recips_b64);
        System.out.println("packMessage nonce="+nonce64);
        jsonObject.put("iv", nonce64);
        jsonObject.put("ciphertext", ciphertext64);
        jsonObject.put("tag", tag64);
        return jsonObject.toString();
    }


    /**
     * Decode a packed message.
     * Disassemble and unencrypt a packed message, returning the message content,
     * verification key of the sender (if available), and verification key of the
     * recipient.
     *
     * @param encMessage: The encrypted message
     * @param myVerkey:   (bytes or base58 string) Verkey for decrypt
     * @param mySigkey:   (bytes or base58 string) Sigkey for decrypt
     * @return A tuple of (message, sender_vk, recip_vk)
     * @throws Exception (ValueError) If the packed message is invalid
     * @throws Exception * If the packed message reipients are invalid
     * @throws Exception If the pack algorithm is unsupported
     * @throws Exception If the sender's public key was not provided
     */
    public UnpackModel unpackMessage(String encMessage, String myVerkey, String mySigkey) throws SiriusInvalidType {
        byte[] my_verkey = ensureIsBytes(myVerkey);
        byte[] my_sigkey = ensureIsBytes(mySigkey);
        KeyPair keyPair = new KeyPair(Key.fromBytes(my_verkey), Key.fromBytes(my_sigkey));
        String error = "";
        try {
            error = "Expected dictionary";
            JSONObject encMessJson = new JSONObject(encMessage);
            error = "Invalid packed message";
            String protected_bin = encMessJson.getString("protected");
            byte[] recips_json = custom.b64ToBytes(protected_bin, true);
            error = "Invalid packed message recipients";
            JSONObject recips_outer = new JSONObject(new String(recips_json));
            String alg = recips_outer.getString("alg");
            boolean is_authcrypt = alg.equals("Authcrypt");
            if (!is_authcrypt && !alg.equals("Anoncrypt")) {
                throw new SiriusFieldValueError(String.format("Unsupported pack algorithm: %s", alg));
            }


            JSONArray recipentsArray = recips_outer.getJSONArray("recipients");
            List<JSONObject> recipents = new ArrayList<>();
            for (int i = 0; i < recipentsArray.length(); i++) {
                JSONObject recip = recipentsArray.getJSONObject(i);
                recipents.add(recip);
            }
          //  cek, sender_vk, recip_vk
            DecryptModel decryptModel  = locate_pack_recipient_key(recipents, keyPair);
            if (decryptModel.getSender_vk() == null && is_authcrypt) {
                throw new SiriusFieldValueError("Sender public key not provided for Authcrypt message");
            }
            byte[] ciphertext = custom.b64ToBytes(encMessJson.getString("ciphertext"), true);
            String ivNonce = encMessJson.getString("iv");
            System.out.println("nonce BEFORE 64 decode="+(ivNonce));
            System.out.println("nonce AFTER 64 decode="+ new String(custom.b64ToBytes(ivNonce,true)));
            byte[] nonce = custom.b64ToBytes(ivNonce, true);
            byte[] tag = custom.b64ToBytes(encMessJson.getString("tag"), true);
            String payload_bin = new String(ciphertext,  StandardCharsets.US_ASCII) + new String(tag, StandardCharsets.US_ASCII);
            System.out.println("tag="+ new String(tag));
            System.out.println("ciphertext="+new String(ciphertext));
            System.out.println("payload_bin="+payload_bin);

            byte[] allByteArray = new byte[ciphertext.length + tag.length];
            ByteBuffer buff = ByteBuffer.wrap(allByteArray);
            buff.put(ciphertext);
            buff.put(tag);
            byte[] combined = buff.array();



             payload_bin = new String(combined,  StandardCharsets.US_ASCII);
         //   payload_bin = new String(LazySodium.toBin(payload_bin),StandardCharsets.US_ASCII);
            System.out.println("payload_bin="+payload_bin);
            String message = decryptPlaintext(payload_bin, protected_bin.getBytes(StandardCharsets.US_ASCII),
                    nonce, decryptModel.cek.getBytes(StandardCharsets.US_ASCII));
            return new UnpackModel(message,decryptModel.sender_vk,decryptModel.recip_vk_b58);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SiriusInvalidType(error);
        }


     //   return null;
    }


}
