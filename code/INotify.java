import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * INTERFACCIA LATO SERVER per RMI CALLBACKS
 */
public interface INotify extends Remote {

    /* registrazione per la callback */
    public void registerForCallback (NotifyEventInterface ClientInterface, String nickname) throws RemoteException;

    /* cancella registrazione per la callback */
    public void unregisterForCallback (NotifyEventInterface ClientInterface, String nickname) throws RemoteException;


}
