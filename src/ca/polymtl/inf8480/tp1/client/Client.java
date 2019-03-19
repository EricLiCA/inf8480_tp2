package ca.polymtl.inf8480.tp1.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import ca.polymtl.inf8480.tp1.nameService.NameServiceInterface;
import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Client {

    private boolean secureMode_ = true;
    private String file_ = null;

    private NameServiceInterface nameService = null;

    public static void main(String[] args) {
        String file = null;
        boolean secureMode = true;

        if (args.length > 0) {
            file = args[0];
            if (args.length > 1) {
                secureMode = false;
            }
        }

        Client client = new Client(file, secureMode);
        client.run();
    }

    public Client(String file, boolean secureMode) {
        super();

        file_ = file;
        secureMode_ = secureMode;

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        String[] nameServiceAddressParts = loadNameService().split(":");
        String nameServiceAddress = nameServiceAddressParts[0];
        int nameServicePort = Integer.parseInt(nameServiceAddressParts[1]);

        nameService = loadNameService(nameServiceAddress, nameServicePort);
    }

    private void run() {
        List<String> servers = null;

        try {
            servers = nameService.availableServers();
        } catch (RemoteException e) {
            System.out.println(e.getMessage());
        }

        List<Node> servers_ = loadServers(servers);
        List<String> tasks = loadTasks(file_);

        if (secureMode_) {
            secureMode(servers_, tasks);
        } else {
            maliciousMode(servers_, tasks);
        }
    }

    private NameServiceInterface loadNameService(String hostname, int port) {
        NameServiceInterface stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname, port);
            stub = (NameServiceInterface) registry.lookup("nameService");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage()
                    + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erreur access: " + e.getMessage());
        }

        return stub;
    }

    private ServerInterface loadServerStub(String hostname, int port) {
        ServerInterface stub = null;

        try {
            Registry registry = LocateRegistry.getRegistry(hostname, port);
            stub = (ServerInterface) registry.lookup("server");
        } catch (NotBoundException e) {
            System.out.println("Erreur: Le nom '" + e.getMessage()
                    + "' n'est pas défini dans le registre.");
        } catch (AccessException e) {
            System.out.println("Erreur access server: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Erreur remote server: " + e.getMessage());
        }

        return stub;
    }

    private List<String> loadTasks(String file) {
        Path path = Paths.get(file);
        List<String> tasks = null;
        try {
            tasks = Files.readAllLines(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tasks;
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

    private List<Node> loadServers(List<String> servers) {
        List<Node> servers_ = new ArrayList<>(servers.size());

        for (String server : servers) {
            String[] parts = server.split(",");
            String address = parts[0];
            int capacity = Integer.parseInt(parts[1]);
            String[] addressParts = address.split(":");
            String ipAddress = addressParts[0];
            int port = Integer.parseInt(addressParts[1]);
            ServerInterface serverStub = loadServerStub(ipAddress, port);
            servers_.add(new Node(serverStub, capacity, ipAddress));
        }

        return servers_;
    }

    private void secureMode(List<Node> servers, List<String> tasks) {
        BlockingQueue<Node> servers_ = new ArrayBlockingQueue<Node>(servers.size());
        servers_.addAll(servers);

        BlockingQueue<String> tasks_ = new ArrayBlockingQueue<>(tasks.size() + 1);
        tasks_.addAll(tasks);
        tasks_.add(""); // Poison pill to stop consumers

        ExecutorService threadPool = Executors.newFixedThreadPool(servers_.size());
        List<Integer> results = Collections.synchronizedList(new ArrayList());

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < servers_.size(); i++) {
            threadPool.execute(new RMITask(servers_, tasks_, results));
        }

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

            long stopTime = System.currentTimeMillis();
            int finalResult = 0;
            for (Integer result : results) {
                finalResult = (finalResult + result) % 5000;
            }
            System.out.println(finalResult);
            System.out.println("Execution time: " + Long.toString(stopTime - startTime));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void maliciousMode(List<Node> servers, List<String> tasks) {
        int result = 0;
        int i = 0;
        int batchSize = 5;

        long startTime = System.currentTimeMillis();

        while (i < tasks.size()) {
            ExecutorService threadPool = Executors.newFixedThreadPool(servers.size());
            List<Integer> results = Collections.synchronizedList(new ArrayList<>());

            int indexesUntilEnd = tasks.size() - i;
            int subListSize = batchSize < indexesUntilEnd ? batchSize : indexesUntilEnd;
            List<String> subTasks = new ArrayList<String>(tasks.subList(i, i + subListSize));

            for (int j = 0; j < servers.size(); j++) {
                threadPool.execute(new MaliciousTask(servers.get(j), subTasks, results));
            }

            threadPool.shutdown();

            try {
                threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                int candidateResult = majorityElement(results);

                if (candidateResult == -1) {
                    System.out.println("Could not find consensus!");
                    return;
                }

                result = (result + majorityElement(results)) % 5000;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            i += batchSize;
        }

        long stopTime = System.currentTimeMillis();

        System.out.println(result);
        System.out.println("Execution time: " + Long.toString(stopTime - startTime));
    }

    private int majorityElement(List<Integer> results) {
        int candidate = results.get(0);
        int count = 1;

        for (int i = 1; i < results.size(); i++) {
            if (results.get(i) == candidate) count++;
            else count--;

            if (count < 0) {
                candidate = results.get(i);
                count = 1;
            }
        }

        int verificationCount = 0;
        for (Integer i : results) {
            if (i == candidate) verificationCount++;
        }

        return verificationCount > results.size() / 2 ? candidate : -1;
    }
}
