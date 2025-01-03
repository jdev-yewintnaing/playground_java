package ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class MemorySegmentWithArena {

    public static void main(String[] args) {
        String pattern = "testing";
        MemorySegment nativeString;
        try (Arena arena = Arena.ofConfined()) {

            // Allocate off-heap memory and copy the argument, a Java string, into off-heap memory
            nativeString = arena.allocateFrom(pattern);

            for (int i = 0; i < pattern.length(); i++ ) {
                // Exception in thread "main" java.lang.IllegalStateException: Already closed
                System.out.print((char)nativeString.get(ValueLayout.JAVA_BYTE, i));
            }

        } // Off-heap memory is deallocated

        // Access the off-heap memory after the arena is closed
        // and it will throw an exception with the message "Already closed"
//        for (int i = 0; i < pattern.length(); i++ ) {
//            // Exception in thread "main" java.lang.IllegalStateException: Already closed
//            System.out.println((char)nativeString.get(ValueLayout.JAVA_BYTE, i));
//        }

        System.out.println();
        System.out.println("====================================");
        // example of allocating memory and writing to it
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(3); // Allocate 3 bytes

            // Write data to the memory
            segment.set(ValueLayout.JAVA_BYTE, 0, (byte) 65); // 'A'
            segment.set(ValueLayout.JAVA_BYTE, 1, (byte) 66); // 'B'
            segment.set(ValueLayout.JAVA_BYTE, 2, (byte) 67); // 'C'

            // Read and print the data
            for (int i = 0; i < 3; i++) {
                System.out.println((char) segment.get(ValueLayout.JAVA_BYTE, i));
            }
        }

        System.out.println();
        System.out.println("====================================");
        // example of allocating memory and writing to it with Integer layout
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_INT.byteSize() * 2); // Allocate 4 bytes

            // Store an integer
            segment.set(ValueLayout.JAVA_INT, 0, 123456);
            segment.set(ValueLayout.JAVA_INT, 4, 123456);

            // Retrieve the integer
            int value = segment.get(ValueLayout.JAVA_INT, 0);
            System.out.println(value); // Output: 123456

            int value1 = segment.get(ValueLayout.JAVA_INT, 4);
            System.out.println(value1); // Output: 123456
        }


        System.out.println();
        System.out.println("====================================");
        // example of allocating memory and writing to it with Long layout
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_LONG.byteSize()); // Allocate 8 bytes

            // Store a long value
            segment.set(ValueLayout.JAVA_LONG, 0, 9876543210L);

            // Retrieve the long value
            long value = segment.get(ValueLayout.JAVA_LONG, 0);
            System.out.println(value); // Output: 9876543210
        }

        System.out.println();
        System.out.println("====================================");
        // example of allocating memory and writing to it with Float layout
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_FLOAT.byteSize()); // Allocate 4 bytes

            // Store a float value
            segment.set(ValueLayout.JAVA_FLOAT, 0, 3.14f);

            // Retrieve the float value
            float value = segment.get(ValueLayout.JAVA_FLOAT, 0);
            System.out.println(value); // Output: 3.14
        }

        System.out.println();
        System.out.println("====================================");
        // example of allocating memory and writing to it with Double layout
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_DOUBLE.byteSize()); // Allocate 8 bytes

            // Store a double value
            segment.set(ValueLayout.JAVA_DOUBLE, 0, 3.14159);

            // Retrieve the double value
            double value = segment.get(ValueLayout.JAVA_DOUBLE, 0);
            System.out.println(value); // Output: 3.14159
        }

        System.out.println();
        System.out.println("====================================");
        // example of allocating memory and writing to it with Char layout
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_CHAR.byteSize()); // Allocate 2 bytes

            // Store a character
            segment.set(ValueLayout.JAVA_CHAR, 0, 'Z');

            // Retrieve the character
            char value = segment.get(ValueLayout.JAVA_CHAR, 0);
            System.out.println(value); // Output: Z
        }






    }
}
