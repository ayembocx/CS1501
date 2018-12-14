public class Apartment implements HasKey {
    public String address;
    public String apartmentNo;
    public String city;
    public int zip;
    public double rent;
    public int sqft;

    private String key = null;

    public Apartment(String address, String aptNo, String city, String zip, String rent, String sqft) {
        this.address = address;
        this.apartmentNo = aptNo;
        this.city = city;
        this.zip = Integer.parseInt(zip);
        this.rent = Double.parseDouble(rent);
        this.sqft = Integer.parseInt(sqft);
    }

    public Apartment(String address, String aptNo, String city, int zip, double rent, int sqft) {
        this.address = address;
        this.apartmentNo = aptNo;
        this.city = city;
        this.zip = zip;
        this.rent = rent;
        this.sqft = sqft;
    }

    @Override
    public String toString() {
        return String.format("$: %6.1f FT: %4d \t [%s]", rent, sqft, address);
    }

    public String toPrettyString() {
        return String.format("%s #%s\n%s %s\n$%.2f per month \n%d square feet", address, apartmentNo, city, zip, rent, sqft);
    }

    public String getKey() {
        if(key == null) key = generateKey(address, apartmentNo, city, ""+zip);
        return key;
    }

    public static String generateKey(String address, String apartmentNo, String city, String zip) {
        return (address.trim() + apartmentNo.trim() + city.trim() + zip.trim()).toLowerCase();
    }
}