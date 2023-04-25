import java.io.Serializable;

public class Chat implements Serializable {
    public Chat(String username, String message) {
        this.username = username;
        this.message = message;
       
    }
    static final long serialVersionUID = 1L;
    private String username;
    private String message;
 
    @Override
    public String toString() {
     return String.format("%5s: %5s \n", username, message);
    }
   
    
}
