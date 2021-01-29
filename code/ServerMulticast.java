import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class ServerMulticast implements Runnable {
    boolean DEBUG = false;
    String ip_multicast; //ip multicast
    int port = 7000; //crea socket sulla porta

    public ServerMulticast(String ip_multicast) {
        this.ip_multicast = ip_multicast;
    }


    /*
     * metodo che crea un gruppo multicast e si registra
     */
    @Override
    public void run() {
        try {
            InetAddress ia=InetAddress.getByName(ip_multicast);
            MulticastSocket ms = new MulticastSocket(port); //crea multicast socket sulla porta 5050
            ms.joinGroup(ia);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //sempre attivo
        while (true) {
        }

    }



}
