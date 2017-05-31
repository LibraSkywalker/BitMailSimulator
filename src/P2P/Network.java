package P2P;

import Define.Const;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Bill on 2017/5/30.
 */
public class Network {
    LinkedList<User> userList = new LinkedList<>();
    private static boolean[] visited;

    private void dfs(int id, int num) throws Exception {
        for (int i = 0; i < num; i++)
            if (userList.get(id).neighbours.contains(userList.get(i)) && !visited[i]) {
                visited[i] = true;
                dfs(i, num);
            }
    }
    public void setSparseNetwork(int num) throws Exception{
        User powerUser = new User();

        //add Node
        for (int i = 0; i < num; i++) {
            userList.add(new User(i,powerUser.privateKey,powerUser.publicKeyList.get(Const.PowerUser)));
        }

        //add Edge
        for (int i = 0; i < num; i++){
            Random random = new Random();
            for (int j = 0; j < 4; j++){
                int k = random.nextInt(num);
                if (k != i) {
                    userList.get(i).addNeighbour(userList.get(k));
                    userList.get(k).addNeighbour(userList.get(i));
                }
            }
        }
        visited = new boolean[num];
        for (int i = 0; i < num; i++)
            visited[i] = false;
        int last = -1;
        for (int i = 0; i < num; i++)
            if (!visited[i]) {
                visited[i] = true;
                dfs(i, num);
                if (last != -1) {
                    userList.get(i).addNeighbour(userList.get(last));
                    userList.get(last).addNeighbour(userList.get(i));
                }
                last = i;
            }

        for (int i = 0; i < num + 10; i++){
            for (User user : userList){
                user.synchronize();
            }
        }

    }
    public void setDenseNetwork(int num) throws Exception{
        User powerUser = new User();

        // add Node
        for (int i = 0; i < num; i++){
            userList.add(new User(i,powerUser.privateKey,powerUser.publicKeyList.get(Const.PowerUser)));
        }

        // add Edge
        for (int i = 0; i < num; i++){
            Random random = new Random();
            for (int j = 0; j < num / 3 * 2; j++){
                int k = random.nextInt(num);
                if (k != i)
                    userList.get(i).addNeighbour(userList.get(k));
            }
        }

        // sychronzie public keys
        for (int i = 0; i < num + 10; i++){
            for (User user : userList){
                user.synchronize();
            }
        }
    }
    public void setNormalNetwork(int num) throws Exception{
        User powerUser = new User();

        //add Node
        for (int i = 0; i < num; i++) {
            userList.add(new User(i,powerUser.privateKey,powerUser.publicKeyList.get(Const.PowerUser)));
        }

        //add Edge
        for (int i = 0; i < num; i++){
            Random random = new Random();
            for (int j = 0; j < num / 4; j++){
                int k = random.nextInt(num);
                if (k != i) {
                    userList.get(i).addNeighbour(userList.get(k));
                    userList.get(k).addNeighbour(userList.get(i));
                }
            }
        }
        visited = new boolean[num];
        for (int i = 0; i < num; i++)
            visited[i] = false;
        int last = -1;
        for (int i = 0; i < num; i++)
            if (!visited[i]) {
                visited[i] = true;
                dfs(i, num);
                if (last != -1) {
                    userList.get(i).addNeighbour(userList.get(last));
                    userList.get(last).addNeighbour(userList.get(i));
                }
                last = i;
            }

        for (int i = 0; i < num + 10; i++){
            for (User user : userList){
                user.synchronize();
            }
        }
    }

    public User getUser(int i){
        return userList.get(i);
    }
    public int run() throws Exception{
        int flag = Const.Normal;
        for (User user : userList){
            user.action();
        }
        for (User user : userList){
            flag = user.error();
            if (flag != Const.Normal)
                return flag;
        }
        return flag;
    }

    public void test() throws Exception {
        User observed = userList.get(0);
        for (User user : userList) {
            if (!user.ID.equals(0))
                user.sendMessage(0);
        }
        for (int i = 1; i < 10; i++){
            observed.sendMessage(i);
        }
        for (int i = 0; i < 10; i++) {
            for (User user : userList) {
                user.checkList();
                user.readMail();
            }
            System.out.println("MailSented:" + observed.mailSent);
            System.out.println("MailReceived:" + observed.mailReceived);
            System.out.println("MailBack:" + observed.mailBack);
        }
    }
}
