import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileUtils
{
    public static byte[] readBytesFromFile(File inputFile) throws FileNotFoundException
    {
        FileInputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            return inputBytes;
        }
        catch (FileNotFoundException e1)
        {
            throw new FileNotFoundException();
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
}
