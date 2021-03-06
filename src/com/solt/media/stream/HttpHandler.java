package com.solt.media.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.solt.libtorrent.FileEntry;
import com.solt.libtorrent.LibTorrent;
import com.solt.libtorrent.PartialPieceInfo;
import com.solt.libtorrent.PieceInfoComparator;
import com.solt.libtorrent.TorrentException;
import com.solt.libtorrent.TorrentManager;
import com.solt.media.util.Constants;
import com.solt.media.util.FileUtils;
import com.solt.media.util.StringUtils;
import com.solt.media.util.SystemProperties;

public class HttpHandler implements Runnable{
	public static final String ACTION_STREAM = "/stream";
	
	public static final String ACTION_VIEW = "/view";

	public static final String ACTION_ADD = "/add";
	
	public static final String ACTION_DEL = "/del";
	
	public static final String ACTION_SHUTDOWN = "/shutdown";

	public static final String PARAM_HASHCODE = "hashcode";
	
	public static final String PARAM_MOVIEID = "movieId";

	public static final String PARAM_FILE = "file";
	
	public static final String PARAM_SUB = "sub";


	private static final Logger logger = Logger.getLogger(HttpHandler.class);
	private static final PieceInfoComparator pieceComparator = new PieceInfoComparator();

	private File rootDir;
	private LibTorrent libTorrent;
	private NanoHTTPD httpd;
	private Socket mySocket;
	private volatile boolean streaming;
	

	/**
	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
	 */
	private static Map<String, String> theMimeTypes = new HashMap<String, String>();
	static {
		StringTokenizer st = new StringTokenizer("css		text/css "
				+ "htm		text/html " + "html		text/html " + "xml		text/xml "
				+ "txt		text/plain " + "asc		text/plain " + "gif		image/gif "
				+ "jpg		image/jpeg " + "jpeg		image/jpeg " + "png		image/png "
				+ "mp3		audio/mpeg " + "m3u		audio/mpeg-url "
				+ "mp4		video/mp4 " + "avi		video/avi " + "ogv		video/ogg "
				+ "flv		video/x-flv " + "wmv		video/x-ms-wmv "
				+ "divx		video/divx "
				+ "mov		video/quicktime " + "asf		video/x-ms-asf "
				+ "swf		application/x-shockwave-flash "
				+ "js			application/javascript " + "pdf		application/pdf "
				+ "doc		application/msword " + "ogg		application/x-ogg "
				+ "zip		application/octet-stream "
				+ "exe		application/octet-stream "
				+ "class		application/octet-stream ");
		while (st.hasMoreTokens())
			theMimeTypes.put(st.nextToken(), st.nextToken());
	}
	
	public HttpHandler(Socket s, NanoHTTPD httpd) {
		mySocket = s;
		streaming = true;
		this.httpd = httpd;
		libTorrent = httpd.getLibTorrent();
		this.rootDir = httpd.getRootDir();
	}

	public void run() {
		try {
			HttpRequest request = parseRequest(mySocket.getInputStream());
			if (request != null) {
				serveRequest(request);
			}
		} catch (IOException ioe) {
			try {
				sendMessage(
						HttpStatus.HTTP_INTERNALERROR,
						"SERVER INTERNAL ERROR: IOException: "
								+ ioe.getMessage());
			} catch (Throwable t) {
			}
		} catch (URISyntaxException e) {
			try {
				sendMessage(HttpStatus.HTTP_BADREQUEST, e.getMessage());
			} catch (InterruptedException e1) {
			}
		}
		catch (InterruptedException ie) {
			// Thrown by sendError, ignore and exit the thread.
		} finally {
			stop();
			httpd.removeHandler(this);
		}
	}

	private void serveRequest(HttpRequest request)
			throws InterruptedException, IOException, URISyntaxException {
		String uri = request.getUri();
		String hashCode = request.getParam(PARAM_HASHCODE);
		if (uri.equals(ACTION_VIEW)) {
			String view = hashCode != null? getTorrentInfo(hashCode) : listTorrents();
			sendMessage(HttpStatus.HTTP_OK, NanoHTTPD.MIME_HTML, view);
		} else {
			sendMessage(HttpStatus.HTTP_BADREQUEST, "invalid uri");
		}
	}

	private String getTorrentInfo(String hashCode) {
		StringBuilder info = new StringBuilder();
		info.append("<html><head><meta http-equiv='refresh' content='1' ></head><body><table>");
		info.append("<tr><td>index<td>state<td>progress<td>status bar\n");
		try {
			PartialPieceInfo[] pieces = libTorrent.getPieceDownloadQueue(hashCode);
			if (pieces != null) {
				Arrays.sort(pieces, pieceComparator);
				for (int i = 0; i < pieces.length; ++i) {
					info.append("<tr><td>").append(pieces[i].getPieceIdx())
						.append("<td>").append(pieces[i].getPieceState())
						.append("<td>").append(pieces[i].getDownloadedBytes())
						.append("<td>").append(StringUtils.progressPiece(pieces[i]))
						.append("\n");
				}
			}
		} catch (TorrentException e) {
		}
		info.append("</table></body></html>");
		return info.toString();
	}

	private String listTorrents() {
		StringBuilder info = new StringBuilder();
		Set<String> torrents = TorrentManager.getInstance().getTorrents();
		info.append("<html><head><meta http-equiv='refresh' content='1' ></head><body><table>");
		info.append("<tr><td>hashcode<td>state<td>progress<td>downloaded<td>download rate<td>name<td>upload mode<td>share mode<td>auto manage\n");
		try {
			for (String hashCode : torrents) {
				info.append("<tr><td>").append(hashCode)
						.append("<td>").append(libTorrent.getTorrentState(hashCode))
						.append("<td>").append(libTorrent.getTorrentProgress(hashCode))
						.append("<td>").append(libTorrent.getTorrentProgressSize(hashCode, 0))
						.append("<td>").append(libTorrent.getTorrentDownloadRate(hashCode, true))
						.append("<td>").append(libTorrent.getTorrentName(hashCode))
						.append("<td>").append(libTorrent.isUploadMode(hashCode))
						.append("<td>").append(libTorrent.isShareMode(hashCode))
						.append("<td>").append(libTorrent.isAutoManaged(hashCode))
						.append('\n');
			}
		} catch (TorrentException e) {

		}
		info.append("</table></body></html>");
		return info.toString();
	}

	/**
	 * Decodes the sent headers and loads the data into java Properties' key
	 * - value pairs
	 **/
	private HttpRequest parseRequest(InputStream is)
			throws InterruptedException {
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(is));
			// Read the request line
			String inLine = in.readLine();
			if (inLine == null)
				return null;
			StringTokenizer st = new StringTokenizer(inLine);
			if (!st.hasMoreTokens())
				sendMessage(HttpStatus.HTTP_BADREQUEST,
						"BAD REQUEST: Syntax error. Usage: GET /example/file.html");

			String methodString = st.nextToken();
			int method = 0;
			if (methodString.equalsIgnoreCase("GET")) {
				method = HttpRequest.METHOD_GET;
			} else if (methodString.equalsIgnoreCase("HEAD")) {
				method = HttpRequest.METHOD_HEAD;
			} else {
				sendMessage(HttpStatus.HTTP_NOTIMPLEMENTED,
						"SERVER DOES NOT IMPLEMENTS THIS METHOD ");
			}

			if (!st.hasMoreTokens())
				sendMessage(HttpStatus.HTTP_BADREQUEST,
						"BAD REQUEST: Missing URI. Usage: GET /example/file.html");

			String uri = st.nextToken();
			HttpRequest request = new HttpRequest();
			request.setMethod(method);
			// Decode parameters from the URI
			int qmi = uri.indexOf('?');
			if (qmi >= 0) {
				decodeParms(uri.substring(qmi + 1), request.getParams());
				uri = decodePercent(uri.substring(0, qmi));
			} else
				uri = decodePercent(uri);
			request.setUri(uri);
			// If there's another token, it's protocol version,
			// followed by HTTP headers. Ignore version but parse headers.
			// NOTE: this now forces header names lowercase since they are
			// case insensitive and vary by client.
			if (st.hasMoreTokens()) {
				String line = in.readLine();
				while (line != null && line.trim().length() > 0) {
					int p = line.indexOf(':');
					if (p >= 0)
						request.setHeader(line.substring(0, p).trim()
								.toLowerCase(), line.substring(p + 1)
								.trim());
					line = in.readLine();
				}
			}
			return request;
		} catch (IOException ioe) {
			sendMessage(
					HttpStatus.HTTP_INTERNALERROR,
					"SERVER INTERNAL ERROR: IOException: "
							+ ioe.getMessage());
		}
		return null;
	}

	/**
	 * Decodes the percent encoding scheme. <br/>
	 * For example: "an+example%20string" -> "an example string"
	 */
	private String decodePercent(String str) throws InterruptedException {
		try {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				switch (c) {
				case '+':
					sb.append(' ');
					break;
				case '%':
					sb.append((char) Integer.parseInt(
							str.substring(i + 1, i + 3), 16));
					i += 2;
					break;
				default:
					sb.append(c);
					break;
				}
			}
			return sb.toString();
		} catch (Exception e) {
			sendMessage(HttpStatus.HTTP_BADREQUEST,
					"BAD REQUEST: Bad percent-encoding.");
			return null;
		}
	}

	/**
	 * Decodes parameters in percent-encoded URI-format ( e.g.
	 * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
	 * Properties. NOTE: this doesn't support multiple identical keys due to
	 * the simplicity of Properties -- if you need multiples, you might want
	 * to replace the Properties with a Hashtable of Vectors or such.
	 */
	private void decodeParms(String parms, Map<String, String> map)
			throws InterruptedException {
		if (parms == null)
			return;

		StringTokenizer st = new StringTokenizer(parms, "&");
		while (st.hasMoreTokens()) {
			String e = st.nextToken();
			int sep = e.indexOf('=');
			if (sep >= 0)
				map.put(decodePercent(e.substring(0, sep)).trim(),
						decodePercent(e.substring(sep + 1)));
		}
	}

	/**
	 * Returns an error message as a HTTP response and throws
	 * InterruptedException to stop further request processing.
	 */
	private void sendMessage(String status, String msg)
			throws InterruptedException {
		sendMessage(status, NanoHTTPD.MIME_PLAINTEXT, msg);
	}
	private void sendMessage(String status, String mimeType, String msg)
			throws InterruptedException {
		try {
			if (status == null)
				throw new Error("sendResponse(): Status can't be null.");

			PrintWriter pw = new PrintWriter(mySocket.getOutputStream());
			pw.print("HTTP/1.0 " + status + " \r\n");
			pw.print("Content-Type: " + mimeType + "\r\n");
			pw.print("Accept-Ranges: bytes\r\n");
			pw.print("\r\n");
			pw.flush();

			if (msg != null) {
				pw.write(msg);
			}
			pw.flush();
			pw.close();
		} catch (IOException ioe) {
		}
		throw new InterruptedException();
	}




	public void stop() {
		streaming = false;
		try {
			mySocket.close();
		} catch (IOException e) {
		}
	}
	
	public boolean isStreaming() {
		return streaming;
	}

	SocketChannel getSocketChannel() {
		return mySocket.getChannel();
	}
	
	NanoHTTPD getHttpd() {
		return httpd;
	}
	// ==================================================
	// API parts
	// ==================================================

	/**
	 * Override this to customize the server.
	 * <p>
	 * 
	 * (By default, this delegates to serveFile() and allows directory
	 * listing.)
	 * 
	 * @param uri
	 *            Percent-decoded URI without parameters, for example
	 *            "/index.cgi"
	 * @param method
	 *            "GET", "POST" etc.
	 * @param parms
	 *            Parsed, percent decoded parameters from URI and, in case
	 *            of POST, data.
	 * @param header
	 *            Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 * @throws InterruptedException
	 */
	TorrentRequest serve(HttpRequest request) throws InterruptedException {
		TorrentRequest res = null;
		try {
			String hashCode = request.getParam(PARAM_HASHCODE);
			String file = request.getParam(PARAM_FILE);
			int index = -1;
			FileEntry[] entries = null;
			if (file != null) {
				index = Integer.parseInt(file);
			} else {
				do {
					entries = libTorrent.getTorrentFiles(hashCode);
					if (entries == null) {
						Thread.sleep(1000);
					} else {
						break;
					}
				} while (!libTorrent.isUploadMode(hashCode));
				if (entries != null) {
					long maxSize = 0;
					for (int i = 0; i < entries.length; ++i) {
						if (FileUtils.isStreamable(entries[i]) && entries[i].getSize() > maxSize) {
							maxSize = entries[i].getSize();
							index = i;
						}
					}
				}
			}
			if (index == -1) {
				sendMessage(HttpStatus.HTTP_NOTFOUND, "Error 404, file not found.");
				return null;
			}
			File f = new File(rootDir, entries[index].getPath());

			// Get MIME type from file name extension, if possible
			String mime = null;
			int dot = f.getCanonicalPath().lastIndexOf('.');
			if (dot >= 0)
				mime = (String) theMimeTypes.get(f.getCanonicalPath()
						.substring(dot + 1).toLowerCase());
			if (mime == null)
				mime = NanoHTTPD.MIME_DEFAULT_BINARY;

			// Calculate etag
			String etag = Integer.toHexString((f.getAbsolutePath()
					+ f.lastModified() + "" + f.length()).hashCode());

			// Support (simple) skipping:
			long startFrom = 0;
			long endAt = -1;
			String range = request.getHeader("range");
			if (range != null) {
				if (range.startsWith("bytes=")) {
					range = range.substring("bytes=".length());
					int minus = range.indexOf('-');
					try {
						if (minus > 0) {
							startFrom = Long.parseLong(range.substring(0,
									minus));
							endAt = Long.parseLong(range
									.substring(minus + 1));
						}
					} catch (NumberFormatException nfe) {
					}
				}
			}

			// Change return code and add Content-Range header when skipping
			// is requested
			long fileLen = entries[index].getSize();
			if (range != null && startFrom >= 0) {
				if (startFrom >= fileLen) {
					res = new TorrentRequest(HttpStatus.HTTP_RANGE_NOT_SATISFIABLE,
							NanoHTTPD.MIME_PLAINTEXT, null);
					res.setHeader("Content-Range", "bytes 0-0/" + fileLen);
					res.setHeader("ETag", etag);
				} else {
					if (endAt < 0)
						endAt = fileLen - 1;
					long newLen = endAt - startFrom + 1;
					if (newLen < 0)
						newLen = 0;

					final long dataLen = newLen;
					if (request.getMethod() == HttpRequest.METHOD_HEAD) {
						res = new TorrentRequest(HttpStatus.HTTP_PARTIALCONTENT, mime,
								null);
					} else {
						res = new TorrentRequest(HttpStatus.HTTP_PARTIALCONTENT, mime,
								hashCode, index, startFrom, dataLen);
					}
					res.setHeader("Content-Length", "" + dataLen);
					res.setHeader("Content-Range", "bytes " + startFrom
							+ "-" + endAt + "/" + fileLen);
					res.setHeader("ETag", etag);
				}
			} else {
				if (etag.equals(request.getHeader("if-none-match")))
					res = new TorrentRequest(HttpStatus.HTTP_NOTMODIFIED, mime, null);
				else {
					res = request.getMethod() == HttpRequest.METHOD_HEAD ? new TorrentRequest(
							HttpStatus.HTTP_OK, mime, null) : new TorrentRequest(
							HttpStatus.HTTP_OK, mime, hashCode, index, 0, fileLen);
					res.setHeader("Content-Length", "" + fileLen);
					res.setHeader("ETag", etag);
				}
			}
			logger.debug("serve request torrent: " + hashCode + " from " + res.getTransferOffset() + " to consume " + res.getDataLength() + " with method: " + request.getMethod());
		} catch (NumberFormatException e) {
			sendMessage(HttpStatus.HTTP_NOTFOUND, "Error 404, file not found.");
		} catch (IOException ioe) {
			sendMessage(HttpStatus.HTTP_FORBIDDEN, "FORBIDDEN: Reading file failed.");
		} catch (TorrentException e) {
			sendMessage(HttpStatus.HTTP_NOTFOUND, "Error 404, file not found.");
		}

		res.setHeader("Accept-Ranges", "bytes"); // Announce that the file
													// server accepts
													// partial content
													// requestes
		return res;
	}



}
