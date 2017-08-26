package gzhu.cjwddz.hellovoicehelper.connects;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

import static java.lang.Thread.sleep;

/**
 * Created by cjwddz on 2017/5/16.
 */

public abstract class LinkPcService {
    public boolean hasLinkPc =false;
    Socket socket;
    int TCP_PORT =1230;
    String TCP_IP ="10.0.2.2";
    int UDP_PORT=4422;
    Thread t;
    public void OnStart(){
        Runnable r=new Runnable() {
            @Override
            public void run() {
                //通过模拟器端口连接
                tcpService();
                //通过udp监听tcp端口切换请求
                BroadCastUdp();
            }
        };
        t=new Thread(r);
        t.start();
    }
    /**
     * udp搜索可用服务端程序
     * 搜索到服务端程序后获取IP，进入TCP服务
     */
    void BroadCastUdp(){
        DatagramSocket udpSocket=null;
        try {
            udpSocket = new DatagramSocket(UDP_PORT);
            hasLinkPc =false;
        } catch (Exception e) {
            System.out.println(e.toString());
            linkFailed();
        }
        udpScanning();
        byte[] rec=new byte[128];
        while(true){
            DatagramPacket dp=new DatagramPacket(rec,rec.length);
            try {
                udpSocket.receive(dp);
                String r=dp.getAddress().getHostAddress();
                udpfindDevice(r);
                TCP_IP =r;
                udpSocket.close();
                tcpService();
                break;
            } catch(Exception e) {
                try {
                    sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * TCP服务
     */
    void tcpService(){
        if(socket==null ||socket.isClosed()){
            try {
                socket=new Socket(TCP_IP, TCP_PORT);
                socket.setKeepAlive(true);
                hasLinkPc =true;
                linkSuccess();
                t=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        rec();
                    }
                });
                t.start();
            } catch (IOException e) {
                hasLinkPc =false;
                linkFailed();
            }
        }
    }
    void rec(){
        try {
            InputStream in=socket.getInputStream();
            while(true){
                int i=in.available();
                if(i<=0){
                    sleep(100);
                    continue;
                }
                byte[] buf=new byte[i];
                in.read(buf);
                recData(buf);
            }
        } catch (IOException e) {
            e.printStackTrace();
            hasLinkPc =false;
            linkLost();
        } catch (InterruptedException e) {
            hasLinkPc =false;
            linkLost();
        }
    }

    public boolean Send(@NonNull byte[] data){
        if(socket==null ||socket.isClosed())
            return false;
        try {
            OutputStream out=socket.getOutputStream();
            out.write(data);
            out.flush();
            Thread.sleep(200);
            return true;
        } catch (IOException e) {
            hasLinkPc =false;
            linkLost();
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

    public abstract void udpScanning();
    public abstract void udpfindDevice(String ip);
    public abstract void linkLost();
    public abstract void linkFailed();
    public abstract void linkSuccess();
    public abstract void recData(byte[] data);
}
