import java.io.Serializable;

public class Utente implements Serializable {
    private static final long serialVersionUID = -103370079703346050L;
    private String username;
    private String password;


    //RICORDA: Ci vuole il costruttore default senza argomenti, altrimenti errore per serializzazione.
    public Utente () {
    }

    public Utente(String username, String password) {
        if (username==null) {
            System.out.println("username non inserito correttamente");
            return;
        }
        if(password==null) {
            System.out.println("password non inserita correttamente");
            return;
        }

        this.password=password;
        this.username=username;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void print() {
        System.out.println("user: "+username+", passw: "+password);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj==null) return false;
        if( !(obj instanceof Utente)) return false;

        Utente o = (Utente) obj;
        if (this.username==o.username && this.password==o.password)
            return true;

        return false;
    }


}
