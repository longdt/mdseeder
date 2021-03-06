/*
 * torrentinfo.h
 *
 *  Created on: May 18, 2012
 *      Author: user
 */

#ifndef TORRENTINFO_H_
#define TORRENTINFO_H_
#include "concurrentqueue.h"
#include "piecedataqueue.h"

struct cancel_piece {
	int index;
	bool force;
	cancel_piece(): index(-1), force(false) {}
	cancel_piece(int index, bool force): index(index), force(force) {}
};

class TorrentInfo {
public:
	libtorrent::torrent_handle handle;

	int pieceTransferIdx;
	//pieces from [pieceTransferIdx, firstPieceIncompleteIdx) were downloaded
	int firstPieceIncompleteIdx;

	mutable boost::mutex cont_piece_mutex;

	solt::concurrent_queue<cancel_piece> cancel_piece_tasks;

	solt::piece_data_queue piece_queue;

	TorrentInfo() : handle(), pieceTransferIdx(0), firstPieceIncompleteIdx(0) {
	}
};

TorrentInfo* GetTorrentInfo(libtorrent::sha1_hash &hash);

#endif /* TORRENTINFO_H_ */
