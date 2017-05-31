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
        network.setNormalNetwork(Const.UserNumber);

        for (int i = 0; i < 200; i++){
            System.out.println("Round" + i + ":");
            network.run();
            network.getUser(1).printInfo();
        }
    }
}
