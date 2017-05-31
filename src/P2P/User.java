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
    public int mailSent = 0, mailBack = 0 , mailReceived = 0, mailForward = 0, mailBlock = 0;
    PrivateKey oldPrivateKey,privateKey,systemKey;
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

    public User(int ID,PrivateKey systemKey, PublicKey publicKey) throws Exception{
        this.ID = ID;
        this.systemKey = systemKey;
        KeyPair keys = EncryptionUtil.generate();
        oldPrivateKey = privateKey = keys.getPrivate();
        publicKeyList.put(ID,keys.getPublic());
        publicKeyList.put(Const.PowerUser,publicKey);
        keyStack.add(ID);
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
        if (Math.random() < 0.01){
            requestUpdate();
        }
    }

    int error(){
        //TODO
        return Const.Normal;
    }

    void forward(Message message){
        for (User user : neighbours) {
            user.recive(message);
        }
    }

    public void printInfo(){
        System.out.println("MailSented:" + mailSent);
        System.out.println("MailReceived:" + mailReceived);
        System.out.println("MailBack:" + mailBack);
        System.out.println("MailForward:" + mailForward);
        System.out.println("MailBlock:" + mailBlock);
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
        mailSent++;
        Random random = new Random();
        Integer number = random.nextInt();
        Integer target = random.nextInt(Const.UserNumber);
        if (target == ID) return;
        String content="send:" + number.toString();
        sendTo(target,content);
        sended.put(number,target);
    }

    void sendMessage(Integer target) throws Exception{
        mailSent++;
        Random random = new Random();
        Integer number = random.nextInt();
        String content="send:" + number.toString();
        sendTo(target,content);
        sended.put(number,target);
    }

    private LinkedList<Integer> keyStack = new LinkedList<>();
    void synchronize() throws Exception{
        for (Integer aim : keyStack){
            for (User user : neighbours) {
                if (!user.publicKeyList.containsKey(aim)){
                    user.publicKeyList.put(aim,publicKeyList.get(aim));
                    user.keyStack.add(aim);
                }
            }
        }
        keyStack.clear();
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
        oldPrivateKey = privateKey;
        privateKey = keys.getPrivate();
    }

    void sendPublicKey(int userID) throws Exception{
        KeyPair keys = EncryptionUtil.generate();

        String content = "update:"
                + Message.PublicKey2String(keys.getPublic());

        sendTo(userID,content);
    }

    boolean verifyReply(int content,int target){
        if (sended.containsKey(content) && sended.get(content).equals(target)) {
            sended.remove(content);
            return true;
        }
        return false;
    }

    boolean permit(int userID){
        int count = 0;
        for (Integer ID : recentForward){
            if (ID.equals(userID)) ++count;
        }
        if (count > Const.maxForward){
            mailBlock++;
        }
        return  (count <= Const.maxForward);
    }

    void checkList() {
        for (Message message : mailList){
            mailForward += mailList.size();
            if (message.Decrypt(privateKey) || message.Decrypt(systemKey) || message.Decrypt(oldPrivateKey)) {
                PublicKey publicKey = publicKeyList.get(message.getSender());
                try {
                    if (message.ValidateCipher(publicKey)) {
                        mailBox.add(new Mail(message.getContent(), message.getSender()));
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            PublicKey publicKey = publicKeyList.get(message.getSender());
            try {
                if (message.Validate(publicKey) && permit(message.getSender())){
                    forward(message);
                    recentForward.add(message.getSender());
                    if (recentForward.size() > Const.recentSize)
                        recentForward.removeFirst();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        mailList.clear();
    }

    boolean readMail() throws Exception{
        if (!mailBox.isEmpty()){
            mailReceived += mailBox.size();
            for (Mail mail : mailBox){
                if (mail.content.startsWith("send:")){
                    String content = mail.content.substring(5);
                    replyMessage(mail.userID,content);
                } else if (mail.content.startsWith("reply:")){
                    String content = mail.content.substring(6);
                    if (!verifyReply(Integer.parseInt(content),mail.userID)) return false;
                    else {
                        mailBack++;
                    }
                } else if (mail.content.startsWith("update:")){
                    String content = mail.content.substring(7);
                    PublicKey publicKey = Message.String2PublicKey(content);
                    if (mail.userID != Const.PowerUser)
                        publicKeyList.put(mail.userID,publicKey);
                }
            }
            mailBox.clear();
        }

        return true;
    }
}
