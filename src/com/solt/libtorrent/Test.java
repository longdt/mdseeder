package com.solt.libtorrent;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.solt.stream.NanoHTTPD;

public class Test {
	private static volatile boolean shutdown = false;
	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws TorrentException 
	 */
	public static void main(String[] args) throws InterruptedException,
			IOException, TorrentException {
		System.out.println(Charset.defaultCharset());
		final LibTorrent libTorrent = new LibTorrent();
		
		libTorrent.setSession(0, "D:\\.mediacache", 100 * 1024, 0 * 1024);
		libTorrent.setSessionOptions(true, true, true, true);
		final NanoHTTPD httpd = new NanoHTTPD(18008, new File("./"), libTorrent);
		String torrentFile = "magnet:?xt=urn:btih:55E4841CE7A176DC4E0888E172EFC053314A4A69&xl=2160066560&dn=Safe.2012.720p.BRRip.x264.AC3-JYK&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A80&tr=udp%3A%2F%2Ftracker.publicbt.com%3A80&tr=udp%3A%2F%2Ftracker.ccc.de%3A80&tr=udp%3A%2F%2Ftracker.istole.it%3A80&tr=http%3A%2F%2Ftracker.torrentbay.to%3A6969%2Fannounce&tr=http%3A%2F%2Ftracker.istole.it%3A6969%2Fannounce&tr=http%3A%2F%2Ftracker.publicbt.com%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%2Fannounce&tr=http%3A%2F%2Ftracker.torrent.to%3A2710%2Fannounce&tr=http%3A%2F%2Finferno.demonoid.com%3A3402%2Fannounce&tr=http%3A%2F%2Ffr33dom.h33t.com%3A3310%2Fannounce&tr=http%3A%2F%2Fexodus.desync.com%3A6969%2Fannounce";
//		String torrentFile = "Tranformers.2007.720p.Soha.mp4.torrent";
		if (args.length > 0 && !args[0].trim().isEmpty()) {
			torrentFile = args[0].trim();
		}
		final Map<String, String> torrents = new HashMap<>();
		File dir = new File(".");
		for (String f : dir.list()) {
			if (f.endsWith(".torrent")) {
				torrents.put(libTorrent.addAsyncTorrent(f, 0), f);
			}
		}
		final String hashCode = libTorrent.addAsyncMagnetUri(torrentFile, 0, LibTorrent.DEFAULT_FLAGS);
		torrents.put(hashCode, torrentFile);
//		libTorrent.setAutoManaged(hashCode, false);
		libTorrent.resumeTorrent(hashCode);
		libTorrent.setUploadMode(hashCode, false);
//		libTorrent.setTorrentDownloadLimit(hashCode, 1024 * 1024);
		String mediaUrl = "http://localhost:18008/"
				+ URLEncoder.encode(hashCode, "UTF-8");
		System.out.println(mediaUrl);
		
		int state = libTorrent.getTorrentState(hashCode);
		while (state == 7 || state == 1) {
			libTorrent.handleAlerts();
			System.out.println("state:" + state + " size: " + libTorrent.getTorrentProgressSize(hashCode, 1));
			Thread.sleep(1000);
			state = libTorrent.getTorrentState(hashCode);
			
		}

		System.out.println("state:" + state);
		libTorrent.resumeTorrent(hashCode);
		Thread shutdowner = new Thread("shutdowner") {
			@Override
			public void run() {
				try {
					int c = 0;
					while ((c = System.in.read()) != 'q') {
						if (c == 's') {
							System.out.println(libTorrent.getSessionStatusText() + "\n" + libTorrent.getTorrentStatusText(hashCode));
							libTorrent.saveResumeData();
						} else if (c == 't') {
							for (Entry<String, String> entry : torrents.entrySet()) {
								String t = entry.getKey();
								System.out.println(libTorrent.getTorrentProgress(t) + "\t" + t + "\t" + libTorrent.getTorrentState(t) + "\t" + libTorrent.getTorrentDownloadRate(t, true) + "\t" + libTorrent.getTorrentName(t) + "\t" + entry.getValue());
							}
						} else if (c == 'p') {
							libTorrent.pauseTorrent(hashCode);
							System.out.println("paused");
						} else if (c == 'o') {
							libTorrent.resumeTorrent(hashCode);
							System.out.println("resumed");
						}
					}
					shutdown = true;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TorrentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		shutdowner.setDaemon(true);
		shutdowner.start();
		PieceInfoComparator comparator = new PieceInfoComparator();
		int counter = 0;
		while (!shutdown) {
			System.out.println(" size: " + libTorrent.getTorrentProgressSize(hashCode, 0) + " speed: " + libTorrent.getTorrentDownloadRate(hashCode, true));
			PartialPieceInfo[] infos = libTorrent.getPieceDownloadQueue(hashCode);
			if (infos != null) {
				Arrays.sort(infos, comparator);
				for (int i = 0; i < infos.length; ++i) {
					System.out.println("piece[" + infos[i].getPieceIdx() + "]: "
						+ infos[i].getPieceState() + progressPiece(infos[i]));
				}
				System.out.println();
			}
			Thread.sleep(1000);
			libTorrent.handleAlerts();
			++counter;
		}
		System.out.println(libTorrent.getTorrentStatusText(hashCode));
		System.out.println("start remove torrent");
		httpd.shutdown();
		libTorrent.removeTorrent(hashCode, false);
		System.out.println("removed");
		libTorrent.pauseSession();
		libTorrent.abortSession();
	}

	private static String progressPiece(PartialPieceInfo info) {
		StringBuilder builder = new StringBuilder();
		int[] blocks = info.getBlocks();
		int totalBytes = 0;
		for (int i = 0; i < info.getNumBlocks(); ++i) {
			totalBytes += blocks[i * 4 + 1];
			int state = blocks[i * 4];
			if (state == 3) {
				builder.append('#');
			} else if (state == 2) {
				builder.append('=');
			} else if (state == 1) {
				builder.append('+');
			} else if (state == 0) {
				builder.append('_');
			} else {
				builder.append(' ');
			}
		}
		builder.append('\t').append(totalBytes);
		return builder.toString();
	}

}

class PieceInfoComparator implements Comparator<PartialPieceInfo> {

	@Override
	public int compare(PartialPieceInfo o1, PartialPieceInfo o2) {
		if (o1.getPieceIdx() < o2.getPieceIdx()) {
			return -1;
		} else if (o1.getPieceIdx() > o2.getPieceIdx()) {
			return 1;
		} else {
			return 0;
		}
	}
	
}