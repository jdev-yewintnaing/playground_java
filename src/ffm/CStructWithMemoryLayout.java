package ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

public class CStructWithMemoryLayout {

    public static void main(String[] args) {

        simpleStructLayout();
        slicingAllocator();
        sliceMemorySegment();

    }

    private static void sliceMemorySegment() {
        try (Arena arena = Arena.ofShared()) {
            MemoryLayout fractionLayout = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("numerator"),
                ValueLayout.JAVA_INT.withName("denominator")
                                                                   );

            long fractionSize = fractionLayout.byteSize();
            int amount = 20;

            // Allocate memory for 20 fractions in a contiguous memory block
            MemorySegment fractionsSegment = arena.allocate(fractionLayout.byteSize() * amount);

            // VarHandles to access the fields
            VarHandle numeratorHandle = fractionLayout.varHandle(MemoryLayout.PathElement.groupElement("numerator"));
            VarHandle denominatorHandle = fractionLayout.varHandle(MemoryLayout.PathElement.groupElement("denominator"));

            // Initialize fractions with sample values (e.g., (1/2), (2/3), etc.)
            for (int i = 0; i < amount; i++) {
                MemorySegment fractionSlice = fractionsSegment.asSlice(i * fractionSize, fractionSize);
                numeratorHandle.set(fractionSlice, 0,  i + 1);
                denominatorHandle.set(fractionSlice, 0, (i + 2));
            }

            for (int i = 0; i < amount; i++) {
                MemorySegment fractionSlice = fractionsSegment.asSlice(i * fractionSize, fractionSize);
                int numerator = (int) numeratorHandle.get(fractionSlice, 0);
                int denominator = (int) denominatorHandle.get(fractionSlice, 0);
                System.out.println("Fraction " + i + ": " + numerator + "/" + denominator);
            }
        }
    }

    private static void slicingAllocator() {
        try (Arena arena = Arena.ofConfined()) {
            // Define the layout for a fraction (two integers: numerator and denominator)
            MemoryLayout fractionLayout = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("numerator"),
                ValueLayout.JAVA_INT.withName("denominator")
                                                                   );


            // Create a sequence layout for storing 20 fractions
            SequenceLayout fractionArrayLayout = MemoryLayout.sequenceLayout(20, fractionLayout);

            // Allocate memory for 20 fractions
            MemorySegment segment = arena.allocate(fractionArrayLayout);
            SegmentAllocator allocator = SegmentAllocator.slicingAllocator(segment);

            // Create an array of MemorySegments for each fraction
            MemorySegment[] fractions = new MemorySegment[20];

            // VarHandles to access the fields
            VarHandle numeratorHandle = fractionLayout.varHandle(MemoryLayout.PathElement.groupElement("numerator"));
            VarHandle denominatorHandle = fractionLayout.varHandle(MemoryLayout.PathElement.groupElement("denominator"));

            // Initialize fractions with values (e.g., (1/2), (3/4), etc.)
            for (int i = 0; i < 20; i++) {
                fractions[i] = allocator.allocate(fractionLayout);
                numeratorHandle.set(fractions[i],0, i + 1);
                denominatorHandle.set(fractions[i],0, (i + 2));
            }

            MemorySegment resultFraction = arena.allocate(fractionLayout);

            for (int i = 0; i < 20; i+=2) {

                int n1 = (int) numeratorHandle.get(fractions[i], 0);
                int d1 = (int) denominatorHandle.get(fractions[i], 0);
                int n2 = (int) numeratorHandle.get(fractions[i+1], 0);
                int d2 = (int) denominatorHandle.get(fractions[i+1], 0);

                int resultNumerator = (n1 * d2) + (n2 * d1);
                int resultDenominator = d1 * d2;

                // Store the result in resultFraction
                numeratorHandle.set(resultFraction, 0L, resultNumerator);
                denominatorHandle.set(resultFraction, 0L, resultDenominator);

                // Retrieve and print the result
                System.out.println("Result Fraction: " +
                                       numeratorHandle.get(resultFraction, 0L) + "/" +
                                       denominatorHandle.get(resultFraction, 0L));
            }
        }

    }

    private static void simpleStructLayout() {

        try (Arena arena = Arena.ofConfined()) {

            MemoryLayout fractionLayout = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("numerator"),
                ValueLayout.JAVA_INT.withName("denominator"));

            MemorySegment fraction1 = arena.allocate(fractionLayout);
            MemorySegment fraction2 = arena.allocate(fractionLayout);
            MemorySegment resultFraction = arena.allocate(fractionLayout);

            VarHandle numeratorHandle = fractionLayout.varHandle(MemoryLayout.PathElement.groupElement("numerator"));
            VarHandle denominatorHandle =
                fractionLayout.varHandle(MemoryLayout.PathElement.groupElement("denominator"));

            numeratorHandle.set(fraction1, 0, 1);
            denominatorHandle.set(fraction1, 0, 3);

            numeratorHandle.set(fraction2, 0, 1);
            denominatorHandle.set(fraction2, 0, 2);

            int n1 = (int) numeratorHandle.get(fraction1, 0);
            int d1 = (int) denominatorHandle.get(fraction1, 0);
            int n2 = (int) numeratorHandle.get(fraction2, 0);
            int d2 = (int) denominatorHandle.get(fraction2, 0);

            // Add fractions: resultNumerator = (n1 * d2) + (n2 * d1);
            int resultNumerator = (n1 * d2) + (n2 * d1);
            int resultDenominator = d1 * d2;

            // Store the result in resultFraction
            numeratorHandle.set(resultFraction, 0, resultNumerator);
            denominatorHandle.set(resultFraction, 0, resultDenominator);

            System.out.println("Result Fraction: " +
                                   numeratorHandle.get(resultFraction, 0L) + "/" +
                                   denominatorHandle.get(resultFraction, 0L));
        }
    }

}
