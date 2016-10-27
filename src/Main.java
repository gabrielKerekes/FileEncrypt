import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Scanner;

public class Main
{
    public static void mainMenu()
    {
        boolean shouldRun = true;
        while (shouldRun)
        {
            System.out.println("Zvolte moznost:");
            System.out.println("1. Sifrovat");
            System.out.println("2. Desifrovat");
            System.out.println("3. Vygenerovat kluc");
            System.out.println("4. Test na gigovom subor");
            System.out.println("0. Ukoncit");
            System.out.println("Vasa volba: ");

            Scanner in = new Scanner(System.in);

            int num = 0;
            try
            {
                num = in.nextInt();
            }
            catch (Exception e)
            {
                System.out.println("Nespravny vstup");
            }

            switch (num)
            {
                case 1:
                    encrypt();
                    break;

                case 2:
                    decrypt();
                    break;

                case 3:
                    generateKey();
                    break;

                case 4:
                    gigaFileTest();
                    break;

                case 0:
                    shouldRun = false;
                    break;

                default:
                    break;
            }
        }
    }

    public static void encrypt()
    {
        Scanner in = new Scanner(System.in);

        try
        {

            System.out.println("Zadajte meno suboru na zasifrovanie: ");
            String fileNameToEncrypt = in.nextLine();

            System.out.println("Zadajte meno suboru, do ktoreho sa ulozi zasifrovany text: ");
            String fileNameToSaveEncrypted = in.nextLine();

            System.out.println("Zadajte meno suboru s klucom: ");
            String keyFileName = in.nextLine();

            File keyFile = new File(keyFileName);
            byte[] key = FileUtils.readBytesFromFile(keyFile);

            File inputFile = new File(fileNameToEncrypt);
            File outputFile = new File(fileNameToSaveEncrypted);

            CryptoClass.encrypt(key, inputFile, outputFile);
            System.out.println("Subor bol uspesne zasifrovany a ulozeny do " + fileNameToSaveEncrypted);
        }
        catch (FileNotFoundException e1)
        {
            System.out.println("Niektory zo suborov nebol najdeny");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void decrypt()
    {
        Scanner in = new Scanner(System.in);

        try
        {

            System.out.println("Zadajte meno suboru na desifrovanie: ");
            String fileNameToDecrypt = in.nextLine();
            System.out.println("Zadajte meno suboru, do ktoreho sa ulozi desifrovany text: ");
            String fileNameToSaveDecrypted = in.nextLine();
            System.out.println("Zadajte meno suboru s klucom: ");
            String keyFileName = in.nextLine();

            File keyFile = new File(keyFileName);
            byte[] key = FileUtils.readBytesFromFile(keyFile);

            File inputFile = new File(fileNameToDecrypt);
            File outputFile = new File(fileNameToSaveDecrypted);

            CryptoClass.decrypt(key, inputFile, outputFile);
            System.out.println("Subor bol uspesne desifrovany a ulozeny do " + fileNameToSaveDecrypted);
        }
        catch (FileNotFoundException e1)
        {
            System.out.println("Niektory zo suborov nebol najdeny");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void generateKey()
    {
        Scanner in = new Scanner(System.in);

        System.out.println("Zadajte meno suboru, do ktoreho ma byt ulozeny kluc: ");
        String keyFileName = in.nextLine();

        SecureRandom random = null;
        try
        {
            random = SecureRandom.getInstance("SHA1PRNG");

            byte randomKeyBytes[] = new byte[16];
            random.nextBytes(randomKeyBytes);

            File keyFile = new File(keyFileName);

            FileUtils.writeBytesToFile(keyFile, randomKeyBytes);

            System.out.println("Kluc bol vygenerovany a ulozeny do suboru: " + keyFileName);
        }
        catch (NoSuchAlgorithmException e1)
        {
            e1.printStackTrace();
        }
    }

    public static void gigaFileTest()
    {
        Scanner in = new Scanner(System.in);
        System.out.println("Zadajte nazov noveho suboru: ");
        String gigaFileName = in.nextLine();

        File file = new File(gigaFileName);
        RandomAccessFile gigaFile = null;
        try
        {
            System.out.println("Vytvaranie gigoveho suboru...");
            gigaFile = new RandomAccessFile(file, "rw");
            gigaFile.setLength(1024*1024*1024); //1024*1024*1024 for 1 GB
            System.out.println("Gigovy subor vytvoreny...");
        }
        catch (IOException e)
        {
            System.out.println("Vytvorenie gigoveho suboru zlyhalo");
            e.printStackTrace();
        }
        System.out.println("Vytvaranie nahodneho kluca...");
        SecureRandom random = null;
        try
        {
            random = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e1)
        {
            e1.printStackTrace();
        }
        byte randomKeyBytes[] = new byte[16];
        random.nextBytes(randomKeyBytes);
        System.out.println("Sifrovanie gigoveho suboru...");
        File outputFile = new File("gigaOutput.txt");
        long startTime = System.currentTimeMillis();
        try
        {
            CryptoClass.encrypt(randomKeyBytes, file, outputFile);
            long endTime = System.currentTimeMillis();
            System.out.println("Subor zasifrovany. Trvanie: " + (endTime-startTime) + " milisekund.");
        }
        catch (CryptoException e)
        {
            e.printStackTrace();
        }
        catch (OutOfMemoryError e2)
        {
            System.out.println("Prilis velky subor, alebo ste nezadali parametre -Xms1g -Xmx5g. PRERUŠENÉ.");
        }
    }

    public static void main(String[] args)
    {
        mainMenu();
    }
}
