package org.joshi.network;

import java.io.Serializable;

/**
 * This class represents a message that is sent across the network by the client and the server.
 */
public abstract class Message implements Serializable {

    public abstract String getType();
}
