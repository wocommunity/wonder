package er.extensions.foundation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;

/**
 * NSNotificationCenter that can post simple notifications to other
 * applications. Currently just posts the name, no object and the userInfo as a
 * dictionary of strings (which all together can't be more than 1k) Note: you
 * must specifically register here, not at
 * <code>NSNotificationCenter.defaultCenter()</code>.
 * 
 * @author ak
 */

// TODO subclass of NSNotification that custom-serialize itself to a sparser
// format
public abstract class ERXRemoteNotificationCenter extends NSNotificationCenter {
	private static final Logger log = Logger.getLogger(ERXRemoteNotificationCenter.class);

	private static ERXRemoteNotificationCenter _sharedInstance;

	private static class SimpleCenter extends ERXRemoteNotificationCenter {
		public static final int IDENTIFIER_LENGTH = 6;
		private static final int JOIN = 1;
		private static final int LEAVE = 2;
		private static final int POST = 3;

		private boolean _postLocal;
		private byte[] _identifier;
		private InetAddress _localBindAddress;
		private NetworkInterface _localNetworkInterface;
		private InetSocketAddress _multicastGroup;
		private int _multicastPort;
		private MulticastSocket _multicastSocket;
		private boolean _listening;
		private int _maxReceivePacketSize;

		protected SimpleCenter() throws IOException {
			init();
		}

		protected void init() throws UnknownHostException, SocketException, IOException {
			String localBindAddressStr = ERXProperties.stringForKey("er.extensions.ERXRemoteNotificationsCenter.localBindAddress");
			if (localBindAddressStr == null) {
				_localBindAddress = WOApplication.application().hostAddress();
			}
			else {
				_localBindAddress = InetAddress.getByName(localBindAddressStr);
			}

			String multicastGroup = ERXProperties.stringForKeyWithDefault("er.extensions.ERXRemoteNotificationsCenter.group", "230.0.0.1");
			_multicastPort = ERXProperties.intForKeyWithDefault("er.extensions.ERXRemoteNotificationsCenter.port", 9754);
			int maxPacketSize = ERXProperties.intForKeyWithDefault("er.extensions.ERXRemoteNotificationsCenter.maxPacketSize", 1024);
			_maxReceivePacketSize = 2 * maxPacketSize;

			String multicastIdentifierStr = ERXProperties.stringForKey("er.extensions.ERXRemoteNotificationsCenter.identifier");
			if (multicastIdentifierStr == null) {
				_identifier = new byte[IDENTIFIER_LENGTH];
				byte[] hostAddressBytes = _localBindAddress.getAddress();
				System.arraycopy(hostAddressBytes, 0, _identifier, 0, hostAddressBytes.length);
				int multicastInstance = WOApplication.application().port().shortValue();
				_identifier[4] = (byte) (multicastInstance & 0xff);
				_identifier[5] = (byte) ((multicastInstance >>> 8) & 0xff);
			}
			else {
				_identifier = ERXStringUtilities.hexStringToByteArray(multicastIdentifierStr);
			}

			_localNetworkInterface = NetworkInterface.getByInetAddress(_localBindAddress);
			_multicastGroup = new InetSocketAddress(InetAddress.getByName(multicastGroup), _multicastPort);
			_multicastSocket = new MulticastSocket(null);
			_multicastSocket.setInterface(_localBindAddress);
			_multicastSocket.setTimeToLive(4);
			_multicastSocket.setReuseAddress(true);
			_multicastSocket.bind(new InetSocketAddress(_multicastPort));
			listen();
			join();
		}

		public void join() throws IOException {
			if (log.isInfoEnabled()) {
				log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(_identifier) + " joining.");
			}
			_multicastSocket.joinGroup(_multicastGroup, _localNetworkInterface);
			MulticastByteArrayOutputStream baos = new MulticastByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.write(_identifier);
			dos.writeByte(JOIN);
			dos.flush();
			_multicastSocket.send(baos.createDatagramPacket());
		}

		public void leave() throws IOException {
			if (log.isInfoEnabled()) {
				log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(_identifier) + " leaving.");
			}
			MulticastByteArrayOutputStream baos = new MulticastByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.write(_identifier);
			dos.writeByte(LEAVE);
			dos.flush();
			_multicastSocket.send(baos.createDatagramPacket());
			_multicastSocket.leaveGroup(_multicastGroup, _localNetworkInterface);
			_listening = false;
		}

		protected void postRemoteNotification(NSNotification notification) {
			try {
				MulticastByteArrayOutputStream baos = new MulticastByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(baos);
				dos.write(_identifier);

				dos.writeByte(POST);

				writeNotification(notification, dos);
				_multicastSocket.send(baos.createDatagramPacket());
				if (log.isDebugEnabled()) {
					log.info("Multicast instance " + ERXStringUtilities.byteArrayToHexString(_identifier) + ": Writing " + notification);
				}
				dos.close();
				if (_postLocal) {
					postLocalNotification(notification);
				}
			}
			catch (Exception e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}

		public void listen() throws IOException {
			Thread listenThread = new Thread(new Runnable() {
				public void run() {
					_listening = true;
					byte[] buffer = new byte[_maxReceivePacketSize];
					while (_listening) {
						DatagramPacket receivePacket = new DatagramPacket(buffer, 0, buffer.length);
						try {
							_multicastSocket.receive(receivePacket);
							handlePacket(receivePacket);

						}
						catch (Throwable t) {
							log.error("Failed to read multicast notification.", t);
						}
					}
				}

				private void handlePacket(DatagramPacket receivePacket) throws IOException {
					ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData(), 0, receivePacket.getLength());
					DataInputStream dis = new DataInputStream(bais);

					byte[] identifier = new byte[IDENTIFIER_LENGTH];
					dis.readFully(identifier);

					byte code = dis.readByte();
					if (code == JOIN) {
						if (log.isDebugEnabled()) {
							log.info("Received JOIN");
						}
					}
					else if (code == LEAVE) {
						if (log.isDebugEnabled()) {
							log.info("Received LEAVE");
						}
					}
					else if (code == POST) {
						String self = ERXStringUtilities.byteArrayToHexString(_identifier);
						String remote = ERXStringUtilities.byteArrayToHexString(identifier);

						if (self.equals(remote)) {
							if (log.isDebugEnabled()) {
								log.info("Received POST from self");
							}
						}
						else {
							if (log.isDebugEnabled()) {
								log.info("Received POST from " + ERXStringUtilities.byteArrayToHexString(identifier));
							}
							NSNotification notification = readNotification(dis);
							if (log.isDebugEnabled()) {
								log.debug("Received notification: " + notification);
							}
							else if (log.isInfoEnabled()) {
								log.info("Received " + notification.name() + " notification from " + remote);
							}
							postLocalNotification(notification);
						}
					}
				}
			});
			listenThread.start();
		}

		protected class MulticastByteArrayOutputStream extends ByteArrayOutputStream {
			public byte[] buffer() {
				return buf;
			}

			public DatagramPacket createDatagramPacket() throws SocketException {
				return new DatagramPacket(buf, 0, count, _multicastGroup);
			}
		}

		private NSNotification readNotification(DataInputStream dis) throws IOException {
			short nameLen = dis.readShort();
			byte[] nameBytes = new byte[nameLen];
			dis.readFully(nameBytes);

			short objectLen = dis.readShort();
			byte[] objectBytes = new byte[objectLen];
			dis.readFully(objectBytes);

			short userInfoLen = dis.readShort();
			NSMutableDictionary userInfo = new NSMutableDictionary();
			for (int i = 0; i < userInfoLen; i++) {
				short keyLen = dis.readShort();
				byte[] keyBytes = new byte[keyLen];
				dis.readFully(keyBytes);

				short valueLen = dis.readShort();
				byte[] valueBytes = new byte[valueLen];
				dis.readFully(valueBytes);

				userInfo.setObjectForKey(new String(valueBytes), new String(keyBytes));

			}

			NSNotification notification = new NSNotification(new String(nameBytes), null, userInfo);
			return notification;
		}

		private void writeNotification(NSNotification notification, DataOutputStream dos) throws IOException {
			byte[] name = notification.name().getBytes();
			dos.writeShort(name.length);
			dos.write(name);

			byte[] object = new byte[0];
			dos.writeShort(object.length);
			dos.write(object);

			NSDictionary userInfo = notification.userInfo();
			if (userInfo == null) {
				userInfo = NSDictionary.EmptyDictionary;
			}

			dos.writeShort(userInfo.count());
			for (Object key : userInfo.allKeys()) {
				byte[] keyBytes = key.toString().getBytes();
				byte[] valueBytes = userInfo.objectForKey(key).toString().getBytes();
				dos.writeShort(keyBytes.length);
				dos.write(keyBytes);
				dos.writeShort(valueBytes.length);
				dos.write(valueBytes);
			}

			dos.flush();
			if (dos.size() > _maxReceivePacketSize) {
				throw new IllegalArgumentException("More than " + _maxReceivePacketSize + " bytes");
			}
		}
	}

	public static ERXRemoteNotificationCenter defaultCenter() {
		if (_sharedInstance == null) {
			synchronized (ERXRemoteNotificationCenter.class) {
				if (_sharedInstance == null) {
					try {
						_sharedInstance = new SimpleCenter();
					}
					catch (IOException e) {
						throw NSForwardException._runtimeExceptionForThrowable(e);
					}
				}
			}
		}
		return _sharedInstance;
	}

	public static void setDefaultCenter(ERXRemoteNotificationCenter center) {
		_sharedInstance = center;
	}

	public void postLocalNotification(NSNotification notification) {
		super.postNotification(notification);
	}

	protected abstract void postRemoteNotification(NSNotification notification);

	public void postNotification(NSNotification notification) {
		try {
			postRemoteNotification(notification);
		}
		catch (Exception e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}
}
