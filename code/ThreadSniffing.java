import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/*
 * CLASSE LATO CLIENT che crea un Task che intercetta i messaggi ricevuti sul gruppo multicast di ip
 * "ip_multicast" filtrandoli e salvandoli nella struttura "msglist".
 */

public class ThreadSniffing implements Runnable {
    private ConcurrentHashMap<String, Messaggi> msglist;
    private boolean DEBUG = true;
    private String nickname;
    private String ip_multicast; //indirizzo ip del gruppo multicast a cui collegarsi
    private int port; //porta dove creare la MulticastSocket

    /*
     * costruttore
     */
    public ThreadSniffing(ConcurrentHashMap<String, Messaggi> msglist, String ip_multicast, int port) {
        this.msglist = msglist;
        nickname = null;
        this.ip_multicast = ip_multicast;
        this.port = port;
    }


    public String getNickname() {
        return nickname;
    }


    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


    /*
     * Questo metodo crea una MulticastSocket sulla porta "port" e si aggiunge nel gruppo di multicast di ip "ip_multicast".
     * A questo punto intercetta i pacchetti che arrivano su questo gruppo e li filtra salvando i messaggi nella coda della lista della classe "Messaggi"
     * associata alla chiave del progetto utilizzando la struttura dati "msglist".
     * RICORDA: vengono salvati tutti i messaggi, anche quelli che sono diretti a chat di progetto di cui non faccio parte. Non vengono salvati però
     * i messaggi che sono stati inviati da me.
     */
    @Override
    public void run() {
        MulticastSocket ms = null;
        InetAddress group = null;
        try {
            //creo un MulticastSocket e mi aggiungo al gruppo di multicast
            ms = new MulticastSocket(port);
            group = InetAddress.getByName(ip_multicast);
            ms.joinGroup(group);

            byte [] buffer = new byte[1024];
            while (true) {
                DatagramPacket dp=new DatagramPacket(buffer,buffer.length);
                ms.receive(dp);

                /* inserisci messaggio nella struttura dati. RICORDA: il msg estratto dal pacchetto è di questo tipo: projectname nickname messaggio */
                //ottieni messaggio dal pacchetto
                byte[] realData = Arrays.copyOf( dp.getData(), dp.getLength() );
                String msg_receive = new String(realData); //messaggio estratto dal pacchetto
                String ar[] = msg_receive.split(" ");

                String messaggio = new String(); //messaggio da scrivere in chat
                for (int i=2;i<ar.length;i++)
                    if (i==ar.length-1) messaggio = messaggio + ar[i];
                    else messaggio = messaggio + ar[i] +" ";

                if (DEBUG) System.out.println("[thread sniffing] messaggio ricevuto: "+messaggio);

                /* salva messaggio nella struttura dati "msglist" */
                if (nickname!=null) {
                    if (!nickname.contentEquals(ar[1])) { //il msg non lo scrivo se sono stato io a inviarlo
                        if (msglist.containsKey(ar[0])) { //progetto esistente
                            Messaggi tmp = msglist.get(ar[0]);
                            tmp.add_msg(messaggio);
                            msglist.remove(ar[0]);
                            msglist.put(ar[0], tmp);
                        }
                        else { //progetto non esistente, va creato
                            Messaggi tmp = new Messaggi(ar[0]);
                            tmp.add_msg(messaggio);
                            msglist.put(ar[0], tmp);
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}