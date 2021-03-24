package oliv.scratch;

import java.util.Map;

public class ScratchPad {

    public static void main(String... args) {
        Map<String, String> map = Map.of("Key", "value", "Akeu", "Coucou");
        map.keySet().forEach(k -> System.out.printf("%s -> %s %n", k, map.get(k)));
    }
}
