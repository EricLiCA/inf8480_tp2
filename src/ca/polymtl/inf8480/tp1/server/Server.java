package ca.polymtl.inf8480.tp1.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import ca.polymtl.inf8480.tp1.nameService.NameServiceInterface;
import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Server implements ServerInterface {

    public static void main(String[] args) {
        int q = 5;
        int m = 0;
        String ipAddress = "127.0.0.1";
        int port = 5003;

        if (args.length > 0) {
            ipAddress = args[0];
            port = Integer.parseInt(args[1]);
            if (args.length > 2) {
                q = Integer.parseInt(args[2]);
                if (args.length > 3) {
                    m = Integer.parseInt(args[3]);
                }
            }
        }

        Server server = new Server(q, m);
        server.run(ipAddress, port);
    }

    private NameServiceInterface nameService = null;

    public Server(int q, int m) {
        super();

        q_ = q;
        m_ = m;

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        String[] nameServiceAddressParts = loadNameService().split(":");
        String nameServiceAddress = nameServiceAddressParts[0];
        int nameServicePort = Integer.parseInt(nameServiceAddressParts[1]);

        nameService = loadNameService(nameServiceAddress, nameServicePort);
    }

    private void run(String ipAddress, int port) {
        try {
            ServerInterface stub = (ServerInterface) UnicastRemoteObject
                    .exportObject(this, port + 1);

            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("server", stub);
            System.out.println("Server ready.");
        } catch (ConnectException e) {
            System.err
                    .println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
            System.err.println();
            System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }

        try {
            nameService.register(ipAddress, port, q_);
            System.out.println("Registered");
        } catch (RemoteException e) {
            System.out.println(e.getMessage());
        }
    }

    private NameServiceInterface loadNameService(String hostname, int port) {
        NameServiceInterface stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname, port);
            stub = (NameServiceInterface) registry.lookup("nameService");

        } catch (NotBoundException e) {
            System.out.println("Not Bound error: Le nom '" + e.getMessage()
                    + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Access error: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Remote error: " + e.getMessage());
        }

        return stub;
    }

    /*
     * Méthode accessible par RMI.
     */
    @Override
    public int calculate(List<String> tasks, String username, String password) throws RemoteException {
        if (!accept(tasks.size()) || !nameService.authenticate(username, password)) return -1;

        int result = 0;

        for (String task : tasks) {
            String[] parts = task.split(" ");
            String operation = parts[0];
            String operand = parts[1];

            if (operation.equals("pell")) {
                result = (result + Operations.pell(Integer.parseInt(operand)) % 5000) % 5000;
            } else if (operation.equals("prime")) {
                result = (result + Operations.prime(Integer.parseInt(operand)) % 5000) % 5000;
            }
        }

        result = maliciousness(result);

        return result;
    }

    private boolean accept(int tasks) {
        if (tasks <= q_) return true;
        double refusalRate = 100 * ((double) tasks - q_) / (5 * q_);
        double random = Math.random() * 100;
        return random >= refusalRate;
    }

    private int maliciousness(int result) {
        if (m_ == 0) return result;
        int random = (int) Math.random() * 100;
        if (m_ > random) return (int) Math.random() * 5000;
        else return result;
    }

    private static String loadNameService() {
        Path path = Paths.get("nameService.config");
        List<String> tasks = null;
        try {
            tasks = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tasks.get(0);
    }

    private int q_;
    private int m_;
}
