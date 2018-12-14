import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ApartmentPQ {
    private Comparator<Apartment> rentComparator = new Comparator<Apartment>() {
        @Override
        public int compare(Apartment o1, Apartment o2) {
            return (int)(o1.rent - o2.rent);
        }
    };

    private Comparator<Apartment> sqftComparator = new Comparator<Apartment>() {
        @Override
        public int compare(Apartment o1, Apartment o2) {
            return (o2.sqft - o1.sqft);
        }
    };

    private MinMaxPriorityQueue<Apartment> apartmentPQ;

    private Map<String, MinMaxPriorityQueue<Apartment>> cityMap;

    public ApartmentPQ() {
        apartmentPQ = new MinMaxPriorityQueue<>(rentComparator, sqftComparator);
        cityMap = new HashMap<String, MinMaxPriorityQueue<Apartment>>();
    }

    public void add(Apartment apt) {
        String city = apt.city;
        if(!cityMap.containsKey(city)) {
            cityMap.put(city, new MinMaxPriorityQueue<Apartment>(rentComparator, sqftComparator));
        }
        MinMaxPriorityQueue<Apartment> cityPQ = cityMap.get(city);
        cityPQ.add(apt);
        apartmentPQ.add(apt);
    }

    public Apartment get(String address) {
        return apartmentPQ.get(address);
    }

    public void remove(String address) {
        Apartment apt = get(address);
        String city = apt.city;
        cityMap.get(city).remove(address);
        apartmentPQ.remove(address);
    }

    private void update(String address, Apartment apt) {
        String city = apt.city;
        cityMap.get(city).update(address);
        apartmentPQ.update(address);
    }

    public void updateRent(Apartment apt, double rent) {
        apt.rent = rent;
        update(apt.getKey(), apt);
    }

    public Apartment getMinRent() {
        return apartmentPQ.getMin();
    }

    public Apartment getMinRentByCity(String city) {
        if(!isValidCity(city)) return null;
        return cityMap.get(city).getMin();
    }

    public Apartment getMaxSqft() {
        return apartmentPQ.getMax();
    }

    public boolean isValidCity(String city) {
        return cityMap.containsKey(city);
    }

    public Apartment getMaxSqftByCity(String city) {
        if(!isValidCity(city)) return null;
        return cityMap.get(city).getMax();
    }

    public String toString() {
        String mapStr = "";
        for(String city : cityMap.keySet()) {
            mapStr += city + " : \n" + cityMap.get(city) + "\n-----------------\n";
        }
        return "APARTMENT PQ\n===================\n" + apartmentPQ + "\n CITY MAP\n===================\n" + mapStr;
    }
}
