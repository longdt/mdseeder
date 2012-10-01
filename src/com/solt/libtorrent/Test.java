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
		
		libTorrent.setSession(0, "./", 100 * 1024, 0 * 1024);
		libTorrent.setSessionOptions(true, true, true, true);
		final NanoHTTPD httpd = new NanoHTTPD(18008, new File("./"), libTorrent);
		String torrentFile = "Tranformers.2007.720p.Soha.mp4.torrent";
		if (args.length > 0 && !args[0].trim().isEmpty()) {
			torrentFile = args[0].trim();
		}
		final Map<String, String> torrents = new HashMap<>();
		File dir = new File(".");
		for (String f : dir.list()) {
			if (f.endsWith(".torrent")) {
				torrents.put(libTorrent.addTorrent(f, 0), f);
			}
		}
		final String hashCode = libTorrent.addTorrent(torrentFile, 0);
		
//		libTorrent.setTorrentDownloadLimit(hashCode, 1024 * 1024);
		String mediaUrl = "http://localhost:18008/"
				+ URLEncoder.encode(hashCode, "UTF-8");
		System.out.println(mediaUrl);
		
		int state = libTorrent.getTorrentState(hashCode);
		while (state == 7 || state == 1) {
			System.out.println("state:" + state + " size: " + libTorrent.getTorrentProgressSize(hashCode, 1));
			Thread.sleep(1000);
			state = libTorrent.getTorrentState(hashCode);
		}

		System.out.println("state:" + state);

		Thread shutdowner = new Thread("shutdowner") {
			@Override
			public void run() {
				try {
					int c = 0;
					while ((c = System.in.read()) != 'q') {
						if (c == 's') {
							System.out.println(libTorrent.getSessionStatusText() + "\n" + libTorrent.getTorrentStatusText(hashCode));
						} else if (c == 't') {
							for (Entry<String, String> entry : torrents.entrySet()) {
								String t = entry.getKey();
								System.out.println(libTorrent.getTorrentProgress(t) + "\t" + t + "\t" + libTorrent.getTorrentState(t) + "\t" + libTorrent.getTorrentDownloadRate(t, true) + "\t" + libTorrent.getTorrentName(t) + "\t" + entry.getValue());
							}
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
			++counter;
			if (counter == 600) {
				libTorrent.saveResumeData();
				counter = 0;
			}
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