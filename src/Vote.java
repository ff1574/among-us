import java.io.Serializable;

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
