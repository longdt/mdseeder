package com.solt.media.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import com.solt.libtorrent.LibTorrent;
import com.solt.libtorrent.TorrentException;
import com.solt.libtorrent.TorrentManager;
import com.solt.media.config.ConfigurationManager;

public class MdSeeder {
	private static final String TORRENT_LIST_URL = "http://sharephim.vn/download/torrents.info";
	private volatile boolean running;
	private TorrentManager torrManager;
	private Thread worker;
	private ConfigurationManager conf;
	
	public MdSeeder() {
		 torrManager = TorrentManager.getInstance();
		 worker = Thread.currentThread();
		 conf = ConfigurationManager.getInstance();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MdSeeder seeder = new MdSeeder();
		seeder.run();
		seeder.exit();
	}

	private synchronized void run() {
		if (torrManager == null) {
			return;
		}
		running = true;
		Runtime.getRuntime().addShutdownHook(new Thread("Shutdowner") {
			@Override
			public void run() {
				running = false;
				worker.interrupt();
				exit();
			}
		});
		try {
			while (running) {
				loadTorrents();
				Thread.sleep(60 * 60000);
			}
		} catch (InterruptedException e) {
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	private void loadTorrents() throws MalformedURLException, InterruptedException {
		URL url = new URL(TORRENT_LIST_URL);
		BufferedReader reader = null;
		LibTorrent libTorrent = torrManager.getLibTorrent();
		int state = 0;
		try {
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line = null;
			String hashCode = null;
			while (true) {
				if (hashCode == null) {
					line = reader.readLine();
					if (line == null) {
						break;
					}
					try {
						hashCode = torrManager.addTorrent(new URL(line));
					} catch (MalformedURLException e) {
					}
				} else {
					try {
						state = libTorrent.getTorrentState(hashCode);
						hashCode = (state == 4 || state == 5) ? null : hashCode;
					} catch (TorrentException e) {
						hashCode = null;
					}
				}

				Thread.sleep(10 * 60000);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private synchronized void exit() {
		if (torrManager == null) {
			return;
		}
		torrManager.shutdown();
		try {
			ConfigurationManager conf = ConfigurationManager.getInstance();
			conf.setStrings(ConfigurationManager.TORRENT_HASHCODES,
					torrManager.getTorrents());
			conf.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		torrManager = null;
	}

}
