package repro;

import com.azure.core.util.Context;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.file.share.models.ShareFileUploadOptions;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Upload {
    private static final String CONNECTION_STRING = "xxx";
    private static final String SHARE_NAME = "xxx";
    private static final String DIRECTORY_NAME = "xxx";

    public static void main(String[] args) {
        String filename = "testfile_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".txt";
        ShareFileClient client = new ShareServiceClientBuilder()
                .connectionString(CONNECTION_STRING)
                .buildClient()
                .getShareClient(SHARE_NAME)
                .getDirectoryClient(DIRECTORY_NAME)
                .getFileClient(filename);

        byte[] mb100 = new byte[100_000_000];
        Arrays.fill(mb100, (byte) 0);

        Collection<ByteArrayInputStream> byteStreams3gb = IntStream.range(0,30)
                .mapToObj(n -> new ByteArrayInputStream(mb100))
                .collect(Collectors.toList());

        InputStream dataStream = new BufferedInputStream(new SequenceInputStream(Collections.enumeration(byteStreams3gb)));
        long size = mb100.length * 30L;

        client.create(size);
        client.uploadWithResponse(new ShareFileUploadOptions(dataStream, size), null, Context.NONE);
    }
}
