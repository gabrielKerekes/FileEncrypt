import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoClass
{
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public static void encrypt(byte[] key, File inputFile, File outputFile) throws CryptoException
    {
        try
        {
            Key secretKey = new SecretKeySpec(key, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            IvParameterSpec ivParams = createIv(cipher);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);

            byte[] inputBytes = FileUtils.readBytesFromFile(inputFile);

            byte[] outputBytes = cipher.doFinal(inputBytes);
            byte[] iv = ivParams.getIV();
            byte[] hmac = createHmac(key, outputBytes);

            byte[] outputBytesWithIvAndHmac = new byte[outputBytes.length + iv.length + hmac.length];
            System.arraycopy(outputBytes, 0, outputBytesWithIvAndHmac, 0, outputBytes.length);
            System.arraycopy(iv, 0, outputBytesWithIvAndHmac, outputBytes.length, iv.length);
            System.arraycopy(hmac, 0, outputBytesWithIvAndHmac, outputBytes.length + iv.length, hmac.length);

            FileUtils.writeBytesToFile(outputFile, outputBytesWithIvAndHmac);
        }
        catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException ex)
        {
            throw new CryptoException("Error encrypting file", ex);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void decrypt(byte[] key, File inputFile, File outputFile) throws CryptoException
    {
        try
        {
            byte[] inputBytes = FileUtils.readBytesFromFile(inputFile);

            byte[] hmac = Arrays.copyOfRange(inputBytes, inputBytes.length - 32, inputBytes.length);
            byte[] iv = Arrays.copyOfRange(inputBytes, inputBytes.length - 32 - 16, inputBytes.length - 32);
            byte[] inputBytesNoHmac = Arrays.copyOfRange(inputBytes, 0, inputBytes.length - 32 - 16);

            byte[] calculatedHmac = createHmac(key, inputBytesNoHmac);

            if (MessageDigest.isEqual(hmac, calculatedHmac))
            {
                Key secretKey = new SecretKeySpec(key, ALGORITHM);
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);

                IvParameterSpec ivParams = new IvParameterSpec(iv);

                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);

                byte[] outputBytes = cipher.doFinal(inputBytesNoHmac);

                FileUtils.writeBytesToFile(outputFile, outputBytes);
            }
            else
            {
                System.out.println("Data boli zmenene. PRERUŠENÉ.");
            }
        }
        catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException ex)
        {
            throw new CryptoException("Error decrypting file", ex);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static byte[] createHmac(byte[] key, byte[] outputBytes)
    {
        try
        {
            Key secretKeyForHmacEncrypt = new SecretKeySpec(key, ALGORITHM);
            Mac m = Mac.getInstance("HmacSHA256");
            m.init(secretKeyForHmacEncrypt);

            return m.doFinal(outputBytes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static IvParameterSpec createIv(Cipher cipher)
    {
        try
        {
            SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG"); //maybe rather new SecureRandom();
            byte[] iv = new byte[cipher.getBlockSize()];
            randomSecureRandom.nextBytes(iv);

            return new IvParameterSpec(iv);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
