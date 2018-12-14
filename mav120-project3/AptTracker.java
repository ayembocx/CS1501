import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class AptTracker {
    static ApartmentPQ pq = new ApartmentPQ();
    static Scanner scanner = new Scanner(System.in);

    public static void loadFromFile() {
        System.out.print("Loading Apartments....");
        try {
            BufferedReader in = new BufferedReader(new FileReader("apartments.txt"));
            String line;
            while((line = in.readLine()) != null) {
                if(line.charAt(0) == '#') continue;
                String[] info = line.split(":");
                Apartment a = new Apartment(info[0], info[1], info[2], info[3], info[4], info[5]);
                pq.add(a);
            }

        } catch(FileNotFoundException e) {
            System.out.println("Could not load [apartments.txt] file.");
        } catch(IOException e) {
            System.out.println("Error processing [apartments.txt] file.");
        }
        System.out.print("\n\n\n");

    }

    public static void showOptions() {
        System.out.println("\nType in a number for the action you wish to perform. Type anything else to exit. ");
        System.out.println("\t1. Add an apartment ");
        System.out.println("\t2. Update an apartment");
        System.out.println("\t3. Remove apartment");
        System.out.println("\t4. Show lowest price apartment");
        System.out.println("\t5. Show highest square footage apartment");
        System.out.println("\t6. Show lowest price apartment of a city. ");
        System.out.println("\t7. Show highest square footage apartment of a city. \n");
    }

    public static int getUserOption() {
        System.out.print("Enter option: ");
        String opt = scanner.nextLine();

        char num = opt.charAt(0);
        if(opt.length() == 1 && "123456789".indexOf(num) > -1) {
            return Character.getNumericValue(num);
        }

        return -1;
    }

    public static void performAction(int opt) {
        switch(opt) {
            case 1:
                addApartment();
                break;
            case 2:
                updateApartment();
                break;
            case 3:
                removeApartment();
                break;
            case 4:
                showLowestPrice();
                break;
            case 5:
                showHighestSqft();
                break;
            case 6:
                showLowestPriceCity();
                break;
            case 7:
                showHighestSqftCity();
                break;
            default:
                System.out.println("Incorrect option.");
        }

        System.out.println("\n");

    }

    private static String askForCity() {
        System.out.print("Enter city ");
        String city = scanner.nextLine();
        return pq.isValidCity(city) ? city : null;
    }

    private static void showLowestPriceCity() {
        String city = askForCity();
        if(city == null) {
            System.out.println("No apartments in that city!");
        } else {
            Apartment apt = pq.getMinRentByCity(city);
            System.out.println("\nThis is the lowest priced apartment in " + city + ": \n" + apt.toPrettyString());
        }
    }

    private static void showHighestSqftCity() {
        String city = askForCity();
        if(city == null) {
            System.out.println("No apartments in that city!");
        } else {
            Apartment apt = pq.getMaxSqftByCity(city);
            System.out.println("\nThis is the apartment with the most square footage in " + city + ": \n" + apt.toPrettyString());
        }
    }

    private static void showHighestSqft() {
        Apartment apt = pq.getMaxSqft();
        System.out.println("\nThis is the apartment with the most square footage: \n" + apt.toPrettyString());
    }

    private static void showLowestPrice() {
        Apartment apt = pq.getMinRent();
        System.out.println("\nThis is the apartment with the lowest rent: \n" + apt.toPrettyString());
    }

    private static void removeApartment() {
        Apartment apt = askForApartmentKey();
        System.out.println(apt.toPrettyString());
        System.out.printf("\n(y/n) Would you like to remove this apartment?", apt.rent);
        String answer = scanner.nextLine();
        if(answer.length() < 1) answer = "n";
        if('y' == Character.toLowerCase(answer.charAt(0))) {
            pq.remove(apt.getKey());
            System.out.println("\nRemoved!");
        }
    }

    private static Apartment askForApartmentKey() {
        String key =  "";
        Apartment apt = null;

        while(apt == null) {
            System.out.print("Enter street address of apartment ");
            String address = scanner.nextLine();
            System.out.print("Enter apartment number ");
            String aptno = scanner.nextLine();
            System.out.print("Enter city ");
            String city = scanner.nextLine();
            System.out.print("Enter zip ");
            String zip = scanner.nextLine();

            key = Apartment.generateKey(address, aptno, city, zip);
            apt = pq.get(key);
            if(apt == null) {
                System.out.println("\n\nApartment not found. Try again.\n\n");
            }
        }
        return apt;
    }

    private static void updateApartment() {
        Apartment apt = askForApartmentKey();
        System.out.printf("\n(y/n) Would you like to update the rent? It is currently %.2f per month. ", apt.rent);
        String answer = scanner.nextLine();
        if(answer.length() < 1) answer = "n";
        if('y' == Character.toLowerCase(answer.charAt(0))) {
            System.out.print("Enter updated rent: ");
            double rent = Double.parseDouble(scanner.nextLine());
            pq.updateRent(apt, rent);
            System.out.println("\nUpdated!");
        }

    }

    private static void addApartment() {
        System.out.print("Enter street address (e.g. 4200 Forbes Ave.) ");
        String address = scanner.nextLine();
        System.out.print("Enter apartment number (e.g. 3061) ");
        String aptno = scanner.nextLine();
        System.out.print("Enter city (e.g. Pittsburgh) ");
        String city = scanner.nextLine();
        System.out.print("Enter zip code (e.g. 15213) ");
        String zip = scanner.nextLine();
        System.out.print("Enter price to rent (in US dollars per month) ");
        String rent = scanner.nextLine();
        System.out.print("Enter square footage of the apartment) ");
        String sqft = scanner.nextLine();
        Apartment apt = new Apartment(address, aptno, city, zip, rent, sqft);
        pq.add(apt);
        System.out.println("\n\nAdded apartment!");

    }

    public static void askOptionLoop() {
        int opt = getUserOption();
        while(opt > -1) {
            performAction(opt);
            showOptions();
            opt = getUserOption();
        }
        System.out.println("\nWould you like to exit the program? Type \"no\" for no, anything else to exit.");
        String exitStr = scanner.nextLine();
        if("no".equals(exitStr)) {
            showOptions();
            askOptionLoop();
        }
    }

    public static void startMenu() {
        System.out.println("============================================================");
        System.out.println("                 Apartment Viewing Program                  ");
        System.out.println("============================================================");
        showOptions();
        askOptionLoop();
    }

    public static void main(String[] args) {
        loadFromFile();
        startMenu();

        System.out.println("\n\nThank you for viewing the apartments!");
        scanner.close();
    }
}
