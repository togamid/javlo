package org.javlo.ztatic;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.io.TransactionFile;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;
import org.javlo.service.exception.ServiceException;
import org.javlo.service.syncro.SynchroHelper;

/**
 * this class is used for cache transformation of a file. The transformation is
 * is identified from a key.
 * 
 * @author pvandermaesen
 * 
 */
public class FileCache {

	private static Logger logger = Logger.getLogger(FileCache.class.getName());

	private static final String KEY = FileCache.class.getName();
	public static final String BASE_DIR = "/tmp";

	public static final Object PDF_LOCK = new Object();

	private String baseDirName;

	ServletContext application = null;

	File baseDir = null;

	private FileCache(ServletContext inApplication) {
		application = inApplication;
		application.setAttribute(KEY, this);

		StaticConfig staticConfig = StaticConfig.getInstance(application);
		baseDirName = staticConfig.getImageCacheFolder();
		String realCacheFolder;
		if (!baseDirName.startsWith("/")) {
			baseDirName = "/" + baseDirName; // tomcat 8
			realCacheFolder = ResourceHelper.getRealPath(application, baseDirName);
		} else {
			realCacheFolder = baseDirName;
		}
		if (realCacheFolder == null) {
			throw new RuntimeException(baseDirName + " can't be converted to absolute path.");
		}
		try {
			baseDir = new File(realCacheFolder).getCanonicalFile();
			baseDir.mkdirs();
		} catch (IOException e) {
			logger.severe("problem create baseDir width : " + baseDirName);
			e.printStackTrace();
		}

		File dir = getCacheDir();
		if (!dir.exists()) {
			logger.info("create dir : " + dir);
			dir.mkdirs();
		}
	}

	/**
	 * create a instance of FileCache
	 * 
	 * @param inApplication
	 *            the ServletContext
	 * @return a instance in application scope.
	 */
	public synchronized static final FileCache getInstance(ServletContext inApplication) {
		FileCache fc = (FileCache) inApplication.getAttribute(KEY);
		if (fc == null) {
			fc = new FileCache(inApplication);
			inApplication.setAttribute(KEY, fc);
		}
		return fc;
	}

	private String getRelativeFilePath(String key, String fileName) {
		fileName = fileName.replace('\\', '/');
		String cacheFileName = baseDirName + '/' + key;
		if (fileName.startsWith("/")) {
			cacheFileName = cacheFileName + fileName;
		} else {
			cacheFileName = cacheFileName + '/' + fileName;
		}
		return cacheFileName;
	}

	public File getFileName(String key, String fileName) {
		fileName = fileName.replace('\\', '/');
		String cacheFileName = baseDir.getAbsolutePath() + '/' + key;
		if (fileName.startsWith("/")) {
			cacheFileName = cacheFileName + fileName;
		} else {
			cacheFileName = cacheFileName + '/' + fileName;
		}
		return new File(cacheFileName);
	}

	/**
	 * put file in cache. if this file exist old file is replace.
	 * 
	 * @param key
	 *            the key of the file.
	 * @param fileName
	 *            the complete name of the file.
	 * @param in
	 *            the data of the file.
	 * @throws IOException
	 */
	public void saveFile(String key, String fileName, InputStream in) throws IOException {
		File file = getFileName(key, fileName);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);

		try {
			int available = in.available();
			byte[] buffer = new byte[available];
			int read = in.read(buffer);
			while (read > 0) {
				out.write(buffer);
				available = in.available();
				buffer = new byte[available];
				read = in.read(buffer);
			}
		} finally {
			out.close();
		}
	}

	/**
	 * put file in cache. if this file exist old file is replace.
	 * 
	 * @param key
	 *            the key of the file.
	 * @param fileName
	 *            the complete name of the file.
	 * @param in
	 *            the data of the file.
	 * @throws IOException
	 */
	public static void saveFile(String fileName, InputStream in) throws IOException {
		File file = new File(fileName);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		OutputStream out = new FileOutputStream(file);
		try {
			int available = in.available();
			byte[] buffer = new byte[available];
			int read = in.read(buffer);
			while (read > 0) {
				out.write(buffer);
				available = in.available();
				buffer = new byte[available];
				read = in.read(buffer);
			}
		} finally {
			out.close();
		}
	}

	/**
	 * put file in cache. if this file exist old file is replace.
	 * 
	 * @param key
	 *            the key of the file.
	 * @param fileName
	 *            the complete name of the file.
	 * @return a outstream to the new file.
	 * @throws IOException
	 */
	public OutputStream saveFile(String key, String fileName) throws IOException {
		File file = getFileName(key, fileName);
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (Exception e) {
				logger.warning("error on create file : " + file);
				throw new IOException(e);
			}
		}
		OutputStream out = new FileOutputStream(file);
		return out;
	}

	public TransactionFile saveFileTransactional(String key, String fileName) throws IOException {
		File file = getFileName(key, fileName);
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
			} catch (Exception e) {
				logger.warning("error on create file : " + file);
				throw new IOException(e);
			}
		}
		return new TransactionFile(file);
	}

	/**
	 * get a file in the cache, return null if file is'nt in the cache.
	 * 
	 * @param key
	 *            the key of caching
	 * @param fileName
	 *            the file name
	 * @return a inputStream from file.
	 * @throws FileNotFoundException
	 */
	public InputStream getFileInputStream(String key, String fileName, long latestModificationDate) throws IOException {
		File file = getFileName(key, fileName).getCanonicalFile();

		if (latestModificationDate > file.lastModified()) {
			return null;
		}
		if (!file.exists() || file.isDirectory()) {
			return null;
		} else {
			return new FileInputStream(file);
		}
	}

	public File getFile(String key, String fileName, long latestModificationDate) throws IOException {
		File file = getFileName(key, fileName);
		if (latestModificationDate > file.lastModified()) {
			return null;
		}
		if (!file.exists() || file.isDirectory()) {
			return null;
		} else {
			return file;
		}
	}

	/**
	 * get the last modified of a cached file.
	 * 
	 * @param key
	 *            the key of caching
	 * @param fileName
	 *            the file name
	 * @return a inputStream from file.
	 * @throws FileNotFoundException
	 */
	public long getLastModified(String key, String fileName) throws FileNotFoundException {
		File file = getFileName(key, fileName);
		if (!file.exists()) {
			logger.fine("file not found : " + file);
		}
		return file.lastModified();
	}

	protected File getCacheDir() {
		if (baseDir == null) {
			baseDir = new File(ResourceHelper.getRealPath(application, BASE_DIR));
		}
		return baseDir;
	}

	/**
	 * clear a file for all keys.
	 * 
	 * @param fileName
	 *            a file name.
	 */
	public void delete(ContentContext ctx, String fileName) {
		if (fileName == null) {
			return;
		}

		if (ctx != null && ctx.getGlobalContext().getDMZServerIntra() != null) {
			SynchroHelper.deletedRemoteCacheFile(ctx, fileName);
		}

		File cacheDir = getCacheDir();
		// File[] keys = cacheDir.listFiles(new DirectoryFilter());

		Collection<File> keys = ResourceHelper.getAllDirList(cacheDir);

		int deleted = 0;
		final String[] extension = new String[] {"webp", "jpg", "png"};
		for (File file : keys) {
			// File cacheFile = getFileName(file.getName(), fileName);
			File cacheFile = new File(URLHelper.mergePath(file.getAbsolutePath(), fileName));
			if (cacheFile.exists()) {
				deleted++;
				cacheFile.delete();
			}
			// filter can change extension
			for (String ext : extension) {
				cacheFile = new File(URLHelper.mergePath(file.getAbsolutePath(), fileName+"."+ext));
				if (cacheFile.exists()) {
					deleted++;
					cacheFile.delete();
				}
			}
		}

		logger.info("#files deleted : " + deleted);
	}
	
	public static void main(String[] args) {
		File file = new File("C:\\opt\\tomcat8 - mariepoppies\\temp\\image-cache\\localhost\\banner\\pc\\banner\\marie-poppies\\none\\static\\images\\fun\\lara-croft-tomb-raider-costume-wallpaper-3.jpg");
		System.out.println(file.exists());
	}

	/**
	 * delete all files with this fileName
	 * 
	 * @param fileName
	 * @param context
	 *            a context, if null all context is considered.
	 */
	public void deleteAllFile(String context, String fileName) {
		File cacheDir;
		if (context != null) {
			cacheDir = new File(getCacheDir().getAbsolutePath(), context);
		} else {
			cacheDir = getCacheDir();
		}
		for (File file : ResourceHelper.getAllFilesList(cacheDir)) {
			if (file.getName().equals(fileName)) {
				file.delete();
			}
		}
	}

	public void clear(String context) {
		File cacheDir;
		if (context != null) {
			cacheDir = new File(URLHelper.mergePath(getCacheDir().getAbsolutePath(), context));
		} else {
			cacheDir = getCacheDir();
		}
		try {
			FileUtils.deleteDirectory(cacheDir);
		} catch (IOException e) {
			e.printStackTrace();
		}

		File oldCacheDir = new File(ResourceHelper.getRealPath(application, "/_dc_cache"));
		try {
			FileUtils.deleteDirectory(oldCacheDir);
		} catch (IOException e) {
			e.printStackTrace();
		}

		cacheDir.mkdirs();
	}

	public void clear() {
		clear(null);
	}

	public void storeBean(String key, Serializable obj) throws IOException {
		String fileName = StringHelper.createFileName(key) + ".serial.xml";
		File file = new File(URLHelper.mergePath(getCacheDir().getAbsolutePath(), fileName));
		if (file.exists() && obj == null) {
			file.delete();
			return;
		}
		file.createNewFile();
		XMLEncoder encoder = new XMLEncoder(new FileOutputStream(file));
		try {
			encoder.writeObject(obj);
			encoder.flush();
		} finally {
			encoder.close();
		}
	}

	public Serializable loadBean(String key) throws ClassNotFoundException, IOException {
		String fileName = StringHelper.createFileName(key) + ".serial.xml";
		File file = new File(URLHelper.mergePath(getCacheDir().getAbsolutePath(), fileName));
		if (!file.exists()) {
			return null;
		}
		XMLDecoder decorder = new XMLDecoder(new FileInputStream(file));
		try {
			logger.fine("load bean : " + file);
			return (Serializable) decorder.readObject();
		} finally {
			decorder.close();
		}
	}

	public void clearPDF(ContentContext ctx) throws IOException {
		synchronized (PDF_LOCK) {
			File pdfCache = new File(URLHelper.mergePath(getCacheDir().getAbsolutePath(), ctx.getGlobalContext().getContextKey(), "pdf"));
			FileUtils.deleteDirectory(pdfCache);
		}
	}

	public File getPDFPage(ContentContext ctx, MenuElement page, boolean low) throws ServiceException {
		PersistenceService persistenceService = PersistenceService.getInstance(ctx.getGlobalContext());
		String fileName = "" + persistenceService.getVersion();
		if (ctx.isAsViewMode() && ctx.getGlobalContext().isPreviewMode()) {
			fileName = "page";
		}
		if (low) {
			fileName = fileName + "_low";
		}
		File cacheFile = new File(URLHelper.mergePath(getCacheDir().getAbsolutePath(), ctx.getGlobalContext().getContextKey(), "pdf", ctx.getRenderModeKey(ctx.getRenderMode()), page.getName(), ctx.getRequestContentLanguage(), fileName + ".pdf"));
		File dir = cacheFile.getParentFile();
		if (!cacheFile.exists()) {
			dir.mkdirs();
			synchronized (PDF_LOCK) {
				for (File f : dir.listFiles()) {
					f.delete();
				}
			}
		}
		return cacheFile;
	}
}