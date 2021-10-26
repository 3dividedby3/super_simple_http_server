package simple.http.business.exc;

public class SuperSimpleException extends RuntimeException {

    private static final long serialVersionUID = -7435737119639831509L;

    public SuperSimpleException(Throwable t) {
        super(t);
    }
    
    public SuperSimpleException(String message) {
        super(message);
    }
    
    public SuperSimpleException(String message, Throwable t) {
        super(message, t);
    }
}
