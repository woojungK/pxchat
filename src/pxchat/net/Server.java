package pxchat.net;

import java.io.IOException;

import pxchat.net.protocol.core.FrameAdapter;
import pxchat.net.protocol.core.FrameAdapterListener;
import pxchat.net.protocol.core.ServerFrameAdapter;
import pxchat.net.protocol.core.ServerFrameAdapterListener;
import pxchat.net.protocol.frames.Frame;
import pxchat.net.protocol.frames.SessionIDFrame;
import pxchat.net.protocol.frames.VersionFrame;
import pxchat.net.tcp.CustomSocket;
import pxchat.net.tcp.TCPServer;
import pxchat.net.tcp.TCPServerListener;

/**
 * This class implements the server for pxchat. It uses a TCP server to listen
 * on a specified port and handles incoming data.
 * 
 * @author Markus Döllinger
 */
public class Server {

	/**
	 * The underlying TCP server
	 */
	private TCPServer server;

	/**
	 * The frame adapter used to control the data flow
	 */
	private ServerFrameAdapter serverFrameAdapter;

	/**
	 * The TCP server listener used to process events of the underlying server
	 * socket.
	 */
	private TCPServerListener tcpServerListener = new TCPServerListener() {

		@Override
		public void clientRead(CustomSocket client, Object data) {
			serverFrameAdapter.getAdapter(client).receive(data);
		}

		@Override
		public void clientDisconnect(CustomSocket client) {
			int sessionID = serverFrameAdapter.getAdapter(client).getSessionID();
			System.out.println(this + "> Client with id " + sessionID + " disconnected.");
		}

		@Override
		public void clientConnect(CustomSocket client) {
			FrameAdapter adapter = serverFrameAdapter.getAdapter(client);
			System.out.println(this + "> new connection " + client + " --> " + adapter);
		}

		@Override
		public void clientConnecting(CustomSocket client) {
		}
	};

	/**
	 * The server frame adapter listener used to process events of the frame
	 * adapter.
	 */
	private ServerFrameAdapterListener serverFrameAdapterListener = new ServerFrameAdapterListener() {

		@Override
		public void destroyAdapter(ServerFrameAdapter serverAdapter, FrameAdapter adapter) {

		}

		@Override
		public void createAdapter(ServerFrameAdapter serverAdapter, FrameAdapter adapter) {
		}
	};

	/**
	 * The frame adapter listener used for the sockets connected to the server.
	 */
	private FrameAdapterListener frameAdapterListener = new FrameAdapterListener() {

		@Override
		public void process(FrameAdapter adapter) {
			System.out.println(this + "> executes " + adapter.getIncoming() + " from " + adapter
					.getSocket());

			for (Frame frame : adapter.getIncoming()) {

				switch (frame.getId()) {

					/*
					 * This is a dummy command that should not be used.
					 */
					case Frame.ID_NOP:
						break;

					/*
					 * The client sent a version frame. Usually this is sent
					 * immediately after the client connected to the server. If
					 * the version is compatible to the current version of this
					 * server, the client will receive a session id. If not, the
					 * connection is terminated.
					 * 
					 * TODO: If another frame is sent before the version frame
					 * arrives, or the version frame does not arrive after a
					 * specified timeout, the connection needs to be terminated.
					 * 
					 * TODO: Should the version frame include the
					 * authentication?
					 */
					case Frame.ID_VERSION:
						VersionFrame vf = (VersionFrame) frame;
						if (!vf.isCompatible(VersionFrame.getCurrent())) {
							System.out.println(this + "> Version control unsuccessful.");
							adapter.disconnect();
						} else {
							System.out
									.println(this + "> Version control successful, send sessionID");
							adapter.getOutgoing().add(new SessionIDFrame(adapter.getSessionID()));
							adapter.send();
						}
						break;
				}
			}

			// clear all processed frames
			adapter.getIncoming().clear();
		}
	};

	/**
	 * Constructs a new server.
	 */
	public Server() {
		server = new TCPServer(tcpServerListener);
		serverFrameAdapter = new ServerFrameAdapter(serverFrameAdapterListener,
				frameAdapterListener);
	}

	/**
	 * Lets the server listen on the specified port. If the server is already
	 * listening, nothing is done.
	 * 
	 * @param port The port to listen on
	 * @throws IOException if an I/O error occurs when opening the socket
	 */
	public void listen(int port) throws IOException {
		server.listen(port);
	}

	/**
	 * Closes the server and disconnects all clients.
	 */
	public void close() {
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
