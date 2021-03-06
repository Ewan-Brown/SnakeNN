package com.nanovash.snakenn.neuralnetwork;

import com.nanovash.snakenn.neuralnetwork.util.Connection;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Neuron {

    private @Getter List<Connection> connections = new ArrayList<>();
    private @Getter @Setter double value = 0;
    private @NonNull @Getter @Setter double threshold;

    /**
     * Passes on the neuron's value to its connected neurons if the value is bigger than the threshold
     */
    public void passValues() {
        if(value > threshold)
            for (Connection conn : connections)
                conn.getReceiver().setValue(conn.getReceiver().getValue() + value * conn.getWeight());
    }

    public void addConnection(Connection c) {
        connections.add(c);
    }
}
