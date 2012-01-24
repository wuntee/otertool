/*
 * Copyright 2004 - 2007 University of Cardiff.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wuntee.oter.smali;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.KeySpec;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import sun.misc.BASE64Encoder;
import sun.security.util.ManifestDigester;

/**
 * works for Java 1.5.
 *
 * @author Andrew Harrison
 * @version $Revision: 131 $
 * @created Mar 15, 2007: 2:19:53 PM
 * @date $Date: 2007-04-06 15:31:20 +0100 (Fri, 06 Apr 2007) $ modified by $Author: scmabh $
 * @todo Put your notes here...
 */


public class JarSigner15 {
	private static Logger logger = Logger.getLogger(JarSigner15.class);
	
    // the alias for the signing key, the private key to sign with,
    // and the certificate chain
    private String alias;
    private PrivateKey privateKey;
    private X509Certificate[] certChain;

    public JarSigner15(String alias, PrivateKey privateKey, X509Certificate[] certChain) {
        this.alias = alias;
        this.privateKey = privateKey;
        this.certChain = certChain;
    }

    // retrieve the manifest from a jar file -- this will either

    // load a pre-existing META-INF/MANIFEST.MF, or create a new
    // one

    private static Manifest getManifestFile(JarFile jarFile)
            throws IOException {
        JarEntry je = jarFile.getJarEntry("META-INF/MANIFEST.MF");
        if (je != null) {
            Enumeration entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                je = (JarEntry) entries.nextElement();
                if ("META-INF/MANIFEST.MF".equalsIgnoreCase(je.getName()))
                    break;
                else
                    je = null;
            }
        }
        // create the manifest object
        Manifest manifest = new Manifest();
        if (je != null)
            manifest.read(jarFile.getInputStream(je));
        return manifest;

    }

    // given a manifest file and given a jar file, make sure that
    // the contents of the manifest file is correct and return a
    // map of all the valid entries from the manifest

    private static Map pruneManifest(Manifest manifest, JarFile jarFile)
            throws IOException {
        Map map = manifest.getEntries();
        Iterator elements = map.keySet().iterator();
        while (elements.hasNext()) {
            String element = (String) elements.next();
            if (jarFile.getEntry(element) == null)
                elements.remove();

        }
        return map;

    }

    // make sure that the manifest entries are ready for the signed
    // JAR manifest file. if we already have a manifest, then we
    // make sure that all the elements are valid. if we do not
    // have a manifest, then we create a new signed JAR manifest
    // file by adding the appropriate headers

    private static Map createEntries(Manifest manifest, JarFile jarFile)
            throws IOException {
        Map entries = null;
        if (manifest.getEntries().size() > 0)
            entries = pruneManifest(manifest, jarFile);

        else {
            // if there are no pre-existing entries in the manifest,
            // then we put a few default ones in
            Attributes attributes = manifest.getMainAttributes();
            attributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
            attributes.putValue("Created-By", System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
            entries = manifest.getEntries();
        }
        return entries;
    }

    // helper function to update the digest
    private static BASE64Encoder b64Encoder = new BASE64Encoder();

    private static String updateDigest(MessageDigest digest, InputStream inputStream)
            throws IOException {
        byte[] buffer = new byte[2048];
        int read = 0;
        while ((read = inputStream.read(buffer)) > 0)
            digest.update(buffer, 0, read);
        inputStream.close();

        return b64Encoder.encode(digest.digest());

    }

    // update the attributes in the manifest to have the
    // appropriate message digests. we store the new entries into

    // the entries Map and return it (we do not compute the digests
    // for those entries in the META-INF directory)

    private static Map updateManifestDigest(Manifest manifest, JarFile jarFile, MessageDigest messageDigest, Map entries)
            throws IOException {
        Enumeration jarElements = jarFile.entries();
        while (jarElements.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) jarElements.nextElement();
            if (jarEntry.getName().startsWith("META-INF"))
                continue;

            else if (manifest.getAttributes(jarEntry.getName()) != null) {
                // update the digest and record the base 64 version of
                // it into the attribute list
                Attributes attributes = manifest.getAttributes(jarEntry.getName());
                attributes.putValue("SHA1-Digest", updateDigest(messageDigest, jarFile.getInputStream(jarEntry)));

            } else if (!jarEntry.isDirectory()) {
                // store away the digest into a new Attribute
                // because we don't already have an attribute list
                // for this entry. we do not store attributes for
                // directories within the JAR
                Attributes attributes = new Attributes();
                attributes.putValue("SHA1-Digest", updateDigest(messageDigest, jarFile.getInputStream(jarEntry)));
                entries.put(jarEntry.getName(), attributes);

            }

        }
        return entries;
    }

    // a small helper function that will convert a manifest into an
    // array of bytes
    private byte[] serialiseManifest(Manifest manifest)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        manifest.write(baos);
        baos.flush();
        baos.close();
        return baos.toByteArray();

    }

    // create a signature file object out of the manifest and the
    // message digest
    private SignatureFile createSignatureFile(Manifest manifest, MessageDigest messageDigest)
            throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        // construct the signature file and the signature block for
        // this manifest
        ManifestDigester manifestDigester = new ManifestDigester(serialiseManifest(manifest));
        return new SignatureFile(new MessageDigest[]{messageDigest}, manifest, manifestDigester, this.alias, true);

    }

    // a helper function that can take entries from one jar file and
    // write it to another jar stream
    private static void writeJarEntry(JarEntry je, JarFile jarFile, JarOutputStream jos)
            throws IOException {
        jos.putNextEntry(je);
        byte[] buffer = new byte[2048];
        int read = 0;
        InputStream is = jarFile.getInputStream(je);
        while ((read = is.read(buffer)) > 0)
            jos.write(buffer, 0, read);
        jos.closeEntry();

    }

    // the actual JAR signing method -- this is the method which
    // will be called by those wrapping the JARSigner class
    public void signJarFile(JarFile jarFile, OutputStream outputStream)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, CertificateException, InstantiationException, ClassNotFoundException {

        // calculate the necessary files for the signed jAR

        // get the manifest out of the jar and verify that
        // all the entries in the manifest are correct
        Manifest manifest = getManifestFile(jarFile);
        Map entries = createEntries(manifest, jarFile);

        // create the message digest and start updating the
        // the attributes in the manifest to contain the SHA1
        // digests
        MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
        updateManifestDigest(manifest, jarFile, messageDigest, entries);

        // construct the signature file object and the
        // signature block objects
        SignatureFile signatureFile = createSignatureFile(manifest, messageDigest);
        SignatureFile.Block block = signatureFile.generateBlock(privateKey, certChain, true, jarFile);

        // start writing out the signed JAR file

        // write out the manifest to the output jar stream
        String manifestFileName = "META-INF/MANIFEST.MF";
        JarOutputStream jos = new JarOutputStream(outputStream);
        JarEntry manifestFile = new JarEntry(manifestFileName);
        jos.putNextEntry(manifestFile);
        byte manifestBytes[] = serialiseManifest(manifest);
        jos.write(manifestBytes, 0, manifestBytes.length);
        jos.closeEntry();

        // write out the signature file -- the signatureFile
        // object will name itself appropriately
        String signatureFileName = signatureFile.getMetaName();
        JarEntry signatureFileEntry = new JarEntry(signatureFileName);
        jos.putNextEntry(signatureFileEntry);
        signatureFile.write(jos);
        jos.closeEntry();

        // write out the signature block file -- again, the block
        // will name itself appropriately
        String signatureBlockName = block.getMetaName();
        JarEntry signatureBlockEntry = new JarEntry(signatureBlockName);
        jos.putNextEntry(signatureBlockEntry);
        block.write(jos);
        jos.closeEntry();

        // commit the rest of the original entries in the
        // META-INF directory. if any of their names conflict
        // with one that we created for the signed JAR file, then
        // we simply ignore it
        Enumeration metaEntries = jarFile.entries();
        while (metaEntries.hasMoreElements()) {
            JarEntry metaEntry = (JarEntry) metaEntries.nextElement();
            if (metaEntry.getName().startsWith("META-INF") &&
                    !(manifestFileName.equalsIgnoreCase(metaEntry.getName()) ||
                            signatureFileName.equalsIgnoreCase(metaEntry.getName()) ||
                            signatureBlockName.equalsIgnoreCase(metaEntry.getName())))
                writeJarEntry(metaEntry, jarFile, jos);

        }

        // now write out the rest of the files to the stream
        Enumeration allEntries = jarFile.entries();
        while (allEntries.hasMoreElements()) {
            JarEntry entry = (JarEntry) allEntries.nextElement();
            if (!entry.getName().startsWith("META-INF"))
                writeJarEntry(entry, jarFile, jos);

        }

        // finish the stream that we have been writing to
        jos.flush();
        jos.finish();

        // close the JAR file that we have been using
        jarFile.close();

    }


    private static Constructor findConstructor(Class c, Class... argTypes) throws NoSuchMethodException {
        Constructor ct = c.getDeclaredConstructor(argTypes);
        if (ct == null) {
            throw new RuntimeException(c.getName());
        }
        ct.setAccessible(true);
        return ct;
    }

    private static Method findMethod(Class c, String methodName, Class... argTypes) throws NoSuchMethodException {
        Method m = c.getDeclaredMethod(methodName, argTypes);
        if (m == null) {
            throw new RuntimeException(c.getName());
        }
        m.setAccessible(true);
        return m;
    }

    private class SignatureFile {

        private Object sigFile;

        private Class JDKsfClass;

        private Method getMetaNameMethod;
        private Method writeMethod;

        private static final String JDK_SIGNATURE_FILE = "sun.security.tools.SignatureFile";
        private static final String GETMETANAME_METHOD = "getMetaName";
        private static final String WRITE_METHOD = "write";

        public SignatureFile(MessageDigest digests[],
                             Manifest mf,
                             ManifestDigester md,
                             String baseName,
                             boolean signManifest) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

            JDKsfClass = getClass().forName(JDK_SIGNATURE_FILE);

            Constructor constructor = findConstructor(JDKsfClass,
                    MessageDigest[].class,
                    Manifest.class,
                    ManifestDigester.class,
                    String.class,
                    Boolean.TYPE);

            sigFile = constructor.newInstance(digests, mf, md, baseName, signManifest);

            getMetaNameMethod = findMethod(JDKsfClass, GETMETANAME_METHOD);
            writeMethod = findMethod(JDKsfClass, WRITE_METHOD, OutputStream.class);
        }

        public Block generateBlock(PrivateKey privateKey,
                                   X509Certificate[] certChain,
                                   boolean externalSF, ZipFile zipFile)
                throws NoSuchAlgorithmException, InvalidKeyException, IOException,
                SignatureException, CertificateException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            return new Block(this, privateKey, certChain, externalSF, zipFile);
        }

        public Class getJDKSignatureFileClass() {
            return JDKsfClass;
        }

        public Object getJDKSignatureFile() {
            return sigFile;
        }

        public String getMetaName() throws IllegalAccessException, InvocationTargetException {
            return (String) getMetaNameMethod.invoke(sigFile);
        }

        public void write(OutputStream os) throws IllegalAccessException, InvocationTargetException {
            writeMethod.invoke(sigFile, os);
        }

        private class Block {

            private Object block;

            private static final String JDK_BLOCK = JDK_SIGNATURE_FILE + "$Block";
            private static final String JDK_CONTENT_SIGNER = "com.sun.jarsigner.ContentSigner";

            private Method getMetaNameMethod;
            private Method writeMethod;

            public Block(SignatureFile sfg, PrivateKey privateKey,
                         X509Certificate[] certChain, boolean externalSF, ZipFile zipFile) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {

                Class blockClass = getClass().forName(JDK_BLOCK);

                Class contentSignerClass = getClass().forName(JDK_CONTENT_SIGNER);

                Constructor constructor = findConstructor(blockClass,
                        sfg.getJDKSignatureFileClass(),
                        PrivateKey.class,
                        X509Certificate[].class,
                        Boolean.TYPE,
                        String.class,
                        X509Certificate.class,
                        contentSignerClass,
                        String[].class,
                        ZipFile.class);

                getMetaNameMethod = findMethod(blockClass, GETMETANAME_METHOD);
                writeMethod = findMethod(blockClass, WRITE_METHOD, OutputStream.class);

                block = constructor.newInstance(
                        sfg.getJDKSignatureFile(), /* explicit argument on the constructor */
                        privateKey,
                        certChain,
                        externalSF,
                        null,
                        null,
                        null,
                        null,
                        zipFile);
            }

            public String getMetaName() throws IllegalAccessException, InvocationTargetException {
                return (String) getMetaNameMethod.invoke(block);
            }

            public void write(OutputStream os) throws IllegalAccessException, InvocationTargetException {
                writeMethod.invoke(block, os);
            }
        }
    }


    public static File sign(File jar, String newName, String keystoreLocation, String alias, char[] passwd, char[] keypasswd) throws Exception {

        FileInputStream fileIn = new FileInputStream(keystoreLocation);
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(fileIn, passwd);
        java.security.cert.Certificate[] chain = keyStore.getCertificateChain(alias);
        X509Certificate certChain[] = new X509Certificate[chain.length];

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        for (int count = 0; count < chain.length; count++) {
            ByteArrayInputStream certIn = new ByteArrayInputStream(chain[0].getEncoded());
            X509Certificate cert = (X509Certificate) cf.generateCertificate(certIn);
            certChain[count] = cert;
        }


        Key key = keyStore.getKey(alias, keypasswd);
        logger.debug("Algo: " + key.getAlgorithm());
        KeyFactory keyFactory = KeyFactory.getInstance(key.getAlgorithm());
        KeySpec keySpec = keyFactory.getKeySpec(key, DSAPrivateKeySpec.class);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        /*Key key = keyStore.getKey(alias, keypasswd);
        X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(bobEncodedPubKey);
        KeyFactory keyFactory = KeyFactory.getInstance(key.getAlgorithm());
        PublicKey bobPubKey = keyFactory.generatePublic(bobPubKeySpec);*/
        
        JarSigner15 jarSigner = new JarSigner15(alias, privateKey, certChain);

        JarFile jarFile = new JarFile(jar.getCanonicalPath());
        File newJar = new File(jar.getParentFile(), newName);
        OutputStream outStream = new FileOutputStream(newJar);
        jarSigner.signJarFile(jarFile, outStream);
        fileIn.close();
        return newJar;

    }

    public static File sign(File jar, String keystoreLocation, String alias, char[] passwd, char[] keypasswd) throws Exception {
        return sign(jar, "s-" + jar.getName(), keystoreLocation, alias, passwd, keypasswd);

    }

}