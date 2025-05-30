package security;

import java.rmi.RMISecurityManager;

public class MySecurityManager extends RMISecurityManager {
    public MySecurityManager() {
        super();
    }
    
    @Override
    public void checkConnect(String host, int port) {
        // Allow all connections
    }
    
    @Override
    public void checkConnect(String host, int port, Object context) {
        // Allow all connections
    }
}
