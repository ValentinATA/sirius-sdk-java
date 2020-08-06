package com.sirius.sdk.naclJava;

import com.goterl.lazycode.lazysodium.LazySodium;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.interfaces.AEAD;
import com.goterl.lazycode.lazysodium.interfaces.Box;
import com.goterl.lazycode.lazysodium.interfaces.Sign;
import com.goterl.lazycode.lazysodium.utils.Key;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import com.sirius.sdk.errors.sirius_exceptions.SiriusFieldValueError;


import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class CryptoAead {


    /**
     *     Decrypt the given ``ciphertext`` using the IETF ratified chacha20poly1305
     *     construction described in RFC7539.
     * @param ciphertext
     * @param aad
     * @param nonce
     * @param key
     * @return message
     */
/*
    public byte[]  cryptoAaeadChacha20poly1305IetfDecrypt(byte[] ciphertext,byte[] aad,byte[] nonce, byte[]key){

        int  clen = ciphertext.length;


        mxout = clen - crypto_aead_chacha20poly1305_ietf_ABYTES

        mlen = ffi.new("unsigned long long *")
        message = ffi.new("unsigned char[]", mxout)
        int aalen = 0;
        if (aad != null){
             aalen = aad.length;
        }

        LibSodium.getInstance().getLazySodium().cryptoAeadChaCha20Poly1305IetfDecrypt(ciphertext, clen, null,)

        res = lib.crypto_aead_chacha20poly1305_ietf_decrypt(message,
                mlen,
                ffi.NULL,
                ciphertext,
                clen,
                _aad,
                aalen,
                nonce,
                key)

        ensure(res == 0, "Decryption failed.", raising=exc.CryptoError)

        return ffi.buffer(message, mlen[0])[:]

    }

*/




/*

# Copyright 2017 Donald Stufft and individual contributors
        #
        # Licensed under the Apache License, Version 2.0 (the "License");
        # you may not use this file except in compliance with the License.
        # You may obtain a copy of the License at
        #
        # http://www.apache.org/licenses/LICENSE-2.0
        #
        # Unless required by applicable law or agreed to in writing, software
        # distributed under the License is distributed on an "AS IS" BASIS,
        # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        # See the License for the specific language governing permissions and
        # limitations under the License.

        from __future__ import absolute_import, division, print_function

        from nacl import exceptions as exc
        from nacl._sodium import ffi, lib
        from nacl.exceptions import ensure

        """
Implementations of authenticated encription with associated data (*AEAD*)
constructions building on the chacha20 stream cipher and the poly1305
authenticator
"""

        crypto_aead_chacha20poly1305_ietf_KEYBYTES = \
        lib.crypto_aead_chacha20poly1305_ietf_keybytes()
        crypto_aead_chacha20poly1305_ietf_NSECBYTES = \
        lib.crypto_aead_chacha20poly1305_ietf_nsecbytes()
        crypto_aead_chacha20poly1305_ietf_NPUBBYTES = \
        lib.crypto_aead_chacha20poly1305_ietf_npubbytes()
        crypto_aead_chacha20poly1305_ietf_ABYTES = \
        lib.crypto_aead_chacha20poly1305_ietf_abytes()
        crypto_aead_chacha20poly1305_ietf_MESSAGEBYTES_MAX = \
        lib.crypto_aead_chacha20poly1305_ietf_messagebytes_max()


        crypto_aead_chacha20poly1305_KEYBYTES = \
        lib.crypto_aead_chacha20poly1305_keybytes()
        crypto_aead_chacha20poly1305_NSECBYTES = \
        lib.crypto_aead_chacha20poly1305_nsecbytes()
        crypto_aead_chacha20poly1305_NPUBBYTES = \
        lib.crypto_aead_chacha20poly1305_npubbytes()
        crypto_aead_chacha20poly1305_ABYTES = \
        lib.crypto_aead_chacha20poly1305_abytes()
        crypto_aead_chacha20poly1305_MESSAGEBYTES_MAX = \
        lib.crypto_aead_chacha20poly1305_messagebytes_max()
        _aead_chacha20poly1305_CRYPTBYTES_MAX = \
        crypto_aead_chacha20poly1305_MESSAGEBYTES_MAX + \
        crypto_aead_chacha20poly1305_ABYTES

        crypto_aead_xchacha20poly1305_ietf_KEYBYTES = \
        lib.crypto_aead_xchacha20poly1305_ietf_keybytes()
        crypto_aead_xchacha20poly1305_ietf_NSECBYTES = \
        lib.crypto_aead_xchacha20poly1305_ietf_nsecbytes()
        crypto_aead_xchacha20poly1305_ietf_NPUBBYTES = \
        lib.crypto_aead_xchacha20poly1305_ietf_npubbytes()
        crypto_aead_xchacha20poly1305_ietf_ABYTES = \
        lib.crypto_aead_xchacha20poly1305_ietf_abytes()
        crypto_aead_xchacha20poly1305_ietf_MESSAGEBYTES_MAX = \
        lib.crypto_aead_xchacha20poly1305_ietf_messagebytes_max()
        _aead_xchacha20poly1305_ietf_CRYPTBYTES_MAX = \
        crypto_aead_xchacha20poly1305_ietf_MESSAGEBYTES_MAX + \
        crypto_aead_xchacha20poly1305_ietf_ABYTES


        def crypto_aead_chacha20poly1305_ietf_encrypt(message, aad, nonce, key):
        """
    Encrypt the given ``message`` using the IETF ratified chacha20poly1305
    construction described in RFC7539.

    :param message:
    :type message: bytes
    :param aad:
    :type aad: bytes
    :param nonce:
    :type nonce: bytes
    :param key:
    :type key: bytes
    :return: authenticated ciphertext
    :rtype: bytes
    """
        ensure(isinstance(message, bytes), 'Input message type must be bytes',
        raising=exc.TypeError)

        mlen = len(message)

        ensure(mlen <= crypto_aead_chacha20poly1305_ietf_MESSAGEBYTES_MAX,
        'Message must be at most {0} bytes long'.format(
        crypto_aead_chacha20poly1305_ietf_MESSAGEBYTES_MAX),
        raising=exc.ValueError)

        ensure(isinstance(aad, bytes) or (aad is None),
        'Additional data must be bytes or None',
        raising=exc.TypeError)

        ensure(isinstance(nonce, bytes) and
        len(nonce) == crypto_aead_chacha20poly1305_ietf_NPUBBYTES,
        'Nonce must be a {0} bytes long bytes sequence'.format(
        crypto_aead_chacha20poly1305_ietf_NPUBBYTES),
        raising=exc.TypeError)

        ensure(isinstance(key, bytes) and
        len(key) == crypto_aead_chacha20poly1305_ietf_KEYBYTES,
        'Key must be a {0} bytes long bytes sequence'.format(
        crypto_aead_chacha20poly1305_ietf_KEYBYTES),
        raising=exc.TypeError)

        if aad:
        _aad = aad
        aalen = len(aad)
        else:
        _aad = ffi.NULL
        aalen = 0

        mxout = mlen + crypto_aead_chacha20poly1305_ietf_ABYTES

        clen = ffi.new("unsigned long long *")

        ciphertext = ffi.new("unsigned char[]", mxout)

        res = lib.crypto_aead_chacha20poly1305_ietf_encrypt(ciphertext,
        clen,
        message,
        mlen,
        _aad,
        aalen,
        ffi.NULL,
        nonce,
        key)

        ensure(res == 0, "Encryption failed.", raising=exc.CryptoError)
        return ffi.buffer(ciphertext, clen[0])[:]



        def crypto_aead_chacha20poly1305_encrypt(message, aad, nonce, key):
        """
    Encrypt the given ``message`` using the "legacy" construction
    described in draft-agl-tls-chacha20poly1305.

    :param message:
    :type message: bytes
    :param aad:
    :type aad: bytes
    :param nonce:
    :type nonce: bytes
    :param key:
    :type key: bytes
    :return: authenticated ciphertext
    :rtype: bytes
    """
        ensure(isinstance(message, bytes), 'Input message type must be bytes',
        raising=exc.TypeError)

        mlen = len(message)

        ensure(mlen <= crypto_aead_chacha20poly1305_MESSAGEBYTES_MAX,
        'Message must be at most {0} bytes long'.format(
        crypto_aead_chacha20poly1305_MESSAGEBYTES_MAX),
        raising=exc.ValueError)

        ensure(isinstance(aad, bytes) or (aad is None),
        'Additional data must be bytes or None',
        raising=exc.TypeError)

        ensure(isinstance(nonce, bytes) and
        len(nonce) == crypto_aead_chacha20poly1305_NPUBBYTES,
        'Nonce must be a {0} bytes long bytes sequence'.format(
        crypto_aead_chacha20poly1305_NPUBBYTES),
        raising=exc.TypeError)

        ensure(isinstance(key, bytes) and
        len(key) == crypto_aead_chacha20poly1305_KEYBYTES,
        'Key must be a {0} bytes long bytes sequence'.format(
        crypto_aead_chacha20poly1305_KEYBYTES),
        raising=exc.TypeError)

        if aad:
        _aad = aad
        aalen = len(aad)
        else:
        _aad = ffi.NULL
        aalen = 0

        mlen = len(message)
        mxout = mlen + crypto_aead_chacha20poly1305_ietf_ABYTES

        clen = ffi.new("unsigned long long *")

        ciphertext = ffi.new("unsigned char[]", mxout)

        res = lib.crypto_aead_chacha20poly1305_encrypt(ciphertext,
        clen,
        message,
        mlen,
        _aad,
        aalen,
        ffi.NULL,
        nonce,
        key)

        ensure(res == 0, "Encryption failed.", raising=exc.CryptoError)
        return ffi.buffer(ciphertext, clen[0])[:]


        def crypto_aead_chacha20poly1305_decrypt(ciphertext, aad, nonce, key):
        """
    Decrypt the given ``ciphertext`` using the "legacy" construction
    described in draft-agl-tls-chacha20poly1305.

    :param ciphertext: authenticated ciphertext
    :type ciphertext: bytes
    :param aad:
    :type aad: bytes
    :param nonce:
    :type nonce: bytes
    :param key:
    :type key: bytes
    :return: message
    :rtype: bytes
    """
        ensure(isinstance(ciphertext, bytes),
        'Input ciphertext type must be bytes',
        raising=exc.TypeError)

        clen = len(ciphertext)

        ensure(clen <= _aead_chacha20poly1305_CRYPTBYTES_MAX,
        'Ciphertext must be at most {0} bytes long'.format(
        _aead_chacha20poly1305_CRYPTBYTES_MAX),
        raising=exc.ValueError)

        ensure(isinstance(aad, bytes) or (aad is None),
        'Additional data must be bytes or None',
        raising=exc.TypeError)

        ensure(isinstance(nonce, bytes) and
        len(nonce) == crypto_aead_chacha20poly1305_NPUBBYTES,
        'Nonce must be a {0} bytes long bytes sequence'.format(
        crypto_aead_chacha20poly1305_NPUBBYTES),
        raising=exc.TypeError)

        ensure(isinstance(key, bytes) and
        len(key) == crypto_aead_chacha20poly1305_KEYBYTES,
        'Key must be a {0} bytes long bytes sequence'.format(
        crypto_aead_chacha20poly1305_KEYBYTES),
        raising=exc.TypeError)

        mxout = clen - crypto_aead_chacha20poly1305_ABYTES

        mlen = ffi.new("unsigned long long *")
        message = ffi.new("unsigned char[]", mxout)

        if aad:
        _aad = aad
        aalen = len(aad)
        else:
        _aad = ffi.NULL
        aalen = 0

        res = lib.crypto_aead_chacha20poly1305_decrypt(message,
        mlen,
        ffi.NULL,
        ciphertext,
        clen,
        _aad,
        aalen,
        nonce,
        key)

        ensure(res == 0, "Decryption failed.", raising=exc.CryptoError)

        return ffi.buffer(message, mlen[0])[:]


        def crypto_aead_xchacha20poly1305_ietf_encrypt(message, aad, nonce, key):
        """
    Encrypt the given ``message`` using the long-nonces xchacha20poly1305
    construction.

    :param message:
    :type message: bytes
    :param aad:
    :type aad: bytes
    :param nonce:
    :type nonce: bytes
    :param key:
    :type key: bytes
    :return: authenticated ciphertext
    :rtype: bytes
    """
        ensure(isinstance(message, bytes), 'Input message type must be bytes',
        raising=exc.TypeError)

        mlen = len(message)

        ensure(mlen <= crypto_aead_xchacha20poly1305_ietf_MESSAGEBYTES_MAX,
        'Message must be at most {0} bytes long'.format(
        crypto_aead_xchacha20poly1305_ietf_MESSAGEBYTES_MAX),
        raising=exc.ValueError)

        ensure(isinstance(aad, bytes) or (aad is None),
        'Additional data must be bytes or None',
        raising=exc.TypeError)

        ensure(isinstance(nonce, bytes) and
        len(nonce) == crypto_aead_xchacha20poly1305_ietf_NPUBBYTES,
        'Nonce must be a {0} bytes long bytes sequence'.format(
        crypto_aead_xchacha20poly1305_ietf_NPUBBYTES),
        raising=exc.TypeError)

        ensure(isinstance(key, bytes) and
        len(key) == crypto_aead_xchacha20poly1305_ietf_KEYBYTES,
        'Key must be a {0} bytes long bytes sequence'.format(
        crypto_aead_xchacha20poly1305_ietf_KEYBYTES),
        raising=exc.TypeError)

        if aad:
        _aad = aad
        aalen = len(aad)
        else:
        _aad = ffi.NULL
        aalen = 0

        mlen = len(message)
        mxout = mlen + crypto_aead_xchacha20poly1305_ietf_ABYTES

        clen = ffi.new("unsigned long long *")

        ciphertext = ffi.new("unsigned char[]", mxout)

        res = lib.crypto_aead_xchacha20poly1305_ietf_encrypt(ciphertext,
        clen,
        message,
        mlen,
        _aad,
        aalen,
        ffi.NULL,
        nonce,
        key)

        ensure(res == 0, "Encryption failed.", raising=exc.CryptoError)
        return ffi.buffer(ciphertext, clen[0])[:]


        def crypto_aead_xchacha20poly1305_ietf_decrypt(ciphertext, aad, nonce, key):
        """
    Decrypt the given ``ciphertext`` using the long-nonces xchacha20poly1305
    construction.

    :param ciphertext: authenticated ciphertext
    :type ciphertext: bytes
    :param aad:
    :type aad: bytes
    :param nonce:
    :type nonce: bytes
    :param key:
    :type key: bytes
    :return: message
    :rtype: bytes
    """
        ensure(isinstance(ciphertext, bytes),
        'Input ciphertext type must be bytes',
        raising=exc.TypeError)

        clen = len(ciphertext)

        ensure(clen <= _aead_xchacha20poly1305_ietf_CRYPTBYTES_MAX,
        'Ciphertext must be at most {0} bytes long'.format(
        _aead_xchacha20poly1305_ietf_CRYPTBYTES_MAX),
        raising=exc.ValueError)

        ensure(isinstance(aad, bytes) or (aad is None),
        'Additional data must be bytes or None',
        raising=exc.TypeError)

        ensure(isinstance(nonce, bytes) and
        len(nonce) == crypto_aead_xchacha20poly1305_ietf_NPUBBYTES,
        'Nonce must be a {0} bytes long bytes sequence'.format(
        crypto_aead_xchacha20poly1305_ietf_NPUBBYTES),
        raising=exc.TypeError)

        ensure(isinstance(key, bytes) and
        len(key) == crypto_aead_xchacha20poly1305_ietf_KEYBYTES,
        'Key must be a {0} bytes long bytes sequence'.format(
        crypto_aead_xchacha20poly1305_ietf_KEYBYTES),
        raising=exc.TypeError)

        mxout = clen - crypto_aead_xchacha20poly1305_ietf_ABYTES
        mlen = ffi.new("unsigned long long *")
        message = ffi.new("unsigned char[]", mxout)

        if aad:
        _aad = aad
        aalen = len(aad)
        else:
        _aad = ffi.NULL
        aalen = 0

        res = lib.crypto_aead_xchacha20poly1305_ietf_decrypt(message,
        mlen,
        ffi.NULL,
        ciphertext,
        clen,
        _aad,
        aalen,
        nonce,
        key)

        ensure(res == 0, "Decryption failed.", raising=exc.CryptoError)

        return ffi.buffer(message, mlen[0])[:]
*/

    /**
     * Converts a public Ed25519 key (encoded as bytes ``public_key_bytes``) to
     * a public Curve25519 key as bytes
     *
     * @param public_key_bytes: bytes
     * @return bytes
     * @throws com.sirius.sdk.errors.sirius_exceptions.SiriusFieldValueError if ``public_key_bytes`` is not of length
     *                                                                       ``crypto_sign_PUBLICKEYBYTES``.
     */
    public byte[] crypto_sign_ed25519_pk_to_curve25519(byte[] public_key_bytes) throws SiriusFieldValueError {

        if (public_key_bytes.length != Sign.PUBLICKEYBYTES) {
            throw new SiriusFieldValueError("Invalid curve public key");
        }
        int curve_public_key_len = Sign.CURVE25519_PUBLICKEYBYTES;
        // curve_public_key = ffi.new("unsigned char[]", curve_public_key_len)


        // rc = lib.crypto_sign_ed25519_pk_to_curve25519(curve_public_key,
        //        public_key_bytes)
   /*

        curve_public_key_len = crypto_sign_curve25519_BYTES
        curve_public_key = ffi.new("unsigned char[]", curve_public_key_len)


        ensure(rc == 0,
        'Unexpected library error',
        raising=exc.RuntimeError)

        return ffi.buffer(curve_public_key, curve_public_key_len)[:]*/
        return null;
    }


    //  public KeyPair convertKeyPairEd25519ToCurve25519(KeyPair ed25519KeyPair) throws SodiumException {
    /*    byte[] edPkBytes = ed25519KeyPair.getPublicKey().getAsBytes();
        byte[] edSkBytes = ed25519KeyPair.getSecretKey().getAsBytes();
        byte[] curvePkBytes = new byte[32];
        byte[] curveSkBytes = new byte[32];
        boolean pkSuccess = this.convertPublicKeyEd25519ToCurve25519(curvePkBytes, edPkBytes);
        boolean skSuccess = this.convertSecretKeyEd25519ToCurve25519(curveSkBytes, edSkBytes);
        if (pkSuccess && skSuccess) {
            return new KeyPair(Key.fromBytes(curvePkBytes), Key.fromBytes(curveSkBytes));
        } else {
            throw new SodiumException("Could not convert this key pair.");
        }*/
    //  }



    public String decrypt(String cipher, byte[] additionalData, byte[] nPub, Key k, com.goterl.lazycode.lazysodium.interfaces.AEAD.Method method) {
        return this.decrypt(cipher, additionalData, (byte[]) null, nPub, k, method);
    }

    public String decrypt(String cipher, byte[] additionalData, byte[] nSec, byte[] nPub, Key k, com.goterl.lazycode.lazysodium.interfaces.AEAD.Method method) {
        byte[] cipherBytes = cipher.getBytes(StandardCharsets.US_ASCII);
        byte[] additionalDataBytes = additionalData == null ? new byte[0] : additionalData;
        long additionalBytesLen = additionalData == null ? 0L : (long) additionalDataBytes.length;
        byte[] keyBytes = k.getAsBytes();
        byte[] messageBytes;
        if (method.equals(com.goterl.lazycode.lazysodium.interfaces.AEAD.Method.CHACHA20_POLY1305_IETF)) {
            messageBytes = new byte[cipherBytes.length - 16];
            long[] mlen = new long[messageBytes.length];
            LibSodium.getInstance().getLazySodium().cryptoAeadChaCha20Poly1305IetfDecrypt(messageBytes, null, nSec, cipherBytes, (long) cipherBytes.length, additionalDataBytes, additionalBytesLen, nPub, keyBytes);
            return new String(messageBytes,StandardCharsets.US_ASCII);
        }
        return null;
    }

    /**
     * Encrypts and returns a message ``message`` using the secret key ``sk``,
     *     public key ``pk``, and the nonce ``nonce``.
     *
     *     :param message: bytes
     *     :param nonce: bytes
     *     :param pk: bytes
     *     :param sk: bytes
     *     :rtype: bytes
     */
  /*  public String  crypto_box(byte[] message, byte[]nonce, byte[]pk, byte[]sk){
      //  Box.
        padded = (b"\x00" * crypto_box_ZEROBYTES) + message
        ciphertext = ffi.new("unsigned char[]", len(padded))

        rc = lib.crypto_box(ciphertext, padded, len(padded), nonce, pk, sk)
        ensure(rc == 0,
                'Unexpected library error',
                raising=exc.RuntimeError)

        return ffi.buffer(ciphertext, len(padded))[crypto_box_BOXZEROBYTES:]
    }*/

    public String cryptoBoxEasy(String message, byte[] nonce, KeyPair keyPair) throws SodiumException {
        byte[] messageBytes = message.getBytes(StandardCharsets.US_ASCII);

        ByteArrayOutputStream bObj = new ByteArrayOutputStream();
        bObj.reset();
        byte[] cipherBytesPadding = new byte[32];
        for (byte cipherBytesPadding1 :cipherBytesPadding ){
            bObj.write(cipherBytesPadding1);
        }
        for (byte mesByte : messageBytes ){
            bObj.write(mesByte);
        }
        byte[] cipherBytes = new byte[32 + messageBytes.length];
        byte[] messageBytesPadded = bObj.toByteArray();
        boolean res = LibSodium.getInstance().getNativeBox().cryptoBoxEasy(cipherBytes, messageBytesPadded, (long)messageBytesPadded.length, nonce, keyPair.getPublicKey().getAsBytes(), keyPair.getSecretKey().getAsBytes());
        if (!res) {
            throw new SodiumException("Could not encrypt your message.");
        } else {
            ByteArrayOutputStream bObj2 = new ByteArrayOutputStream();
            bObj2.reset();
            int i=0;
            for (byte mesByte : cipherBytes ){
                if(i<=15){
                    i++;
                    continue;
                }
                bObj2.write(mesByte);

            }
            byte[] message16 = bObj2.toByteArray();
            return new String(message16,StandardCharsets.US_ASCII);

            //return new String(cipherBytes);
        }
    }

    public String cryptoBoxOpenEasy(String cipherText, byte[] nonce, KeyPair keyPair) throws SodiumException {
        byte[] cipher = cipherText.getBytes(StandardCharsets.US_ASCII);
        byte[] message = new byte[cipher.length + 16];
        byte[] cipherBytesPadding = new byte[16];
        ByteArrayOutputStream bObj = new ByteArrayOutputStream();
        bObj.reset();

        for (byte cipherBytesPadding1 :cipherBytesPadding ){
            bObj.write(cipherBytesPadding1);
        }
        for (byte mesByte : cipher ){
            bObj.write(mesByte);
        }
        byte[] padded = bObj.toByteArray();


        boolean res =  LibSodium.getInstance().getNativeBox().cryptoBoxOpenEasy(message, padded, (long)padded.length, nonce, keyPair.getPublicKey().getAsBytes(), keyPair.getSecretKey().getAsBytes());
        if (!res) {
            throw new SodiumException("Could not decrypt your message.");
        } else {
           // new byte[32]{message}
            ByteArrayOutputStream bObj2 = new ByteArrayOutputStream();
            bObj2.reset();
            int i=0;
            for (byte mesByte : message ){
                if(i<=31){
                    i++;
                    continue;
                }
                bObj2.write(mesByte);

            }

            byte[] messageAfter32 = bObj2.toByteArray();
            return new String(messageAfter32,StandardCharsets.US_ASCII);
        }
    }

    public String cryptoBoxSealEasy(String messageString, Key publicKey) throws SodiumException {
        byte[] keyBytes = publicKey.getAsBytes();
        byte[] message = messageString.getBytes(StandardCharsets.US_ASCII);
        int _mlen = message.length;
        int _clen = 48 + _mlen;
        byte[] ciphertext = new byte[_clen];
        if (!LibSodium.getInstance().getNativeBox().cryptoBoxSeal(ciphertext, message, (long)_mlen, keyBytes)) {
            throw new SodiumException("Could not encrypt message.");
        } else {
            return new String(ciphertext,StandardCharsets.US_ASCII);
        }
    }

    public String cryptoBoxSealOpenEasy(String cipherString, KeyPair keyPair) throws SodiumException {
        byte[] cipherText = cipherString.getBytes(StandardCharsets.US_ASCII);
        int _clen = cipherText.length;
        int _mlen = _clen - 48;

        byte[] plaintext = new byte[_mlen];
        boolean res = LibSodium.getInstance().getNativeBox().cryptoBoxSealOpen(plaintext, cipherText, (long)_clen, keyPair.getPublicKey().getAsBytes(), keyPair.getSecretKey().getAsBytes());
        if (!res) {
            throw new SodiumException("Could not decrypt your message.");
        } else {
            return new String(plaintext,StandardCharsets.US_ASCII);
        }
    }



/*

    def crypto_box_seal(message, pk):
            """
    Encrypts and returns a message ``message`` using an ephemeral secret key
    and the public key ``pk``.
    The ephemeral public key, which is embedded in the sealed box, is also
    used, in combination with ``pk``, to derive the nonce needed for the
    underlying box construct.

    :param message: bytes
    :param pk: bytes
    :rtype: bytes

    .. versionadded:: 1.2
    """
    ensure(isinstance(message, bytes),
           "input message must be bytes",
    raising=TypeError)

    ensure(isinstance(pk, bytes),
           "public key must be bytes",
    raising=TypeError)

            if len(pk) != crypto_box_PUBLICKEYBYTES:
    raise exc.ValueError("Invalid public key")

    _mlen = len(message)
    _clen = crypto_box_SEALBYTES + _mlen

            ciphertext = ffi.new("unsigned char[]", _clen)

    rc = lib.crypto_box_seal(ciphertext, message, _mlen, pk)
    ensure(rc == 0,
           'Unexpected library error',
           raising=exc.RuntimeError)

    return ffi.buffer(ciphertext, _clen)[:]
*/



/*
    def crypto_box_open(ciphertext, nonce, pk, sk):
            """
    Decrypts and returns an encrypted message ``ciphertext``, using the secret
    key ``sk``, public key ``pk``, and the nonce ``nonce``.

    :param ciphertext: bytes
    :param nonce: bytes
    :param pk: bytes
    :param sk: bytes
    :rtype: bytes
    """
            if len(nonce) != crypto_box_NONCEBYTES:
    raise exc.ValueError("Invalid nonce size")

    if len(pk) != crypto_box_PUBLICKEYBYTES:
    raise exc.ValueError("Invalid public key")

    if len(sk) != crypto_box_SECRETKEYBYTES:
    raise exc.ValueError("Invalid secret key")

    padded = (b"\x00" * crypto_box_BOXZEROBYTES) + ciphertext
            plaintext = ffi.new("unsigned char[]", len(padded))

    res = lib.crypto_box_open(plaintext, padded, len(padded), nonce, pk, sk)
    ensure(res == 0, "An error occurred trying to decrypt the message",
           raising=exc.CryptoError)

    return ffi.buffer(plaintext, len(padded))[crypto_box_ZEROBYTES:]

*/

 /*   public String cryptoBoxSealEasy(String message, Key publicKey) throws SodiumException {
        byte[] keyBytes = publicKey.getAsBytes();
        byte[] messageBytes = message.getBytes(StandardCharsets.US_ASCII);

        byte[] cipher = new byte[Box.SEALBYTES + messageBytes.length];
        if (!LibSodium.getInstance().getNativeBox().cryptoBoxSeal(cipher, messageBytes, (long)messageBytes.length, keyBytes)) {
            throw new SodiumException("Could not encrypt message.");
        } else {
            return new String(cipher,StandardCharsets.US_ASCII);
        }
    }*/

}