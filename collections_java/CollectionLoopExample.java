import java.util.*;  // Import all collection classes

public class CollectionLoopExample {
    public static void main(String[] args) {
        
        List<String> fruits = new ArrayList<>();
        fruits.add("Apple");
        fruits.add("Banana");
        fruits.add("Cherry");
        fruits.add("Date");
        
        for (int i = 0; i < fruits.size(); i++) {
            System.out.println("Element at " + i + ": " + fruits.get(i));
        }
        
        for (String fruit : fruits) {
            System.out.println("Fruit: " + fruit);
        }
        
        Iterator<String> it = fruits.iterator();
        while (it.hasNext()) {
            String f = it.next();
            System.out.println("Iterator: " + f);
        }
        
        fruits.forEach(fruit -> System.out.println("Lambda: " + fruit));
        
        Map<String, Integer> ages = new HashMap<>();
        ages.put("Alice", 25);
        ages.put("Bob", 30);
        ages.put("Charlie", 35);
        
        for (String name : ages.keySet()) {
            System.out.println("Key: " + name);
        }
        for (Integer age : ages.values()) {
            System.out.println("Age: " + age);
        }
        
        for (Map.Entry<String, Integer> entry : ages.entrySet()) {
            System.out.println(entry.getKey() + " is " + entry.getValue() + " years old");
        }
        
        ages.forEach((name, age) -> System.out.println(name + " -> " + age));
    }
}