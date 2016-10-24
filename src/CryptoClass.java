import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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
    private static byte[] hmacForEncrypt;

    private static final String ALGORITHM = "AES";
    //private static final String TRANSFORMATION = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public static byte[] readBytesFromFile(File inputFile)
    {
        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            return inputBytes;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (inputStream != null)
                    inputStream.close();
            }
            catch (Exception e)
            {

            }
        }

        return null;
    }

    public static void writeBytesToFile(File outputFile, byte[] bytes)
    {
        FileOutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(outputFile);
            outputStream.write(bytes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (outputStream != null)
                    outputStream.close();
            }
            catch (Exception e)
            {

            }
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
            //randomSecureRandom.nextBytes(iv);
            iv = new byte[] { 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65};
            return new IvParameterSpec(iv);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static void encrypt(byte[] key, File inputFile, File outputFile) throws CryptoException
    {
        try
        {
            Key secretKey = new SecretKeySpec(key, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION); //if transformation was AES we would get error -- ECB mode cannot use IV

            //Kebyze pouzijeme transformaciu AES tak by sa zvolil blokovy mod ECB a nemohli by sme pridat IV
            //CBC is better for larger files
            //GCM does all the hard work, provides authenticated encryption (integrity)
            //Nemame ale pouzit hotove riesenia takze podla mna nemozeme GCM pouzit ale to CBC radsej a pouzijeme HMAC
            //http://security.stackexchange.com/questions/63132/when-to-use-hmac-alongside-aes
            //http://netnix.org/2015/04/19/aes-encryption-with-hmac-integrity-in-java/ --PRECITAT

            //implementing IV
            IvParameterSpec ivParams = createIv(cipher);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);

            byte[] inputBytes = readBytesFromFile(inputFile);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            byte[] hmac = createHmac(key, outputBytes);

            byte[] outputBytesWithHmac = new byte[outputBytes.length + hmac.length];
            System.arraycopy(outputBytes, 0, outputBytesWithHmac, 0, outputBytes.length);
            System.arraycopy(hmac, 0, outputBytesWithHmac, outputBytes.length, hmac.length);

            writeBytesToFile(outputFile, outputBytesWithHmac);
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
            byte[] inputBytes = readBytesFromFile(inputFile);

            byte[] hmac = Arrays.copyOfRange(inputBytes, inputBytes.length - 32, inputBytes.length);
            byte[] inputBytesNoHmac = Arrays.copyOfRange(inputBytes, 0, inputBytes.length - 32);

            //inputBytes[3] = 'c'; Data will be tampered

            byte[] calculatedHmac = createHmac(key, inputBytesNoHmac);

            if (MessageDigest.isEqual(hmac, calculatedHmac))
            {
                Key secretKey = new SecretKeySpec(key, ALGORITHM);
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);

                IvParameterSpec ivParams = createIv(cipher);

                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);

                byte[] outputBytes = cipher.doFinal(inputBytesNoHmac);

                writeBytesToFile(outputFile, outputBytes);
            }
            else
            {
                System.out.println("Data has been tampered");
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
}
