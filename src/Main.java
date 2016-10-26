import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Scanner;

/*
 * Ako funguje integrita:
 * Vo funkcii encrypt sa subor zasifruje a potom sa pomocou nahodnym klucom a tymto
 * zasifrovanym suborov vykona mac funkcia. Vystup sa zachova ako hmacEncrypt staticka premenna
 *
 * Pri desifrovani sa najrpv vstupny subor (zasifrovany originalny subor) spolu s nahodnym klucom
 * vlozia do vstupu mac funkcie. Ak sa zachovala integrita, tak vystpu z mac funkcie by mal byt
 * rovnaky ako hmacEncrypt.
 *
 *
 */

// todo: rozumne try catch vsade asi treba
// todo: if file name empty use example file
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
            // todo: mozno dat prec - naco? nech si tam on da nieco 1GB ked tak
            System.out.println("4. Vygenerovat gigovy subor");
            System.out.println("0. Ukoncit");
            System.out.println("Vasa volba: ");

            Scanner in = new Scanner(System.in);
            int num = in.nextInt();

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

        System.out.println("Zadajte meno suboru na zasifrovanie: ");
        String fileNameToEncrypt = in.nextLine();

        System.out.println("Zadajte meno suboru, do ktoreho sa ulozi zasifrovany text: ");
        String fileNameToSaveEncrypted = in.nextLine();

        System.out.println("Zadajte meno suboru s klucom: ");
        String keyFileName = in.nextLine();

        File keyFile = new File(keyFileName);
        byte[] key = CryptoClass.readBytesFromFile(keyFile);

        File inputFile = new File(fileNameToEncrypt);
        File outputFile = new File(fileNameToSaveEncrypted);

        try
        {
            CryptoClass.encrypt(key, inputFile, outputFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void decrypt()
    {
        Scanner in = new Scanner(System.in);

        System.out.println("Zadajte meno suboru na desifrovanie: ");
        String fileNameToDecrypt = in.nextLine();
        System.out.println("Zadajte meno suboru, do ktoreho sa ulozi desifrovany text: ");
        String fileNameToSaveDecrypted = in.nextLine();
        System.out.println("Zadajte meno suboru s klucom: ");
        String keyFileName = in.nextLine();

        File keyFile = new File(keyFileName);
        byte[] key = CryptoClass.readBytesFromFile(keyFile);

        File inputFile = new File(fileNameToDecrypt);
        File outputFile = new File(fileNameToSaveDecrypted);

        try
        {
            CryptoClass.decrypt(key, inputFile, outputFile);
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
        }
        catch (NoSuchAlgorithmException e1)
        {
            e1.printStackTrace();
        }
        byte randomKeyBytes[] = new byte[16];
        random.nextBytes(randomKeyBytes);

        File keyFile = new File(keyFileName);

        CryptoClass.writeBytesToFile(keyFile, randomKeyBytes);

        System.out.println("Kluc bol vygenerovany a ulozeny do suboru: " + keyFileName);
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
            gigaFile.setLength(1024*1024*50); //1024*1024*1024 for 1 GB
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
        System.out.println("Sifrovane gigoveho suboru...");
        File outputFile = new File("gigaOutput.txt");
        long startTime = System.currentTimeMillis();
        try {
            CryptoClass.encrypt(randomKeyBytes, file, outputFile);
        } catch (CryptoException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Subor zasifrovany. Trvanie: " + (endTime-startTime));
    }

    public static void main(String[] args)
    {
        //IMPORTANT!! = java -Xms1g -Xmx5g // run with these parameters if file is 1GB
        mainMenu();
    }
}
