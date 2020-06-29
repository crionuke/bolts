package com.crionuke.bolts.example.udpechoserver;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class NetworkUtils {

    static ByteBuffer createByteBuffer(String payload) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(payload.length() + Short.BYTES);
        byteBuffer.putShort((short) payload.length());
        byteBuffer.put(payload.getBytes());
        byteBuffer.flip();
        return byteBuffer;
    }

    static String extractPayload(ByteBuffer byteBuffer) {
        int length = byteBuffer.getShort();
        byte[] bytes = new byte[length];
        byteBuffer.get(bytes);
        return new String(bytes);
    }

    static SocketAddress generateSocketAddress() {
        SocketAddress socketAddress = new InetSocketAddress("127.0.0.1",
                (int) (Math.random() * 55535) + 10000);
        return socketAddress;
    }
}
