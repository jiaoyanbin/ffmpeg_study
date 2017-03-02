#include <string.h>
#include <jni.h>
#include "com_wedrive_android_welink_jni_FFmpegUtils.h"
#include "com_wedrive_android_welink_jni_FFmpegUtils_OnDecodeListener.h"
#include "include/libavformat/avformat.h"
#include "include/libavcodec/avcodec.h"
#include "include/libavutil/avutil.h"
#include "include/libavfilter/avfilter.h"
#include "libavutil/pixfmt.h"

#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"

#include "utils/my_log.h"

#define isDebug 1
typedef long LONG;
typedef unsigned long DWORD;
typedef unsigned short WORD;

typedef struct {
	WORD bfType;
	DWORD bfSize;
	WORD bfReserved1;
	WORD bfReserved2;
	DWORD bfOffBits;
} BMPFILEHEADER_T;

typedef struct {
	DWORD biSize;
	LONG biWidth;
	LONG biHeight;
	WORD biPlanes;
	WORD biBitCount;
	DWORD biCompression;
	DWORD biSizeImage;
	LONG biXPelsPerMeter;
	LONG biYPelsPerMeter;
	DWORD biClrUsed;
	DWORD biClrImportant;
} BMPINFOHEADER_T;

struct SwsContext *m_pImgCtx;
AVPacket m_avpkt;
AVCodec *m_codec;
AVCodecContext *m_pCodecCtx;
AVCodecParserContext *m_pCodecParserCtx;
AVFrame *m_picture;
AVFrame *m_pFrameRGB;
int m_PicBytes = 0;
//uint8_t[] m_PicBuf;
uint8_t *m_PicBuf;
struct SwsContext *m_pImgCtx;
int frameNumber = 0;
char* savePath;

int initdecode();
void uninit_decode();
void SaveAsBMP(AVFrame *pFrameRGB, int width, int height, int index, int bpp,
		char *filenameP) {
	char buf[5] = { 0 };
	FILE *fp;
	char filename[255] = { 0 };

	//文件存放路径，根据自己的修改
	sprintf(filename, "%svideo%d.bmp", filenameP, index);

//	if(isDebug)
//		MY_LOG_ERROR("=====picturename = %s",filename);

	if ((fp = fopen(filename, "wb+")) == NULL) {
		if (isDebug) {
			MY_LOG_ERROR("=====open file failed! %s", filename);
		}
		return;
	}

	int size = width * height * 3; // 每个像素点3个字节
	// 位图第一部分，文件信息
	BMPFILEHEADER_T bfh;
	bfh.bfType = (WORD) 0x4d42; //bm
	bfh.bfSize = size // data size
	+ sizeof(BMPFILEHEADER_T) // first section size
			+ sizeof(BMPINFOHEADER_T) // second section size
			;
	bfh.bfReserved1 = 0; // reserved
	bfh.bfReserved2 = 0; // reserved

	bfh.bfOffBits = (WORD) 0x36;
	// 位图第二部分，数据信息
	BMPINFOHEADER_T bih;
	bih.biSize = sizeof(BMPINFOHEADER_T);
	bih.biWidth = width;
	bih.biHeight = height; //BMP图片从最后一个点开始扫描，显示时图片是倒着的，所以用-height，这样图片就正了
	bih.biPlanes = 1; //为1，不用改
	bih.biBitCount = 24;
	bih.biCompression = 0; //不压缩

	bih.biSizeImage = (width * bpp + 31) / 32 * 4 * height;
	bih.biXPelsPerMeter = 100; //像素每米
	bih.biYPelsPerMeter = 100;
	bih.biClrUsed = 0; //已用过的颜色，24位的为0
	bih.biClrImportant = 0; //每个像素都重要

	fwrite(&bfh, 8, 1, fp); //由于linux上4字节对齐，而信息头大小为54字节，第一部分14字节，第二部分40字节，所以会将第一部分补齐为16自己，直接用sizeof，打开图片时就会遇到premature end-of-file encountered错误
	fwrite(&bfh.bfReserved2, sizeof(bfh.bfReserved2), 1, fp);
	fwrite(&bfh.bfOffBits, sizeof(bfh.bfOffBits), 1, fp);
	fwrite(&bih, sizeof(BMPINFOHEADER_T), 1, fp);
	fwrite(pFrameRGB->data[0], size, 1, fp);
	fclose(fp);
}



jboolean JNICALL Java_com_wedrive_android_welink_jni_FFmpegUtils_decodeFile4Jni(
		JNIEnv *env, jobject obj, jstring filePath, jstring outPath) {

	av_register_all();
	int videoStream = -1;
	AVCodecContext *pCodecCtx;
	AVFormatContext *pFormatCtx;
	AVCodec *pCodec;
	AVFrame *pFrame, *pFrameRGB;
	struct SwsContext *pSwsCtx;
	char *filename;
	char *outFilePath;
	AVPacket packet;
	int frameFinished;
	int PictureSize;
	uint8_t *outBuff;

	filename = (*env)->GetStringUTFChars(env, filePath, NULL);
	outFilePath = (*env)->GetStringUTFChars(env, outPath, NULL);

	if (isDebug)
		MY_LOG_ERROR("=====inputFileName = %s=====outputFileName = %s",
				filename, outFilePath);

	avformat_network_init();

	pFormatCtx = avformat_alloc_context();

	if (avformat_open_input(&pFormatCtx, filename, NULL, NULL) != 0) {
		if (isDebug)
			MY_LOG_ERROR("=====111 av open input file failed!");

	}

	//获取流信息
	if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
		if (isDebug)
			MY_LOG_ERROR("=====222 av find stream info failed!");

	}
	//获取视频流
	for (int i = 0; i < pFormatCtx->nb_streams; i++) {
		if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
			videoStream = i;
			break;
		}
	}
	if (videoStream == -1) {
		if (isDebug)
			MY_LOG_ERROR("=====333 find video stream failed!");

	}

	// 寻找解码器
	pCodecCtx = pFormatCtx->streams[videoStream]->codec;
	pCodec = avcodec_find_decoder(pCodecCtx->codec_id);
	if (pCodec == NULL) {
		if (isDebug)
			MY_LOG_ERROR("=====444 avcode find decoder failed!");

	}

	//打开解码器
	if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
		if (isDebug)
			MY_LOG_ERROR("=====555 avcode open failed!");

	}

	//为每帧图像分配内存
	pFrame = av_frame_alloc();
	pFrameRGB = av_frame_alloc();
	if ((pFrame == NULL) || (pFrameRGB == NULL)) {
		if (isDebug)
			MY_LOG_ERROR("=====666 avcodec alloc frame failed!");

	}

	// 确定图片尺寸
	PictureSize = avpicture_get_size(AV_PIX_FMT_BGR24, pCodecCtx->width,
			pCodecCtx->height);
	outBuff = (uint8_t*) av_malloc(PictureSize);
	if (outBuff == NULL) {
		if (isDebug)
			MY_LOG_ERROR("=====777 av malloc failed!");

	}
	avpicture_fill((AVPicture *) pFrameRGB, outBuff, AV_PIX_FMT_BGR24,
			pCodecCtx->width, pCodecCtx->height);

	//设置图像转换上下文
	pSwsCtx = sws_getContext(pCodecCtx->width, pCodecCtx->height,
			pCodecCtx->pix_fmt, pCodecCtx->width, pCodecCtx->height,
			AV_PIX_FMT_BGR24, SWS_BICUBIC, NULL, NULL, NULL);

	int i = 0;
	while (av_read_frame(pFormatCtx, &packet) >= 0 && i < 200) {
		if (packet.stream_index == videoStream) {
			avcodec_decode_video2(pCodecCtx, pFrame, &frameFinished, &packet);
			MY_LOG_ERROR("=====i = %d", strlen(&pFrame));
			if (frameFinished) {
				//反转图像 ，否则生成的图像是上下调到的
				pFrame->data[0] += pFrame->linesize[0]
						* (pCodecCtx->height - 1);
				pFrame->linesize[0] *= -1;
				pFrame->data[1] += pFrame->linesize[1]
						* (pCodecCtx->height / 2 - 1);
				pFrame->linesize[1] *= -1;
				pFrame->data[2] += pFrame->linesize[2]
						* (pCodecCtx->height / 2 - 1);
				pFrame->linesize[2] *= -1;

				//转换图像格式，将解压出来的YUV420P的图像转换为BRG24的图像

				sws_scale(pSwsCtx, pFrame->data, pFrame->linesize, 0,
						pCodecCtx->height, pFrameRGB->data,
						pFrameRGB->linesize);

				SaveAsBMP(pFrameRGB, pCodecCtx->width, pCodecCtx->height, i++,
						24, outFilePath);
				i++;
			}
		} else {
			int a = 2;
			int b = a;
		}

		av_free_packet(&packet);
	}

	sws_freeContext(pSwsCtx);
	av_free(pFrame);
	av_free(pFrameRGB);
	avcodec_close(pCodecCtx);
	avformat_close_input(&pFormatCtx);

	return i;

}

void JNICALL Java_com_wedrive_android_welink_jni_FFmpegUtils_initdecode
(JNIEnv *env, jobject obj, jstring outpath) {
	frameNumber = 0;
	savePath = (*env)->GetStringUTFChars(env, outpath, NULL);
	initdecode();

}

void JNICALL Java_com_wedrive_android_welink_jni_FFmpegUtils_destorydecode
(JNIEnv *env, jobject obj) {
	frameNumber = 0;
	savePath = NULL;
	uninit_decode();
}

jbyteArray JNICALL Java_com_wedrive_android_welink_jni_FFmpegUtils_decodeStream(
		JNIEnv *env, jobject obj, jbyteArray buffer) {

//	if(!m_pCodecCtx){
//		MY_LOG_ERROR("2222222222222 ting zhi jie ma 222222");
//		return NULL;
//	}
	char* buf = NULL;
	jsize buf_len = (*env)->GetArrayLength(env, buffer);

	jbyte* ba = (*env)->GetByteArrayElements(env, buffer, JNI_FALSE); //jbyteArray转为jbyte*

	if (buf_len > 0) {
		buf = (char*) malloc(buf_len + 1); //"\0"
		memcpy(buf, ba, buf_len);
		buf[buf_len] = 0;
	}
	(*env)->ReleaseByteArrayElements(env, buffer, ba, 0); //释放掉

	int got, len, paser_len;

	if (buf == NULL || buf_len == 0) {
		return NULL;
	}

	paser_len = av_parser_parse2(m_pCodecParserCtx, m_pCodecCtx, &m_avpkt.data,
			&m_avpkt.size, (uint8_t *) buf, buf_len, AV_NOPTS_VALUE,
			AV_NOPTS_VALUE, AV_NOPTS_VALUE);

	if (paser_len == 0) {
		switch (m_pCodecParserCtx->pict_type) {
		case AV_PICTURE_TYPE_I:
			if (isDebug)
				MY_LOG_ERROR("2222222222222222222 Type: I");
			break;
		case AV_PICTURE_TYPE_P:
			if (isDebug)
				MY_LOG_ERROR("2222222222222222222 Type: P");
			break;
		case AV_PICTURE_TYPE_B:
			if (isDebug)
				MY_LOG_ERROR("2222222222222222222 Type: B");
			break;

		}

		len = avcodec_decode_video2(m_pCodecCtx, m_picture, &got, &m_avpkt);

		if (len < 0) {
			return NULL;
		}

		//==========================================
		int width = m_pCodecCtx->width;
		int height = m_pCodecCtx->height;

		// 输出文件路径
		char out_file[255] = { 0 };
		sprintf(out_file, "%s%d.jpg", savePath, frameNumber);

		// 分配AVFormatContext对象
		AVFormatContext* pFormatCtx = avformat_alloc_context();

		// 设置输出文件格式
		pFormatCtx->oformat = av_guess_format("mjpeg", NULL, NULL);
		// 创建并初始化一个和该url相关的AVIOContext
		if (avio_open(&pFormatCtx->pb, out_file, AVIO_FLAG_READ_WRITE) < 0) {
			printf("Couldn't open output file.");
			return NULL;
		}

		// 构建一个新stream
		AVStream* pAVStream = avformat_new_stream(pFormatCtx, 0);
		if (pAVStream == NULL) {
			return NULL;
		}

		// 设置该stream的信息
		AVCodecContext* pCodecCtx = pAVStream->codec;

		pCodecCtx->codec_id = pFormatCtx->oformat->video_codec;
		pCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
		pCodecCtx->pix_fmt = AV_PIX_FMT_YUVJ420P;
		pCodecCtx->width = width;
		pCodecCtx->height = height;
		pCodecCtx->time_base.num = 1;
		pCodecCtx->time_base.den = 25;

		// Begin Output some information
		av_dump_format(pFormatCtx, 0, out_file, 1);
		// End Output some information

		// 查找解码器
		AVCodec* pCodec = avcodec_find_encoder(pCodecCtx->codec_id);
		if (!pCodec) {
			printf("Codec not found.");
			return NULL;
		}
		// 设置pCodecCtx的解码器为pCodec
		if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
			printf("Could not open codec.");
			return NULL;
		}

		//Write Header
		avformat_write_header(pFormatCtx, NULL);

		int y_size = pCodecCtx->width * pCodecCtx->height;

		//Encode
		// 给AVPacket分配足够大的空间
		AVPacket pkt;
		av_new_packet(&pkt, y_size * 3);

		//
		int got_picture = 0;
		int ret = avcodec_encode_video2(pCodecCtx, &pkt, m_picture,
				&got_picture);
		if (ret < 0) {
			printf("Encode Error.\n");
			return NULL;
		}
		if (got_picture == 1) {
			//pkt.stream_index = pAVStream->index;
			ret = av_write_frame(pFormatCtx, &pkt);
		}

		av_free_packet(&pkt);

		//Write Trailer
		av_write_trailer(pFormatCtx);

		printf("Encode Successful.\n");

		if (pAVStream) {
			avcodec_close(pAVStream->codec);
		}
		avio_close(pFormatCtx->pb);
		avformat_free_context(pFormatCtx);

		//======================================
		FILE *fp;
		fp = fopen(out_file, "r");
		fseek(fp, 0, SEEK_END);
		int file_size;
		file_size = ftell(fp);

		char *dataImgByte;
		fseek(fp, 0, SEEK_SET);
		dataImgByte = (char *) malloc(file_size * sizeof(char));
		fread(dataImgByte, file_size, sizeof(char), fp);
//TODO
//		gets(out_file);
//		remove(out_file);
//=============================================================================================
//		if (got) {
//			if (m_PicBytes == 0) {
//				m_PicBytes = avpicture_get_size(AV_PIX_FMT_BGR24,
//						m_pCodecCtx->width, m_pCodecCtx->height);
//				m_PicBuf = (uint8_t *) malloc(m_PicBytes * sizeof(uint8_t));
//				avpicture_fill((AVPicture *) m_pFrameRGB, m_PicBuf,
//						AV_PIX_FMT_BGR24, m_pCodecCtx->width,
//						m_pCodecCtx->height);
//			}
//
//			if (!m_pImgCtx) {
//				m_pImgCtx = sws_getContext(m_pCodecCtx->width,
//						m_pCodecCtx->height, m_pCodecCtx->pix_fmt,
//						m_pCodecCtx->width, m_pCodecCtx->height,
//						AV_PIX_FMT_BGR24, SWS_BICUBIC, NULL, NULL, NULL);
//			}
//
//			m_picture->data[0] += m_picture->linesize[0]
//					* (m_pCodecCtx->height - 1);
//			m_picture->linesize[0] *= -1;
//			m_picture->data[1] += m_picture->linesize[1]
//					* (m_pCodecCtx->height / 2 - 1);
//			m_picture->linesize[1] *= -1;
//			m_picture->data[2] += m_picture->linesize[2]
//					* (m_pCodecCtx->height / 2 - 1);
//			m_picture->linesize[2] *= -1;
//			sws_scale(m_pImgCtx, (const uint8_t* const *) m_picture->data,
//					m_picture->linesize, 0, m_pCodecCtx->height,
//					m_pFrameRGB->data, m_pFrameRGB->linesize);
//
		frameNumber++;
//
////=====================================================================================================
//
//			int width = m_pCodecCtx->width;
//			int height = m_pCodecCtx->height;
//
//			int size = width * height * 3; // 每个像素点3个字节
//			// 位图第一部分，文件信息
//			BMPFILEHEADER_T bfh;
//			bfh.bfType = (WORD) 0x4d42; //bm
//			bfh.bfSize = size // data size
//			+ sizeof(BMPFILEHEADER_T) // first section size
//					+ sizeof(BMPINFOHEADER_T) // second section size
//					;
//			bfh.bfReserved1 = 0; // reserved
//			bfh.bfReserved2 = 0; // reserved
//
//			bfh.bfOffBits = (WORD) 0x36;
//			// 位图第二部分，数据信息
//			BMPINFOHEADER_T bih;
//			bih.biSize = sizeof(BMPINFOHEADER_T);
//			bih.biWidth = width;
//			bih.biHeight = height; //BMP图片从最后一个点开始扫描，显示时图片是倒着的，所以用-height，这样图片就正了
//			bih.biPlanes = 1; //为1，不用改
//			bih.biBitCount = 24;
//			bih.biCompression = 0; //不压缩
//
//			bih.biSizeImage = (width * 24 + 31) / 32 * 4 * height;
//			bih.biXPelsPerMeter = 100; //像素每米
//			bih.biYPelsPerMeter = 100;
//			bih.biClrUsed = 0; //已用过的颜色，24位的为0
//			bih.biClrImportant = 0; //每个像素都重要
//
//			int file_size =
//					(8 + sizeof(bfh.bfReserved2) + sizeof(bfh.bfOffBits)
//							+ sizeof(BMPINFOHEADER_T) + size);
//			char *dataImgByte = (char *) malloc(file_size);
//
//			memcpy(dataImgByte, &bfh, 8);
//			memcpy(dataImgByte + 8, &bfh.bfReserved2,
//					sizeof(bfh.bfReserved2));
//			memcpy(dataImgByte + 8 + sizeof(bfh.bfReserved2), &bfh.bfOffBits,
//					sizeof(bfh.bfOffBits));
//			memcpy(
//					dataImgByte + 8 + sizeof(bfh.bfReserved2)
//							+ sizeof(bfh.bfOffBits), &bih,
//					sizeof(BMPINFOHEADER_T));
//			memcpy(
//					dataImgByte + 8 + sizeof(bfh.bfReserved2)
//							+ sizeof(bfh.bfOffBits) + sizeof(BMPINFOHEADER_T),m_pFrameRGB->data[0], size);
//
//			MY_LOG_ERROR("============= strlen = %d ",file_size);
			jbyteArray RtnArr = NULL;
			RtnArr = (*env)->NewByteArray(env,file_size);
			(*env)->SetByteArrayRegion(env,RtnArr, 0, file_size,
					(jbyte*) dataImgByte);
			if (dataImgByte) {
				free(dataImgByte);
			}
//=====================================================================================================

			jclass cls = (*env)->FindClass(env,
					"com/wedrive/android/welink/jni/FFmpegUtils");
			jmethodID callback = (*env)->GetMethodID(env, cls, "onImage",
					"([B)V");
			(*env)->CallVoidMethod(env, obj, callback, RtnArr);

//=====================================================================================================
//
//			SaveAsBMP(m_pFrameRGB, m_pCodecCtx->width, m_pCodecCtx->height,
//					frameNumber, 24, savePath);
//		}
	}

	if (paser_len == 0) {
		paser_len = av_parser_parse2(m_pCodecParserCtx, m_pCodecCtx,
				&m_avpkt.data, &m_avpkt.size, (uint8_t *) buf, buf_len,
				AV_NOPTS_VALUE, AV_NOPTS_VALUE, AV_NOPTS_VALUE);
	}

	return NULL;

}

int initdecode() {

	avformat_network_init();

	avformat_alloc_context();
	av_init_packet(&m_avpkt);
	av_register_all();
	m_codec = avcodec_find_decoder(AV_CODEC_ID_H264);
	if (!m_codec) {
		if (isDebug)
			MY_LOG_ERROR("2222222222222 Codec not found");
		return -1;
	}
	m_pCodecCtx = avcodec_alloc_context3(m_codec);
	if (!m_pCodecCtx) {
		if (isDebug)
			MY_LOG_ERROR("2222222222222 video codec context");
		return -1;
	}

	m_pCodecParserCtx = av_parser_init(AV_CODEC_ID_H264);
	if (!m_pCodecParserCtx) {
		if (isDebug)
			MY_LOG_ERROR(
					"2222222222222 Could not allocate video parser context");
		return -1;
	}

	if (m_codec->capabilities & CODEC_CAP_TRUNCATED)
		m_pCodecCtx->flags |= CODEC_FLAG_TRUNCATED;

	if (avcodec_open2(m_pCodecCtx, m_codec, NULL) < 0) {
		if (isDebug)
			MY_LOG_ERROR("2222222222222 Could not open codec");
		return -1;
	}

	m_picture = av_frame_alloc();
	m_pFrameRGB = av_frame_alloc();
	if (!m_picture || !m_pFrameRGB) {
		if (isDebug)
			MY_LOG_ERROR("2222222222222 Could not allocate video frame");
		return -1;
	}

	m_PicBytes = 0;
	m_PicBuf = NULL;
	m_pImgCtx = NULL;

	return 0;
}

void uninit_decode() {
	if (isDebug)
				MY_LOG_ERROR("2222222222222 video codec uninit_decode222222");
	if(m_pCodecCtx){
		avcodec_close(m_pCodecCtx);
		m_pCodecCtx = NULL;
	}

	if (m_pCodecCtx) {
		av_free(m_pCodecCtx);
		m_pCodecCtx = NULL;
	}
	if (m_picture) {
		av_frame_free(&m_picture);
		m_picture = NULL;
	}
	if (m_pFrameRGB) {
		av_frame_free(&m_pFrameRGB);
		m_pFrameRGB = NULL;
	}
	if (m_pImgCtx) {
		sws_freeContext(m_pImgCtx);
	}

	if (m_pCodecParserCtx) {
		av_parser_close(m_pCodecParserCtx);
	}

	m_PicBytes = 0;
	if (m_PicBuf != NULL) {
		free(m_PicBuf);
		m_PicBuf = NULL;
	}
}

