import java.io.Serializable;

/**
 * simple Vote object that returns a string of the value selected in client's
 * voteMenu, to be passed between server/clients
 */
public class Vote implements Serializable {
    public Vote(String voteValue) {
        this.voteValue = voteValue;
    }

    String voteValue;
    static final long serialVersionUID = 1L;

    public String getVoteValue() {
        return voteValue;
    }

    public void setVoteValue(String voteValue) {
        this.voteValue = voteValue;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}
