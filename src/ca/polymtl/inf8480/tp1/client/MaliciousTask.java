package ca.polymtl.inf8480.tp1.client;

import java.rmi.RemoteException;
import java.util.List;

public class MaliciousTask implements Runnable {
    private Node server_;
    private List<String> tasks_;
    private List<Integer> results_;

    private String username_;
    private String password_;

    public MaliciousTask(Node server, List<String> tasks, List<Integer> results) {
        server_ = server;
        tasks_ = tasks;
        results_ = results;

        username_ = "user";
        password_ = "motdepasse";
    }

    @Override
    public void run() {
        try {
            while (true) {
                int result = server_.server_.calculate(tasks_, username_, password_);
                if (result >= 0) {
                    results_.add(result);
                    return;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
