package demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 
 * @author gongzhihao
 *
 */
public class NIOServer {
	// 通道管理器
	private Selector selector;

	/**
	 * 获得一个ServerSocket通道，并对该通道做一些初始化的工作
	 * 
	 * @param port 绑定的端口号
	 * @throws IOException
	 */
	public void initServer(int port) throws IOException {
		// 获得一个ServerSocket通道
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		// 设置通道为非阻塞
		serverChannel.configureBlocking(false);
		// 将该通道对应的ServerSocket绑定到指定端口
		serverChannel.socket().bind(new InetSocketAddress(port));
		// 获得一个通道管理器
		this.selector = Selector.open();
		// 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件,注册该事件后，
		// 当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	/**
	 * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
	 * 
	 * @throws IOException
	 */
	public void listen() throws IOException {
		System.out.println("nio server listen start.");
		// 轮询访问selector
		while (true) {
			// 当注册的事件到达时，方法返回；否则,该方法会一直阻塞
			selector.select();
			// 获得selector中选中的项的迭代器，选中的项为注册的事件
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				// 删除已选的key,以防重复处理
				it.remove();
				// 客户端请求连接事件
				if (key.isAcceptable()) {
					ServerSocketChannel server = (ServerSocketChannel) key.channel();
					//System.out.println(server.getLocalAddress());
					// 获得和客户端连接的通道
					SocketChannel channel = server.accept();
					//System.out.println(channel.getLocalAddress());
					Socket socket = channel.socket();
					//System.out.println(socket.getInetAddress().getHostAddress() + socket.getPort() + socket.getLocalAddress() + socket.getLocalPort());
					System.out.println(socket.getInetAddress().getHostAddress() + "," + socket.getPort());
					// 设置成非阻塞
					channel.configureBlocking(false);
					// 在这里可以给客户端发送信息
					channel.write(ByteBuffer.wrap(new String("服务器收到客户端连接请求.").getBytes()));
					// 在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限。
					channel.register(selector, SelectionKey.OP_READ);
					// 获得了可读事件
				} else if (key.isReadable()) {
					read(key);
				}
			}
		}
	}

	/**
	 * 处理读取客户端发来的信息 的事件
	 * 
	 * @param key
	 * @throws IOException
	 */
	public void read(SelectionKey key) throws IOException {
		// 服务器可读取消息:得到事件发生的Socket通道
		SocketChannel channel = (SocketChannel) key.channel();
		// 创建读取的缓冲区
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		channel.read(buffer);
		byte[] data = buffer.array();
		String msg = new String(data);
		System.out.println("服务器端收到客户端信息:" + msg);
		// 将消息回送给客户端
		ByteBuffer outBuffer = ByteBuffer.wrap(new String("服务器端发送客户端消息: world.").getBytes());
		channel.write(outBuffer);
	}

	// 服务器端测试
	public static void main(String[] args) throws IOException {
		NIOServer nioServer = new NIOServer();
		nioServer.initServer(8000);
		nioServer.listen();
	}
}