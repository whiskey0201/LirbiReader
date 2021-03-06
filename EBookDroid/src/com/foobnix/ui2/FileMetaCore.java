package com.foobnix.ui2;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.ebookdroid.BookType;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.foobnix.android.utils.LOG;
import com.foobnix.android.utils.TxtUtils;
import com.foobnix.dao2.FileMeta;
import com.foobnix.ext.CacheZipUtils;
import com.foobnix.ext.CacheZipUtils.UnZipRes;
import com.foobnix.ext.CalirbeExtractor;
import com.foobnix.ext.EbookMeta;
import com.foobnix.ext.EpubExtractor;
import com.foobnix.ext.Fb2Extractor;
import com.foobnix.ext.MobiExtract;
import com.foobnix.pdf.info.ExtUtils;

import android.app.Activity;
import android.support.v4.util.Pair;

public class FileMetaCore {
    private static FileMetaCore in = new FileMetaCore();

    public static FileMetaCore get() {
        return in;
    }

    public static void checkOrCreateMetaInfo(Activity a) {
        try {
            String path = a.getIntent().getData().getPath();
            LOG.d("checkOrCreateMetaInfo", path);
            if (new File(path).isFile()) {
                FileMeta fileMeta = AppDB.get().getOrCreate(path);
                if (TxtUtils.isEmpty(fileMeta.getTitle())) {

                    EbookMeta ebookMeta = FileMetaCore.get().getEbookMeta(path);

                    FileMetaCore.get().upadteBasicMeta(fileMeta, new File(path));
                    FileMetaCore.get().udpateFullMeta(fileMeta, ebookMeta);

                    AppDB.get().update(fileMeta);
                    LOG.d("checkOrCreateMetaInfo", "UPDATE", path);
                } else {
                    LOG.d("checkOrCreateMetaInfo", "LOAD", path);
                }
            }
        } catch (Exception e) {
            LOG.e(e);
        }
    }

    public EbookMeta getEbookMeta(String path) {
        EbookMeta ebookMeta = EbookMeta.Empty();
        try {
            if (path.toLowerCase(Locale.US).endsWith(".zip")) {
                CacheZipUtils.cacheLock.lock();
                try {
                    UnZipRes res = CacheZipUtils.extracIfNeed(path);
                    ebookMeta = getEbookMeta(path, res.unZipPath, res.entryName);
                    ebookMeta.setUnzipPath(res.unZipPath);
                } finally {
                    CacheZipUtils.cacheLock.unlock();
                }
            } else {
                ebookMeta = getEbookMeta(path, path, null);
                ebookMeta.setUnzipPath(path);
            }

        } catch (Exception e) {
            LOG.e(e);
        }
        return ebookMeta;

    }

    private EbookMeta getEbookMeta(String path, String unZipPath, String child) throws IOException {
        EbookMeta ebookMeta = EbookMeta.Empty();

        if (CalirbeExtractor.isCalibre(unZipPath)) {
            ebookMeta = CalirbeExtractor.getBookMetaInformation(unZipPath);
            LOG.d("isCalibre find");
        } else if (BookType.EPUB.is(unZipPath)) {
            ebookMeta = EpubExtractor.get().getBookMetaInformation(unZipPath);
        } else if (BookType.FB2.is(unZipPath)) {
            ebookMeta = Fb2Extractor.get().getBookMetaInformation(unZipPath);
        } else if (BookType.MOBI.is(unZipPath)) {
            ebookMeta = MobiExtract.getBookMetaInformation(unZipPath, true);
        }
        if (TxtUtils.isEmpty(ebookMeta.getTitle())) {
            Pair<String, String> pair = TxtUtils.getTitleAuthorByPath(ExtUtils.getFileName(unZipPath));
            ebookMeta = new EbookMeta(pair.first, pair.second);
        }

        if (ebookMeta.getsIndex() == null && (path.contains("_") || path.contains(")"))) {
            for (int i = 20; i >= 1; i--) {
                if (path.contains("_" + i + "_") || path.contains(" " + i + ")") || path.contains(" 0" + i + ")")) {
                    ebookMeta.setsIndex(i);
                    break;
                }
            }
        }

        if (ebookMeta.getsIndex() != null) {
            ebookMeta.setTitle(ebookMeta.getTitle() + " [" + ebookMeta.getsIndex() + "]");
        }

        if (path.endsWith(".zip") && !path.endsWith("fb2.zip")) {
            ebookMeta.setTitle("{" + ExtUtils.getFileName(path) + "} " + ebookMeta.getTitle());
        }

        return ebookMeta;
    }

    public static String getBookOverview(String path) {
        String info = "";
        try {

            if (CalirbeExtractor.isCalibre(path)) {
                return CalirbeExtractor.getBookOverview(path);
            }

            path = CacheZipUtils.extracIfNeed(path).unZipPath;

            if (BookType.EPUB.is(path)) {
                info = EpubExtractor.get().getBookOverview(path);
            } else if (BookType.FB2.is(path)) {
                info = Fb2Extractor.get().getBookOverview(path);
            } else if (BookType.MOBI.is(path)) {
                info = MobiExtract.getBookOverview(path);
            }
            info = Jsoup.clean(info, Whitelist.none());
            info = info.replace("&nbsp;", " ");
        } catch (Exception e) {
            LOG.e(e);
        }
        return info;
    }

    public void udpateFullMeta(FileMeta fileMeta, EbookMeta meta) {
        fileMeta.setAuthor(meta.getAuthor());
        fileMeta.setTitle(meta.getTitle());
        fileMeta.setSequence(meta.getSequence());
        fileMeta.setGenre(meta.getGenre());
        fileMeta.setAnnotation(meta.getAnnotation());
        fileMeta.setSIndex(meta.getsIndex());
        fileMeta.setChild(ExtUtils.getFileExtension(meta.getUnzipPath()));

    }

    public void upadteBasicMeta(FileMeta fileMeta, File file) {
        String path = file.getPath();

        fileMeta.setTitle(file.getName());// temp

        fileMeta.setSize(file.length());
        fileMeta.setDate(file.lastModified());

        fileMeta.setExt(ExtUtils.getFileExtension(path));
        fileMeta.setSizeTxt(ExtUtils.readableFileSize(file.length()));
        fileMeta.setDateTxt(ExtUtils.getDateFormat(file));
        fileMeta.setPathTxt(file.getName());
    }

}
