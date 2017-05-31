package P2P;

import Define.Const;
import message.Message;
import message.encryptionutil.EncryptionUtil;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

/**
 * Created by Bill on 2017/5/30.
 */
public class User {
    Map<Integer,Integer> sended = new HashMap<>();
    Map<Integer,PublicKey> publicKeyList = new HashMap<>();
    Set<User> neighbours = new HashSet<>();
    Integer ID;
    PrivateKey privateKey,systemKey;
    LinkedList<Message> mailList = new LinkedList<>();
    Set<Message> recieved = new HashSet<>();
    LinkedList<Integer> recentForward = new LinkedList<>();
    public User(){
        ID = Const.PowerUser;
        KeyPair keys = EncryptionUtil.generate();
        privateKey = keys.getPrivate();
        publicKeyList.put(ID,keys.getPublic());
    }

    private class Mail{
        String content;
        int userID;
        public Mail(String content,int userID){
            this.content = content;
            this.userID = userID;
        }
    }

    private LinkedList<Mail> mailBox = new LinkedList<>();

    public User(int ID,PrivateKey systemKey) throws Exception{
        this.ID = ID;
        this.systemKey = systemKey;
        KeyPair keys = EncryptionUtil.generate();
        privateKey = keys.getPrivate();
        publicKeyList.put(ID,keys.getPublic());
    }

    public void addNeighbour(User user){
        neighbours.add(user);
    }

    void action() throws Exception{
        checkList();
        readMail();
        if (Math.random() < 0.2){
            sendMessage();
        }
        if (Math.random() < 0.1){
            requestUpdate();
        }
    }

    int error(){
        return Const.Normal;
    }

    void forward(Message message){
        for (User user : neighbours) {
            user.recive(message);
        }
    }

    void recive(Message message){
        if (!recieved.contains(message)){
            mailList.add(message);
            recieved.add(message);
        }
    }

    void sendTo(int target,String content) throws Exception{
        Message message = new Message();
        message.Encrypt(ID,content,privateKey,publicKeyList.get(target));
        forward(message);
    }

    void sendMessage() throws Exception{
        Random random = new Random();
        Integer number = random.nextInt();
        Integer target = random.nextInt(Const.UserNumber);
        String content="send:" + number.toString();
        sendTo(target,content);
        sended.put(number,target);
    }

    private boolean update_flag = false;
    void sychronize() throws Exception{
        if (!update_flag){
            requestUpdate();
            update_flag = true;
        } else {
            checkList();
            readMail();
        }
    }

    void replyMessage(int target,String content) throws Exception{
        content = "reply:" + content;
        sendTo(target,content);
    }

    void requestUpdate() throws Exception{
        KeyPair keys = EncryptionUtil.generate();

        String content = "update:"
                + Message.PublicKey2String(keys.getPublic());

        sendTo(Const.PowerUser,content);
        privateKey = keys.getPrivate();
    }

    void sendPublicKey(int userID) throws Exception{
        KeyPair keys = EncryptionUtil.generate();

        String content = "update:"
                + Message.PublicKey2String(keys.getPublic());

        sendTo(userID,content);
    }

    boolean verifyReply(int content,int target){
        if (sended.containsKey(target) && sended.get(target).equals(content)) {
            sended.remove(target);
            return true;
        }
        return false;
    }

    boolean permit(int userID){
        int count = 0;
        for (Integer ID : recentForward){
            if (ID.equals(userID)) ++count;
        }
        return  (count <= Const.maxForward);
    }

    void checkList() throws Exception{
        for (Message message : mailList){
            if (message.Decrypt(privateKey) || message.Decrypt(systemKey)) {
                PublicKey publicKey = publicKeyList.get(message.getSender());
                if (message.ValidateCipher(publicKey)) {
                    mailBox.add(new Mail(message.getContent(), message.getSender()));
                }
            }
            PublicKey publicKey = publicKeyList.get(message.getSender());
            if (message.Validate(publicKey) && permit(message.getSender())){
                forward(message);
                recentForward.add(message.getSender());
                if (recentForward.size() > Const.recentSize)
                    recentForward.removeFirst();
            }
        }
    }

    boolean readMail() throws Exception{
        if (!mailBox.isEmpty()){
            for (Mail mail : mailBox){
                if (mail.content.startsWith("send:")){
                    String content = mail.content.substring(5);
                    replyMessage(mail.userID,content);
                } else if (mail.content.startsWith("reply:")){
                    String content = mail.content.substring(6);
                    if (!verifyReply(Integer.parseInt(content),mail.userID)) return false;
                } else if (mail.content.startsWith("update:")){
                    String content = mail.content.substring(7);
                    PublicKey publicKey = Message.String2PublicKey(content);
                    if (mail.userID != Const.PowerUser)
                        publicKeyList.put(mail.userID,publicKey);
                } else if (mail.content.equals("Sychronize.")){
                    sendPublicKey(mail.userID);
                }
            }
            mailBox.clear();
        }
        return true;
    }
}
