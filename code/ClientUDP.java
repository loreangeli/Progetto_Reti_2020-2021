import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * CLASSE LATO CLIENT che gestisce l'invio e la ricezione dei messaggi della chat di progetto
 */

public class ClientUDP {
    private String ip_multicast; //ip multicast del gruppo
    private ConcurrentHashMap<String, Messaggi> msglist;
    boolean DEBUG = false;

    public ClientUDP(String ip_multicast, ConcurrentHashMap<String, Messaggi> msglist) {
        this.ip_multicast = ip_multicast;
        this.msglist = msglist;
    }

    /*
     * invia msg al gruppo multicast, il messaggio da inviare è formato da tre campi:
     * nomeprogetto ,nickname e messaggio.
     * arg1=projectname, arg2=nickname, arg3=messaggio vero e proprio
     * projectname serve al destinatario del pacchetto per sapere in quale chat di progetto deve essere inserito il messaggio
     * il nickname serve al ricevente del pacchetto per sapere chi è stato ad inviare il messaggio
     */
    public void MsgSendChat(String msg1, String nickname, String msg3) {
        DatagramSocket ms=null;
        try {
            InetAddress ia=InetAddress.getByName(ip_multicast);
            String messaggio_by_user = nickname+" ha detto: "+msg3; //messaggio vero e proprio
            String msg_send = msg1+" "+nickname+" "+messaggio_by_user;
            byte [] data = new byte [msg_send.length()+1];
            data = msg_send.getBytes();
            int port = 7000;
            DatagramPacket dp = new DatagramPacket(data,data.length,ia,port);
            /* genero porta */
            int i;
            for (i=1024;i<65000;i++) {
                try {
                    ms = new DatagramSocket(i);
                    break; //esco dal for
                }
                catch (BindException e) {

                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }

            if (DEBUG) System.out.println("messaggio inviato: "+msg_send);
            ms.send(dp);
            System.out.println("ok");
            ms.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
     * metodo per leggere tutti i messaggi pendenti (quelli non letti) della chat di progetto
     * msg: nameproject
     * nameproject serve per sapere da quale chat di progetto leggere
     * NOTA BENE: se l'utente non fa parte di quella chat di progetto non potrà leggere i messaggi
     */
    public void MsgReadChat(String nameproject) {
        Messaggi tmp = msglist.get(nameproject);
        if (tmp!=null) {
            ConcurrentLinkedQueue<String> listamsg= new ConcurrentLinkedQueue<String> (tmp.getMsglist());
            //stampa lista messaggi
            for (String msg : listamsg)
                System.out.println(msg);

            //cancella messaggi letti dalla struttura
            msglist.remove(nameproject);
        }
        else System.out.println("nessun messaggio presente");
    }


}
