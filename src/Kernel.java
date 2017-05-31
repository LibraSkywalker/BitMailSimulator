import Define.Const;
import P2P.Network;

/**
 * Created by Bill on 2017/5/31.
 */
public class Kernel {

    public static void main(String[] args) throws Exception{
        Network network = new Network();
        network.setDenseNetwork(Const.UserNumber);
        for (int i = 0; i < 100; i++){
            network.run();
        }

    }
}
