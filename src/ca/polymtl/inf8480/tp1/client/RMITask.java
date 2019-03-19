package ca.polymtl.inf8480.tp1.client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;


public class RMITask implements Runnable {
    private BlockingQueue<Node> servers_;
    private BlockingQueue<String> tasks_;
    private List<Integer> results_;

    private String username_;
    private String password_;

    public RMITask(BlockingQueue<Node> servers, BlockingQueue<String> tasks, List<Integer> results) {
        servers_ = servers;
        tasks_ = tasks;
        results_ = results;

        username_ = "user";
        password_ = "motdepasse";
    }

    @Override
    public void run() {
        while (true) {
            Node server = null;
            try {
                server = servers_.take();

                List<String> tasks = retrieveTasks(server.capacity_);

                if (tasks.size() == 0) return;

                try {
                    int result = server.server_.calculate(tasks, username_, password_);
                    if (result >= 0) {
                        results_.add(result);
                    } else { // calculation refused
                        tasks_.addAll(tasks);
                    }

                    servers_.add(server);
                } catch (RemoteException e) {
                    // server failed, add tasks back to queue
                    tasks_.addAll(tasks);
                    // don't add server back to queue
                }
            } catch (InterruptedException e) {
                if (server != null) servers_.add(server);
                e.printStackTrace();
            }
        }
    }

    private List<String> retrieveTasks(int capacity) throws InterruptedException {
        List<String> tasks = new ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            String task = tasks_.take();
            if (task.equals("")) {
                tasks_.add(""); // Add poison pill back for other threads
            } else {
                tasks.add(task);
            }
        }

        return tasks;
    }
}
