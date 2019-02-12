package demo;

import java.io.IOException;

public class Task1 implements Runnable{

	@Override
	public void run() {
		//
		System.out.println("thread name:" + Thread.currentThread().getName() + ", thread id:" + Thread.currentThread().getId());
		
		NIOClient nioClient = new NIOClient();
		try {
			nioClient.initClient("127.0.0.1", 8000); //一个线程 就是一个客户端
			nioClient.listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
