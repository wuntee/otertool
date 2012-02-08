package com.wuntee.oter.smali;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.apache.log4j.Logger;
import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;
import org.jf.smali.LexerErrorInterface;
import org.jf.smali.smaliFlexLexer;
import org.jf.smali.smaliParser;
import org.jf.smali.smaliTreeWalker;
import org.jf.util.IndentingWriter;

import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;

import com.wuntee.oter.OterStatics;
import com.wuntee.oter.OterWorkshop;
import com.wuntee.oter.command.TerminatingCommand;
import com.wuntee.oter.exception.BuildApkException;
import com.wuntee.oter.exception.SmaliDexException;
import com.wuntee.oter.exception.SmaliSyntaxException;
import com.wuntee.oter.security.tools.JarSigner;

public class SmaliWorkshop {
	private static Logger logger = Logger.getLogger(SmaliWorkshop.class);
	
	public static String CLASSTYPE_HEAD = "type_id_item: L";
		
	public static String classDefItemToFilename(ClassDefItem c){
		String ct = c.getClassType().toString();
		String ret = ct.substring(CLASSTYPE_HEAD.length(), (ct.length()-1)).replace("/", ".");
		return(ret);
	}
	
	public static File createSmaliClassFile(File tmpDir, ClassDefItem c) throws IOException{
		String ct = c.getClassType().toString();
		String fullPath = tmpDir.getAbsolutePath() + System.getProperty("file.separator") + ct.substring(CLASSTYPE_HEAD.length(), (ct.length()-1)) + ".smali";
		logger.debug("Full path: " + fullPath);
		String dirPath = fullPath.substring(0, fullPath.lastIndexOf(System.getProperty("file.separator")));
		logger.debug("Creating directory: " + dirPath);
		File dir = OterWorkshop.createDirectoryRecursive(dirPath);
		File ret = new File(fullPath);
		return(ret);
	}
	
	public static Map<String, File> getSmaliSource(File sourceSmaliOrDexFile, File destinationDirectory) throws IOException{
		Map<String, File> ret = new HashMap<String, File>();
		
		DexFile dexFile = new DexFile(sourceSmaliOrDexFile);
		
		IndentingWriter idWriter;

        for(ClassDefItem c : dexFile.ClassDefsSection.getItems()){
        	
        	File classFile = SmaliWorkshop.createSmaliClassFile(destinationDirectory, c);
        	String className = SmaliWorkshop.classDefItemToFilename(c);
        	logger.debug("Got class: " + className + " [" + classFile + "]");
        	
            BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(classFile)));
            idWriter = new IndentingWriter(fileWriter);
                
        	ClassDefinition cd = new ClassDefinition(c);
        	cd.writeTo(idWriter);
        	
        	ret.put(className, classFile);
        	
        	idWriter.close();
        }
        
		return(sortMapByKey(ret));
	}

	
	private static Map<String, File> sortMapByKey(Map<String, File> aItems){
	    TreeMap<String, File> result = new TreeMap<String, File>(String.CASE_INSENSITIVE_ORDER);
	    result.putAll(aItems);
	    return result;
	 }
	
	public static void buildApk(File sourceApk, File smaliSourceDirectory, File destinationApkFile) throws BuildApkException{
		try{
			// Unzip the current APK
			File apkDirectory = OterWorkshop.createTemporaryDirectory("aat.newapk");
			OterWorkshop.unzipArchive(sourceApk, apkDirectory);
			
			// Remove META-INF directory
			File metaInfDirectory = new File(apkDirectory.getAbsolutePath() + System.getProperty("file.separator") + OterStatics.META_INF);
			if(metaInfDirectory.exists() && metaInfDirectory.isDirectory()){
				logger.info("Deleting: " + metaInfDirectory.getAbsolutePath());
				for(File f : metaInfDirectory.listFiles()){
					f.delete();
				}
			} else {
				logger.warn("META-INF directory did not exist");
			}
			
			// Create META-INF directory
			logger.debug(metaInfDirectory.getAbsoluteFile());
			metaInfDirectory.mkdir();
			
			// Build the current dex file
			File destinationDexFile = new File(apkDirectory.getAbsoluteFile() + System.getProperty("file.separator") + OterStatics.CLASSES_DEX);
			logger.debug("Building dex file: " + destinationDexFile.getAbsolutePath());
			buildDex(smaliSourceDirectory, destinationDexFile);
					
			// Zip the modified APK directory
			logger.debug("Ziping archive to: " + destinationApkFile.getAbsolutePath());
			OterWorkshop.zipArchive(destinationApkFile, apkDirectory);
			
			// Remove temporary new APK directory
			//apkDirectory.delete();
		} catch (Exception e){
			logger.error("Could not build apk: ", e);
			throw new BuildApkException(e.getMessage());
		}
	}
	
	private static void buildDex(File smaliSourceDirectory, File destination) throws RecognitionException, SmaliSyntaxException, SmaliDexException, IOException{
		DexFile dexFile = new DexFile();

		// Load files into set
		logger.debug("Loading smali files");
		LinkedHashSet<File> filesToProcess = new LinkedHashSet<File>();
		getSmaliFilesInDir(smaliSourceDirectory, filesToProcess);

		// Process each file
		logger.debug("Processing files");
		for (File file : filesToProcess) {
			logger.debug("Processing: " + file.getAbsolutePath());
			FileInputStream fis = new FileInputStream(file.getAbsolutePath());
			InputStreamReader reader = new InputStreamReader(fis, "UTF-8");

			LexerErrorInterface lexer = new smaliFlexLexer(reader);
			((smaliFlexLexer) lexer).setSourceFile(file);
			CommonTokenStream tokens = new CommonTokenStream((TokenSource) lexer);
			smaliParser parser = new smaliParser(tokens);
			smaliParser.smali_file_return result = parser.smali_file();

			// Errors
			if (parser.getNumberOfSyntaxErrors() > 0) {
				throw new SmaliSyntaxException(file, parser.getNumberOfSyntaxErrors());
			}
			if (lexer.getNumberOfSyntaxErrors() > 0) {
				throw new SmaliSyntaxException(file, lexer.getNumberOfSyntaxErrors());
			}
			CommonTree t = (CommonTree) result.getTree();

			CommonTreeNodeStream treeStream = new CommonTreeNodeStream(t);
			treeStream.setTokenStream(tokens);

			smaliTreeWalker dexGen = new smaliTreeWalker(treeStream);

			dexGen.dexFile = dexFile;
			dexGen.smali_file();

			if (dexGen.getNumberOfSyntaxErrors() > 0) {
				throw new SmaliDexException(file, dexGen.getNumberOfSyntaxErrors());
			}
		}

		// Calculate signatures and write file
		logger.debug("Writing dex file.");
		dexFile.place();
		ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput();

		dexFile.writeTo(out);

		byte[] bytes = out.toByteArray();

		DexFile.calcSignature(bytes);
		DexFile.calcChecksum(bytes);

		FileOutputStream fileOutputStream = new FileOutputStream(destination);

		fileOutputStream.write(bytes);
		fileOutputStream.close();
	}
	
	private static void getSmaliFilesInDir(File dir, Set<File> smaliFiles) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				getSmaliFilesInDir(file, smaliFiles);
			} else if (file.getName().endsWith(".smali")) {
				smaliFiles.add(file);
			}
		}
	}
	
	public static void signJar(String keystore, String keystorePassword, String jarFile, String alias) throws Exception{
		// TODO: make this programatic, vs calling jarsigner
		//JarSigner js = new JarSigner();
		//js.signJar(keystore, keystorePassword, jarFile, alias);
		//JarSigner.sign(new File(jarFile), keystore, alias, OterStatics.SOME_STRING.toCharArray(), keystorePassword.toCharArray());
		
		//jarsigner -keystore somestore.ks modified-application.apk some-key-name -storepass
		
		
		TerminatingCommand c = new TerminatingCommand(new String[]{"jarsigner", "-keystore", keystore, "-storepass", keystorePassword, jarFile, alias});
		int ret = c.execute();
		if(ret != 0){
			String output = "";
			for(String o: c.getOutput()){
				output = output + o + "\n";
			}
			throw new Exception("Could not sign jar, jarsigner returned:\n" + output);
		}
	}
	
	public static KeyStore createKeystoreWithSecretKey(String alias) throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, CertificateException, IOException, InvalidKeyException, SignatureException {
		KeyStore ret = KeyStore.getInstance(KeyStore.getDefaultType());
		ret.load(null);
		
        int keysize = 1024;
        int validity = 10000;
        String keyAlgName = "DSA";
        String sigAlgName = "SHA1WithDSA";

        CertAndKeyGen keypair = new CertAndKeyGen(keyAlgName, sigAlgName, null);

        X500Name x500Name = new X500Name(OterStatics.SOME_STRING, OterStatics.SOME_STRING, OterStatics.SOME_STRING, OterStatics.SOME_STRING, OterStatics.SOME_STRING, OterStatics.SOME_STRING);

        keypair.generate(keysize);
        PrivateKey privKey = keypair.getPrivateKey();

        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = keypair.getSelfCertificate(x500Name, (long) validity * 24 * 60 * 60);

        ret.setKeyEntry(alias, privKey, OterStatics.SOME_STRING.toCharArray(), chain);
        
        return(ret);
	}
	
	public static File writeKeystoreToTemporaryFile(KeyStore ks, String password) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException{
		File tmp = File.createTempFile(OterStatics.TEMP_PREFIX, "keystore");
		
		logger.debug("Writing keystore to: " + tmp.getAbsolutePath());
		
		FileOutputStream out = new FileOutputStream(tmp);
		ks.store(out, password.toCharArray());
		
		return(tmp);
	}

}
