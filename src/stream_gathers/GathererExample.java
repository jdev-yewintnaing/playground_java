package stream_gathers;

import java.util.*;
import java.util.stream.*;

public class GathererExample {
    public static void main(String[] args) {
        // Input list of integers
        List<Integer> input = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // Create a FixedWindowGatherer with a window size of 3
        FixedWindowGatherer gatherer = new FixedWindowGatherer(3);

        // Initialize the state
        List<Integer> state = gatherer.initializer().get();

        // Simulate downstream processing
        List<List<Integer>> results = new ArrayList<>();
        Gatherer.Downstream<List<Integer>> downstream = results::add;

        // Process each element in the input
        for (Integer element : input) {
            boolean continueProcessing = gatherer.integrator().integrate(state, element, downstream);
            if (!continueProcessing) break;
        }

        // Finalize the gatherer
        gatherer.finisher().accept(state, downstream);

        // Print the results
        System.out.println(results); // Output: [[1, 2, 3], [4, 5, 6], [7, 8, 9]]


        List<String> inputForDistinctByLengthExample = List.of("cat", "dog", "fish", "bear", "shark");

        DistinctByLengthGatherer distinctByLengthGatherer = new DistinctByLengthGatherer();

        // Initialize state
        Set<Integer> state1 = distinctByLengthGatherer.initializer().get();

        // Simulate downstream handling
        List<String> results1 = new ArrayList<>();
        Gatherer.Downstream<String> downstreamForDistinctByLength = results1::add;

        // Process each element
        for (String element : inputForDistinctByLengthExample) {
            boolean continueProcessing = distinctByLengthGatherer.integrator().integrate(state1, element, downstreamForDistinctByLength);
            if (!continueProcessing) break;
        }

        // Finalize (if necessary)
        distinctByLengthGatherer.finisher().accept(state1, downstreamForDistinctByLength);

        // Print results
        System.out.println(results1); // Output: [cat, fish, shark]


        var streamGatherOutput = inputForDistinctByLengthExample.stream()
                .gather(distinctByLengthGatherer);

        streamGatherOutput.toList()
                .forEach(System.out::println);
    }
}