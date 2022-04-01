package nl.rijksoverheid.rdw.rde.clientlib.messaging.zipV2;

import com.google.gson.Gson;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipInputStream;

import nl.rijksoverheid.rdw.rde.clientlib.messaging.*;

//@Service
public class ZipMessageDecoder
{
    private String version;
    private RdeSessionArgs rdeSessionArgs;
    private final Gson gson = new Gson();
    private Cipher messageCipher;
    private byte[] message;
    private String rdeSessionArgsJson;

    final private ArrayList<MessageFile> files = new ArrayList<>();
    private SecretKey secretKey;

    public RdeSessionArgs decodeRdeSessionArgs(byte[] message) throws IOException
    {
        if (this.message != null) throw new IllegalStateException();
        this.message = message;

        this.version = readPlainTextString(ZipMessageEncoder.VersionEntryName);
        if (!version.equals(ZipMessageEncoder.Version))
            throw new IllegalArgumentException("Version not supported.");

        this.rdeSessionArgsJson = readPlainTextString(ZipMessageEncoder.RdeSessionArgsEntryName);
        this.rdeSessionArgs = gson.fromJson(rdeSessionArgsJson, RdeSessionArgs.class);
        return rdeSessionArgs;
    }

    public MessageV2 decode(final SecretKey secretKey)
            throws IOException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException
    {
        if (this.message == null) throw new IllegalStateException();
        if (this.messageCipher != null) throw new IllegalStateException();

        this.secretKey = secretKey;

        messageCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        messageCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(rdeSessionArgs.getIv()));

        verifyPlainTextString(this.version, ZipMessageEncoder.VersionGmacEntryName);
        verifyPlainTextString(this.rdeSessionArgsJson, ZipMessageEncoder.RdeSessionArgsGmacEntryName);
        final var note = readPlainTextString(ZipMessageEncoder.NoteEntryName);
        verifyPlainTextString(note, ZipMessageEncoder.NoteGmacEntryName);


        final var metadataJsonBytes = readAndVerify(ZipMessageEncoder.MetadataEntryName, ZipMessageEncoder.MetadataGmacEntryName);
        final var json = new String(metadataJsonBytes, StandardCharsets.UTF_8);
        final var metadata = gson.fromJson(json, Metadata.class);

        for (var i = 0; i < metadata.getFilenames().length; i++)
        {
            var entryName = nextEntryName();
            files.add(new MessageFile(metadata.getFilenames()[i], readAndVerify(entryName, gmacEntryName())));
        }

        return new MessageV2(note, rdeSessionArgs, files.toArray(new MessageFile[0]));
    }

    private byte[] readAndVerify(String entryName, String gmacEntryName) throws IOException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        final var cipherText = readEntry(entryName);
        var result = this.messageCipher.doFinal(cipherText);
        verify(result, gmacEntryName);
        return result;
    }

    private int fileCounter = 4; //-> First one is R_5_1
    private String nextEntryName() {fileCounter++; return String.format(Locale.ROOT, "R_%d_1", fileCounter); }
    private String gmacEntryName() {return String.format(Locale.ROOT, "A_%d", fileCounter);}

    private String readPlainTextString(String entryName) throws IOException {
        return new String(readEntry(entryName), StandardCharsets.UTF_8);
    }

    private void verifyPlainTextString(String value, String entryName) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        verify(value.getBytes(StandardCharsets.UTF_8), entryName);
    }

    private void verify(byte[] value, String gmacEntryName) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        final var gmac = readEntry(gmacEntryName);
        final var decryptCipher = Cipher.getInstance("AES/GCM/NoPadding");
        decryptCipher.init(Cipher.DECRYPT_MODE, this.secretKey, new GCMParameterSpec(rdeSessionArgs.getIv().length * 8, rdeSessionArgs.getIv()), new SecureRandom());
        decryptCipher.updateAAD(value);
        decryptCipher.update(gmac);
        decryptCipher.doFinal(); //Throws if bad gmac
    }

    private byte[] readEntry(final String name) throws IOException
    {
        try(final var input = new ByteArrayInputStream(this.message))
        {
            try(final var stream = new ZipInputStream(input))
            {
                var zipEntry = stream.getNextEntry();
                while (zipEntry != null)
                {
                    if (zipEntry.getName().equals(name))
                    {
                        final var buffer = new byte[2048];
                        final var result = new ByteArrayOutputStream();
                        int len = stream.read(buffer);
                        while (len > 0)
                        {
                            result.write(buffer, 0, len);
                            len = stream.read(buffer);
                        }
                        return result.toByteArray();
                    }
                    zipEntry = stream.getNextEntry();
                }
                throw new IllegalStateException("Entry not found.");
            }
        }
    }
}