package com.foobnix.ext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.compress.utils.IOUtils;

import com.foobnix.android.utils.LOG;
import com.foobnix.android.utils.TxtUtils;
import com.foobnix.libmobi.LibMobi;
import com.foobnix.mobi.parser.MobiParser;
import com.foobnix.pdf.info.wrapper.AppState;

public class MobiExtract {

    private static MobiParser parse;

    public static FooterNote extract(String inputPath, final String outputDir, String hashCode) throws IOException {
        try {
            LibMobi.convertToEpub(inputPath, new File(outputDir, hashCode + "").getPath());
            File result = new File(outputDir, hashCode + hashCode + ".epub");
            return new FooterNote(result.getPath(), null);
        } catch (Exception e) {
            LOG.e(e);
        }
        return new FooterNote("", null);
    }

    public static EbookMeta getBookMetaInformation(String path, boolean onlyTitle) throws IOException {
        File file = new File(path);
        try {
            byte[] raw = IOUtils.toByteArray(new FileInputStream(file));

            parse = new MobiParser(raw);
            String title = parse.getTitle();
            String author = parse.getAuthor();
            String subject = parse.getSubject();

            if (TxtUtils.isEmpty(title)) {
                title = file.getName();
            }
            byte[] decode = null;
            if (!onlyTitle) {
                decode = parse.getCoverOrThumb();
            }

            if (AppState.get().isFirstSurname) {
                author = TxtUtils.replaceLastFirstName(author);
            }

            EbookMeta ebookMeta = new EbookMeta(title, author, decode);
            ebookMeta.setGenre(subject);
            return ebookMeta;

        } catch (Throwable e) {
            LOG.e(e);
            return EbookMeta.Empty();
        }
    }

    public static String getBookOverview(String path) {
        String info = "";
        try {
            File file = new File(path);
            byte[] raw = IOUtils.toByteArray(new FileInputStream(file));

            parse = new MobiParser(raw);
            info = parse.getDescription();

        } catch (Throwable e) {
            LOG.e(e);
        }
        return info;
    }

    public static byte[] getBookCover(String path) {
        try {
            File file = new File(path);
            byte[] raw = IOUtils.toByteArray(new FileInputStream(file));
            parse = new MobiParser(raw);
            return parse.getCoverOrThumb();
        } catch (Throwable e) {
            LOG.e(e);
        }
        return null;
    }

}
