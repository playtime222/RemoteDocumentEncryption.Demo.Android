package nl.rijksoverheid.rdw.rde.clientlib.messaging.zipV2;

import com.google.gson.Gson;

import nl.rijksoverheid.rdw.rde.clientlib.messaging.*;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;






public class ZipMessageEncoder implements Closeable, MessageEncoder
{
    public static final String Version = "1.0.0";
    public static final String VersionEntryName = "R_1_1";
    public static final String VersionGmacEntryName = "AT_1";

    //JSON Not encrypted
    public static final String RdeSessionArgsEntryName = "R_2_1";
    public static final String RdeSessionArgsGmacEntryName = "AT_2";

    public static final String NoteEntryName = "R_3_1";
    public static final String NoteGmacEntryName = "AT_3";

    public static final String MetadataEntryName = "R_4_1";
    public static final String MetadataGmacEntryName = "AT_4";

    private final Gson gson = new Gson();
    private Cipher messageCipher;
    private SecretKey secretKey;
    private byte[] iv;

    private final ByteArrayOutputStream result;
    private final ZipOutputStream zipStream;

    public ZipMessageEncoder()
    {
        result = new ByteArrayOutputStream();
        zipStream = new ZipOutputStream(result);
    }

    private byte[] generateIv() {
        var result = new byte[16];
        new SecureRandom().nextBytes(result);
        this.iv = result;
        return result;
    }

    private int fileCounter = 4; //-> First one is R_5_1
    //TODO private int filePartCounter...
    private String nextEntryName() {fileCounter++; return String.format("R_%d_1", fileCounter);}
    private String gmacEntryName() {return String.format("A_%d", fileCounter);}

    //TODO version marker entry
    public byte[] encode(final MessageContentArgs messageArgs, final RdeSessionArgs rdeSessionArgs, final SecretKey secretKey)
            throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException
    {
        this.secretKey = secretKey;
        rdeSessionArgs.setIv(generateIv());
        messageCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        messageCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(rdeSessionArgs.getIv()));

        writePlain(VersionEntryName, VersionGmacEntryName, Version.getBytes(StandardCharsets.UTF_8));
        writePlain(NoteEntryName,NoteGmacEntryName, messageArgs.getUnencryptedNote().getBytes(StandardCharsets.UTF_8));
        var json = gson.toJson(rdeSessionArgs);
        writePlain(RdeSessionArgsEntryName, RdeSessionArgsGmacEntryName, json.getBytes(StandardCharsets.UTF_8));

        var filenames = new ArrayList<String>();
        for (var i = 0; i < messageArgs.getFileArgs().length; i++)
        {
            var item = messageArgs.getFileArgs()[i];
            filenames.add(item.getName());
            writeEncrypted(item.getContent());
        }

        var metadata = new Metadata();
        var v = filenames.toArray(new String[filenames.size()]);
        metadata.setFilenames(v);
        writeEncrypted(MetadataEntryName, MetadataGmacEntryName, gson.toJson(metadata).getBytes(StandardCharsets.UTF_8));
        return result.toByteArray();
    }

    @Override
    public void close() throws IOException {
        zipStream.close();
        result.close();
    }

    private void writePlain(final String entryName, final String gmacEntryName, final byte[] content) throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        write(entryName, content);
        writeGmac(gmacEntryName, content);
    }

    private void writeEncrypted(final byte[] content) throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        var e = nextEntryName();
        writeEncrypted(e, gmacEntryName(), content);
    }

    private void writeEncrypted(final String entryName, final String gmacEntryName, final byte[] content) throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        write(entryName, messageCipher.doFinal(content));
        writeGmac(gmacEntryName, content);
    }

    private void writeGmac(final String entryName, final byte[] content) throws IOException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        final var digestCipher = Cipher.getInstance("AES/GCM/NoPadding");
        digestCipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(iv.length * 8, iv), new SecureRandom());
        digestCipher.updateAAD(content);
        write(entryName, digestCipher.doFinal());
    }

    private void write(final String entryName, final byte[] content) throws IOException
    {
        var entry = new ZipEntry(entryName);
        zipStream.putNextEntry(entry);
        zipStream.write(content);
        zipStream.closeEntry();
    }
}
