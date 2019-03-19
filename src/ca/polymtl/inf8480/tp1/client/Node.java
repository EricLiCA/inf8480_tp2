package ca.polymtl.inf8480.tp1.client;

import ca.polymtl.inf8480.tp1.shared.ServerInterface;

public class Node {
    public ServerInterface server_;
    public int capacity_;
    public String host_;

    public Node(ServerInterface server, int capacity, String host) {
        server_ = server;
        capacity_ = capacity;
        host_ = host;
    }
}
