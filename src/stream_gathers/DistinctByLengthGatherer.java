package stream_gathers;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

class DistinctByLengthGatherer implements Gatherer<String, Set<Integer>, String> {
    @Override
    public Supplier<Set<Integer>> initializer() {
        // Initializes an empty set to track string lengths
        return HashSet::new;
    }

    @Override
    public Integrator<Set<Integer>, String, String> integrator() {
        // Integrates each string into the set based on length
        return (state, element, downstream) -> {
            int length = element.length();
            if (state.add(length)) { // Add length to the set if not already present
                return downstream.push(element); // Push downstream if new
            }
            return true; // Continue processing
        };
    }

    @Override
    public BinaryOperator<Set<Integer>> combiner() {
        // Combines two sets of lengths (parallel processing support)
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
    }

    @Override
    public BiConsumer<Set<Integer>, Downstream<? super String>> finisher() {
        // End-of-stream logic (no-op for this example)
        return (state, downstream) -> {
            // No additional processing needed at the end
        };
    }
}
