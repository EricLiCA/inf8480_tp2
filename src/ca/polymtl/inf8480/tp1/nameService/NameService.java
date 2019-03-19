package ca.polymtl.inf8480.tp1.nameService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class NameService implements NameServiceInterface {

    public static void main(String[] args) {
        String ipAddress = args[0];
        int port = Integer.parseInt(args[1]);

        NameService nameService = new NameService();
        nameService.run(ipAddress, port);
    }

    public NameService() {
        servers_ = new ArrayList<>();
        username_ = "user";
        password_ = "motdepasse";
    }

    private void run(String ipAddress, int port) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            NameServiceInterface stub = (NameServiceInterface) UnicastRemoteObject.exportObject(this, port + 1);

            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("nameService", stub);

            writeConfig(ipAddress, port);
            System.out.println("NameService ready");
        } catch (ConnectException e) {
            System.err
                    .println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    @Override
    public List<String> availableServers() throws RemoteException {
        System.out.println("Requested servers");
        ArrayList<String> servers = new ArrayList<>();
        for (Server server : servers_) {
            servers.add(server.ipAddress_ + ":" + server.port_ + "," + server.q_);
        }
        return servers;
    }

    @Override
    public boolean authenticate(String username, String password) throws RemoteException {
        return username_.equals(username) && password_.equals(password);
    }

    @Override
    public boolean register(String ipAddress, int port, int q) throws RemoteException {
        Server server = new Server(ipAddress, port, q);
        servers_.add(server);
        System.out.println("Registered " + ipAddress + " with capacity " + q);
        return true;
    }

    private void writeConfig(String ipAddress, int port) throws IOException {
        Path path = Paths.get("nameService.config");
        List<String> config = new ArrayList<>();
        config.add(ipAddress + ":" + port);
        Files.write(path, config);
    }

    class Server {
        public Server(String ipAddress, int port, int q) {
            ipAddress_ = ipAddress;
            port_ = port;
            q_ = q;
        }

        public String ipAddress_;
        public int port_;
        public int q_;
    }

    private List<Server> servers_;
    private String username_;
    private String password_;
}
