package P2P;

import Define.Const;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Bill on 2017/5/30.
 */
public class Network {
    LinkedList<User> userList;
    public void setSparseNetwork(int num) throws Exception{

    }
    public void setDenseNetwork(int num) throws Exception{
        User powerUser = new User();

        // add Node
        for (int i = 0; i < num; i++){
            userList.add(new User(i,powerUser.privateKey));
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
                user.sychronize();
            }
        }
    }
    public void setNormalNetwork(int num) throws Exception{

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

}
