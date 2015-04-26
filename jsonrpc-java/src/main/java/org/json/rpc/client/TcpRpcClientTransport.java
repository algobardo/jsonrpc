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

    public TcpRpcClientTransport(URL url) {
        this.url = url;
    }

    public String call(String data) throws IOException {
        Socket socket = new Socket(url.getHost(),url.getPort());

        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        byte[] outData = data.getBytes();
        byte[] dataLen = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(outData.length).array();

        out.write(dataLen);
        out.write(outData);
        out.flush();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] resLenBytes = new byte[4];
        int n;
        int lenRemaining = resLenBytes.length;
        int i = 0;
        while(lenRemaining > 0 && ((n = in.read(resLenBytes, i, lenRemaining)) > 0)) {
            i += n;
            lenRemaining -= n;
        }
        if(lenRemaining != 0) throw new RuntimeException("Error with the remote request");
        
        int resLen = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(resLenBytes).getInt(0);

        in = new BufferedInputStream(in);
        byte[] buff = new byte[1024];
        
        int remaining = resLen;
        while (remaining > 0 && ((n = in.read(buff,0,Math.min(remaining, buff.length))) > 0) ) {
            bos.write(buff, 0, n);
            remaining-=n;
        }
        bos.flush();
        bos.close();

        try {
            in.close();
        } catch (Exception e) {
        }

        try {
            out.close();
        } catch (Exception e) {
        }

        return bos.toString();
    }
}
