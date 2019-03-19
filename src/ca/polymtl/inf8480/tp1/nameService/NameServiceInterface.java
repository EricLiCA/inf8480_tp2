package ca.polymtl.inf8480.tp1.nameService;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface NameServiceInterface extends Remote {

    List<String> availableServers() throws RemoteException;

    boolean authenticate(String username, String password) throws RemoteException;

    boolean register(String ipAddress, int port, int q) throws RemoteException;
}
