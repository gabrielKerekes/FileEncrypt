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

public class Main
{
    public static void mainMenu()
    {
        // todo: nejak ukoncit while
        while (true) {
            System.out.println("Zvolte moznost:");
            System.out.println("1. Sifrovat");
            System.out.println("2. Desifrovat");
            System.out.println("3. Vygenerovat kluc");
            System.out.println("Vasa volba: ");

            Scanner in = new Scanner(System.in);
            int num = in.nextInt();

            switch (num) {
                case 1:
                    encrypt();
                    break;
                case 2:
                    decrypt();
                    break;
                case 3:
                    generateKey();
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
        System.out.println("Zadajte meno suboru, do ktoreho ma byt ulozeny kluc: ");
        String keyFileName = "";

        SecureRandom randomKey = null;
        try
        {
            randomKey = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        byte randomBytesKey[] = new byte[16];
        randomKey.nextBytes(randomBytesKey);

        //todo: write key to file

        System.out.println("Kluc bol vygenerovany a ulozeny do suboru: " + keyFileName + ".key");
    }

    public static void main(String[] args)
    {
        //IMPORTANT!! = java -Xms1g -Xmx5g // run with these parameters if file is 1GB
        //String key = "Mary has two cat"; //key has to have 16bytes

        mainMenu();

        //nahodne vytvaranie kluca
        SecureRandom randomKey = null;
        try
        {
            randomKey = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        byte randomBytesKey[] = new byte[] { 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65};
        randomKey.nextBytes(randomBytesKey);


        File inputFile = new File("document.txt");

//        RandomAccessFile gigaFile;
//        try
//        {
//            gigaFile = new RandomAccessFile(inputFile, "rw");
//            gigaFile.setLength(1024); //1024*1024*1024 for 1 GB
//        }
//        catch (IOException e)
//        {
//            System.out.println("Failed while creating 1gb file");
//            e.printStackTrace();
//        }

        File encryptedFile = new File("documentEncrypted.txt");
        File decryptedFile = new File("documentDecrypted.txt");

        long start = System.nanoTime();
        try
        {
            CryptoClass.encrypt(randomBytesKey, inputFile, encryptedFile);
            CryptoClass.decrypt(randomBytesKey, encryptedFile, decryptedFile);
        }
        catch (CryptoException ex)
        {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

        long end = System.nanoTime();
        long elapsedTime = end - start;
        double seconds = (double)elapsedTime / 1000000000.0;
        System.out.println("Time in seconds: "+seconds);
    }
}
