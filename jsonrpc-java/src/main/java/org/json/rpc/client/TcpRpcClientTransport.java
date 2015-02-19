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

package org.json.rpc.client;

import org.json.rpc.commons.JsonRpcClientException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class TcpRpcClientTransport implements JsonRpcClientTransport {

    private URL url;
    private InputStream in;
    private OutputStream out;

    public TcpRpcClientTransport(URL url) {
        this.url = url;
        try {
            Socket kkSocket = new Socket(url.getHost(),url.getPort());
            in = kkSocket.getInputStream();
            out = kkSocket.getOutputStream();
        }
        catch(Exception e) {
            throw new RuntimeException("Error connecting to the server, check permission and server status");
        }

    }

    public String call(String data)
            throws IOException {


        byte[] outData = data.getBytes();
        byte[] dataLen = ByteBuffer.allocate(4).putInt(outData.length).order(ByteOrder.LITTLE_ENDIAN).array();

        out.write(dataLen);
        out.write(outData);
        out.flush();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] resLenBytes = new byte[4];
        if(in.read(resLenBytes) != 4) throw new RuntimeException("Error with the remote request");

        int resLen = ByteBuffer.allocate(4).put(resLenBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

        in = new BufferedInputStream(in);
        byte[] buff = new byte[1024];
        int n;
        int remaining = resLen;
        while (remaining > 0 && ((n = in.read(buff,0,Math.min(remaining, buff.length))) > 0) ) {
            bos.write(buff, 0, n);
            remaining-=n;
        }
        bos.flush();
        bos.close();

        return bos.toString();
    }
}
