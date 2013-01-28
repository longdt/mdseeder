/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_solt_libtorrent_LibTorrent */

#ifndef _Included_com_solt_libtorrent_LibTorrent
#define _Included_com_solt_libtorrent_LibTorrent
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setSession
 * Signature: (ILjava/lang/String;II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_solt_libtorrent_LibTorrent_setSession
  (JNIEnv *, jobject, jint, jstring, jint, jint);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setProxy
 * Signature: (ILjava/lang/String;ILjava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_solt_libtorrent_LibTorrent_setProxy
  (JNIEnv *, jobject, jint, jstring, jint, jstring, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setSessionOptions
 * Signature: (ZZZZ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_solt_libtorrent_LibTorrent_setSessionOptions
  (JNIEnv *, jobject, jboolean, jboolean, jboolean, jboolean);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    addTorrent
 * Signature: (Ljava/lang/String;IZ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_solt_libtorrent_LibTorrent_addTorrent
  (JNIEnv *, jobject, jstring, jint, jboolean);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    addAsyncTorrent
 * Signature: (Ljava/lang/String;IZ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_solt_libtorrent_LibTorrent_addAsyncTorrent
  (JNIEnv *, jobject, jstring, jint, jboolean);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    addMagnetUri
 * Signature: (Ljava/lang/String;IZ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_solt_libtorrent_LibTorrent_addMagnetUri
  (JNIEnv *, jobject, jstring, jint, jboolean);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    addAsyncMagnetUri
 * Signature: (Ljava/lang/String;IZ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_solt_libtorrent_LibTorrent_addAsyncMagnetUri
  (JNIEnv *, jobject, jstring, jint, jboolean);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    saveResumeData
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_solt_libtorrent_LibTorrent_saveResumeData
  (JNIEnv *, jobject);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    pauseSession
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_solt_libtorrent_LibTorrent_pauseSession
  (JNIEnv *, jobject);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    resumeSession
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_solt_libtorrent_LibTorrent_resumeSession
  (JNIEnv *, jobject);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    abortSession
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_solt_libtorrent_LibTorrent_abortSession
  (JNIEnv *, jobject);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    removeTorrent
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_com_solt_libtorrent_LibTorrent_removeTorrent
  (JNIEnv *, jobject, jstring, jboolean);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    pauseTorrent
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_solt_libtorrent_LibTorrent_pauseTorrent
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    resumeTorrent
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_solt_libtorrent_LibTorrent_resumeTorrent
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getTorrentProgress
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_solt_libtorrent_LibTorrent_getTorrentProgress
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getTorrentProgressSize
 * Signature: (Ljava/lang/String;I)J
 */
JNIEXPORT jlong JNICALL Java_com_solt_libtorrent_LibTorrent_getTorrentProgressSize
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getTorrentContinuousSize
 * Signature: (Ljava/lang/String;J)J
 */
JNIEXPORT jlong JNICALL Java_com_solt_libtorrent_LibTorrent_getTorrentContinuousSize
  (JNIEnv *, jobject, jstring, jlong);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setTorrentReadPiece
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_setTorrentReadPiece
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    readTorrentPiece
 * Signature: (Ljava/lang/String;I[B)I
 */
JNIEXPORT jint JNICALL Java_com_solt_libtorrent_LibTorrent_readTorrentPiece
  (JNIEnv *, jobject, jstring, jint, jbyteArray);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setDownloadRateLimit
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_setDownloadRateLimit
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getDownloadRateLimit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_solt_libtorrent_LibTorrent_getDownloadRateLimit
  (JNIEnv *, jobject);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setUploadRateLimit
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_setUploadRateLimit
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getUploadRateLimit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_solt_libtorrent_LibTorrent_getUploadRateLimit
  (JNIEnv *, jobject);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setTorrentDownloadLimit
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_setTorrentDownloadLimit
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getTorrentDownloadLimit
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_solt_libtorrent_LibTorrent_getTorrentDownloadLimit
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getTorrentDownloadRate
 * Signature: (Ljava/lang/String;Z)I
 */
JNIEXPORT jint JNICALL Java_com_solt_libtorrent_LibTorrent_getTorrentDownloadRate
  (JNIEnv *, jobject, jstring, jboolean);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setTorrentUploadLimit
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_setTorrentUploadLimit
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getTorrentUploadLimit
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_solt_libtorrent_LibTorrent_getTorrentUploadLimit
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setUploadMode
 * Signature: (Ljava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_setUploadMode
  (JNIEnv *, jobject, jstring, jboolean);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    isAutoManaged
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_solt_libtorrent_LibTorrent_isAutoManaged
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setAutoManaged
 * Signature: (Ljava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_setAutoManaged
  (JNIEnv *, jobject, jstring, jboolean);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getPieceNum
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_solt_libtorrent_LibTorrent_getPieceNum
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getPieceSize
 * Signature: (Ljava/lang/String;Z)I
 */
JNIEXPORT jint JNICALL Java_com_solt_libtorrent_LibTorrent_getPieceSize
  (JNIEnv *, jobject, jstring, jboolean);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getFirstPieceIncomplete
 * Signature: (Ljava/lang/String;J)I
 */
JNIEXPORT jint JNICALL Java_com_solt_libtorrent_LibTorrent_getFirstPieceIncomplete
  (JNIEnv *, jobject, jstring, jlong);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setPiecePriority
 * Signature: (Ljava/lang/String;II)V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_setPiecePriority
  (JNIEnv *, jobject, jstring, jint, jint);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getPiecePriority
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_solt_libtorrent_LibTorrent_getPiecePriority
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setPiecePriorities
 * Signature: (Ljava/lang/String;[B)V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_setPiecePriorities
  (JNIEnv *, jobject, jstring, jbyteArray);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getPiecePriorities
 * Signature: (Ljava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_solt_libtorrent_LibTorrent_getPiecePriorities
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setPieceDeadline
 * Signature: (Ljava/lang/String;IIZ)V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_setPieceDeadline
  (JNIEnv *, jobject, jstring, jint, jint, jboolean);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    resetPieceDeadline
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_resetPieceDeadline
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    clearPiecesDeadline
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_clearPiecesDeadline
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    cancelTorrentPiece
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_cancelTorrentPiece
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    initPartialPiece
 * Signature: (Ljava/lang/String;I[I)[I
 */
JNIEXPORT jintArray JNICALL Java_com_solt_libtorrent_LibTorrent_initPartialPiece
  (JNIEnv *, jobject, jstring, jint, jintArray);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getPieceDownloadQueue
 * Signature: (Ljava/lang/String;)[Lcom/solt/libtorrent/PartialPieceInfo;
 */
JNIEXPORT jobjectArray JNICALL Java_com_solt_libtorrent_LibTorrent_getPieceDownloadQueue
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getTorrentState
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_solt_libtorrent_LibTorrent_getTorrentState
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getTorrentStatusText
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_solt_libtorrent_LibTorrent_getTorrentStatusText
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getSessionStatusText
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_solt_libtorrent_LibTorrent_getSessionStatusText
  (JNIEnv *, jobject);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getTorrentFiles
 * Signature: (Ljava/lang/String;)[Lcom/solt/libtorrent/FileEntry;
 */
JNIEXPORT jobjectArray JNICALL Java_com_solt_libtorrent_LibTorrent_getTorrentFiles
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    setTorrentFilesPriority
 * Signature: ([BLjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_solt_libtorrent_LibTorrent_setTorrentFilesPriority
  (JNIEnv *, jobject, jbyteArray, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getTorrentFilesPriority
 * Signature: (Ljava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_solt_libtorrent_LibTorrent_getTorrentFilesPriority
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getTorrentName
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_solt_libtorrent_LibTorrent_getTorrentName
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    getTorrentSize
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_solt_libtorrent_LibTorrent_getTorrentSize
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_solt_libtorrent_LibTorrent
 * Method:    handleAlerts
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_solt_libtorrent_LibTorrent_handleAlerts
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
