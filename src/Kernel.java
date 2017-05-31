import Define.Const;
import P2P.Network;

import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;

/**
 * Created by Bill on 2017/5/31.
 */
public class Kernel {

    public static void main(String[] args) throws Exception{
        Network network = new Network();
        network.setDenseNetwork(Const.UserNumber);
        File file=new File("result.txt");
        BufferedWriter bf=new BufferedWriter(new PrintWriter(file));
        for (int i = 0; i < 20; i++){
            network.run();
            System.out.println("MailSented:" + network.getUser(0).mailSent);
            System.out.println("MailReceived:" + network.getUser(0).mailReceived);
        }

    }
}
