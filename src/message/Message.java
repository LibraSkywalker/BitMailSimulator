package message;

import java.util.*;
import java.util.Base64.*;
import javax.crypto.*;
import java.security.*;
import java.security.spec.*;
import message.encryptionutil.*;

public class Message{
    private int number_of_signature = 0;
    private String message;
    private String signature;

    private String cipherSign;
    private String content;
    private int sender;

    public Message(){}

    public Message(String msg) throws Exception{
        Scanner sc = new Scanner(msg);
        //Parse the signature
        sc.useDelimiter("");
        String sign = "";
        for(int i = 0; i < 256; i++){
            char c = sc.next().charAt(0);
            sign = sign + c;
        }
        signature = sign;

        // Read the cypher text.
        message = "";
        while(sc.hasNext()){
            message = message + sc.next();
        }

    }

    public String toString(){
        return signature + message;
    }

    public KeyPair Generate() throws Exception{
        return EncryptionUtil.generate();
    }

    public void Encrypt(int sender,String content,PrivateKey mykey,PublicKey key) throws Exception{
        String sign = EncryptionUtil.sign(content,mykey);
        String msg = sender + " " + sign + content;
        message = EncryptionUtil.encrypt(msg,key);
        signature = EncryptionUtil.sign(message,mykey);
        this.sender = sender;
    }

    // Decrypt the message using private key.
    // Return false if the decode process is wrong.
    public Boolean Decrypt(PrivateKey key) {
        try{
            String msg = EncryptionUtil.decrypt(message,key);
            Scanner sc = new Scanner(msg);
            sender = sc.nextInt();
            sc.useDelimiter("");
            sc.next();

            //Parse the signature
            cipherSign = "";
            for(int i = 0; i < 256; i++){
                char c = sc.next().charAt(0);
                cipherSign = cipherSign + c;
            }
            // Read the cypher text.
            content = "";
            while(sc.hasNext()){
                content = content + sc.next();
            }

        }catch(Exception e){
            return false;
        }
        return true;
    }

    public Boolean ValidateCipher(PublicKey key)throws Exception{
        return EncryptionUtil.verify(content,key,cipherSign);
    }

    public String getContent(){
        return content;
    }

    public int getSender() {
        return sender;
    }

    public boolean Validate(PublicKey key) throws Exception{
        return EncryptionUtil.verify(message,key,signature);
    }

    public static String PublicKey2String(PublicKey key) throws Exception{
        String keyString = EncryptionUtil.ByteArray2String(key.getEncoded());
        return keyString;
    }

    public static PublicKey String2PublicKey(String st) throws Exception{
        byte[] keyBytes = EncryptionUtil.String2ByteArray(st);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec x509KeySpec2 = new X509EncodedKeySpec(keyBytes);
        PublicKey publicKey = keyFactory.generatePublic(x509KeySpec2);
        return publicKey;
    }


}
