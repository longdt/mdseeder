package com.solt.libtorrent;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.solt.libtorrent.policy.CachePolicy;
import com.solt.libtorrent.policy.CachePolicyFactory;
import com.solt.media.config.ConfigurationManager;
import com.solt.media.stream.HttpHandler;
import com.solt.media.stream.NanoHTTPD;
import com.solt.media.util.Constants;
import com.solt.media.util.FileUtils;
import com.solt.media.util.SystemProperties;

public class TorrentManager {
	private static final Logger logger = Logger.getLogger(TorrentManager.class);
	private static final int HTTPD_PORT = 18989;
	private static final Boolean TORRENT_FILE = true;
	private static final Boolean MAGNET_FILE = false;
	private String currentStream;
	private LibTorrent libTorrent;
	private NanoHTTPD httpd;
	private LinkedHashMap<String, Boolean> torrents;
	private BiMap<URL, String> torLinks;
	private File torrentsDir;
	private CachePolicy policy;
	private static TorrentManager instance;
	private boolean processAlerts;
	private Thread alertsService;
	private File root;

	private TorrentManager(int port, String wwwRoot) throws IOException {
		torrentsDir = SystemProperties.getTorrentsDir();
		torrents = new LinkedHashMap<String, Boolean>(16, 0.75f, true);
		torLinks = HashBiMap.<URL, String>create();
		root = new File(wwwRoot);
		httpd = new NanoHTTPD(HTTPD_PORT, root);
		libTorrent = new LibTorrent();
		ConfigurationManager conf = ConfigurationManager.getInstance();
		int upload = conf.getInt(ConfigurationManager.SESSION_UPLOAD_LIMIT, 200 * 1024);
		int download = conf.getInt(ConfigurationManager.SESSION_DOWNLOAD_LIMIT, 100 * 1024);
		libTorrent.setSession(port, root, upload, download);
		libTorrent.setSessionOptions(true, true, true, true);
		loadAsyncExistTorrents();
		httpd.setLibTorrent(libTorrent);
	}

	/**
	 * 
	 */
	private void loadAsyncExistTorrents() {
		String magnet = null;
		int flags = LibTorrent.FLAG_AUTO_MANAGED;
		Collection<String> hashCodes = ConfigurationManager.getInstance().getStringCollection(ConfigurationManager.TORRENT_HASHCODES);
		for (String hashCode : hashCodes) {
			File torrent = new File(torrentsDir, hashCode + Constants.TORRENT_FILE_EXTENSION);
			if (torrent.isFile()) {
				hashCode = libTorrent.addAsyncTorrent(torrent.getAbsolutePath(), 0,
						flags);
				if (hashCode != null) {
					torrents.put(hashCode, TORRENT_FILE);
				}
			} else {
				torrent = new File(torrentsDir, hashCode + Constants.MAGNET_FILE_EXTENSION);
				if (torrent.isFile()) {
					magnet = FileUtils.getStringContent(torrent);
					hashCode = libTorrent.addAsyncMagnetUri(magnet, 0, flags);
					if (hashCode != null) {
						torrents.put(hashCode, MAGNET_FILE);
					}
				}
			}
		}
	}
	
	public LibTorrent getLibTorrent() {
		return libTorrent;
	}

	public synchronized static TorrentManager getInstance() {
		if (instance == null) {
			ConfigurationManager conf = ConfigurationManager.getInstance();
			int port = conf.getInt(ConfigurationManager.TORRENT_LISTEN_PORT, 0);
			String wwwRoot = conf
					.get(ConfigurationManager.TORRENT_DOWNLOAD_DIR);
			try {
				instance = new TorrentManager(port, wwwRoot);
				instance.policy = CachePolicyFactory.getDefaultCachePolicy();
				instance.startAlertsProcessService(1000);
			} catch (IOException e) {
			}
		}
		return instance;
	}
	
	public synchronized void startAlertsProcessService(final long duration) {
		if (processAlerts) {
			return;
		}
		alertsService = new Thread("AlertsProcService") {
			@Override
			public void run() {
				try {
					while (true) {
						libTorrent.handleAlerts();
						Thread.sleep(duration);
					}
				} catch (InterruptedException e) {
				}
			}
		};
		alertsService.start();
		processAlerts = true;
	}
	
	public synchronized void stopAlertsProcessService() {
		if (!processAlerts) {
			return;
		}
		alertsService.interrupt();
		try {
			alertsService.join();
			processAlerts = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void initStream(String hashCode) {
//		try {
////			libTorrent.setAutoManaged(hashCode, false);
////			libTorrent.setUploadMode(hashCode, false);
//			libTorrent.setShareMode(hashCode, false);
//			libTorrent.resumeTorrent(hashCode);
//			if (currentStream == null) {
//				currentStream = hashCode;
//			} else if (!hashCode.equals(currentStream)) {
////				libTorrent.setUploadMode(currentStream, true);
//				libTorrent.setShareMode(currentStream, true);
//				currentStream = hashCode;
//			}
//		} catch (TorrentException e) {
//			e.printStackTrace();
//		}
	}
	

	public synchronized String addTorrent(File torrentFile) {
		String hashCode = libTorrent.addTorrent(
				torrentFile.getAbsolutePath(), 0, LibTorrent.FLAG_AUTO_MANAGED);
		if (hashCode != null) {
			initStream(hashCode);
			Boolean existFile = torrents.put(hashCode, TORRENT_FILE);
			if (existFile == null) {
				FileUtils.copyFile(torrentFile, new File(torrentsDir,
						hashCode + Constants.TORRENT_FILE_EXTENSION));
				policy.prepare(torrentFile.getAbsolutePath());
			} else if (!existFile) {
				FileUtils.copyFile(torrentFile, new File(torrentsDir,
						hashCode + Constants.TORRENT_FILE_EXTENSION));
			}
			return (hashCode);
		}
		return null;
	}

	public synchronized String addTorrent(URL url) {
		String hashCode = torLinks.get(url);
		if (hashCode != null) {
			initStream(hashCode);
			return hashCode;
		}
		File torrentFile = new File(torrentsDir, ".temp");
		try {
			FileUtils.copyFile(url.openStream(), torrentFile);
			hashCode = libTorrent.addTorrent(
					torrentFile.getAbsolutePath(), 0, LibTorrent.FLAG_AUTO_MANAGED);
			if (hashCode != null) {
				initStream(hashCode);
				torLinks.put(url, hashCode);
				Boolean existFile = torrents.put(hashCode, TORRENT_FILE);
				if (existFile == null) {
					torrentFile.renameTo(new File(torrentsDir, hashCode + Constants.TORRENT_FILE_EXTENSION));
					policy.prepare(torrentFile.getAbsolutePath());
				} else if (!existFile) {
					torrentFile.renameTo(new File(torrentsDir, hashCode + Constants.TORRENT_FILE_EXTENSION));
				}
				return (hashCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public synchronized String addTorrent(URI magnetUri) {
		String hashCode = libTorrent.addMagnetUri(
				magnetUri.toString(), 0, LibTorrent.FLAG_AUTO_MANAGED);
		if (hashCode != null) {
			initStream(hashCode);
			Boolean existFile = torrents.put(hashCode, MAGNET_FILE);
			if (existFile == null) {
				//TODO save magnet link
				FileUtils.writeFile(new File(torrentsDir,
						hashCode + Constants.MAGNET_FILE_EXTENSION), magnetUri.toString());
				policy.prepare(hashCode);
			}
			return (hashCode);
		}
		return null;
	}
	
	public int getTorrentState(String hashCode) throws TorrentException {
		return libTorrent.getTorrentState(hashCode);
	}
	
	public int getTorrentDownloadRate(String hashCode) throws TorrentException {
		return libTorrent.getTorrentDownloadRate(hashCode, true);
	}
	
	public String getMediaUrl(String hashCode) {
		if (contains(hashCode)) {
			return "http://127.0.0.1:" + HTTPD_PORT + HttpHandler.ACTION_STREAM
					+ "?" + HttpHandler.PARAM_HASHCODE + "=" + hashCode;
		}
		return null;
	}
	
	public String getCurrentStream() {
		return currentStream;
	}

	public Set<String> getTorrents() {
		return torrents.keySet();
	}
	
	public synchronized boolean contains(String hashCode) {
		return torrents.containsKey(hashCode);
	}
	
	public void cancelStream() {
		httpd.cancelStream();
		try {
			if (currentStream != null) {
//				libTorrent.setUploadMode(currentStream, true);
				libTorrent.setShareMode(currentStream, true);
			}
		} catch (TorrentException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		long start = System.nanoTime();
		if (currentStream != null) {
			try {
//				libTorrent.setUploadMode(currentStream, true);
				libTorrent.setShareMode(currentStream, true);
			} catch (TorrentException e) {
				e.printStackTrace();
			}
		}
		httpd.shutdown();
		stopAlertsProcessService();
		libTorrent.abortSession();
		System.out.println(System.nanoTime() - start);
	}

	public synchronized void removeTorrent(String hashCode)
			throws TorrentException {
		if (libTorrent.removeTorrent(hashCode, true)) {
			Boolean existFile = torrents.remove(hashCode);
			torLinks.inverse().remove(hashCode);
			if (existFile == null) {
				logger.error("error occur when remove torrent: no torrent in manager");
			} else {
				String extention = existFile ? Constants.TORRENT_FILE_EXTENSION : Constants.MAGNET_FILE_EXTENSION;
				(new File(torrentsDir, hashCode + extention)).delete();
			}
		}
	}

	/**
	 * asynchronize request add torrent
	 * @param string
	 * @throws MalformedURLException 
	 */
	public static void requestAddTorrent(String movieId, boolean file, boolean sub) throws MalformedURLException {
		Socket socket = null;
		try {
			socket = new Socket("127.0.0.1", HTTPD_PORT);
			PrintWriter writer = new PrintWriter(socket.getOutputStream());
			writer.println("GET " + HttpHandler.ACTION_ADD + "?" + HttpHandler.PARAM_MOVIEID + "=" + movieId + "&" + HttpHandler.PARAM_FILE +"=" + file + "&" + HttpHandler.PARAM_SUB + "=" + sub + " HTTP/1.0");
			writer.println();
			writer.close();
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
