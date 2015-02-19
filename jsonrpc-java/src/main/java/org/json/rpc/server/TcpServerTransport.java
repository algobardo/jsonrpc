/*
 * Copyright (C) 2011 ritwik.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.json.rpc.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TcpServerTransport implements JsonRpcServerTransport {

    private static final int BUFF_LENGTH = 1024;

    private Socket clientSocket;

    public TcpServerTransport(int port){
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to create the server");
        }

    }

    public String readRequest() throws Exception {
        InputStream in = clientSocket.getInputStream();

        
        byte[] reqLenBytes = new byte[4];
        if (in.read(reqLenBytes) != 4) throw new RuntimeException("Unable to read the request");

        int reqLen = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(reqLenBytes).getInt(0);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buff = new byte[BUFF_LENGTH];
        int n;
        int remaining = reqLen;
        while (remaining > 0 && (n = in.read(buff, 0, Math.min(remaining, buff.length))) > 0) {
            remaining -= n;
            bos.write(buff, 0, n);
        }

        return bos.toString();
    }

    public void writeResponse(String responseData) throws Exception {

        byte[] data = responseData.getBytes();

        byte[] lenBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data.length).array();

        OutputStream out = clientSocket.getOutputStream();
        out.write(lenBytes);
        out.write(data);
        out.flush();
    }
}
