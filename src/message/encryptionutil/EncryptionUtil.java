package message.encryptionutil;

import java.io.*;
import java.util.*;
import javax.crypto.*;
import java.security.*;

public class EncryptionUtil{
    /**
     * String to hold name of the encryption algorithm.
     */
    public static final String ENCRYPT_ALGORITHM = "RSA";

    /**
     * String to hold name of the signature algorithm.
     */
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * Integer to hold the length of RSA key
     */
    public static final Integer RSA_KEY_LENGTH = 2048;

    /**
     * Integer to hold the length of summary
     */
    public static final Integer SUMMARY_LENGTH = 10;

    /**
     * Convert String to byte array.
     */
    public static final byte[] String2ByteArray(String st) throws Exception{
        return st.getBytes("ISO-8859-1");
    }

    /**
     * Convert byte array to String.
     */
    public static final String ByteArray2String(byte[] byteArray)throws Exception{
        return new String(byteArray,"ISO-8859-1");
    }

    /**
     * Generate key which contains a pair of private and public key using 1024
     * bytes. Store the set of keys in Prvate.key and Public.key files.
     *
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static KeyPair generate(){
        KeyPair keys = null;
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ENCRYPT_ALGORITHM);
            keyGen.initialize(RSA_KEY_LENGTH);
            keys = keyGen.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keys;
    }

    /**
     * Encrypt the plain text using public key.
     *
     * @param text
     *          : original plain text
     * @param key
     *          :The public key
     * @return Encrypted text
     * @throws java.lang.Exception
     */
    public static String encryptChunk(String text, PublicKey key) {
        String cipherText = "";
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = ByteArray2String(cipher.doFinal(String2ByteArray(text)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    public static String encrypt(String text, PublicKey key) {
        String cipherText = "";
        int len = text.length();
        for(int i = 0; i < ((len - 1) / 200 + 1); i++){
            int start = i * 200;
            int end = start + 200;
            if(end > len){
                end = len;
            }
            cipherText = cipherText +
                encryptChunk(text.substring(start,end),key);
        }
        return cipherText;
    }


    /**
     * Decrypt text using private key.
     *
     * @param text
     *          :encrypted text
     * @param key
     *          :The private key
     * @return plain text
     * @throws java.lang.Exception
     */
    public static String decryptChunk(String text, PrivateKey key) {
        String decryptedText = "";
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);

            // decrypt the text using the private key
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedText = ByteArray2String(cipher.doFinal(String2ByteArray(text)));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return decryptedText;
    }

    public static String decrypt(String text, PrivateKey key) throws Exception{
        String decryptedText = "";

        Scanner sc = new Scanner(text);
        sc.useDelimiter("");
        while(sc.hasNext()){
            String chunk = "";
            for(int i = 0; i < 256; i++){
                char c = sc.next().charAt(0);
                chunk = chunk + c;
            }
            decryptedText = decryptedText + decryptChunk(chunk,key);
        }
        return decryptedText;
    }


    /**
     * Sign the text using privete key.
     *
     * @param text
     *          :origin text
     * @param key
     *          :the private key
     * @return signed text
     * @throws java.lang.Exception
     */
    public static String sign(String text, PrivateKey key){
        String sign = "";
        try{
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(key);
            signature.update(String2ByteArray(text));
            sign = ByteArray2String(signature.sign());
        } catch(Exception e){
            e.printStackTrace();
        }
        return sign;
    }

    /**
     * Verify if the signed text belongs to the owner of the private key.
     * @param text
     *          :origin text
     * @param key
     *          :public key
     * @param sign
     *          :signature
     * @return boolean,if verification succeed,return true，otherwise return false
     * @throws java.lang.Exception
     **/
    public static boolean verify(String text,PublicKey key,String sign) {
        Boolean result = false;
        try{
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(key);
            signature.update(String2ByteArray(text));
            result = signature.verify(String2ByteArray(sign));
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Return the summary of a long String.
     * @param text
     *          :origin text
     * @return String
     * @throws java.lang.Exception
     **/
    public static String summary(String text) throws Exception{
        String result;
        if (text.length() < SUMMARY_LENGTH) {
            result = text;
        } else{
            result = text.substring(0,SUMMARY_LENGTH) + "...";
        }
        return result;
    }

    /**
     * Test the EncryptionUtil
     */
    public static void main(String[] args) {

        try {
            Scanner sc = new Scanner(System.in);

            KeyPair keys = generate();
            final PublicKey publicKey = keys.getPublic();
            final PrivateKey privateKey = keys.getPrivate();

            final String originalText = sc.next();

            // Encrypt the string using the public key.
            final String cipherText = encrypt(originalText, publicKey);

            // Decrypt the cipher text using the private key.
            final String plainText = decrypt(cipherText, privateKey);

            // Test signature algorithm.
            final String signText = sign(originalText, privateKey);
            final Boolean verifyResult = verify(originalText, publicKey,signText);

            // Test summary algorithm
            final String summaryResult = summary(originalText);

            // Printing the Original, Encrypted and Decrypted Text
            System.out.println("Original: " + originalText);
            System.out.println("Encrypted: " + cipherText);
            System.out.println("Decrypted: " + plainText);
            System.out.println("Signed: " + signText);
            System.out.println("Decrypted: " + verifyResult);
            System.out.println("Summary: " + summaryResult);
            System.out.println(signText.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
