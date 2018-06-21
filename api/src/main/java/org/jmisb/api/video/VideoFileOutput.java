package org.jmisb.api.video;

import org.bytedeco.javacpp.*;
import org.jmisb.core.video.FfmpegUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.swscale.SWS_FAST_BILINEAR;

/**
 * Write video/metadata to a file
 */
public class VideoFileOutput extends VideoOutput implements IVideoFileOutput
{
    private static Logger logger = LoggerFactory.getLogger(VideoFileOutput.class);
    private String filename;

    /**
     * Constructor
     * <p>
     * Clients must use {@link VideoSystem#createOutputFile(VideoOutputOptions)} to construct new instances
     */
    VideoFileOutput(VideoOutputOptions options)
    {
        super(options);
    }

    @Override
    public void open(String filename) throws IOException
    {
        this.filename = filename;

        initCodecs();
        initFormat();
        openVideoCodec();
        createVideoStream();

        // TODO: make optional
        createMetadataStream();

        // Open the file
        int ret;
        avformat.AVIOContext ioContext = new avformat.AVIOContext(null);
        if ((ret = avio_open2(ioContext, filename, AVIO_FLAG_WRITE, null, null)) < 0)
        {
            throw new IOException("Error opening file: " + FfmpegUtils.formatError(ret));
        }
        formatContext.pb(ioContext);

        avutil.AVDictionary opts = new avutil.AVDictionary(null);
        avformat_write_header(formatContext, opts);
        av_dict_free(opts);

//        av_dump_format(formatContext, 0, filename, 1);
    }

    @Override
    public boolean isOpen()
    {
        return formatContext != null;
    }

    @Override
    public void close() throws IOException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Closing " + filename);
        }

        if (!isOpen())
        {
            logger.warn("Video output file " + filename + " is already closed; ignoring close() call");
            return;
        }

        // Write out any remaining frames
        flush();

        // Clean up in super
        cleanup();
    }

    @Override
    public void addVideoFrame(VideoFrame frame) throws IOException
    {
        if (frame.getImage().getWidth() != options.getWidth() ||
                frame.getImage().getHeight() != options.getHeight())
        {
            throw new IllegalArgumentException("Invalid image dimensions");
        }

        // Luca's note on using the new ffmpeg API:
        //
        // – You feed data using the avcodec_send_* functions until you get a AVERROR(EAGAIN), that signals that the
        //   internal input buffer is full.
        //
        // – You get the data back using the matching avcodec_receive_* function until you get a AVERROR(EAGAIN),
        //   signalling that the internal output buffer is empty.
        //
        // – Once you are done feeding data you have to pass a NULL to signal the end of stream.
        //
        // – You can keep calling the avcodec_receive_* function until you get AVERROR_EOF.
        //

        encodeFrame(frame);

        // Write out any available packets
        writeAvailablePackets(false);
    }

    @Override
    public void addMetadataFrame(MetadataFrame frame) throws IOException
    {
        AVPacket packet = convert(frame);

        // Write the packet to the file
        int ret;
        if ((ret = av_write_frame(formatContext, packet)) < 0)
        {
            throw new IOException("Error writing metadata packet: " + FfmpegUtils.formatError(ret));
        }

        // Free packet
        av_packet_free(packet);
    }

    /**
     * Takes all available packets out of the encoder's internal buffer, then writes them to the output
     *
     * @param eof If true, expect EOF and throw exception if not found
     * @throws IOException if expected EOF packet is not found
     */
    private void writeAvailablePackets(boolean eof) throws IOException
    {
        List<AVPacket> packets = new ArrayList<>();

        // Drain all packets from the encoder
        int ret2 = 0;
        while (ret2 != AVERROR_EOF && ret2 != AVERROR_EAGAIN())
        {
            AVPacket packet = av_packet_alloc();
            ret2 = avcodec_receive_packet(videoCodecContext, packet);
            if (ret2 == 0)
            {
                packets.add(packet);
            }
            else if (ret2 == AVERROR_EOF)
            {
                logger.debug("EOF reached");
            }
        }

        if (eof && ret2 != AVERROR_EOF)
        {
            throw new IOException("Expected EOF packet not found");
        }

        // Write all new packets to the output
        for (AVPacket packet : packets)
        {
            int ret3;
            if ((ret3 = av_write_frame(formatContext, packet)) < 0)
            {
                logger.error("Error writing video packet: " + FfmpegUtils.formatError(ret3));
            }
            framesWritten++;
            av_packet_free(packet);
        }
    }

    /**
     * Write all available data to the output before closing
     *
     * @throws IOException if the data could not be written
     */
    private void flush() throws IOException
    {
        // Send null to the encoder, signalling EOF and entering "draining mode"
        avcodec_send_frame(videoCodecContext, null);

        // Write out all available packets
        writeAvailablePackets(true);

        // Write trailer
        // TODO: may not be necessary
        av_write_trailer(formatContext);

        if (logger.isDebugEnabled())
        {
            logger.debug("# frames written: " + framesWritten);
        }
    }
}
